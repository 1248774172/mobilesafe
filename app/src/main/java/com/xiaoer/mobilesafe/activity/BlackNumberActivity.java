package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.db.dao.impl.BlackNumberDaoImpl;
import com.xiaoer.mobilesafe.entity.BlackNumberInfo;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class BlackNumberActivity extends AppCompatActivity {

    /**
     * 黑名单
     */
    private String TAG = "BlackNumberActivity";
    private ListView lv_blackNumber;
    private List<BlackNumberInfo> mBlackNumberInfo;
    private MyAdapter mMyAdapter;
    //防止在加载过程中依旧满足条件而进行多次加载  加载过程中mIsload会改成true  当加载完成之后再改为false
    private boolean mIsLoad = false;

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {


        @Override
        public void handleMessage(@NonNull Message msg) {
            if(mMyAdapter == null) {
                mMyAdapter = new MyAdapter();
                lv_blackNumber.setAdapter(mMyAdapter);
            }else {
                mMyAdapter.notifyDataSetChanged();
            }
        }
    };
    /**
     * 请输入电话号
     */
    private MaterialEditText met_blacknumber_phone;
    private RadioGroup rg_model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number);


        //初始化ui
        initView();

        //初始化数据
        initData();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        new Thread() {
            @Override
            public void run() {
                BlackNumberDaoImpl instance = BlackNumberDaoImpl.getInstance(getApplicationContext());
                //查询20条数据
                mBlackNumberInfo = instance.queryByIndex(0);
                //查好数据后告诉主线程可以加载数据适配器了
                myHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 初始化ui
     */
    private void initView() {
        lv_blackNumber = (ListView) findViewById(R.id.lv_blackNumber);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //给添加按钮增加点击事件
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                //展示添加黑名单窗口
                showAddDialog();
            }
        });

        //给ListView添加点击事件
        lv_blackNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showChangeDialog(mBlackNumberInfo.get(position).getPhone(),
                        mBlackNumberInfo.get(position).getModel());
            }
        });

        //给ListView添加滑动监听
        /*
         * 同时满足三点说明应该继续加载数据
         * 1、当前最后一个条目可见 lv_blackNumber.getLastVisiblePosition() >= mBlackNumberInfo.size() - 1
         * 2、滑动状态是空闲状态 scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
         * 3、没有在加载数据 !mIsLoad
         * */
        lv_blackNumber.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mTotalCount =BlackNumberDaoImpl.getInstance(getApplicationContext()).getTotalCount();
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(mBlackNumberInfo != null) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE &&
                            lv_blackNumber.getLastVisiblePosition() >= mBlackNumberInfo.size() - 1 &&
                                !mIsLoad) {
                        mIsLoad = true;
                        final BlackNumberDaoImpl instance = BlackNumberDaoImpl.getInstance(getApplicationContext());
                        //检查是否全部加载完了
                        if(mTotalCount > mBlackNumberInfo.size()) {
                            new Thread() {
                                @Override
                                public void run() {
                                    List<BlackNumberInfo> blackNumberInfos = instance.queryByIndex(mBlackNumberInfo.size());
                                    mBlackNumberInfo.addAll(blackNumberInfos);
                                    myHandler.sendEmptyMessage(0);
                                    mIsLoad = false;
                                }
                            }.start();
                        }else {
                            Toast.makeText(getApplicationContext(),"没有数据了",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * 展示修改拦截模式的对话框
     * @param phone 电话号
     */
    private void showChangeDialog(String phone,int model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.dialog_update_blacknumber, null);
        met_blacknumber_phone = (MaterialEditText) view.findViewById(R.id.met_blacknumber_phone);
        rg_model = (RadioGroup) view.findViewById(R.id.rg_model);
        RadioButton rb_model_sms = view.findViewById(R.id.rb_model_sms);
        RadioButton rb_model_phone = view.findViewById(R.id.rb_model_phone);
        RadioButton rb_model_all = view.findViewById(R.id.rb_model_all);
        Button bt_blacknumber_update = (Button) view.findViewById(R.id.bt_blacknumber_update);
        Button bt_blacknumber_cancel = (Button) view.findViewById(R.id.bt_blacknumber_cancel);

        //数据回显
        met_blacknumber_phone.setText(phone);
        switch (model){
            case 1:
                rb_model_sms.setChecked(true);
                break;
            case 2:
                rb_model_phone.setChecked(true);
                break;
            case 3:
                rb_model_all.setChecked(true);
                break;
        }

        //给修改按钮增加监听时间
        bt_blacknumber_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlackNumberDaoImpl instance = BlackNumberDaoImpl.getInstance(getApplicationContext());
                String phone = Objects.requireNonNull(met_blacknumber_phone.getText()).toString().trim();
                int checkedRadioButtonId = rg_model.getCheckedRadioButtonId();
                int model;   //1:拦截短信  2：拦截电话 3：拦截所有
                switch (checkedRadioButtonId) {
                    case R.id.rb_model_phone:
                        model = 2;
                        break;
                    case R.id.rb_model_all:
                        model = 3;
                        break;
                    default:
                        model = 1;
                        break;
                }

                if (instance.queryByPhone(phone)) {
                    //如果数据库中有电话号说明要更新模式
                    boolean b = instance.updateModelByPhone(phone, model);
                    if (b) {
                        Log.d(TAG, "onClick: ----------------------存在电话号，更新模式成功");
                        for (int i = 0; i < mBlackNumberInfo.size(); i++) {
                            if (mBlackNumberInfo.get(i).getPhone().equals(phone)) {
                                mBlackNumberInfo.get(i).setModel(model);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
                        if (mMyAdapter != null) {
                            Log.d(TAG, "onClick: -----------------------更新模式成功，更新数据适配器");
                            mMyAdapter.notifyDataSetChanged();
                        }
                    }
                }
                alertDialog.dismiss();
            }
        });

        //给取消按钮添加监听
        bt_blacknumber_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }

    /**
     * 展示添加黑名单窗口
     */
    private void showAddDialog() {
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.dialog_add_blacknumber, null);

        met_blacknumber_phone = (MaterialEditText) view.findViewById(R.id.met_blacknumber_phone);
        rg_model = (RadioGroup) view.findViewById(R.id.rg_model);
        Button bt_blacknumber_add = (Button) view.findViewById(R.id.bt_blacknumber_add);
        Button bt_blacknumber_cancel = (Button) view.findViewById(R.id.bt_blacknumber_cancel);

        //给添加按钮添加监听
        bt_blacknumber_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                met_blacknumber_phone.clearFocus();
                BlackNumberDaoImpl instance = BlackNumberDaoImpl.getInstance(getApplicationContext());
                String phone = Objects.requireNonNull(met_blacknumber_phone.getText()).toString().trim();
                int checkedRadioButtonId = rg_model.getCheckedRadioButtonId();
                int model;   //1:拦截短信  2：拦截电话 3：拦截所有
                switch (checkedRadioButtonId){
                    case R.id.rb_model_phone:
                        model = 2;
                        break;
                    case R.id.rb_model_all:
                        model = 3;
                        break;
                    default:
                        model = 1;
                        break;
                }
                if(instance.queryByPhone(phone)) {
                    //如果数据库中有电话号说明要更新模式
                    boolean b = instance.updateModelByPhone(phone, model);
                    if(b){
                        Log.d(TAG, "onClick: ----------------------存在电话号，更新模式成功");
                        for (int i = 0;i < mBlackNumberInfo.size();i++) {
                            if(mBlackNumberInfo.get(i).getPhone().equals(phone)){
                                mBlackNumberInfo.get(i).setModel(model);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
                        if(mMyAdapter != null) {
                            Log.d(TAG, "onClick: -----------------------更新模式成功，更新数据适配器");
                            mMyAdapter.notifyDataSetChanged();
                        }
                    }

                }else{
                    //如果数据库中没有电话号  就进入信息
                    boolean insert = instance.insert(phone, model);
                    if(insert) {
                        //插入成功后更新数据适配器的数据 然后通知数据适配器
                        BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
                        blackNumberInfo.setModel(model);
                        blackNumberInfo.setPhone(phone);
                        mBlackNumberInfo.add(0,blackNumberInfo);
                        if(mMyAdapter != null) {
                            Log.d(TAG, "onClick: -----------------------添加成功，更新数据适配器");
                            mMyAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show();
                    }
                }
                alertDialog.dismiss();
            }
        });
        //给取消按钮添加监听
        bt_blacknumber_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mBlackNumberInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return mBlackNumberInfo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.listview_blacknumber_item, null);
                vh = new ViewHolder();
                vh.tv_phone = convertView.findViewById(R.id.tv_blacknumber_phone);
                vh.tv_model = convertView.findViewById(R.id.tv_blacknumber_model);
                vh.iv_delete = convertView.findViewById(R.id.iv_delete);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.tv_phone.setText(mBlackNumberInfo.get(position).getPhone());
            int model = mBlackNumberInfo.get(position).getModel();
            switch (model) {
                case 1:
                    vh.tv_model.setText("拦截短信");
                    break;
                case 2:
                    vh.tv_model.setText("拦截电话");
                    break;
                case 3:
                    vh.tv_model.setText("拦截所有");
                    break;
            }
            //给删除按钮添加点击事件
            vh.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //展示确认删除的对话框
                    showDeleteDialog(mBlackNumberInfo.get(position).getPhone(),position);
                }
            });

            return convertView;
        }
    }

    /**
     * 展示确认删除的对话框
     * @param phone 电话号
     * @param position  条目id
     */
    private void showDeleteDialog(String phone, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定删除吗");
        builder.setIcon(R.drawable.met_ic_clear);
        builder.setMessage("您将从黑名单中移除："+phone);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BlackNumberDaoImpl instance = BlackNumberDaoImpl.getInstance(getApplicationContext());
                boolean b = instance.deleteByPhone(mBlackNumberInfo.get(position).getPhone());
                if(b) {
                    mBlackNumberInfo.remove(position);
                    Log.d(TAG, "onClick: ------------------------删除成功");
                    Toast.makeText(getApplicationContext(),"删除成功",Toast.LENGTH_SHORT).show();
                    if(mMyAdapter!=null)
                        mMyAdapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //加静态static  不会创建多个对象 优化
    private static class ViewHolder {
        TextView tv_phone;
        TextView tv_model;
        ImageView iv_delete;
    }
}