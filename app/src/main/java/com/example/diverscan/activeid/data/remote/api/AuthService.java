package com.example.diverscan.activeid.data.remote.api;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final String TAG = "LOGIN_AUTHSERVICE";
    private final ApiClient apiClient;
    private final Context context;

    public AuthService(Context context) {
        this.context = context.getApplicationContext();
        this.apiClient = ApiClient.getInstance(this.context);
    }

    public void login(String username, String password, ApiCallback<LoginResult> callback) {
        String endpoint = "auth/login";
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Type type = new TypeToken<LoginResult>() {}.getType();
        apiClient.<LoginResult>post(endpoint, body, type, response -> {
            LoginResult result = response.data;
            if (response.success && response.data != null) {
                if (result.token != null && !result.token.isEmpty()) {
                    SessionManager.saveSession(
                            context,
                            result.token,
                            result.username,
                            result.email != null ? result.email : "",
                            result.userId != null ? result.userId : ""
                    );
                }
                callback.onComplete(ApiResponse.success(result, response.statusCode));
                Log.d(TAG, "Usuario autenticado: " + result.username + " ID: " + result.userId);
            } else {
                callback.onComplete(ApiResponse.failure(response.errorMessage, response.statusCode));
                Log.d(TAG, "Usuario no autenticado: " + response.errorMessage);
            }
        });
    }
    public static class LoginResult {
        public boolean success;
        public String token;
        public String userId;
        public String username;
        public String email;
        public String message;
    }
}
