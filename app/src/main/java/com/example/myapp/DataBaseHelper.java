package com.example.myapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "DataBaseHelper";
    private static String KBO_DB_PATH = "";
    private static String KBO_DB_NAME = "kbo2022.db";
    private SQLiteDatabase mDataBase;
    private Context mContext;

    public DataBaseHelper(@Nullable Context context) {
        super(context, KBO_DB_NAME, null, DATABASE_VERSION);
        KBO_DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;
        dataBaseCheck();
    }

    private void dataBaseCheck(){
        File dbFile = new File(KBO_DB_PATH+KBO_DB_NAME);
        if(!dbFile.exists()){
            dbCopy();
            Log.d(TAG, "Database is copied");
        }
    }

    @Override
    public synchronized void close(){
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        Log.d(TAG, "onOpen() : DB Opening");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
        Log.d(TAG,"onUpgrade() : DB Schema Modified and Executing onCreate()");
    }

    private void dbCopy(){
        try{
            File folder = new File(KBO_DB_PATH);
            if(!folder.exists())
                folder.mkdir();
            Log.d("dbCopy", "mk folder");
            InputStream inputStream = mContext.getAssets().open(KBO_DB_NAME);
            Log.d("dbCopy", "inputstream");
            String out_filename = KBO_DB_PATH + KBO_DB_NAME;
            Log.d("dbCopy", "out_filename="+out_filename);
            OutputStream outputStream = new FileOutputStream(out_filename);
            Log.d("dbCopy", "outputStream");
            byte[] mBuffer = new byte[1024];
            int mLength;
            while((mLength = inputStream.read(mBuffer)) > 0)
                outputStream.write(mBuffer, 0, mLength);
            Log.d("dbCopy", "writed");
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch(IOException e){
            e.printStackTrace();
            Log.d("dbCopy", "IOException");
        }

    }

}