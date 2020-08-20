package com.xiaoer.mobilesafe.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.xiaoer.mobilesafe.Utils.PermissionsUtil;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.activity.HomeActivity;

import static android.content.ContentValues.TAG;

public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //获取位置管理者
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //允许使用网络
        criteria.setCostAllowed(true);
        //获取最优定位方式
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = lm.getBestProvider(criteria, true);
        //检查权限
        String [] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            assert bestProvider != null;
            lm.requestLocationUpdates(bestProvider, 0, 1, new LocationListener() {
                @SuppressLint("UnlocalizedSms")
                @Override
                public void onLocationChanged(Location location) {
                    //经度
                    double longitude = location.getLongitude();
                    //纬度
                    double latitude = location.getLatitude();
                    Log.d(TAG, "onLocationChanged: --------------------------------有权限，发送短信");
                    String phone = SpUtil.getString(getApplicationContext(), SpKey.SAFE_PHONE, "");
                    assert phone != null;
                    SmsManager.getDefault().sendTextMessage(phone,null,
                            "经度："+longitude+",纬度："+latitude,null,null);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }else{
            Log.d(TAG, "onCreate: ------------------------无权限");
            PermissionsUtil.getInstance().checkPermissions(new Activity(), permissions, new PermissionsUtil.IPermissionsResult() {
                @Override
                public void passPermissons() {
                }
                @Override
                public void forbitPermissons() {
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
