package com.example.diverscan.activeid.data.local.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB_DAO";
    private static final String DB_NAME = "Test_ActiveId_v1";
    private static final int DB_VERSION = 8;
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
                "TotalActivos TEXT)");

        ensureTomasFisicasDetalleTable(db);
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
        ensureUbicacionHHTable(db);
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
                "Observaciones TEXT)");
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
                "SYNC_STATUS INTEGER DEFAULT 1" +
                ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ActivosApi_TAG_EPC ON ActivosApi (TAG_EPC)");
        
        // Ensure column exists for upgrades from older versions if table existed
        try {
            db.execSQL("ALTER TABLE ActivosApi ADD COLUMN SYNC_STATUS INTEGER DEFAULT 1");
        } catch (Exception e) {
            // Column likely exists
        }
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
}
