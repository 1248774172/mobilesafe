package com.xiaoer.mobilesafe.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.admin.DeviceAdminInfo;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.activity.ChooseContactActivity;
import com.xiaoer.mobilesafe.view.SettingItemView;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Setup2Fragment extends Fragment {

    private View mView;
    private SettingItemView siv_sim;
    private MaterialEditText met_number;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup2, container, false);

        //初始化ui
        initUI();


        return mView;
    }

    /**
     * 初始化ui
     */
    private void initUI() {
        siv_sim = mView.findViewById(R.id.siv_sim);
        siv_sim.hideLine();

        Button bt_choose = mView.findViewById(R.id.bt_choose);
        met_number = mView.findViewById(R.id.met_number);

        //给电话号码输入框增加监听事件，判断输入的电话号码是否正确
        met_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String number = Objects.requireNonNull(met_number.getText()).toString();
                String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
                Pattern compile = Pattern.compile(regex);
                Matcher matcher = compile.matcher(number);
                if(matcher.matches()){
                    met_number.setHelperText("");
                    //电话号输入正确 保存电话号
                    SpUtil.putString(getContext(),SpKey.SAFE_PHONE,number);
                    met_number.clearFocus();
                }else{
                    met_number.setHelperText("请输入正确的电话号码");
                }

            }
        });

        //回显sp中的电话号
        String safe_phone = SpUtil.getString(getContext(), SpKey.SAFE_PHONE, "");
        met_number.setText(safe_phone);

        //获取sp的config文件中的sim卡ID  如果不为空说明绑定过sim卡 否则没绑定过
        String sim_id = SpUtil.getString(getContext(), SpKey.SIM_ID, "");
        boolean safe_open = !TextUtils.isEmpty(sim_id);
        siv_sim.setChecked(safe_open);

        //给绑定sim卡条目添加监听事件
        siv_sim.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //获取焦点让edittext失去焦点
                siv_sim.setFocusable(true);
                siv_sim.setFocusableInTouchMode(true);
                siv_sim.requestFocus();
                //判断是否有读取手机信息的权限
                PermissionsUtil.getInstance().checkPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, new PermissionsUtil.IPermissionsResult() {
                    @Override
                    public void passPermissons() {
                        //点击后将状态取反
                        siv_sim.setChecked(!siv_sim.isChecked());
                    }

                    @Override
                    public void forbitPermissons() {

                    }
                });

                //储存手机sim卡的ID
                if(siv_sim.isChecked()) {
                    //点击开启后绑定sim卡
                    Log.d(TAG, "onClick: 绑定sim卡");
                    if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

                            //获取权限后的操作
                        @SuppressLint("HardwareIds") String simSerialNumber = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            simSerialNumber = String.valueOf(tm.getSimSpecificCarrierId());
                        }else{
                            simSerialNumber = tm.getSimSerialNumber();
                        }
                        SpUtil.putString(getContext(), SpKey.SIM_ID, simSerialNumber);
                    }
                }else {
                    //点击关闭后解绑sim卡
                    Log.d(TAG, "onClick: 解绑sim卡");
                    SpUtil.remove(getContext(), SpKey.SIM_ID);
                }
            }
        });
        //给添加联系人按钮绑定监听事件
        bt_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取焦点让edittext失去焦点
                siv_sim.setFocusable(true);
                siv_sim.setFocusableInTouchMode(true);
                siv_sim.requestFocus();
                //检查是否有读取联系人的权限
                PermissionsUtil.getInstance().checkPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, new PermissionsUtil.IPermissionsResult() {
                    @Override
                    public void passPermissons() {
                        Intent intent = new Intent(getContext(),ChooseContactActivity.class);
                        startActivityForResult(intent,1);
                    }

                    @Override
                    public void forbitPermissons() {

                    }
                });
            }
        });
    }

    /**
     * 处理得到的数据
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            String phone = data.getStringExtra("phone");
            assert phone != null;
            String s = phone.replaceAll("-", "".replaceAll(" ", "").trim());
            met_number.setText(s);
            SpUtil.putString(getContext(),SpKey.SAFE_PHONE,s);
        }
    }

    /**
     * @return 判断用户是否填写了手机号
     */
    public static boolean isAddPhone(Context context){
        String string = SpUtil.getString(context, SpKey.SAFE_PHONE, "");
        return !TextUtils.isEmpty(string);
    }

    /**
     * @return 判断用户是否绑定了sim卡
     */
    public static boolean isBandSim(Context context){
        String string = SpUtil.getString(context, SpKey.SIM_ID, "");
        return !TextUtils.isEmpty(string);
    }
}