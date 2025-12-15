package com.example.diverscan.activeid.data.local.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB_DAO";
    private static final String DB_NAME = "dbTest";
    private static final int DB_VERSION = 1;
    private final Context context;

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (" +
                "userSysId TEXT, " +
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "isApproved INTEGER, " +
                "isOnLine INTEGER, " +
                "isLockedOut INTEGER, " +
                "Idrol TEXT )");

        db.execSQL("CREATE TABLE RolHH (" +
                "IdRol TEXT, " +
                "Page TEXT, " +
                "Description TEXT, " +
                "Username TEXT, " +
                "UserSysId TEXT, " +
                "Esta_Bloqueado INTEGER )");

        db.execSQL("CREATE TABLE UbicacionHH (" +
                "ASysId TEXT, " +
                "UbicacionA TEXT, " +
                "BSysId TEXT, " +
                "UbicacionB TEXT, " +
                "CSysId TEXT, " +
                "UbicacionC TEXT, " +
                "DSysId TEXT, " +
                "UbicacionD TEXT )");

        db.execSQL("CREATE TABLE TomasFisicas (" +
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

        db.execSQL("CREATE TABLE TomasFisicasResumen (" +
                "TomaFisicaId TEXT PRIMARY KEY, " +
                "NumeroToma TEXT, " +
                "IdToma TEXT, " +
                "TotalLecturas TEXT)");

        db.execSQL("CREATE TABLE TomasFisicasDetalle (" +
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

        db.execSQL("CREATE TABLE Activos (" +
                "ID_ACTIVO TEXT PRIMARY KEY, " +
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
                "FECHA_CREACION_ACTIVO TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS RolHH");
        db.execSQL("DROP TABLE IF EXISTS UbicacionHH");
        db.execSQL("DROP TABLE IF EXISTS TomasFisicas");
        db.execSQL("DROP TABLE IF EXISTS TomasFisicasResumen");
        db.execSQL("DROP TABLE IF EXISTS TomasFisicasDetalle");
        db.execSQL("DROP TABLE IF EXISTS Activos");

        onCreate(db);
    }
}
