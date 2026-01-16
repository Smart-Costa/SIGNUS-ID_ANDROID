package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.EmpresaEntity;
import com.example.diverscan.activeid.Utilities.Fechas;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivosInventarios;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActivoDao {
    private static final String TAG = "DB_DAO_ACTIVO";
    private static final String TABLE_ACTIVOS = "ActivosApi";
    private final AppDatabaseHelper dbHelper;
    private final Context context;

    // Interface para enviar logs a la UI
    public interface LogListener {
        void onLog(String message);
    }
    private LogListener logListener;

    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    private void logToUI(String msg) {
        if (logListener != null) {
            logListener.onLog(msg);
        }
    }

    public ActivoDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public int getActivosCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ACTIVOS, null)) {
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting activos", e);
        }
        db.close();
        return count;
    }

    private ContentValues entityToContentValues(ActivoEntity a) {
        ContentValues v = new ContentValues();

        v.put("ID_ACTIVO", a.getIdActivo());
        v.put("NUMERO_ACTIVO", a.getNumeroActivo());
        v.put("NUMERO_ETIQUETA", a.getNumeroEtiqueta());
        v.put("DESCRIPCION_CORTA", a.getDescripcionCorta());
        v.put("DESCRIPCION_LARGA", a.getDescripcionLarga());
        v.put("CATEGORIA", a.getCategoria());
        v.put("ESTADO", a.getEstado());
        v.put("EMPRESA", a.getEmpresa());
        v.put("MARCA", a.getMarca());
        v.put("MODELO", a.getModelo());
        v.put("NUMERO_SERIE", a.getNumeroSerie());
        v.put("COSTO", a.getCosto());
        v.put("NUMERO_FACTURA", a.getNumeroFactura());
        v.put("FECHA_COMPRA", a.getFechaCompra());
        v.put("FECHA_CAPITALIZACION", a.getFechaCapitalizacion());
        v.put("VALOR_RESIDUAL", a.getValorResidual());
        v.put("DOCUMENTO", a.getDocumento());
        v.put("FOTOS", a.getFotos());
        v.put("NUMERO_PARTE_FABRICANTE", a.getNumeroParteFabricante());
        v.put("DEPRECIADO", a.getDepreciado());
        v.put("DESCRIPCION_DEPRECIADO", a.getDescripcionDepreciado());
        v.put("ANOS_VIDA_UTIL", a.getAnosVidaUtil());
        v.put("CUENTA_CONTABLE_DEPRESIACION", a.getCuentaContableDepresiacion());
        v.put("CENTRO_COSTOS", a.getCentroCostos());
        v.put("DESCRIPCION_ESTADO_ULTIMO_INVENTARIO", a.getDescripcionEstadoUltimoInventario());
        // v.put("TAG_EPC", "EPC Asignado"); // REMOVED: Do not write garbage value
        v.put("EMPLEADO", a.getEmpleado());
        v.put("UBICACION_A", a.getUbicacionA());
        v.put("UBICACION_B", a.getUbicacionB());
        v.put("UBICACION_C", a.getUbicacionC());
        v.put("UBICACION_D", a.getUbicacionD());
        v.put("UBICACION_SECUNDARIA", a.getUbicacionSecundaria());
        v.put("FECHA_GARANTIA", a.getFechaGarantia());
        v.put("COLOR", a.getColor());
        v.put("TAMANIO_MEDIDA", a.getTamanioMedida());
        v.put("OBSERVACIONES", a.getObservaciones());
        v.put("ESTADO_ACTIVO", (a.getEstadoActivo() != null && a.getEstadoActivo()) ? 1 : 0);
        v.put("FECHA_CREACION_ACTIVO", a.getFechaCreacionActivo());

        // Logica robusta para obtener el EPC real
        String realEpc = a.getEpc();
        if (realEpc == null || realEpc.trim().isEmpty() || "EPC Asignado".equalsIgnoreCase(realEpc)) {
            realEpc = a.getTagEpc();
        }
        if ("EPC Asignado".equalsIgnoreCase(realEpc)) {
            realEpc = null;
        }
        
        v.put("EPC", realEpc); // Guardamos el valor REAL del tag en la columna EPC
        v.put("TAG_EPC", realEpc); // Also update legacy column with REAL value

        v.put("CATEGORIA_A", a.getCategoriaA());
        v.put("CATEGORIA_B", a.getCategoriaB());
        v.put("CATEGORIA_C", a.getCategoriaC());
        v.put("UBICACION_LOGICA_A", a.getUbicacionLogicaA());
        v.put("UBICACION_LOGICA_B", a.getUbicacionLogicaB());
        v.put("UBICACION_LOGICA_C", a.getUbicacionLogicaC());
        v.put("ENTIDAD_ASOCIADA", a.getEntidadAsociada());
        v.put("COSTO_DEPRECIACION", a.getCostoDepreciacion());

        return v;
    }

    public long insertActivo(ActivoEntity activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = entityToContentValues(activo);
        values.put("SYNC_STATUS", 0); // 0 = Pendiente de sincronizar

        long result = db.insertWithOnConflict(TABLE_ACTIVOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result;
    }

    public void syncActivos(List<ActivoEntity> activos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        int count = 0;
        try {
            for (ActivoEntity a : activos) {
                ContentValues values = entityToContentValues(a);
                values.put("SYNC_STATUS", 1); // 1 = Sincronizado
                db.insertWithOnConflict(TABLE_ACTIVOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                count++;
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Resumen de activos sincronizados: " + count);
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando activos", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void pushLocalChangesToApi(final ApiCallback<JsonElement> callback) {
        final List<ActivoEntity> pending = getPendingActivos();
        if (pending.isEmpty()) {
            Log.d(TAG, "No hay activos pendientes de sincronizar.");
            if (callback != null) callback.onComplete(ApiResponse.success(null, 200));
            return;
        }

        ApiClient api = ApiClient.getInstance(context);

        JsonArray jsonArray = new JsonArray();
        for (ActivoEntity a : pending) {
            JsonObject o = new JsonObject();
            // Mapeo manual para asegurar nombres de campos y validar GUIDs
            o.addProperty("ID_ACTIVO", validateGuid(a.getIdActivo()));
            
            // Enviamos como tipos nativos para coincidir con el modelo Activos.cs del servidor (int, long, decimal)
            if (a.getNumeroActivo() != null) {
                 try {
                     o.addProperty("NUMERO_ACTIVO", Long.parseLong(a.getNumeroActivo()));
                 } catch (Exception e) {
                     o.addProperty("NUMERO_ACTIVO", a.getNumeroActivo()); // Fallback a string si no es numero
                 }
            } else {
                o.add("NUMERO_ACTIVO", com.google.gson.JsonNull.INSTANCE);
            }

            o.addProperty("NUMERO_ETIQUETA", a.getNumeroEtiqueta());
            o.addProperty("DESCRIPCION_CORTA", a.getDescripcionCorta());
            o.addProperty("DESCRIPCION_LARGA", a.getDescripcionLarga());
            o.addProperty("CATEGORIA", validateGuid(a.getCategoria()));
            o.addProperty("ESTADO", validateGuid(a.getEstado()));
            o.addProperty("EMPRESA", validateGuid(a.getEmpresa()));
            o.addProperty("MARCA", validateGuid(a.getMarca()));
            o.addProperty("MODELO", validateGuid(a.getModelo()));
            o.addProperty("NUMERO_SERIE", a.getNumeroSerie());
            o.addProperty("COSTO", a.getCosto());
            o.addProperty("NUMERO_FACTURA", a.getNumeroFactura());
            o.addProperty("FECHA_COMPRA", validateDate(a.getFechaCompra()));
            o.addProperty("FECHA_CAPITALIZACION", validateDate(a.getFechaCapitalizacion()));
            o.addProperty("VALOR_RESIDUAL", a.getValorResidual());
            o.addProperty("DOCUMENTO", a.getDocumento());
            o.addProperty("FOTOS", a.getFotos());
            o.addProperty("NUMERO_PARTE_FABRICANTE", a.getNumeroParteFabricante());
            o.addProperty("DEPRECIADO", a.getDepreciado());
            o.addProperty("DESCRIPCION_DEPRECIADO", a.getDescripcionDepreciado());
            o.addProperty("ANOS_VIDA_UTIL", a.getAnosVidaUtil());
            o.addProperty("CUENTA_CONTABLE_DEPRESIACION", a.getCuentaContableDepresiacion());
            o.addProperty("CENTRO_COSTOS", a.getCentroCostos());
            o.addProperty("DESCRIPCION_ESTADO_ULTIMO_INVENTARIO", a.getDescripcionEstadoUltimoInventario());
            
            // Fix: Send real EPC, not "EPC Asignado"
            String realEpc = a.getEpc();
            if (realEpc == null || realEpc.isEmpty() || "EPC Asignado".equalsIgnoreCase(realEpc)) {
                 realEpc = a.getTagEpc();
            }
            if ("EPC Asignado".equalsIgnoreCase(realEpc)) realEpc = null;
            
            o.addProperty("TAG_EPC", realEpc);
            o.addProperty("EPC", realEpc); // Also send as EPC just in case
            
            o.addProperty("EMPLEADO", validateGuid(a.getEmpleado()));
            o.addProperty("UBICACION_A", validateGuid(a.getUbicacionA()));
            o.addProperty("UBICACION_B", validateGuid(a.getUbicacionB()));
            o.addProperty("UBICACION_C", validateGuid(a.getUbicacionC()));
            o.addProperty("UBICACION_D", validateGuid(a.getUbicacionD()));
            o.addProperty("COLOR", a.getColor());
            o.addProperty("TAMANIO_MEDIDA", a.getTamanioMedida());
            o.addProperty("OBSERVACIONES", a.getObservaciones());
            // El modelo API define ESTADO_ACTIVO como bool (ver Activos.cs en API), enviamos boolean nativo
            o.addProperty("ESTADO_ACTIVO", (a.getEstadoActivo() != null && a.getEstadoActivo()));
            o.addProperty("FECHA_CREACION_ACTIVO", validateDate(a.getFechaCreacionActivo()));

            jsonArray.add(o);
        }

        Type type = new TypeToken<JsonElement>() {}.getType();

        String jsonLog = jsonArray.toString();
        Log.d(TAG, "PAYLOAD_SYNC_BATCH: " + jsonLog);
        Log.d(TAG, "Enviando SyncBatch payload (pending_count=" + pending.size() + ", size_bytes=" + jsonLog.length() + ")");

        api.post("Activos/SyncBatch", jsonArray, type, new ApiCallback<JsonElement>() {
            @Override
            public void onComplete(ApiResponse<JsonElement> response) {
                if (response.success) {
                    Log.d(TAG, "Activos sincronizados correctamente: " + pending.size());
                    markAsSynced(pending);
                } else {
                    Log.e(TAG, "Error al sincronizar activos: " + response.errorMessage + " Code: " + response.statusCode);
                }
                if (callback != null) callback.onComplete(response);
            }
        });
    }

    private String validateGuid(String guid) {
        if (guid == null || guid.trim().isEmpty()) {
            return null;
        }
        // Tratar GUID vacío como null para evitar errores de llave foránea
        if (guid.equals("00000000-0000-0000-0000-000000000000")) {
            return null;
        }
        try {
            java.util.UUID.fromString(guid);
            return guid;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Number validateLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            Double d = Double.parseDouble(value);
            return d.longValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String validateDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            // Convert dd-MM-yyyy to yyyy-MM-dd if necessary
            if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String[] parts = date.split("-");
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
            // Check if already ISO
            if (date.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return date;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }


    public java.util.Map<String, Integer> getPendingSummary() {
        java.util.Map<String, Integer> summary = new java.util.HashMap<>();
        int creados = 0;
        int bajas = 0;
        List<ActivoEntity> pending = getPendingActivos();
        
        for (ActivoEntity a : pending) {
            // Asumimos que si ESTADO_ACTIVO es false, es una baja. Si es true, es creado/modificado.
            if (a.getEstadoActivo() != null && !a.getEstadoActivo()) {
                bajas++;
            } else {
                creados++;
            }
        }
        summary.put("creados", creados);
        summary.put("bajas", bajas);
        summary.put("total", pending.size());
        return summary;
    }

    private List<ActivoEntity> getPendingActivos() {
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // SYNC_STATUS = 0
        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ACTIVOS + " WHERE SYNC_STATUS = 0", null)) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToEntity(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo activos pendientes", e);
        }
        return list;
    }

    private void markAsSynced(List<ActivoEntity> activos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("SYNC_STATUS", 1);
            for (ActivoEntity a : activos) {
                db.update(TABLE_ACTIVOS, cv, "ID_ACTIVO = ?", new String[]{a.getIdActivo()});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error marcando activos como sincronizados", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private ActivoEntity cursorToEntity(Cursor c) {
        ActivoEntity a = new ActivoEntity();
        a.setIdActivo(c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")));
        a.setNumeroActivo(c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")));
        a.setNumeroEtiqueta(c.getString(c.getColumnIndexOrThrow("NUMERO_ETIQUETA")));
        a.setDescripcionCorta(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")));
        a.setDescripcionLarga(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_LARGA")));
        a.setCategoria(c.getString(c.getColumnIndexOrThrow("CATEGORIA")));
        a.setEstado(c.getString(c.getColumnIndexOrThrow("ESTADO")));
        a.setEmpresa(c.getString(c.getColumnIndexOrThrow("EMPRESA")));
        a.setMarca(c.getString(c.getColumnIndexOrThrow("MARCA")));
        a.setModelo(c.getString(c.getColumnIndexOrThrow("MODELO")));
        a.setNumeroSerie(c.getString(c.getColumnIndexOrThrow("NUMERO_SERIE")));
        a.setCosto(c.getDouble(c.getColumnIndexOrThrow("COSTO")));
        a.setNumeroFactura(c.getString(c.getColumnIndexOrThrow("NUMERO_FACTURA")));
        a.setFechaCompra(c.getString(c.getColumnIndexOrThrow("FECHA_COMPRA")));
        a.setFechaCapitalizacion(c.getString(c.getColumnIndexOrThrow("FECHA_CAPITALIZACION")));
        a.setValorResidual(c.getDouble(c.getColumnIndexOrThrow("VALOR_RESIDUAL")));
        a.setDocumento(c.getString(c.getColumnIndexOrThrow("DOCUMENTO")));
        a.setFotos(c.getString(c.getColumnIndexOrThrow("FOTOS")));
        a.setNumeroParteFabricante(c.getString(c.getColumnIndexOrThrow("NUMERO_PARTE_FABRICANTE")));
        a.setDepreciado(c.getString(c.getColumnIndexOrThrow("DEPRECIADO")));
        a.setDescripcionDepreciado(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_DEPRECIADO")));
        a.setAnosVidaUtil(c.getInt(c.getColumnIndexOrThrow("ANOS_VIDA_UTIL")));
        a.setCuentaContableDepresiacion(c.getString(c.getColumnIndexOrThrow("CUENTA_CONTABLE_DEPRESIACION")));
        a.setCentroCostos(c.getString(c.getColumnIndexOrThrow("CENTRO_COSTOS")));
        a.setDescripcionEstadoUltimoInventario(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_ESTADO_ULTIMO_INVENTARIO")));
        
        // Fix: Read real EPC, prioritize EPC column, fallback to TAG_EPC but ignore "EPC Asignado"
        String epcVal = "";
        try { 
            epcVal = c.getString(c.getColumnIndexOrThrow("EPC")); 
        } catch (Exception e) { 
            // EPC column might not exist in old DB versions, ignore
        }
        
        if (epcVal == null || epcVal.isEmpty()) {
            epcVal = c.getString(c.getColumnIndexOrThrow("TAG_EPC"));
        }
        
        if ("EPC Asignado".equalsIgnoreCase(epcVal)) {
            epcVal = "";
        }
        
        a.setTagEpc(epcVal);
        a.setEpc(epcVal);

        a.setEmpleado(c.getString(c.getColumnIndexOrThrow("EMPLEADO")));
        a.setUbicacionA(c.getString(c.getColumnIndexOrThrow("UBICACION_A")));
        a.setUbicacionB(c.getString(c.getColumnIndexOrThrow("UBICACION_B")));
        a.setUbicacionC(c.getString(c.getColumnIndexOrThrow("UBICACION_C")));
        a.setUbicacionD(c.getString(c.getColumnIndexOrThrow("UBICACION_D")));
        a.setUbicacionSecundaria(c.getString(c.getColumnIndexOrThrow("UBICACION_SECUNDARIA")));
        a.setFechaGarantia(c.getString(c.getColumnIndexOrThrow("FECHA_GARANTIA")));
        a.setColor(c.getString(c.getColumnIndexOrThrow("COLOR")));
        a.setTamanioMedida(c.getString(c.getColumnIndexOrThrow("TAMANIO_MEDIDA")));
        a.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
        a.setEstadoActivo(c.getInt(c.getColumnIndexOrThrow("ESTADO_ACTIVO")) == 1);
        a.setFechaCreacionActivo(c.getString(c.getColumnIndexOrThrow("FECHA_CREACION_ACTIVO")));
        // Handle optional columns that might not exist in older DB versions if ensureActivosApiTable wasn't fully effective yet
        // a.setEpc set above
        try { a.setCategoriaA(c.getString(c.getColumnIndexOrThrow("CATEGORIA_A"))); } catch (IllegalArgumentException e) {}
        try { a.setCategoriaB(c.getString(c.getColumnIndexOrThrow("CATEGORIA_B"))); } catch (IllegalArgumentException e) {}
        try { a.setCategoriaC(c.getString(c.getColumnIndexOrThrow("CATEGORIA_C"))); } catch (IllegalArgumentException e) {}
        try { a.setUbicacionLogicaA(c.getString(c.getColumnIndexOrThrow("UBICACION_LOGICA_A"))); } catch (IllegalArgumentException e) {}
        try { a.setUbicacionLogicaB(c.getString(c.getColumnIndexOrThrow("UBICACION_LOGICA_B"))); } catch (IllegalArgumentException e) {}
        try { a.setUbicacionLogicaC(c.getString(c.getColumnIndexOrThrow("UBICACION_LOGICA_C"))); } catch (IllegalArgumentException e) {}
        try { a.setEntidadAsociada(c.getString(c.getColumnIndexOrThrow("ENTIDAD_ASOCIADA"))); } catch (IllegalArgumentException e) {}
        try { a.setCostoDepreciacion(c.getDouble(c.getColumnIndexOrThrow("COSTO_DEPRECIACION"))); } catch (IllegalArgumentException e) {}
        
        return a;
    }

    public List<ActivoEntity> getActivosByFiltros(String ua, String ub, String uc, String ud) {
        return getActivosByFiltros(ua, ub, uc, ud, null);
    }

    public List<ActivoEntity> getActivosByFiltros(String ua, String ub, String uc, String ud, String us) {
        // Implementation delegates to a private helper or we build the query here
        // For simplicity, let's build the query.
        // If parameters are null or empty, we ignore them (wildcard behavior).
        // BUT if the user strictly wants "only A", and B is not selected, do we filter B?
        // Usually, if B is not selected, we don't filter by B.
        // However, the caller (NuevaTomaActivity) will pass the selected values.
        
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        StringBuilder selection = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();
        
        if (ua != null && !ua.isEmpty() && !ua.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_A = ?");
            args.add(ua);
        }
        if (ub != null && !ub.isEmpty() && !ub.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_B = ?");
            args.add(ub);
        }
        if (uc != null && !uc.isEmpty() && !uc.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_C = ?");
            args.add(uc);
        }
        if (ud != null && !ud.isEmpty() && !ud.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_D = ?");
            args.add(ud);
        }
        if (us != null && !us.isEmpty() && !us.equals("Todas")) { // Assuming "Todas" is the default "All" value
             // If US is "NULL", we might want to search for IS NULL?
             // Or if it's a specific string.
             selection.append(" AND UBICACION_SECUNDARIA = ?");
             args.add(us);
        }

        Cursor c = null;
        try {
            c = db.query(TABLE_ACTIVOS, null, selection.toString(), args.toArray(new String[0]), null, null, null);
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToEntity(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getActivosByFiltros", e);
        } finally {
            if (c != null) c.close();
            // db.close();
        }
        return list;
    }

    public int countActivosByFiltros(String ua, String ub, String uc, String ud) {
        return countActivosByFiltros(ua, ub, uc, ud, null);
    }

    public int countActivosByFiltros(String ua, String ub, String uc, String ud, String us) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();

        if (ua != null && !ua.isEmpty() && !ua.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_A = ?");
            args.add(ua);
        }
        if (ub != null && !ub.isEmpty() && !ub.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_B = ?");
            args.add(ub);
        }
        if (uc != null && !uc.isEmpty() && !uc.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_C = ?");
            args.add(uc);
        }
        if (ud != null && !ud.isEmpty() && !ud.equals("00000000-0000-0000-0000-000000000000")) {
            selection.append(" AND UBICACION_D = ?");
            args.add(ud);
        }
        if (us != null && !us.isEmpty() && !us.equals("Todas")) {
            selection.append(" AND UBICACION_SECUNDARIA = ?");
            args.add(us);
        }

        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ACTIVOS + " WHERE " + selection,
                args.toArray(new String[0])
            );
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error countActivosByFiltros", e);
            return 0;
        } finally {
            if (c != null) c.close();
            // db.close();
        }
    }

    public List<String> getDistinctUbicacionSecundaria(String ud) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            String selection = "UBICACION_SECUNDARIA IS NOT NULL AND UBICACION_SECUNDARIA != ''";
            List<String> args = new ArrayList<>();
            if (ud != null && !ud.isEmpty() && !ud.equals("00000000-0000-0000-0000-000000000000")) {
                selection += " AND UBICACION_D = ?";
                args.add(ud);
            }
            
            c = db.query(true, TABLE_ACTIVOS, new String[]{"UBICACION_SECUNDARIA"}, selection, args.toArray(new String[0]), null, null, "UBICACION_SECUNDARIA ASC", null);
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getDistinctUbicacionSecundaria", e);
        } finally {
            if (c != null) c.close();
            // db.close();
        }
        return list;
    }

    public List<ActivoEntity> getActivosByUbicacion(String idOficina) {
        // Mantenemos este metodo por compatibilidad, asumiendo que idOficina mapea a UBICACION_D
        return getActivosByFiltros(null, null, null, idOficina);
    }

    public List<ActivoEntity> getActivosByUbicacionColumn(String column, String ubicacionId) {
        if (ubicacionId == null || ubicacionId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String safeColumn;
        if ("UBICACION_A".equalsIgnoreCase(column)) safeColumn = "UBICACION_A";
        else if ("UBICACION_B".equalsIgnoreCase(column)) safeColumn = "UBICACION_B";
        else if ("UBICACION_C".equalsIgnoreCase(column)) safeColumn = "UBICACION_C";
        else if ("UBICACION_D".equalsIgnoreCase(column)) safeColumn = "UBICACION_D";
        else throw new IllegalArgumentException("Invalid column: " + column);

        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_ACTIVOS + " WHERE " + safeColumn + " = ?",
                new String[]{ubicacionId.trim()}
        )) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToEntity(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo activos por columna de ubicacion", e);
        } finally {
            // db.close();
        }
        return list;
    }

    public int countActivosByUbicacionColumn(String column, String ubicacionId) {
        if (ubicacionId == null || ubicacionId.trim().isEmpty()) {
            return 0;
        }

        String safeColumn;
        if ("UBICACION_A".equalsIgnoreCase(column)) safeColumn = "UBICACION_A";
        else if ("UBICACION_B".equalsIgnoreCase(column)) safeColumn = "UBICACION_B";
        else if ("UBICACION_C".equalsIgnoreCase(column)) safeColumn = "UBICACION_C";
        else if ("UBICACION_D".equalsIgnoreCase(column)) safeColumn = "UBICACION_D";
        else throw new IllegalArgumentException("Invalid column: " + column);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ACTIVOS + " WHERE " + safeColumn + " = ?",
                new String[]{ubicacionId.trim()}
        )) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error contando activos por columna de ubicacion", e);
        } finally {
            // db.close();
        }
        return 0;
    }

    public List<ActivoEntity> getActivosByAnyUbicacion(String ubicacionId) {
        if (ubicacionId == null || ubicacionId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String id = ubicacionId.trim();
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ACTIVOS + " WHERE UBICACION_A = ? OR UBICACION_B = ? OR UBICACION_C = ? OR UBICACION_D = ?";
        Log.d(TAG, "getActivosByAnyUbicacion: Executing query: " + query + " with param: " + id);
        try (Cursor c = db.rawQuery(
                query,
                new String[]{id, id, id, id}
        )) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToEntity(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo activos por cualquier ubicacion", e);
        } finally {
            // db.close();
        }
        Log.d(TAG, "getActivosByAnyUbicacion: Found " + list.size() + " records.");
        return list;
    }

    public int countActivosByAnyUbicacion(String ubicacionId) {
        if (ubicacionId == null || ubicacionId.trim().isEmpty()) {
            return 0;
        }

        String id = ubicacionId.trim();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ACTIVOS + " WHERE UBICACION_A = ? OR UBICACION_B = ? OR UBICACION_C = ? OR UBICACION_D = ?";
        Log.d(TAG, "countActivosByAnyUbicacion: Executing query: " + query + " with param: " + id);
        try (Cursor c = db.rawQuery(
                query,
                new String[]{id, id, id, id}
        )) {
            if (c.moveToFirst()) {
                int count = c.getInt(0);
                Log.d(TAG, "countActivosByAnyUbicacion: Count result=" + count);
                return count;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error contando activos por cualquier ubicacion", e);
        } finally {
            // db.close();
        }
        return 0;
    }

    public List<ActivoEntity> getAllLocalActivos() {
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ACTIVOS, null)) {
            if (c.moveToFirst()) {
                do {
                    ActivoEntity a = cursorToEntity(c);
                    list.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo activos locales", e);
        } finally {
            // db.close();
        }

        return list;
    }

    public ActivoEntity getActivoByEpc(String epc) {
        String msg1 = "getActivoByEpc: Buscando EPC='" + epc + "'";
        Log.d(TAG, msg1);
        logToUI(msg1);

        ActivoEntity activo = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Debug: check if any row exists with this EPC
        try (Cursor debugC = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ACTIVOS + " WHERE EPC = ?", new String[]{epc})) {
             if (debugC.moveToFirst()) {
                 String msg = "getActivoByEpc: Coincidencias exactas encontradas en columna EPC: " + debugC.getInt(0);
                 Log.d(TAG, msg);
                 logToUI(msg);
             }
        } catch (Exception e) { Log.e(TAG, "Error debug count EPC", e); }

        // Debug: check if it exists in TAG_EPC just in case
        try (Cursor debugC2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ACTIVOS + " WHERE TAG_EPC = ?", new String[]{epc})) {
             if (debugC2.moveToFirst()) {
                 String msg = "getActivoByEpc: Coincidencias encontradas en columna TAG_EPC: " + debugC2.getInt(0);
                 Log.d(TAG, msg);
                 logToUI(msg);
             }
        } catch (Exception e) { Log.e(TAG, "Error debug count TAG_EPC", e); }

        // CORREGIDO: Buscar por columna EPC en lugar de TAG_EPC
        String selection = "EPC = ?"; 
        
        try (Cursor c = db.query(
                TABLE_ACTIVOS,
                null,
                selection,
                new String[]{epc},
                null,
                null,
                null,
                "1"
        )) {
            if (c.moveToFirst()) {
                activo = cursorToEntity(c);
                String msg = "getActivoByEpc: ACTIVO ENCONTRADO -> " + activo.getDescripcionCorta() + " ID: " + activo.getIdActivo();
                Log.d(TAG, msg);
                logToUI(msg);
            } else {
                String msg = "getActivoByEpc: ACTIVO NO ENCONTRADO para EPC: " + epc;
                Log.d(TAG, msg);
                logToUI(msg);
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por EPC", e);
            logToUI("Error consultando por EPC: " + e.getMessage());
        } finally {
            // db.close();
        }

        return activo;
    }

    public ActivoEntity getActivoById(String id) {
        return getActivoById(id, true);
    }

    public ActivoEntity getActivoById(String id, boolean incluirBajas) {
        ActivoEntity activo = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = "NUMERO_ACTIVO = ?";
        if (!incluirBajas) {
            selection += " AND ESTADO_ACTIVO = 1";
        }

        try (Cursor c = db.query(
                TABLE_ACTIVOS,
                null,
                selection,
                new String[]{id},
                null,
                null,
                null,
                "1"
        )) {
            if (c.moveToFirst()) {
                activo = cursorToEntity(c);
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por ID", e);
        } finally {
            // db.close();
        }

        return activo;
    }

    public ActivoEntity getActivoByIdActivo(String idActivo) {
        ActivoEntity activo = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_ACTIVOS + " WHERE ID_ACTIVO = ? LIMIT 1",
                new String[]{idActivo}
        )) {
            if (c.moveToFirst()) {
                activo = new ActivoEntity();

                activo.setIdActivo(c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")));
                activo.setNumeroActivo(c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")));
                activo.setNumeroEtiqueta(c.getString(c.getColumnIndexOrThrow("NUMERO_ETIQUETA")));
                activo.setDescripcionCorta(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")));
                activo.setNumeroSerie(c.getString(c.getColumnIndexOrThrow("NUMERO_SERIE")));
                
                // Fix: Read real EPC
                String epcVal = "";
                try { epcVal = c.getString(c.getColumnIndexOrThrow("EPC")); } catch(Exception e) {}
                if (epcVal == null || epcVal.isEmpty()) epcVal = c.getString(c.getColumnIndexOrThrow("TAG_EPC"));
                if ("EPC Asignado".equalsIgnoreCase(epcVal)) epcVal = "";
                
                activo.setTagEpc(epcVal);
                activo.setEpc(epcVal);
                
                activo.setFotos(c.getString(c.getColumnIndexOrThrow("FOTOS")));
                activo.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por ID_ACTIVO", e);
        } finally {
            // db.close();
        }

        return activo;
    }

    public EntidadActivosInventarios getActivoInventarioByEpc(String epc) {
        Log.d(TAG, "getActivoInventarioByEpc: Buscando EPC='" + epc + "'");
        EntidadActivosInventarios result = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT a.*, o.Nombre as NombreOficina FROM " + TABLE_ACTIVOS + " a " +
                       "LEFT JOIN Oficina o ON a.UBICACION_D = o._id " +
                       "WHERE a.EPC = ?";
                       
        try (Cursor c = db.rawQuery(query, new String[]{epc})) {
            if (c.moveToFirst()) {
                 result = new EntidadActivosInventarios(
                    c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")),
                    c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")),
                    c.getString(c.getColumnIndexOrThrow("EPC")),
                    c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")),
                    c.getString(c.getColumnIndexOrThrow("NombreOficina")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_D")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_C")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_B")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_A")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_SECUNDARIA"))
                 );
                 Log.d(TAG, "getActivoInventarioByEpc: ENCONTRADO -> " + result.getDescripcion());
            } else {
                Log.d(TAG, "getActivoInventarioByEpc: NO ENCONTRADO");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching ActivoInventario", e);
        } finally {
            // db.close();
        }
        return result;
    }

    public EntidadActivosInventarios getActivoInventarioByBarcode(String barcode) {
        EntidadActivosInventarios result = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT a.*, o.Nombre as NombreOficina FROM " + TABLE_ACTIVOS + " a " +
                       "LEFT JOIN Oficina o ON a.UBICACION_D = o._id " +
                       "WHERE a.NUMERO_ACTIVO = ?";
                       
        try (Cursor c = db.rawQuery(query, new String[]{barcode})) {
            if (c.moveToFirst()) {
                 result = new EntidadActivosInventarios(
                    c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")),
                    c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")),
                    c.getString(c.getColumnIndexOrThrow("EPC")), // Usar EPC real en lugar de TAG_EPC
                    c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")),
                    c.getString(c.getColumnIndexOrThrow("NombreOficina")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_D")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_C")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_B")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_A")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_SECUNDARIA"))
                 );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching ActivoInventario by barcode", e);
        } finally {
            db.close();
        }
        return result;
    }

    public int updateEstadoActivo(ActivoEntity activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            boolean estadoActivo = activo.getEstadoActivo() != null && activo.getEstadoActivo();
            values.put("ESTADO_ACTIVO", estadoActivo ? 1 : 0);
            values.put("SYNC_STATUS", 0); // Mark as pending sync

            return db.update(
                    TABLE_ACTIVOS,
                    values,
                    "ID_ACTIVO = ?",
                    new String[]{activo.getIdActivo()}
            );
        } finally {
            db.close();
        }
    }

    public void clearSyncedData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deleted = db.delete(TABLE_ACTIVOS, "SYNC_STATUS = 1", null);
            Log.d(TAG, "Limpieza de datos sincronizados: " + deleted + " registros eliminados.");
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando datos sincronizados", e);
        } finally {
            db.close();
        }
    }

    // public void fetchEmpresas(final ApiCallback<List<EmpresaEntity>> callback) {
    //     ApiClient api = ApiClient.getInstance(context);
    //     Type type = new TypeToken<List<EmpresaEntity>>() {}.getType();
    //     api.get("ActivosDetail/Empresas", type, callback);
    // }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        // Start pagination with page 1 and size 5000
        fetchPage(1, 5000, onComplete);
    }

    private void fetchPage(int page, int pageSize, final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<ActivoEntity>>() {}.getType();
        
        // Filter by specific company to avoid fetching unrelated data (and millions of records)
        // Dynamic company ID passed from UI/Logic
        String endpoint = "Activos?page=" + page + "&pageSize=" + pageSize;
        Log.d(TAG, "Requesting Activos page " + page + " (size=" + pageSize + ")");

        api.<List<ActivoEntity>>get(endpoint, type, new ApiCallback<List<ActivoEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<ActivoEntity>> response) {
                if (response.success && response.data != null) {
                    int count = response.data.size();
                    if (count > 0) {
                        syncActivos(response.data);
                        Log.d(TAG, "Page " + page + " synced: " + count + " assets.");
                        
                        // Fetch next page recursively
                        fetchPage(page + 1, pageSize, onComplete);
                    } else {
                        // Empty page means we are done
                        Log.d(TAG, "Finished syncing all pages.");
                        if (onComplete != null) onComplete.run();
                    }
                } else {
                    Log.e(TAG, "Error syncing page " + page + ": " + response.errorMessage);
                    // Stop on error, but notify completion
                    if (onComplete != null) onComplete.run();
                }
            }
        });
    }

    public void fetchAndSyncActivosPorEstado(String idToma, String estado, final ApiCallback<List<ActivoEntity>> callback) {
        ApiClient api = ApiClient.getInstance(context);
        Type listType = new TypeToken<List<ActivoEntity>>() {}.getType();

        String endpoint = "TomasFisicas/ActivosPorEstado?idToma=" + Uri.encode(idToma != null ? idToma.trim() : "")
                + "&estado=" + Uri.encode(estado != null ? estado.trim() : "");

        api.<JsonElement>get(endpoint, JsonElement.class, new ApiCallback<JsonElement>() {
            @Override
            public void onComplete(ApiResponse<JsonElement> response) {
                if (callback == null) return;

                if (!response.success || response.data == null) {
                    Log.e(TAG, "Error al sincronizar activos por estado desde API: " + response.errorMessage);
                    callback.onComplete(ApiResponse.failure(response.errorMessage, response.statusCode));
                    return;
                }

                try {
                    JsonArray arr = null;
                    JsonElement root = response.data;
                    
                    Log.d(TAG, "ActivosPorEstado Response: " + (root != null ? root.toString() : "null"));

                    if (root.isJsonArray()) {
                        arr = root.getAsJsonArray();
                    } else if (root.isJsonObject()) {
                        JsonObject o = root.getAsJsonObject();
                        JsonElement d = o.has("Data") ? o.get("Data") : (o.has("data") ? o.get("data") : null);
                        if (d != null && d.isJsonArray()) arr = d.getAsJsonArray();
                    }

                    if (arr == null) {
                        // Si es null o vacio, asumimos lista vacia si el success es true
                        if (response.success && root != null && root.isJsonArray() && root.getAsJsonArray().size() == 0) {
                             arr = root.getAsJsonArray();
                        } else {
                            // Intentar parsear como array vacio si es null
                            Log.w(TAG, "Respuesta no es array, asumiendo vacio o error: " + root);
                        }
                    }

                    List<ActivoEntity> list = new ArrayList<>();
                    if (arr != null) {
                        list = api.getGson().fromJson(arr, listType);
                    }
                    
                    if (list != null) {
                        syncActivos(list);
                        Log.d(TAG, "Activos por estado sincronizados desde API: " + list.size());
                    } else {
                        Log.d(TAG, "Activos por estado sincronizados desde API: 0 (null list)");
                        list = new ArrayList<>();
                    }

                    callback.onComplete(ApiResponse.success(list, response.statusCode));
                } catch (Exception e) {
                    Log.e(TAG, "Error parseando ActivosPorEstado", e);
                    callback.onComplete(ApiResponse.failure("Parse error: " + e.getMessage(), response.statusCode));
                }
            }
        });
    }





}

