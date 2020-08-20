package com.xiaoer.mobilesafe.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class BootReceiver extends BroadcastReceiver {

    @SuppressLint({"UnlocalizedSms", "UnsafeProtectedBroadcastReceiver", "HardwareIds"})
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ------------------------重启完成");
        String sim_id = SpUtil.getString(context, SpKey.SIM_ID, "");
        Log.d(TAG, "onReceive: ----------------------------simid"+sim_id);
        if(!TextUtils.isEmpty(sim_id)){
            //验证绑定的sim卡是否被更换了
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("HardwareIds") String simSerialNumber;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                simSerialNumber = tm.getSimSpecificCarrierId()+"";
                Log.d(TAG, "onReceive: ----------------------------Q以上"+simSerialNumber);
            }else{
                simSerialNumber = tm.getSimSerialNumber();
                Log.d(TAG, "onReceive: ----------------------------Q以下"+simSerialNumber);
            }
            //相等说明没更换sim卡
            //不相等说明sim卡被更换了
            String safe_phone = SpUtil.getString(context, SpKey.SAFE_PHONE, "");
            assert safe_phone!=null;
            if(!sim_id.equals(simSerialNumber)){
                Log.d(TAG, "onReceive: -------------------------------发送短信");
                SmsManager aDefault = SmsManager.getDefault();
                aDefault.sendTextMessage(safe_phone,null,"sim卡已被更换",null,null);
            }
        }

    }
}
