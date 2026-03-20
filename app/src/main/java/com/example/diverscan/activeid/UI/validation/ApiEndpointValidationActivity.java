package com.example.diverscan.activeid.UI.validation;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.BuildConfig;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

public class ApiEndpointValidationActivity extends AppCompatActivity {
    private static final String TAG = "ApiEndpointValidation";

    private TextView tvBaseUrl;
    private EditText etPath;
    private TextView tvLog;
    private Button btnProbeTcp;
    private Button btnGet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_endpoint_validation);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Probar API");
        }
        tvBaseUrl = findViewById(R.id.tvBaseUrl);
        etPath = findViewById(R.id.etEndpointPath);
        tvLog = findViewById(R.id.tvOutput);
        btnProbeTcp = findViewById(R.id.btnProbeTcp);
        btnGet = findViewById(R.id.btnTestGet);

        tvLog.setMovementMethod(new ScrollingMovementMethod());
        tvBaseUrl.setText(BuildConfig.BASE_URL);
        if (etPath.getText().toString().trim().isEmpty()) {
            etPath.setText("ConsolidacionApi/ConsultarNovedades?username=Wilbert");
        }

        btnProbeTcp.setOnClickListener(v -> probeTcp());
        btnGet.setOnClickListener(v -> testGet());
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            tvLog.append(msg + "\n");
            final int scrollAmount = tvLog.getLayout() != null ? tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight() : 0;
            if (scrollAmount > 0) tvLog.scrollTo(0, scrollAmount);
        });
        Log.d(TAG, msg);
    }

    private void probeTcp() {
        appendLog("Iniciando prueba TCP...");
        new Thread(() -> {
            try {
                URI uri = URI.create(BuildConfig.BASE_URL);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
                long t0 = System.currentTimeMillis();
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host, port), 3000);
                long dt = System.currentTimeMillis() - t0;
                s.close();
                appendLog("TCP OK " + host + ":" + port + " (" + dt + " ms)");
            } catch (IOException e) {
                appendLog("TCP ERROR: " + e.getMessage());
            } catch (Exception e) {
                appendLog("TCP EXCEPTION: " + e.getMessage());
            }
        }).start();
    }

    private void testGet() {
        String path = etPath.getText().toString().trim();
        if (path.isEmpty()) {
            appendLog("Debe indicar un endpoint relativo.");
            return;
        }
        appendLog("GET " + BuildConfig.BASE_URL + "/" + path);
        ApiClient.getInstance(this).get(path, JsonElement.class, new ApiCallback<JsonElement>() {
            @Override
            public void onComplete(ApiResponse<JsonElement> response) {
                if (response.success) {
                    appendLog("HTTP OK " + response.statusCode + " totalPages=" + response.totalPages);
                    appendLog(response.data != null ? response.data.toString() : "(sin cuerpo)");
                } else {
                    appendLog("HTTP ERROR " + response.statusCode + " -> " + response.errorMessage);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

