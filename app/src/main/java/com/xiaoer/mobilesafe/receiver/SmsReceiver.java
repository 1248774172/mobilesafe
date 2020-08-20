package com.xiaoer.mobilesafe.receiver;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.service.LocationService;

import java.security.Key;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SmsReceiver extends BroadcastReceiver {


    private DevicePolicyManager mDpm;
    private ComponentName mComponentName;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
//        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
//            Object[] pdus = (Object[]) bundle.get("pdus");
//            assert pdus != null;
//            SmsMessage[] messages = new SmsMessage[pdus.length];
//            for (int i = 0; i < pdus.length; i++) {
//                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//            }
//
//            for (SmsMessage message : messages) {
//                String strFrom = message.getDisplayOriginatingAddress();
//                String strMsg = message.getDisplayMessageBody();
//                Log.d("SMSReceiver","From:"+strFrom);
//                Log.d("SMSReceiver","Msg:"+strMsg);
//            }
//        }
        //判断是否开启了手机防盗
        boolean safe_open = SpUtil.getBoolean(context, SpKey.SAFE_OPEN, false);
        if(safe_open) {
            //如果短信过长会分条
            Object[] objects = (Object[]) Objects.requireNonNull(intent.getExtras()).get("pdus");
            assert objects != null;
            //遍历每条短信
            for (Object obj :
                    objects) {
                //获取短信的内容
                SmsMessage fromPdu = SmsMessage.createFromPdu((byte[]) obj);
                String messageBody = fromPdu.getMessageBody();

                //如果短信包含#*alarm*# 那么报警
                if (messageBody.contains("#*alarm*#")) {
                    Log.d(TAG, "onReceive: --------------------收到短信开始报警。。。。");
                    //将媒体音量调到最高
                    AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    int streamMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume,0);

                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
                    //是否一直重复
//                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }

                //如果短信包含#*location*# 那么发送位置
                if (messageBody.contains("#*location*#")) {
                    Intent service = new Intent(context, LocationService.class);
                    context.startService(service);
                }

                //如果短信包含#*lockscreen*# 那么锁屏
                if (messageBody.contains("#*lockscreen*#")) {
                    //初始化
                    initDpm(context);
                    //判断是否拥有设备管理员权限
                    if(checkAdmin()){
                        //有就锁屏
                        mDpm.lockNow();
                    }
                }

                //如果短信包含<pwd> 那么设置锁屏密码
                if(messageBody.contains("<pwd>")) {
                    int start = messageBody.indexOf("<pwd>");
                    int end = messageBody.indexOf("</pwd>");
                    String substring = messageBody.substring(start+5, end);
                    Log.d(TAG, "onReceive: -----------------------密码:"+substring);
//                    if(substring.length()>=4)
                        mDpm.resetPassword(substring, 0);
                }

                //如果短信包含#*wipedata*# 那么清除数据
                if (messageBody.contains("#*wipedata*#")) {
                    //初始化
                    initDpm(context);
                    //判断是否拥有设备管理员权限
                    if(checkAdmin()){
                        //有就清除数据
                        mDpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            mDpm.wipeData(DevicePolicyManager.WIPE_EUICC);
                        }
                        mDpm.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mDpm.wipeData(DevicePolicyManager.WIPE_SILENTLY);
                        }
                    }
                }

            }
        }
    }

    /**
     * 初始化DevicePolicyManager
     * @param context 上下文对象
     */
    private void initDpm(Context context) {
        //获取DevicePolicyManager
        mDpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //获取ComponentName对象 用来判断是否开启了设备管理器功能
        mComponentName = new ComponentName(context,MyDeviceAdminReceiver.class);
    }

    private boolean checkAdmin() {
        return mDpm.isAdminActive(mComponentName);
    }


}
