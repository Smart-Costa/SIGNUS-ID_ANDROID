package com.example.diverscan.activeid.data.remote.api;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.Utilities.NetworkUtils;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ApiClient {
    private static final String TAG = "LOGIN_APICLIENT";
    private static final String BASE_URL = "http://192.168.2.36:5200/Api";
    private static ApiClient instance;
    private final AsyncHttpClient client;
    private final Context context;
    private final Gson gson;

    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.client = new AsyncHttpClient();
        this.gson = new Gson();

        client.setMaxRetriesAndTimeout(2, 10_000);
        client.addHeader("Accept", "application/json");
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.client.removeAllHeaders();
            instance = null;
        }
    }

    /* Helpers */
    private void ensureAuthHeader() {
        client.removeHeader("Authorization");
        String token = SessionManager.getToken(context);
        if (token != null && !token.isEmpty() && !SessionManager.isTokenExpired(token)) {
            client.addHeader("Authorization", "Bearer " + token);
        }
    }

    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("/")) endpoint = endpoint.substring(1);
        return BASE_URL + "/" + endpoint;
    }

    /* GET Factory */
    public <T> void get(String endpoint, final Type typeOfT, final ApiCallback<T> callback) {
        if (!NetworkUtils.isOnline(context)) {
            callback.onComplete(ApiResponse.failure("No network connection", -1));
            return;
        }

        ensureAuthHeader();
        String url = buildUrl(endpoint);
        Log.d(TAG, "GET " + url);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, StandardCharsets.UTF_8);
                    T data = gson.fromJson(resp, typeOfT);
                    callback.onComplete(ApiResponse.success(data, statusCode));
                } catch (Exception e) {
                    Log.e(TAG, "GET parse error", e);
                    callback.onComplete(ApiResponse.failure("Parse error: " + e.getMessage(), statusCode));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String body = null;
                if (responseBody != null) {
                    body = new String(responseBody, StandardCharsets.UTF_8);
                }
                String message = (error != null) ? error.getMessage() : "HTTP error";
                Log.e(TAG, "GET failed: " + statusCode + " body:" + body, error);
                callback.onComplete(ApiResponse.failure(message + (body != null ? " - " + body : ""), statusCode));
            }
        });
    }

    /* POST Factory with JSON */
    public <T> void post(String endpoint, Object bodyObject, final Type typeOfT, final ApiCallback<T> callback) {
        if (!NetworkUtils.isOnline(context)) {
            callback.onComplete(ApiResponse.failure("No network connection", -1));
            return;
        }

        ensureAuthHeader();
        String url = buildUrl(endpoint);
        try {
            String json = gson.toJson(bodyObject);
            //StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            //entity.setContentType("application/json");
            StringEntity entity = new StringEntity(json, "application/json", "UTF-8");

            Log.d(TAG, "POST " + url + " -> " + json);

            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String resp = new String(responseBody, StandardCharsets.UTF_8);
                        T data = gson.fromJson(resp, typeOfT);
                        callback.onComplete(ApiResponse.success(data, statusCode));
                    } catch (Exception e) {
                        Log.e(TAG, "POST parse error", e);
                        callback.onComplete(ApiResponse.failure("Parse error: " + e.getMessage(), statusCode));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String body = null;
                    if (responseBody != null) {
                        body = new String(responseBody, StandardCharsets.UTF_8);
                    }
                    String message = (error != null) ? error.getMessage() : "HTTP error";
                    Log.e(TAG, "POST failed: " + statusCode + " body:" + body, error);
                    callback.onComplete(ApiResponse.failure(message + (body != null ? " - " + body : ""), statusCode));
                }
            });

        } catch (Exception e) {
            callback.onComplete(ApiResponse.failure("Request error: " + e.getMessage(), -1));
        }
    }

    /* ---------- Utilities ---------- */
    public void clearAuth() {
        client.removeHeader("Authorization");
        SessionManager.clearSession(context);
    }

    public Gson getGson() {
        return gson;
    }
}


/*

    *//* GetRol *//*
    public void getRolHH(RolCallback callback) {
        String currentToken = SessionManager.getToken(context);
        if (currentToken == null || currentToken.isEmpty() || SessionManager.isTokenExpired(currentToken)) {
            callback.onResponse(new ArrayList<>());
            return;
        }

        client.removeAllHeaders();
        client.addHeader("Authorization", "Bearer " + currentToken);

        String url = BASE_URL + "/GetRolHH";
        Log.d("LOGIN_DEBUG", "Consultando roles en: " + url);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d("LOGIN_DEBUG", "Respuesta roles: " + resp);

                    List<RolHHEntity> roles = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(resp);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject o = jsonArray.getJSONObject(i);
                            RolHHEntity r = new RolHHEntity(
                                    o.optString("IdRol"),
                                    o.optString("Page"),
                                    o.optString("Description"),
                                    o.optString("Username"),
                                    o.optString("UserSysId"),
                                    o.optBoolean("Esta_Bloqueado") // nota el guion bajo
                            );
                            roles.add(r);
                        }
                    } catch (JSONException ex) {
                        JSONObject json = new JSONObject(resp);
                        JSONObject result = json.optJSONObject("ObtenerRolHHResult");
                        if (result != null && result.optBoolean("State")) {
                            JSONArray data = result.optJSONArray("Data");
                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject o = data.getJSONObject(i);
                                    RolHHEntity r = new RolHHEntity(
                                            o.optString("IdRol"),
                                            o.optString("Page"),
                                            o.optString("Description"),
                                            o.optString("Username"),
                                            o.optString("UserSysId"),
                                            o.optBoolean("EstaBloqueado")
                                    );
                                    roles.add(r);
                                }
                            }
                        }
                    }

                    Log.d("LOGIN_DEBUG", "Roles sincronizados correctamente (" + roles.size() + ")");
                    callback.onResponse(roles);

                } catch (Exception e) {
                    Log.e("LOGIN_DEBUG", "Error procesando roles: " + e.getMessage(), e);
                    callback.onResponse(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("LOGIN_DEBUG", "Fallo consultando roles: " + error.getMessage());
                callback.onResponse(new ArrayList<>());
            }
        });
    }*/
