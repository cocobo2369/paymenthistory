package com.example.paymenthistory3;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PatternMatcher;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

import static android.app.Notification.EXTRA_NOTIFICATION_TAG;
import static android.content.ContentValues.TAG;

public class NotificationListener extends NotificationListenerService{
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Notification notification = sbn.getNotification();

        Bundle extras = sbn.getNotification().extras;
        String title =  extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();

/*        Toast.makeText(this, "onNotificationPosted ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId() +
                " postTime: " + sbn.getPostTime() +
                " title: " + title +
                " text : " + text +
                " subText: " + subText, Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();

        Bundle extras = sbn.getNotification().extras;
        String tag = sbn.getPackageName();
        String history = Notification.EXTRA_TITLE != null ? extras.getString(Notification.EXTRA_TITLE ):null;
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(sbn.getPostTime()));
        CharSequence finder = extras.getCharSequence(Notification.EXTRA_TEXT);



        Set<String> keys=extras.keySet();
        Log.d(TAG,"onNotificationPosted"+"COUNT"+keys.size());

        for(String _key : keys)
            Log.d(TAG,"onNotificationPosted"+" key : "+_key+" value :"+extras.get(_key));
        Log.d(TAG, "onNotificationPosted ~ " +
                " packageName: " + tag +
                " id: " + sbn.getId() +
                " postTime: " + date +
                " history: " + history +
                " finder : " + finder);

        if (tag != null && history != null) {
            if (tag.equals("com.samsung.android.spay"))
                historySamsungPay(tag, history, date, finder);
            if (tag.equals("com.kakaobank.channel"))
                historyKakaobank(tag, history, date, finder);
        }
    }

    public void historyKakaobank(String tag, String history, String date,CharSequence finder){
        boolean b_usedMoney = Pattern.compile(".*출금.*").matcher(finder).matches();
        boolean b_savedMoney =Pattern.compile(".*입금.*").matcher(finder).matches();

        if (b_usedMoney || b_savedMoney) {
            Log.d(TAG, "onNotificationPosted ~ " + "출처"+ tag);

            String delimeter = " ";
            String[] history_cost = finder.toString().split(delimeter);
            String str = history_cost[4].split("원")[0];

            saveHistory(b_usedMoney,b_savedMoney,date,str);
        } else {
            Log.d(TAG, "onNotificationPosted ~ " + "이것은 노노노");
        }
    }
    public void historySamsungPay(String tag, String history, String date,CharSequence finder){
        boolean b_usedMoney = finder.equals("결제 내역 확인");
        boolean b_savedMoney = finder.equals("결제 취소");

        if (b_usedMoney || b_savedMoney) {
            Log.d(TAG, "onNotificationPosted ~ " + "출처"+ tag);

            String delimeter = " ";
            if (b_usedMoney) delimeter = "₩";
            else if (b_savedMoney) delimeter = "-₩";
            String[] history_cost = history.split(delimeter);
            String str = history_cost[1];

            saveHistory(b_usedMoney,b_savedMoney,date,str);
        } else {
            Log.d(TAG, "onNotificationPosted ~ " + "이것은 노노노");
        }
    }


    public void saveHistory(boolean b_usedMoney,boolean b_savedMoney, String date, String str){
        if (b_usedMoney)          str=date + "   " + str;
        else if (b_savedMoney)    str= date + "   "+"-"+ str;
        Log.d(TAG, "onNotificationPosted ~ " +"b_usedMoney "+ b_usedMoney+" b_savedMoney "+b_savedMoney+"저장될 때 !!"+str);
        File saveFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!saveFile.exists()) { // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedWriter buf = new BufferedWriter(
                    new FileWriter(saveFile + "/paymentHistory.txt", true));

            buf.append(str);
            buf.newLine(); // 개행
            buf.close();
        } catch (FileNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsStrting = sw.toString();

            Log.e(TAG, exceptionAsStrting);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsStrting = sw.toString();

            Log.e(TAG, exceptionAsStrting);
        }
    }
}
