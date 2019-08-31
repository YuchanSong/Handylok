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
    static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT, " +
            "place TEXT, " +
            "date TEXT, " +
            "contexts TEXT);";

    // Table Names
    private static final String DB_TABLE = "table_image";
    // column names
    private static final String KEY_NAME = "image_id";
    private static final String KEY_IMAGE = "image_data";
    // Table create statement
    private static final String CREATE_TABLE_IMAGE = "CREATE TABLE " + DB_TABLE + "("+
            KEY_NAME + " TEXT," +
            KEY_IMAGE + " BLOB);";

    static final String DROP = "drop table ";
    private SQLiteDatabase db;
    private OpenHelper dbHelper;

    public DBAdapter(Context ctx) {
        context = ctx;
    }

    public class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(Context c) {
            super(c, DB, null, 2);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE);
            db.execSQL(CREATE_TABLE_IMAGE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
            db.execSQL(CREATE);
            db.execSQL(DROP + TABLE);
            db.execSQL(CREATE);

            db.execSQL(CREATE_TABLE_IMAGE);
            db.execSQL(DROP + DB_TABLE);
            db.execSQL(CREATE_TABLE_IMAGE);
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

    public void modifyData(int id, String name, String place, String date, String context) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("place", place);
        values.put("date", date);
        values.put("contexts", context);

        db.update(TABLE, values, "_id = " + id, null);
    }

    public Cursor searchDataByColumn(String column, String data) {
        return db.query(TABLE, null, column + " like '%" + data + "%'", null, null, null, null);
    }

}
