package com.example.diverscan.activeid.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.FotoActivo.EFotoActivo;

import java.util.ArrayList;

public class FotoDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 9;

    public FotoDBHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean InsertarFotoDB(String fotoID, String rutaFoto, String NombreFoto, String fotoConsecutivo, String AssetSysId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try{
            contentValues.put("_idFoto", fotoID);
            contentValues.put("assetSysId", AssetSysId);
            contentValues.put("rutaImagen", rutaFoto);
            contentValues.put("nombreFoto", NombreFoto);
            contentValues.put("consecutivoFoto", fotoConsecutivo);
            db.insert("FotoActivo", null, contentValues);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            Log.e("error", ex.toString());
            return false;
        }
    }

    public boolean TieneFoto(String assetSysId) {
        String query = "SELECT * FROM FotoActivo WHERE assetSysId = '"+assetSysId+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            return true;
        }else
            return false;
    }

    public int CantidadFotos(String AssetSysId){
        int cantidad=0;

        String query ="SELECT COUNT(*) FROM FotoActivo WHERE assetSysId = '"+AssetSysId+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            cantidad = cursor.getCount();
        }
        return cantidad;
    }

    public ArrayList<EFotoActivo> ObtenerFotoActivo(String AssetSysID){
        ArrayList<EFotoActivo> eFotoActivos = new ArrayList<EFotoActivo>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT _idFoto, assetSysId, rutaImagen, nombreFoto, consecutivoFoto FROM FotoActivo WHERE assetSysId = '"+AssetSysID+"'";
        Cursor cursor = db.rawQuery(query, null);
        int cantidadFotos = cursor.getCount();
        if(cantidadFotos != 0){
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String _idFoto = cursor.getString(cursor.getColumnIndex("_idFoto"));
                String assetSysId = cursor.getString(cursor.getColumnIndex("assetSysId"));
                String rutaImagen = cursor.getString(cursor.getColumnIndex("rutaImagen"));
                String nombreFoto = cursor.getString(cursor.getColumnIndex("nombreFoto"));
                String consecutivoFoto = cursor.getString(cursor.getColumnIndex("consecutivoFoto"));

                EFotoActivo eFotoActivo = new EFotoActivo(rutaImagen, consecutivoFoto, assetSysId,
                        nombreFoto, _idFoto);
                eFotoActivos.add(eFotoActivo);
            }
        }
        return  eFotoActivos;
    }

    public boolean EliminarFoto(String idFoto){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            String query = "Delete FROM FotoActivo where _idFoto ='"+idFoto+"'";
            db.execSQL(query);
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }finally {
            db.endTransaction();
            return true;
        }
    }
    public ArrayList<EFotoActivo> EnviarFotoActivo(){
        ArrayList<EFotoActivo> eFotoActivos = new ArrayList<EFotoActivo>();
        String query = "Select * From FotoActivo";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorFotos = db.rawQuery(query, null);
        int cantidadFotos = cursorFotos.getCount();
        cursorFotos.moveToFirst();

        for(int i = 0; i < cantidadFotos; i++){
            String fotoSysId = cursorFotos.getString(cursorFotos.getColumnIndex("_idFoto"));
            String assetSysId = cursorFotos.getString(cursorFotos.getColumnIndex("assetSysId"));
            String rutaImagen = cursorFotos.getString(cursorFotos.getColumnIndex("rutaImagen"));
            String nombreFoto = cursorFotos.getString(cursorFotos.getColumnIndex("nombreFoto"));
            String consecutivoFoto = cursorFotos.getString(cursorFotos.getColumnIndex("consecutivoFoto"));

            EFotoActivo fotoActivo = new EFotoActivo(rutaImagen, consecutivoFoto, assetSysId, nombreFoto, fotoSysId);
            eFotoActivos.add(fotoActivo);
            cursorFotos.moveToNext();
        }
        return eFotoActivos;
    }
}
