package com.example.diverscan.activeid.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.TomasFisicas.TomasFisias;
import com.example.diverscan.activeid.Assign_tag_Offices.sincronizarTag;
import com.example.diverscan.activeid.Inventory.EUbicacionActivo;
import com.example.diverscan.activeid.Inventory.EntidadTomaFisicaEPC;
import com.example.diverscan.activeid.Inventory.EntidadTomaFisicaManual;
import com.example.diverscan.activeid.Oficina.ActualizarUbicacionActivo;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OfficesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 9;

    public OfficesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       /* //
        db.execSQL("CREATE TABLE if not exists Oficina (_id Text PRIMARY KEY, Nombre Text, Piso Text, Tag Text, Nuevo Text, sinc Text)");
        //
        db.execSQL("CREATE TABLE if not exists Pisos ( _id Text PRIMARY KEY, Nombre Text, Edificio Text)");
        //
        db.execSQL("CREATE TABLE if not exists Edificios ( _id Text PRIMARY KEY, Nombre Text, RazonSocial Text)");
        //
        db.execSQL("CREATE TABLE if not exists RazonSocial ( _id Text PRIMARY KEY, Nombre Text )");
        //
        db.execSQL("CREATE TABLE if not exists OfficesXTags (_id Text PRIMARY KEY, officeSysId Text, tagSysId Text, Sync Text)");*/

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

      /*  db.execSQL("DROP TABLE IF EXISTS Oficina");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Pisos");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Edificios");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS RazonSocial");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS OfficesXTags");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Tags");
        onCreate(db);*/
    }

    public Cursor TraerSectores (String NombreSector){

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select o.Nombre AS oficinaNombre, o.Piso as Piso,o._id As idOficina, p._id, p.Nombre as pisoNombre, e._id, e.Nombre as edificioNombre," +
                    "r._id, r.Nombre as razonNombre, t.EPC " +
                    "from Oficina o Inner Join Pisos p ON o.idPiso = p._id " +
                    "Inner Join Edificios e ON p.idEdificio = e._id " +
                    "Inner Join RazonSocial r ON e.idRazonSocial = r._id " +
                    "Inner Join Tags t ON o.Tag = t._id " +
                    "where o.Piso Like " + "'%" + NombreSector + "%' OR oficinaNombre Like " + "'%" + NombreSector + "%'";


         /*   "LEFT JOIN OfficesXTags ot ON o._id = ot.officeSysId " +
                    "LEFT OUTER JOIN Tags t ON ot.tagSysId = t._id " +*/

            return db.rawQuery(query, null);

        }catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public EntidadTomaFisicaEPC CargarSector(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            String id  = cursor.getString(cursor.getColumnIndex("_id"));
            String nombreOficina = cursor.getString(cursor.getColumnIndex("Nombre"));
            EntidadTomaFisicaEPC entidadTomaFisicaEPC = new EntidadTomaFisicaEPC(nombreOficina, id);
            cursor.close();
            return entidadTomaFisicaEPC;
        }
        return null;
    }
    public boolean ActivosEnUbicacion(String idSector){
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag from Oficina o " +
                    " Inner Join Activos a ON a.IdOficina = o._id " +
                    " where o._id = '" + idSector + "'";
            Cursor activosUbicacion = db.rawQuery(query, null);
            if(activosUbicacion.getCount() != 0){
                return true;
            }else{
                return false;
            }
        }catch(Exception ex){
            Log.e("Eleccion Toma Fisica", ex.getMessage());
            return false;
        }
    }
    public EUbicacionActivo VerSectorEPC2(String EPC) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o._id AS IdOficina, o.Nombre AS NombreOficina, p._id AS IdPiso ,p.Nombre AS NombrePiso, \n" +
                "e._id AS IdEdificio, e.Nombre AS NombreEdificio, " +
                "c._id AS IdRazonSocial, c.Nombre AS NombreRazonSocial from Oficina o " +
                "inner join Tags t ON o.Tag = t._id " +
                "inner join Pisos p ON o.idPiso = p._id " +
                "inner join Edificios e ON p.idEdificio = e._id " +
                "inner join RazonSocial c ON e.idRazonSocial = c._id" +
                " where t.EPC ='" + EPC + "'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            return new EUbicacionActivo(
                    cursor.getString(cursor.getColumnIndex("IdOficina")),
                    cursor.getString(cursor.getColumnIndex("NombreOficina")),
                    cursor.getString(cursor.getColumnIndex("IdPiso")),
                    cursor.getString(cursor.getColumnIndex("NombrePiso")),
                    cursor.getString(cursor.getColumnIndex("IdEdificio")),
                    cursor.getString(cursor.getColumnIndex("NombreEdificio")),
                    cursor.getString(cursor.getColumnIndex("IdRazonSocial")),
                    cursor.getString(cursor.getColumnIndex("NombreRazonSocial")));
        }
        return null;
    }

    public Map<Integer, oficinaNuevo> ObtenerOficinaPorPisoDescripcion3(String idPiso, String descripcion) {
        Map<Integer, oficinaNuevo> mapOficinas = new HashMap<Integer, oficinaNuevo>();
        String query = "Select * from Oficina where idPiso = '" + idPiso + "' and Nombre like " + "'%" + descripcion + "%' or  Piso like " + "'%" + descripcion + "%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor oficinasCursor = db.rawQuery(query, null);
        int cantidadDatos = oficinasCursor.getCount();
        oficinasCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos; i++) {
            String idOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("_id"));
            String nombreOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("Nombre"));
            String IdOficina = oficinasCursor.getString(oficinasCursor.getColumnIndex("idPiso"));
            String idExterno = oficinasCursor.getString(oficinasCursor.getColumnIndex("Piso"));
            oficinaNuevo oficinanuevo = new oficinaNuevo(idOficina, idExterno + " - " + nombreOficina, IdOficina);
            mapOficinas.put(i, oficinanuevo);
            oficinasCursor.moveToNext();
        }
        return mapOficinas;
    }
    public ArrayList<TomasFisias> ActivosUbicacion(String Sector){
        String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag, " +
                "p._id as IdPiso, e._id as IdEdificio, r._id as IdCompania " +
                "from Oficina o " +
                " Inner Join Activos a ON a.IdOficina = o._id " +
                " Left Join Pisos p ON o.idPiso = p._id " +
                " Left Join Edificios e ON p.idEdificio = e._id " +
                " Left Join RazonSocial r ON e.idRazonSocial = r._id " +
                " where o._id = '"+ Sector + "'";
        return CargarActivosSector(query);

    }

    public ArrayList<TomasFisias> ActivosUbicacionEPC(String Sector, String EPC){
        String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag, " +
                "p._id as IdPiso, e._id as IdEdificio, r._id as IdCompania " +
                "from Oficina o " +
                " Inner Join Activos a ON a.IdOficina = o._id " +
                " inner join Tags t ON o.Tag = t._id  " +
                " Left Join Pisos p ON o.idPiso = p._id " +
                " Left Join Edificios e ON p.idEdificio = e._id " +
                " Left Join RazonSocial r ON e.idRazonSocial = r._id " +
                " where o._id = '"+ Sector + "' AND t.EPC = '"+EPC+"' OR o.EPC = '"+EPC+"'";
        return CargarActivosSector(query);
    }

    public ArrayList<ActualizarUbicacionActivo> UbicacionCompleta(String IdOficina){
        String query = "Select o._id idOficina, o.Nombre oficinaNombre, p._id idPiso, p.Nombre pisoNombre " +
                ", e._id idEdificio " +
                ", e.Nombre edificioNombre" +
                ", c._id IdCompania " +
                ", c.Nombre companiaNombre " +
                "from Oficina o " +
                "inner join Pisos p  on o.idPiso = p._id " +
                "inner join Edificios e on p.idEdificio = e._id " +
                "inner join RazonSocial c on e.idRazonSocial = c._id " +
                "where o._id = '"+IdOficina+"' ";
        return CargarOficina(query);
    }

    public ArrayList<ActualizarUbicacionActivo> CargarOficina(String query){
        ArrayList<ActualizarUbicacionActivo> listaSector = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int cantidadSector = cursor.getCount();
        cursor.moveToFirst();
        for(int i= 0; i < cantidadSector; i++ ){
            ActualizarUbicacionActivo actualizarUbicacionActivo = getUbicacion(cursor);
            listaSector.add(actualizarUbicacionActivo);
            cursor.moveToNext();
        }
        return listaSector;
    }

    private ActualizarUbicacionActivo getUbicacion(Cursor cursor){
        String IdOficina = cursor.getString(cursor.getColumnIndex("idOficina"));
        String Oficina= cursor.getString(cursor.getColumnIndex("oficinaNombre"));
        String IdPiso= cursor.getString(cursor.getColumnIndex("idPiso"));
        String Piso= cursor.getString(cursor.getColumnIndex("pisoNombre"));
        String IdEdificio= cursor.getString(cursor.getColumnIndex("idEdificio"));
        String Edificio= cursor.getString(cursor.getColumnIndex("edificioNombre"));
        String IdCompania= cursor.getString(cursor.getColumnIndex("IdCompania"));
        String Compania= cursor.getString(cursor.getColumnIndex("companiaNombre"));

        ActualizarUbicacionActivo actualizarUbicacionActivo = new ActualizarUbicacionActivo( Compania,IdCompania,Edificio,IdEdificio, Piso,IdPiso,
                Oficina,IdOficina);
        return actualizarUbicacionActivo;
    }

    public ArrayList<TomasFisias> ActivosUbicacionID(String Sector){
        String query = "Select a._id, a.CodeBar, a.Descripcion, a.IdOficina, o.Nombre, a.Tag, " +
                "p._id as IdPiso, e._id as IdEdificio, r._id as IdCompania " +
                "from Oficina o " +
                " Inner Join Activos a ON a.IdOficina = o._id " +
                " Left Join Pisos p ON o.idPiso = p._id " +
                " Left Join Edificios e ON p.idEdificio = e._id " +
                " Left Join RazonSocial r ON e.idRazonSocial = r._id " +
                " where a.IdOficina like '%"+ Sector + "%'";
        return CargarActivosSector(query);
    }

    public ArrayList<TomasFisias> CargarActivosSector(String query){
        ArrayList<TomasFisias> listaInventario = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int cantidadInventario = cursor.getCount();
        cursor.moveToFirst();
        for(int i = 0; i < cantidadInventario; i++){

            TomasFisias inventarioVisual = getInventarioVisual(cursor);
            listaInventario.add(inventarioVisual);
            cursor.moveToNext();

        }
        return listaInventario;
    }

    private TomasFisias getInventarioVisual (Cursor cursor){

        String AssetSysId= cursor.getString(cursor.getColumnIndex("_id"));
        String Numero = cursor.getString(cursor.getColumnIndex("CodeBar"));
        String Descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
        String IdOficina = cursor.getString(cursor.getColumnIndex("IdOficina"));
        String Oficina = cursor.getString(cursor.getColumnIndex("Nombre"));
        String EPC = cursor.getString(cursor.getColumnIndex("Tag"));

        // Nuevos campos
        String IdPiso = cursor.getColumnIndex("IdPiso") != -1 ? cursor.getString(cursor.getColumnIndex("IdPiso")) : "";
        String IdEdificio = cursor.getColumnIndex("IdEdificio") != -1 ? cursor.getString(cursor.getColumnIndex("IdEdificio")) : "";
        String IdCompania = cursor.getColumnIndex("IdCompania") != -1 ? cursor.getString(cursor.getColumnIndex("IdCompania")) : "";

        TomasFisias inventarioVisual = new TomasFisias(Numero, Descripcion,"",  EPC, AssetSysId,Oficina, IdOficina, IdPiso, IdEdificio, IdCompania );
        return inventarioVisual;
    }

    public EntidadTomaFisicaEPC VerSectorEPC(String EPC){
        String query="Select o._id, o.Nombre  from Oficina o " +
                "inner join Tags t ON o.Tag = t._id  where o.EPC ='"+EPC+"' or t.EPC ='"+EPC+"'";
        return CargarSector(query);
    }

    public EntidadTomaFisicaEPC VerSectorID(String EPC){
        String query="Select o._id, o.Nombre  from Oficina o " +
                "inner join Tags t ON o.Tag = t._id  where t.EPC ='"+EPC+"'";
        return CargarSector(query);
    }

    public EntidadTomaFisicaManual cargarEPC(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            String epc = cursor.getString(cursor.getColumnIndex("EPC"));
            EntidadTomaFisicaManual entidadTomaFisicaManual = new EntidadTomaFisicaManual(epc);
            cursor.close();
            return entidadTomaFisicaManual;
        }
        return null;
    }


    public String NombreSectorActualiza(String idOficina){
        String query="Select Nombre from Oficina where _id ='"+idOficina+"'";
        return cargarSector(query);
    }

    public String cargarSector(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            String nombreOficina = cursor.getString(cursor.getColumnIndex("Nombre"));

            cursor.close();
            return nombreOficina;
        }
        return null;
    }

    public boolean ActualizarEPC(String IdOficina, String tagId){
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "Update Oficina Set EPC ="+"'"+tagId+"' ,sinc = '1' " +
                    "where _id ="+"'"+IdOficina+"'" ;
            db.execSQL(query);
            return true;
        }catch(Exception ex){
            ex.toString();
        }
        return false;
    }

    // Listar las Razones Sociales
    public Cursor listarRazonSocial (){

        try{

            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select R._id as id, R.Nombre as NombreRazon from RazonSocial as R";
            return db.rawQuery(query, null);

        } catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    // Listar los edificios por la Razon Social
    public Cursor listarEdificioporRazon (int id){

    try{
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "Select E._id as id, E.Nombre as NombreEdificio, E.RazonSocial as idRazon from Edificios as  E " +
                       "Where E.RazonSocial = '"+id+"'";

        return db.rawQuery(query, null);

        } catch (Exception ex){
            ex.toString();
            return null;
        }
    }

    //
    public Cursor listarPisoporEdificio (int id){
        try{

            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select P._id as id, P.Nombre as NombrePiso from Pisos as  P " +
                           "Where P.Edificio = '"+id+"'";

            return db.rawQuery(query, null);

        } catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public Cursor listarOficinaporPiso (int id){

        try{

            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select O._id as id, O.Nombre as NombreOficina from Oficina as  O " +
                    "Where O.Piso = '"+id+"'";

            return db.rawQuery(query, null);

        } catch (Exception ex){

            ex.toString();
            return null;
        }
    }

    public ArrayList<sincronizarTag> IngresarSectorSync(){

        ArrayList<sincronizarTag> listTag = new ArrayList<>();
        String query = "Select * From Oficina Where sinc = '1'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor tagCursor = db.rawQuery(query, null);
        int cantidadDatos = tagCursor.getCount();
        tagCursor.moveToFirst();
        for(int i = 0; i< cantidadDatos; i++){
        String tagId = tagCursor.getString(tagCursor.getColumnIndex("EPC"));
        String officeSysId = tagCursor.getString(tagCursor.getColumnIndex("_id"));
        String oficinaNombre = tagCursor.getString(tagCursor.getColumnIndex("Nombre"));

        sincronizarTag tagRecord = new sincronizarTag(tagId, officeSysId, oficinaNombre);
        listTag.add(tagRecord);
        tagCursor.moveToNext();
        }

        return listTag;
    }

    public boolean tagSync(ArrayList<sincronizarTag> listActivos){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for(sincronizarTag entidadTags : listActivos) {
                String query = "Update Oficina Set sinc = '0' where _id ='"+entidadTags.getOfficeSysId()+"'";
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

