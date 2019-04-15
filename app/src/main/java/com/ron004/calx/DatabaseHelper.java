package com.ron004.calx;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "webhealth.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Cursor getAllData() {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"Restaurant", "Item", "Calorie", "Category"};
        String sqlTables = "Food";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        c.moveToFirst();
        return c;
    }

    public Cursor getItems(String restaurant, String category) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("select * from Food where Restaurant = ? AND Category = ?", new String[] {restaurant, category});

        c.moveToFirst();
        return c;
    }

    public Cursor getAllItems(String restaurant) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("select * from Food where Restaurant = ?", new String[] {restaurant});

        c.moveToFirst();
        return c;
    }

    public void writeTrend(String date, int usedcal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Date", date);
        values.put("UsedCal", usedcal);
        db.insert("Trend", null, values);
    }

    public Cursor readTrend() {
        SQLiteDatabase db = getReadableDatabase();

        String getAll = "select * from Trend";
        Cursor c = db.rawQuery(getAll, null);
        c.moveToFirst();
        return c;
    }
}