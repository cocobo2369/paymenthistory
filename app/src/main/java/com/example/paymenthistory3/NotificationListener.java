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

            if (tag.equals("com.kakaobank.channel"))
                historyKakaobank(tag, history, date, finder);
            else if (tag.equals("com.kbcard.kbkookmincard")) {
                Log.d(TAG, "onNotificationPosted ~ " + "진입전 출처" + tag);
                historyKBcard(tag, history, date, finder);
            }
            else if (tag.equals("com.hyundaicard.appcard"))
                historyHyundaicard(tag, history, date, finder);
            //else if (tag.equals("com.samsung.android.spay"))
               // historySamsungPay(tag, history, date, finder);
        }
    }
    public String specialTagForCard30(String str, String base){
        if(str.toString().contains("KB국민비씨1*4*"))
            return "KB비씨";
        return base;
    }
    public void historyKakaobank(String tag, String history, String date,CharSequence finder){
        //boolean b_usedMoney = Pattern.compile(".*출금.*").matcher(finder).matches();
        //boolean b_savedMoney =Pattern.compile(".*입금.*").matcher(finder).matches();
        boolean b_usedMoney = finder.toString().contains("출금");
        boolean b_savedMoney =finder.toString().contains("입금");
        if (b_usedMoney || b_savedMoney) {
            Log.d(TAG, "onNotificationPosted ~ " + "출처"+ tag);

            String delimeter = " ";
            String[] history_cost = finder.toString().split(delimeter);
            String str = history_cost[4].split("원")[0];

            saveHistory(b_usedMoney,b_savedMoney,date,str,"카뱅");
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

            saveHistory(b_usedMoney,b_savedMoney,date,str,"삼페");
        } else {
            Log.d(TAG, "onNotificationPosted ~ " + "이것은 노노노");
        }
    }
    public void historyKBcard(String tag, String history, String date,CharSequence finder){
        //boolean b_usedMoney = Pattern.compile(".*승인.*").matcher(finder).matches();
        //boolean b_savedMoney =Pattern.compile(".*취소.*").matcher(finder).matches();
        boolean b_usedMoney = finder.toString().contains("승인");
        boolean b_savedMoney =finder.toString().contains("취소");
        if (b_usedMoney || b_savedMoney) {
            Log.d(TAG, "onNotificationPosted ~ " + "출처"+ tag);
            Log.d(TAG, "onNotificationPosted ~ " + "국민카드");

            /*KB국민비씨1*4*승인(or 취소)
            최*일
            30,400원 일시불
            07/01 22:55
            G마켓
            누적372,280원*/

            String delimeter = "\n";
            String[] history_info = finder.toString().split(delimeter);
            String money = history_info[2].split("원")[0];
            String tagCard = specialTagForCard30(history_info[0],"KB");
            saveHistory(b_usedMoney,b_savedMoney,date,money,tagCard);
        } else {
            Log.d(TAG, "onNotificationPosted ~ " + "이것은 노노노");
        }
    }
    public void historyHyundaicard(String tag, String history, String date,CharSequence finder){
        //boolean b_usedMoney = Pattern.compile("^(?!.*취소).*승인.*$").matcher(finder).matches();
        //boolean b_savedMoney =Pattern.compile(".*승인취소.*").matcher(finder).matches();
        boolean b_usedMoney = finder.toString().contains("승인 ");
        boolean b_savedMoney =finder.toString().contains("승인취소");
        if (b_usedMoney || b_savedMoney) {
            Log.d(TAG, "onNotificationPosted ~ " + "출처"+ tag);
            Log.d(TAG, "onNotificationPosted ~ " + "현대카드");

            String delimeter = " ";
            String[] history_cost = finder.toString().split(delimeter);
            String str = null;
            if(b_usedMoney) str =history_cost[2].split("원")[0]; //최*일님, 스마일카드승인 33,500원 일시불 07/02 00:20
            else if(b_savedMoney) str =history_cost[3].split("원")[0]; //최*일님, 스마일카드 승인취소 33,500원 일시불 07/02 00:21

            saveHistory(b_usedMoney,b_savedMoney,date,str,"현대");
        } else {
            Log.d(TAG, "onNotificationPosted ~ " + "이것은 노노노");
        }
    }


    public void saveHistory(boolean b_usedMoney,boolean b_savedMoney, String date, String str, String tag){
        if (b_usedMoney)          str=date + "   " + str;
        else if (b_savedMoney)    str= date + "   "+"-"+ str;
        str = str + "   "+tag;
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
