package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SelectTeamActivity extends AppCompatActivity {
    Button[] button = new Button[10];
    Integer[] buttonId = {
            R.id.team1, R.id.team2, R.id.team3, R.id.team4, R.id.team5,
            R.id.team6, R.id.team7, R.id.team8, R.id.team9, R.id.team10 };
    String TAG = "SELECT TEAM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < button.length; i++)
            button[i] = (Button) findViewById(buttonId[i]);
        for (int i = 0; i < button.length; i++) {
            final int IDX;
            IDX = i;
            button[IDX].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button selectedTeam = (Button) view;
                    String myTeamText = selectedTeam.getText().toString();  // 이거 넘겨줄 방법;

                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra("team", myTeamText);
                    Log.d(TAG, myTeamText+" selected");
                    startActivity(intent);
                }
            });
        }
    }


}