package com.example.diverscan.activeid.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.Inventory.EntidadDetalleInventario;
import com.example.diverscan.activeid.Inventory.EntidadInventario;
import com.example.diverscan.activeid.Inventory.EntidadTiposInventarios;
import com.example.diverscan.activeid.Inventory.EntidadTomasInventario;
import com.example.diverscan.activeid.Inventory.Entidad_TomaDetalle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InventoryDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {}

    public Cursor ObtenerTomasDelInventario(){
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "Select IdTomasDelInventario, Fecha, Usuario, Oficina, ID_TipoInventario from TomasDelInventario Where Sync = '1'";

            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public Cursor ObtenerInventario(){
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "Select idInventory, Numero, Leidos, Total, Encontrados,Faltantes,Sobrantes,Fecha,IdTomaInventario,Marca,Metodo from Inventario Where Sync = '1'";

            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public Map<Integer, EntidadTiposInventarios> ObtenerTiposInventario() {
        Map<Integer, EntidadTiposInventarios> tiposInventariosMap = new HashMap<>();

        String query = "SELECT _id, Nombre, Descripcion FROM TipoTomaInventario";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        try {
            if (c == null) return tiposInventariosMap;

            // Calcula los índices UNA sola vez y lanza excepción si no existen
            final int colId          = c.getColumnIndexOrThrow("_id");
            final int colNombre      = c.getColumnIndexOrThrow("Nombre");
            final int colDescripcion = c.getColumnIndexOrThrow("Descripcion");

            int i = 0;
            while (c.moveToNext()) {
                String idTipoInventario = c.getString(colId);
                String nombreTipo       = c.isNull(colNombre) ? "" : c.getString(colNombre).trim();
                String descripcionTipo  = c.isNull(colDescripcion) ? "" : c.getString(colDescripcion).trim();

                EntidadTiposInventarios item =
                        new EntidadTiposInventarios(idTipoInventario, nombreTipo, descripcionTipo);

                tiposInventariosMap.put(i++, item);
            }
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }

        return tiposInventariosMap;
    }


    public Cursor ObtenerTomasInventario(){
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "Select _id, TakeName, TakeDescription, TakeDate, TakeStatus from Tomas Where TakeStatus = 'True'";

            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public Cursor ObtenerTomaFisicaDetalle(){
        try
        {
            String query = "Select idTakeDetail,FK_TomaFisica, EPC, DateRead  from TomaFisicaDetalle Where Sync='1'";
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public Cursor ObtenerDetalleInventario(){
        try
        {
            String query = "Select IdDetalleInventario, FK_idInventory, NumeroActivo, Descripcion, EPC, EstadoActivo, Excluido  from DetalleInventario Where Sync='1'";
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean ActualizarTomaDetalleSync(ArrayList<Entidad_TomaDetalle> listTomaDetalle){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try
        {
            for(Entidad_TomaDetalle entidadTags : listTomaDetalle) {
                String query = "Update TomaFisicaDetalle Set Sync = '0' where idTakeDetail = '"+entidadTags.getIdTakeDetail()+"'";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }

    public boolean ActualizarDetalleInventarioSync(ArrayList<EntidadDetalleInventario> listDetalleInventario){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try
        {
            for(EntidadDetalleInventario entidadTags : listDetalleInventario) {
                String query = "Update DetalleInventario Set Sync = '0' where IdDetalleInventario = '"+entidadTags.getId()+"'";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }

    public boolean ActualizarInventarioSync(ArrayList<EntidadInventario> listInventario ){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try
        {
            for(EntidadInventario entidadTags : listInventario) {
                String query = "Update Inventario Set Sync = '0' where idInventory = '"+entidadTags.getId()+"'";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }

    public boolean ActualizarTomaDelInventarioSync(ArrayList<EntidadTomasInventario> listTomasInventario){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try
        {
            for(EntidadTomasInventario entidadTags : listTomasInventario) {
                String query = "Update TomasDelInventario Set Sync = '0' where IdTomasDelInventario = '"+entidadTags.getIdTakeInventory()+"'";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;

        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }

    //Tomas físicas
    public Cursor VerTomasFisicas (){
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query ="Select _id, Nombre, Descripcion, fechaInicio, estado, fechaFinal from TipoTomaInventario WHERE estado = 1 OR estado = 3 " +
                    "ORDER BY fechaInicio ASC";

             return db.rawQuery(query, null);
         }
        catch (Exception ex)
        {
            ex.toString();
            return null;
        }
    }

    public Cursor ListarTipoInventario (){
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();

            String query = "Select I._id as id, I.Nombre as NombreTipo from TipoTomaInventario as I";
            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.toString();
            return null;
        }
    }

    public boolean InsertOrReplaceDetalleInventario(ArrayList<EntidadDetalleInventario> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (EntidadDetalleInventario item : items) {
                ContentValues v = new ContentValues();
                v.put("IdDetalleInventario", item.getId());
                v.put("FK_idInventory",      item.getIdInventario());
                v.put("NumeroActivo",        item.getNumeroActivo());
                v.put("Descripcion",         item.getDescripcion());
                v.put("EPC",                 item.getEPC());
                v.put("EstadoActivo",        item.getEstado());
                v.put("Excluido",            item.getExcluido());
                v.put("Sync",                1); // ← CLAVE

                db.insertWithOnConflict("DetalleInventario", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            Log.w("myApp", "InsertOrReplaceDetalleInventario error: " + ex.toString(), ex);
            return false;
        } finally {
            db.endTransaction();
        }
    }


    public boolean InsertOrReplaceTomaInventario(EntidadTomasInventario item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("IdTomasDelInventario", item.getIdTakeInventory());
            v.put("Fecha",                item.getDateTakeInventory()); // ya le pasamos ISO
            v.put("Usuario",              item.getUsuario());
            v.put("Oficina",              item.getOficina());
            v.put("ID_TipoInventario",    item.getTiposDeInventario());
            v.put("Sync",                 1); // ← CLAVE

            db.insertWithOnConflict("TomasDelInventario", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            Log.w("myApp", "InsertOrReplaceTomaInventario error: " + ex.toString(), ex);
            return false;
        } finally {
            db.endTransaction();
        }
    }



    public boolean InsertOrReplaceInventario(ArrayList<EntidadInventario> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (EntidadInventario item : items) {
                ContentValues v = new ContentValues();
                v.put("idInventory",       item.getId());
                v.put("Numero",            item.getNumero());
                v.put("Leidos",            item.getLeidos());
                v.put("Total",             item.getTotal());
                v.put("Encontrados",       item.getEncontrados());
                v.put("Faltantes",         item.getFaltantes());
                v.put("Sobrantes",         item.getSobrantes());
                v.put("Fecha",             item.getFecha());
                v.put("IdTomaInventario",  item.getIdTomasdeInventario());
                v.put("Marca",             "1");      // como tu implementación original
                v.put("Metodo",            "RFID");   // idem
                v.put("Sync",              1);        // ← CLAVE: pendiente de sincronizar

                db.insertWithOnConflict("Inventario", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            Log.w("myApp", "InsertOrReplaceInventario error: " + ex.toString(), ex);
            return false;
        } finally {
            db.endTransaction();
        }
    }


    public boolean InsertarTomaDetalle(String idTakeDetail,String Fk_Detalle, String Epc, String fecha){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("idTakeDetail", idTakeDetail);
            contentValues.put("FK_TomaFisica", Fk_Detalle);
            contentValues.put("EPC", Epc);
            contentValues.put("DateRead", fecha);
            contentValues.put("Sync", "1");
            db.insert("TomaFisicaDetalle", null, contentValues);
            db.setTransactionSuccessful();
            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            ex.toString();
            return false;
        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }
}
