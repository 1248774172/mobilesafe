package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class ADActivity extends AppCompatActivity {

    private static final String TAG = "ADActivity";
    private SmartImageView siv_ad;
    private String json;
    private Button bt_stepAD;
    private TimerTask mTimerTask;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        //加载UI
        initUI();
        //申请权限
//        PermissionsUtil.checkPermissions(this,this);
        //加载广告数据
        initAD();
        //5s后自动退出广告页面
        stepAD();
        //隐藏状态栏
        StatusBarUtil.hideStatusBar(this);

        //给跳过按钮添加监听事件
        bt_stepAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerTask.cancel();
                enterHome();
                mTimerTask.cancel();
            }
        });
    }

    /**
     *  @describe 5s自动关闭广告页面
     */
    private void stepAD() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mTimerTask = new TimerTask() {
                    int i = 6;
                    @Override
                    public void run() {
                        if (i > 0) {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    bt_stepAD.setText(i + "  跳过");
                                }
                            });
                            i--;
                        } else {
                            enterHome();
                            this.cancel();
                        }
                    }
                };
                mTimer = new Timer();
                mTimer.schedule(mTimerTask, 0, 1000);

            }
        }.start();
    }

    /**
     *  @describe 进入主界面,传递json
     */
    private void enterHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        intent.putExtra("json",json);
        startActivity(intent);
        finish();
    }

    /**
     *  @describe 加载广告
     */
    private void initAD() {
//        iv_icon.setImageResource();
        String adUrl=null;
        Intent intent = getIntent();
        json = intent.getStringExtra("json");
        Log.d(TAG, "initAD: "+json);
        try {
            if(json!=null) {
                JSONObject jsonObject = new JSONObject(json);
                adUrl = jsonObject.getString("ADUrl");
            }
            siv_ad.setImageUrl(adUrl,R.drawable.ad);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *  @describe 加载UI
     */
    private void initUI() {
        siv_ad = findViewById(R.id.siv_ad);
        bt_stepAD = findViewById(R.id.bt_stepAD);
        ImageView iv_icon = findViewById(R.id.iv_icon);
    }
}