package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class TomaFisicaDetallesDao {
    private final AppDatabaseHelper dbHelper;
    private final Context context;
    private static final String TAG = "DB_DAO_TFDETALLE";

    public TomaFisicaDetallesDao(Context context) {
        this.context = context;
        this.dbHelper = new AppDatabaseHelper(context);
    }

    private ContentValues entityToValues(TomaFisicaDetallesEntity a) {
        ContentValues v = new ContentValues();
        v.put("IdTakeDetail", a.getIdTakeDetail());
        v.put("IdToma", a.getIdToma());
        v.put("NumeroToma", a.getNumeroToma());
        v.put("FechaToma", a.getFechaToma());
        v.put("EPC", a.getEpc());
        v.put("DateRead", a.getDateRead());
        v.put("ActivoId", a.getActivoId());
        v.put("EstadoInventario", a.getEstadoInventario());
        v.put("UbicacionDetalleA", a.getUbicacionDetalleA());
        v.put("UbicacionDetalleB", a.getUbicacionDetalleB());
        v.put("UbicacionDetalleC", a.getUbicacionDetalleC());
        v.put("UbicacionDetalleD", a.getUbicacionDetalleD());
        v.put("Observaciones", a.getObservaciones());
        return v;
    }

    public void syncDetalle(List<TomaFisicaDetallesEntity> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaDetallesEntity a : list) {
                db.insertWithOnConflict("TomasFisicasDetalle", null, entityToValues(a),
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando detalle", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void fetchAndSyncFromApi() {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaDetallesEntity>>() {}.getType();

        api.<List<TomaFisicaDetallesEntity>>get("TomasFisicas/TFDetalle", type, new ApiCallback<List<TomaFisicaDetallesEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaDetallesEntity>> response) {
                if (response.success && response.data != null) {
                    syncDetalle(response.data);
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
            }
        });
    }
}
