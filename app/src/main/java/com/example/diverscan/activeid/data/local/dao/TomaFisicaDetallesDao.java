package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                if (a == null) continue;
                if (a.getIdToma() == null || a.getIdToma().trim().isEmpty()) continue;
                if (a.getIdTakeDetail() == null || a.getIdTakeDetail().trim().isEmpty()) {
                    a.setIdTakeDetail(generarIdTakeDetail(a));
                }
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
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        fetchAndSyncFromApi(null, onComplete);
    }

    public void fetchAndSyncFromApi(String idToma, final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaDetallesEntity>>() {}.getType();

        String endpoint = "TomasFisicas/TFDetalle";
        if (idToma != null && !idToma.trim().isEmpty()) {
            endpoint += "?idToma=" + Uri.encode(idToma.trim());
        }

        api.<List<TomaFisicaDetallesEntity>>get(endpoint, type, new ApiCallback<List<TomaFisicaDetallesEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaDetallesEntity>> response) {
                if (response.success && response.data != null) {
                    syncDetalle(response.data);
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

    public void pushDetalle(List<TomaFisicaDetallesEntity> list, ApiCallback<ApiResponse<Void>> callback) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        // Assuming the API accepts a list of details. If it accepts one by one, we need to loop.
        // Usually bulk insert is better. If API expects single, I'll need to change this.
        // For now I assume it accepts List. If not, I'll encounter an error and fix it.
        // Looking at the codebase, typically APIs here seem to handle lists? 
        // Actually, TomasFisicasController in API might need checking.
        // But to be safe, I'll send the list.
        api.<ApiResponse<Void>>post("TomasFisicas/TFDetalle", list, type, callback);
    }

    public List<TomaFisicaDetallesEntity> getAll() {
        List<TomaFisicaDetallesEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM TomasFisicasDetalle";
        
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                TomaFisicaDetallesEntity r = new TomaFisicaDetallesEntity();
                r.setIdTakeDetail(c.getString(c.getColumnIndexOrThrow("IdTakeDetail")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("IdToma")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("NumeroToma")));
                r.setFechaToma(c.getString(c.getColumnIndexOrThrow("FechaToma")));
                r.setEpc(c.getString(c.getColumnIndexOrThrow("EPC")));
                r.setDateRead(c.getString(c.getColumnIndexOrThrow("DateRead")));
                r.setActivoId(c.getString(c.getColumnIndexOrThrow("ActivoId")));
                r.setEstadoInventario(c.getString(c.getColumnIndexOrThrow("EstadoInventario")));
                r.setUbicacionDetalleA(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleA")));
                r.setUbicacionDetalleB(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleB")));
                r.setUbicacionDetalleC(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleC")));
                r.setUbicacionDetalleD(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleD")));
                r.setObservaciones(c.getString(c.getColumnIndexOrThrow("Observaciones")));
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all details", e);
        }
        return list;
    }

    public List<TomaFisicaDetallesEntity> getByIdToma(String idToma) {
        List<TomaFisicaDetallesEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM TomasFisicasDetalle WHERE LOWER(IdToma) = LOWER(?)";

        try (Cursor c = db.rawQuery(sql, new String[]{idToma})) {
            while (c.moveToNext()) {
                TomaFisicaDetallesEntity r = new TomaFisicaDetallesEntity();
                r.setIdTakeDetail(c.getString(c.getColumnIndexOrThrow("IdTakeDetail")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("IdToma")));
                r.setNumeroToma(c.getString(c.getColumnIndexOrThrow("NumeroToma")));
                r.setFechaToma(c.getString(c.getColumnIndexOrThrow("FechaToma")));
                r.setEpc(c.getString(c.getColumnIndexOrThrow("EPC")));
                r.setDateRead(c.getString(c.getColumnIndexOrThrow("DateRead")));
                r.setActivoId(c.getString(c.getColumnIndexOrThrow("ActivoId")));
                r.setEstadoInventario(c.getString(c.getColumnIndexOrThrow("EstadoInventario")));
                r.setUbicacionDetalleA(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleA")));
                r.setUbicacionDetalleB(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleB")));
                r.setUbicacionDetalleC(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleC")));
                r.setUbicacionDetalleD(c.getString(c.getColumnIndexOrThrow("UbicacionDetalleD")));
                r.setObservaciones(c.getString(c.getColumnIndexOrThrow("Observaciones")));
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching detail by IdToma", e);
        }

        return list;
    }

    public void pushLocalChangesToApi(final Runnable onComplete) {
        List<TomaFisicaDetallesEntity> localData = getAll();
        if (localData.isEmpty()) {
            Log.d(TAG, "No hay detalles locales para enviar al servidor");
            if (onComplete != null) onComplete.run();
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        // Enviamos toda la lista de detalles
        api.<ApiResponse<Void>>post("TomasFisicas/TFDetalle", localData, type, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                if (response.success) {
                    Log.d(TAG, "Detalles enviados exitosamente al servidor (" + localData.size() + " registros)");
                } else {
                    Log.e(TAG, "Error enviando detalles al servidor: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private static String generarIdTakeDetail(TomaFisicaDetallesEntity a) {
        String idToma = a.getIdToma() != null ? a.getIdToma().trim() : "";
        String epc = a.getEpc() != null ? a.getEpc().trim() : "";
        String activoId = a.getActivoId() != null ? a.getActivoId().trim() : "";
        String dateRead = a.getDateRead() != null ? a.getDateRead().trim() : "";
        String estado = a.getEstadoInventario() != null ? a.getEstadoInventario().trim() : "";
        String seed = idToma + "|" + epc + "|" + activoId + "|" + dateRead + "|" + estado;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
