package com.example.diverscan.activeid.UI.validation;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderFactory;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * iMin-specific RFID configuration screen.
 * Uses iMin RFID SDK v1.0.3.
 * Tests: iMin peripheral service health check, connect/disconnect,
 *        single-read, multi-read, antenna power (dBm) configuration.
 *
 * IMIN SDK notes:
 * - Requires com.imin.peripherservice to be installed (system app on iMin devices)
 * - RFIDManager.getInstance() → .connect(context) → .getHelper()
 * - RFIDHelper.registerReaderCall(ReaderCall) → .tagInventoryRawStartReading()
 * - Power via rfidHelper.extendOperation(CMD.SET_READ_WRITE_POWER, jsonConfig)
 * - readPower / writePower in dBm range [5–33]
 */
public class IminRfidConfigActivity extends AppCompatActivity implements IReaderListener {

    private static final String TAG = "IminRfidConfig";

    // UI
    private TextView tvServiceStatus, tvStatus, tvDeviceInfo, tvTags, tvLog, tvPowerLabel;
    private Button btnCheckService, btnConnect, btnDisconnect, btnSingle, btnMulti, btnSetPower, btnDiagnostic;
    private SeekBar sbPower;

    // RFID
    private IReaderDevice iminDevice;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    // Read state
    private boolean isSingleReading = false;
    private boolean isMultiReading = false;
    private Map<String, Integer> multiReadTags = new HashMap<>();
    private int logLineCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_config_imin);

        tvServiceStatus = findViewById(R.id.tv_imin_service_status);
        tvStatus        = findViewById(R.id.tv_imin_status);
        tvDeviceInfo    = findViewById(R.id.tv_imin_device_info);
        tvTags          = findViewById(R.id.tv_imin_tags);
        tvLog           = findViewById(R.id.tv_imin_log);
        tvPowerLabel    = findViewById(R.id.tv_imin_power_label);
        btnCheckService = findViewById(R.id.btn_imin_check_service);
        btnConnect      = findViewById(R.id.btn_imin_connect);
        btnDisconnect   = findViewById(R.id.btn_imin_disconnect);
        btnSingle       = findViewById(R.id.btn_imin_single);
        btnMulti        = findViewById(R.id.btn_imin_multi);
        btnSetPower     = findViewById(R.id.btn_imin_set_power);
        btnDiagnostic   = findViewById(R.id.btn_imin_diagnostic);
        sbPower         = findViewById(R.id.sb_imin_power);

        tvLog.setMovementMethod(new ScrollingMovementMethod());

        // iMin power: 5–33 dBm
        sbPower.setMax(28); // Max - Min = 33 - 5
        sbPower.setProgress(25); // default 30 dBm (25 offset)
        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPowerLabel.setText((p + 5) + " dBm");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        btnCheckService.setOnClickListener(v -> checkIminService());
        btnConnect.setOnClickListener(v -> connectImin());
        btnDisconnect.setOnClickListener(v -> disconnectImin());
        btnSingle.setOnClickListener(v -> startSingleRead());
        btnMulti.setOnClickListener(v -> toggleMultiRead());
        btnSetPower.setOnClickListener(v -> applyPower());
        btnDiagnostic.setOnClickListener(v -> showDiagnostic());

        logInfo("iMin Config UI lista. Modelo: " + Build.MODEL);
        logInfo("SDK: IminRfidSdk v1.0.3 | Requiere com.imin.peripherservice");
        logWarn("Este dispositivo DEBE ser un iMin con módulo RFID interno (I24P01 / Lark 1).");

        // Auto-check service on start
        handler.postDelayed(this::checkIminService, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iminDevice != null) {
            try { iminDevice.dispose(); } catch (Exception ignored) {}
            iminDevice = null;
        }
    }

    // ─── Service Check ────────────────────────────────────────────────────────

    private void checkIminService() {
        // Check if the iMin peripheral service is installed
        boolean installed = isPackageInstalled("com.imin.peripherservice");
        boolean rfidInstalled = isPackageInstalled("com.imin.rfid");
        StringBuilder sb = new StringBuilder();
        sb.append("com.imin.peripherservice: ").append(installed ? "✅ INSTALADO" : "❌ AUSENTE").append("\n");
        sb.append("com.imin.rfid: ").append(rfidInstalled ? "✅ INSTALADO" : "⚠ Opcional").append("\n");
        if (!installed) {
            sb.append("CRÍTICO: El servicio iMin es requerido para el módulo RFID.\nEste dispositivo puede no ser un iMin compatible.");
        } else {
            sb.append("Servicio OK. Puede proceder a conectar.");
        }
        tvServiceStatus.setText(sb.toString());
        if (installed) logInfo("Servicio iMin detectado correctamente.");
        else logError("Servicio iMin NO encontrado. Sin soporte RFID.");
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // ─── Connect / Disconnect ─────────────────────────────────────────────────

    private void connectImin() {
        logInfo("Conectando a iMin RFID...");
        if (iminDevice == null) {
            iminDevice = ReaderFactory.createReader(ReaderType.IMIN, this, this);
        } else {
            iminDevice.setListener(this);
            iminDevice.connect();
        }
    }

    private void disconnectImin() {
        if (iminDevice != null) {
            iminDevice.disconnect();
            logInfo("Solicitando desconexión iMin...");
        } else {
            logWarn("No hay dispositivo iMin activo.");
        }
    }

    // ─── Read Tests ───────────────────────────────────────────────────────────

    private void startSingleRead() {
        if (iminDevice == null || !iminDevice.isConnected()) { toast("iMin desconectado"); return; }
        isSingleReading = true; isMultiReading = false;
        tvTags.setText("Esperando tag...");
        iminDevice.startInventory();
        logInfo("Esperando lectura sencilla iMin... (tagInventoryRawStartReading)");
    }

    private void toggleMultiRead() {
        if (iminDevice == null || !iminDevice.isConnected()) { toast("iMin desconectado"); return; }
        if (isMultiReading) {
            iminDevice.stopInventory();
            isMultiReading = false;
            showMultiSummary();
            btnMulti.setText("Lectura Múltiple");
            logInfo("Lectura múltiple detenida. tags únicos=" + multiReadTags.size());
        } else {
            isMultiReading = true; isSingleReading = false;
            multiReadTags.clear();
            iminDevice.startInventory();
            btnMulti.setText("Detener (0)");
            tvTags.setText("Escaneando...");
            logInfo("Lectura múltiple iMin iniciada... (tagInventoryRawStartReading)");
        }
    }

    // ─── Power ───────────────────────────────────────────────────────────────

    private void applyPower() {
        if (iminDevice == null || !iminDevice.isConnected()) { toast("iMin desconectado"); return; }
        int dBm = sbPower.getProgress() + 5; // offset to get dBm value
        logInfo("Aplicando potencia: " + dBm + " dBm (CMD.SET_READ_WRITE_POWER)");
        iminDevice.setPower(dBm);
    }

    // ─── Diagnostics ──────────────────────────────────────────────────────────

    private void showDiagnostic() {
        StringBuilder diag = new StringBuilder();
        diag.append("=== DIAGNÓSTICO iMin ===\n");
        diag.append("Modelo: ").append(Build.MODEL).append("\n");
        diag.append("SDK Android: ").append(Build.VERSION.SDK_INT).append("\n");
        diag.append("SDK iMin RFID: v1.0.3\n");
        diag.append("com.imin.peripherservice: ").append(isPackageInstalled("com.imin.peripherservice") ? "✅" : "❌").append("\n");
        diag.append("Estado conexión: ").append(iminDevice != null && iminDevice.isConnected() ? "CONECTADO" : "DESCONECTADO").append("\n");
        if (iminDevice instanceof IminReaderImpl) {
            diag.append(((IminReaderImpl) iminDevice).getDiagnosticInfo());
        }
        diag.append("Potencia seleccionada: ").append(sbPower.getProgress() + 5).append(" dBm\n");
        logInfo("Diagnóstico iMin ejecutado.");
        new AlertDialog.Builder(this)
                .setTitle("Diagnóstico iMin")
                .setMessage(diag.toString())
                .setPositiveButton("OK", null).show();
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    private void updateConnectionUI() {
        boolean c = iminDevice != null && iminDevice.isConnected();
        tvStatus.setText(c ? "CONECTADO" : "DESCONECTADO");
        tvStatus.setTextColor(c ? 0xFF00AA00 : 0xFFFF0000);
        tvDeviceInfo.setText(c ? "Dispositivo: " + iminDevice.getDeviceName() : "Dispositivo: --");
    }

    private void showMultiSummary() {
        StringBuilder sb = new StringBuilder("Tags Únicos: " + multiReadTags.size() + "\n\n");
        for (String epc : multiReadTags.keySet())
            sb.append("• ").append(epc).append(" (").append(multiReadTags.get(epc)).append("x)\n");
        new AlertDialog.Builder(this)
                .setTitle("Resumen iMin Multi-Read")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null).show();
    }

    // ─── IReaderListener ─────────────────────────────────────────────────────

    @Override
    public void onConnected(String readerName) {
        logInfo("iMin Conectado: " + readerName);
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onDisconnected() {
        logWarn("iMin Desconectado.");
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onConnectionError(String message) {
        logError("Error iMin: " + message);
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (tags == null || tags.isEmpty()) return;
        String epc = tags.get(0).getEpc();
        logInfo("Tag iMin: " + epc + " (RSSI=" + tags.get(0).getRssi() + ")");
        runOnUiThread(() -> {
            tvTags.setText(epc);
            if (isSingleReading) {
                iminDevice.stopInventory();
                isSingleReading = false;
                new AlertDialog.Builder(this)
                        .setTitle("Lectura Sencilla iMin")
                        .setMessage("EPC: " + epc + "\nRSSI: " + tags.get(0).getRssi())
                        .setPositiveButton("OK", null).show();
            } else if (isMultiReading) {
                for (ReaderTag t : tags)
                    multiReadTags.put(t.getEpc(), multiReadTags.getOrDefault(t.getEpc(), 0) + 1);
                btnMulti.setText("Detener (" + multiReadTags.size() + ")");
            }
        });
    }

    @Override
    public void onTrigger(boolean pressed) {
        logInfo("Trigger iMin: " + (pressed ? "Presionado" : "Liberado"));
    }

    @Override
    public void onStatusMessage(String message) {
        logInfo("[SDK] " + message);
    }

    // ─── Logging ─────────────────────────────────────────────────────────────

    private void logInfo(String msg) { log("INFO", msg); }
    private void logWarn(String msg) { log("WARN", msg); }
    private void logError(String msg) { log("ERROR", msg); }

    private void log(String level, String msg) {
        if (isFinishing() || isDestroyed()) return;
        String ts = timeFormat.format(new Date());
        if ("ERROR".equals(level)) Log.e(TAG, msg);
        else if ("WARN".equals(level)) Log.w(TAG, msg);
        else Log.i(TAG, msg);
        runOnUiThread(() -> {
            if (tvLog == null) return;
            tvLog.append("\n[" + ts + "][" + level + "] " + msg);
            logLineCount++;
            if (logLineCount > 300) {
                String text = tvLog.getText().toString();
                int br = text.indexOf('\n');
                if (br >= 0) { tvLog.setText(text.substring(br + 1)); logLineCount = 300; }
            }
            if (tvLog.getLayout() != null) {
                int scroll = tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight();
                if (scroll > 0) tvLog.scrollTo(0, scroll);
            }
        });
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
