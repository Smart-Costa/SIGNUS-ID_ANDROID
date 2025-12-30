package com.example.diverscan.activeid.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.diverscan.activeid.Roles.EntidadDatosRol;

import java.util.ArrayList;

public class HHRolHelper  extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 8;


    public HHRolHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<String>  GetRolHH (String param){
        try{
            ArrayList<String> RolesHH = new ArrayList<String>();
            String query="Select a.description from RolHH  a where a.Esta_Bloqueado = 'False' AND a.UserSysId = '"+ param + "'";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor=  db.rawQuery(query, null);
            int cantidadRoles = cursor.getCount();
            cursor.moveToFirst();
            for(int i = 0; i < cantidadRoles; i++) {
                RolesHH.add(cursor.getString(cursor.getColumnIndex("Description")));
                cursor.moveToNext();
            }
            cursor.close();
            return  RolesHH;
        }catch (Exception e){
            return null;
        }
    }

    public ArrayList<String> cargarRolHH (String query){
        ArrayList<String> RolesHH = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor=  db.rawQuery(query, null);
        cursor.moveToFirst();
        int cantidadRoles = cursor.getCount();
        for(int i = 0; i < cantidadRoles; i++) {
            RolesHH.add(cursor.getString(cursor.getColumnIndex("Description")));
            cursor.moveToNext();
        }
        cursor.close();
        return  RolesHH;
        }

   /* private EntidadDatosRol getRolHH(Cursor cursor){
        try{
            String description= cursor.getString(cursor.getColumnIndex("Description"));
            String page = cursor.getString(cursor.getColumnIndex("Page"));
            String username = cursor.getString(cursor.getColumnIndex("Username"));
            String userSysId = cursor.getString(cursor.getColumnIndex("UserSysId"));
            String estaBloqueado = cursor.getString(cursor.getColumnIndex("Esta_Bloqueado"));
            EntidadDatosRol entidadtipoTags= new EntidadDatosRol(description,page,username, userSysId,estaBloqueado);
            return  entidadtipoTags;
        }catch (Exception ex){
            return  null;
        }
    }*/
}
