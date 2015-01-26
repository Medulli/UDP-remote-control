package com.placeholder.julien.udp_remote_control;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


/**
 * Created by Julien on 23/01/2015.
 */
public class DataBaseHelper extends SQLiteAssetHelper {

    private SQLiteDatabase db;

    public DataBaseHelper(Context context, String name,CursorFactory cFactory, int version){
        super(context, name, cFactory, version);
    }

    //called when there is a database version mismatch meaning that the version
    //of the database on disk needs to be upgraded to the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Log the version upgrade.
        Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        //upgrade the existing database to conform to the new version
        //drop the old tables
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (cursor.moveToNext()) {
            db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0));
        }
        //create a new database
        onCreate(db);
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        //open database
        db = getWritableDatabase();
        return db;
    }

    @Override
    public synchronized void close() {
        //close database
        if(db != null)
            db.close();
        super.close();
    }

}

