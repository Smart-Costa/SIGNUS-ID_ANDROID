package com.example.diverscan.activeid.RazonSocial;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class RazonSocialDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;


    public RazonSocialDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public Map<Integer, RazonSocialRecord> ObtenerSociedades (){
            Map<Integer, RazonSocialRecord> mapRazonSociales = new HashMap<Integer, RazonSocialRecord>();
            SQLiteDatabase db = this.getReadableDatabase();
            String query="Select * from RazonSocial";
            Cursor sociedadesCursor = db.rawQuery(query, null);
            int cantidadDatos = sociedadesCursor.getCount();
            sociedadesCursor.moveToFirst();
            for (int i = 0; i < cantidadDatos;i++) {
                String idRazon = sociedadesCursor.getString(sociedadesCursor.getColumnIndex("_id"));
                String nombreRazon = sociedadesCursor.getString(sociedadesCursor.getColumnIndex("Nombre"));
                RazonSocialRecord razonSocialRecord = new RazonSocialRecord(idRazon,nombreRazon);
                mapRazonSociales.put(i,razonSocialRecord);
                sociedadesCursor.moveToNext();
            }
            return mapRazonSociales;
    }

    public Map<Integer, RazonNuevo> ObtenerRazon (){

        Map<Integer, RazonNuevo> mapRazon = new HashMap<Integer, RazonNuevo>();

        String query="Select * from RazonSocial";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor SociedadCursor = db.rawQuery(query, null);
        int cantidadDatos = SociedadCursor.getCount();
        SociedadCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
             String idRazon = SociedadCursor.getString(SociedadCursor.getColumnIndex("_id"));
             String nombreRazon =  SociedadCursor.getString(SociedadCursor.getColumnIndex("Nombre"));


            RazonNuevo razonNuevo = new RazonNuevo(idRazon,nombreRazon);
            mapRazon.put(i, razonNuevo);
            SociedadCursor.moveToNext();

        }
        return mapRazon;

    }


}
