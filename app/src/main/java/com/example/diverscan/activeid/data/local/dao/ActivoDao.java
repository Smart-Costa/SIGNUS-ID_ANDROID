package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
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

    public ActivoDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new AppDatabaseHelper(context);
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
        v.put("TAG_EPC", a.getTagEpc());
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
        try {
            for (ActivoEntity a : activos) {
                ContentValues values = entityToContentValues(a);
                values.put("SYNC_STATUS", 1); // 1 = Sincronizado
                db.insertWithOnConflict(TABLE_ACTIVOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando activos", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void pushLocalChangesToApi(final Runnable onComplete) {
        final List<ActivoEntity> pending = getPendingActivos();
        if (pending.isEmpty()) {
            Log.d(TAG, "No hay activos pendientes de sincronizar.");
            if (onComplete != null) onComplete.run();
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        
        // Construir JsonArray
        JsonArray jsonArray = new JsonArray();
        for (ActivoEntity a : pending) {
            JsonObject o = new JsonObject();
            // Mapear campos requeridos por el API (según ActivosController.cs)
            // Nota: El API espera nombres de propiedades específicos.
            // Si el API usa deserialización automática, deben coincidir con las propiedades de la clase C#.
            // ActivosController usa: item.Value<string>("NUMERO_ACTIVO") etc.
            
            o.addProperty("ID_ACTIVO", a.getIdActivo());
            o.addProperty("NUMERO_ACTIVO", a.getNumeroActivo());
            o.addProperty("NUMERO_ETIQUETA", a.getNumeroEtiqueta());
            o.addProperty("DESCRIPCION_CORTA", a.getDescripcionCorta());
            o.addProperty("DESCRIPCION_LARGA", a.getDescripcionLarga());
            o.addProperty("CATEGORIA", a.getCategoria());
            o.addProperty("ESTADO", a.getEstado());
            o.addProperty("EMPRESA", a.getEmpresa());
            o.addProperty("MARCA", a.getMarca());
            o.addProperty("MODELO", a.getModelo());
            o.addProperty("NUMERO_SERIE", a.getNumeroSerie());
            o.addProperty("COSTO", a.getCosto());
            o.addProperty("NUMERO_FACTURA", a.getNumeroFactura());
            o.addProperty("FECHA_COMPRA", a.getFechaCompra());
            o.addProperty("FECHA_CAPITALIZACION", a.getFechaCapitalizacion());
            o.addProperty("VALOR_RESIDUAL", a.getValorResidual());
            o.addProperty("TAG_EPC", a.getTagEpc());
            o.addProperty("COLOR", a.getColor());
            o.addProperty("TAMANIO_MEDIDA", a.getTamanioMedida());
            o.addProperty("OBSERVACIONES", a.getObservaciones());
            o.addProperty("ESTADO_ACTIVO", (a.getEstadoActivo() != null && a.getEstadoActivo()));
            // Añadir más campos si es necesario
            
            jsonArray.add(o);
        }

        // Endpoint: api/Activos/sincronizar
        // ApiClient.post espera un objeto, pasamos JsonArray
        Type type = new TypeToken<JsonElement>() {}.getType(); // Respuesta genérica

        api.post("Activos/sincronizar", jsonArray, type, new ApiCallback<JsonElement>() {
            @Override
            public void onComplete(ApiResponse<JsonElement> response) {
                if (response.success) {
                    Log.d(TAG, "Activos sincronizados correctamente: " + pending.size());
                    markAsSynced(pending);
                } else {
                    Log.e(TAG, "Error al sincronizar activos: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
            }
        });
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
        a.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
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
        return a;
    }

    public List<ActivoEntity> getActivosByUbicacion(String idOficina) {
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ACTIVOS + " WHERE UBICACION_D = ?", new String[]{idOficina})) {
            if (c.moveToFirst()) {
                do {
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
                    a.setValorResidual(c.getDouble(c.getColumnIndexOrThrow("VALOR_RESIDUAL")));
                    a.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
                    a.setColor(c.getString(c.getColumnIndexOrThrow("COLOR")));
                    a.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
                    a.setUbicacionA(c.getString(c.getColumnIndexOrThrow("UBICACION_A")));
                    a.setUbicacionB(c.getString(c.getColumnIndexOrThrow("UBICACION_B")));
                    a.setUbicacionC(c.getString(c.getColumnIndexOrThrow("UBICACION_C")));
                    a.setUbicacionD(c.getString(c.getColumnIndexOrThrow("UBICACION_D")));
                    a.setUbicacionSecundaria(c.getString(c.getColumnIndexOrThrow("UBICACION_SECUNDARIA")));
                    list.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo activos por ubicacion", e);
        } finally {
            db.close();
        }
        return list;
    }

    public List<ActivoEntity> getAllLocalActivos() {
        List<ActivoEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ACTIVOS, null)) {
            if (c.moveToFirst()) {
                do {
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
                    a.setValorResidual(c.getDouble(c.getColumnIndexOrThrow("VALOR_RESIDUAL")));
                    a.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
                    a.setColor(c.getString(c.getColumnIndexOrThrow("COLOR")));
                    a.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
                    list.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo activos locales", e);
        } finally {
            db.close();
        }

        return list;
    }

    public ActivoEntity getActivoByEpc(String epc) {
        ActivoEntity activo = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_ACTIVOS + " WHERE TAG_EPC = ? LIMIT 1",
                new String[]{epc}
        )) {
            if (c.moveToFirst()) {
                activo = new ActivoEntity();

                activo.setIdActivo(c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")));
                activo.setNumeroActivo(c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")));
                activo.setNumeroEtiqueta(c.getString(c.getColumnIndexOrThrow("NUMERO_ETIQUETA")));
                activo.setDescripcionCorta(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")));
                activo.setDescripcionLarga(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_LARGA")));
                activo.setCategoria(c.getString(c.getColumnIndexOrThrow("CATEGORIA")));
                activo.setEstado(c.getString(c.getColumnIndexOrThrow("ESTADO")));
                activo.setEmpresa(c.getString(c.getColumnIndexOrThrow("EMPRESA")));
                activo.setMarca(c.getString(c.getColumnIndexOrThrow("MARCA")));
                activo.setModelo(c.getString(c.getColumnIndexOrThrow("MODELO")));
                activo.setNumeroSerie(c.getString(c.getColumnIndexOrThrow("NUMERO_SERIE")));
                activo.setCosto(c.getDouble(c.getColumnIndexOrThrow("COSTO")));
                activo.setNumeroFactura(c.getString(c.getColumnIndexOrThrow("NUMERO_FACTURA")));
                activo.setValorResidual(c.getDouble(c.getColumnIndexOrThrow("VALOR_RESIDUAL")));
                activo.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
                activo.setFotos(c.getString(c.getColumnIndexOrThrow("FOTOS")));
                activo.setColor(c.getString(c.getColumnIndexOrThrow("COLOR")));
                activo.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por EPC", e);
        } finally {
            db.close();
        }

        return activo;
    }

    public ActivoEntity getActivoById(String id) {
        ActivoEntity activo = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_ACTIVOS + " WHERE NUMERO_ACTIVO = ? LIMIT 1",
                new String[]{id}
        )) {
            if (c.moveToFirst()) {
                activo = new ActivoEntity();

                activo.setIdActivo(c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")));
                activo.setNumeroActivo(c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")));
                activo.setNumeroEtiqueta(c.getString(c.getColumnIndexOrThrow("NUMERO_ETIQUETA")));
                activo.setDescripcionCorta(c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")));
                activo.setNumeroSerie(c.getString(c.getColumnIndexOrThrow("NUMERO_SERIE")));
                activo.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
                activo.setFotos(c.getString(c.getColumnIndexOrThrow("FOTOS")));
                activo.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por EPC", e);
        } finally {
            db.close();
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
                activo.setTagEpc(c.getString(c.getColumnIndexOrThrow("TAG_EPC")));
                activo.setFotos(c.getString(c.getColumnIndexOrThrow("FOTOS")));
                activo.setObservaciones(c.getString(c.getColumnIndexOrThrow("OBSERVACIONES")));
            }
        } catch (Exception e) {
            Log.e("TAG", "Error consultando por ID_ACTIVO", e);
        } finally {
            db.close();
        }

        return activo;
    }

    public EntidadActivosInventarios getActivoInventarioByEpc(String epc) {
        EntidadActivosInventarios result = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT a.*, o.Nombre as NombreOficina FROM " + TABLE_ACTIVOS + " a " +
                       "LEFT JOIN Oficina o ON a.UBICACION_D = o._id " +
                       "WHERE a.TAG_EPC = ?";
                       
        try (Cursor c = db.rawQuery(query, new String[]{epc})) {
            if (c.moveToFirst()) {
                 result = new EntidadActivosInventarios(
                    c.getString(c.getColumnIndexOrThrow("NUMERO_ACTIVO")),
                    c.getString(c.getColumnIndexOrThrow("DESCRIPCION_CORTA")),
                    c.getString(c.getColumnIndexOrThrow("TAG_EPC")),
                    c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")),
                    c.getString(c.getColumnIndex("NombreOficina")), // Use getColumnIndex, might be -1 if not found? No, rawQuery returns it.
                    c.getString(c.getColumnIndexOrThrow("UBICACION_D")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_C")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_B")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_A")),
                    c.getString(c.getColumnIndexOrThrow("UBICACION_SECUNDARIA"))
                 );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching ActivoInventario", e);
        } finally {
            db.close();
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
                    c.getString(c.getColumnIndexOrThrow("TAG_EPC")),
                    c.getString(c.getColumnIndexOrThrow("ID_ACTIVO")),
                    c.getString(c.getColumnIndex("NombreOficina")),
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

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<ActivoEntity>>() {}.getType();

        api.<List<ActivoEntity>>get("Activos", type, new ApiCallback<List<ActivoEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<ActivoEntity>> response) {
                if (response.success && response.data != null) {
                    syncActivos(response.data);
                    Log.d(TAG, "Activos sincronizados desde API: " + response.data.size());
                } else {
                    Log.e(TAG, "Error al sincronizar activos desde API: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
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

    public void pushLocalChangesToApi(final ApiCallback<JsonElement> callback) {
        List<ActivoEntity> localActivos = getAllLocalActivos();
        if (localActivos.isEmpty()) {
            Log.d(TAG, "No hay activos locales para sincronizar");
            if (callback != null) callback.onComplete(ApiResponse.success(null, 200));
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<JsonElement>() {}.getType();

        // Use SyncBatch as it is available on the remote server
        api.post("Activos/SyncBatch", localActivos, type, callback);
    }
}

