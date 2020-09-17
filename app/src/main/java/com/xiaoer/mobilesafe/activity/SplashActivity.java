package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.StatusBarUtil;
import com.xiaoer.mobilesafe.Utils.StringUtil;
import com.xiaoer.mobilesafe.entity.Permissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private TextView tv_versionName;
    private SmartImageView siv_icon;
    private String s;
    private RelativeLayout rl_root;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.hideStatusBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        检查权限
        checkPermissions();

        //初始化UI
        initUI();
        //初始化动画
        initAnimation();
        //初始化数据
        initDate();
        //隐藏状态栏
        StatusBarUtil.hideStatusBar(this);

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
    @SuppressLint("SetTextI18n")
    private void initDate() {
        Log.d(TAG, "initDate: ----------------开始初始化数据");
        siv_icon.setImageUrl("http://10.0.2.2:8080/splash.png",R.drawable.splash);
        PackageManager packageManager = getPackageManager();
        try {
            //获取版本名称
            PackageInfo packageInfo = packageManager.getPackageInfo("com.xiaoer.mobilesafe", 0);
            String versionName = packageInfo.versionName;
            tv_versionName.setText("当前版本："+versionName);
            //加载数据库
            initDB("address.db");
            initDB("commonnum.db");

            //加载广告
            getAD();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拷贝数据库
     * @param dbName 数据库名称
     */
    private void initDB(String dbName){
        InputStream open = null;
        FileOutputStream fos = null;
        try {
//            创建数据库文件
            File filesDir = getFilesDir();
            File file = new File(filesDir, dbName);
//            如果数据库文件存在，就不用拷贝了
            if(file.exists()) {
                Log.d(TAG, "initAddressDB: --------------------------------------数据库已经存在！");
                return;
            }
//            获取资产文件中的数据库
            open = getAssets().open(dbName);
//            open = Objects.requireNonNull(getClass().getClassLoader()).getResourceAsStream("assets/"+dbName);
            fos = new FileOutputStream(file);
            byte [] bytes = new byte[1024];
            int len = -1;
            while((len = open.read(bytes)) != -1){
                fos.write(bytes,0,len);
            }
            Log.d(TAG, "initAddressDB: --------------------------------拷贝数据库完成");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if( fos!=null)
                    fos.close();
                if( open!=null)
                    open.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *  初始化UI
     */
    private void initUI() {
        tv_versionName = findViewById(R.id.tv_versionName);
        siv_icon = findViewById(R.id.siv_icon);
        rl_root = findViewById(R.id.rl_root);
        Log.d(TAG, "initUI: -----------------------------ui初始化完成");
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
                        Log.d(TAG, "run: ---------------------开启子线程获取服务器上的json信息");
//                        URL url = new URL("http://192.168.1.5:8080/config.json");
                        URL url = new URL("http://10.0.2.2:8080/config.json");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setReadTimeout(2000);
                        urlConnection.setConnectTimeout(3000);
                        if (urlConnection.getResponseCode() == 200) {
                            InputStream inputStream = urlConnection.getInputStream();
                            s = StringUtil.stream2String(inputStream);
                            Log.d(TAG, "run: --------------------------------------------"+ s);
                        } else {
                            Log.d(TAG, "getAD: ------------------------------广告信息获取失败");
                        }
//                        enterAD();
                        SystemClock.sleep(3000);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        enterAD();
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

    /**
     * 检查权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkPermissions() {
        //检查权限
        PermissionsUtil.getInstance().checkPermissions(this, Permissions.permissions, new PermissionsUtil.IPermissionsResult() {
            @Override
            public void passPermissons() {
                Log.d(TAG, "passPermissons: --------------------------权限通过，进入系统");
                enterAD();
            }

            @Override
            public void forbitPermissons() {
                Log.d(TAG, "forbitPermissons: ---------------------------------拒绝了权限");
                System.exit(0);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtil.getInstance().onRequestPermissionsResult(this, requestCode,grantResults);
        boolean b = true;
        for (int i :
                grantResults) {
            if (i < 0) {
                b = false;
                break;
            }
        }
        if(b)
            enterAD();
    }
}

