package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.engine.AppInfoDao;
import com.xiaoer.mobilesafe.entity.AppInfo;

import java.util.List;

public class AppManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AppManagerActivity";
    private ListView lv_app;
    //避免用户在加载数据过程中就关闭了activity造成错误
    private boolean isShow = true;
    private List<AppInfo> mAppInfos;
    //系统应用和用户应用在一个集合中 这个是两种应用的分界线 前面是用户应用 后面是系统应用
    private int mSeparator;
    private TextView tv_app_type;
    private ProgressBar pb;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            //收到数据加载完毕的消息
            mMyAdapter = new MyAdapter();
            //给ListView添加数据适配器
            lv_app.setAdapter(mMyAdapter);
            //数据加载完就可以关闭progressBar了
            pb.setVisibility(View.INVISIBLE);
            //常驻悬浮窗实现
            tv_app_type.setVisibility(View.VISIBLE);
        }
    };
    private AppInfo mAppInfo;
    private PopupWindow mPopupWindow;
    private MyAdapter mMyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        initView();

        initData();
    }

    @Override
    protected void onResume() {
        isShow = true;
        Log.d(TAG, "onResume: ----------------------------重新获取焦点");
        initView();
        if(mAppInfos != null) {
            if(mAppInfo != null) {
                PackageManager packageManager = getPackageManager();
                PackageInfo pi;
                try {
                    pi = packageManager.getPackageInfo(mAppInfo.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    pi = null;
                    e.printStackTrace();
                }
                if (pi == null) {
                    Log.d(TAG, "onResume: -----------------------移除了" + mAppInfo.getPackageName());
                    mAppInfos.remove(mAppInfo);
                    for (int i = 0; i < mAppInfos.size(); i++) {
                        if (mAppInfos.get(i).getModel() == 2) {
                            mSeparator = i;
                            break;
                        }
                    }
                    if (mMyAdapter != null)
                        mMyAdapter.notifyDataSetChanged();
                }
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isShow = false;
    }

    private void initData() {
        Log.d(TAG, "initData: --------------------加载数据");
        Thread thread = new Thread() {
            @Override
            public void run() {
                mAppInfos = AppInfoDao.getAppInfo(getApplicationContext());
                for (int i = 0; i < mAppInfos.size(); i++) {
                    if (mAppInfos.get(i).getModel() == 2) {
                        mSeparator = i;
                        break;
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        };
        if (isShow) {
            thread.start();
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        TextView tv_free = findViewById(R.id.tv_free);
        TextView tv_total = findViewById(R.id.tv_total);
        pb = findViewById(R.id.pb);
        tv_app_type = findViewById(R.id.tv_app_type);
        lv_app = findViewById(R.id.lv_app);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        StatFs statFs = new StatFs(path);
        long freeBytes = statFs.getFreeBytes();
        long totalBytes = statFs.getTotalBytes();

        String formatTotalBytes = Formatter.formatFileSize(getApplicationContext(), totalBytes);
        String formatFreeBytes = Formatter.formatFileSize(getApplicationContext(), freeBytes);
        tv_free.setText("可用空间:" + formatFreeBytes);
        tv_total.setText("全部空间:" + formatTotalBytes);

        lv_app.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mAppInfos != null) {
                    if (firstVisibleItem <= mSeparator) {
                        tv_app_type.setText("用户应用(" + mSeparator + ")");
                    } else {
                        tv_app_type.setText("系统应用(" + (mAppInfos.size() - mSeparator) + ")");
                    }
                }

            }
        });

        //单机事件
        lv_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: ---------------------单击了");
                if (position != 0 && position != mSeparator + 1) {

                    if (position < mSeparator + 1) {
                        mAppInfo = mAppInfos.get(position - 1);
                    } else {
                        mAppInfo = mAppInfos.get(position - 2);
                    }
                }
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //第二个参数为包名
                Uri uri = Uri.fromParts("package", mAppInfo.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            }
        });

        //长按事件
        lv_app.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick: -----------------------长按了");
                if (position != 0 && position != mSeparator + 1) {

                    if (position < mSeparator + 1) {
                        mAppInfo = mAppInfos.get(position - 1);
                    } else {
                        mAppInfo = mAppInfos.get(position - 2);
                    }
                    showPopupWindow(view);
                }
                return true;
            }
        });
    }

    private void showPopupWindow(View view) {
        //加载弹窗布局
        View inflate = View.inflate(getApplicationContext(), R.layout.popupwindow_view, null);
        mPopupWindow = new PopupWindow(inflate, 700,
                250, true);
        //给弹窗设置背景才能被返回键关闭这个弹窗

        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        //给弹窗设置显示的位置
        mPopupWindow.showAsDropDown(view, mPopupWindow.getWidth() / 2, -view.getHeight());
        //透明动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        ///缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        //添加动画
        inflate.setAnimation(animationSet);

        /*
         * 应用详情
         */
        TextView tv_uninstall = inflate.findViewById(R.id.tv_uninstall);
        tv_uninstall.setOnClickListener(this);
        /*
         * 打开应用
         */
        TextView tv_start_app = inflate.findViewById(R.id.tv_start_app);
        tv_start_app.setOnClickListener(this);
        /*
         * 分享应用
         */
        TextView tv_share_app = inflate.findViewById(R.id.tv_share_app);
        tv_share_app.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_uninstall:
                //打开应用详情
//                    Intent intent = new Intent("android.intent.action.DELETE");
//                    intent.addCategory("android.intent.category.DEFAULT");
//                    intent.setData(Uri.parse("package:" + mAppInfo.getPackageName()));
//                    startActivity(intent);
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //第二个参数为包名
                Uri uri = Uri.fromParts("package", mAppInfo.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
            case R.id.tv_start_app:
                //开启应用
                // 通过桌面启动指定包名的应用
                PackageManager pm = getPackageManager();
                // 通过Launch开启指定包名的意图，去开启应用
                Intent intentForPackage = pm.getLaunchIntentForPackage(mAppInfo
                        .getPackageName());
                if (intentForPackage != null) {
                    startActivity(intentForPackage);
                } else {
                    Toast.makeText(getApplicationContext(),"此应用无法打开",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_share_app:
                //分享应用
                // TODO: 2020/9/9 分享应用
                break;
        }
        if(mPopupWindow!=null)
            mPopupWindow.dismiss();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getViewTypeCount() {
            //返回ListView中共有几种条目
            return super.getViewTypeCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            //规定什么位置显示什么样的条目
            if (position == 0 || position == mSeparator + 1) {
                //显示分割文字的样式
                return 0;
            } else {
                //显示应用信息的样式
                return 1;
            }
        }

        @Override
        public int getCount() {
            return mAppInfos.size() + 2;
        }

        @Override
        public AppInfo getItem(int position) {
            if (position == 0 || position == mSeparator + 1)
                return null;
            else if (position < mSeparator + 1)
                return mAppInfos.get(position - 1);
            else
                return mAppInfos.get(position - 2);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemViewType = getItemViewType(position);
            if (itemViewType == 0) {
                //应用类型分割
                TitleViewHolder titleViewHolder;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.app_list_item_title, null);
                    titleViewHolder = new TitleViewHolder(convertView);
                    convertView.setTag(titleViewHolder);
                } else {
                    titleViewHolder = (TitleViewHolder) convertView.getTag();
                }
                if (position == 0) {
                    titleViewHolder.tv_app_title.setText("用户应用(" + mSeparator + ")");
                } else {
                    titleViewHolder.tv_app_title.setText("系统应用(" + (mAppInfos.size() - mSeparator) + ")");
                }
            } else {
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.app_list_item, null);
                    viewHolder = new ViewHolder(convertView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.siv_app_icon.setBackground(getItem(position).getIcon());
                viewHolder.tv_app_name.setText(getItem(position).getName());
                int model = getItem(position).getModel();
                if (model == 1)
                    viewHolder.tv_app_model.setText("用户应用");
                else if (model == 2)
                    viewHolder.tv_app_model.setText("系统应用");
            }
            return convertView;
        }
    }

    static class TitleViewHolder {
        View view;
        TextView tv_app_title;

        TitleViewHolder(View view) {
            this.view = view;
            this.tv_app_title = view.findViewById(R.id.tv_app_title);
        }
    }

    static class ViewHolder {
        View view;
        SmartImageView siv_app_icon;
        TextView tv_app_name;
        TextView tv_app_model;

        ViewHolder(View view) {
            this.view = view;
            this.siv_app_icon = view.findViewById(R.id.siv_app_icon);
            this.tv_app_name = view.findViewById(R.id.tv_app_name);
            this.tv_app_model = view.findViewById(R.id.tv_app_model);
        }
    }
}