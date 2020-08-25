package com.xiaoer.mobilesafe.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.engine.AddressDao;

public class AddressService extends Service {
    private static final String TAG = "AddressService";
    private TelephonyManager mTm;
    private MyPhoneStateListener mMyPhoneStateListener;
    private WindowManager mWm;
    private View mToast_view;
    private String mAddress;

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //收到查询完成的消息就设置
            TextView tv_toast = mToast_view.findViewById(R.id.tv_toast);
            tv_toast.setText(mAddress);
        }
    };


    @Override
    public void onCreate() {
        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mWm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //监听手机状态
        mMyPhoneStateListener = new MyPhoneStateListener();
        mTm.listen(mMyPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy() {
        //关闭服务 取消监听事件
        if(mWm!=null && mMyPhoneStateListener!=null)
            mTm.listen(mMyPhoneStateListener,PhoneStateListener.LISTEN_NONE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyPhoneStateListener extends PhoneStateListener{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCallStateChanged(int state,String phoneNumber){
            switch(state){
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(TAG,"onCallStateChanged: -----------------空闲状态");
                //此时不应该显示Toast
                if(mWm!=null && mToast_view!=null)
                    mWm.removeView(mToast_view);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d(TAG,"onCallStateChanged: -----------------接听状态");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(TAG,"onCallStateChanged: -----------------响铃状态");
                //此时可以显示Toast
                showToast();
                //设置手机号码归属地
                getAddress(phoneNumber);
                break;
            }
        }
    }

    /**
     * 开子线程获取电话号归属地，获取完后发消息设置textview
     * @param phoneNumber 电话号
     */
    private void getAddress(final String phoneNumber) {
        new Thread(){
            @Override
            public void run() {
                mAddress = AddressDao.getAddress(getApplicationContext(), phoneNumber);
                myHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    /**
     * 弹出自定义的吐司
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RtlHardcoded")
    public void showToast() {
        if (PermissionsUtil.canShowAlert(getApplicationContext())) {
            Log.d(TAG, "showToast: --------------------------有权限可以弹窗");
            //设置Toast的具体属性值
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.gravity = Gravity.RIGHT + Gravity.TOP;
            //加载Toast的布局文件
            mToast_view = View.inflate(getApplicationContext(), R.layout.toast_view, null);

            //给窗体添加这个吐司
            mWm.addView(mToast_view, params);
        }else {
            Toast.makeText(this,"悬浮窗权限已被拒绝",Toast.LENGTH_SHORT).show();
        }
    }
}
