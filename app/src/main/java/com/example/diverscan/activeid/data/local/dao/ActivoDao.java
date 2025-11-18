package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActivoDao extends SQLiteOpenHelper {

    private static final String DB_NAME = "dbSignusId.db";
    private static final int DB_VERSION = 7;
    private static final String TAG = "ACTIVO_DAO";
    private final Context context;

    public ActivoDao(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Activos (" +
                "ID_ACTIVO TEXT, " +
                "NUMERO_ACTIVO INTEGER, " +
                "NUMERO_ETIQUETA TEXT, " +
                "DESCRIPCION_CORTA TEXT, " +
                "DESCRIPCION_LARGA TEXT, " +
                "CATEGORIA TEXT, " +
                "ESTADO TEXT, " +
                "EMPRESA TEXT, " +
                "MARCA TEXT, " +
                "MODELO TEXT, " +
                "NUMERO_SERIE TEXT, " +
                "COSTO REAL, " +
                "NUMERO_FACTURA TEXT, " +
                "FECHA_COMPRA TEXT, " +
                "FECHA_CAPITALIZACION TEXT, " +
                "VALOR_RESIDUAL REAL, " +
                "DOCUMENTO TEXT, " +
                "FOTOS TEXT, " +
                "NUMERO_PARTE_FABRICANTE TEXT, " +
                "DEPRECIADO TEXT, " +
                "DESCRIPCION_DEPRECIADO TEXT, " +
                "ANOS_VIDA_UTIL INTEGER, " +
                "CUENTA_CONTABLE_DEPRESIACION TEXT, " +
                "CENTRO_COSTOS TEXT, " +
                "DESCRIPCION_ESTADO_ULTIMO_INVENTARIO TEXT, " +
                "TAG_EPC TEXT, " +
                "EMPLEADO TEXT, " +
                "UBICACION_A TEXT, " +
                "UBICACION_B TEXT, " +
                "UBICACION_C TEXT, " +
                "UBICACION_D TEXT, " +
                "UBICACION_SECUNDARIA TEXT, " +
                "FECHA_GARANTIA TEXT, " +
                "COLOR TEXT, " +
                "TAMANIO_MEDIDA TEXT, " +
                "OBSERVACIONES TEXT, " +
                "ESTADO_ACTIVO INTEGER, " +
                "FECHA_CREACION_ACTIVO TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS activos");
        onCreate(db);
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


    public void syncActivos(List<ActivoEntity> activos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ActivoEntity a : activos) {
                ContentValues values = entityToContentValues(a);
                db.insertWithOnConflict("Activos", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando activos", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<ActivoEntity> getAllLocalActivos() {
        List<ActivoEntity> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM Activos", null)) {
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

    public void fetchAndSyncFromApi() {
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
            }
        });
    }

    public void pushLocalChangesToApi() {
        List<ActivoEntity> localActivos = getAllLocalActivos();
        if (localActivos.isEmpty()) {
            Log.d(TAG, "No hay activos locales para sincronizar con el servidor");
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        api.<ApiResponse<Void>>post("Activos", localActivos, type, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                if (response.success) {
                    Log.d(TAG, "Activos locales enviados exitosamente al servidor");
                } else {
                    Log.e(TAG, "Error enviando activos al servidor: " + response.errorMessage);
                }
            }
        });
    }
}

