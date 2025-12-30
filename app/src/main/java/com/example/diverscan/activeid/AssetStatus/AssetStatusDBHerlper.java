package com.example.diverscan.activeid.AssetStatus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class AssetStatusDBHerlper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 8;

    public AssetStatusDBHerlper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    }

    public Map<Integer, EntidadAssetStatus> GetAssetStatus ()
    {
        Map<Integer, EntidadAssetStatus> mapAssetStatus = new HashMap<Integer, EntidadAssetStatus>();
        String query="Select * from AssetStatus order by Name desc";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor assetStatusCursor = db.rawQuery(query, null);
        int cantidadDatos = assetStatusCursor.getCount();
        assetStatusCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++)
        {
            String id= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("_id"));
            String name= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("Name"));
            String description= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("Description"));

            EntidadAssetStatus entidadAssetStatus = new EntidadAssetStatus(id,name, description);
            mapAssetStatus.put(i, entidadAssetStatus);
            assetStatusCursor.moveToNext();
        }
        return mapAssetStatus;

    }

    public EntidadAssetStatus GetAssetStatusbyId (String StatusId){
        String query="Select * from AssetStatus Where _id = '"+StatusId+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor assetStatusCursor = db.rawQuery(query, null);
        int cantidadDatos = assetStatusCursor.getCount();
        assetStatusCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String id= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("_id"));
            String name= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("Name"));
            String description= assetStatusCursor.getString(assetStatusCursor.getColumnIndex("Description"));

            EntidadAssetStatus entidadAssetStatus = new EntidadAssetStatus(id,name, description);
           return entidadAssetStatus;
        }
        return null;

    }
}
