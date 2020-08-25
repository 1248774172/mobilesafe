package com.xiaoer.mobilesafe.Utils;

import android.Manifest;

public class Permissions {
    public static String [] permissions = new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
}
