package com.example.diverscan.activeid.data.local.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB_DAO";
    private static final String DB_NAME = "Test_ActiveId_v1";
    private static final int DB_VERSION = 9;
    private final Context context;

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Solo creamos las tablas que son responsabilidad de este módulo y no existen en LoginDBHelper
        
        db.execSQL("CREATE TABLE IF NOT EXISTS TomasFisicas (" +
                "tomaFisicaId TEXT PRIMARY KEY, " +
                "nombre TEXT, " +
                "fechaInicial TEXT, " +
                "fechaFinal TEXT, " +
                "categoria TEXT, " +
                "usuarioAsignado TEXT, " +
                "unidadOrganizativa TEXT, " +
                "estadoActivo TEXT, " +
                "ubicacionA TEXT, " +
                "ubicacionB TEXT, " +
                "ubicacionC TEXT, " +
                "ubicacionD TEXT ) ");

        db.execSQL("CREATE TABLE IF NOT EXISTS TomasFisicasResumen (" +
                "IdToma TEXT PRIMARY KEY, " +
                "TomaFisicaId TEXT, " +
                "NumeroToma TEXT, " +
                "TotalLecturas TEXT, " +
                "FechaCreacion TEXT, " +
                "ActivosLeidos TEXT, " +
                "Sobrantes TEXT, " +
                "Faltantes TEXT, " +
                "TotalActivos TEXT, " +
                "SYNC_STATUS INTEGER DEFAULT 0)");

        if (!checkColumnExists(db, "TomasFisicasResumen", "SYNC_STATUS")) {
            try {
                db.execSQL("ALTER TABLE TomasFisicasResumen ADD COLUMN SYNC_STATUS INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Ignore
            }
        }

        ensureTomasFisicasDetalleTable(db);
        ensurePendingDeletesTable(db);
        ensureActivosApiTable(db);
        ensureUbicacionHHTable(db);

        db.execSQL("CREATE TABLE if not exists TipoTomaInventario (_id Text PRIMARY KEY, " +
                "Nombre Text, Descripcion Text, fechaInicio Text, fechaFinal Text, estado Text)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureActivosApiTable(db);
        ensureTomasFisicasDetalleTable(db);
        ensurePendingDeletesTable(db);
        ensureUbicacionHHTable(db);
        
        // Ensure SYNC_STATUS column in TomasFisicasResumen on open
        if (!checkColumnExists(db, "TomasFisicasResumen", "SYNC_STATUS")) {
            try {
                db.execSQL("ALTER TABLE TomasFisicasResumen ADD COLUMN SYNC_STATUS INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static void ensureTomasFisicasDetalleTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS TomasFisicasDetalle (" +
                "IdTakeDetail TEXT PRIMARY KEY, " +
                "IdToma TEXT, " +
                "NumeroToma TEXT, " +
                "FechaToma TEXT, " +
                "EPC TEXT, " +
                "DateRead TEXT, " +
                "ActivoId TEXT, " +
                "EstadoInventario TEXT, " +
                "UbicacionDetalleA TEXT, " +
                "UbicacionDetalleB TEXT, " +
                "UbicacionDetalleC TEXT, " +
                "UbicacionDetalleD TEXT, " +
                "Observaciones TEXT, " +
                "SYNC_STATUS INTEGER DEFAULT 0)");

        if (!checkColumnExists(db, "TomasFisicasDetalle", "SYNC_STATUS")) {
            try {
                db.execSQL("ALTER TABLE TomasFisicasDetalle ADD COLUMN SYNC_STATUS INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static void ensurePendingDeletesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS PendingDeletes (" +
                "Id TEXT PRIMARY KEY, " +
                "EntityType TEXT, " +
                "RefId TEXT, " +
                "CreatedAt TEXT" +
                ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_PendingDeletes_Entity_RefId ON PendingDeletes (EntityType, RefId)");
    }

    private static void ensureActivosApiTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS ActivosApi (" +
                "ID_ACTIVO TEXT PRIMARY KEY, " +
                "NUMERO_ACTIVO TEXT, " +
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
                "FECHA_CREACION_ACTIVO TEXT, " +
                "EPC TEXT, " +
                "CATEGORIA_A TEXT, " +
                "CATEGORIA_B TEXT, " +
                "CATEGORIA_C TEXT, " +
                "UBICACION_LOGICA_A TEXT, " +
                "UBICACION_LOGICA_B TEXT, " +
                "UBICACION_LOGICA_C TEXT, " +
                "ENTIDAD_ASOCIADA TEXT, " +
                "COSTO_DEPRECIACION REAL, " +
                "SYNC_STATUS INTEGER DEFAULT 1" +
                ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ActivosApi_TAG_EPC ON ActivosApi (TAG_EPC)");
        
        // Ensure column exists for upgrades from older versions if table existed
        if (!checkColumnExists(db, "ActivosApi", "SYNC_STATUS")) {
            try {
                db.execSQL("ALTER TABLE ActivosApi ADD COLUMN SYNC_STATUS INTEGER DEFAULT 1");
            } catch (Exception e) {
                // Column likely exists or other error
            }
        }
        
        // Ensure new columns for model consistency
        String[] newColumns = {
            "EPC", "CATEGORIA_A", "CATEGORIA_B", "CATEGORIA_C", 
            "UBICACION_LOGICA_A", "UBICACION_LOGICA_B", "UBICACION_LOGICA_C", 
            "ENTIDAD_ASOCIADA", "COSTO_DEPRECIACION"
        };
        
        for (String col : newColumns) {
            if (!checkColumnExists(db, "ActivosApi", col)) {
                try {
                    String type = col.equals("COSTO_DEPRECIACION") ? "REAL" : "TEXT";
                    db.execSQL("ALTER TABLE ActivosApi ADD COLUMN " + col + " " + type);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private static boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean exists = false;
        try (android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex("name");
                    if (nameIndex >= 0) {
                        String name = cursor.getString(nameIndex);
                        if (columnName.equalsIgnoreCase(name)) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return exists;
    }

    private static void ensureUbicacionHHTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS UbicacionHH (" +
                "ASysId TEXT, " +
                "UbicacionA TEXT, " +
                "BSysId TEXT, " +
                "UbicacionB TEXT, " +
                "CSysId TEXT, " +
                "UbicacionC TEXT, " +
                "DSysId TEXT, " +
                "UbicacionD TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            onCreate(db);
            ensureActivosApiTable(db);
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("TomasFisicas", null, null);
            db.delete("TomasFisicasResumen", null, null);
            db.delete("TomasFisicasDetalle", null, null);
            db.delete("PendingDeletes", null, null);
            db.delete("ActivosApi", null, null);
            db.delete("UbicacionHH", null, null);
            db.delete("TipoTomaInventario", null, null);
            // Add other tables here if needed
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
