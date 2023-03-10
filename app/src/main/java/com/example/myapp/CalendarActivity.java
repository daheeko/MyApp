package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class CalendarActivity extends AppCompatActivity {

    String matchURL="";
    String myTeam;
    int tYear, tMonth, tDay;
    CalendarView calendarView;
    LinearLayout diaryView, buttonsView1, buttonsView2;
    TextView titleView, matchInfo, savedDiaryView, noMatchInfo;
    EditText editDiaryView;
    CheckBox liveCheck;
    Button expandButton, scoreButton, saveButton, cancelButton, editButton, delButton;
    String tDiary, fileName, filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        myTeam = intent.getStringExtra("team");

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        filePath="/data/data/"+this.getPackageName()+"/files/";

        setContentView(R.layout.activity_calendar);
        titleView = findViewById(R.id.calendarTitle);
        calendarView = findViewById(R.id.calendarView);
        diaryView = findViewById(R.id.diaryView);
        buttonsView1 = findViewById(R.id.layoutButtons1);
        buttonsView2 = findViewById(R.id.layoutButtons2);
        matchInfo = findViewById(R.id.matchInfo);
        liveCheck = findViewById(R.id.liveCheck);
        expandButton = findViewById(R.id.expandButton);
        scoreButton = findViewById(R.id.scoreButton);
        editDiaryView = findViewById(R.id.editDiary);
        savedDiaryView = findViewById(R.id.savedDiary);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        editButton = findViewById(R.id.editButton);
        delButton = findViewById(R.id.delButton);
        noMatchInfo = findViewById(R.id.noMatchInfo);

        titleView.setText(myTeam+" Diary");
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                tYear = year;
                tMonth = month+1;
                tDay = dayOfMonth;
                tDiary = "";
                fileName = year+dateFormat(tMonth)+dateFormat(tDay)+teamInitial(myTeam)+".txt";

                editDiaryView.setText(tDiary);
                // ?????? ????????? ??? ?????? ????????? matchInfo??? setText & diaryView VISIBLE
                // ????????? noMatchInfo VISIBLE
                String match_id = getMatch(dbHelper, myTeam, tYear, tMonth, tDay);
                if(match_id != null){
                    matchURL = getMatchURL(match_id, tYear, tMonth, tDay);
                    noMatchInfo.setVisibility(View.GONE);
                    // ???????????? ????????? ?????? ??????, ????????? ?????? ??????
                    tDiary = checkDiary(tYear, tMonth, tDay);  // ????????? ????????? ?????? or ""
                    setDiaryView(tDiary);
                }
                else{
                    diaryView.setVisibility(View.GONE);
                    noMatchInfo.setText("????????? ????????????");
                    noMatchInfo.setVisibility(View.VISIBLE);
                }
                // ?????? ???????????? ????????? ??? ????????????
                if(getLiveChecked(dbHelper, tMonth, tDay, myTeam) == 1){
                    liveCheck.setChecked(true);
                }
                else
                    liveCheck.setChecked(false);
            }
        });
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tDiary = editDiaryView.getText().toString();

            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tDiary = editDiaryView.getText().toString();
                saveDiary(tDiary, fileName);  // ???????????? ??????
                setDiaryView(tDiary);
                int liveChecked;
                if(liveCheck.isChecked())
                    liveChecked = 1;
                else
                    liveChecked = 0;
                saveLiveChecked(dbHelper, tMonth, tDay, myTeam, liveChecked);

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String file = filePath+fileName;
                if(new File(file).exists()){  // ?????? ?????? ?????? ?????? ????????? ?????? ????????? ??? ??????
                    editDiaryView.setVisibility(View.GONE);
                    savedDiaryView.setVisibility(View.VISIBLE);
                    buttonsView1.setVisibility(View.GONE);
                    buttonsView2.setVisibility(View.VISIBLE);
                }
                else{  // ?????? ?????? ?????? ?????? ?????? ????????? ?????? ????????????
                    editDiaryView.setText("");
                    editDiaryView.setHint("????????? ??????????????????");
                }
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {  // setDiaryView -> buttons1 ??? ????????? buttons2 ????????? ??????
            @Override
            public void onClick(View view) {
                tDiary = savedDiaryView.getText().toString();
                editDiaryView.setText(tDiary);
                savedDiaryView.setVisibility(View.GONE);
                buttonsView1.setVisibility(View.VISIBLE);  // ?????? ?????? ?????? ?????????
                buttonsView2.setVisibility(View.GONE);  // ?????? ?????? ??? ?????????
                editDiaryView.setVisibility(View.VISIBLE);  // ?????????
            }
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????? ??????
                tDiary = "";
                liveCheck.setChecked(false);
                saveLiveChecked(dbHelper, tMonth, tDay, myTeam, 0);
                setDiaryView(tDiary);
                deleteDiary(fileName);

            }
        });
        scoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = matchURL;
                Log.d("URL", url);
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(urlIntent);
            }
        });
    }

    public String getMatchURL(String match_id, int year, int month, int dayOfMonth){
        int date = Integer.parseInt(dateFormat(month) + dateFormat(dayOfMonth));
        String year_id;
        if(date < 1013)
            year_id = String.valueOf(year);
        else if(date == 1013)  // ??????
            year_id = "4444";
        else if(date <= 1022 )  // ?????????
            year_id = "3333";
        else if(date >= 1024 && date < 1101)  // ??????
            year_id = "5555";
        else
            year_id = "7777";  // ??????

        match_id = year_id + match_id;
        String url = String.format("https://m.sports.naver.com/game/%s/record", match_id);
        return url;
    }

    public String dateFormat(int date){
        if(date>0 && date<10)
            return "0" + String.valueOf(date);
        else
            return String.valueOf(date);
    }

    public String teamInitial(String teamName){
        HashMap<String, String> teams = new HashMap<>();
        teams.put("SSG", "SK");
        teams.put("??????", "WO");
        teams.put("LG", "LG");
        teams.put("KT", "KT");
        teams.put("KIA", "HT");
        teams.put("NC", "NC");
        teams.put("??????", "SS");
        teams.put("??????", "LT");
        teams.put("??????", "OB");
        teams.put("??????", "HH");
        return teams.get(teamName);
    }
    public String getMatch(DataBaseHelper myHelper, String team, int year, int month, int dayOfMonth){
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MatchTime, AwayTeam, HomeTeam, Stadium FROM kbo2022 WHERE MONTH="+ month +
                " AND DAY=" + dayOfMonth + " AND (HomeTeam='"+ team + "' OR AwayTeam='" + team + "')", null);
        if(cursor.getCount()==0){
            return null;
        }
        else{
            cursor.moveToFirst();
            String match_time = cursor.getString(0);
            String away_team = cursor.getString(1);
            String home_team = cursor.getString(2);
            String stadium = cursor.getString(3);

            String match_text = home_team + ":" + away_team + "\t\t" + stadium + "\t\t" + match_time;
            matchInfo.setText(match_text);

            // [??????4??????][???????????????][??????????????????][???????????????][??????]
            String match_id = dateFormat(month)+dateFormat(dayOfMonth)+teamInitial(away_team)+teamInitial(home_team)+"0"+year;
            return match_id;
        }
    }
    public void saveLiveChecked(DataBaseHelper myHelper, int month, int dayOfMonth, String team, int liveChecked){
        String sql = "UPDATE kbo2022 SET GameLive=" + liveChecked + " WHERE MONTH= "+ month + " AND DAY=" + dayOfMonth +
                " AND (HomeTeam='"+ team + "' OR AwayTeam='" + team + "')";
        try{
            SQLiteDatabase db = myHelper.getWritableDatabase();
            db.execSQL(sql);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public int getLiveChecked(DataBaseHelper myHelper, int month, int dayOfMonth, String team){
        SQLiteDatabase db = myHelper.getReadableDatabase();
        String sql = "SELECT GameLive FROM kbo2022 WHERE MONTH="+ month +
                " AND DAY=" + dayOfMonth + " AND (HomeTeam='"+ team + "' OR AwayTeam='" + team + "')";
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.getCount()==0){
            return 0;
        }
        else {
            cursor.moveToFirst();
            int isChecked = cursor.getInt(0);
            return isChecked;
        }
    }
    public String checkDiary(int year, int month, int dayOfMonth) {
        // ????????????????????? ?????? ????????? ?????? ????????? ??????
        String filename = year+dateFormat(tMonth)+dateFormat(tDay)+teamInitial(myTeam)+".txt";
        String diary= "";
        FileInputStream fis;
        try{
            fis = openFileInput(filename);
            byte[] txt = new byte[1000];
            fis.read(txt);
            fis.close();
            diary = (new String(txt)).trim();
        }catch(IOException e){
            editDiaryView.setHint("????????? ??????????????????");
        }
        // ???????????? ???????????? ?????? ????????????
        return diary;
    }

    public void setDiaryView(String diary){
        if(diary != null && !diary.equals("")){  // ???????????? ?????????
            savedDiaryView.setText(diary);  // ????????? ?????? textView??? ?????????
            editDiaryView.setVisibility(View.GONE);  // editText ??? ??????
            buttonsView1.setVisibility(View.GONE);  // ?????? ?????? ??? ?????????
            diaryView.setVisibility(View.VISIBLE);  // ???????????? ??? ????????? ???
            savedDiaryView.setVisibility(View.VISIBLE);  // ???????????? ????????? textView
            buttonsView2.setVisibility(View.VISIBLE);  // ?????? ?????? ?????????
        }
        else{  // ?????????
            savedDiaryView.setVisibility(View.GONE);
            buttonsView2.setVisibility(View.GONE);
            editDiaryView.setText("");
            editDiaryView.setHint("????????? ??????????????????");
            diaryView.setVisibility(View.VISIBLE);  // ???????????? ??? ????????? ???
            editDiaryView.setVisibility(View.VISIBLE);  // ???????????? ??? editText
            buttonsView1.setVisibility(View.VISIBLE);
        }
    }

    public void deleteDiary(String filename){
        FileOutputStream fos = null;
        try{
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            String content = "";
            fos.write((content).getBytes());
            fos.close();
            Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
        }catch (IOException e){ }
    }

    public void saveDiary(String diary, String filename){
        if(diary.equals("")){
            Toast.makeText(getApplicationContext(), "?????? ??? ?????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
        }
        else{
            try{
                FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(diary.getBytes());
                fos.close();
                Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
            }catch (IOException e){}
        }
    }
}