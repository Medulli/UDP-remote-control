package com.placeholder.julien.udp_remote_control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Created by Julien on 22/01/2015.
 */

public class VideoSequenceDBAdapter {

    public static final String DATABASE_NAME = "messagesequence.db";

    public static final int DATABASE_VERSION = 1;

    /**
     * SQL Statement to create a new database.
     * 4 tables:
     * videosequence with video names
     * command with available commands (ie start, stop, etc.).
     * video with available videos and effects (ie video1, fade, etc.).
     * videosequencecontent with an ordered set of videos + delay
     */
    /*
    public static final String DATABASE_CREATE_VID_SEQ = "CREATE TABLE IF NOT EXISTS "+"videosequence"+
            "( " +"_id"+" integer primary key autoincrement,"+ "name  text); ";
    public static final String DATABASE_CREATE_CMD = "CREATE TABLE IF NOT EXISTS "+"command"+
            "( " +"_id"+" integer primary key autoincrement,"+ "name  text); ";
    public static final String DATABASE_CREATE_VIDEO = "CREATE TABLE IF NOT EXISTS "+"video"+
            "( " +"_id"+" integer primary key autoincrement,"+ "name  text); ";
    public static final String DATABASE_CREATE_VID_SEQ_CONTENT = "CREATE TABLE IF NOT EXISTS "+"videosequencecontent"+
            "( " +"_id"+" integer primary key autoincrement,"+ "idVidSeq integer,delay integer,idCmd  integer,idVid integer,orderSeq integer); ";
    */

    // Variable to hold the database instance
    private SQLiteDatabase db;
    // Context of the application using the database.
    private final Context context;
    // Database helper (open,upgrade)
    private DataBaseHelper dbHelper;

    public  VideoSequenceDBAdapter(Context context){
        this.context = context;
        this.dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //open the database
    public  VideoSequenceDBAdapter open() throws SQLException{
        this.db = dbHelper.openDataBase();
        //this.db = dbHelper.getWritableDatabase();
        return this;
    }

    //close the database
    public void close(){
        //this.db.close();
        dbHelper.close();
    }

    //insert in videosequence, command or video
    public void insertBasicEntry(String name, String table){

        ContentValues newEntry = new ContentValues();
        //assign value
        newEntry.put("name", name);

        //insert the row into the table
        this.db.insert(table, null, newEntry);
        //Toast.makeText(context, "Data saved", Toast.LENGTH_LONG).show();
    }

    //insert a full video sequence in videosequencecontent
    public void fillVideoSequence(VideoSequence vidSeq){

        String name=vidSeq.getName();
        ArrayList<Integer> delayList=vidSeq.getDelay();
        ArrayList<String> cmdList=vidSeq.getCommand();
        ArrayList<String> videoList=vidSeq.getVideo();

        //get the id corresponding to the videosequence name
        Integer idVidSeq=getIdByName("videosequence",name);

        for(int i=0; i<delayList.size(); i++){
            //get the id corresponding to the command name
            Integer idCmd=getIdByName("command",cmdList.get(i));
            //get the id corresponding to the video name
            Integer idVid=getIdByName("video",videoList.get(i));
            //add in database
            addVideoSequence(idVidSeq,delayList.get(i),idCmd,idVid,i);
        }

        Toast.makeText(context, "video sequence added", Toast.LENGTH_LONG).show();
    }

    //insert one entry in videosequencecontent
    private void addVideoSequence(int idVidSeq, Integer delay, int idCmd, int idVid, int orderSeq){

        ContentValues newEntry = new ContentValues();
        //assign values
        newEntry.put("idVidSeq", idVidSeq);
        newEntry.put("delay", delay);
        newEntry.put("idCmd", idCmd);
        newEntry.put("idVid", idVid);
        newEntry.put("orderSeq", orderSeq);

        //insert the row into the table
        this.db.insert("videosequencecontent", null, newEntry);
        //Toast.makeText(context, "Video added to video sequence", Toast.LENGTH_LONG).show();
    }

    //remove all content in videosequencecontent for a specific video sequence
    public int emptyVideoSequence(String name){

        //get the id corresponding to the videosequence name
        Integer idVidSeq=getIdByName("videosequence",name);

        //delete corresponding content
        int numberEntriesDeleted= db.delete("videosequencecontent", "idVidSeq=?", new String[]{idVidSeq.toString()}) ;
        //Toast.makeText(context, "Number of entries deleted successfully : "+numberEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberEntriesDeleted;
    }

    //get video sequence content from name
    public VideoSequence getVideoSequence(String name){

        //query builder, used to have access to JOIN
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //sql SELECT
        String[] querySelect={
                "vsc.delay del", //delay
                "c.name cmd", //command
                "v.name vid", //video
        };

        //sql FROM
        queryBuilder.setTables("videosequencecontent vsc " +
                "INNER JOIN videosequence vs ON (vsc.idVidSeq=vs._id)" +
                "INNER JOIN command c ON (vsc.idCmd=c._id)" +
                "INNER JOIN video v ON (vsc.idVid=v._id)");

        //sql WHERE
        String queryWhere = "vs.name='"+name+"'";

        //sql ORDER BY
        String queryOrder="vsc.OrderSeq ASC";

        //query
        Cursor cursor = queryBuilder.query(this.db,querySelect,queryWhere,null, null, null, queryOrder);

        //result
        VideoSequence vidSeq=new VideoSequence(name);
        while (cursor.moveToNext()) {
            vidSeq.addVideoCommand(cursor.getInt(0),cursor.getString(1),cursor.getString(2));
        }
        return vidSeq;
    }

    //delete in table
    public int deleteEntry(String name, String table){

        int numberOFEntriesDeleted= db.delete(table, "name=?", new String[]{name}) ;
        Toast.makeText(context, "Number of entries deleted successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }

    //select in table
    public Integer getIdByName(String table, String name){

        Cursor cursor=this.db.query(table, null, " name=?", new String[]{name}, null, null, null);
        if(cursor.getCount()<1) // UserName Not Exist
            return -1;
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex("_id"));
    }

    //get possible commands, videos or video sequences' names
    public ArrayList<String> getPossibleEntries(String table){

        String[] querySelect={"name"};
        Cursor cursor=this.db.query(table, querySelect, null, null, null, null, null);

        //result
        ArrayList<String> result=new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }
        //Toast.makeText(context, "Entries found : "+result.toString(), Toast.LENGTH_LONG).show();
        return result;
    }

}