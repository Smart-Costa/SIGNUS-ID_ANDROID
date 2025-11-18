package com.example.diverscan.activeid.UI.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.Utilities.NetworkUtils;
import com.example.diverscan.activeid.data.repository.LoginRepository;

public class LoginViewModel extends AndroidViewModel {
    private final LoginRepository repository;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public enum LoginState { LOADING, SUCCESS, INVALID_CREDENTIALS, OFFLINE, ERROR }

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new LoginRepository(application.getApplicationContext());
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    /* Init Login Option */
    public void login(String user, String pass) {
        loginState.postValue(LoginState.LOADING);

        if (!NetworkUtils.isOnline(getApplication())) {
            boolean localSuccess = repository.loginLocal(user, pass);
            if (localSuccess) {
                loginState.postValue(LoginState.SUCCESS);
            } else {
                loginState.postValue(LoginState.INVALID_CREDENTIALS);
            }
            return;
        }

        repository.loginRemote(user, pass, success -> {
            if (success) {
                repository.syncRemoteToLocal();
                loginState.postValue(LoginState.SUCCESS);
            }
            else {
                loginState.postValue(LoginState.INVALID_CREDENTIALS);
            }
        });
    }
}
