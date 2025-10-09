package com.example.diverscan.activeid.sqlite;

import   android.content.ContentValues ;
import   android.content.Context ;
import   android.database.Cursor ;
import   android.database.sqlite.SQLiteDatabase ;
import   android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends  SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Test_ActiveId_v1.db";
    public static final int    DATABASE_VERSION = 3;
    public static final String TABLE_USERS = "Users";
    public static final String KEY_ID = "ID";
    public static final String KEY_USER_NAME = "username" ;
    public static final String KEY_EMAIL = "email" ;
    public static final String KEY_PASSWORD = "pass" ;

    private SQLiteDatabase myDataBase;

    //private final Context myContext;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME ,null , DATABASE_VERSION);
        //this.myContext = context;
    }

    public void onCreate (SQLiteDatabase  db ){
        String sql = "CREATE TABLE Users (_id TEXT PRIMARY KEY, username Text, pass Text, email Text)";
        db.execSQL(sql);
    }

    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    public Cursor Login (String username){

        try {

        SQLiteDatabase db = this.getReadableDatabase();

        String query="Select * from Users where username ='"+username+"'";

        return db.rawQuery(query, new String[] { username });

        //Cursor result = db.rawQuery(query, null);

       /* if (result != null){

            return result;

        }else{

            return null;
        }*/

        }catch (Exception ex){

            ex.toString();

            return null;
        }
    }


}
