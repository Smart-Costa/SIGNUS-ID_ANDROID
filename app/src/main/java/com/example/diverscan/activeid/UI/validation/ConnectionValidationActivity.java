package com.example.diverscan.activeid.UI.validation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.zebra.rfid.api3.TagData;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.method.ScrollingMovementMethod;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ConnectionValidationActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView tvStatus;
    private TextView tvDevice;
    private TextView tvFoundDevices;
    private TextView tvLog;
    private Button btnReconnect;
    private TagWriter rfidHandler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_validation);

        checkAndRequestPermissions();

        tvStatus = findViewById(R.id.tv_connection_status);
        tvDevice = findViewById(R.id.tv_device_info);
        tvFoundDevices = findViewById(R.id.tv_found_devices);
        tvLog = findViewById(R.id.tv_log);
        tvLog.setMovementMethod(new ScrollingMovementMethod()); // Habilitar scroll

        btnReconnect = findViewById(R.id.btn_reconnect);

        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        btnReconnect.setOnClickListener(v -> {
            log("Reiniciando conexión...");
            // Usamos InitSDK para forzar una nueva búsqueda de lectores si es necesario
            rfidHandler.InitSDK();
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't nullify handler here if we want background updates, 
        // but for safety in this app structure:
        // if (rfidHandler != null) rfidHandler.setResponseHandler(null);
    }

    private void updateUI() {
        boolean connected = rfidHandler.isConnected();
        tvStatus.setText(connected ? "CONECTADO" : "DESCONECTADO");
        tvStatus.setTextColor(connected ? 0xFF00AA00 : 0xFFFF0000); // Green / Red

        if (connected) {
            String name = rfidHandler.getReaderName();
            String model = rfidHandler.getReaderModel();
            tvDevice.setText("Nombre: " + name + "\nModelo: " + model);
        } else {
            tvDevice.setText("Dispositivo: --");
        }
        updateDeviceList();
    }

    private void updateDeviceList() {
        if (rfidHandler != null) {
            List<String> devices = rfidHandler.getFoundDevices();
            if (devices != null && !devices.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String d : devices) {
                    sb.append("• ").append(d).append("\n");
                }
                tvFoundDevices.setText(sb.toString());
            } else {
                tvFoundDevices.setText("Buscando... (0 encontrados)");
            }
        }
    }

    private void log(String msg) {
        if (isFinishing() || isDestroyed()) return;
        String timestamp = timeFormat.format(new Date());
        runOnUiThread(() -> {
            tvLog.append("\n[" + timestamp + "] " + msg);
            if (tvLog.getLayout() != null) {
                final int scrollAmount = tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight();
                if (scrollAmount > 0)
                    tvLog.scrollTo(0, scrollAmount);
            }
        });
    }

    // ResponseHandlerInterface implementation
    @Override
    public void handleTagdata(TagData[] tagData) {
        // Not used here
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        log("Gatillo: " + (pressed ? "Presionado" : "Liberado"));
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        if (!isFinishing() && !isDestroyed()) {
            log(Text);
            runOnUiThread(this::updateDeviceList);
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Simple check
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                log("Advertencia: Permisos no concedidos. La conexión podría fallar.");
                Toast.makeText(this, "Permisos necesarios no concedidos", Toast.LENGTH_LONG).show();
            } else {
                log("Permisos concedidos. Intentando conectar...");
                if (!rfidHandler.isInitialized()) {
                    rfidHandler.onCreate(this);
                } else {
                    rfidHandler.InitSDK();
                }
            }
        }
    }
}
