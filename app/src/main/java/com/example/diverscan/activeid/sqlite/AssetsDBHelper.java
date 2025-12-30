package com.example.diverscan.activeid.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.TomasFisicas.ActivoRecord;
import com.example.diverscan.activeid.TomasFisicas.AjustarActivoUbicacion;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivos;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivosInventarios;
import com.example.diverscan.activeid.TomasFisicas.EntidadCategoriaActivos;
import com.example.diverscan.activeid.TomasFisicas.entidadaAjustarUbicacion;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;

import java.util.ArrayList;

public class AssetsDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_FILE_PATH = "/Android/DBActive";
    Context _context;
    public AssetsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public boolean ActualizarEPC (String numactivo, String epcnuevo){

        try {

            SQLiteDatabase db = this.getWritableDatabase();

            String query = "Update Activos Set Tag ='"+epcnuevo+"', Sinc = 1 where Numero ='"+numactivo+"'";
            db.execSQL(query);
            return true;

        }catch (Exception ex){

            ex.toString();
            return false;
        }
    }

    public Cursor VerActivo (String NumeroActivo){

        try {

            SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve

            String query="Select * from Activos where Numero ='"+NumeroActivo+"' or CodeBar='"+NumeroActivo+"'";
            return db.rawQuery(query, null);

        }catch (Exception ex){

            ex.toString();
            return null;
        }
    }


    public EntidadActivos cargarActivo (String query){
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            EntidadActivos entidadActivos=getEntidadActivos(cursor);
            cursor.close();
            return  entidadActivos;
        }
        return  null;
    }

    public ArrayList<String> ObtenerUbicacionesSecundarias(String idCompania, String idEdificio, String idPiso, String idOficina) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT DISTINCT Departamento FROM Activos WHERE Departamento IS NOT NULL AND Departamento != ''");

            if (idCompania != null && !idCompania.equals("-1")) {
                queryBuilder.append(" AND IdCompania = '").append(idCompania).append("'");
            }
            if (idEdificio != null && !idEdificio.equals("-1")) {
                queryBuilder.append(" AND IdEdificio = '").append(idEdificio).append("'");
            }
            if (idPiso != null && !idPiso.equals("-1")) {
                queryBuilder.append(" AND IdPiso = '").append(idPiso).append("'");
            }
            if (idOficina != null && !idOficina.equals("-1")) {
                queryBuilder.append(" AND IdOficina = '").append(idOficina).append("'");
            }

            queryBuilder.append(" ORDER BY Departamento");

            Cursor cursor = db.rawQuery(queryBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                do {
                    String dep = cursor.getString(0);
                    if (dep != null) {
                        list.add(dep);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean ActualizarSubActivo(String idActivo){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            String query = "Update Activos SET parentAssetSysId = '00000000-0000-0000-0000-000000000000'" +
                    ",SyncData = '1'  WHERE _id ='"+idActivo+"'";
            db.execSQL(query);
            return true;

        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            return false;
        }
    }
    public boolean RecuperaSubActivo(String idActivo, String parentAssetSys){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            String query = "Update Activos SET parentAssetSysId = '"+parentAssetSys+"' where _Id ='"+idActivo+"'";
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

    public EntidadActivosInventarios CargarActivosInventario (String query){
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            EntidadActivosInventarios entidadActivos=getEntidadActivosInventarios(cursor);
            cursor.close();
            return  entidadActivos;
        }
        return  null;
    }

    public EntidadCategoriaActivos cargarCategoriaActivo (String query){
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            EntidadCategoriaActivos entidadCategoriaActivos=getEntidadCategoriaActivos(cursor);
            cursor.close();
            return  entidadCategoriaActivos;
        }
        return  null;
    }

    public EntidadActivos VerActivoPlaca (String NumeroActivo){
        String query="Select * from Activos where CodeBar='"+NumeroActivo+"'";
        return  cargarActivo(query);
    }



    public EntidadCategoriaActivos NombreCategoria (String Idcategoria){
        String query="Select assetCategorySysId, description, name from categoriaActivos where assetCategorySysId='"+Idcategoria+"'";
        return  cargarCategoriaActivo(query);
    }

//    public EntidadActivos Comparar (String epc){
//        String query="Select * from Activos where CodeBar='"+epc+"'";
//        return  cargarActivo(query);
//    }

    public EntidadActivos VerActivoEpc (String epc){
        String query="Select * from Activos where Tag='"+epc+"'";  /*inner join Tags on Activos.Tag = Tags._id  where Tags.EPC='"+epc+"'";*/
        return  cargarActivo(query);
    }

    public EntidadActivosInventarios ActivosUbicacionInventario(String epc){
        String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag, " +
                "p._id as IdPiso, e._id as IdEdificio, r._id as IdCompania " +
                "from Oficina o " +
                " Inner Join Activos a ON a.IdOficina = o._id " +
                " Left Join Pisos p ON o.idPiso = p._id " +
                " Left Join Edificios e ON p.idEdificio = e._id " +
                " Left Join RazonSocial r ON e.idRazonSocial = r._id " +
                " where a.Tag='"+epc+"'";
        return CargarActivosInventario(query);

    }
    public EntidadActivosInventarios ActivosUbicacionInventarioBarcode(String Placa) {
        String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag, " +
                "p._id as IdPiso, e._id as IdEdificio, r._id as IdCompania " +
                "from Oficina o " +
                " Inner Join Activos a ON a.IdOficina = o._id " +
                " Left Join Pisos p ON o.idPiso = p._id " +
                " Left Join Edificios e ON p.idEdificio = e._id " +
                " Left Join RazonSocial r ON e.idRazonSocial = r._id " +
                " where a.CodeBar='" + Placa + "'";
        return CargarActivosInventario(query);

    }
    public EntidadActivos VerActivoSerie(String serie){
        String query="Select * from Activos where Serial='"+serie+"'";  /*inner join Tags on Activos.Tag = Tags._id  where Tags.EPC='"+epc+"'";*/
        return  cargarActivo(query);
    }


    public EntidadActivos VerActivoNumero (String NumeroActivo){

        String query="Select * from Activos where Numero ='"+NumeroActivo+"'";
        return  cargarActivo(query);
    }



    public Cursor ObtenerSubActivos (String idActivo){

        try {

            SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve

            String query="Select _id AS _assetSysId, Descripcion AS _assetDescription, Alias AS _assetAttendant" +
                    ", Tag AS _assetTag, Numero AS _assetNumber, CodeBar AS _assetBarcode, parentAssetSysId from " +
                    " Activos where parentAssetSysId = "+"'"+idActivo+"'" +
                    " UNION Select assetId AS _assetSysId, Descripcion AS _assetDescription" +
                    ", employeeName AS _assetAttendant, Tag AS _assetTag, Numero AS _assetNumber" +
                    ", CodeBar  AS _assetBarcode, parentAssetSysId from NewAssets where parentAssetSysId = "+"'"+idActivo+"'";
            return db.rawQuery(query, null);

        }catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public Cursor TraerActivos (String txtDescripcion){

        try {

            SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve

            String query="Select * from Activos where Descripcion like "+"'%"+txtDescripcion+"%'";
            return db.rawQuery(query, null);

        }catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public EntidadActivos ObtenerActivosPorOficina (String IdOficina){

        String query="Select * from Activos where IdOficina ='"+IdOficina+"'";
        return  cargarActivo(query);
    }

    public ArrayList<EntidadActivos> CargarActivos(String query)
    {
        ArrayList<EntidadActivos> activos = new ArrayList<EntidadActivos>();
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                activos.add(getEntidadActivos (cursor));

            } while(cursor.moveToNext());
            cursor.close();
        }
        return  activos;
    }

    private EntidadActivos getEntidadActivos(Cursor cursor){
        String idActivo= cursor.getString(cursor.getColumnIndex("_id"));
        String descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
        String compania = cursor.getString(cursor.getColumnIndex("Compania"));
        String idcompania = cursor.getString(cursor.getColumnIndex("IdCompania"));
        String edificio = cursor.getString(cursor.getColumnIndex("Edificio"));
        String idEdificio = cursor.getString(cursor.getColumnIndex("IdEdificio"));
        String piso = cursor.getString(cursor.getColumnIndex("Piso"));
        String idPiso = cursor.getString(cursor.getColumnIndex("IdPiso"));
        String oficina = cursor.getString(cursor.getColumnIndex("Oficina"));
        String idOficina = cursor.getString(cursor.getColumnIndex("IdOficina"));
        String Encargado=cursor.getString(cursor.getColumnIndex("Alias"));
        String Marca=cursor.getString(cursor.getColumnIndex("Marca"));
        String Modelo=cursor.getString(cursor.getColumnIndex("Modelo"));
        String Serie=cursor.getString(cursor.getColumnIndex("Serial"));
        String Tag=cursor.getString(cursor.getColumnIndex("Tag"));
        String numero= cursor.getString(cursor.getColumnIndex("Numero"));
        String codeBar =cursor.getString(cursor.getColumnIndex("CodeBar"));
        String IdCategoria = cursor.getString(cursor.getColumnIndex("IdCategoria"));
        String employeeRelated = cursor.getString(cursor.getColumnIndex("EmployeeRelatedSysId"));
        String assetStatusSysId = cursor.getString(cursor.getColumnIndex("AssetStatusSysId"));
        String parentAssetSysId = cursor.getString(cursor.getColumnIndex("parentAssetSysId"));
        String anoFabricacion= cursor.getString(cursor.getColumnIndex("AnoFabricacion"));
        String capacidad= cursor.getString(cursor.getColumnIndex("Capacidad"));
        String estadoDescripcion = cursor.getString(cursor.getColumnIndex("EstadoDescripcion"));
        String estadoConservacion = cursor.getString(cursor.getColumnIndex("EstadoConservacion"));
        String departamento = null;
        if(cursor.getColumnIndex("Departamento") != -1) {
            departamento = cursor.getString(cursor.getColumnIndex("Departamento"));
        }
        EntidadActivos entidadActivos= new EntidadActivos(idActivo,descripcion,compania,idcompania,edificio,idEdificio,piso,idPiso,oficina,idOficina,Tag
                ,numero,codeBar,Marca,Modelo,Serie,Encargado,IdCategoria, employeeRelated, assetStatusSysId, parentAssetSysId, anoFabricacion, capacidad
                ,estadoDescripcion,estadoConservacion, departamento);
        return  entidadActivos;
    }



    private EntidadActivosInventarios getEntidadActivosInventarios(Cursor cursor){
        String idActivo= cursor.getString(cursor.getColumnIndex("_id"));
        String descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
        String oficina = cursor.getString(cursor.getColumnIndex("Nombre"));
        String idOficina = cursor.getString(cursor.getColumnIndex("IdOficina"));
        String Tag=cursor.getString(cursor.getColumnIndex("Tag"));
        String codeBar =cursor.getString(cursor.getColumnIndex("CodeBar"));
        String idPiso = cursor.getString(cursor.getColumnIndex("IdPiso"));
        String idEdificio = cursor.getString(cursor.getColumnIndex("IdEdificio"));
        String idCompania = cursor.getString(cursor.getColumnIndex("IdCompania"));
        String departamento = null;
        if(cursor.getColumnIndex("Departamento") != -1) {
            departamento = cursor.getString(cursor.getColumnIndex("Departamento"));
        }

      EntidadActivosInventarios entidadActivos= new EntidadActivosInventarios(codeBar,descripcion,Tag,idActivo,oficina,idOficina, idPiso, idEdificio, idCompania, departamento);
        return  entidadActivos;
    }

    private EntidadCategoriaActivos getEntidadCategoriaActivos(Cursor cursor){
        String assetCategorySysId = cursor.getString(cursor.getColumnIndex("assetCategorySysId"));
        String description= cursor.getString(cursor.getColumnIndex("description"));
        String name = cursor.getString(cursor.getColumnIndex("name"));

        EntidadCategoriaActivos entidadCategoriaActivos= new EntidadCategoriaActivos(assetCategorySysId,description,name);
        return  entidadCategoriaActivos;
    }


    public Cursor ConsultarActivosLocate (String NumeroActivo){

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select A._id, A.CodeBar as NumeroActivo, A.Descripcion as Descripcion," +
                           "A.Compania as RazonSocial, A.Edificio as Edificio, A.Piso as Piso, " +
                           "T.Nombre as Oficina, A.Tag as Tag from Activos A " +
                    " INNER JOIN Oficina T ON T._id = A.IdOficina"+
                           " where A.CodeBar like " +"'%"+NumeroActivo+"%'";

            return db.rawQuery(query, null);

        } catch (Exception ex){

            ex.toString();
            return null;
        }
    }
    public boolean ActualizarUbicacionActivo(String AcIdCompania,String AcNombreCompania,String AcIdEdificio,
                                             String AcNombreEdificio,String AcIdPiso,String AcPisoNombre,
                                             String AcIdoficina,String AcOficinaNombre,String FillidActivo){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        try {
            SQLiteDatabase db = this.getWritableDatabase();

            String query = "Update Activos Set IdCompania = '"+AcIdCompania+"'," +
                    "Compania = '"+AcNombreCompania+"', IdEdificio = '"+AcIdEdificio+"', Edificio = '"+AcNombreEdificio+"'," +
                    "IdPiso = '"+AcIdPiso+"', Piso = '"+AcPisoNombre+"', IdOficina = '"+AcIdoficina+"', Oficina = '"+AcOficinaNombre+"'," +
                    "SyncData = '1', UpdateUser = '"+userId+"' "+
                    " where _id ='"+FillidActivo+"'";
            db.execSQL(query);
            return true;
        } catch (Exception ex){
        ex.toString();
        return false;
    }
    }
    public boolean ActualizarUbicacionTodo (String AcIdCompania,String AcNombreCompania,String AcIdEdificio,
                                           String AcNombreEdificio,String AcIdPiso,String AcPisoNombre,
                                           String AcIdoficina,String AcOficinaNombre, ArrayList<entidadaAjustarUbicacion> entidadaAjustarUbicacions){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (entidadaAjustarUbicacion item: entidadaAjustarUbicacions){

                String query = "Update Activos Set IdCompania = '"+AcIdCompania+"'," +
                        "Compania = '"+AcNombreCompania+"', IdEdificio = '"+AcIdEdificio+"', Edificio = '"+AcNombreEdificio+"'," +
                        "IdPiso = '"+AcIdPiso+"', Piso = '"+AcPisoNombre+"', IdOficina = '"+AcIdoficina+"', Oficina = '"+AcOficinaNombre+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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

    public boolean ActualizarUbicacionSeleccionado (String AcIdCompania,String AcNombreCompania,String AcIdEdificio,
                                            String AcNombreEdificio,String AcIdPiso,String AcPisoNombre,
                                            String AcIdoficina,String AcOficinaNombre, ArrayList<AjustarActivoUbicacion> entidadaAjustarUbicacions){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (AjustarActivoUbicacion item: entidadaAjustarUbicacions){

                String query = "Update Activos Set IdCompania = '"+AcIdCompania+"'," +
                        "Compania = '"+AcNombreCompania+"', IdEdificio = '"+AcIdEdificio+"', Edificio = '"+AcNombreEdificio+"'," +
                        "IdPiso = '"+AcIdPiso+"', Piso = '"+AcPisoNombre+"', IdOficina = '"+AcIdoficina+"', Oficina = '"+AcOficinaNombre+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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

    //creado por Alejandra metodo para actualizar el activo
    public boolean ActualizarActivo (String numero, String placa, String descripcion, String idCompania,
                                     String compania, String idEdificio, String edificio ,String idPiso,
                                     String piso, String idOficina, String oficina, String marca,
                                     String modelo, String serie, String epc, String EmployeeRelated,
                                     String AssetStatusSysId, String EmployeName, String AnoFabricacion, String Capacidad,
                                     String DetalleEstado, String EstadoConservacion){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);
        try {

            SQLiteDatabase db = this.getWritableDatabase();

            String query = "Update Activos Set Descripcion = '"+descripcion+"', IdCompania = '"+idCompania+"'," +
                    "Compania = '"+compania+"', IdEdificio = '"+idEdificio+"', Edificio = '"+edificio+"'," +
                    "IdPiso = '"+idPiso+"', Piso = '"+piso+"', IdOficina = '"+idOficina+"', Oficina = '"+oficina+"'," +
                    "Alias = '"+EmployeName+"', Marca = '"+marca+"', Modelo = '"+modelo+"', Serial = '"+serie+"'," +
                    "Tag ='"+epc+"', SyncData = '1', CodeBar = '"+placa+"', UpdateUser = '"+userId+"',"+
                    " EmployeeRelatedSysId = '"+EmployeeRelated+"', AssetStatusSysId = '"+AssetStatusSysId+"', " +
                    " AnoFabricacion = '"+AnoFabricacion+"', Capacidad = '"+Capacidad+"', EstadoDescripcion = '"+DetalleEstado+"'," +
                    " EstadoConservacion = '"+EstadoConservacion+"'"+
                    " where Numero ='"+numero+"'";  //or CodeBar ='"+placa+"'
            db.execSQL(query);
            return true;

        }catch (Exception ex){
            ex.toString();
            return false;
        }
    }

    public Boolean doesRecordExist()
    {
        String q = "Select * FROM  Activos";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(q, null);
        if (cursor.moveToFirst()) {
            return true;
        } else
        {
            return false;
        }
    }

    public ArrayList<ActivoRecord> ObtenerActivoSync (){
        ArrayList<ActivoRecord> listActivos = new ArrayList<>();
        String query="Select * from Activos where SyncData ='1'";
        SQLiteDatabase db = this.getReadableDatabase(); //    el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor activosCursor = db.rawQuery(query, null);
        int cantidadDatos = activosCursor.getCount();
        activosCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String Alias = activosCursor.getString(activosCursor.getColumnIndex("Alias"));
            String Descripcion = activosCursor.getString(activosCursor.getColumnIndex("Descripcion"));
            String IdTag = activosCursor.getString(activosCursor.getColumnIndex("Tag"));
            String idActivo= activosCursor.getString(activosCursor.getColumnIndex("_id"));
            String IdOficina = activosCursor.getString(activosCursor.getColumnIndex("IdOficina"));
            String IdPiso = activosCursor.getString(activosCursor.getColumnIndex("IdPiso"));
            String IdEdificio = activosCursor.getString(activosCursor.getColumnIndex("IdEdificio"));
            String IdCompania = activosCursor.getString(activosCursor.getColumnIndex("IdCompania"));
            String Marca = activosCursor.getString(activosCursor.getColumnIndex("Marca"));
            String Modelo = activosCursor.getString(activosCursor.getColumnIndex("Modelo"));
            String Serial = activosCursor.getString(activosCursor.getColumnIndex("Serial"));
            String CodeBar = activosCursor.getString(activosCursor.getColumnIndex("CodeBar"));
            String UpdateUser = activosCursor.getString(activosCursor.getColumnIndex("UpdateUser"));
            String parentAssetSyId = activosCursor.getString(activosCursor.getColumnIndex("parentAssetSysId"));
            String employeeRelatedSysId= activosCursor.getString(activosCursor.getColumnIndex("EmployeeRelatedSysId"));
            String assetStatusSysId= activosCursor.getString(activosCursor.getColumnIndex("AssetStatusSysId"));
            String AnoFabricacion= activosCursor.getString(activosCursor.getColumnIndex("AnoFabricacion"));
            String Capacidad = activosCursor.getString(activosCursor.getColumnIndex("Capacidad"));
            String _estadoDescripcion = activosCursor.getString(activosCursor.getColumnIndex("EstadoDescripcion"));
            String _estadoConservacion = activosCursor.getString(activosCursor.getColumnIndex("EstadoConservacion"));
            ActivoRecord activoRecord = new ActivoRecord(Alias, Descripcion, IdTag, idActivo, IdOficina,
                    IdPiso, IdEdificio, IdCompania, Marca, Modelo, Serial, CodeBar, UpdateUser, parentAssetSyId
                    ,employeeRelatedSysId,assetStatusSysId,AnoFabricacion,Capacidad,_estadoDescripcion,_estadoConservacion);
            listActivos.add(activoRecord);
            activosCursor.moveToNext();

        }
        return listActivos;

    }

    public boolean ActualizarSync (ArrayList<ActivoRecord> listActivos){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for(ActivoRecord entidadActivos : listActivos) {
                String query = "Update Activos Set SyncData = '0' where _id ='"+entidadActivos.getIdActivo()+"'";
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

    public EntidadTiposTags NombreTipoTag (String Idcategoria){
        String query="Select a.IdTipoTag, a.code, a.name, a.description, a.category from tipoTags a inner join Tags t on a.IdTipoTag = t.IdTipoTag where t.EPC ='"+Idcategoria+"'";
        return  cargarNombreTag(query);
    }

    public EntidadTiposTags cargarNombreTag (String query){
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            EntidadTiposTags entidadTiposTags=getEntidadTiposTags(cursor);
            cursor.close();
            return  entidadTiposTags;
        }
        return  null;
    }

    private EntidadTiposTags getEntidadTiposTags(Cursor cursor){
        String tagTypeSysId = cursor.getString(cursor.getColumnIndex("IdTipoTag"));
        String code= cursor.getString(cursor.getColumnIndex("code"));
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String description = cursor.getString(cursor.getColumnIndex("description"));
        String category = cursor.getString(cursor.getColumnIndex("category"));

        EntidadTiposTags entidadtipoTags= new EntidadTiposTags(tagTypeSysId,code,name,description,category);
        return  entidadtipoTags;
    }


    //region
    public boolean ActualizarEmpleadoAsignado(String employeeRelatedSysId, ArrayList<AjustarActivoUbicacion> AjustarActivoUbicacionList){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (AjustarActivoUbicacion item: AjustarActivoUbicacionList){

                String query = "Update Activos Set EmployeeRelatedSysId = '"+employeeRelatedSysId+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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

    public boolean ActualizarTodosEmpleados(String employeeRelatedSysId, ArrayList<entidadaAjustarUbicacion> AjustarActivoUbicacionList){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (entidadaAjustarUbicacion item: AjustarActivoUbicacionList){

                String query = "Update Activos Set EmployeeRelatedSysId = '"+employeeRelatedSysId+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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

    public boolean ActualizarEstadoDelActivo(String assetStatusSysId, ArrayList<AjustarActivoUbicacion> AjustarActivoUbicacionList){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (AjustarActivoUbicacion item: AjustarActivoUbicacionList){

                String query = "Update Activos Set AssetStatusSysId = '"+assetStatusSysId+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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
    public boolean ActualizarTodoEstadoDelActivo(String assetStatusSysId, ArrayList<entidadaAjustarUbicacion> AjustarActivoUbicacionList){

        String userId = SharedPreferencesGetSet.leer_local("_userId", _context);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (entidadaAjustarUbicacion item: AjustarActivoUbicacionList){

                String query = "Update Activos Set AssetStatusSysId = '"+assetStatusSysId+"'," +
                        " SyncData = '1', UpdateUser = '"+userId+"' where _id ='"+item.getAssetSysId()+"'";
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



    //endregion


}
