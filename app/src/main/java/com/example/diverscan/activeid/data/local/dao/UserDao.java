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
public class UserDao extends SQLiteOpenHelper {

    private static final String DB_NAME = "dbSignusId.db";
    private static final int DB_VERSION = 8;
    private static final String TAG = "USER_DAO";

    private final Context context;

    public UserDao(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "userSysId TEXT, " +
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "isApproved INTEGER, " +
                "isOnLine INTEGER, " +
                "isLockedOut INTEGER, " +
                "Idrol TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public boolean validateUser(String user, String pass) {
        SQLiteDatabase db = getReadableDatabase();
        String encryptedInput = EncryptUtil.encrypting(pass, true);
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?",
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
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userSysId", u.userSysId);
        values.put("username", u.username);
        values.put("email", u.email);
        values.put("password", u.password);
        values.put("isApproved", u.isApproved ? 1 : 0);
        values.put("isOnLine", u.isOnLine ? 1 : 0);
        values.put("isLockedOut", u.isLockedOut ? 1 : 0);
        values.put("Idrol", u.Idrol);

        db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void syncUsers(List<LoginEntity> users) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (LoginEntity u : users) {
                ContentValues values = entityToContentValues(u);
                db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users", null);

        if (cursor.moveToFirst()) {
            do {
                LoginEntity u = new LoginEntity();
                u.userSysId = cursor.getString(cursor.getColumnIndexOrThrow("userSysId"));
                u.username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                u.email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                u.password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                u.isApproved = cursor.getInt(cursor.getColumnIndexOrThrow("isApproved")) == 1;
                u.isOnLine = cursor.getInt(cursor.getColumnIndexOrThrow("isOnLine")) == 1;
                u.isLockedOut = cursor.getInt(cursor.getColumnIndexOrThrow("isLockedOut")) == 1;
                u.Idrol = cursor.getString(cursor.getColumnIndexOrThrow("Idrol"));
                users.add(u);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }
}
