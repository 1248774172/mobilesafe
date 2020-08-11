package com.xiaoer.mobilesafe.activity;

import android.app.ActionBar;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.StatusBarUtils;
import com.xiaoer.mobilesafe.Utils.StringUtil;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private TextView tv_versionName;
    private SmartImageView siv_icon;
    private String s;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.hideStatusBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //初始化UI
        initUI();
        //初始化数据
        initDate();
        //初始化动画
        initAnimation();
        //隐藏状态栏
        StatusBarUtils.hideStatusBar(this);

    }
/**
 *  @describe 初始化动画
 */
    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 100);
        alphaAnimation.setDuration(2500);
        rl_root.startAnimation(alphaAnimation);
    }

    /**
     *  @describe 初始化数据
     */
    private void initDate() {
        Log.d(TAG, "initDate: ******************************");
        siv_icon.setImageUrl("http://10.0.2.2:8080/splash.png",R.drawable.splash);
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo("com.xiaoer.mobilesafe", 0);
            String versionName = packageInfo.versionName;
            tv_versionName.setText("当前版本："+versionName);
            getAD();//加载广告
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *  初始化UI
     */
    private void initUI() {
        tv_versionName = findViewById(R.id.tv_versionName);
        siv_icon = findViewById(R.id.siv_icon);
        rl_root = findViewById(R.id.rl_root);
        Log.d(TAG, "initUI: ****************************");
    }

    /**
     *  @describe 服务器上加载广告以及版本信息等
     */
    private void getAD() {
        new Thread() {
                @Override
                public void run () {
                    super.run();
                    try {
                        Log.d(TAG, "run: *********************************************");
//                        URL url = new URL("http://192.168.1.5:8080/config.json");
                        URL url = new URL("http://10.0.2.2:8080/config.json");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setReadTimeout(2000);
                        urlConnection.setConnectTimeout(3000);
                        if (urlConnection.getResponseCode() == 200) {
                            Log.d(TAG, "run: *********************************************");
                            InputStream inputStream = urlConnection.getInputStream();
                            s = StringUtil.stream2String(inputStream);
//                            Log.d(TAG, "run: "+ s);
                            SystemClock.sleep(5000);
                            enterAD();

                        } else {
                            Log.d(TAG, "getAD: wrong");
                            enterAD();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        enterAD();
                    } catch (IOException e) {
                        e.printStackTrace();
                        enterAD();
                    }
                }
            }.start();
        }

        /**
         *  @describe 进入广告页面
         */
    private void enterAD() {
        Intent intent = new Intent(getApplicationContext(), ADActivity.class);
        intent.putExtra("json",s);
        startActivity(intent);
        finish();
    }


}

