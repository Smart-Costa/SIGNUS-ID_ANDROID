package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UbicacionDao {
    private static final String TAG = "DB_DAO_UBICACION";
    private final AppDatabaseHelper dbHelper;
    private final Context context;

    public UbicacionDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public void syncUbicaciones(List<UbicacionEntity> lista) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete("UbicacionHH", null, null);
            Log.d(TAG, "Tabla UbicacionHH limpiada");

            for (UbicacionEntity u : lista) {
                ContentValues v = entityToContentValues(u);
                long result = db.insert("UbicacionHH", null, v);
                Log.d(TAG, "Insert UbicacionHH → resultado=" + result);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando ubicaciones", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private ContentValues entityToContentValues(UbicacionEntity u) {
        ContentValues v = new ContentValues();
        v.put("ASysId", u.getASysId());
        v.put("UbicacionA", u.getUbicacionA());
        v.put("BSysId", u.getBSysId());
        v.put("UbicacionB", u.getUbicacionB());
        v.put("CSysId", u.getCSysId());
        v.put("UbicacionC", u.getUbicacionC());
        v.put("DSysId", u.getDSysId());
        v.put("UbicacionD", u.getUbicacionD());
        return v;
    }

    public List<UbicacionEntity> getAllUbicaciones() {
        List<UbicacionEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM UbicacionHH", null);

        try {
            if (c.moveToFirst()) {
                do {
                    UbicacionEntity u = new UbicacionEntity();
                    u.setASysId(c.getString(c.getColumnIndexOrThrow("ASysId")));
                    u.setUbicacionA(c.getString(c.getColumnIndexOrThrow("UbicacionA")));
                    u.setBSysId(c.getString(c.getColumnIndexOrThrow("BSysId")));
                    u.setUbicacionB(c.getString(c.getColumnIndexOrThrow("UbicacionB")));
                    u.setCSysId(c.getString(c.getColumnIndexOrThrow("CSysId")));
                    u.setUbicacionC(c.getString(c.getColumnIndexOrThrow("UbicacionC")));
                    u.setDSysId(c.getString(c.getColumnIndexOrThrow("DSysId")));
                    u.setUbicacionD(c.getString(c.getColumnIndexOrThrow("UbicacionD")));

                    list.add(u);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo ubicaciones", e);
        } finally {
            c.close();
            db.close();
        }

        return list;
    }

    public UbicacionEntity getUbicacionByASysId(String aSysId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM UbicacionHH WHERE ASysId = ?",
                new String[]{ aSysId }
        );

        UbicacionEntity u = null;

        try {
            if (c.moveToFirst()) {
                u = new UbicacionEntity();
                u.setASysId(c.getString(c.getColumnIndexOrThrow("ASysId")));
                u.setUbicacionA(c.getString(c.getColumnIndexOrThrow("UbicacionA")));
                u.setBSysId(c.getString(c.getColumnIndexOrThrow("BSysId")));
                u.setUbicacionB(c.getString(c.getColumnIndexOrThrow("UbicacionB")));
                u.setCSysId(c.getString(c.getColumnIndexOrThrow("CSysId")));
                u.setUbicacionC(c.getString(c.getColumnIndexOrThrow("UbicacionC")));
                u.setDSysId(c.getString(c.getColumnIndexOrThrow("DSysId")));
                u.setUbicacionD(c.getString(c.getColumnIndexOrThrow("UbicacionD")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo ubicación por ASysId", e);
        } finally {
            c.close();
            db.close();
        }

        return u;
    }

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onSynced) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<UbicacionEntity>>() {}.getType();

        api.<List<UbicacionEntity>>get("GetUbicacionesHH", type, new ApiCallback<List<UbicacionEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<UbicacionEntity>> response) {
                if (response.success && response.data != null) {
                    syncUbicaciones(response.data);
                    if (onSynced != null) onSynced.run();
                } else {
                    Log.e(TAG, "Error al sincronizar ubicaciones: " + response.errorMessage);
                }
            }
        });
    }
}

