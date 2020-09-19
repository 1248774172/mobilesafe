package com.xiaoer.mobilesafe.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.activity.SplashActivity;
import com.xiaoer.mobilesafe.service.UpdateWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class MyAppWidget extends AppWidgetProvider {

    private static final String TAG = "MyAppWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.i(TAG, "updateAppWidget: -------------------创建"+appWidgetId+"窗体");

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.process_widget);
//
//        Intent intent = new Intent(context, SplashActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
//                PendingIntent.FLAG_CANCEL_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.ll_root,pendingIntent);
//
//        Intent intent1 = new Intent("android.intent.action.KILL_BACKGROUND_PROCESS");
//        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent1,
//                PendingIntent.FLAG_CANCEL_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.btn_widget_clear,broadcast);
//
//        appWidgetManager.updateAppWidget(appWidgetId,remoteViews);

        Intent service = new Intent(context, UpdateWidgetService.class);
        context.startService(service);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.i(TAG, "onEnabled: ---------------创建第一个窗体");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled: -----------------最后一个无了 关闭服务");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.stopService(intent);
    }

}

