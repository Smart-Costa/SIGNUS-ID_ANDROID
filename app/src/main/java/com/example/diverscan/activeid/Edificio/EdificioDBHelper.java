package com.example.diverscan.activeid.Edificio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.diverscan.activeid.RazonSocial.RazonNuevo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EdificioDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;

    public EdificioDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public Map<Integer, EdificioRecord> ObtenerEdificiosxCompania (String idrazonSocial){
        Map<Integer, EdificioRecord> mapEdificios = new HashMap<Integer, EdificioRecord>();
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        String query="Select * from Edificios where idRazonSocial = '"+idrazonSocial+"'";
        Cursor edificiosCursor = db.rawQuery(query, null);
        int cantidadDatos = edificiosCursor.getCount();
        edificiosCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idEdificio = edificiosCursor.getString(edificiosCursor.getColumnIndex("_id"));
            String nombreEdificio = edificiosCursor.getString(edificiosCursor.getColumnIndex("Nombre"));
            String IdRazonSocial = edificiosCursor.getString(edificiosCursor.getColumnIndex("idRazonSocial"));
            EdificioRecord edificioRecord = new EdificioRecord(idEdificio,nombreEdificio, IdRazonSocial);
            mapEdificios.put(i,edificioRecord);
            edificiosCursor.moveToNext();
        }
        return mapEdificios;
    }


    public Map<Integer, EdificioNuevo> ObtenerEdificio (String IdCompania){

     try {
         Map<Integer, EdificioNuevo> mapEdificios = new HashMap<Integer, EdificioNuevo>();
         String query = "Select * from Edificios WHERE idRazonSocial ='"+IdCompania+"'";
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor edificiosCursor = db.rawQuery(query, null);
         int cantidadDatos = edificiosCursor.getCount();
         edificiosCursor.moveToFirst();
         for (int i = 0; i < cantidadDatos; i++) {
             String idEdificio = edificiosCursor.getString(edificiosCursor.getColumnIndex("_id"));
             String nombreEdificio = edificiosCursor.getString(edificiosCursor.getColumnIndex("Nombre"));
             String IdRazonSocial = edificiosCursor.getString(edificiosCursor.getColumnIndex("idRazonSocial"));

             EdificioNuevo razonNuevo = new EdificioNuevo(idEdificio, nombreEdificio, IdRazonSocial);
             mapEdificios.put(i, razonNuevo);
             edificiosCursor.moveToNext();


         }
         return mapEdificios;
     } catch (Exception e){
         e.toString();
         return null;
     }

    }
}
