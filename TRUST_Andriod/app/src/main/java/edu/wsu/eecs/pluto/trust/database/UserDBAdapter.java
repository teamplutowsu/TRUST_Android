package edu.wsu.eecs.pluto.trust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Kameron on 1/22/2017.
 */

public class UserDBAdapter {
    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1; //If we change the database INCREMENT THIS!!!
    private UserDBHelper uDBAdapter;

    // Define table that holds the logs.
    private static final String NOTE_TABLE = "user";
    private static final String COLUMN_NUM = "colNum";
    private static final String ID_NUM = "id";
    private static final String LOGS_LAST_SENT_DATE = "logs_last_sent_date";

    private SQLiteDatabase sqlDB;
    private Context context;

    //private static final String COLUMN_LOCATION = "location";

    private String[] allColumns = {ID_NUM, LOGS_LAST_SENT_DATE};

    public static final String DATABASE_CREATE_TABLE = "create table " +
            NOTE_TABLE + " ( " + COLUMN_NUM + " integer primary key autoincrement, " + ID_NUM + " text not null, " + LOGS_LAST_SENT_DATE + " text not null);";

    public UserDBAdapter(Context ctx){
        context = ctx;
    }

    public UserDBAdapter open() throws android.database.SQLException{
        uDBAdapter = new UserDBHelper(context);
        sqlDB = uDBAdapter.getWritableDatabase();
        return this;
    }

    public String getID(){
        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, null, null, null, null, null);

        cursor.moveToLast();
        if (cursor.getPosition() == -1) {
            cursor.close();
            return "";
        }
        User user = cursorToUser(cursor);
        cursor.close();

        return user.get_id();
    }

    public void setSentLogsDate(String updateDate){

        ContentValues cv = new ContentValues();
        cv.put(LOGS_LAST_SENT_DATE, updateDate);

        sqlDB.update(NOTE_TABLE, cv, COLUMN_NUM + "= 1", null);
    }

    private User cursorToUser(Cursor cursor){
        //String s0 = cursor.getString(1);

        User user = new User(cursor.getString(0), cursor.getString(1));
        return user;
    }

    public boolean isUserinDB () {
        // Check if a user is already in the in-app db
        Cursor ucursor = sqlDB.rawQuery("SELECT count(*) FROM " + NOTE_TABLE, null);
        ucursor.moveToFirst();
        int count = ucursor.getInt(0);
        ucursor.close();
        if (count == 0 ) { return false; }
        return true;
    }

    public User insertUser(String id){

        ContentValues values = new ContentValues();

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles")); // explicitly set timezone
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        String date = month + "-" + day + "-" + year;

        values.put(ID_NUM, id);
        values.put(LOGS_LAST_SENT_DATE, date);

        long insertEvent = sqlDB.insert(NOTE_TABLE, null, values);

        //Log.e("TAG", "createLog: inserting new LastPage in db");

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, COLUMN_NUM + " = 1", null, null, null, null);
        cursor.moveToFirst();
        User newUser = cursorToUser(cursor);
        cursor.close();

        return newUser;
    }

    public long deleteID(){
        return sqlDB.delete(NOTE_TABLE, COLUMN_NUM +" = " + "1" ,null);
        //return sqlDB.delete(NOTE_TABLE,"1",null);
        //return 1;
    }

    public void close(){
        uDBAdapter.close();
    }

    private static class UserDBHelper extends SQLiteOpenHelper {

        UserDBHelper(Context ctx){
            super(ctx,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            //create note table
            db.execSQL(DATABASE_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            //update the db
            db.execSQL("DROP TABLE IF EXISTS " +NOTE_TABLE);
            onCreate(db);
        }
    }
}
