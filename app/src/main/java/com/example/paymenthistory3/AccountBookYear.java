package com.example.paymenthistory3;

import android.app.AppComponentFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class AccountBookYear extends MainActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_book);
        updateUsedMoneyOfMonth2();
    }

    public void updateUsedMoneyOfMonth2(){ //updateUsedMoneyOfMonth 로 선언해서 쓸 경우 부모의 메소드가 호출되는지 에러가 남
        Log.d(TAG, "onNotificationPosted ~ " + "자식호출");
        Calendar cal = Calendar.getInstance();
        int monthFirstDay = cal.getMinimum(Calendar.DATE);
        int monthLastDay = cal.getActualMaximum (Calendar.DAY_OF_MONTH);
        int firstWeek = getWeekOfYear(makeDateFormat(YEAR,MONTH,monthFirstDay));
        int lastWeek =  getWeekOfYear(makeDateFormat(YEAR,MONTH,monthLastDay));

        int [] weekUsedMoney = new int[53];

        File loadFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String paymentInfo = null;
        if (!loadFile.exists()) { // 폴더 없을 경우
            loadFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedReader buf = new BufferedReader(
                    new FileReader(loadFile + "/paymentHistory.txt"));


            int total = 0;
            while((paymentInfo=buf.readLine())!=null){
                Log.d(TAG, "onNotificationPosted ~ " + "읽는다");
                String[] temp = paymentInfo.toString().split("   ");

                int week = getWeekOfYear(temp[0]);
                Log.d(TAG, "onNotificationPosted ~ " + "몆주차 : " +week +"  "+temp[0]);
                weekUsedMoney[week]  += Integer.parseInt(temp[1].replace(",",""));
            }

            for(int i = 0 ;i<53;i++)
                Log.d(TAG, "onNotificationPosted ~ " + i+" "+weekUsedMoney[i]);
            //int resId = getResources().getIdentifier("tableTextMoney"+(i-firstWeek+1),"id",this.getPackageName());
            //weekView[i-firstWeek] = (TextView)findViewById(resId);
            //weekView[i-firstWeek].setText(total+"원");
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateAccountBook(weekUsedMoney);
        return;
    }

    public void updateAccountBook(int [] weekUsedMoney){
        Calendar mcal = Calendar.getInstance();
        for(int m = 1 ; m<= 12; m++){
            mcal.set(Calendar.MONTH,m-1);
            int monthFirstDay = mcal.getMinimum(Calendar.DATE);
            int monthLastDay = mcal.getActualMaximum (Calendar.DAY_OF_MONTH);
            int firstWeek = getWeekOfYear(makeDateFormat(YEAR,m,monthFirstDay));
            int lastWeek =  getWeekOfYear(makeDateFormat(YEAR,m,monthLastDay));
            TextView weekView =null;
            int resId=0;
            int month_total=0;
            for(int month_week = firstWeek; month_week <= lastWeek;month_week++){
                resId = getResources().getIdentifier("month"+m+"_money"+(month_week-firstWeek+1),"id",this.getPackageName());
                Log.d(TAG, "onNotificationPosted ~ resource " + resId+"/"+month_week+"/"+"month"+m+"_money"+(month_week-firstWeek+1));
                weekView = (TextView)findViewById(resId);
                Log.d(TAG, "onNotificationPosted ~ weekView   " + weekView);
                month_total+=weekUsedMoney[month_week];

                weekView.setText(weekUsedMoney[month_week]+"원");

            }
            resId = getResources().getIdentifier("month"+m+"_total","id",this.getPackageName());
            weekView = (TextView)findViewById(resId);
            if(weekView != null)
                weekView.setText(month_total+"원");
        }
    }
}
