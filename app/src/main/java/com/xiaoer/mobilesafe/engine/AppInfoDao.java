package com.xiaoer.mobilesafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.xiaoer.mobilesafe.entity.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppInfoDao {
    /**
     * @param context 上下文
     * @return  返回储存着应用信息的集合 按model大小排序
     */
    public static List<AppInfo> getAppInfo(Context context){
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        ArrayList<AppInfo> AppInfos = new ArrayList<>();

        for (ApplicationInfo installedApplication:installedApplications) {
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(installedApplication.packageName);
            appInfo.setName(installedApplication.loadLabel(packageManager).toString()+installedApplication.uid);
            appInfo.setIcon(installedApplication.loadIcon(packageManager));
            if((installedApplication.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                //用户程序
                appInfo.setModel(1);
            }else{
                //系统应用
                appInfo.setModel(2);
            }
            AppInfos.add(appInfo);
        }

        Collections.sort(AppInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return Integer.compare(o1.getModel(), o2.getModel());
            }
        });
        return AppInfos;
    }
}
