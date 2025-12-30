package com.example.diverscan.activeid.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.Tags.EntidadClasificacionTags;
import com.example.diverscan.activeid.Tags.EntidadClasificarTag;
import com.example.diverscan.activeid.Tags.EntidadTagInventariados;
import com.example.diverscan.activeid.Tags.EntidadTags;
import com.example.diverscan.activeid.Tags.EntidadTagsClasificados;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 8;

    public TagsDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public EntidadTiposTags NombreTipoTag (String Idcategoria){
        String query="Select a.IdTipoTag, a.code, a.name, a.description, a.category from tipoTags a inner join Tags t on a.IdTipoTag = t.IdTipoTag where t.EPC ='"+Idcategoria+"'";
        return  cargarNombreTag(query);
    }

    public Boolean doesRecordExist() {
        String q = "Select * FROM  Tags";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(q, null);
        if (cursor.moveToFirst()) {
            return true;
        } else
        {
            return false;
        }
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

    public ArrayList<EntidadTagsClasificados> ObtenerTagsTipos(){
        ArrayList<EntidadTagsClasificados> tagsClasificados = new ArrayList<EntidadTagsClasificados>();
        String query = "Select t._id, t.EPC, c.IdTipoTag As IdTipoTag, c.name As name from Tags t inner join tipoTags c on t.IdTipoTag = c.IdTipoTag";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor TagCursor = db.rawQuery(query, null);
        int cantidadTags = TagCursor.getCount();
        TagCursor.moveToFirst();
        for(int i = 0; i < cantidadTags; i++){
            String idTag = TagCursor.getString(TagCursor.getColumnIndex("_id"));
            String epc   = TagCursor.getString(TagCursor.getColumnIndex("EPC"));
            String IdTipoTag = TagCursor.getString(TagCursor.getColumnIndex("IdTipoTag"));
            String name = TagCursor.getString(TagCursor.getColumnIndex("name"));

            EntidadTagsClasificados entidadTagsClasificados = new EntidadTagsClasificados(idTag, epc, IdTipoTag, name,"");
            tagsClasificados.add(entidadTagsClasificados);
            TagCursor.moveToNext();
        }
        return tagsClasificados;
    }

    public EntidadTagInventariados ObtenerInventarioTag(String epc){

        String query = "Select t._id, t.EPC, c.IdTipoTag As IdTipoTag, c.name As name from Tags t inner join tipoTags c on t.IdTipoTag = c.IdTipoTag where t.EPC='"+epc+"'";

        return CargarTagsInventariados(query);
    }


    public EntidadTagInventariados CargarTagsInventariados (String query){
        SQLiteDatabase db = this.getReadableDatabase(); // el login ?  podemos empezar de nuevo pero tambien debugueando lo del login ?? oki ve
        Cursor cursor=  db.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            EntidadTagInventariados entidadActivos=getEntidadTagInventariados(cursor);
            cursor.close();
            return  entidadActivos;
        }
        return  null;
    }

    private EntidadTagInventariados getEntidadTagInventariados(Cursor TagCursor){
        String idTag = TagCursor.getString(TagCursor.getColumnIndex("_id"));
        String epc   = TagCursor.getString(TagCursor.getColumnIndex("EPC"));
        String IdTipoTag = TagCursor.getString(TagCursor.getColumnIndex("IdTipoTag"));
        String name = TagCursor.getString(TagCursor.getColumnIndex("name"));
        EntidadTagInventariados entidadActivos= new EntidadTagInventariados(idTag, epc, IdTipoTag, name);
        return  entidadActivos;
    }

    public Map<Integer, EntidadClasificarTag> ObtenerTipoTags (){

        Map<Integer, EntidadClasificarTag> mapTipoTag = new HashMap<Integer, EntidadClasificarTag>();
        String query="Select * from tipoTags";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor TagCursor = db.rawQuery(query, null);
        int cantidadDatos = TagCursor.getCount();
        TagCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String tagTypeSysId = TagCursor.getString(TagCursor.getColumnIndex("IdTipoTag"));
            String name =  TagCursor.getString(TagCursor.getColumnIndex("name"));


            EntidadClasificarTag entidadClasificarTag = new EntidadClasificarTag(tagTypeSysId,name);
            mapTipoTag.put(i, entidadClasificarTag);
            TagCursor.moveToNext();

        }
        return mapTipoTag;

    }

    public ArrayList<EntidadTags> ObtenerTagsClasificadosSync(){

        ArrayList<EntidadTags> listTag = new ArrayList<>();
        String query = "Select * From Tags Where sinc = '1'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor tagCursor = db.rawQuery(query, null);
        int cantidadDatos = tagCursor.getCount();
        tagCursor.moveToFirst();
        for(int i = 0; i< cantidadDatos; i++){
            String idTag = tagCursor.getString(tagCursor.getColumnIndex("_id"));
            String epc   = tagCursor.getString(tagCursor.getColumnIndex("EPC"));
            String IdTipoTag = tagCursor.getString(tagCursor.getColumnIndex("IdTipoTag"));

            EntidadTags tagRecord = new EntidadTags(idTag, epc, IdTipoTag);
            listTag.add(tagRecord);
            tagCursor.moveToNext();
        }

        return listTag;
    }
    public boolean ReclasificarTags (String IdTipoTag, ArrayList<EntidadClasificacionTags> entidadaAjustarUbicacions){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadClasificacionTags item: entidadaAjustarUbicacions){
                String query = "Update Tags Set IdTipoTag = '"+IdTipoTag+"'," +
                        " sinc = '1' where _id ='"+item.getTagSysId()+"'";
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

    public boolean tagSync(ArrayList<EntidadTags> listaSincronizada){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for(EntidadTags entidadTags : listaSincronizada) {
                String query = "Update Tags Set sinc = '0' where _id ='"+entidadTags.getTagSysId()+"'";
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
