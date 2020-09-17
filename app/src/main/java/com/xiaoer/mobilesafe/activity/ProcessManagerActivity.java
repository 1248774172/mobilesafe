package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.ServiceUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.engine.ProcessProvider;
import com.xiaoer.mobilesafe.entity.ProcessInfo;
import com.xiaoer.mobilesafe.service.LockScreenClean;
import com.xiaoer.mobilesafe.view.SettingItemView;

import java.util.ArrayList;
import java.util.List;

public class ProcessManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProcessManagerActivity";
    private ListView lv_process;
    //系统进程和用户进程在一个集合中 这个是两种进程的分界线 前面是用户进程 后面是系统进程
    private List<ProcessInfo> mProcessInfos;
    private int mSeparator;
    private MyAdapter mMyAdapter;
    //避免用户在加载数据过程中就关闭了activity造成错误
    boolean isShow = true;
    private ProgressBar pb;
    private TextView tv_process_type;
    private ProcessInfo mProcessInfo;
    private TextView tv_process_count;
    private TextView tv_process_mem;
    private int mProcessCount;
    private boolean isHome = true;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                //收到数据加载完毕的消息
                mMyAdapter = new MyAdapter();
                //给ListView添加数据适配器
                lv_process.setAdapter(mMyAdapter);
                //数据加载完就可以关闭progressBar了
                pb.setVisibility(View.INVISIBLE);
                //常驻悬浮窗实现
                tv_process_type.setVisibility(View.VISIBLE);
                bt_process_selectAll.setOnClickListener(ProcessManagerActivity.this);
                bt_process_reverse.setOnClickListener(ProcessManagerActivity.this);
                bt_process_clean.setOnClickListener(ProcessManagerActivity.this);
                bt_process_set.setOnClickListener(ProcessManagerActivity.this);
            } else {
                //进程数加载完的消息
                tv_process_count.setText("进程总数:" + mProcessCount);
            }
        }
    };
    private Button bt_process_selectAll;
    private Button bt_process_reverse;
    private Button bt_process_clean;
    private Button bt_process_set;
    private SettingItemView siv_process_view;
    private SettingItemView siv_process_clean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);

        initView();

        initDate();
        Log.d(TAG, "onCreate: ----------------" + getPackageName());
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

    private void initDate() {
        Log.d(TAG, "initDate: ---------------------加载数据");
        Thread thread = new Thread() {
            @Override
            public void run() {
                mProcessInfos = ProcessProvider.getProcessInfo(getApplicationContext());
                //数据加载完后 找到用户进程和系统进程的分界线
                if (mProcessInfos.size() > 1) {
                    for (int i = 0; i < mProcessInfos.size(); i++) {
                        if (mProcessInfos.get(i).isSystem()) {
                            mSeparator = i;
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "run: ----------------------只有一个数据");
                    mSeparator = 1;
                }
                mHandler.sendEmptyMessage(0);
            }
        };
        if (isShow)
            thread.start();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        pb = findViewById(R.id.pb);
        /*
         * 进程总数：
         */
        tv_process_count = findViewById(R.id.tv_process_count);
        /*
         * 内存可用空间/内存总空间
         */
        tv_process_mem = findViewById(R.id.tv_process_mem);
        lv_process = findViewById(R.id.lv_process);
        tv_process_type = findViewById(R.id.tv_process_type);

        /*
         * 全选
         */
        bt_process_selectAll = findViewById(R.id.bt_process_selectAll);
        /*
         * 反选
         */
        bt_process_reverse = findViewById(R.id.bt_process_reverse);
        /*
         * 一键清理
         */
        bt_process_clean = findViewById(R.id.bt_process_clean);
        /*
         * 设置
         */
        bt_process_set = findViewById(R.id.bt_process_set);


        //实现常驻悬浮窗
        lv_process.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mProcessInfos != null) {
                    if (firstVisibleItem <= mSeparator) {
                        tv_process_type.setText("用户进程(" + mSeparator + ")");
                    } else {
                        tv_process_type.setText("系统进程(" + (mProcessInfos.size() - mSeparator) + ")");
                    }
                }

            }
        });
        //点击事件 选中checkBox并且在集合中更改isChecked属性
        lv_process.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb_process = view.findViewById(R.id.cb_process);
                cb_process.setChecked(!cb_process.isChecked());
                Log.d(TAG, "onItemClick: ---------------------单击了");
                if (position != 0 && position != mSeparator + 1) {
                    if (position < mSeparator + 1) {
                        mProcessInfo = mProcessInfos.get(position - 1);
                    } else {
                        mProcessInfo = mProcessInfos.get(position - 2);
                    }
                }
                //避免自己的应用被选中
                if (!mProcessInfo.getPackageName().equals(getPackageName())) {
                    mProcessInfo.setChecked(cb_process.isChecked());
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                mProcessCount = ProcessProvider.getProcessCount(getApplicationContext());
                mHandler.sendEmptyMessage(1);
            }
        }.start();
        tv_process_mem.setText(ProcessProvider.getAvailMem(this) + " 可用 | " +
                ProcessProvider.getTotalMem(this));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //全选
            case R.id.bt_process_selectAll:
                selectAll();
                break;

            //反选
            case R.id.bt_process_reverse:
                reverse();
                break;

            // 清理
            case R.id.bt_process_clean:
                clean();
                break;

            //设置
            case R.id.bt_process_set:
                //todo
                enterSetView();
                break;
        }
    }

    private void enterSetView() {
        setContentView(R.layout.process_setting_view);
        isHome = false;
        siv_process_view = findViewById(R.id.siv_process_view);
        siv_process_clean = findViewById(R.id.siv_process_clean);

        boolean aBoolean = SpUtil.getBoolean(getApplicationContext(), SpKey.SYS_PROCESS, false);
        siv_process_view.setChecked(aBoolean);
        siv_process_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siv_process_view.setChecked(!siv_process_view.isChecked());
                SpUtil.putBoolean(getApplicationContext(), SpKey.SYS_PROCESS,siv_process_view.isChecked());
                if(mMyAdapter!=null)
                    mMyAdapter.notifyDataSetChanged();
            }
        });

        boolean running = ServiceUtil.isRunning(getApplicationContext(),
                "com.xiaoer.mobilesafe.service.LockScreenClean");
        siv_process_clean.setChecked(running);
        siv_process_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siv_process_clean.setChecked(!siv_process_clean.isChecked());
                Intent intent = new Intent(getApplicationContext(), LockScreenClean.class);
                if(siv_process_clean.isChecked())
                    startService(intent);
                else
                    stopService(intent);
            }
        });

    }

    /**
     * 清理选中进程并且更新ui
     */
    @SuppressLint("SetTextI18n")
    private void clean() {
        ArrayList<ProcessInfo> processInfos = new ArrayList<>();
        for (ProcessInfo pi :
                mProcessInfos) {
            if (pi.isChecked()) {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses(pi.getPackageName());
                processInfos.add(pi);
            }
        }
        mProcessInfos.removeAll(processInfos);
        //数据更改完后 找到用户进程和系统进程的分界线
        if (mProcessInfos.size() > 1) {
            for (int i = 0; i < mProcessInfos.size(); i++) {
                if (mProcessInfos.get(i).isSystem()) {
                    mSeparator = i;
                    break;
                }
            }
        } else {
            Log.d(TAG, "clean: ----------------------只有一个数据");
            mSeparator = 1;
        }
        if (mMyAdapter != null)
            mMyAdapter.notifyDataSetChanged();
        mProcessCount -= processInfos.size();
        Toast.makeText(this, "成功清理了" + processInfos.size() + "个进程", Toast.LENGTH_SHORT).show();
        tv_process_count.setText("进程总数:" + mProcessCount);
        tv_process_mem.setText(ProcessProvider.getAvailMem(this) + " 可用 | " +
                ProcessProvider.getTotalMem(this));
    }

    /**
     * 反选
     */
    private void reverse() {
        for (ProcessInfo pi :
                mProcessInfos) {
            if (!pi.getPackageName().equals(getPackageName())) {
                pi.setChecked(!pi.isChecked());
            }
            if (mMyAdapter != null)
                mMyAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 全选
     */
    private void selectAll() {
        for (ProcessInfo pi :
                mProcessInfos) {
            if (!pi.getPackageName().equals(getPackageName())) {
                pi.setChecked(true);
            }
            if (mMyAdapter != null)
                mMyAdapter.notifyDataSetChanged();
        }
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
            boolean aBoolean = SpUtil.getBoolean(getApplicationContext(), SpKey.SYS_PROCESS, false);
            if(aBoolean)
                return mSeparator + 1;
            else
                return mProcessInfos.size() + 2;
        }

        @Override
        public ProcessInfo getItem(int position) {
            if (position == 0 || position == mSeparator + 1)
                return null;
            else if (position < mSeparator + 1)
                return mProcessInfos.get(position - 1);
            else
                return mProcessInfos.get(position - 2);
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
                    titleViewHolder.tv_app_title.setText("用户进程(" + mSeparator + ")");
                } else {
                    titleViewHolder.tv_app_title.setText("系统进程(" + (mProcessInfos.size() - mSeparator) + ")");
                }
            } else {
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.process_list_item, null);
                    viewHolder = new ViewHolder(convertView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.siv_process_icon.setBackground(getItem(position).getIcon());
                viewHolder.tv_process_name.setText(getItem(position).getName());
                if (getItem(position).isSystem()) {
                    viewHolder.tv_process_model.setText("谨慎清理");
                    viewHolder.cb_process.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.tv_process_model.setText("可以清理");
                    if (getItem(position).getPackageName().equals(getPackageName())) {
                        viewHolder.cb_process.setVisibility(View.INVISIBLE);
                        viewHolder.tv_process_model.setText("本应用");
                    }
                }
                viewHolder.cb_process.setChecked(getItem(position).isChecked());

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
        SmartImageView siv_process_icon;
        TextView tv_process_name;
        TextView tv_process_model;
        CheckBox cb_process;

        ViewHolder(View view) {
            this.view = view;
            this.siv_process_icon = view.findViewById(R.id.siv_process_icon);
            this.tv_process_name = view.findViewById(R.id.tv_process_name);
            this.tv_process_model = view.findViewById(R.id.tv_process_model);
            this.cb_process = view.findViewById(R.id.cb_process);
        }
    }

    @Override
    public void onBackPressed() {
        if (isHome)
            super.onBackPressed();
        else {
            finish();
            isHome = true;
            Intent intent = new Intent(getApplicationContext(), ProcessManagerActivity.class);
            startActivity(intent);
        }
    }
}