package com.example.handylok;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
    private final Context context;
    static final String DB = "Handyrok";
    static final String TABLE = "CONFERENCE";
    static final String CREATE = "CREATE TABLE " + TABLE +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT, " +
            "place TEXT, " +
            "date TEXT, " +
            "contexts TEXT);";

    static final String DROP = "drop table ";
    private SQLiteDatabase db;
    private OpenHelper dbHelper;

    public DBAdapter(Context ctx) {
        context = ctx;
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(Context c) {
            super(c, DB, null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
            db.execSQL(CREATE);
            db.execSQL(DROP + TABLE);
            db.execSQL(CREATE);
        }
    }

    public DBAdapter open() throws SQLException {
        dbHelper = new OpenHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor fetchAllData() {
        return db.query(TABLE, null, null, null, null, null, null);
    }

    public long addData(String name, String place, String date, String context) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("place", place);
        values.put("date", date);
        values.put("contexts", context);

        long id = db.insert(TABLE, null, values);
        return id;
    }

    public void delData(int id) {
        db.delete(TABLE, "_id = " + id, null);
    }

    public void modifyData(long id, String name, String place, String date, String context) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("place", place);
        values.put("date", date);
        values.put("contexts", context);
        db.update(TABLE, values, "_id='" + id + "'", null);
    }

    public Cursor searchDataByName(String name) {
        return db.query(TABLE, null, "name like '%" + name + "%'", null, null, null, null);
    }

}
