package com.xiaoer.mobilesafe.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.view.SettingItemView;

public class SettingActivity extends AppCompatActivity {

    private SettingItemView siv_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        加载自动更新
        initUpdate();

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