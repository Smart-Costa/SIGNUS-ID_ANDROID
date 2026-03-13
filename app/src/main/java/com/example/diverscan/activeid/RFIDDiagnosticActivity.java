package com.example.diverscan.activeid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.UI.login.LoginActivity;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderFactory;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RFIDDiagnosticActivity — Pantalla de diagnóstico para probar la conexión
 * con lectores RFID Zebra.
 *
 * Para acceder desde el menú principal o de configuración:
 * Intent intent = new Intent(this, RFIDDiagnosticActivity.class);
 * startActivity(intent);
 *
 * También puede ser lanzada directamente como launcher activity durante el
 * desarrollo.
 */
public class RFIDDiagnosticActivity extends AppCompatActivity implements IReaderListener {

    private static final String TAG = "RFIDDiagnostic";

    // ── UI ────────────────────────────────────────────────────────────────────
    // private RadioGroup rgReaderType; // REMOVED: Zebra only
    private RadioGroup rgConnectionType;
    private TextView tvConnectionStatus;
    private TextView tvDeviceName;
    private TextView tvTagCount;
    private TextView tvTagLog;
    private TextView tvSystemLog;
    private ScrollView scrollView;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnStartInventory;
    private Button btnStopInventory;
    private Button btnClear;
    private Button btnContinueLogin;

