package com.example.diverscan.activeid.UI.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.diverscan.activeid.databinding.ActivitySplashBinding;
import com.example.diverscan.activeid.UI.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private static final int SPLASH_DURATION = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.logoSplash.animate()
                .alpha(1f)
                .setDuration(800)
                .start();

        binding.getRoot().postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
