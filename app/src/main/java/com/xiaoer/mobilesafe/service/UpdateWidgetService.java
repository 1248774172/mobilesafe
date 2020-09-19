package com.xiaoer.mobilesafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.activity.HomeActivity;
import com.xiaoer.mobilesafe.activity.SplashActivity;
import com.xiaoer.mobilesafe.engine.ProcessProvider;
import com.xiaoer.mobilesafe.receiver.KillProcessReceiver;
import com.xiaoer.mobilesafe.receiver.MyAppWidget;

import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class UpdateWidgetService extends Service {

//    @SuppressLint("HandlerLeak")
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            Log.d(TAG, "handleMessage: ------------收到消息，加载ui");
//            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget);
//            remoteViews.setTextViewText(R.id.tv_widget_count,"进程数:"+mProcessInfos.size());
//            remoteViews.setTextViewText(R.id.tv_widget_memory,"可用内存:"+mAvailMem);
//        }
//    };
    private Timer mTimer;
    private innerBroadcastReceiver mInnerBroadcastReceiver;

    public UpdateWidgetService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ------------------服务已开启");

        getProcessCount();

        //屏幕锁屏后关闭定时任务
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mInnerBroadcastReceiver = new innerBroadcastReceiver();
        registerReceiver(mInnerBroadcastReceiver,intentFilter);
    }
    class innerBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)){
                Log.d(TAG, "onReceive: -------------------屏幕开启");
                getProcessCount();
            }else {
                Log.d(TAG, "onReceive: -------------------屏幕关闭");
                cancelTimer();
            }

        }
    }

    private void getProcessCount() {
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: --------------定时执行.....");
                updateWidget();
            }
        };
        mTimer.schedule(timerTask,300,5000);
    }

    private void updateWidget() {
        AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget);

        //更新两个TextView
        remoteViews.setTextViewText(R.id.tv_widget_count,"进程数:"+
                ProcessProvider.getProcessCount(getApplicationContext()));
        remoteViews.setTextViewText(R.id.tv_widget_memory,"可用内存:"+
                ProcessProvider.getAvailMem(getApplicationContext()));

        //点击除了按钮以外的部分时，进入程序主界面
//        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        Intent intent = new Intent("com.xiaoer.mobilesafe.Home");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_root,pendingIntent);

        //点击按钮时，清理进程
        Intent intent1 = new Intent(getApplicationContext(), KillProcessReceiver.class);
//        Intent intent1 = new Intent("com.xiaoer.mobilesafe.KILL_BACKGROUND_PROCESS");
//        KillProcessReceiver killProcessReceiver = new KillProcessReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.xiaoer.mobilesafe.KILL_BACKGROUND_PROCESS");
//        registerReceiver(killProcessReceiver,intentFilter);
        PendingIntent broadcast = PendingIntent.getBroadcast(getApplicationContext(), 1, intent1,
                PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_widget_clear,broadcast);

        //提交，更新窗体小部件
        ComponentName componentName = new ComponentName(getApplication(), MyAppWidget.class);
        awm.updateAppWidget(componentName,remoteViews);

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        if(mInnerBroadcastReceiver!=null)
            unregisterReceiver(mInnerBroadcastReceiver);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