    // ── State ─────────────────────────────────────────────────────────────────
    private IReaderDevice currentReader;
    private int tagCount = 0;
    private final StringBuilder tagLog = new StringBuilder();
    private final StringBuilder sysLog = new StringBuilder();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_diagnostic);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Diagnóstico RFID");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        setupListeners();
        logSystem("Activity iniciada. Selecciona tipo de lector y presiona 'Conectar'.");
    }

    private void bindViews() {
        // rgReaderType = findViewById(R.id.rg_reader_type); // REMOVED
        rgConnectionType = findViewById(R.id.rg_connection_type);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvTagCount = findViewById(R.id.tv_tag_count);
        tvTagLog = findViewById(R.id.tv_tag_log);
        tvSystemLog = findViewById(R.id.tv_system_log);
        scrollView = (ScrollView) tvTagLog.getParent();
        btnConnect = findViewById(R.id.btn_connect);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnStartInventory = findViewById(R.id.btn_start_inventory);
        btnStopInventory = findViewById(R.id.btn_stop_inventory);
        btnClear = findViewById(R.id.btn_clear);
        btnContinueLogin = findViewById(R.id.btn_continue_login);
    }

    private void setupListeners() {
        btnConnect.setOnClickListener(v -> onConnectClicked());
        btnDisconnect.setOnClickListener(v -> onDisconnectClicked());
        btnStartInventory.setOnClickListener(v -> onStartInventoryClicked());
        btnStopInventory.setOnClickListener(v -> onStopInventoryClicked());
        btnClear.setOnClickListener(v -> clearTags());
        btnContinueLogin.setOnClickListener(v -> goToLogin());

        // Long-press en el estado permite saltar al login sin lector (solo dev)
        tvConnectionStatus.setOnLongClickListener(v -> {
            Toast.makeText(this, "Saltando al login sin lector...", Toast.LENGTH_SHORT).show();
            goToLogin();
            return true;
        });
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    private void onConnectClicked() {
        if (currentReader != null) {
            currentReader.dispose();
            currentReader = null;
        }

        ReaderType readerType = getSelectedReaderType();
        ConnectionType connectionType = getSelectedConnectionType();

        logSystem("Creando lector: " + readerType + " / " + connectionType);
        setStatus("Conectando...", "#FF9800");

        try {
            currentReader = ReaderFactory.createReader(readerType, connectionType, this, this);
            currentReader.initialize(this);
            logSystem("Reader inicializado. Esperando evento de conexión...");
        } catch (Exception e) {
            logSystem("ERROR al crear reader: " + e.getMessage());
            setStatus("● Error: " + e.getMessage(), "#F44336");
        }
    }

    private void onDisconnectClicked() {
        if (currentReader != null) {
            logSystem("Desconectando...");
            currentReader.dispose();
            currentReader = null;
        }
        setStatus("● Desconectado", "#F44336");
        tvDeviceName.setText("Dispositivo: —");
        setInventoryButtonsEnabled(false);
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
    }

    private void onStartInventoryClicked() {
        if (currentReader != null && currentReader.isConnected()) {
            boolean started = currentReader.startInventory();
            logSystem("startInventory() → " + (started ? "OK" : "FALLÓ"));
            if (started) {
                btnStartInventory.setEnabled(false);
                btnStopInventory.setEnabled(true);
            }
        } else {
            logSystem("No hay lector conectado.");
        }
    }

    private void onStopInventoryClicked() {
        if (currentReader != null) {
            boolean stopped = currentReader.stopInventory();
            logSystem("stopInventory() → " + (stopped ? "OK" : "FALLÓ"));
            btnStartInventory.setEnabled(true);
            btnStopInventory.setEnabled(false);
        }
    }

    private void clearTags() {
        tagCount = 0;
        tagLog.setLength(0);
        tvTagCount.setText("0");
        tvTagLog.setText("— Esperando tags... —");
    }

    // ── IReaderListener callbacks (vienen en UI thread via Handler) ───────────

    @Override
    public void onConnected(String readerName) {
        logSystem("✅ Conectado: " + readerName);
        setStatus("● Conectado", "#4CAF50");
        tvDeviceName.setText("Dispositivo: " + readerName);
        btnConnect.setEnabled(false);
        btnDisconnect.setEnabled(true);
        setInventoryButtonsEnabled(true);
        // Habilitar el botón de continuar al login
        btnContinueLogin.setEnabled(true);
        btnContinueLogin.setAlpha(1.0f);
    }

    @Override
    public void onDisconnected() {
        logSystem("🔌 Desconectado.");
        setStatus("● Desconectado", "#F44336");
        tvDeviceName.setText("Dispositivo: —");
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        setInventoryButtonsEnabled(false);
        btnContinueLogin.setEnabled(false);
        btnContinueLogin.setAlpha(0.4f);
    }

    @Override
    public void onConnectionError(String message) {
        logSystem("❌ Error: " + message);
        setStatus("● Error de conexión", "#F44336");
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        setInventoryButtonsEnabled(false);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        for (ReaderTag tag : tags) {
            tagCount++;
            String line = sdf.format(new Date()) + "  EPC: " + tag.getEpc()
                    + "  RSSI: " + tag.getRssi() + " dBm\n";
            tagLog.insert(0, line); // más reciente arriba
        }
        tvTagCount.setText(String.valueOf(tagCount));
        tvTagLog.setText(tagLog.toString());
    }

    @Override
    public void onTrigger(boolean pressed) {
        logSystem("Trigger: " + (pressed ? "PRESIONADO" : "SUELTO"));
    }

    @Override
    public void onStatusMessage(String message) {
        logSystem("[SDK] " + message);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ReaderType getSelectedReaderType() {
        return ReaderType.ZEBRA;
    }

    private ConnectionType getSelectedConnectionType() {
        int id = rgConnectionType.getCheckedRadioButtonId();
        if (id == R.id.rb_bluetooth)
            return ConnectionType.BLUETOOTH;
        if (id == R.id.rb_serial_usb)
            return ConnectionType.SERIAL_USB;
        return ConnectionType.AUTO;
    }

    private void setStatus(String text, String colorHex) {
        runOnUiThread(() -> {
            tvConnectionStatus.setText(text);
            tvConnectionStatus.setTextColor(android.graphics.Color.parseColor(colorHex));
        });
    }

    private void setInventoryButtonsEnabled(boolean enabled) {
        btnStartInventory.setEnabled(enabled);
        btnStopInventory.setEnabled(false); // stop siempre desactivado al conectar
    }

    /** Navega al LoginActivity cerrando esta pantalla de diagnóstico */
    private void goToLogin() {
        if (currentReader != null) {
            // Detener inventario si estaba activo, pero mantener la conexión
            currentReader.stopInventory();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Cerrar diagnóstico para que Back no regrese aquí
    }

    private void logSystem(String msg) {
        String line = sdf.format(new Date()) + " > " + msg + "\n";
        sysLog.insert(0, line);
        // Mantener solo las últimas 8 líneas para no saturar el área
        String[] lines = sysLog.toString().split("\n");
        if (lines.length > 8) {
            sysLog.setLength(0);
            for (int i = 0; i < 8; i++)
                sysLog.append(lines[i]).append("\n");
        }
        runOnUiThread(() -> tvSystemLog.setText(sysLog.toString()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentReader != null) {
            currentReader.dispose();
            currentReader = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
