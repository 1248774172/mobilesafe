package com.xiaoer.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.format.Formatter;
import android.util.Log;

import com.xiaoer.mobilesafe.entity.ProcessInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProcessProvider {

    private static final String TAG = "ProcessProvider";

    /**
     * @param context 上下文对象
     * @return 获取运行中的进程数
     */
    public static int getProcessCount(Context context){
        return getProcessInfo(context).size();
    }

    /**
     * @param context 上下文对象
     * @return 手机剩余可用运行内存
     */
    public static String getAvailMem(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return Formatter.formatFileSize(context, memoryInfo.availMem);
    }

    /**
     * @param context  上下文对象
     * @return 手机全部可用内存
     */
    public static String getTotalMem(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        long l = memoryInfo.totalMem;
        return Formatter.formatFileSize(context, l);
        //        FileReader fileReader = null;
//        BufferedReader bufferedReader = null;
//        try {
//            File file = new File("/proc/meminfo");
//            fileReader = new FileReader(file);
//            bufferedReader = new BufferedReader(fileReader);
//            String line = bufferedReader.readLine().trim();
//            String[] split = line.split("\\s+");
//            int i = Integer.parseInt(split[1]);
//            long mem = (long)i * 1024;
////            String formatAllMemSize = Formatter.formatFileSize(this, mem);
//            tv_process_mem.setText(formatAvailMemSize+" 可用 | "+formatAllMemSize);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                if (fileReader != null)
//                    fileReader.close();
//                if (bufferedReader != null)
//                    bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * @param context 上下文对象
     * @return 返回一个存储运行进程的信息的集合
     */
    @SuppressLint("UseCompatLoadingForDrawables")
//    public static List<ProcessInfo> getProcessInfo(Context context){
//        ArrayList<ProcessInfo> processInfos = new ArrayList<>();
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        PackageManager pm = context.getPackageManager();
////        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
//
//        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = AndroidProcesses.getRunningAppProcessInfo(context);
//        ProcessInfo processInfo;
//        for (ActivityManager.RunningAppProcessInfo rapi :
//                runningAppProcesses) {
//            processInfo = new ProcessInfo();
//            processInfo.setPackageName(rapi.processName);
//
//            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{rapi.pid});
//            long sizeBytes = (long)(processMemoryInfo[0].getTotalPrivateDirty()) * 1024;
//            String formatFileSize = Formatter.formatFileSize(context,sizeBytes);
//            processInfo.setMemSize(formatFileSize);
//
//            try {
//                ApplicationInfo applicationInfo = pm.getApplicationInfo(rapi.processName, 0);
//                processInfo.setIcon(applicationInfo.loadIcon(pm));
//                processInfo.setName(applicationInfo.loadLabel(pm).toString());
//
//                if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
//                    //用户应用
//                    processInfo.setSystem(false);
//                }else{
//                    processInfo.setSystem(true);
//                }
//            } catch (PackageManager.NameNotFoundException e) {
//                processInfo.setName(rapi.processName);
//                processInfo.setIcon(context.getDrawable(R.mipmap.ic_launcher));
//                processInfo.setSystem(true);
//                e.printStackTrace();
//            }
//            processInfos.add(processInfo);
//        }
//        Collections.sort(processInfos, new Comparator<ProcessInfo>() {
//            @Override
//            public int compare(ProcessInfo o1, ProcessInfo o2) {
//                if(o1.isSystem()) {
//                    if (o2.isSystem()) {
//                        return 0;
//                    }else {
//                        return 1;
//                    }
//                }else {
//                    if(o2.isSystem()) {
//                        return -1;
//                    }else {
//                        return 0;
//                    }
//                }
//            }
//        });
//        return processInfos;
//    }

    public static List<ProcessInfo> getProcessInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ArrayList<ProcessInfo> processInfos = new ArrayList<>();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (int i = 0; i < installedPackages.size(); i++) {
            ProcessInfo processInfo = new ProcessInfo();
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.packageName.split(":")[0];

            if((ApplicationInfo.FLAG_STOPPED & packageInfo.applicationInfo.flags) == 0){
                processInfo.setPackageName(packageName);
                processInfo.setSystem((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0);
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                processInfo.setName(applicationInfo.loadLabel(packageManager).toString());
                processInfo.setIcon(applicationInfo.loadIcon(packageManager));
                processInfos.add(processInfo);
            }

        }
//        for (ApplicationInfo info :
//                installedApplications) {
//            ProcessInfo processInfo = new ProcessInfo();
//
//            if (isRunning(context,info.packageName)){
//                processInfo.setSystem(isSystem(info));
//
//                if(!isSystem(info))
//                    Log.d(TAG, "getProcessInfo: -------------------用户进程"+info.loadLabel(packageManager));
//
//                processInfo.setName(info.loadLabel(packageManager).toString());
//                processInfo.setIcon(info.loadIcon(packageManager));
//                processInfo.setPackageName(info.packageName);
//                processInfos.add(processInfo);
//            }
//        }
        Collections.sort(processInfos, new Comparator<ProcessInfo>() {
            @Override
            public int compare(ProcessInfo o1, ProcessInfo o2) {
                if(o1.isSystem()) {
                    if (o2.isSystem()) {
                        return 0;
                    }else {
                        return 1;
                    }
                }else {
                    if(o2.isSystem()) {
                        return -1;
                    }else {
                        return 0;
                    }
                }
            }
        });
        return processInfos;
    }

//    public static boolean isSystem(ApplicationInfo applicationInfo){
//        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0);
//    }
//    public static boolean isRunning(Context context, String packageName){
//        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(Integer.MAX_VALUE);
//        boolean isAppRunning = false;
//        //100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
//        for (ActivityManager.RunningTaskInfo info : list) {
//            assert info.baseActivity != null;
//            assert info.topActivity != null;
//            if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName)) {
//                isAppRunning = true;
//                break;
//            }
//        }
//        return isAppRunning;
//    }

//    public void getRunningApp(Context context){
//        PackageManager localPackageManager = context.getPackageManager();
//        List localList = localPackageManager.getInstalledPackages(0);
//        for (int i = 0; i < localList.size(); i++) {
//            PackageInfo localPackageInfo1 = (PackageInfo) localList.get(i);
//            String str1 = localPackageInfo1.packageName.split(":")[0];
//            if (((ApplicationInfo.FLAG_SYSTEM & localPackageInfo1.applicationInfo.flags) == 0) && ((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP & localPackageInfo1.applicationInfo.flags) == 0) && ((ApplicationInfo.FLAG_STOPPED & localPackageInfo1.applicationInfo.flags) == 0)) {
//                Log.e(TAG,str1);
//            }
//        }
//    }
    public static void killAllProcess(Context context){
        Log.d(TAG, "killAllProcess: ---------------------清理全部");
        List<ProcessInfo> processInfo = getProcessInfo(context);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ProcessInfo pi :
                processInfo) {
            if (!pi.getPackageName().equals(context.getPackageName()))
                am.killBackgroundProcesses(pi.getPackageName());
            else
                Log.d(TAG, "killAllProcess: -------------成功过滤本程序");
        }
    }
}
