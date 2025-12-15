package com.example.diverscan.activeid.sqlite;

    import android.content.ContentValues;
    import android.content.Context;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.database.sqlite.SQLiteOpenHelper;
    import android.util.Log;

    import com.example.diverscan.activeid.TomasFisicas.NuevoActivo;
    import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;

    import java.util.ArrayList;

public class newAssets extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;
    Context _context;
    public newAssets(Context context){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Boolean ExisteActivo(String idActivo) {
        String q = "Select * FROM  NewAssets Where CodeBar ='" + idActivo + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(q, null);
        if (cursor.moveToFirst()) {
            return true;
        } else
        {
            return false;
        }
    }

    public boolean VerificarPlaca(String Placa){
        SQLiteDatabase db = this.getReadableDatabase();
        String query="Select * from NewAssets Where CodeBar ='" + Placa + "'";
        Cursor activosCursor = db.rawQuery(query, null);
        if (activosCursor.moveToFirst()) {
            return true;
        } else
        {
            return false;
        }
    }
    public boolean VerificarNumero(String numero){
        SQLiteDatabase db = this.getReadableDatabase();
        String query="Select * from NewAssets Where Numero ='" + numero + "'";
        Cursor activosCursor = db.rawQuery(query, null);
        if (activosCursor.moveToFirst()) {
            return true;
        } else
        {
            return false;
        }
    }

    public boolean InsertarActivo (String numero, String placa, String descripcion,String idCompania,String compania, String idEdificio,String edificio,
                                   String idPiso,String piso, String idOficina,String oficina, String nombreEmpleado,String encargado,
                                   String marca, String modelo, String Serial, String epc, String estado,
                                   String EstadoDescripcion, String EstadoConservacion, String AnoFabricacion, String Capacidad) {

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Numero", numero);
        contentValues.put("CodeBar", placa);
        contentValues.put("Descripcion", descripcion);
        contentValues.put("IdCompania", idCompania);
        contentValues.put("Compania", compania);
        contentValues.put("IdEdificio", idEdificio);
        contentValues.put("Edificio", edificio);
        contentValues.put("IdPiso", idPiso);
        contentValues.put("Piso", piso);
        contentValues.put("IdOficina", idOficina);
        contentValues.put("Oficina", oficina);
        contentValues.put("employeeName", nombreEmpleado);
        contentValues.put("employeeRelated", encargado);
        contentValues.put("Marca", marca);
        contentValues.put("Modelo", modelo);
        contentValues.put("Serial", Serial);
        contentValues.put("Tag", epc);
        contentValues.put("parentAssetSysId", "00000000-0000-0000-0000-000000000000");
        contentValues.put("assetStatusSysId", estado);
        contentValues.put("entryUser", userId);
        contentValues.put("syncData", "1" );
        contentValues.put("EstadoDescripcion", EstadoDescripcion);
        contentValues.put("EstadoConservacion", EstadoConservacion);
        contentValues.put("AnoFabricacion", AnoFabricacion);
        contentValues.put("Capacidad", Capacidad);

        db.insert("NewAssets", null, contentValues);

   return true;
    }

    public boolean InsertarSubActivo (String numero, String placa, String descripcion,String idCompania,String compania, String idEdificio,String edificio,
                                   String idPiso,String piso, String idOficina,String oficina, String nombreEmpleado,String encargado,
                                   String marca, String modelo, String Serial, String epc, String idParentActivo, String estado) {

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Numero", numero);
        contentValues.put("CodeBar", placa);
        contentValues.put("Descripcion", descripcion);
        contentValues.put("IdCompania", idCompania);
        contentValues.put("Compania", compania);
        contentValues.put("IdEdificio", idEdificio);
        contentValues.put("Edificio", edificio);
        contentValues.put("IdPiso", idPiso);
        contentValues.put("Piso", piso);
        contentValues.put("IdOficina", idOficina);
        contentValues.put("Oficina", oficina);
        contentValues.put("employeeName", nombreEmpleado);
        contentValues.put("employeeRelated", encargado);
        contentValues.put("Marca", marca);
        contentValues.put("Modelo", modelo);
        contentValues.put("Serial", Serial);
        contentValues.put("Tag", epc);
        contentValues.put("parentAssetSysId", idParentActivo);
        contentValues.put("assetStatusSysId", estado);
        contentValues.put("entryUser", userId);
        contentValues.put("syncData", "1" );

        db.insert("NewAssets", null, contentValues);

        return true;
    }


    public ArrayList<NuevoActivo> IngresarActivoSync (){

        ArrayList<NuevoActivo> listActivos = new ArrayList<>();
        String query="Select * from NewAssets Where syncData = '1'";
        SQLiteDatabase db = this.getReadableDatabase(); //    el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor activosCursor = db.rawQuery(query, null);
        int cantidadDatos = activosCursor.getCount();
        activosCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String assetId  = activosCursor.getString(activosCursor.getColumnIndex("assetId"));
            String numero = activosCursor.getString(activosCursor.getColumnIndex("Numero"));
            String placa = activosCursor.getString(activosCursor.getColumnIndex("CodeBar"));
            String Descripcion = activosCursor.getString(activosCursor.getColumnIndex("Descripcion"));
            String IdCompania = activosCursor.getString(activosCursor.getColumnIndex("IdCompania"));
            String IdEdificio = activosCursor.getString(activosCursor.getColumnIndex("IdEdificio"));
            String IdPiso = activosCursor.getString(activosCursor.getColumnIndex("IdPiso"));
            String IdOficina = activosCursor.getString(activosCursor.getColumnIndex("IdOficina"));
            String encargado= activosCursor.getString(activosCursor.getColumnIndex("employeeRelated"));
            String Marca = activosCursor.getString(activosCursor.getColumnIndex("Marca"));
            String Modelo = activosCursor.getString(activosCursor.getColumnIndex("Modelo"));
            String serial= activosCursor.getString(activosCursor.getColumnIndex("Serial"));
            String epc = activosCursor.getString(activosCursor.getColumnIndex("Tag"));
            String compania = activosCursor.getString(activosCursor.getColumnIndex("Compania"));
            String edificio = activosCursor.getString(activosCursor.getColumnIndex("Edificio"));
            String piso = activosCursor.getString(activosCursor.getColumnIndex("Piso"));
            String oficina = activosCursor.getString(activosCursor.getColumnIndex("Oficina"));
            String parentAssetSyId = activosCursor.getString(activosCursor.getColumnIndex("parentAssetSysId"));
            String assetStatusSysId = activosCursor.getString(activosCursor.getColumnIndex("assetStatusSysId"));
            String entryUser = activosCursor.getString(activosCursor.getColumnIndex("entryUser"));
            String EstadoDescripcion = activosCursor.getString(activosCursor.getColumnIndex("EstadoDescripcion"));
            String EstadoConservacion = activosCursor.getString(activosCursor.getColumnIndex("EstadoConservacion"));
            String AnoFabricacion = activosCursor.getString(activosCursor.getColumnIndex("AnoFabricacion"));
            String Capacidad = activosCursor.getString(activosCursor.getColumnIndex("Capacidad"));
            NuevoActivo activoRecord = new NuevoActivo(assetId, numero, placa, Descripcion, IdCompania, IdEdificio,
                    IdPiso, IdOficina, encargado, Marca, Modelo, serial,epc, compania, edificio, piso, oficina, parentAssetSyId,assetStatusSysId, entryUser,
                    AnoFabricacion, Capacidad,EstadoDescripcion, EstadoConservacion);

            listActivos.add(activoRecord);
            activosCursor.moveToNext();

        }
        return listActivos;
    }

    public boolean EliminarSubActivo(String idActivo){
        SQLiteDatabase db = this.getWritableDatabase();


        try{
            String query = "Update NewAssets SET parentAssetSysId = '00000000-0000-0000-0000-000000000000' WHERE assetId ='"+idActivo+"'";
            db.execSQL(query);
            return true;

        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            return false;
        }
    }

    public boolean RecuperarSubActivo(String idActivo, String parentAssetSys){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            String query = "Update NewAssets SET parentAssetSysId = '"+parentAssetSys+"' where assetId ='"+idActivo+"'";
            db.execSQL(query);

        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            return false;
        }
        finally {
            db.endTransaction();
            return true;
        }
    }

    public boolean IngresarSync(ArrayList<NuevoActivo> listActivos){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for(NuevoActivo nuevoActivo : listActivos) {
                String query = "Delete FROM NewAssets where assetId ='"+nuevoActivo.getAssetId()+"'";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }finally {
            db.endTransaction();
            return true;
        }
    }

}

