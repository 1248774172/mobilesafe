package com.xiaoer.mobilesafe.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.xiaoer.mobilesafe.R;
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
        siv_update.setTitle("自动更新");
        siv_update.setChecked(true);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(siv_update.isChecked()){
                    siv_update.setChecked(false);
                }else{
                    siv_update.setChecked(true);
                }
            }
        });
    }
}