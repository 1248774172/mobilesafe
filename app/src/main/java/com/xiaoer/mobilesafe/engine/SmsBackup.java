package com.xiaoer.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import androidx.documentfile.provider.DocumentFile;

import com.xiaoer.mobilesafe.Utils.DocumentUtils;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static android.content.ContentValues.TAG;

public class SmsBackup {
    /**
     * @param context 上下文对象
     * @param file  备份路径
     * @param pc    接口
     * @return 返回备份短信的条数
     */
    @SuppressLint("ResourceType")
    public static int backupSms(Context context, File file, ProgressChange pc){
        int index = 0;
        FileOutputStream fos = null;
        Cursor cursor = null;
        try {
            Log.d(TAG, "backupSms: -----------------------备份路径："+file.getPath());
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(Uri.parse("content://sms/"),
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
//                Log.d(TAG, "backupSms: -------------------address"+cursor.getString(0));
                xmlSerializer.endTag(null,"address");

                xmlSerializer.startTag(null,"date");
                xmlSerializer.text(cursor.getString(1));
//                Log.d(TAG, "backupSms: -------------------date"+cursor.getString(1));
                xmlSerializer.endTag(null,"date");

                xmlSerializer.startTag(null,"type");
                xmlSerializer.text(cursor.getString(2));
//                Log.d(TAG, "backupSms: -------------------type"+cursor.getString(2));
                xmlSerializer.endTag(null,"type");


                xmlSerializer.startTag(null,"read");
                xmlSerializer.text(cursor.getString(3));
//                Log.d(TAG, "backupSms: -------------------read"+cursor.getString(3));
                xmlSerializer.endTag(null,"read");

                xmlSerializer.startTag(null,"body");
                xmlSerializer.text(cursor.getString(4));
//                Log.d(TAG, "backupSms: -------------------body"+cursor.getString(4));
                xmlSerializer.endTag(null,"body");

                xmlSerializer.endTag(null,"sms");

                index ++;
                pc.setIndex(index);
                SystemClock.sleep(50);
            }

            xmlSerializer.endTag(null,"smss");
            //必须写！！！！！！！不写的话文件是0kb！！！
            xmlSerializer.endDocument();

            return cursor.getCount();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if(cursor != null) {
                    cursor.close();
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
