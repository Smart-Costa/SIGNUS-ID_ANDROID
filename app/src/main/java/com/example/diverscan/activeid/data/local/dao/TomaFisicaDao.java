package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TomaFisicaDao {
    private static final String TAG = "DB_DAO_TOMASFISICAS";
    private final AppDatabaseHelper dbHelper;
    private final Context context;

    public TomaFisicaDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new AppDatabaseHelper(context);
    }

    private ContentValues entityToContentValues(TomaFisicaEntity a) {
        ContentValues v = new ContentValues();

        v.put("tomaFisicaId", a.getTomaFisicaId());
        v.put("nombre", a.getNombre());
        v.put("fechaInicial", a.getFechaInicial());
        v.put("fechaFinal", a.getFechaFinal());
        v.put("categoria", a.getCategoria());
        v.put("usuarioAsignado", a.getUsuarioAsignado());
        v.put("unidadOrganizativa", a.getUnidadOrganizativa());
        v.put("estadoActivo", a.getEstadoActivo());
        v.put("ubicacionA", a.getUbicacionA());
        v.put("ubicacionB", a.getUbicacionB());
        v.put("ubicacionC", a.getUbicacionC());
        v.put("ubicacionD", a.getUbicacionD());

        return v;
    }

    public List<TomaFisicaEntity> getAllTomasFisicas() {
        List<TomaFisicaEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TomasFisicas", null)) {
            if (c.moveToFirst()) {
                do {
                    TomaFisicaEntity a = new TomaFisicaEntity();
                    a.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                    a.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
                    a.setFechaInicial(c.getString(c.getColumnIndexOrThrow("fechaInicial")));
                    a.setFechaFinal(c.getString(c.getColumnIndexOrThrow("fechaFinal")));
                    a.setCategoria(c.getString(c.getColumnIndexOrThrow("categoria")));
                    a.setUsuarioAsignado(c.getString(c.getColumnIndexOrThrow("usuarioAsignado")));
                    a.setUnidadOrganizativa(c.getString(c.getColumnIndexOrThrow("unidadOrganizativa")));
                    a.setEstadoActivo(c.getString(c.getColumnIndexOrThrow("estadoActivo")));
                    a.setUbicacionA(c.getString(c.getColumnIndexOrThrow("ubicacionA")));
                    a.setUbicacionB(c.getString(c.getColumnIndexOrThrow("ubicacionB")));
                    a.setUbicacionC(c.getString(c.getColumnIndexOrThrow("ubicacionC")));
                    a.setUbicacionD(c.getString(c.getColumnIndexOrThrow("ubicacionD")));

                    list.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo tomas fisicas locales", e);
        } finally {
            db.close();
        }

        return list;
    }

    public List<TomaFisicaEntity> getTomaFisicaByName(String nombre) {
        List<TomaFisicaEntity> listbyName = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM TomasFisicas WHERE nombre LIKE '%' || ? || '%' LIMIT 5",
                new String[]{nombre}
        )) {
            if (c.moveToFirst()) {
                do {
                    TomaFisicaEntity a = new TomaFisicaEntity();
                    a.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                    a.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
                    a.setFechaInicial(c.getString(c.getColumnIndexOrThrow("fechaInicial")));
                    a.setFechaFinal(c.getString(c.getColumnIndexOrThrow("fechaFinal")));

                    listbyName.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error consultando por Nombre", e);
        } finally {
            db.close();
        }

        return listbyName;
    }

    public TomaFisicaEntity getTomaFisicaById(String id) {
        TomaFisicaEntity entity = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TomasFisicas WHERE tomaFisicaId = ?", new String[]{id})) {
            if (c.moveToFirst()) {
                entity = new TomaFisicaEntity();
                entity.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                entity.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
                entity.setFechaInicial(c.getString(c.getColumnIndexOrThrow("fechaInicial")));
                entity.setFechaFinal(c.getString(c.getColumnIndexOrThrow("fechaFinal")));
                entity.setCategoria(c.getString(c.getColumnIndexOrThrow("categoria")));
                entity.setUsuarioAsignado(c.getString(c.getColumnIndexOrThrow("usuarioAsignado")));
                entity.setUnidadOrganizativa(c.getString(c.getColumnIndexOrThrow("unidadOrganizativa")));
                entity.setEstadoActivo(c.getString(c.getColumnIndexOrThrow("estadoActivo")));
                entity.setUbicacionA(c.getString(c.getColumnIndexOrThrow("ubicacionA")));
                entity.setUbicacionB(c.getString(c.getColumnIndexOrThrow("ubicacionB")));
                entity.setUbicacionC(c.getString(c.getColumnIndexOrThrow("ubicacionC")));
                entity.setUbicacionD(c.getString(c.getColumnIndexOrThrow("ubicacionD")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo toma fisica por ID", e);
        } finally {
            db.close();
        }
        return entity;
    }

    public void syncTomasFisicas(List<TomaFisicaEntity> tomasfisicas) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaEntity a : tomasfisicas) {
                ContentValues values = entityToContentValues(a);
                db.insertWithOnConflict("TomasFisicas", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando tomas fisicas", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void fetchAndSyncFromApi(final Runnable onSuccess) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaEntity>>() {}.getType();

        api.<List<TomaFisicaEntity>>get("TomasFisicas", type, new ApiCallback<List<TomaFisicaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaEntity>> response) {
                if (response.success && response.data != null) {
                    syncTomasFisicas(response.data);
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
            }
        });
    }

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }
}
