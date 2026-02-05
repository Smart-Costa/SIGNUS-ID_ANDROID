package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.data.local.entity.RolEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.example.diverscan.activeid.sqlite.LoginDBHelper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RolDao {
    private static final String TAG = "DB_DAO_ROL";
    private final LoginDBHelper dbHelper;
    private final Context context;

    public RolDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new LoginDBHelper(context);
    }

    public void syncRoles(List<RolEntity> roles) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete("RolHH", null, null);
            Log.d(TAG, "Tabla RolHH limpiada");

            int count = 0;
            for (RolEntity r : roles) {
                ContentValues v = entityToContentValues(r);
                db.insertWithOnConflict("RolHH", null, v, SQLiteDatabase.CONFLICT_REPLACE);
                count++;
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Roles sincronizados desde API: " + count);
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando roles", e);
        } finally {
            db.endTransaction();
            // db.close();
        }
    }

    private ContentValues entityToContentValues(RolEntity r) {
        ContentValues v = new ContentValues();
        v.put("_idRol", r.getIdRol());
        v.put("Page", r.getPage());
        v.put("Description", r.getDescription());
        v.put("Username", r.getUsername());
        v.put("UserSysId", r.getUserSysId());
        v.put("Esta_Bloqueado", r.isEstaBloqueado() ? 1 : 0);
        return v;
    }

    public List<RolEntity> getAllLocalRoles() {
        List<RolEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM RolHH", null);

        try {
            if (c.moveToFirst()) {
                do {
                    RolEntity r = new RolEntity(
                            c.getString(c.getColumnIndexOrThrow("_idRol")),
                            c.getString(c.getColumnIndexOrThrow("Page")),
                            c.getString(c.getColumnIndexOrThrow("Description")),
                            c.getString(c.getColumnIndexOrThrow("Username")),
                            c.getString(c.getColumnIndexOrThrow("UserSysId")),
                            c.getInt(c.getColumnIndexOrThrow("Esta_Bloqueado")) == 1
                    );
                    list.add(r);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo roles locales", e);
        } finally {
            c.close();
            db.close();
        }

        return list;
    }

    public List<String> getRolesForUser(String userSysId) {
        List<String> roles = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT Description FROM RolHH WHERE UserSysId = ?",
                new String[]{ userSysId }
        );

        try {
            if (c.moveToFirst()) {
                do {
                    roles.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo roles del usuario", e);
        } finally {
            c.close();
            db.close();
        }

        return roles;
    }

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<RolEntity>>() {}.getType();

        api.<List<RolEntity>>get("GetRolHH", type, new ApiCallback<List<RolEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<RolEntity>> response) {
                if (response.success && response.data != null) {
                    syncRoles(response.data);
                } else {
                    Log.e(TAG, "Error al sincronizar roles desde API: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    /*
    public void pushLocalChangesToApi() {
        List<RolEntity> localRoles = getAllLocalRoles();
        if (localRoles.isEmpty()) {
            Log.d(TAG, "No hay roles locales para enviar al servidor");
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        api.<ApiResponse<Void>>post("PostRolHH", localRoles, type, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                if (response.success) {
                    Log.d(TAG, "Roles locales enviados exitosamente al servidor");
                } else {
                    Log.e(TAG, "Error enviando roles al servidor: " + response.errorMessage);
                }
            }
        });
    }
    */
}
