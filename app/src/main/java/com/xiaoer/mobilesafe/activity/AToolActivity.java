package com.xiaoer.mobilesafe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.engine.AddressDao;

import java.util.Objects;

public class AToolActivity extends AppCompatActivity {

    private TextView tv_sms;
    private TextView tv_lock;
    private TextView tv_number;
    private TextView tv_address;
    //用来判断当前view是具体功能还是功能列表  true代表是功能列表
    boolean b = true;
    private String mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atool);


        //初始化ui
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

    private void addLockListener() {
    }

    private void addPhoneListener() {

    }

    private void addSmsListener() {

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
}