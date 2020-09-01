package com.xiaoer.mobilesafe.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.internal.telephony.ITelephony;
import com.xiaoer.mobilesafe.db.dao.impl.BlackNumberDaoImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class BlackNumberService extends Service {

    private InnerBroadcastReceiver mInnerBroadcastReceiver;
    private MyListener mMyListener;
    private TelephonyManager mTm;

    public BlackNumberService() {
    }

    @Override
    public void onCreate() {

        //开启广播接收者 一旦在黑名单中且不是拦截电话 那么拦截短信
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(Integer.MAX_VALUE);

        mInnerBroadcastReceiver = new InnerBroadcastReceiver();
        registerReceiver(mInnerBroadcastReceiver, intentFilter);


        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mMyListener = new MyListener();
        mTm.listen(mMyListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                int model = BlackNumberDaoImpl.getInstance(getApplicationContext()).queryModelByPhone(phoneNumber);
                if (model == 2 || model == 3) {
                    Log.d(TAG, "onCallStateChanged: -----------------------符合条件拦截电话！");
                    endCall(phoneNumber);
                }

            }
        }
    }

    /**
     * 挂断电话并删除此电话记录
     * @param phone 黑名单中的电话号
     */
    private void endCall(String phone) {
        boolean b = false;
        /*api28以上不能通过反射调用ITelephony的endCall()方法
        *需要获取TelecomManager调用其endCall()方法
        */
        //判断api版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.d(TAG, "endCall: ---------------------------api 28以上需要检查权限才能拦截"+Build.VERSION.SDK_INT);
            TelecomManager manager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"没有权限，拦截失败！",Toast.LENGTH_SHORT).show();
                }
                b = manager.endCall();
            }
        }else {
            //ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            try {
                //获取类
                @SuppressLint("PrivateApi")
                Class<?> serviceManager = Class.forName("android.os.ServiceManager");
                //获取方法
                Method getService = serviceManager.getMethod("getService", String.class);
                //使用方法
                IBinder invoke = (IBinder) getService.invoke(null, Context.TELEPHONY_SERVICE);
                ITelephony iTelephony = ITelephony.Stub.asInterface(invoke);

                Log.d(TAG, "endPhone: --------------------------拦截电话！");
                assert iTelephony != null;
                b = iTelephony.endCall();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | RemoteException e) {
                e.printStackTrace();
            }
        }

        if(b) {
            Toast.makeText(getApplicationContext(), "成功拦截", Toast.LENGTH_SHORT).show();
            //拦截后删除通话记录
            ContentObserver contentObserver = new MyContentObserver(new Handler(), phone);
            getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"),
                    true, contentObserver);
        }

    }

    /**
     * 通话记录的内容观察者 当数据改变时删除通话记录
     */
    private class MyContentObserver extends ContentObserver{
        private String phone;
        public MyContentObserver(Handler handler,String phone) {
            super(handler);
            this.phone = phone;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //插入数据后再进行删除
            getContentResolver().delete(Uri.parse("content://call_log/calls"),
                    "number = ?", new String[]{phone});
            Log.d(TAG, "onChange: --------------------删除"+phone+"的通话记录成功！");
            super.onChange(selfChange, uri);
        }
    }

    /**
     * 短信的广播接收者
     */
    private class InnerBroadcastReceiver extends BroadcastReceiver{
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            Object [] pdus = (Object[]) Objects.requireNonNull(intent.getExtras()).get("pdus");

            assert pdus != null;
            for (Object obj :
                    pdus) {
                SmsMessage fromPdu = SmsMessage.createFromPdu((byte[]) obj);
                String originatingAddress = fromPdu.getOriginatingAddress();
                String messageBody = fromPdu.getMessageBody();
                Log.d(TAG, "onReceive: ----------------------------短信内容："+messageBody);
                int model = BlackNumberDaoImpl.getInstance(getApplicationContext()).
                        queryModelByPhone(originatingAddress);

                if(model == 1 || model == 3){
                    Log.d(TAG, "onReceive: ----------------------------符合拦截条件 进行拦截");
                    abortBroadcast();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if(mInnerBroadcastReceiver != null){
            unregisterReceiver(mInnerBroadcastReceiver);
        }
        if(mMyListener != null){
            mTm.listen(mMyListener,PhoneStateListener.LISTEN_NONE);
        }
        super.onDestroy();
    }
}
