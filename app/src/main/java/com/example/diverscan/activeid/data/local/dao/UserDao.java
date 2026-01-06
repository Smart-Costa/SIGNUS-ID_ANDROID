package com.example.diverscan.activeid.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.diverscan.activeid.Utilities.EncryptUtil;
import com.example.diverscan.activeid.data.local.entity.LoginEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class UserDao {
    private static final String TAG = "DB_DAO_USER";
    private final AppDatabaseHelper dbHelper;
    private final Context context;

    public UserDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public boolean validateUser(String user, String pass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String encryptedInput = EncryptUtil.encrypting(pass, true);
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Users WHERE username=? AND password=?",
                new String[]{user, encryptedInput}
        );

        if (cursor.moveToFirst()) {
            do {
                String dbUser = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String dbPass = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            } while (cursor.moveToNext());
        } else {
        }

        boolean valid = cursor.moveToFirst();

        cursor.close();
        db.close();

        return valid;
    }

    public void saveUser(LoginEntity u) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", u.userSysId);
        values.put("username", u.username);
        values.put("email", u.email);
        values.put("pass", u.password);
        values.put("aprobado", u.isApproved ? 1 : 0);
        values.put("isOnLine", u.isOnLine ? 1 : 0);
        values.put("bloqueado", u.isLockedOut ? 1 : 0);
        values.put("Idrol", u.Idrol);

        db.insertWithOnConflict("Users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void syncUsers(List<LoginEntity> users) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (LoginEntity u : users) {
                ContentValues values = entityToContentValues(u);
                db.insertWithOnConflict("Users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing users", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private ContentValues entityToContentValues(LoginEntity u) {
        ContentValues values = new ContentValues();
        values.put("userSysId", u.userSysId);
        values.put("username", u.username);
        values.put("email", u.email);
        values.put("password", u.password);
        values.put("isApproved", u.isApproved ? 1 : 0);
        values.put("isOnLine", u.isOnLine ? 1 : 0);
        values.put("isLockedOut", u.isLockedOut ? 1 : 0);
        values.put("Idrol", u.Idrol);
        return values;
    }

    /* NEW FUNCTIONS */

    public void fetchAndSyncFromApi() {
        fetchAndSyncFromApi(null);
    }

    public void fetchAndSyncFromApi(final Runnable onComplete) {
        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<List<LoginEntity>>() {}.getType();

        api.<List<LoginEntity>>get("Users", type, new ApiCallback<List<LoginEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<LoginEntity>> response) {
                if (response.success && response.data != null) {
                    syncUsers(response.data);
                    Log.d(TAG, "Usuarios sincronizados desde API: " + response.data.size());
                } else {
                    Log.e(TAG, "Error al sincronizar desde API: " + response.errorMessage);
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void pushLocalChangesToApi() {
        List<LoginEntity> localUsers = getAllLocalUsers();
        if (localUsers.isEmpty()) {
            Log.d(TAG, "No hay usuarios locales para sincronizar con el servidor");
            return;
        }

        ApiClient api = ApiClient.getInstance(context);
        Type type = new TypeToken<ApiResponse<Void>>() {}.getType();

        api.<ApiResponse<Void>>post("Users", localUsers, type, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                if (response.success) {
                    Log.d(TAG, "Usuarios locales enviados exitosamente al servidor");
                } else {
                    Log.e(TAG, "Error enviando usuarios al servidor: " + response.errorMessage);
                }
            }
        });
    }

    public List<LoginEntity> getAllLocalUsers() {
        List<LoginEntity> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users", null);

        if (cursor.moveToFirst()) {
            do {
                LoginEntity u = new LoginEntity();
                u.userSysId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                u.username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                u.email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                u.password = cursor.getString(cursor.getColumnIndexOrThrow("pass"));
                u.isApproved = cursor.getInt(cursor.getColumnIndexOrThrow("aprobado")) == 1;
                u.isOnLine = cursor.getInt(cursor.getColumnIndexOrThrow("isOnLine")) == 1;
                u.isLockedOut = cursor.getInt(cursor.getColumnIndexOrThrow("bloqueado")) == 1;
                u.Idrol = cursor.getString(cursor.getColumnIndexOrThrow("Idrol"));
                users.add(u);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }
}
