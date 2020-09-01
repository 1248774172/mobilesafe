package com.xiaoer.mobilesafe.entity;

import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Permissions {
    public static String [] permissions = new String[]{Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_CALL_LOG,Manifest.permission.READ_CALL_LOG,Manifest.permission.ANSWER_PHONE_CALLS
            };
}
