package com.xiaoer.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.xiaoer.mobilesafe.engine.ProcessProvider;

public class KillProcessReceiver extends BroadcastReceiver {

    private static final String TAG = "KillProcessReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: -----------------------收到广播清理进程");
        ProcessProvider.killAllProcess(context);
        Toast.makeText(context,"清理成功",Toast.LENGTH_SHORT).show();
    }
}
