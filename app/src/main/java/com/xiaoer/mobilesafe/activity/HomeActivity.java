package com.xiaoer.mobilesafe.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.loopj.android.image.SmartImageView;
import com.xiaoer.mobilesafe.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int INSTALL_PERMISS_CODE = 2;
    private String json;
    private JSONObject jsonObject;
    private GridView gv_tool;
    private String[] mToolStr;
    private int[] mDrawableId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //初始化ui
        initUI();
        //初始化数据
        initDate();
        //检查是否有新版本
        Log.d(TAG, "onCreate: 检查更新........");
        checkUpdate();

        gv_tool.setAdapter(new MyAdapter());
        gv_tool.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 8:
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        break;
                }

            }
        });
    }

    /**
     * @describe 初始化数据
     */
    private void initDate() {
        //获取前页面传过来的json信息
        getJson();

        //模拟功能列表的信息
        mToolStr = new String[]{"手机防盗", "通信卫士", "软件管理",
                "进程管理", "流量统计", "手机杀毒",
                "缓存清理", "高级工具", "设置中心"};
        mDrawableId = new int[]{R.drawable.home_safe, R.drawable.home_correspond,R.drawable.home_ruanjian,
                R.drawable.home_jincheng, R.drawable.home_gprs, R.drawable.home_shadu,
                R.drawable.home_huancun,R.drawable.home_gaoji,R.drawable.home_set
        };
    }

    /**
     * @describe 初始化ui
     */
    private void initUI() {
        gv_tool = findViewById(R.id.gv_tool);
    }

    /**
     * @describe 获取传来的json信息
     */
    private void getJson() {

        Intent intent = getIntent();
        json = intent.getStringExtra("json");
        try {
            if (json != null) {
                jsonObject = new JSONObject(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * @describe 更新应用对话框
     */
    private void showUpdateDialog() {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("发现新版本");
            builder.setMessage(jsonObject.getString("msg"));
            builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick: 确认更新");
                    getInstallPermission();
                    downloadAPK();
                }
            });
            builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick: 关闭更新页面");
                }
            });
            builder.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @describe 下载服务器上的apk
     */
    private void downloadAPK() {
        x.Ext.init(this.getApplication());
        x.Ext.setDebug(false);
        try {
            String downloadUrl = jsonObject.getString("downloadUrl");
            RequestParams entity = new RequestParams(downloadUrl);
            entity.setSaveFilePath(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "mobilesafe.apk");
            entity.setAutoRename(false);
            x.http().get(entity, new Callback.CommonCallback<File>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onSuccess(File result) {
                    Log.d(TAG, "onSuccess: 成功了" + result.getPath());
                    installAPK(result);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.d(TAG, "onError: 失败了");

                }

                @Override
                public void onCancelled(CancelledException cex) {
                    Log.d(TAG, "onCancelled: 取消了");
                }

                @Override
                public void onFinished() {
                    Log.d(TAG, "onFinished: 结束了");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @describe 安装下载的更新包
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void installAPK(File file) {
        Log.d(TAG, "installAPK: 安装apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 100);
        return;
    }

    /**
     * @describe 检查是否需要安装未知应用的权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Uri packageURI = Uri.parse("package:" + this.getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, INSTALL_PERMISS_CODE);
            }
        }
    }

    /**
     * @describe 检查是否有新版本
     */
    private void checkUpdate() {

        try {
            if (json != null) {
                //解析json中的版本号
                String versionCode = jsonObject.getString("versionCode");
                //获取当前应用的版本号
                PackageManager packageManager = getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo("com.xiaoer.mobilesafe", 0);
                int currentVersionCode = packageInfo.versionCode;
                //如果当前应用的版本号比json中的版本号低  就需要提醒用户更新
                //否则不用
                if (currentVersionCode < Integer.parseInt(versionCode)) {
                    Log.d(TAG, "checkUpdate: 需要更新");
                    showUpdateDialog();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mToolStr.length;
        }

        @Override
        public Object getItem(int position) {
            return mToolStr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = View.inflate(getApplicationContext(),R.layout.gridview_item,null);
            SmartImageView siv_icon = inflate.findViewById(R.id.siv_icon);
            TextView tv_tool = inflate.findViewById(R.id.tv_tool);

            siv_icon.setBackgroundResource(mDrawableId[position]);
            tv_tool.setText(mToolStr[position]);
            return inflate;
        }
    }
}