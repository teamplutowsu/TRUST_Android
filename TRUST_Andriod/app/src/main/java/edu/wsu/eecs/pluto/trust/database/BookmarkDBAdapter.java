package edu.wsu.eecs.pluto.trust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Kameron on 1/22/2017.
 */

public class BookmarkDBAdapter {
    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1; //If we change the database INCREMENT THIS!!!
    private BookmarkDBHelper bmDbHelper;

    // Define table that holds the logs.
    private static final String NOTE_TABLE = "bookmark";
    private static final String MODULE_NUM = "module_num";
    private static final String PAGE_NUM = "page_num";

    private SQLiteDatabase sqlDB;
    private Context context;

    //private static final String COLUMN_LOCATION = "location";

    private String[] allColumns = {MODULE_NUM, PAGE_NUM};//COLUMN_LOCATION};

    public static final String DATABASE_CREATE_TABLE = String.format("CREATE TABLE %s ( %s text not null, %s text not null, PRIMARY KEY (%s, %s));",
            NOTE_TABLE, MODULE_NUM, PAGE_NUM, MODULE_NUM, PAGE_NUM);

    public BookmarkDBAdapter(Context ctx){
        context = ctx;
    }

    public BookmarkDBAdapter open() throws android.database.SQLException{
        bmDbHelper = new BookmarkDBHelper(context);
        sqlDB = bmDbHelper.getWritableDatabase();
        return this;
    }

    public ArrayList<Bookmark> getAllBookmarks(){
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, null, null, null, null, null);

        //Go thought the DB and write most recent log first.
        for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            Bookmark bookmark = cursorToBookmark(cursor);
            bookmarks.add(bookmark);
        }

        cursor.close();

        return bookmarks;
    }

    private Bookmark cursorToBookmark(Cursor cursor){
        String s0 = cursor.getString(0);
        String s1 = cursor.getString(1);

        Bookmark newBookmark = new Bookmark(cursor.getString(0),cursor.getString(1)); //cursor.getString(5));
        return newBookmark;
    }


    public void createBookmark(String modNum, String pageNum){
        ContentValues values = new ContentValues();

        values.put(MODULE_NUM, modNum);
        values.put(PAGE_NUM, pageNum);

        long insertEvent = sqlDB.insert(NOTE_TABLE,null,values);
        Log.e("TAG", "createLog: inserting new bookmark in db");

        Cursor cursor = sqlDB.query(NOTE_TABLE,allColumns, MODULE_NUM + " = " + insertEvent,null,null,null,null);
        //cursor.moveToFirst();
        //Bookmark newBookmark = cursorToLog(cursor);
        //cursor.close();

        //return newBookmark;
    }

    public long removeBookmark(String module, String page){
        //return sqlDB.delete(NOTE_TABLE,COLUMN_ID +" = " + idToDelete,null);
        //return sqlDB.delete(NOTE_TABLE,"1",null);
        return sqlDB.delete(NOTE_TABLE, MODULE_NUM + " = " + module + " AND " + PAGE_NUM + " = " + page, null);
    }

    public void close(){
        bmDbHelper.close(); 
    }

    private static class BookmarkDBHelper extends SQLiteOpenHelper {

        BookmarkDBHelper(Context ctx){
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
