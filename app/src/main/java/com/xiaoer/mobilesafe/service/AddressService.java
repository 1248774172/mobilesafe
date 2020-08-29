package com.xiaoer.mobilesafe.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
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
            //收到查询归属地完成的消息就设置
            //加载图片资源并把id存在数组中
            final int [] toast_color = new int[]{R.drawable.call_locate_white,R.drawable.call_locate_blue,
                    R.drawable.call_locate_gray,R.drawable.call_locate_green,R.drawable.call_locate_orange};
            //获取sp中用户设置储存的颜色
            int address_color = SpUtil.getInt(getApplicationContext(), SpKey.ADDRESS_COLOR, 0);
            TextView tv_toast = mToast_view.findViewById(R.id.tv_toast);
            tv_toast.setBackgroundResource(toast_color[address_color]);
            tv_toast.setText(mAddress);

        }
    };
    private MyBroadcastReceiver mMyBroadcastReceiver;
    private int mScreenWidth;
    private int mScreenHeight;


    @Override
    public void onCreate() {
        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mWm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenWidth = mWm.getDefaultDisplay().getWidth();
        mScreenHeight = mWm.getDefaultDisplay().getHeight();

        //监听手机状态 设置监听事件
        mMyPhoneStateListener = new MyPhoneStateListener();
        mTm.listen(mMyPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        mMyBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(mMyBroadcastReceiver,intentFilter);
    }

    class MyBroadcastReceiver extends BroadcastReceiver{

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            String resultData = getResultData();
            Log.d(TAG, "onReceive: ---------------------------播出电话号："+resultData);
            showToast(resultData);
        }
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
                showToast(phoneNumber);
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
    public void showToast(String phone) {
        if (PermissionsUtil.canShowAlert(getApplicationContext())) {
            Log.d(TAG, "showToast: --------------------------有权限可以弹窗");

            mToast_view = View.inflate(getApplicationContext(), R.layout.toast_view, null);

            //设置Toast的具体属性值
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //如果不设置这个 悬浮窗不会被移动
//            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //得把原点定在左上才能保证坐标正确 应该是默认的原点不是左上角
            params.gravity = Gravity.LEFT + Gravity.TOP;
            //加载Toast的位置信息
            int toast_location_x = SpUtil.getInt(this, SpKey.TOAST_LOCATION_X, 0);
            int toast_location_y = SpUtil.getInt(this, SpKey.TOAST_LOCATION_Y, 0);
            //控件左上角的x坐标
            params.x = toast_location_x;
            //控件左上角的y坐标
            params.y = toast_location_y;
            Log.d(TAG, "showToast: -----------------------------------params.x = " + params.x);
            Log.d(TAG, "showToast: -----------------------------------params.y = " + params.y);


            mToast_view.setOnTouchListener(new View.OnTouchListener() {
                private int mStartY;
                private int mStartX;
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        //当点击时
                        case MotionEvent.ACTION_DOWN:
                            Log.d(TAG, "onTouch: ----------------------------悬浮窗被按下");
                            //记录起始位置
//                        getX是以组件左上角为坐标原点，获取X坐标轴上的值。
//                        getRawX是以屏幕左上角为左脚做预案，获取X坐标轴上的值。
                            mStartX = (int) event.getRawX();
                            mStartY = (int) event.getRawY();
                            break;
                        //当移动时
                        case MotionEvent.ACTION_MOVE:
                            Log.d(TAG, "onTouch: ----------------------------悬浮窗被移动");
                            //记录移动完的位置
                            int endX = (int) event.getRawX();
                            int endY = (int) event.getRawY();

                            //计算差值
                            int disX = endX - mStartX;
                            int disY = endY - mStartY;

                            //得到移动完的具体位置
                            params.x = params.x + disX;
                            params.y = params.y + disY;

                            //容错处理
                            if(params.x > mScreenWidth - mToast_view.getWidth()){
                                params.x = mScreenWidth - mToast_view.getWidth();
                            }
                            if(params.x < 0){
                                params.x = 0;
                            }

                            if(params.y > mScreenHeight - mToast_view.getHeight()){
                                params.y = mScreenHeight - mToast_view.getHeight();
                            }
                            if(params.y < 0){
                                params.y = 0;
                            }


                            //更新位置
                            mWm.updateViewLayout(mToast_view,params);

                            //重置起始位置 将最后的位置当成起始位置
                            mStartX = endX;
                            mStartY = endY;
                            break;
                        //当抬起时
                        case MotionEvent.ACTION_UP:
                            //保存位置
                            SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_X,params.x);
                            SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_Y,params.y);
                            break;
                    }
                    //只有触摸事件时返回值是true才能响应触摸事件
                    //既有触摸事件又有点击事件时 返回false 点击事件才能生效   P964
                    return true;
                }
            });

            //给窗体添加这个吐司
            mWm.addView(mToast_view, params);

            //设置手机号码归属地
            getAddress(phone);

        }else {
            Toast.makeText(this,"悬浮窗权限已被拒绝",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        //关闭服务 取消监听事件
        if(mWm!=null && mMyPhoneStateListener!=null) {
            Log.d(TAG, "onDestroy: ---------------------------移除服务");
            mTm.listen(mMyPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            if(mToast_view != null){
                mWm.removeView(mToast_view);
            }
        }
        if(mMyBroadcastReceiver != null ){
            unregisterReceiver(mMyBroadcastReceiver);
        }
    }
}
