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
    public enum Card30 {
        KB(1),HD(2),SS(3);
        private int value;
        private Card30(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }

    }
    Calendar cal = Calendar.getInstance();
    String format = "yyyy-MM-dd";
    String date = new SimpleDateFormat(format).format(cal.getTime());
    int YEAR = cal.get(Calendar.YEAR);
    int MONTH = cal.get(Calendar.MONTH)+1;

    private int monthFirstDay = cal.getMinimum(Calendar.DATE);
    private int monthLastDay = cal.getActualMaximum (Calendar.DAY_OF_MONTH);

    int [] card30 = new int[5];

    TextView textView;
    TextView totalView;
    TextView tableTitle;

    TextView weekView;


    Button btn1;
    Button btnAccountBook;
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
        btnAccountBook = (Button)findViewById(R.id.buttonAccountBook);
        btnAccountBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AccountBookYear.class);
                startActivity(intent);
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
        int [] weekUsedMoney = new int[7];

        File loadFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String paymentInfo = null;
        if (!loadFile.exists()) { // 폴더 없을 경우
            loadFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedReader buf = new BufferedReader(
                    new FileReader(loadFile + "/paymentHistory.txt"));

            while((paymentInfo=buf.readLine())!=null){
                Log.d(TAG, "onNotificationPosted ~ " + "읽는다");
                String[] info = paymentInfo.toString().split("   ");
                int week = getWeekOfYear(info[0]);
                Log.d(TAG, "onNotificationPosted ~ " + "몆주차 : " +week +"  "+info[0]);
                if(firstWeek <= week && week <= lastWeek) {
                    int money = Integer.parseInt(info[1].replace(",", ""));
                    weekUsedMoney[week - firstWeek + 1] += money;

                    if (info.length > 2)
                        updateAccept30Card(info[2], money);
                }
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i = 1;i <6;i++) {
            int resId = getResources().getIdentifier("tableTextMoney" + i, "id", this.getPackageName());
            weekView = (TextView) findViewById(resId);
            weekView.setText(weekUsedMoney[i] + "원");
        }

        for(int i = 1 ;i<=4;i++) {
            int resId = getResources().getIdentifier("card"+i, "id", this.getPackageName());
            weekView = (TextView) findViewById(resId);
            if(weekView != null) weekView.setText(card30[i] + "원");
        }
        return;
    }

    public void updateAccept30Card(String info,int money){
        Log.d(TAG, "onNotificationPosted ~ " + "updateAccept30Card : "+info + " "+money);
        Card30 kind = null;
        if(info.equals("KB비씨")){
            kind = Card30.KB;
            Log.d(TAG, "onNotificationPosted ~ " + "잘들어왔네" + kind.getValue());
            card30[kind.getValue()]+= money;
        }else
            Log.d(TAG, "onNotificationPosted ~ " + "다름");
    }
    int getWeekOfYear(String date) {//일~토
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