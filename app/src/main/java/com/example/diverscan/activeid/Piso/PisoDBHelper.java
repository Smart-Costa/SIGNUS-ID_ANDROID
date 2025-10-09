package com.example.diverscan.activeid.Piso;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.diverscan.activeid.Edificio.EdificioNuevo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PisoDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;

    public PisoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public Map<Integer, PisoRecord> ObtenerPisosxEdificio (String idEdificio){
        Map<Integer, PisoRecord> mapPisos = new HashMap<Integer, PisoRecord>();
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        String query="Select * from Pisos where idEdificio = '"+idEdificio+"'";
        Cursor pisosCursor = db.rawQuery(query, null);
        int cantidadDatos = pisosCursor.getCount();
        pisosCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idPiso = pisosCursor.getString(pisosCursor.getColumnIndex("_id"));
            String nombrePiso = pisosCursor.getString(pisosCursor.getColumnIndex("Nombre"));
            String IdEdificio = pisosCursor.getString(pisosCursor.getColumnIndex("idEdificio"));
            PisoRecord pisoRecord = new PisoRecord(idPiso,nombrePiso, IdEdificio);
            mapPisos.put(i,pisoRecord);
            pisosCursor.moveToNext();
        }
        return mapPisos;
    }

    public Map<Integer, PisoNuevo> ObtenerPiso (String idEdificio){
        Map<Integer, PisoNuevo> mapPisos = new HashMap<Integer, PisoNuevo>();
        String query="Select * from Pisos Where idEdificio = '"+idEdificio+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor pisosCursor = db.rawQuery(query, null);
        int cantidadDatos = pisosCursor.getCount();
        pisosCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idPiso = pisosCursor.getString(pisosCursor.getColumnIndex("_id"));
            String nombrePiso = pisosCursor.getString(pisosCursor.getColumnIndex("Nombre"));
            String IdEdificio = pisosCursor.getString(pisosCursor.getColumnIndex("idEdificio"));
            PisoNuevo pisoNuevo = new PisoNuevo(idPiso,nombrePiso, IdEdificio);
            mapPisos.put(i, pisoNuevo);
            pisosCursor.moveToNext();

        }
        return mapPisos;

    }
}
