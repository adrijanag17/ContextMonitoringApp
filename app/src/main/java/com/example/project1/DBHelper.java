package com.example.project1;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "healthDB";
    private static final int DATABASE_ID = 1;
    private static final String TABLE_HEALTH = "signsSymptoms";
    private static final String KEY_ID = "id";
    private static final String KEY_HEART = "heartRate";
    private static final String KEY_RESP = "respRate";
    private static final String KEY_NAUSEA = "nausea";
    private static final String KEY_HEADACHE = "headache";
    private static final String KEY_DIARRHEA = "diarrhea";
    private static final String KEY_THROAT = "soreThroat";
    private static final String KEY_FEVER = "fever";
    private static final String KEY_MUSCLE = "muscleAche";
    private static final String KEY_SMELLTASTE = "smellTaste";
    private static final String KEY_COUGH = "cough";
    private static final String KEY_BREATH = "breathShortness";
    private static final String KEY_TIRED = "tired";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_ID);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_HEALTH + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_HEART + " TEXT," + KEY_RESP + " TEXT," + KEY_NAUSEA + " INTEGER," + KEY_HEADACHE +
                " INTEGER," + KEY_DIARRHEA + " INTEGER," + KEY_THROAT + " INTEGER," + KEY_FEVER + " INTEGER," +
                KEY_MUSCLE + " INTEGER," + KEY_SMELLTASTE + " INTEGER," + KEY_COUGH + " INTEGER," +
                KEY_BREATH + " INTEGER," + KEY_TIRED + " INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH);
        onCreate(db);

    }

    public long addHeartResp(String heart, String resp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HEART, heart);
        values.put(KEY_RESP, resp);
        long id = db.insert(TABLE_HEALTH, null,  values);
        return id;
    }


    public void addSymptoms(int[] symptoms, long id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAUSEA, symptoms[0]);
        values.put(KEY_HEADACHE, symptoms[1]);
        values.put(KEY_DIARRHEA, symptoms[2]);
        values.put(KEY_THROAT, symptoms[3]);
        values.put(KEY_FEVER, symptoms[4]);
        values.put(KEY_MUSCLE, symptoms[5]);
        values.put(KEY_SMELLTASTE, symptoms[6]);
        values.put(KEY_COUGH, symptoms[7]);
        values.put(KEY_BREATH, symptoms[8]);
        values.put(KEY_TIRED, symptoms[9]);

        db.update(TABLE_HEALTH, values, "id=" + id, null);
    }
}
