package edu.wsu.eecs.pluto.trust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.Image;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Kameron on 2/6/2017.
 */

public class ModuleDBAdapter {
    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1; //If we change the database INCREMENT THIS!!!
    private ModuleDBAdapter.ModuleDBHelper mDbHelper;

    // Define table that holds the logs.
    private static final String NOTE_TABLE = "module";
    private static final String MODULE_NUM = "module_num";
    private static final String TITLE = "title";
    private static final String NUM_PAGES = "npages";
    private static final String IMG_FILE = "img_file";
    private static final String LAST_PAGE = "lastpageaccessed";


    private SQLiteDatabase sqlDB;
    private Context context;

    //private static final String COLUMN_LOCATION = "location";

    private String[] allColumns = {MODULE_NUM, TITLE, NUM_PAGES, IMG_FILE, LAST_PAGE};//COLUMN_LOCATION};

    public static final String DATABASE_CREATE_TABLE = String.format("CREATE TABLE %s (%s text primary key, %s text not null, %s integer, %s text not null, %s text not null);",
            NOTE_TABLE, MODULE_NUM, TITLE, NUM_PAGES, IMG_FILE, LAST_PAGE);

    public ModuleDBAdapter(Context ctx){
        context = ctx;
    }

    public ModuleDBAdapter open() throws android.database.SQLException{
        mDbHelper = new ModuleDBAdapter.ModuleDBHelper(context);
        sqlDB = mDbHelper.getWritableDatabase();
        return this;
    }

    public ArrayList<Module> getAllModules(){
        ArrayList<Module> modules = new ArrayList<>();

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, null, null, null, null, null);

        //Go thought the DB and write most recent log first.
        for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            Module module = cursorToModule(cursor);
            modules.add(module);
        }

        cursor.close();

        return modules;
    }

    public Module getModule(String mnumber){
        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, MODULE_NUM + " = ?", new String[] {mnumber}, null, null, null);

        cursor.moveToLast();

        Module module = cursorToModule(cursor);
        cursor.close();

        return module;
    }

    public String getLastPageAccessed (String modNum){

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, MODULE_NUM + " = ?", new String[] {modNum}, null, null, null);
        //Cursor cursor = sqlDB.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s';", NOTE_TABLE, MODULE_NUM, modNum), null);

        cursor.moveToFirst();
        Module module = cursorToModule(cursor);
        cursor.close();

        return module.getLastPage();
    }

    public String getNPages (String modNum){

        Cursor cursor = sqlDB.query(NOTE_TABLE, allColumns, MODULE_NUM + " = ?", new String[] {modNum}, null, null, null);
        //Cursor cursor = sqlDB.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s';", NOTE_TABLE, MODULE_NUM, modNum), null);

        cursor.moveToFirst();
        Module module = cursorToModule(cursor);
        cursor.close();

        return module.getNpages();
    }

    private Module cursorToModule(Cursor cursor){
        Module newModule = new Module(cursor.getString(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)); //cursor.getString(5));
        return newModule;
    }

    public void insertModule(String modNum, String title, String nPages, String imgFile, String lastPage){
        ContentValues values = new ContentValues();

        values.put(MODULE_NUM, modNum);
        values.put(TITLE, title);
        values.put(NUM_PAGES, nPages);
        values.put(IMG_FILE, imgFile);
        values.put(LAST_PAGE, lastPage);

        long insertEvent = sqlDB.insert(NOTE_TABLE,null,values);
        Cursor cursor = sqlDB.query(NOTE_TABLE,allColumns, MODULE_NUM + " = " + insertEvent,null,null,null,null);
        cursor.moveToFirst();
        //Module newModule = cursorToLog(cursor);
        cursor.close();

        //return newModule;
    }

    public long updateLastPage(String module, String page){

        ContentValues cv = new ContentValues();
        cv.put(MODULE_NUM, module);
        cv.put(LAST_PAGE, page);

        sqlDB.update(NOTE_TABLE, cv, "MODULE_NUM = ?", new String[] {module}); //Leaving where clause null should update the whole table. Only one row should be in the table.

        return 1;
    }

    public long removeModule(String module){
        //return sqlDB.delete(NOTE_TABLE,COLUMN_ID +" = " + idToDelete,null);
        //return sqlDB.delete(NOTE_TABLE,"1",null);
        return sqlDB.delete(NOTE_TABLE, MODULE_NUM + " = " + module, null);
    }

    public void close(){
        mDbHelper.close();
    }

    private static class ModuleDBHelper extends SQLiteOpenHelper {

        ModuleDBHelper(Context ctx){
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
