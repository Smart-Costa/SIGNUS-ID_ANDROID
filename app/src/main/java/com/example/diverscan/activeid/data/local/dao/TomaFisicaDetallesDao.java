package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        // SYNC_STATUS handling is external (in insert/update calls) or defaults to 0
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
                ContentValues v = entityToValues(a);
                v.put("SYNC_STATUS", 1); // From API -> Synced
                db.insertWithOnConflict("TomasFisicasDetalle", null, v,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando detalle", e);
        } finally {
            db.endTransaction();
            // db.close();
        }
    }

    /**
     * Guarda detalles locales marcándolos como pendientes de envío.
     */
    public void saveLocal(List<TomaFisicaDetallesEntity> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaDetallesEntity a : list) {
                if (a == null) continue;
                if (a.getIdToma() == null || a.getIdToma().trim().isEmpty()) continue;
                if (a.getIdTakeDetail() == null || a.getIdTakeDetail().trim().isEmpty()) {
                    a.setIdTakeDetail(generarIdTakeDetail(a));
                }
                ContentValues v = entityToValues(a);
                v.put("SYNC_STATUS", 0); // Local change -> Pending
                db.insertWithOnConflict("TomasFisicasDetalle", null, v,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando detalle local", e);
        } finally {
            db.endTransaction();
            // db.close();
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
                    List<String> deletedTomas = getPendingDeleteRefIds("TFDetalleByToma");
                    if (deletedTomas != null && !deletedTomas.isEmpty()) {
                        List<TomaFisicaDetallesEntity> filtered = new ArrayList<>();
                        for (TomaFisicaDetallesEntity item : response.data) {
                            if (item == null) continue;
                            String t = item.getIdToma();
                            if (t != null && containsIgnoreCase(deletedTomas, t.trim())) {
                                continue;
                            }
                            filtered.add(item);
                        }

                        // Limpieza PREVIA: Eliminar datos sincronizados antiguos
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        try {
                            String where = "SYNC_STATUS = 1";
                            List<String> args = new ArrayList<>();
                            if (idToma != null && !idToma.trim().isEmpty()) {
                                where += " AND LOWER(IdToma) = LOWER(?)";
                                args.add(idToma.trim());
                            }
                            db.delete("TomasFisicasDetalle", where, args.toArray(new String[0]));
                            Log.d(TAG, "Limpiados detalles sincronizados previos. Filtro idToma=" + idToma);
                        } catch (Exception e) {
                            Log.e(TAG, "Error limpiando detalles previos", e);
                        }

                        syncDetalle(filtered);
                        
                    } else {
                        // Limpieza PREVIA (Caso sin deletes pendientes)
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        try {
                            String where = "SYNC_STATUS = 1";
                            List<String> args = new ArrayList<>();
                            if (idToma != null && !idToma.trim().isEmpty()) {
                                where += " AND LOWER(IdToma) = LOWER(?)";
                                args.add(idToma.trim());
                            }
                            db.delete("TomasFisicasDetalle", where, args.toArray(new String[0]));
                            Log.d(TAG, "Limpiados detalles sincronizados previos. Filtro idToma=" + idToma);
                        } catch (Exception e) {
                            Log.e(TAG, "Error limpiando detalles previos", e);
                        }

                        syncDetalle(response.data);
                    }
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                } else if (response.statusCode == 404) {
                    Log.w(TAG, "API devolvió 404 (Not Found) para TFDetalle. Se asume lista vacía y se limpian locales.");
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    try {
                        String where = "SYNC_STATUS = 1";
                        List<String> args = new ArrayList<>();
                        if (idToma != null && !idToma.trim().isEmpty()) {
                            where += " AND LOWER(IdToma) = LOWER(?)";
                            args.add(idToma.trim());
                        }
                        db.delete("TomasFisicasDetalle", where, args.toArray(new String[0]));
                        Log.d(TAG, "Limpiados detalles sincronizados previos (por 404). Filtro idToma=" + idToma);
                    } catch (Exception e) {
                        Log.e(TAG, "Error limpiando detalles previos en 404", e);
                    }
                } else {
                    Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    public void pushDetalle(List<TomaFisicaDetallesEntity> list, ApiCallback<JsonObject> callback) {
        if (list != null) {
            Log.d(TAG, "pushDetalle: Enviando " + list.size() + " detalles.");
        } else {
            Log.d(TAG, "pushDetalle: Lista de detalles es nula.");
        }
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();

        List<TfDetalleRequest> payload = new ArrayList<>();
        if (list != null) {
            for (TomaFisicaDetallesEntity d : list) {
                if (d == null) continue;
                // VALIDACION: No subir activos que no existen en la base de datos ("No Inventariado")
                if ("No Inventariado".equalsIgnoreCase(d.getEstadoInventario())) {
                    continue;
                }
                payload.add(toRequest(d));
            }
        }
        
        Log.d(TAG, "pushDetalle: Payload size=" + payload.size());

        api.<JsonObject>post("TomasFisicas/TFDetalle", payload, type, callback);
    }

    private static class TfDetalleRequest {
        @SerializedName("IdTakeDetail")
        String idTakeDetail;

        @SerializedName("IdToma")
        String idToma;

        @SerializedName("FK_TomaFisica")
        String fkTomaFisicaUpper;

        @SerializedName("Fk_TomaFisica")
        String fkTomaFisicaCamel;

        @SerializedName("NumeroToma")
        String numeroToma;

        @SerializedName("FechaToma")
        String fechaToma;

        @SerializedName("EPC")
        String epc;

        @SerializedName("DateRead")
        String dateRead;

        @SerializedName("ActivoId")
        String activoId;

        @SerializedName("EstadoInventario")
        String estadoInventario;

        @SerializedName("UbicacionDetalleA")
        String ubicacionDetalleA;

        @SerializedName("UbicacionDetalleB")
        String ubicacionDetalleB;

        @SerializedName("UbicacionDetalleC")
        String ubicacionDetalleC;

        @SerializedName("UbicacionDetalleD")
        String ubicacionDetalleD;

        @SerializedName("Observaciones")
        String observaciones;
    }

    private static TfDetalleRequest toRequest(TomaFisicaDetallesEntity d) {
        TfDetalleRequest r = new TfDetalleRequest();
        r.idTakeDetail = d != null ? safe(d.getIdTakeDetail()) : "";
        r.idToma = d != null ? safe(d.getIdToma()) : "";
        r.fkTomaFisicaUpper = r.idToma;
        r.fkTomaFisicaCamel = r.idToma;
        r.numeroToma = d != null ? safe(d.getNumeroToma()) : "";
        r.fechaToma = d != null ? safe(d.getFechaToma()) : "";
        if (r.fechaToma.isEmpty()) {
            r.fechaToma = d != null ? safe(d.getDateRead()) : "";
        }
        r.epc = d != null ? safe(d.getEpc()) : "";
        r.dateRead = d != null ? safe(d.getDateRead()) : "";
        r.activoId = d != null ? safeGuidOptional(d.getActivoId()) : null;
        r.estadoInventario = d != null ? safe(d.getEstadoInventario()) : "";
        r.ubicacionDetalleA = d != null ? safeGuidRequired(d.getUbicacionDetalleA()) : "00000000-0000-0000-0000-000000000000";
        r.ubicacionDetalleB = d != null ? safeGuidOptional(d.getUbicacionDetalleB()) : null;
        r.ubicacionDetalleC = d != null ? safeGuidOptional(d.getUbicacionDetalleC()) : null;
        r.ubicacionDetalleD = d != null ? safeGuidOptional(d.getUbicacionDetalleD()) : null;
        r.observaciones = d != null ? safe(d.getObservaciones()) : "";
        return r;
    }

    private static String safeGuidRequired(String s) {
        String v = normalizeGuid(s);
        if (v == null) return "00000000-0000-0000-0000-000000000000";
        return v;
    }

    private static String safeGuidOptional(String s) {
        return normalizeGuid(s);
    }

    private static String normalizeGuid(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        if (v.equalsIgnoreCase("NULL")) return null;
        if (v.equals("00000000-0000-0000-0000-000000000000")) return null;
        try {
            java.util.UUID.fromString(v);
            return v;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean serverSuccess(JsonObject body) {
        if (body == null) return false;
        if (!body.has("success")) return false;
        try {
            return body.get("success").getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String serverMessage(JsonObject body) {
        if (body == null) return "";
        try {
            if (body.has("message") && !body.get("message").isJsonNull()) {
                return body.get("message").getAsString();
            }
            if (body.has("errorMessage") && !body.get("errorMessage").isJsonNull()) {
                return body.get("errorMessage").getAsString();
            }
            return body.toString();
        } catch (Exception e) {
            return "";
        }
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

    public int getPendienteStatus(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int status = 1;
        try (Cursor c = db.rawQuery("SELECT SYNC_STATUS FROM TomasFisicasDetalle WHERE IdTakeDetail = ?", new String[]{id})) {
            if (c.moveToFirst()) {
                status = c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking sync status", e);
        }
        return status;
    }

    public void deleteByToma(String idToma) {
        if (idToma == null || idToma.trim().isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String trimmed = idToma.trim();
        db.beginTransaction();
        try {
            insertarPendingDelete(db, "TFDetalleByToma", trimmed);
            int rows = db.delete("TomasFisicasDetalle", "LOWER(IdToma) = LOWER(?)", new String[]{trimmed});
            Log.d(TAG, "Detalles eliminados local para toma: " + trimmed + " Rows affected: " + rows);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting details for toma " + trimmed, e);
        } finally {
            db.endTransaction();
            // db.close();
        }
    }

    public List<TomaFisicaDetallesEntity> getPendingDetalles() {
        List<TomaFisicaDetallesEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Get from New Table (SYNC_STATUS = 0 is Pending)
        try (Cursor c = db.rawQuery("SELECT * FROM TomasFisicasDetalle WHERE SYNC_STATUS = 0", null)) {
            while (c.moveToNext()) {
                list.add(cursorToEntity(c));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching pending details from TomasFisicasDetalle", e);
        }

        // 2. Get from Legacy Table (TomaFisicaDetalle) where Sync = '1' (Pending)
        // Check if table exists first to avoid crash if it was dropped?
        // AppDatabaseHelper doesn't create it, InventoryDBHelper does.
        // But we are using AppDatabaseHelper to get DB. If both helpers use same DB name/version, it's fine.
        try (Cursor c = db.rawQuery("SELECT * FROM TomaFisicaDetalle WHERE Sync = '1'", null)) {
            while (c.moveToNext()) {
                TomaFisicaDetallesEntity r = new TomaFisicaDetallesEntity();
                r.setIdTakeDetail(c.getString(c.getColumnIndexOrThrow("idTakeDetail")));
                r.setIdToma(c.getString(c.getColumnIndexOrThrow("FK_TomaFisica")));
                r.setEpc(c.getString(c.getColumnIndexOrThrow("EPC")));
                r.setDateRead(c.getString(c.getColumnIndexOrThrow("DateRead")));
                // Map other fields to defaults or null
                // NumeroToma, ActivoId, etc. might be missing
                // Set default for non-nullable API fields (UbicacionDetalleA) to avoid 400 error
                r.setUbicacionDetalleA("00000000-0000-0000-0000-000000000000");
                list.add(r);
            }
        } catch (Exception e) {
             // Table might not exist or other error. Log but don't crash.
             Log.w(TAG, "Could not fetch from TomaFisicaDetalle (Legacy): " + e.getMessage());
        }

        return list;
    }

    private TomaFisicaDetallesEntity cursorToEntity(Cursor c) {
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
        return r;
    }

    public void markAsSynced(String idTakeDetail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // Try updating New Table
            ContentValues cv = new ContentValues();
            cv.put("SYNC_STATUS", 1);
            int rows = db.update("TomasFisicasDetalle", cv, "IdTakeDetail = ?", new String[]{idTakeDetail});
            
            // Try updating Legacy Table
            // Legacy uses Sync='1' for Pending. We set to '0' for Synced?
            // Assuming '0' is Synced for legacy.
            if (rows == 0) {
                ContentValues cvLegacy = new ContentValues();
                cvLegacy.put("Sync", "0");
                db.update("TomaFisicaDetalle", cvLegacy, "idTakeDetail = ?", new String[]{idTakeDetail});
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking detail as synced", e);
        } finally {
            // db.close();
        }
    }

    public void pushLocalChangesToApi(final Runnable onComplete) {
        List<TomaFisicaDetallesEntity> localData = getPendingDetalles();
        Runnable pushDeletesThenFinish = () -> pushPendingDeletesToApi(onComplete);

        if (localData.isEmpty()) {
            Log.d(TAG, "No hay detalles pendientes para enviar al servidor");
            pushDeletesThenFinish.run();
            return;
        }

        List<TomaFisicaDetallesEntity> sendable = new ArrayList<>();
        Map<String, Integer> skippedByReason = new HashMap<>();

        for (TomaFisicaDetallesEntity d : localData) {
            if (d == null) {
                inc(skippedByReason, "null_detail");
                continue;
            }
            
            // VALIDACION: No subir activos que no existen en la base de datos ("No Inventariado")
            if ("No Inventariado".equalsIgnoreCase(d.getEstadoInventario())) {
                inc(skippedByReason, "no_inventariado");
                // Log.w(TAG, "Omitiendo detalle por ser 'No Inventariado': " + safe(d.getEpc()));
                continue;
            }

            String idToma = d.getIdToma() != null ? d.getIdToma().trim() : "";
            if (idToma.isEmpty()) {
                inc(skippedByReason, "missing_idToma");
                continue;
            }

            Integer headerStatus = getHeaderSyncStatus(idToma);
            if (headerStatus == null) {
                inc(skippedByReason, "header_missing");
                Log.w(TAG, "Omitiendo detalle: header no existe en SQLite. idToma=" + idToma + " idTakeDetail=" + safe(d.getIdTakeDetail()));
                continue;
            }
            if (headerStatus == 0) {
                inc(skippedByReason, "header_pending");
                Log.w(TAG, "Omitiendo detalle: header aún pendiente (SYNC_STATUS=0). idToma=" + idToma + " idTakeDetail=" + safe(d.getIdTakeDetail()));
                continue;
            }

            sendable.add(d);
        }

        Log.d(TAG, "TFDetalle pendientes total=" + localData.size() + " | enviables=" + sendable.size() + " | omitidos=" + (localData.size() - sendable.size()) + " | razones=" + skippedByReason);

        Map<String, Integer> countByIdToma = new HashMap<>();
        Map<String, Integer> countByEstado = new HashMap<>();
        int sinActivoId = 0;
        for (TomaFisicaDetallesEntity d : sendable) {
            String idT = d != null && d.getIdToma() != null ? d.getIdToma().trim().toLowerCase() : "";
            if (idT.isEmpty()) idT = "(sin_idToma)";
            countByIdToma.put(idT, countByIdToma.getOrDefault(idT, 0) + 1);

            String est = d != null && d.getEstadoInventario() != null ? d.getEstadoInventario().trim() : "";
            if (est.isEmpty()) est = "(sin_estado)";
            countByEstado.put(est, countByEstado.getOrDefault(est, 0) + 1);

            String actId = d != null && d.getActivoId() != null ? d.getActivoId().trim() : "";
            if (actId.isEmpty()) sinActivoId++;
        }
        Log.d(TAG, "TFDetalle enviables resumen: porIdToma=" + countByIdToma + " porEstado=" + countByEstado + " sinActivoId=" + sinActivoId);

        if (sendable.isEmpty()) {
            pushDeletesThenFinish.run();
            return;
        }

        ActivoDao activoDao = new ActivoDao(context);
        int logLimit = Math.min(sendable.size(), 50);
        for (int i = 0; i < logLimit; i++) {
            TomaFisicaDetallesEntity d = sendable.get(i);
            ActivoEntity a = null;
            String activoId = d.getActivoId() != null ? d.getActivoId().trim() : "";
            if (!activoId.isEmpty()) {
                a = activoDao.getActivoByIdActivo(activoId);
            }
            Log.d(TAG, "TFDetalle enviar #" + (i + 1)
                    + " idToma=" + safe(d.getIdToma())
                    + " nro=" + safe(d.getNumeroToma())
                    + " idTakeDetail=" + safe(d.getIdTakeDetail())
                    + " activoId=" + safe(d.getActivoId())
                    + " epc=" + safe(d.getEpc())
                    + " estado=" + safe(d.getEstadoInventario())
                    + " activo=" + formatActivo(a));
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();

        List<TfDetalleRequest> payload = new ArrayList<>();
        for (TomaFisicaDetallesEntity d : sendable) {
            payload.add(toRequest(d));
        }

        api.<JsonObject>post("TomasFisicas/TFDetalle", payload, type, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                if (response.success) {
                    boolean ok = serverSuccess(response.data);
                    if (ok) {
                        Log.d(TAG, "Detalles enviados exitosamente al servidor (" + sendable.size() + " registros). Status=" + response.statusCode);
                        for (TomaFisicaDetallesEntity item : sendable) {
                            markAsSynced(item.getIdTakeDetail());
                        }
                    } else {
                        Log.e(TAG, "Error servidor enviando detalles. Status=" + response.statusCode + " Body=" + serverMessage(response.data));
                    }
                } else {
                    Log.e(TAG, "Error enviando detalles al servidor. Status=" + response.statusCode + " Error=" + response.errorMessage);
                    Map<String, Integer> byIdToma = new HashMap<>();
                    for (TomaFisicaDetallesEntity d : sendable) {
                        String idToma = d != null && d.getIdToma() != null ? d.getIdToma().trim().toLowerCase() : "";
                        if (idToma.isEmpty()) idToma = "(sin_idToma)";
                        byIdToma.put(idToma, byIdToma.getOrDefault(idToma, 0) + 1);
                    }
                    Log.e(TAG, "TFDetalle fallo por idToma=" + byIdToma);

                    String em = response.errorMessage != null ? response.errorMessage : "";
                    if (response.statusCode == 500 && em.toLowerCase().contains("foreign key")) {
                        for (String idTomaKey : byIdToma.keySet()) {
                            if (idTomaKey == null || idTomaKey.startsWith("(")) continue;
                            markHeaderAsPending(idTomaKey);
                        }
                    }
                    if (em.toLowerCase().contains("duplicate key") || em.toLowerCase().contains("violation of unique key")) {
                         Log.e(TAG, "ERROR CRITICO: Clave duplicada detectada. El servidor rechazo los datos porque ya existen con otros IDs. " + em);
                    }
                }
                pushDeletesThenFinish.run();
            }
        });
    }

    private void markHeaderAsPending(String idToma) {
        if (idToma == null || idToma.trim().isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put("SYNC_STATUS", 0);
            db.update("TomasFisicasResumen", cv, "LOWER(IdToma)=LOWER(?)", new String[]{idToma.trim()});
        } catch (Exception e) {
            Log.e(TAG, "Error marcando header como pendiente para idToma=" + idToma, e);
        } finally {
            // db.close();
        }
    }

    private Integer getHeaderSyncStatus(String idToma) {
        if (idToma == null || idToma.trim().isEmpty()) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT SYNC_STATUS FROM TomasFisicasResumen WHERE LOWER(IdToma)=LOWER(?) LIMIT 1",
                new String[]{idToma.trim()})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo SYNC_STATUS del header para idToma=" + idToma, e);
            return null;
        } finally {
            db.close();
        }
    }

    private static void inc(Map<String, Integer> map, String key) {
        if (map == null || key == null) return;
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.trim();
    }

    private static String formatActivo(ActivoEntity a) {
        if (a == null) return "(no_resuelto)";
        String numeroActivo = a.getNumeroActivo();
        String numeroEtiqueta = a.getNumeroEtiqueta();
        String serie = a.getNumeroSerie();
        String nombre = a.getDescripcionCorta();

        String left;
        if (numeroActivo != null && !numeroActivo.trim().isEmpty()) {
            left = numeroActivo.trim();
        } else if (numeroEtiqueta != null && !numeroEtiqueta.trim().isEmpty()) {
            left = numeroEtiqueta.trim();
        } else {
            left = safe(a.getIdActivo());
        }

        StringBuilder sb = new StringBuilder(left);
        if (serie != null && !serie.trim().isEmpty()) {
            sb.append("/").append(serie.trim());
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            sb.append(" - ").append(nombre.trim());
        }
        return sb.toString();
    }

    private void pushPendingDeletesToApi(final Runnable onComplete) {
        List<String> ids = getPendingDeleteRefIds("TFDetalleByToma");
        if (ids == null || ids.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        Log.d(TAG, "Iniciando envío de eliminaciones de detalle. Cantidad: " + ids.size() + ". IDs: " + ids.toString());

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();
        api.<JsonObject>post("TomasFisicas/TFDetalleDelete", ids, type, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                if (response.success && serverSuccess(response.data)) {
                    removePendingDeletes("TFDetalleByToma", ids);
                    Log.d(TAG, "Eliminaciones de detalle enviadas exitosamente (" + ids.size() + ")");
                } else {
                    String msg = response.success ? serverMessage(response.data) : response.errorMessage;
                    Log.e(TAG, "Error enviando eliminaciones de detalle: " + msg + ". Status Code: " + response.statusCode);
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void insertarPendingDelete(SQLiteDatabase db, String entityType, String refId) {
        if (entityType == null || entityType.trim().isEmpty()) return;
        if (refId == null || refId.trim().isEmpty()) return;

        ContentValues v = new ContentValues();
        String key = entityType.trim() + "|" + refId.trim();
        v.put("Id", UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString());
        v.put("EntityType", entityType.trim());
        v.put("RefId", refId.trim());
        v.put("CreatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insertWithOnConflict("PendingDeletes", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private List<String> getPendingDeleteRefIds(String entityType) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT RefId FROM PendingDeletes WHERE LOWER(EntityType)=LOWER(?)";
        try (Cursor c = db.rawQuery(sql, new String[]{entityType})) {
            while (c.moveToNext()) {
                String refId = c.getString(0);
                if (refId != null && !refId.trim().isEmpty()) list.add(refId.trim());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo PendingDeletes", e);
        } finally {
            // db.close();
        }
        return list;
    }

    private void removePendingDeletes(String entityType, List<String> refIds) {
        if (refIds == null || refIds.isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String refId : refIds) {
                if (refId == null || refId.trim().isEmpty()) continue;
                db.delete("PendingDeletes", "LOWER(EntityType)=LOWER(?) AND LOWER(RefId)=LOWER(?)", new String[]{entityType, refId.trim()});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error eliminando PendingDeletes", e);
        } finally {
            db.endTransaction();
            // db.close();
        }
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (String s : list) {
            if (s != null && s.equalsIgnoreCase(value)) return true;
        }
        return false;
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
