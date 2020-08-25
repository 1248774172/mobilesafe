package com.xiaoer.mobilesafe.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.ServiceUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.service.AddressService;
import com.xiaoer.mobilesafe.view.SettingItemView;

public class SettingActivity extends AppCompatActivity {

    private SettingItemView siv_update;
    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        加载自动更新
        initUpdate();
        //加载手机号码归属地显示设置
        initAddress();

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