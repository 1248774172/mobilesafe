package com.xiaoer.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.ServiceUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.service.AddressService;
import com.xiaoer.mobilesafe.view.SettingClickView;
import com.xiaoer.mobilesafe.view.SettingItemView;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";
    private SettingItemView siv_update;
    private SettingClickView scv_address_style;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        加载自动更新
        initUpdate();
        //加载手机号码归属地显示设置
        initAddress();
        //加载手机号码归属地显示风格
        initAddressStyle();
        //加载手机号码归属地悬浮窗位置
        initAddressLocation();

    }

    /**
     * 加载手机号码归属地悬浮窗位置
     */
    private void initAddressLocation() {
        SettingClickView scv_address_location = findViewById(R.id.scv_address_location);
        scv_address_location.setBigTitle("归属地悬浮窗位置");
        scv_address_location.setSmallTitle("设置归属地悬浮窗位置");
        scv_address_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ToastLocationActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 加载手机号码归属地显示风格
     */
    private void initAddressStyle() {

        //定义背景颜色的数组
        final String [] colors = new String []{"透明","蓝色","灰色","绿色","橙色"};
        //从sp中取颜色并设置标题等信息
        int address_color = SpUtil.getInt(getApplicationContext(), SpKey.ADDRESS_COLOR, 0);
        scv_address_style = findViewById(R.id.scv_address_style);
        scv_address_style.setBigTitle("设置归属地显示风格");
        scv_address_style.setSmallTitle(colors[address_color]);
        //添加监听事件
        scv_address_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击后展示选择颜色的对话框
                showColorDialog(colors);
            }
        });
    }

    /**
     * 展示选择颜色的单选对话框
     * @param colors 存储颜色名称的数组
     */
    private void showColorDialog(final String[] colors) {
        int address_color = SpUtil.getInt(getApplicationContext(), SpKey.ADDRESS_COLOR, 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(colors, address_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //将用户选择的颜色存储在sp中
                SpUtil.putInt(getApplicationContext(),SpKey.ADDRESS_COLOR,which);
                scv_address_style.setSmallTitle(colors[which]);
                dialog.cancel();
            }
        });
        //取消按钮
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * 加载手机号码归属地显示设置
     */
    private void initAddress() {
        final SettingItemView siv_address = findViewById(R.id.siv_address);

        boolean running = ServiceUtil.isRunning(getApplicationContext(), "com.xiaoer.mobilesafe.service.AddressService");
        siv_address.setChecked(running);
        siv_address.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent intent;
                siv_address.setChecked(!siv_address.isChecked());
                if(siv_address.isChecked()){
                    Log.d(TAG, "onClick: -----------------------开启监听电话服务");
                    if (PermissionsUtil.canShowAlert(getApplicationContext())) {
                        intent = new Intent(getApplicationContext(), AddressService.class);
                        startService(intent);
                    }else{
                        Toast.makeText(getApplicationContext(),"请给予悬浮窗权限",Toast.LENGTH_SHORT).show();
                        intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 0);
                    }
                }else{
                    Log.d(TAG, "onClick: --------------------------关闭监听电话服务");
                    intent = new Intent(getApplicationContext(), AddressService.class);
                    stopService(intent);
                }
            }
        });

    }

    /**
     *  @describe 加载自动更新设置
     */
    private void initUpdate() {
        siv_update = findViewById(R.id.siv_update);
        boolean update = SpUtil.getBoolean(this, SpKey.UPDATE, false);
        siv_update.setChecked(update);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击后将状态取反
                siv_update.setChecked(!siv_update.isChecked());
                //储存状态
                SpUtil.putBoolean(getApplicationContext(), SpKey.UPDATE,siv_update.isChecked());
            }
        });
    }

}