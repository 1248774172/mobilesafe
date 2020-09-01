package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.image.SmartImageView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.Md5Util;
import com.xiaoer.mobilesafe.entity.Permissions;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int INSTALL_PERMISS_CODE = 2;
    private String json;
    private JSONObject jsonObject;
    private GridView gv_tool;
    private String[] mToolStr;
    private int[] mDrawableId;
    private String mInitPwd;
    private String mConfirmPwd;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //检查悬浮窗权限
        if(Build.VERSION.SDK_INT >= 23) {
            boolean canShowAlert = PermissionsUtil.canShowAlert(getApplicationContext());
            Log.d(TAG, "onCreate: --------------------------------悬浮窗权限：" + canShowAlert);
            if (!canShowAlert) {
                Toast.makeText(getApplicationContext(), "请给予悬浮窗权限", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }
        //检查其他权限
        PermissionsUtil.getInstance().checkPermissions(this, Permissions.permissions, new PermissionsUtil.IPermissionsResult() {
            @Override
            public void passPermissons() {
                Log.d(TAG, "passPermissons: ----------------------权限都通过了！");

            }

            @Override
            public void forbitPermissons() {

            }
        });

        //初始化ui
        initUI();
        //初始化数据
        initDate();
        //检查是否有新版本
        Log.d(TAG, "sp中的自动更新信息: "+ SpUtil.getBoolean(this, SpKey.UPDATE,false));
        if(SpUtil.getBoolean(this, SpKey.UPDATE,false)){
            Log.d(TAG, "onCreate: 检查更新........");
            checkUpdate();
        }

        //给主窗口添加监听事件
        gv_tool.setAdapter(new MyAdapter());
        gv_tool.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position){
                    case 0:              //手机防盗
                        String pwd = SpUtil.getString(getApplicationContext(), SpKey.SAFE_PWD, "");
                        if(TextUtils.isEmpty(pwd)){
                            //第一次使用，初始化密码
                            initPwdDialog();
                        }else{
                            //不是第一次使用，检验密码
                            confirmPwdDialog();
                        }
                        break;
                    case 1:
                        intent = new Intent(getApplicationContext(),BlackNumberActivity.class);
                        startActivity(intent);
                        break;
                    case 7:     //高级工具
                        intent = new Intent(getApplicationContext(), AToolActivity.class);
                        startActivity(intent);
                        break;
                    case 8:         //设置页面
                        intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.getInstance().onRequestPermissionsResult(this,requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 展示确认密码的对话框，并校验用户密码
     */
    private void confirmPwdDialog() {
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.confirm_pwd_view, null);
        alertDialog.setView(view);
        //找控件
        Button bt_submit = view.findViewById(R.id.bt_submit);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);
        final MaterialEditText met_confirm_pwd = view.findViewById(R.id.met_confirm_pwd);
        //避免键盘出现的时候拉伸后面的activity
        Window win = getWindow ();
        win.setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        alertDialog.show();


        //给确认按钮添加监听
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfirmPwd = Objects.requireNonNull(met_confirm_pwd.getText()).toString();
                String pwd = SpUtil.getString(getApplicationContext(), SpKey.SAFE_PWD, "");
                if(!TextUtils.isEmpty(mConfirmPwd)) {
                    if(pwd.equals(Md5Util.encoder(mConfirmPwd))) {
                        //进入手机防盗
                        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(),"密码验证通过",Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }else
                        Toast.makeText(getApplicationContext(),"密码错误",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //给取消按钮添加监听
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * 展示初始化密码的对话框,并保存用户设置的密码
     */
    private void initPwdDialog() {
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.init_pwd_view, null);
        alertDialog.setView(view);
        //找控件
        Button bt_submit = view.findViewById(R.id.bt_submit);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);
        final MaterialEditText met_init_pwd = view.findViewById(R.id.met_init_pwd);
        final MaterialEditText met_confirm_pwd = view.findViewById(R.id.met_confirm_pwd);
        //避免键盘出现时拉伸后面的activity
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        alertDialog.show();


        //给确认密码的编辑框加监听 检测两次输入的密码是否一致
        met_confirm_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                //获取两个编辑框的内容
                mInitPwd = Objects.requireNonNull(met_init_pwd.getText()).toString();
                mConfirmPwd = Objects.requireNonNull(met_confirm_pwd.getText()).toString();

                if(mInitPwd.equals(mConfirmPwd)){
                    met_confirm_pwd.setHelperText("");
                }else{
                    met_confirm_pwd.setHelperText("两次密码不一致！");
                }
            }
        });

        //给确认按钮添加监听
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mInitPwd) && !TextUtils.isEmpty(mConfirmPwd)) {
                    if(mInitPwd.equals(mConfirmPwd)) {
                        //进入手机防盗
                        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                        startActivity(intent);
                        //保存密码
                        SpUtil.putString(getApplicationContext(),SpKey.SAFE_PWD, Md5Util.encoder(mInitPwd));
                        Toast.makeText(getApplicationContext(),"密码初始化成功",Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }else
                        Toast.makeText(getApplicationContext(),"两次密码不一致",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //给取消按钮添加监听
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
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
        } catch (PackageManager.NameNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * GridVIew总布局的数据适配器
     */
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
            @SuppressLint("ViewHolder") View inflate = View.inflate(getApplicationContext(),R.layout.gridview_item,null);
            SmartImageView siv_icon = inflate.findViewById(R.id.siv_icon);
            TextView tv_tool = inflate.findViewById(R.id.tv_tool);

            siv_icon.setBackgroundResource(mDrawableId[position]);
            tv_tool.setText(mToolStr[position]);
            return inflate;
        }
    }
}