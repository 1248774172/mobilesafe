package com.xiaoer.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class SmsBackup {
    @SuppressLint("ResourceType")
    public static void backupSms(Context context, File file, ProgressChange pc){
        int index = 0;
        FileOutputStream fos = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            @SuppressLint("Recycle")
            Cursor cursor = contentResolver.query(Uri.parse("content://sms/"),
                    new String []{"address","date","type","read","body"},
                    null,null,null);
            assert cursor != null;
            XmlSerializer xmlSerializer = Xml.newSerializer();
            fos = new FileOutputStream(file);
            xmlSerializer.setOutput(fos,"utf-8");
            xmlSerializer.startDocument("utf-8",true);
            xmlSerializer.startTag(null,"smss");
            pc.setMax(cursor.getCount());
            while (cursor.moveToNext()){
                xmlSerializer.startTag(null,"sms");

                xmlSerializer.startTag(null,"address");
                xmlSerializer.text(cursor.getString(0));
                Log.d(TAG, "backupSms: -------------------address"+cursor.getString(0));
                xmlSerializer.endTag(null,"address");

                xmlSerializer.startTag(null,"date");
                xmlSerializer.text(cursor.getString(1));
                Log.d(TAG, "backupSms: -------------------date"+cursor.getString(1));
                xmlSerializer.endTag(null,"date");

                xmlSerializer.startTag(null,"type");
                xmlSerializer.text(cursor.getString(2));
                Log.d(TAG, "backupSms: -------------------type"+cursor.getString(2));
                xmlSerializer.endTag(null,"type");


                xmlSerializer.startTag(null,"read");
                xmlSerializer.text(cursor.getString(3));
                Log.d(TAG, "backupSms: -------------------read"+cursor.getString(3));
                xmlSerializer.endTag(null,"read");

                xmlSerializer.startTag(null,"body");
                xmlSerializer.text(cursor.getString(4));
                Log.d(TAG, "backupSms: -------------------body"+cursor.getString(4));
                xmlSerializer.endTag(null,"body");

                xmlSerializer.endTag(null,"sms");

                index ++;
                pc.setIndex(index);
                SystemClock.sleep(100);
            }

            xmlSerializer.endTag(null,"smss");
            //必须写！！！！！！！不写的话文件是0kb！！！
            xmlSerializer.endDocument();
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public interface ProgressChange{
        void setMax(int max);

        void setIndex(int index);
    }
}
