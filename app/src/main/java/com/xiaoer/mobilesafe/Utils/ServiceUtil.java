package com.xiaoer.mobilesafe.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

import static android.content.ContentValues.TAG;

public class ServiceUtil {
    /**
     * 判断某个服务是否开启了
     * @param context 上下文对象
     * @param service   服务的名称
     * @return  服务是否开启
     */
    public static boolean isRunning(Context context,String service){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(1000);
        for (ActivityManager.RunningServiceInfo runningService:
                runningServices ) {
            String className = runningService.service.getClassName();
            Log.d(TAG, "isRunning: --------------------------------运行中的服务"+className);
            if(className.equals(service)){
                return true;
            }
        }
        return false;
    }
}
