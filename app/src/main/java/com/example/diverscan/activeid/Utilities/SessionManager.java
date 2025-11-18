package com.example.diverscan.activeid.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class SessionManager {
    private static final String TAG = "LOGIN_SESSION";
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERID = "userSysId";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static void saveSession(Context context, String token, String username, String email, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_USERID, userId)
                .apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }
    public String getUserId() {
        return prefs.getString(KEY_USERID, null);
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_TOKEN, null);
        return token != null && !isTokenExpired(token);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        boolean success = editor.commit();
        Log.d(TAG, "Sesión limpiada completamente de SharedPreferences. Éxito=" + success);
    }

    public static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);

            long exp = json.optLong("exp", 0);
            long now = System.currentTimeMillis() / 1000;

            boolean expired = exp < now;
            if (expired) {
                Log.w(TAG, "Token expirado. exp=" + exp + " now=" + now);
            }

            return expired;
        } catch (Exception e) {
            Log.e(TAG, "Error verificando expiración: " + e.getMessage());
            return true;
        }
    }
}
