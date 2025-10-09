package com.example.diverscan.activeid.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MyDatabase";
    private static final int DATABASE_VERSION = 3;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlusers = "CREATE TABLE users (email TEXT PRIMARY KEY, gender Text, hobbies Text, zodiac Text)";
        db.execSQL(sqlusers);

        String sqlUsuarios = "CREATE TABLE Usuarios (_id text PRIMARY KEY, username Text, pass Text, email Text)";
        db.execSQL(sqlUsuarios);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
         db.execSQL("DROP TABLE IF EXISTS users");
         onCreate(db);

         db.execSQL("DROP TABLE IF EXISTS Usuarios");
         onCreate(db);
    }

    public boolean guardarusuario (String _id , String username, String pass, String email) {
        long result;

        try {

        Cursor resultado = getUsername(_id);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("pass", pass);
        contentValues.put("email", email);

        if (resultado.getCount() == 0) { // Record does not exist

            contentValues.put("_id", _id);
            result = db.insert("Usuarios", null, contentValues);

        } else { // Record exists
            result = db.update("Usuarios", contentValues, "_id=?", new String[] { _id });
         //   result = db.update()
            result = db.update("Usuarios", contentValues, "_id=?", new String[] { _id });
        }

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }catch (Exception ex){

        ex.toString();

    }
        return true;
    }

    public boolean saveUser (String email, String gender, String hobbies, String zodiac)
        {
            Cursor cursor = getUser(email);
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("gender", gender);
            contentValues.put("hobbies", hobbies);
            contentValues.put("zodiac", zodiac);
            long result;


                if (cursor.getCount() == 0) { // Record does not exist
                    contentValues.put("email", email);
                result = db.insert("users", null, contentValues);

            } else { // Record exists

                result = db.update("users", contentValues, "email=?", new String[] { email });
                result = db.update("users", contentValues, "email=?", new String[] { email });
            }

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }

        public Cursor getUser(String email){

        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM users WHERE email=?";
        return db.rawQuery(sql, new String[] { email });
    }

    public Cursor getUsername (String _id){

        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM Usuarios WHERE _id='"+_id+"'";
        return db.rawQuery(sql, new String[] { _id });
    }

    public Cursor Login (String username){

        try {

            SQLiteDatabase db = this.getReadableDatabase();
            //String query="Select * from Users where username ='"+username+"'";
            String query="Select * from Users where username ='"+username+"'";
            //return db.rawQuery(query, null);
            return db.rawQuery(query, new String[] { username });
            //Cursor result = db.rawQuery(query, null);

        }catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public void deleteUser(String email){

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("users", "email=?", new String[] { email });
    }
}
