package com.xiaoer.mobilesafe.activity;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.Utils.StatusBarUtil;
import com.xiaoer.mobilesafe.fragment.Setup1Fragment;
import com.xiaoer.mobilesafe.fragment.Setup2Fragment;
import com.xiaoer.mobilesafe.fragment.Setup3Fragment;
import com.xiaoer.mobilesafe.receiver.MyDeviceAdminReceiver;
import com.xiaoer.mobilesafe.view.SettingItemView;

import java.util.ArrayList;

/**
 *
 */
public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "SetupActivity";
    private ViewPager vp_setup;
    public ArrayList<Fragment> mViews;
    private View mSetup_view;
    private View mSetup_over;
    private SettingItemView siv_safe;
    private SettingItemView siv_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //当设置完成重新回到手机防盗页面时 再检查一遍权限
        Intent intent = getIntent();
        boolean setup_over = intent.getBooleanExtra("setup_over", false);
        if(setup_over){
            String [] permissions = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS};
            PermissionsUtil.getInstance().checkPermissions(this, permissions, new PermissionsUtil.IPermissionsResult() {
                @Override
                public void passPermissons() {

                }

                @Override
                public void forbitPermissons() {

                }
            });
        }
        //初始化ui
        initUi();
        //初始化数据
        initData();

        //如果没设置过手机防盗就设置 设置过了就不设置
        //根据sp配置文件中是否有sim卡的序列号判断是否设置过了
        String sim_id = SpUtil.getString(getApplicationContext(), SpKey.SIM_ID, "");
        if(TextUtils.isEmpty(sim_id)) {
            //sp的config文件中没有sim卡序列号 就设置
            setContentView(mSetup_view);
            showSetup();
        }else{
            setContentView(mSetup_over);
        }


    }

    /**
     * 初始化ui
     */
    private void initUi() {
        //加载两个布局 分别是设置界面和设置完成界面
        mSetup_view = View.inflate(this, R.layout.activity_setup_view, null);
        mSetup_over = View.inflate(this, R.layout.activity_setup_over, null);

        vp_setup = mSetup_view.findViewById(R.id.vp_setup);
        siv_safe = mSetup_over.findViewById(R.id.siv_safe);
        siv_admin = mSetup_over.findViewById(R.id.siv_admin);
        TextView tv_phone = mSetup_over.findViewById(R.id.tv_phone);
        TextView tv_reset = mSetup_over.findViewById(R.id.tv_reset);

        //获取sp中的关于手机防盗的信息
        boolean safe_open = SpUtil.getBoolean(this, SpKey.SAFE_OPEN, false);
        //对设置完成界面进行ui初始化
        //设置手机防盗是否开启条目的状态和监听事件
        siv_safe.setChecked(safe_open);
        siv_safe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siv_safe.setChecked(!siv_safe.isChecked());
                SpUtil.putBoolean(getApplicationContext(), SpKey.SAFE_OPEN, siv_safe.isChecked());
            }
        });

        //获取设备管理器权限是否开启了
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        boolean adminActive = dpm.isAdminActive(componentName);
        //对设置完成界面进行ui初始化
        //设置手机防盗是否开启条目的状态和监听事件
        siv_admin.setChecked(adminActive);
        siv_admin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_DEVICE_ADMIN_SERVICE);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                    startActivity(intent);
                    finish();
            }
        });


        //设置安全手机号条目的状态
        String safe_phone = SpUtil.getString(this, SpKey.SAFE_PHONE, "");
        if(TextUtils.isEmpty(safe_phone)){
            tv_phone.setText("未设置安全手机号");
        }else {
            tv_phone.setText(safe_phone);
        }
        //设置重新进入设置向导条目的监听事件
        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ----------------"+"重新进入手机防盗设置界面");
                setContentView(mSetup_view);
                showSetup();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Setup1Fragment setup1Fragment = new Setup1Fragment();
        Setup2Fragment setup2Fragment = new Setup2Fragment();
        Setup3Fragment setup3Fragment = new Setup3Fragment();
        mViews = new ArrayList<>();
        mViews.add(setup1Fragment);
        mViews.add(setup2Fragment);
        mViews.add(setup3Fragment);
        Log.d(TAG, "initData:-------------------------- " + mViews.size());

    }

    /**
     * 展示设置页面 进行手机防盗的设置
     */
    private void showSetup() {
        //隐藏状态栏
        StatusBarUtil.hideStatusBar(this);

        vp_setup.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mViews.get(position);
            }

            @Override
            public int getCount() {
                return mViews.size();
            }
        });
        vp_setup.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                //第二页必须全部填写才能滑动到第三页，在这进行验证
                if(position == 2){
                    Log.d(TAG, "onPageSelected: ----------------------"+"准备划到第三页");
                    if(Setup2Fragment.isAddPhone(getApplicationContext())&&
                            Setup2Fragment.isBandSim(getApplicationContext())){
                        //用户填写了安全手机号并且绑定了sim卡
                        Log.d(TAG, "onPageScrolled: ---------------"+"用户填写了安全手机号并且绑定了sim卡");
                    }else{
                        Log.d(TAG, "onPageScrolled: ---------------"+"不能继续");
                        vp_setup.setCurrentItem(1,true);
                        if(Setup2Fragment.isAddPhone(getApplicationContext())) {
                            Log.d(TAG, "onPageSelected: 未绑定sim卡");
                            Toast.makeText(getApplicationContext(),"请绑定sim卡",Toast.LENGTH_SHORT).show();
                        }else if(Setup2Fragment.isBandSim(getApplicationContext())) {
                            Log.d(TAG, "onPageSelected: 未填写手机");
                            Toast.makeText(getApplicationContext(),"请填写安全手机",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"请填写信息",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //指定当前出现的界面
        vp_setup.setCurrentItem(0);
    }

}

