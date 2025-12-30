package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import android.net.Uri;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TomaFisicaTomasDao {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private static final String TAG = "DB_DAO_TFRESUMEN";

    public TomaFisicaTomasDao(Context context) {
        this.context = context;
        this.dbHelper = new AppDatabaseHelper(context);
    }

    private ContentValues entityToValues(TomaFisicaTomasEntity a) {
        ContentValues v = new ContentValues();
        v.put("TomaFisicaId", a.getTomaFisicaId());
        v.put("NumeroToma", a.getNumeroToma());
        v.put("IdToma", a.getIdToma());
        v.put("TotalLecturas", a.getTotalLecturas());
        v.put("FechaCreacion", a.getFechaCreacion());
        v.put("ActivosLeidos", a.getActivosLeidos());
        v.put("Sobrantes", a.getSobrantes());
        v.put("Faltantes", a.getFaltantes());
        v.put("TotalActivos", a.getTotalActivos());
        return v;
    }

    public long insert(TomaFisicaTomasEntity entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert("TomasFisicasResumen", null, entityToValues(entity));
    }

    public void syncResumen(List<TomaFisicaTomasEntity> data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaTomasEntity a : data) {
                ContentValues values = entityToValues(a);
                Log.d(TAG, "VALUES → " + values.toString());
                db.insertWithOnConflict("TomasFisicasResumen", null, entityToValues(a),
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando resumen", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        fetchAndSyncFromApi(null, onComplete);
    }

    public void fetchAndSyncFromApi(String tomaFisicaId, final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaTomasEntity>>() {}.getType();

        String endpoint = "TomasFisicas/TFResumen";
        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
            endpoint += "?tomaFisicaId=" + Uri.encode(tomaFisicaId.trim());
        }

        api.<List<TomaFisicaTomasEntity>>get(endpoint, type, new ApiCallback<List<TomaFisicaTomasEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaTomasEntity>> response) {
                if (response.success && response.data != null) {
                    syncResumen(response.data);
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public List<TomaFisicaTomasEntity> getByTomaFisicaId(String tomaFisicaId) {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Usar LOWER para comparar GUIDs sin importar mayúsculas/minúsculas
        Cursor cursor = db.rawQuery("SELECT * FROM TomasFisicasResumen WHERE LOWER(TomaFisicaId) = LOWER(?)", new String[]{tomaFisicaId});
        if (cursor.moveToFirst()) {
            do {
                TomaFisicaTomasEntity entity = new TomaFisicaTomasEntity();
                entity.setTomaFisicaId(cursor.getString(cursor.getColumnIndexOrThrow("TomaFisicaId")));
                entity.setNumeroToma(cursor.getString(cursor.getColumnIndexOrThrow("NumeroToma")));
                entity.setIdToma(cursor.getString(cursor.getColumnIndexOrThrow("IdToma")));
                entity.setTotalLecturas(cursor.getString(cursor.getColumnIndexOrThrow("TotalLecturas")));
                entity.setFechaCreacion(cursor.getString(cursor.getColumnIndexOrThrow("FechaCreacion")));
                entity.setActivosLeidos(cursor.getString(cursor.getColumnIndexOrThrow("ActivosLeidos")));
                entity.setSobrantes(cursor.getString(cursor.getColumnIndexOrThrow("Sobrantes")));
                entity.setFaltantes(cursor.getString(cursor.getColumnIndexOrThrow("Faltantes")));
                entity.setTotalActivos(cursor.getString(cursor.getColumnIndexOrThrow("TotalActivos")));
                list.add(entity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public TomaFisicaTomasEntity getByIdToma(String idToma) {
        if (idToma == null || idToma.trim().isEmpty()) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM TomasFisicasResumen WHERE LOWER(IdToma) = LOWER(?) LIMIT 1";

        try (Cursor c = db.rawQuery(sql, new String[]{idToma.trim()})) {
            if (!c.moveToFirst()) return null;

            TomaFisicaTomasEntity r = new TomaFisicaTomasEntity();
            r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("TomaFisicaId")));
            r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("NumeroToma")));
            r.setIdToma(c.getString(c.getColumnIndexOrThrow("IdToma")));
            r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("TotalLecturas")));
            r.setFechaCreacion(c.getString(c.getColumnIndexOrThrow("FechaCreacion")));
            r.setActivosLeidos(c.getString(c.getColumnIndexOrThrow("ActivosLeidos")));
            r.setSobrantes(c.getString(c.getColumnIndexOrThrow("Sobrantes")));
            r.setFaltantes(c.getString(c.getColumnIndexOrThrow("Faltantes")));
            r.setTotalActivos(c.getString(c.getColumnIndexOrThrow("TotalActivos")));
            return r;
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo TomasFisicasResumen por idToma", e);
            return null;
        } finally {
            db.close();
        }
    }

    public int getPendientesCount(String tomaFisicaId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM TomasFisicasResumen WHERE LOWER(TomaFisicaId) = LOWER(?)", new String[]{tomaFisicaId});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void pushSubtoma(TomaFisicaTomasEntity entity, ApiCallback<ApiResponse<Void>> callback) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        api.<ApiResponse<Void>>post("TomasFisicas/TFResumen", entity, type, callback);
    }

    public void pushLocalChangesToApi(final Runnable onAllFinished) {
        List<TomaFisicaTomasEntity> localData = getAll();
        if (localData.isEmpty()) {
            Log.d(TAG, "No hay resumenes locales para enviar al servidor");
            if (onAllFinished != null) onAllFinished.run();
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        final int total = localData.size();
        final int[] completed = {0};

        // Enviar todos los resumenes pendientes uno por uno
        for (TomaFisicaTomasEntity item : localData) {
            api.<ApiResponse<Void>>post("TomasFisicas/TFResumen", item, type, new ApiCallback<ApiResponse<Void>>() {
                @Override
                public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                    if (response.success) {
                        Log.d(TAG, "Resumen enviado exitosamente: " + item.getIdToma());
                    } else {
                        Log.e(TAG, "Error enviando resumen " + item.getIdToma() + ": " + response.errorMessage);
                    }
                    
                    completed[0]++;
                    if (completed[0] == total && onAllFinished != null) {
                        onAllFinished.run();
                    }
                }
            });
        }
    }

    public List<TomaFisicaTomasEntity> getResumenById(String tomaFisicaId) {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM TomasFisicasResumen WHERE LOWER(TomaFisicaId) = LOWER(?)";

        try (Cursor c = db.rawQuery(sql, new String[]{tomaFisicaId})) {

            while (c.moveToNext()) {
                TomaFisicaTomasEntity r = new TomaFisicaTomasEntity();

                r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("TomaFisicaId")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("NumeroToma")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("IdToma")));
                r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("TotalLecturas")));
                r.setFechaCreacion(c.getString(c.getColumnIndexOrThrow("FechaCreacion")));
                r.setActivosLeidos(c.getString(c.getColumnIndexOrThrow("ActivosLeidos")));
                r.setSobrantes(c.getString(c.getColumnIndexOrThrow("Sobrantes")));
                r.setFaltantes(c.getString(c.getColumnIndexOrThrow("Faltantes")));
                r.setTotalActivos(c.getString(c.getColumnIndexOrThrow("TotalActivos")));

                list.add(r);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo TomasFisicasResumen por tomaFisicaId", e);
        } finally {
            db.close();
        }

        return list;
    }

    public List<TomaFisicaTomasEntity> getAll() {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM TomasFisicasResumen";

        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                TomaFisicaTomasEntity r = new TomaFisicaTomasEntity();
                r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("TomaFisicaId")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("NumeroToma")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("IdToma")));
                r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("TotalLecturas")));
                r.setFechaCreacion(c.getString(c.getColumnIndexOrThrow("FechaCreacion")));
                r.setActivosLeidos(c.getString(c.getColumnIndexOrThrow("ActivosLeidos")));
                r.setSobrantes(c.getString(c.getColumnIndexOrThrow("Sobrantes")));
                r.setFaltantes(c.getString(c.getColumnIndexOrThrow("Faltantes")));
                r.setTotalActivos(c.getString(c.getColumnIndexOrThrow("TotalActivos")));
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all summaries", e);
        }
        return list;
    }
}
