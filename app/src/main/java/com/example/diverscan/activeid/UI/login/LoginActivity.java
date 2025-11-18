package com.example.diverscan.activeid.UI.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diverscan.activeid.UI.main.MainActivity;
import com.example.diverscan.activeid.Utilities.DialogUtils;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.api.AuthService;
import com.example.diverscan.activeid.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        validateSessionExist();
        setupUI();
        observeViewModel();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finishAffinity(); }
        });
    }

    private void validateSessionExist() {
        String savedToken = SessionManager.getToken(this);
        boolean tokenValido = savedToken != null && !savedToken.isEmpty() && !SessionManager.isTokenExpired(savedToken);

        if (SessionManager.isLoggedIn(this) && tokenValido) {
            //ApiClient.getInstance(this).setAuthToken(savedToken);
            AuthService authService = new AuthService(this);
            gotoMain();
        }
    }

    private void setupUI() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.user.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                DialogUtils.warning(this, "Debe ingresar usuario y contraseña");
                return;
            }

            viewModel.login(username, password);
        });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            switch (state) {
                case SUCCESS -> {
                    DialogUtils.toast(this, "Inicio de sesión exitoso");
                    gotoMain();
                }
                case INVALID_CREDENTIALS ->
                        DialogUtils.error(this, "Usuario o contraseña incorrectos");
                case OFFLINE ->
                        DialogUtils.warning(this, "Sin conexión. Intentando modo local");
                case ERROR ->
                        DialogUtils.error(this, "Error inesperado. Intenta nuevamente");
            }
        });
    }

    private void gotoMain() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
