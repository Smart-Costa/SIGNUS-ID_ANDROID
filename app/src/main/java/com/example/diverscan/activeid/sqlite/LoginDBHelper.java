package com.example.diverscan.activeid.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.Inventory.EntidadUsuarios;
import com.example.diverscan.activeid.Roles.EntidadDatosRol;

import java.util.ArrayList;

public class LoginDBHelper extends SQLiteOpenHelper
{

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;
    int i = 1;
    public LoginDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //crea sqlite
        try {

            db.execSQL("CREATE TABLE if not exists Users (_id text PRIMARY KEY, username Text, pass Text, email Text, bloqueado Text," +
                    " aprobado Text, sesionActiva Text, contrasenaFallida Text, UltimaActividad Text, UltimoInicio Text," +
                    " FechaBloqueo Text, Sync Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Users_id ON Users (_id)"); //cada tabla Necesita el index sino no reemplaza y inserta duplicados

            db.execSQL("CREATE TABLE if not exists Oficina (_id Text PRIMARY KEY, Nombre Text, idPiso Text, Piso Text, Tag Text, EPC Text, Nuevo Text, sinc Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Oficina_id ON Oficina (_id)");

            db.execSQL("CREATE TABLE if not exists Pisos (_id Text PRIMARY KEY, Nombre Text, idEdificio Text, Edificio Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Pisos_id ON Pisos (_id)");

            db.execSQL("CREATE TABLE if not exists Edificios (_id Text PRIMARY KEY, Nombre Text, idRazonSocial Text, RazonSocial Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Edificios_id ON Edificios (_id)");

            db.execSQL("CREATE TABLE if not exists RazonSocial (_id Text PRIMARY KEY, Nombre Text )");
            db.execSQL("CREATE UNIQUE INDEX idx_RazonSocial_id ON RazonSocial (_id)");

            db.execSQL("CREATE TABLE if not exists OfficesXTags (_id Text PRIMARY KEY, officeSysId Text, tagSysId Text, Sync Text)");

            db.execSQL("CREATE TABLE if not exists Tags (_id Text PRIMARY KEY, EPC Text,  " +
                       "IdTipoTag Text,  entryDate Text, " +
                       "sinc Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Tags_id ON Tags (_id)");

            db.execSQL("CREATE TABLE if not exists tipoTags (_id Text PRIMARY KEY, IdTipoTag Text,  " +
                    "code Text,  name Text, description Text, category Text, " +
                    "sinc Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_tipoTags_id ON tipoTags (IdTipoTag)");

            db.execSQL("CREATE TABLE if not exists categoriaActivos (_id Text PRIMARY KEY, assetCategorySysId Text, " +
                    "description Text,  name Text, " +
                    "sinc Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_categoriaActivos_id ON categoriaActivos (assetCategorySysId)");

            db.execSQL("CREATE TABLE Inventario (idInventory TEXT PRIMARY KEY, Numero TEXT, Leidos TEXT, Total TEXT, Encontrados TEXT, Faltantes TEXT, " +
                    "Sobrantes	TEXT, " +
                    "Fecha	TEXT, " +
                    "IdTomaInventario	TEXT, " +
                    "Marca	TEXT, " +
                    "Metodo	TEXT, " +
                    "Sync	TEXT) ");


            db.execSQL("CREATE TABLE if not exists TomasDelInventario (IdTomasDelInventario Text PRIMARY KEY, Fecha Text, Usuario Text, Oficina Text, " +
                    "ID_TipoInventario Text, Sync Text)");

            db.execSQL("CREATE TABLE if not exists DetalleInventario (IdDetalleInventario Text PRIMARY KEY, FK_idInventory Text, NumeroActivo Text, " +
                    "Descripcion Text, EPC Text, EstadoActivo Text, " +
                    " Excluido Text, Sync Text)");

            db.execSQL("CREATE TABLE if not exists Tomas (_id Text PRIMARY KEY, TakeName Text, " +
                    "TakeDescription Text, TakeDate Text, TakeStatus Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Tomas_id ON Tomas (_id)");

            db.execSQL("CREATE TABLE if not exists TipoTomaInventario (_id Text PRIMARY KEY, " +
                    "Nombre Text, Descripcion Text)");


            db.execSQL("CREATE TABLE if not exists TomaFisicaDetalle (idTakeDetail Text Primary Key, FK_TomaFisica Text, EPC Text, " +
                    "DateRead Text, Sync Text)");

            db.execSQL("CREATE TABLE if not exists Activos (_id text PRIMARY KEY, Alias Text, Descripcion Text, Departamento Text, Oficina Text, Piso Text, " +
                       "Edificio Text, Compania Text, Tag Text, Numero Text, CodeBar Text, IdOficina Text, Sinc Text, IdEstante Text, IdCategoria Text, " +
                       "IdPiso Text, IdEdificio Text, IdCompania Text, Marca Text, Modelo Text, Serial Text, SyncData Text, UpdateUser Text, parentAssetSysId Text,"+
                       "EmployeeRelatedSysId Text , AssetStatusSysId Text, AnoFabricacion Text, Capacidad Text, EstadoDescripcion Text, EstadoConservacion Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Activos_id ON Activos (_id)");

            db.execSQL("CREATE TABLE if not exists RolHH (_idRol Text, Description text, Page Text, Username Text, UserSysId Text, Esta_Bloqueado Text)");

            db.execSQL("CREATE TABLE if not exists NewAssets (assetId INTEGER PRIMARY KEY AUTOINCREMENT, Numero Text, CodeBar Text, Descripcion Text,IdCompania Text" +
                    ",Compania Text, IdEdificio Text, Edificio Text, IdPiso Text,Piso Text, IdOficina Text, Oficina Text, employeeName Text ,employeeRelated Text," +
                    " Marca Text,  Modelo Text, Serial Text, Tag Text, parentAssetSysId Text, assetStatusSysId Text, entryUser Text,syncData Text, " +
                    "EstadoDescripcion Text, EstadoConservacion Text, AnoFabricacion Text, Capacidad Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_NewAssets_id ON NewAssets (CodeBar)");

            db.execSQL("CREATE TABLE if not exists AssetStatus (_id Text PRIMARY KEY, Name Text, " +
                    "Description Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_AssetStatus_id ON AssetStatus (_id)");

            db.execSQL("CREATE TABLE if not exists Employees (EmployeeSysId Text PRIMARY KEY, Name Text, " +
                    "LastName Text, Id Text, CompanyIdExtern Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_Employees_id ON Employees (EmployeeSysId)");

            db.execSQL("CREATE TABLE IF NOT EXISTS FotoActivo (_idFoto Text PRIMARY KEY, assetSysId Text, " +
                    "  rutaImagen Text, nombreFoto Text, consecutivoFoto Text)");
            db.execSQL("CREATE UNIQUE INDEX idx_idFoto ON FotoActivo (_idFoto)");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Oficina");
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
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Activos");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS TipoTomaInventario");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS Inventario");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS DetalleInventario");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS RolHH");
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS AssetStatus");
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS Employees");
        onCreate(db);
    }

    public boolean InsertOrReplaceUsuarios (ArrayList<EntidadUsuarios> usuario)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try
        {
            for (EntidadUsuarios item: usuario)
            {
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
        }
        catch (Exception ex)
        {
            String E = ex.toString();
            Log.w("myApp","Error 22" +E+ " " +ex.getStackTrace());
            return false;
        }
        finally
        {
            db.endTransaction();
            return true;
        }
    }
    //*********************************************************************************************
    //region validacion Usuarios
    public boolean ExistenUsuarios()
    {
        String query = "SELECT * FROM Users";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
            return true;
        else
            return false;
    }
    //endregion
    //**********************************************************************************************

    public boolean InsertOrReplaceRolHH(ArrayList<EntidadDatosRol> Rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Limpiar la tabla RolHH antes de insertar nuevos registros
            String clearTableQuery = "DELETE FROM RolHH";
            db.execSQL(clearTableQuery);

            // Insertar nuevos registros
            for (EntidadDatosRol item : Rol) {
                String query = "INSERT OR REPLACE INTO RolHH (_idRol, page, username, description, UserSysId, Esta_Bloqueado) " +
                        "VALUES ('" + item.get_idRol() + "','" + item.getPage() + "','" + item.getUsername() + "','" +
                        item.getDescription() + "','" + item.getUserSysId() + "','" + item.getEstaBloqueado() + "')";
                db.execSQL(query);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            Log.w("myApp", "Error 22 " + ex.toString() + " " + ex.getStackTrace());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean guardarusuario (String _id , String username, String pass, String email, String bloqueado, String aprobado, String sesionActiva, String contrasenaFallida,
                                   String UltimaActividad, String UltimoInicio, String FechaBloqueo)
    {
            Cursor cursor = getUsername(username);
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", _id);
            contentValues.put("pass", pass);
            contentValues.put("email", email);
            contentValues.put("bloqueado", bloqueado);
            contentValues.put("aprobado", aprobado);
            contentValues.put("sesionActiva", sesionActiva);
            contentValues.put("contrasenaFallida", contrasenaFallida);
            contentValues.put("UltimaActividad", UltimaActividad);
            contentValues.put("UltimoInicio", UltimoInicio);
            contentValues.put("FechaBloqueo", FechaBloqueo);
            long result;
            if (cursor.getCount() == 0)
            {
                // Record does not exist
                contentValues.put("username", username);
                result = db.insert("Users", null, contentValues);
            }
            else
            {
                // Record exists
                result = db.update("Users", contentValues, "username=?", new String[] { username });
                result = db.update("Users", contentValues, "username=?", new String[] { username });
            }

            if (result == -1)
            {
                return false;
            }
            else
            {
                return true;
            }
    }
    public Cursor getUsername (String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM Users WHERE username=?";
        return db.rawQuery(sql, new String[] { username });
    }
    //*****************************************************************************************************
    public Cursor Login (String Username, String Pass)
    {
        //validacion de datos
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String query="Select * from Users where username ="+"'"+Username+"' and pass="+"'"+Pass+"'";
            return db.rawQuery(query, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

}
