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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import android.net.Uri;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.charset.StandardCharsets;

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
        // Default SYNC_STATUS is 0 (Pending) if not explicitly set via logic, but helper handles default.
        // However, if we are updating, we might want to set it to 0.
        // For simple entityToValues, we leave it out or handle it in specific methods.
        return v;
    }

    public long insert(TomaFisicaTomasEntity entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = entityToValues(entity);
        v.put("SYNC_STATUS", 0); // New/Updated record is pending
        return db.insertWithOnConflict("TomasFisicasResumen", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void syncResumen(List<TomaFisicaTomasEntity> data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaTomasEntity a : data) {
                ContentValues values = entityToValues(a);
                values.put("SYNC_STATUS", 1); // From API -> Synced
                Log.d(TAG, "VALUES → " + values.toString());
                db.insertWithOnConflict("TomasFisicasResumen", null, values,
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

    /**
     * Guarda cambios locales (nuevas subtomas o actualizaciones) marcándolos como pendientes de envío.
     */
    public void saveLocal(List<TomaFisicaTomasEntity> data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TomaFisicaTomasEntity a : data) {
                ContentValues values = entityToValues(a);
                values.put("SYNC_STATUS", 0); // Local change -> Pending
                Log.d(TAG, "SAVING LOCAL RESUMEN (Pending) → " + values.toString());
                db.insertWithOnConflict("TomasFisicasResumen", null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando resumen local", e);
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

        Log.d(TAG, "fetchAndSyncFromApi: Solicitando " + endpoint);

        api.<List<TomaFisicaTomasEntity>>get(endpoint, type, new ApiCallback<List<TomaFisicaTomasEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaTomasEntity>> response) {
                Log.d(TAG, "fetchAndSyncFromApi onComplete: Success=" + response.success + ", StatusCode=" + response.statusCode + ", Message=" + response.errorMessage);
                if (response.success && response.data != null) {
                    if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty() && response.data.isEmpty()) {
                        Log.w(TAG, "TFResumen filtro devolvió 0. Reintentando sin filtro y filtrando en cliente. tomaFisicaId=" + tomaFisicaId.trim());
                        fetchAllThenFilter(tomaFisicaId, onComplete);
                        return;
                    }
                    List<String> deletedIds = getPendingDeleteRefIds("TFResumen");
                    if (deletedIds != null && !deletedIds.isEmpty()) {
                        List<TomaFisicaTomasEntity> filtered = new ArrayList<>();
                        for (TomaFisicaTomasEntity item : response.data) {
                            if (item == null) continue;
                            String idToma = item.getIdToma();
                            if (idToma != null && containsIgnoreCase(deletedIds, idToma.trim())) {
                                continue;
                            }
                            filtered.add(item);
                        }
                        syncResumen(filtered);
                        
                        // Limpieza de huérfanos: Eliminar registros locales que no vinieron del servidor
                        // AHORA: Soportar tanto limpieza por Toma específica como GLOBAL
                        
                        List<TomaFisicaTomasEntity> localItems;
                        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
                             localItems = getByTomaFisicaId(tomaFisicaId);
                        } else {
                             // Si es null, obtenemos TODOS los locales para validar contra la lista completa del servidor
                             localItems = getAll();
                        }

                        List<String> remoteIds = new ArrayList<>();
                        for(TomaFisicaTomasEntity rem : filtered) {
                             if(rem.getIdToma() != null) remoteIds.add(rem.getIdToma().trim().toLowerCase());
                        }
                             
                        for(TomaFisicaTomasEntity loc : localItems) {
                             // Solo eliminar si NO está pendiente de sincronizar (SYNC_STATUS != 0)
                             // Si SYNC_STATUS es 0, es una toma local nueva que aún no está en el servidor, no debemos borrarla.
                             // Nota: getByTomaFisicaId/getAll no devuelven SYNC_STATUS por defecto en el objeto Entity simple
                             // Necesitamos verificarlo.
                             // Opción A: Cargar SYNC_STATUS en la entidad.
                             // Opción B: Verificar el estado antes de borrar.
                             
                             if(loc.getIdToma() != null && !remoteIds.contains(loc.getIdToma().trim().toLowerCase())) {
                                 if (getPendienteStatus(loc.getIdToma()) == 0) { // 0 = Pending
                                     Log.d(TAG, "Protegiendo huérfano local pendiente: " + loc.getIdToma());
                                     continue;
                                 }
                                 Log.d(TAG, "Eliminando huérfano local sincronizado: " + loc.getIdToma());
                                 deleteToma(loc.getIdToma());
                             }
                        }

                    } else {
                        syncResumen(response.data);
                        
                        // Limpieza de huérfanos (Duplicada lógica para caso sin deletes pendientes)
                        List<TomaFisicaTomasEntity> localItems;
                        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
                             localItems = getByTomaFisicaId(tomaFisicaId);
                        } else {
                             localItems = getAll();
                        }

                        List<String> remoteIds = new ArrayList<>();
                        for(TomaFisicaTomasEntity rem : response.data) {
                             if(rem.getIdToma() != null) remoteIds.add(rem.getIdToma().trim().toLowerCase());
                        }
                             
                        for(TomaFisicaTomasEntity loc : localItems) {
                             if(loc.getIdToma() != null && !remoteIds.contains(loc.getIdToma().trim().toLowerCase())) {
                                 if (getPendienteStatus(loc.getIdToma()) == 0) {
                                     Log.d(TAG, "Protegiendo huérfano local pendiente: " + loc.getIdToma());
                                     continue;
                                 }
                                 Log.d(TAG, "Eliminando huérfano local sincronizado: " + loc.getIdToma());
                                 deleteToma(loc.getIdToma());
                             }
                        }
                    }
                    Log.d(TAG, "Tomas Fisicas sincronizadas desde API: " + response.data.size());
                } else {
                    if (response.statusCode == 404) {
                        Log.w(TAG, "API devolvió 404 para TFResumen. Posiblemente sin datos o ID no encontrado. Se ignora error.");
                    } else {
                        Log.e(TAG, "Error al sincronizar tomas fisicas desde API: " + response.errorMessage);
                    }
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void fetchAllThenFilter(String tomaFisicaId, final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<TomaFisicaTomasEntity>>() {}.getType();

        api.<List<TomaFisicaTomasEntity>>get("TomasFisicas/TFResumen", type, new ApiCallback<List<TomaFisicaTomasEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<TomaFisicaTomasEntity>> response) {
                if (response.success && response.data != null) {
                    List<TomaFisicaTomasEntity> filtered = new ArrayList<>();
                    for (TomaFisicaTomasEntity item : response.data) {
                        if (item == null) continue;
                        String tfId = item.getTomaFisicaId();
                        if (tfId != null && tfId.trim().equalsIgnoreCase(tomaFisicaId.trim())) {
                            filtered.add(item);
                        }
                    }
                    syncResumen(filtered);
                    Log.d(TAG, "TFResumen filtrado en cliente: " + filtered.size() + " de " + response.data.size() + " para tomaFisicaId=" + tomaFisicaId.trim());
                } else {
                    Log.e(TAG, "Error al sincronizar TFResumen sin filtro: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
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

    public int getPendienteStatus(String idToma) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int status = 1; // Default to Synced (so we delete if not found, unless explicitly 0)
        try (Cursor c = db.rawQuery("SELECT SYNC_STATUS FROM TomasFisicasResumen WHERE LOWER(IdToma) = LOWER(?)", new String[]{idToma})) {
            if (c.moveToFirst()) {
                status = c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking sync status", e);
        }
        return status;
    }

    public void deleteToma(String idToma) {
        if (idToma == null || idToma.trim().isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String trimmed = idToma.trim();
        db.beginTransaction();
        try {
            insertarPendingDelete(db, "TFResumen", trimmed);
            int rows = db.delete("TomasFisicasResumen", "LOWER(IdToma) = LOWER(?)", new String[]{trimmed});
            Log.d(TAG, "Toma eliminada local: " + trimmed + " Rows affected: " + rows);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting toma " + trimmed, e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public int getPendientesCount(String tomaFisicaId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM TomasFisicasResumen WHERE LOWER(TomaFisicaId) = LOWER(?)", new String[]{tomaFisicaId})) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting pendientes", e);
        } finally {
            db.close();
        }
        return count;
    }

    public void pushSubtoma(TomaFisicaTomasEntity entity, ApiCallback<JsonObject> callback) {
        Log.d(TAG, "pushSubtoma: Enviando subtoma: " + entity.getIdToma() + " Num: " + entity.getNumeroToma() + " TF: " + entity.getTomaFisicaId());
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();

        api.<JsonObject>post("TomasFisicas/TFResumen", entity, type, callback);
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

    public List<TomaFisicaTomasEntity> getPendingResumen() {
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Check if SYNC_STATUS column exists first? AppDatabaseHelper ensures it.
        try (Cursor c = db.rawQuery("SELECT * FROM TomasFisicasResumen WHERE SYNC_STATUS = 0", null)) {
            if (c.moveToFirst()) {
                do {
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
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching pending resumen", e);
        }
        return list;
    }

    public void markAsSynced(String idToma) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put("SYNC_STATUS", 1);
            db.update("TomasFisicasResumen", cv, "IdToma = ?", new String[]{idToma});
        } catch (Exception e) {
            Log.e(TAG, "Error marking resumen as synced", e);
        } finally {
            db.close();
        }
    }

    public void pushLocalChangesToApi(final Runnable onAllFinished) {
        List<TomaFisicaTomasEntity> localData = getPendingResumen();
        Runnable pushDeletesThenFinish = () -> pushPendingDeletesToApi(onAllFinished);

        if (localData.isEmpty()) {
            Log.d(TAG, "No hay resumenes pendientes para enviar al servidor");
            pushDeletesThenFinish.run();
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();

        final int total = localData.size();
        final int[] completed = {0};

        for (TomaFisicaTomasEntity item : localData) {
            api.<JsonObject>post("TomasFisicas/TFResumen", item, type, new ApiCallback<JsonObject>() {
                @Override
                public void onComplete(ApiResponse<JsonObject> response) {
                    if (response.success) {
                        boolean ok = serverSuccess(response.data);
                        if (ok) {
                            Log.d(TAG, "Resumen enviado exitosamente: " + item.getIdToma());
                            markAsSynced(item.getIdToma());
                        } else {
                            Log.e(TAG, "Error servidor enviando resumen " + item.getIdToma() + ": " + serverMessage(response.data));
                        }
                    } else {
                        Log.e(TAG, "Error enviando resumen " + item.getIdToma() + ": " + response.errorMessage);
                    }

                    completed[0]++;
                    if (completed[0] == total) {
                        pushDeletesThenFinish.run();
                    }
                }
            });
        }
    }

    private void pushPendingDeletesToApi(final Runnable onComplete) {
        List<String> ids = getPendingDeleteRefIds("TFResumen");
        if (ids == null || ids.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Log.d(TAG, "Iniciando envío de eliminaciones de resumen. Cantidad: " + ids.size() + ". IDs: " + ids.toString());

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonObject>() {}.getType();
        api.<JsonObject>post("TomasFisicas/TFResumenDelete", ids, type, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                if (response.success && serverSuccess(response.data)) {
                    removePendingDeletes("TFResumen", ids);
                    Log.d(TAG, "Eliminaciones de resumen enviadas exitosamente (" + ids.size() + ")");
                } else {
                    String msg = response.success ? serverMessage(response.data) : response.errorMessage;
                    Log.e(TAG, "Error enviando eliminaciones de resumen: " + msg + ". Status Code: " + response.statusCode);
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
            db.close();
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
            db.close();
        }
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (String s : list) {
            if (s != null && s.equalsIgnoreCase(value)) return true;
        }
        return false;
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
