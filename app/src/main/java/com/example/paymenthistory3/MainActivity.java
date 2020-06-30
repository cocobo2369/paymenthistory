package com.example.paymenthistory3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.BatchUpdateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    Calendar cal = Calendar.getInstance();
    String format = "yyyy-MM-dd";
    String date = new SimpleDateFormat(format).format(cal.getTime());
    int YEAR = cal.get(Calendar.YEAR);
    int MONTH = cal.get(Calendar.MONTH)+1;
    int monthFirstDay = cal.getMinimum(Calendar.DATE);
    int monthLastDay = cal.getActualMaximum (Calendar.DAY_OF_MONTH);

    

    TextView textView;
    TextView totalView;
    TextView tableTitle;

    TextView weekView[] = new TextView[5];


    Button btn1;
    int WEEEKMONEY = 100000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableTitle = (TextView)findViewById(R.id.tableTitle);

        tableTitle.setText(""+MONTH+"월");

        totalView= (TextView)findViewById(R.id.textView2);
        textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                howMuchUsedMoney();
            }
        });
        updateUsedMoneyOfMonth();
        if (!permissionGrantred()) {
            Intent intent = new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }

    public void howMuchUsedMoney(){
        int total = WEEEKMONEY;
        File loadFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String paymentInfo = null;
        if (!loadFile.exists()) { // 폴더 없을 경우
            loadFile.mkdir(); // 폴더 생성
        }
        try {
            Log.d(TAG, "onNotificationPosted ~ " + "눌렀다");
            BufferedReader buf = new BufferedReader(
                    new FileReader(loadFile + "/paymentHistory.txt"));

            textView.setText(null);

            int todayWeek = getWeekOfYear("");
            while((paymentInfo=buf.readLine())!=null){
                Log.d(TAG, "onNotificationPosted ~ " + "읽는다");
                String[] temp = paymentInfo.toString().split("   ");
                int week = getWeekOfYear(temp[0]);
                Log.d(TAG, "onNotificationPosted ~ " + "몆주차 : " +week +"  "+temp[0]);

                if(todayWeek == week) {
                    textView.append("[*]"+paymentInfo);
                    total -= Integer.parseInt(temp[1].replace(",",""));
                    Log.d(TAG, "onNotificationPosted ~ " + "사용된 금액 :"+total);
                }else{
                    int firstWeek = getWeekOfYear(makeDateFormat(YEAR,MONTH,monthFirstDay));
                    if(week > firstWeek) textView.append("["+(week-firstWeek+1)+"째주]"+paymentInfo);
                    else textView.append("[last]"+paymentInfo);
                }
                textView.append("\n");
            }
            totalView.setText(total+"");

            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String makeDateFormat(int year, int month, int day){
        String Year = String.valueOf(year);
        String Month;
        String Day ;
        if(month < 10) Month = "0"+month;
        else Month = ""+month;

        if(day < 10) Day = "0"+day;
        else Day = ""+day;
        return Year+"-"+Month+"-"+Day;
    }
    public void updateUsedMoneyOfMonth(){
        int firstWeek = getWeekOfYear(makeDateFormat(YEAR,MONTH,monthFirstDay));
        int lastWeek =  getWeekOfYear(makeDateFormat(YEAR,MONTH,monthLastDay));

        for(int i = firstWeek ;i <= lastWeek;i++){

            int total = 0;
            File loadFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            String paymentInfo = null;
            if (!loadFile.exists()) { // 폴더 없을 경우
                loadFile.mkdir(); // 폴더 생성
            }
            try {
                BufferedReader buf = new BufferedReader(
                        new FileReader(loadFile + "/paymentHistory.txt"));

                int todayWeek =i;
                while((paymentInfo=buf.readLine())!=null){
                    Log.d(TAG, "onNotificationPosted ~ " + "읽는다");
                    String[] temp = paymentInfo.toString().split("   ");
                    int week = getWeekOfYear(temp[0]);
                    Log.d(TAG, "onNotificationPosted ~ " + "몆주차 : " +week +"  "+temp[0]);

                    if(todayWeek == week)
                        total += Integer.parseInt(temp[1].replace(",",""));
                }
                int resId = getResources().getIdentifier("tableTextMoney"+(i-firstWeek+1),"id",this.getPackageName());
                weekView[i-firstWeek] = (TextView)findViewById(resId);
                weekView[i-firstWeek].setText(total+"원");
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return;
    }

    private int getWeekOfYear(String date) {//일~토
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar calendar = Calendar.getInstance();
        if(date !="") {
            String[] dates = date.split("-");
            int year = Integer.parseInt(dates[0]);
            int month = Integer.parseInt(dates[1])-1;
            int day = Integer.parseInt(dates[2]);
            calendar.set(year, month, day);
        }
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }


    private boolean permissionGrantred() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (sets != null && sets.contains(getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

}