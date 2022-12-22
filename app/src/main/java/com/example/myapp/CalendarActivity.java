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
    Button scoreButton, saveButton, cancelButton, editButton, delButton;
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
                // 해당 날짜에 팀 경기 있으면 matchInfo에 setText & diaryView VISIBLE
                // 없으면 noMatchInfo VISIBLE
                String match_id = getMatch(dbHelper, myTeam, tYear, tMonth, tDay);
                if(match_id != null){
                    matchURL = getMatchURL(match_id, tYear, tMonth, tDay);
                    noMatchInfo.setVisibility(View.GONE);
                    // 다이어리 있으면 수정 삭제, 없으면 저장 취소
                    tDiary = checkDiary(tYear, tMonth, tDay);  // 파일에 저장된 내용 or ""
                    setDiaryView(tDiary);
                }
                else{
                    diaryView.setVisibility(View.GONE);
                    noMatchInfo.setText("경기가 없습니다");
                    noMatchInfo.setVisibility(View.VISIBLE);
                }
                // 직관 체크박스 저장된 거 불러오기
                if(getLiveChecked(dbHelper, tMonth, tDay, myTeam) == 1){
                    liveCheck.setChecked(true);
                }
                else
                    liveCheck.setChecked(false);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tDiary = editDiaryView.getText().toString();
                saveDiary(tDiary, fileName);  // 다이어리 저장
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
                if(new File(file).exists()){  // 수정 중에 취소 누른 경우는 원래 저장된 거 뜨게
                    editDiaryView.setVisibility(View.GONE);
                    savedDiaryView.setVisibility(View.VISIBLE);
                    buttonsView1.setVisibility(View.GONE);
                    buttonsView2.setVisibility(View.VISIBLE);
                }
                else{  // 처음 작성 중에 취소 누른 경우는 초기 세팅으로
                    editDiaryView.setText("");
                    editDiaryView.setHint("경기를 기록해보세요");
                }
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {  // setDiaryView -> buttons1 안 보이고 buttons2 보이는 상태
            @Override
            public void onClick(View view) {
                tDiary = savedDiaryView.getText().toString();
                editDiaryView.setText(tDiary);
                savedDiaryView.setVisibility(View.GONE);
                buttonsView1.setVisibility(View.VISIBLE);  // 다시 저장 취소 보이고
                buttonsView2.setVisibility(View.GONE);  // 수정 삭제 안 보이고
                editDiaryView.setVisibility(View.VISIBLE);  // 수정창
            }
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 파일 삭제
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
        else if(date == 1013)  // 와카
            year_id = "4444";
        else if(date <= 1022 )  // 준플옵
            year_id = "3333";
        else if(date >= 1024 && date < 1101)  // 플옵
            year_id = "5555";
        else
            year_id = "7777";  // 코시

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
        teams.put("키움", "WO");
        teams.put("LG", "LG");
        teams.put("KT", "KT");
        teams.put("KIA", "HT");
        teams.put("NC", "NC");
        teams.put("삼성", "SS");
        teams.put("롯데", "LT");
        teams.put("두산", "OB");
        teams.put("한화", "HH");
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

            // [월일4글자][홈팀이니셜][원정팀이니셜][경기아이디][연도]
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
        // 내부저장소에서 해당 날짜에 기록 있는지 확인
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
            editDiaryView.setHint("경기를 기록해보세요");
        }
        // 파일에서 다이어리 내용 받아오기
        return diary;
    }

    public void setDiaryView(String diary){
        if(diary != null && !diary.equals("")){  // 다이어리 있으면
            savedDiaryView.setText(diary);  // 저장된 내용 textView에 띄우고
            editDiaryView.setVisibility(View.GONE);  // editText 안 보게
            buttonsView1.setVisibility(View.GONE);  // 저장 취소 안 보이게
            diaryView.setVisibility(View.VISIBLE);  // 다이어리 뷰 보여야 함
            savedDiaryView.setVisibility(View.VISIBLE);  // 다이어리 보여줄 textView
            buttonsView2.setVisibility(View.VISIBLE);  // 수정 삭제 보이게
        }
        else{  // 없으면
            savedDiaryView.setVisibility(View.GONE);
            buttonsView2.setVisibility(View.GONE);
            editDiaryView.setText("");
            editDiaryView.setHint("경기를 기록해보세요");
            diaryView.setVisibility(View.VISIBLE);  // 다이어리 뷰 보여야 함
            editDiaryView.setVisibility(View.VISIBLE);  // 다이어리 쓸 editText
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
            Toast.makeText(getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
        }catch (IOException e){ }
    }

    public void saveDiary(String diary, String filename){
        if(diary.equals("")){
            Toast.makeText(getApplicationContext(), "작성 후 저장 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
        }
        else{
            try{
                FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(diary.getBytes());
                fos.close();
                Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
            }catch (IOException e){}
        }
    }
}