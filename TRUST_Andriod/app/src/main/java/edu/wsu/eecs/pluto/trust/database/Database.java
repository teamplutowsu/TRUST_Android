package edu.wsu.eecs.pluto.trust.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gene on 1/31/17.
 */

public class Database {

    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1; //If we change the database INCREMENT THIS!!!
    private DbHelper DbHelper;

    // Define table that holds the logs.
    private static final String LOG_TABLE = "log";
    private static final String LOG_COLUMN_EVENTNUM = "event_num";
    private static final String LOG_COLUMN_EVENT = "event";
    private static final String LOG_COLUMN_ID = "id";
    private static final String LOG_COLUMN_DATE = "date";
    private static final String LOG_COLUMN_TIME = "time";
    private static final String LOG_COLUMN_PLATFORM = "platform";


    // Define table that holds the bookmarks.
    private static final String BM_TABLE = "bookmark";
    private static final String BM_MODULE_NUM = "module_num";
    private static final String BM_PAGE_NUM = "page_num";

    // Define table that holds the user info.
    private static final String USER_TABLE = "user";
    private static final String USER_COLUMN_NUM = "colNum";
    private static final String USER_ID_NUM = "id";
    private static final String USER_LOGS_LAST_SENT_DATE = "logs_last_sent_date";

    // Define table that holds info about module
    private static final String MODULE_TABLE = "module";
    private static final String MODULE_NUM = "module_num";
    private static final String MODULE_TITLE = "title";
    private static final String MODULE_NPAGES = "npages";
    private static final String MODULE_IMGFILE = "img_file";
    private static final String MODULE_LPA = "lastpageaccessed";

    //private static final String COLUMN_LOCATION = "location";

    public static final String LOG_CREATE_TABLE = "create table " +
            LOG_TABLE + " ( " + LOG_COLUMN_EVENTNUM + " integer primary key autoincrement, " +
            LOG_COLUMN_EVENT+ " text not null, "+ LOG_COLUMN_ID+ " text not null," + LOG_COLUMN_DATE+ " text not null, " + LOG_COLUMN_TIME + " text not null, " +
            LOG_COLUMN_PLATFORM + " text not null);";

    public static final String BM_CREATE_TABLE = String.format("CREATE TABLE %s ( %s text not null, %s text not null, PRIMARY KEY (%s, %s));",
            BM_TABLE, BM_MODULE_NUM, BM_PAGE_NUM, BM_MODULE_NUM, BM_PAGE_NUM);

    public static final String USER_CREATE_TABLE = "create table " +
            USER_TABLE + " ( " + USER_COLUMN_NUM + " integer primary key autoincrement, " + USER_ID_NUM + " text not null, " + USER_LOGS_LAST_SENT_DATE + " text not null);";

    public static final String MODULE_CREATE_TABLE = String.format("CREATE TABLE %s (%s text primary key, %s text not null, %s integer, %s text not null, %s text not null);",
            MODULE_TABLE, MODULE_NUM, MODULE_TITLE, MODULE_NPAGES, MODULE_IMGFILE, MODULE_LPA);

    private SQLiteDatabase sqlDB;
    private Context context;

    public Database(Context ctx){
        context = ctx;
    }

    public Database open() throws android.database.SQLException{
        DbHelper = new Database.DbHelper(context);
        sqlDB = DbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        DbHelper.close();
    }

    private static class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context ctx){
            super(ctx,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            //create tables
            db.execSQL(LOG_CREATE_TABLE);
            db.execSQL(BM_CREATE_TABLE);
            db.execSQL(USER_CREATE_TABLE);
            db.execSQL(MODULE_CREATE_TABLE);

            // Insert modules
            db.execSQL(String.format("INSERT INTO %s VALUES (9, 'Advanced Care Planning', 52, 'img_09_', 1)", MODULE_TABLE));
            db.execSQL(String.format("INSERT INTO %s VALUES (1, 'Taking Control of Heart Failure', 16, 'img_01_', 1)", MODULE_TABLE));
            db.execSQL(String.format("INSERT INTO %s VALUES (4, 'Self-Care', 42, 'img_04_', 1)", MODULE_TABLE));
            db.execSQL(String.format("INSERT INTO %s VALUES (6, 'Managing Feelings About Heart Failure', 17, 'img_06_', 1)", MODULE_TABLE));

            // TODO: Insert other modules
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            //update the db
            db.execSQL("DROP TABLE IF EXISTS " +LOG_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " +BM_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " +USER_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " +MODULE_TABLE);
            onCreate(db);
        }
    }

}
