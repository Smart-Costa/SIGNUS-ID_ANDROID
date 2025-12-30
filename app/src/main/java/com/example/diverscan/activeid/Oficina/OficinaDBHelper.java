package com.example.diverscan.activeid.Oficina;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class OficinaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 8;

    public OficinaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public Map<Integer, OficinaRecord> ObtenerOficinasxPiso (String idPiso){
        Map<Integer, OficinaRecord> mapOficinas = new HashMap<Integer, OficinaRecord>();
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        String query="Select * from Oficina where idPiso = '"+idPiso+"'";
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            OficinaRecord oficinaRecord = new OficinaRecord(idOficina,nombreOficina, IdOficina);
            mapOficinas.put(i,oficinaRecord);
            oficinasCursor.moveToNext();
        }
        return mapOficinas;
    }

    public Map<Integer, oficinaNuevo> ObtenerOficina (String idPiso){
        Map<Integer, oficinaNuevo> mapOficinas = new HashMap<Integer, oficinaNuevo>();
        String query="Select * from Oficina where idPiso = '"+idPiso+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            oficinaNuevo oficinanuevo = new oficinaNuevo(idOficina,nombreOficina, IdOficina);
            mapOficinas.put(i, oficinanuevo);
            oficinasCursor.moveToNext();

        }
        return mapOficinas;

    }


    public Map<Integer, oficinaNuevo> ObtenerOficinaPorPisoDescripcion (String idPiso, String descripcion){
        Map<Integer, oficinaNuevo> mapOficinas = new HashMap<Integer, oficinaNuevo>();
        String query="Select * from Oficina where idPiso = '"+idPiso+"' AND Piso like "+"'%"+descripcion+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            oficinaNuevo oficinanuevo = new oficinaNuevo(idOficina,nombreOficina, IdOficina);
            mapOficinas.put(i, oficinanuevo);
            oficinasCursor.moveToNext();

        }
        return mapOficinas;
    }


    public Map<Integer, OficinaRecord> ObtenerOficinaPorPisoDescripcion2 (String idPiso, String descripcion){
        Map<Integer, OficinaRecord> mapOficinas = new HashMap<Integer, OficinaRecord>();
        String query="Select * from Oficina where idPiso = '"+idPiso+"' and Nombre like "+"'%"+descripcion+"%' or  Piso like "+"'%"+descripcion+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            OficinaRecord oficinanuevo = new OficinaRecord(idOficina,nombreOficina, IdOficina);
            mapOficinas.put(i, oficinanuevo);
            oficinasCursor.moveToNext();

        }
        return mapOficinas;

    }

    public Map<Integer, oficinaNuevo> ObtenerOficinaPorPisoDescripcion3 (String idPiso, String descripcion){
        Map<Integer, oficinaNuevo> mapOficinas = new HashMap<Integer, oficinaNuevo>();
        String query="Select * from Oficina where idPiso = '"+idPiso+"' and Nombre like "+"'%"+descripcion+"%' or  Piso like "+"'%"+descripcion+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            oficinaNuevo oficinanuevo = new oficinaNuevo(idOficina,nombreOficina, IdOficina);
            mapOficinas.put(i, oficinanuevo);
            oficinasCursor.moveToNext();

        }
        return mapOficinas;

    }
}
