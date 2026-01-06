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
                if (result == -1) {
                    Log.e(TAG, "Error inserting UbicacionHH: " + u.getUbicacionA());
                } else {
                    // Log.d(TAG, "Insert UbicacionHH → resultado=" + result);
                }
            }
            Log.d(TAG, "Total ubicaciones insertadas: " + lista.size());

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
        Cursor c = null;

        try {
            c = db.rawQuery("SELECT * FROM UbicacionHH", null);
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
            if (c != null) c.close();
            db.close();
        }

        return list;
    }

    public UbicacionEntity getUbicacionByASysId(String aSysId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        UbicacionEntity u = null;

        try {
            c = db.rawQuery(
                    "SELECT * FROM UbicacionHH WHERE ASysId = ?",
                    new String[]{ aSysId }
            );

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
            if (c != null) c.close();
            db.close();
        }

        return u;
    }

    public UbicacionEntity getUbicacionByBSysId(String bSysId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        UbicacionEntity u = null;

        try {
            c = db.rawQuery(
                    "SELECT * FROM UbicacionHH WHERE BSysId = ? LIMIT 1",
                    new String[]{ bSysId }
            );

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
            Log.e(TAG, "Error obteniendo ubicación por BSysId", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }

        return u;
    }

    public UbicacionEntity getUbicacionByCSysId(String cSysId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        UbicacionEntity u = null;

        try {
            c = db.rawQuery(
                    "SELECT * FROM UbicacionHH WHERE CSysId = ? LIMIT 1",
                    new String[]{ cSysId }
            );

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
            Log.e(TAG, "Error obteniendo ubicación por CSysId", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }

        return u;
    }

    public UbicacionEntity getUbicacionByDSysId(String dSysId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        UbicacionEntity u = null;

        try {
            c = db.rawQuery(
                    "SELECT * FROM UbicacionHH WHERE DSysId = ? LIMIT 1",
                    new String[]{ dSysId }
            );

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
            Log.e(TAG, "Error obteniendo ubicación por DSysId", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }

        return u;
    }

    // --- Helper methods for cascading spinners ---

    public List<UbicacionEntity> getDistinctUbicacionA() {
        List<UbicacionEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            // Select distinct ASysId and UbicacionA
            c = db.rawQuery("SELECT DISTINCT ASysId, UbicacionA FROM UbicacionHH WHERE ASysId IS NOT NULL AND ASysId != '' ORDER BY UbicacionA", null);
            if (c.moveToFirst()) {
                do {
                    UbicacionEntity u = new UbicacionEntity();
                    u.setASysId(c.getString(0));
                    u.setUbicacionA(c.getString(1));
                    list.add(u);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getDistinctUbicacionA", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return list;
    }

    public List<UbicacionEntity> getDistinctUbicacionB(String aSysId) {
        List<UbicacionEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT DISTINCT BSysId, UbicacionB FROM UbicacionHH WHERE ASysId = ? AND BSysId IS NOT NULL AND BSysId != '' ORDER BY UbicacionB", new String[]{aSysId});
            if (c.moveToFirst()) {
                do {
                    UbicacionEntity u = new UbicacionEntity();
                    u.setBSysId(c.getString(0));
                    u.setUbicacionB(c.getString(1));
                    list.add(u);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getDistinctUbicacionB", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return list;
    }

    public List<UbicacionEntity> getDistinctUbicacionC(String bSysId) {
        List<UbicacionEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT DISTINCT CSysId, UbicacionC FROM UbicacionHH WHERE BSysId = ? AND CSysId IS NOT NULL AND CSysId != '' ORDER BY UbicacionC", new String[]{bSysId});
            if (c.moveToFirst()) {
                do {
                    UbicacionEntity u = new UbicacionEntity();
                    u.setCSysId(c.getString(0));
                    u.setUbicacionC(c.getString(1));
                    list.add(u);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getDistinctUbicacionC", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return list;
    }

    public List<UbicacionEntity> getDistinctUbicacionD(String cSysId) {
        List<UbicacionEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT DISTINCT DSysId, UbicacionD FROM UbicacionHH WHERE CSysId = ? AND DSysId IS NOT NULL AND DSysId != '' ORDER BY UbicacionD", new String[]{cSysId});
            if (c.moveToFirst()) {
                do {
                    UbicacionEntity u = new UbicacionEntity();
                    u.setDSysId(c.getString(0));
                    u.setUbicacionD(c.getString(1));
                    list.add(u);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getDistinctUbicacionD", e);
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return list;
    }


    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onSynced) {
        ApiClient api = ApiClient.getInstance(context);
        
        api.<com.google.gson.JsonElement>get("GetUbicacionesHH", com.google.gson.JsonElement.class, new ApiCallback<com.google.gson.JsonElement>() {
            @Override
            public void onComplete(ApiResponse<com.google.gson.JsonElement> response) {
                if (response.success && response.data != null) {
                    Log.d(TAG, "Raw JSON Ubicaciones: " + response.data.toString());
                    
                    try {
                        Type type = new TypeToken<List<UbicacionEntity>>() {}.getType();
                        List<UbicacionEntity> lista = api.getGson().fromJson(response.data, type);
                        
                        if (lista != null) {
                            syncUbicaciones(lista);
                            if (onSynced != null) onSynced.run();
                        } else {
                            Log.e(TAG, "Lista nula tras deserializar");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error deserializando ubicaciones", e);
                    }
                } else {
                    Log.e(TAG, "Error al sincronizar ubicaciones: " + response.errorMessage);
                }
            }
        });
    }
}

