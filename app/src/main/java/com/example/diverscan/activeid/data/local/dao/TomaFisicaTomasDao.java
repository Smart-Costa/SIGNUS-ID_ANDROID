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
            for (TomaFisicaTomasEntity a : list) {
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

    public void fetchAndSyncFromApi() {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaTomasEntity>>() {}.getType();

        api.<List<TomaFisicaTomasEntity>>get("TomasFisicas/TFResumen", type, new ApiCallback<List<TomaFisicaTomasEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaTomasEntity>> response) {
                if (response.success && response.data != null) {
                    syncResumen(response.data);
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
            }
        });
    }

    public List<TomaFisicaTomasEntity> getResumenById(String tomaFisicaId) {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT " +
                "tomaFisicaId, " +
                "numeroToma, " +
                "idToma, " +
                "totalLecturas " +
                "FROM TomasFisicasResumen " +
                "WHERE tomaFisicaId = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{tomaFisicaId})) {

            while (c.moveToNext()) {
                TomaFisicaTomasEntity r = new TomaFisicaTomasEntity();

                r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("numeroToma")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("idToma")));
                r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("totalLecturas")));

                list.add(r);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo TomasFisicasResumen por tomaFisicaId", e);
        } finally {
            db.close();
        }

        return list;
    }

    public List<TomaFisicaTomasEntity> getByTomaFisicaId(String tomaFisicaId) {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM TomasFisicasResumen WHERE tomaFisicaId = ?";
        String[] args = { tomaFisicaId };

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                TomaFisicaTomasEntity r = new TomaFisicaTomasEntity();
                r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("numeroToma")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("idToma")));
                r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("totalLecturas")));
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching summaries by ID", e);
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
                r.setTomaFisicaId(c.getString(c.getColumnIndexOrThrow("tomaFisicaId")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("numeroToma")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("idToma")));
                r.setTotalLecturas(c.getString(c.getColumnIndexOrThrow("totalLecturas")));
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all summaries", e);
        }
        return list;
    }
}
