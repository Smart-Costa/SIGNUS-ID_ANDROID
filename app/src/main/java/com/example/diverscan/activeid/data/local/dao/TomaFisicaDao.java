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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        }
        // Removed db.close() to avoid closing the shared connection managed by SQLiteOpenHelper

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
            // db.close(); // Evitar cierre prematuro de conexión compartida
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
        }
        // Removed db.close() to avoid closing the shared connection managed by SQLiteOpenHelper
        
        return entity;
    }

    public String getCategoryNameById(String categoryId) {
        String categoryName = "";
        if (categoryId == null || categoryId.trim().isEmpty()) return categoryName;

        String id = categoryId.trim();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 1. Try match by assetCategorySysId
        try (Cursor c = db.rawQuery("SELECT name FROM categoriaActivos WHERE assetCategorySysId = ?", new String[]{id})) {
            if (c.moveToFirst()) {
                categoryName = c.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error consultando categoria por SysId: " + id, e);
        }

        // 2. If not found, try match by _id
        if (categoryName.isEmpty()) {
            try (Cursor c = db.rawQuery("SELECT name FROM categoriaActivos WHERE _id = ?", new String[]{id})) {
                if (c.moveToFirst()) {
                    categoryName = c.getString(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error consultando categoria por _id: " + id, e);
            }
        }
        
        // 3. If still not found, try case-insensitive match on SysId
         if (categoryName.isEmpty()) {
            try (Cursor c = db.rawQuery("SELECT name FROM categoriaActivos WHERE lower(assetCategorySysId) = ?", new String[]{id.toLowerCase()})) {
                if (c.moveToFirst()) {
                    categoryName = c.getString(0);
                }
            } catch (Exception e) {
                 Log.e(TAG, "Error consultando categoria por SysId (lower): " + id, e);
            }
        }

        if (db != null && db.isOpen()) {
            db.close();
        }
        return categoryName;
    }

    public void syncTomasFisicas(List<TomaFisicaEntity> tomasfisicas) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Limpiar tabla antes de insertar nuevos registros del servidor
            db.delete("TomasFisicas", null, null);
            
            for (TomaFisicaEntity a : tomasfisicas) {
                ContentValues values = entityToContentValues(a);
                db.insertWithOnConflict("TomasFisicas", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            // Evita volver a mostrar tomas huérfanas: si el servidor ya no envía una toma (porque fue eliminada
            // o dejó de ser válida), se limpia del SQLite junto con sus subtomas y detalles asociados.
            deleteOrphanedTomasFisicas(db, tomasfisicas);
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
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
                
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        });
    }

    private void deleteOrphanedTomasFisicas(SQLiteDatabase db, List<TomaFisicaEntity> remote) {
        if (db == null || remote == null) return;

        Set<String> remoteIds = new HashSet<>();
        for (TomaFisicaEntity r : remote) {
            if (r == null) continue;
            String id = r.getTomaFisicaId();
            if (id != null && !id.trim().isEmpty()) {
                remoteIds.add(id.trim().toLowerCase());
            }
        }

        List<String> localIds = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT tomaFisicaId FROM TomasFisicas", null)) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                if (id != null && !id.trim().isEmpty()) {
                    localIds.add(id.trim());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo IDs locales de TomasFisicas", e);
            return;
        }

        for (String localId : localIds) {
            if (localId == null) continue;
            String normalized = localId.trim().toLowerCase();
            if (normalized.isEmpty()) continue;

            if (!remoteIds.contains(normalized)) {
                try {
                    List<String> idTomas = new ArrayList<>();
                    try (Cursor c = db.rawQuery(
                            "SELECT IdToma FROM TomasFisicasResumen WHERE LOWER(TomaFisicaId) = LOWER(?)",
                            new String[]{localId})) {
                        while (c.moveToNext()) {
                            String idToma = c.getString(0);
                            if (idToma != null && !idToma.trim().isEmpty()) {
                                idTomas.add(idToma.trim());
                            }
                        }
                    }

                    for (String idToma : idTomas) {
                        db.delete("TomasFisicasDetalle", "LOWER(IdToma) = LOWER(?)", new String[]{idToma});
                    }

                    db.delete("TomasFisicasResumen", "LOWER(TomaFisicaId) = LOWER(?)", new String[]{localId});
                    db.delete("TomasFisicas", "LOWER(tomaFisicaId) = LOWER(?)", new String[]{localId});

                    try {
                        db.delete("TomaFisicaDetalle", "LOWER(FK_TomaFisica) = LOWER(?)", new String[]{localId});
                    } catch (Exception ignored) {
                    }

                    Log.d(TAG, "Eliminada toma huérfana local: " + localId);
                } catch (Exception e) {
                    Log.e(TAG, "Error eliminando toma huérfana local: " + localId, e);
                }
            }
        }
    }

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }
}
