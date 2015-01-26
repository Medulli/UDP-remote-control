package com.placeholder.julien.udp_remote_control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

/**
 * Created by Julien on 23/01/2015.
 */
public class AdminDBAdapter {

    public static final String DATABASE_NAME = "login.db";

    public static final int DATABASE_VERSION = 1;

    // SQL Statement to create a new database. Contains only the allowed email addresses (admin access)
    /*
    static final String DATABASE_CREATE_LOGIN = "CREATE TABLE IF NOT EXISTS "+"login"+
            "( " +"_id"+" integer primary key autoincrement,"+ "email  text); ";
    */

    // Variable to hold the database instance
    private SQLiteDatabase db;
    // Context of the application using the database.
    private final Context context;
    // Database helper (open,upgrade)
    private DataBaseHelper dbHelper;

    public  AdminDBAdapter(Context context){
        this.context = context;
        this.dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //open the database
    public  AdminDBAdapter open() throws SQLException{
        this.db = dbHelper.openDataBase();
        //db = dbHelper.getWritableDatabase();
        return this;
    }

    //close the Database
    public void close(){
        //db.close();
        dbHelper.close();
    }

    /*
    * Not used yet, might be useful later on.
     */
    //insert admin
    public void insertAdmin(String email){

        ContentValues newEntry = new ContentValues();
        //assign value
        newEntry.put("email", email);

        // Insert the row into the table
        db.insert("login", null, newEntry);
        Toast.makeText(context, "New admin created.", Toast.LENGTH_LONG).show();

    }

    //delete an admin
    public int deleteAdmin(String email){

        int numberOFEntriesDeleted= db.delete("login", "email=?", new String[]{email}) ;
        Toast.makeText(context, "Number fo Entry Deleted Successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }

    //check if an email is in the db
    public boolean isAdmin(String email){
        //select entry with email to check
        Cursor cursor=db.query("login", null, " email=?", new String[]{email}, null, null, null);
        //nothing found
        if(cursor.getCount()<1){
            return false;
        }
        //email in db
        return true;
    }

}
