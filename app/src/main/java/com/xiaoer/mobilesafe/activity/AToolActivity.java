package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.engine.AddressDao;
import com.xiaoer.mobilesafe.engine.CommonNumDao;
import com.xiaoer.mobilesafe.engine.SmsBackup;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class AToolActivity extends AppCompatActivity {

    private TextView tv_sms;
    private TextView tv_lock;
    private TextView tv_number;
    private TextView tv_address;
    //用来判断当前view是具体功能还是功能列表  true代表是功能列表
    boolean b = true;
    private String mNumber;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0) {
                Bundle data = msg.getData();
                int count = data.getInt("count");
                if (count != 0) {
                    Toast.makeText(getApplicationContext(), "成功备份" + count + "条短信", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "服务器繁忙，备份失败", Toast.LENGTH_SHORT).show();
                }
            }
            if(msg.what == 1){
                MyAdapter myAdapter = new MyAdapter();
                elv_commonNum.setAdapter(myAdapter);
                elv_commonNum.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+mGroupList.get(groupPosition).childList.get(childPosition).number));
                        startActivity(intent);
                        return false;
                    }
                });
            }
        }
    };
    private List<CommonNumDao.Group> mGroupList;
    private ExpandableListView elv_commonNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atool);


        //初始化ui
        initUI();

        //给归属地添加点击事件
        addAddressListener();

        //给短信备份添加点击事件
        addSmsListener();

        //给常用号码添加点击事件
        addPhoneListener();

        //给程序锁添加点击事件
        addLockListener();

    }


    private void addLockListener() {
    }

    private void addPhoneListener() {
        View view = getView(tv_number, R.layout.atool_commonnum_view);
        elv_commonNum = view.findViewById(R.id.elv_commonNum);
        new Thread(){
            @Override
            public void run() {
                CommonNumDao commonNumDao = new CommonNumDao();
                mGroupList = commonNumDao.getGroup(getApplicationContext());
                mHandler.sendEmptyMessage(1);
            }
        }.start();

    }

    /**
     * 给短信备份添加点击事件
     */
    private void addSmsListener() {
        tv_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupConfirmDialog();
            }
        });

    }

    /**
     * 展示确认备份短信的对话框
     */
    private void showBackupConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("备份短信");
        builder.setMessage("是否立即备份短信？");
        builder.setPositiveButton("立即备份", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //展示备份进度
                showBackupSmsDialog();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     * 展示备份进度
     */
    private void showBackupSmsDialog() {
//        final String path = Environment.getExternalStorageDirectory().getPath()+
//                File.separator+"mobilesafe"+File.separator+"backup"+File.separator+"SmsBackup"+
//                    File.separator+"sms.xml";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.mipmap.ic_launcher);
        progressDialog.setTitle("短信备份");
        //指定进度条的样式 水平样式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        new Thread() {
            @Override
            public void run() {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

                File file = new File(directory, "sms.xml");

                int i = SmsBackup.backupSms(getApplicationContext(), file, new SmsBackup.ProgressChange() {
                    @Override
                    public void setMax(int max) {
                        progressDialog.setMax(max);
                    }

                    @Override
                    public void setIndex(int index) {
                        progressDialog.setProgress(index);
                    }
                });
                progressDialog.dismiss();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("count", i);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        }.start();
    }


    /**
     * 给指定的textview添加点击事件并且返回指定布局文件的view对象
     * @param tv textview
     * @param id 布局文件
     * @return 返回布局文件的view
     */
    private View getView(TextView tv,int id) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(id, null);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入了具体功能，b改为false
                b = false;
                //加载当前功能的布局
                setContentView(view);
            }
        });
        return view;
    }

    /**
     * 给查询归属地添加监听事件
     */
    private void addAddressListener() {
        View view = getView(tv_address, R.layout.atool_address_view);
        //初始化view中的ui
        final MaterialEditText met_address_number = view.findViewById(R.id.met_address_number);
        final Button bt_address_find = view.findViewById(R.id.bt_address_find);
        final TextView tv_address_result = view.findViewById(R.id.tv_address_result);

        //给查找按钮添加监听事件
        bt_address_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击一次按钮之后 动态查询 不必再点击按钮了
                met_address_number.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void afterTextChanged(Editable s) {
                        mNumber = Objects.requireNonNull(met_address_number.getText()).toString();
                        String address = AddressDao.getAddress(getApplicationContext(), mNumber);
                        tv_address_result.setText(address);
                    }
                });
                //焦点给到button
                met_address_number.clearFocus();
                bt_address_find.setFocusable(true);
                bt_address_find.setFocusableInTouchMode(true);
                //更新ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNumber = Objects.requireNonNull(met_address_number.getText()).toString();
                        //如果number不为空说明输出了
                        if(!TextUtils.isEmpty(mNumber)) {
                            String address = AddressDao.getAddress(getApplicationContext(), mNumber);
                            tv_address_result.setText(address);
                        }else{
                            //为空说明还没有输入 需要振动加抖动 提示用户
                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                            met_address_number.startAnimation(animation);
                            met_address_number.setError("请输入电话号！");
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(80);
                        }
                    }
                });
            }
        });
    }
    /**
     * 初始化ui
     *
     */
    private void initUI() {
        tv_address = findViewById(R.id.tv_address);
        tv_number = findViewById(R.id.tv_number);
        tv_lock = findViewById(R.id.tv_lock);
        tv_sms = findViewById(R.id.tv_sms);
    }

    @Override
    public void onBackPressed() {
        if(b)
            super.onBackPressed();
        else {
            b = true;
            setContentView(R.layout.activity_atool);
            initUI();
            //给归属地添加监听事件
            addAddressListener();
            //给短信备份添加监听事件
            addSmsListener();
            //给常用号码添加监听事件
            addPhoneListener();
            //给程序锁添加监听事件
            addLockListener();
        }
    }

    class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return mGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mGroupList.get(groupPosition).childList.size();
        }

        @Override
        public CommonNumDao.Group getGroup(int groupPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public CommonNumDao.Child getChild(int groupPosition, int childPosition) {
            return mGroupList.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText("                 "+getGroup(groupPosition).name);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setPadding(10,10,10,10);
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = View.inflate(getApplicationContext(), R.layout.commonnum_list_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            viewHolder.tv_commonNum_name.setText(getChild(groupPosition,childPosition).name);
            viewHolder.tv_commonNum_num.setText(getChild(groupPosition,childPosition).number);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    static class ViewHolder{
        View view;
        TextView tv_commonNum_name;
        TextView tv_commonNum_num;
        public ViewHolder(View view){
            this.view = view;
            tv_commonNum_name = view.findViewById(R.id.tv_commonNum_name);
            tv_commonNum_num = view.findViewById(R.id.tv_commonNum_num);
        }



    }
}