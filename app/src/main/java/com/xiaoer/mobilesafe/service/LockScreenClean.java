package com.xiaoer.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.xiaoer.mobilesafe.engine.ProcessProvider;

public class LockScreenClean extends Service {
    private static final String TAG = "LockScreenClean";
    private innerReceiver mInnerReceiver;

    public LockScreenClean() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mInnerReceiver = new innerReceiver();
        registerReceiver(mInnerReceiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        if(mInnerReceiver!=null)
            unregisterReceiver(mInnerReceiver);
    }

    static class innerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: -------------------锁屏了");
            ProcessProvider.killAllProcess(context);
        }
    }

}
