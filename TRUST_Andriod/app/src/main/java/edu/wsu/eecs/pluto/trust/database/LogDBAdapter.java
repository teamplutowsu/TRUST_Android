package edu.wsu.eecs.pluto.trust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Created by Kameron on 1/22/2017.
 */

public class LogDBAdapter {
    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1; //If we change the database INCREMENT THIS!!!
    private LogDbHelper logDbHelper;
    // Define table that holds the logs.
    private static final String NOTE_TABLE = "log";
    private static final String COLUMN_EVENTNUM = "event_num";
    private static final String COLUMN_EVENT = "event";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_PLATFORM = "platform";

    //private static final String COLUMN_LOCATION = "location";

    private String[] allColumns = {COLUMN_EVENTNUM,COLUMN_EVENT,COLUMN_ID,COLUMN_DATE,COLUMN_TIME, COLUMN_PLATFORM};//COLUMN_LOCATION};

    public static final String DATABASE_CREATE_TABLE = "create table " +
            NOTE_TABLE + " ( " + COLUMN_EVENTNUM + " integer primary key autoincrement, " +
            COLUMN_EVENT+ " text not null, "+ COLUMN_ID+ " text not null," + COLUMN_DATE+ " text not null, " + COLUMN_TIME + " text not null, " +
            COLUMN_PLATFORM + " text not null);";
    //COLUMN_LOCATION + ");";

    private SQLiteDatabase sqlDB;
    private Context context;

    public LogDBAdapter(Context ctx){
        context = ctx;
    }

    public LogDBAdapter open() throws android.database.SQLException{
        logDbHelper = new LogDbHelper(context);
        sqlDB = logDbHelper.getWritableDatabase();
        return this;
    }

    public ArrayList<AppLog> getAllLogs(){
        ArrayList<AppLog> logs = new ArrayList<>();

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, null, null, null, null, null);

        //Go thought the DB and write most recent log first.
        for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            AppLog appLog = cursorToLog(cursor);
            logs.add(appLog);
        }

        cursor.close();

        return logs;
    }

    private AppLog cursorToLog(Cursor cursor){
        AppLog newLog = new AppLog(cursor.getString(0),cursor.getString(1),cursor.getString(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5)); //cursor.getString(5));
        return newLog;
    }


    public AppLog createLog(String event, String ID){
        ContentValues values = new ContentValues();

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles")); // explicitly set timezone

        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String time = hour + ":" + minutes + ":" + seconds;

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        String date = month + "-" + day + "-" + year;

        values.put(COLUMN_EVENT, event);
        values.put(COLUMN_ID, ID);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_PLATFORM, "Android");

        //values.put(COLUMN_LOCATION, Location.get);

        long insertEvent = sqlDB.insert(NOTE_TABLE,null,values);

        Log.e("TAG", "createLog: inserting new log in db");

        Cursor cursor = sqlDB.query(NOTE_TABLE,allColumns, COLUMN_EVENTNUM + " = " + insertEvent,null,null,null,null);

        cursor.moveToFirst();
        AppLog newLog = cursorToLog(cursor);
        cursor.close();

        return newLog;
    }

    public long deleteLog(){
        //return sqlDB.delete(NOTE_TABLE,COLUMN_ID +" = " + idToDelete,null);
        return sqlDB.delete(NOTE_TABLE,"1",null);
    }

    public void deleteAllLogs() {
        sqlDB.delete(NOTE_TABLE,null,null);
        return;
    }

    public void close(){
        logDbHelper.close();
    }

    private static class LogDbHelper extends SQLiteOpenHelper {

        LogDbHelper(Context ctx){
            super(ctx,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            //create note table
            //db.execSQL(DATABASE_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            //update the db
            db.execSQL("DROP TABLE IF EXISTS " +NOTE_TABLE);
            onCreate(db);
        }
    }
}