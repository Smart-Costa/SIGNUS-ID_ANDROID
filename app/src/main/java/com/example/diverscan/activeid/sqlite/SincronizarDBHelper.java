package com.example.diverscan.activeid.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.Activo.EntidadActivos;
import com.example.diverscan.activeid.Activo.EntidadCategoriaActivos;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.Inventory.EntidadEdificios;
import com.example.diverscan.activeid.Inventory.EntidadOficina2;
import com.example.diverscan.activeid.Inventory.EntidadPisos;
import com.example.diverscan.activeid.Inventory.EntidadRazonSocial;
import com.example.diverscan.activeid.Inventory.EntidadTiposInventarios;
import com.example.diverscan.activeid.Inventory.EntidadUsuarios;
import com.example.diverscan.activeid.Inventory.Entidad_TomaDetalle;
import com.example.diverscan.activeid.Inventory.Entidad_TomaFisica;
import com.example.diverscan.activeid.Roles.EntidadDatosRol;
import com.example.diverscan.activeid.Tags.EntidadTags;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;

import java.util.ArrayList;
import java.util.List;

public class SincronizarDBHelper  extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;

    public SincronizarDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {}

    //****************************************************************************************************
    //Tabla TipoInventario
    public boolean InsertOrReplaceTipoInventario(ArrayList<EntidadTiposInventarios> tiposInventarios) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Verificar si las columnas 'fechaInicio', 'fechaFinal' y 'estado' existen
            Cursor cursor = db.rawQuery("PRAGMA table_info(TipoTomaInventario);", null);
            boolean fechaInicioExists = false;
            boolean fechaFinalExists = false;
            boolean estadoExists = false;

            while (cursor.moveToNext()) {
                String columnName = cursor.getString(cursor.getColumnIndex("name"));
                if (columnName.equals("fechaInicio")) {
                    fechaInicioExists = true;
                }
                if (columnName.equals("estado")) {
                    estadoExists = true;
                }
                if (columnName.equals("fechaFinal")) {
                    fechaFinalExists = true;
                }
            }
            cursor.close();

            // Si las columnas no existen, agregarlas
            if (!fechaInicioExists) {
                db.execSQL("ALTER TABLE TipoTomaInventario ADD COLUMN fechaInicio TEXT;");
            }
            if (!fechaFinalExists) {
                db.execSQL("ALTER TABLE TipoTomaInventario ADD COLUMN fechaFinal TEXT;");
            }
            if (!estadoExists) {
                db.execSQL("ALTER TABLE TipoTomaInventario ADD COLUMN estado TEXT;");
            }

            // Inserta o reemplaza los datos en la tabla
            for (EntidadTiposInventarios item : tiposInventarios) {
                String query = "INSERT OR REPLACE INTO TipoTomaInventario(_id, Nombre, Descripcion, fechaInicio, fechaFinal, estado) " +
                        "VALUES('" + item.getidTipoToma() + "','" + item.getnombreTipoToma() + "','" + item.getdescripcionTipoToma() + "'," +
                        "'" + item.getfechaInicio() + "','" + item.getfechaFinal() + "','" + item.getestado() + "')";
                db.execSQL(query);
            }

            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.w("myApp", "Error 22 " + ex.toString() + " " + ex.getStackTrace());
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceTomaDetalle(ArrayList<Entidad_TomaDetalle> tomaDetalles){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (Entidad_TomaDetalle item: tomaDetalles){
                String query="INSERT OR REPLACE INTO TomaFisicaDetalle(idTakeDetail, FK_TomaFisica,EPC,DateRead) " +
                        "VALUES('"+item.getIdTakeDetail()+"','"+item.getFk_TomaFisica()+"','"+item.getepc()+"','"+item.getDateRead()+"')";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.w("myApp", "Error 22" + ex.toString()+" "+ex.getStackTrace());
            return false;
        }finally {
            db.endTransaction();
            return true;
        }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceRazones(ArrayList<EntidadRazonSocial> razon){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {

            for (EntidadRazonSocial item: razon) {

                String query="INSERT OR REPLACE INTO RazonSocial(_id, Nombre) " +
                        "VALUES('"+item.getIdRazon()+"','"+item.getNombreRazon()+"')";
                db.execSQL(query);
            }

            db.setTransactionSuccessful();

        }catch (Exception ex){

            Log.w("myApp", "Error 22 " +ex.toString()+ " "+ex.getStackTrace());
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceEdificios(ArrayList<EntidadEdificios> edificio){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadEdificios item: edificio) {

                String query="INSERT OR REPLACE INTO Edificios(_id,Nombre, idRazonSocial, RazonSocial) " +
                        "VALUES('"+item.getIdEdificio()+"','"+item.getNombreEdificio()+"','"+item.getIdRazonSocial()+"','"
                +item.getRazonSocial()+"')";
                db.execSQL(query);
            }

            db.setTransactionSuccessful();

        }catch (Exception ex){

            Log.w("myApp","Error 22" +ex.toString()+ " " +ex.getStackTrace());
            return false;
        }finally{
            db.endTransaction();
            return true;
        }
    }

    //****************************************************************************************************

     public boolean InsertOrReplacePisos (ArrayList<EntidadPisos> piso){

         SQLiteDatabase db = this.getWritableDatabase();
         db.beginTransaction();

         try{
             for (EntidadPisos item: piso){

                 String query = "INSERT OR REPLACE INTO Pisos (_id, Nombre, idEdificio, Edificio) " +
                         "VALUES ('"+item.getIdPiso()+"','"+item.getNombrePiso()+"','"+item.getIdEdificio()+"','"
                 +item.getEdificio()+"')";
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

    //****************************************************************************************************

    public boolean InsertOrReplaceOficinas (ArrayList<EntidadOficina2> oficina){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadOficina2 item: oficina){

                String query = "INSERT OR REPLACE INTO Oficina (_id, Nombre, idPiso, Piso, Tag) " +
                        "VALUES ('"+item.getIdOficina()+"','"+item.getNombreOficina()+"','"+item.getIdPiso()+"','"
                        +item.getPiso()+"','"+item.getIdTag()+"')";
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

    //****************************************************************************************************

    public boolean InsertOrReplaceTags(ArrayList<EntidadTags> tags){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try{
            for(EntidadTags item: tags){
                String query = "INSERT OR REPLACE INTO Tags (_id, EPC, IdTipoTag)" +
                        "Values ('"+item.getTagSysId()+"','"+item.getTagID()+"','"+item.getTagTypeSysId()+"')";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.w("myApp", "Error 22" + ex.toString()+" "+ ex.getStackTrace());
            return false;
        }finally {
            db.endTransaction();
            return true;

        }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceTipoTags(ArrayList<EntidadTiposTags> tags){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try{
            for(EntidadTiposTags item: tags){
                String query = "INSERT OR REPLACE INTO tipoTags (IdTipoTag, code,  name, description, category)" +
                        "Values ('"+item.getTagTypeSysId()+"','"+item.getCode()+"','"+item.getName()+"','"+item.getDescription()+"','"+item.getCategory()+"')";
                db.execSQL(query);
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.w("myApp", "Error 22" + ex.toString()+" "+ ex.getStackTrace());
            return false;
        }finally {
            db.endTransaction();
            return true;

        }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceTomaFisica(ArrayList<Entidad_TomaFisica> tomafisicas){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for(Entidad_TomaFisica item: tomafisicas){
                String query = "INSERT OR REPLACE INTO Tomas(_id, TakeName, TakeDescription, TakeDate, TakeStatus) " +
                        "Values('"+item.getIdToma()+"','"+item.getTakeName()+"','"+item.getTakeDescription()+"','"+item.getTakeDate()+"','"+item.getTakeStatus()+"')";
                db.execSQL(query);
            }

            db.setTransactionSuccessful();
        }
        catch(Exception ex){
            Log.w("myApp", "Error 22" + ex.toString()+" "+ ex.getStackTrace());
            return false;
        }
        finally {
            db.endTransaction();
            return true;
        }
    }

    //****************************************************************************************************

    public Boolean doesRecordExist() {
        String q = "Select * FROM  Tomas";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(q, null);
        if (cursor.moveToFirst()) {
            return true;
        } else
            {
                return false;
            }
    }

    //****************************************************************************************************

    public boolean InsertOrReplaceActivos (ArrayList<EntidadActivos> activo){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadActivos item: activo){

                String query = "INSERT OR REPLACE INTO Activos (_id, Alias, Descripcion, Departamento," +
                        " Oficina, piso, edificio, Compania, Tag, Numero, CodeBar, IdOficina, IdEstante," +
                        " IdCategoria, IdPiso, IdEdificio, IdCompania, Marca, Modelo, Serial, parentAssetSysId,EmployeeRelatedSysId,AssetStatusSysId, AnoFabricacion, Capacidad, EstadoDescripcion, EstadoConservacion) " +
                        "VALUES ('"+item.getIdActivo()+"','"+item.getAlias().replaceAll("'", "''").replace("\"","''")+"','"+item.getDescripcion().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getDepartamento()+"','"+item.getOficina().replaceAll("'", "''").replace("\"","''")+"','"+item.getPiso()+"','"+item.getEdificio()+"','"
                        +item.getCompania()+"','"+item.getTag()+"','"+item.getNumero().replaceAll("'", "''").replace("\"","''")+"','"+item.getCodeBar().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getIdOficina()+"','"+item.getIdEstante()+"','"+item.getIdCategoria()+"','"+item.getIdPiso()+"','"
                        +item.getIdEdificio()+"','"+item.getIdCompania()+"','"+item.getMarca().replaceAll("'", "''").replace("\"","''")+"','"+item.getModelo().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getSerial().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getParentAssetSysId()+"','"+item.getEmployeeRelatedSysId()+"','"+item.getAssetStatusSysId()+"','"
                        +item.getAnoFabricacion().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getCapacidad().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getEstadoDescripcion().replaceAll("'", "''").replace("\"","''")+"','"
                        +item.getEstadoConservacion().replaceAll("'", "''").replace("\"","''")+"')";
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

    //****************************************************************************************************

    public boolean InsertOrReplaceCategoriaActivo (ArrayList<EntidadCategoriaActivos> categoriaActivos){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadCategoriaActivos item: categoriaActivos){

                String query = "INSERT OR REPLACE INTO categoriaActivos (assetCategorySysId, description, name)" +
                        "VALUES ('"+item.getAssetCategorySysId()+"','"+item.getDescription()+"','"+item.getName()+"')";
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

    //****************************************************************************************************

    public boolean InsertOrReplaceUsuarios (ArrayList<EntidadUsuarios> usuario){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try{
            for (EntidadUsuarios item: usuario){

                String query = "INSERT OR REPLACE INTO Users (_id, username, pass, email, bloqueado ," +
                        " aprobado , sesionActiva , contrasenaFallida , UltimaActividad , UltimoInicio ," +
                        " FechaBloqueo) " +
                        "VALUES ('"+item.getUserSysId()+"','"+item.getUserName()+"','"+item.getPassword()+"','"
                        +item.getEmail()+"','"+item.getBloqueado()+"','"+item.getAprobado()+"','"
                        +item.getSesionActiva()+"','"+item.getContrasenaFallida()+"','"+item.getUltimaActividad()+"','"
                        +item.getUltimoInicio()+"','"+item.getFechaBloqueo()+"')";
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

    //****************************************************************************************************

    public boolean InsertOrReplaceRolHH (ArrayList<EntidadDatosRol> Rol){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        int i = 1;
        try{
            for (EntidadDatosRol item: Rol){

                String query = "INSERT OR REPLACE INTO RolHH (_idRol, page, username,description,UserSysId, Esta_Bloqueado) " +
                        "VALUES ('"+item.get_idRol()+"','"+item.getPage()+"','"+item.getUsername()+"','"
                        +item.getDescription()+"','"+item.getUserSysId()+"','"+item.getEstaBloqueado()+"')";
                db.execSQL(query);
                i++;
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

    //****************************************************************************************************

    public boolean InsertOrReplaceAssetStatus (ArrayList<EntidadAssetStatus> assetStatusList){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        int i = 1;
        try{
            for (EntidadAssetStatus item: assetStatusList){

                String query = "INSERT OR REPLACE INTO AssetStatus (_id, Name,Description) " +
                        "VALUES ('"+item.getId()
                        +"','"+item.getName().replaceAll("'", "''").replace("\"","''")
                        +"','"+item.getDescription().replaceAll("'", "''").replace("\"","''")+"')";
                db.execSQL(query);
                i++;
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

    //****************************************************************************************************

    public boolean InsertOrReplaceEmployees (ArrayList<EntidadEmployees> employeesList){

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        int i = 1;
        try{
            for (EntidadEmployees item: employeesList){

                String query = "INSERT OR REPLACE INTO Employees (EmployeeSysId, Name,LastName,Id,CompanyIdExtern) " +
                        "VALUES ('"+item.getEmployeeSysId()
                        +"','"+item.getName().replaceAll("'", "''").replace("\"","''")
                        +"','"+item.getLastName().replaceAll("'", "''").replace("\"","''")
                        +"','"+item.getId()
                        +"','"+item.getCompanyIdExtern()+"')";
                db.execSQL(query);
                i++;
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
