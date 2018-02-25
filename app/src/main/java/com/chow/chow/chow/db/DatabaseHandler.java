package com.chow.chow.chow.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    public DatabaseHandler(Context context) {
        super(context, "tracks", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TRACKS_TABLE = "CREATE TABLE food (" +
                "time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "meal TEXT," +
                "calories TEXT)";

        db.execSQL(CREATE_TRACKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS tracks");
        onCreate(db);

    }


    public void insertRow(String meal, String calories) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("meal", meal);
        values.put("calories", calories);

        db.insert("food", null, values);
        db.close();

    }


    public List<Food> getAllFood() {

        List<Food> trackList = new ArrayList<Food>();
        String selectQuery = "SELECT calories, meal, time FROM food WHERE time >= date('now', '-1 days')";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            do {

                Food track = new Food();
                track.calories = cursor.getString(0);
                track.meal = cursor.getString(1);
                track.time = cursor.getString(2);
                trackList.add(track);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return trackList;

    }

}