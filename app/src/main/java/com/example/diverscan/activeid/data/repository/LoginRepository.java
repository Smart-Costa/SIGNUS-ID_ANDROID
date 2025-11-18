package com.example.diverscan.activeid.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.local.dao.UserDao;
import com.example.diverscan.activeid.data.remote.api.AuthService;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;

import java.util.concurrent.Executors;

public class LoginRepository {
    private static final String TAG = "LOGIN_REPOSITORY";
    private final Context context;
    private final UserDao userDao;
    private final AuthService authService;

    public interface LoginCallback {
        void onResult(boolean success);
    }

    public LoginRepository(Context context) {
        this.context = context.getApplicationContext();
        this.userDao = new UserDao(this.context);
        this.authService = new AuthService(this.context);
    }

    public Context getContext() { return context; }

    public boolean loginLocal(String username, String password) {
        boolean valid = userDao.validateUser(username, password);
        if (valid) {
            long exp = (System.currentTimeMillis() / 1000) + (4 * 60 * 60);
            String fakePayload = "{\"exp\":" + exp + "}";
            String fakeToken = "local." + android.util.Base64.encodeToString(
                    fakePayload.getBytes(), android.util.Base64.URL_SAFE
            ).trim() + ".offline";

            SessionManager.saveSession(context, fakeToken, username, "offline@local", "0");
        }
        return valid;
    }

    public void loginRemote(String username, String password, LoginCallback callback) {
        authService.login(username, password, new ApiCallback<AuthService.LoginResult>() {
            public void onComplete(ApiResponse<AuthService.LoginResult> response) {
                if (response.success && response.data != null && response.data.token != null) {
                    AuthService.LoginResult result = response.data;

                    SessionManager.saveSession(
                            context,
                            result.token,
                            result.username,
                            result.email != null ? result.email : "",
                            result.userId != null ? result.userId : ""
                    );

                    Log.d(TAG, "Usuario autenticado: " + result.username + " ID: " + result.userId);
                    callback.onResult(true);
                } else {
                    String message = response.errorMessage != null ? response.errorMessage : "Login remoto falló";
                    Log.e(TAG, "Error autenticando: " + message);
                    callback.onResult(false);
                }
            }
        });
    }

    public void syncRemoteToLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            //apiClient.getAllUsers(users -> userDao.syncUsers(users));
            //apiClient.getRolHH(roles -> hhRolHelper.insertOrReplaceRoles(roles));
        });
    }


}
