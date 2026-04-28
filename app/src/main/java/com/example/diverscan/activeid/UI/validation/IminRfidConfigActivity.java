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
    // SDK: IminRfidSdk v1.0.3 | com.imin.peripherservice required
    // Lectura Sencilla → IminReaderImpl.startSingleRead() → tagInventoryAsyncFastStartReading()
    // Lectura Múltiple → IminReaderImpl.startInventory()  → tagInventoryRawStartReading()

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

        logInfo("╔══════════════════════════════════════════╗");
        logInfo("║  SIGNUS iMin RFID Config — v1.0.3 SDK   ║");
        logInfo("╚══════════════════════════════════════════╝");
        logInfo("Modelo: " + Build.MODEL + " | Android SDK: " + Build.VERSION.SDK_INT);
        logInfo("SDK iMin RFID: v1.0.3 | Requiere: com.imin.peripherservice");
        logInfo("Modos: SENCILLA=tagInventoryRawStartReading (con timeout) | MÚLTIPLE=tagInventoryRawStartReading");
        logWarn("Dispositivo DEBE ser iMin I24P01/Lark 1 con módulo RFID interno.");

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
        if (iminDevice == null || !iminDevice.isConnected()) {
            toast("iMin desconectado — conecte primero");
            logWarn("[SINGLE] Lectura sencilla abortada: dispositivo no conectado.");
            return;
        }
        isSingleReading = true;
        isMultiReading = false;
        tvTags.setText("Esperando tag único...");
        logInfo("[SINGLE] Iniciando Lectura SENCILLA → tagInventoryRawStartReading()");
        logInfo("[SINGLE] Se detendrá automáticamente al recibir el primer tag.");

        // Usar startSingleRead() del impl para modo sencilla correcto
        boolean started;
        if (iminDevice instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) {
            started = ((com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) iminDevice).startSingleRead();
            logInfo("[SINGLE] startSingleRead() (AsyncFast mode) → " + (started ? "OK ✓" : "FALLÓ ✗"));
        } else {
            // Fallback para otros dispositivos
            started = iminDevice.startInventory();
            logInfo("[SINGLE] startInventory() (fallback) → " + (started ? "OK ✓" : "FALLÓ ✗"));
        }
        if (!started) {
            isSingleReading = false;
            toast("Error iniciando lectura sencilla");
        }
    }

    private void toggleMultiRead() {
        if (iminDevice == null || !iminDevice.isConnected()) {
            toast("iMin desconectado — conecte primero");
            logWarn("[MULTI] Lectura múltiple abortada: dispositivo no conectado.");
            return;
        }
        if (isMultiReading) {
            // ── Detener lectura múltiple ──────────────────────────────────
            logInfo("[MULTI] Deteniendo lectura múltiple → tagInventoryRawStopReading()");
            iminDevice.stopInventory();
            isMultiReading = false;
            logInfo("[MULTI] Lectura múltiple DETENIDA. Tags únicos acumulados: " + multiReadTags.size());
            showMultiSummary();
            btnMulti.setText("Lectura Múltiple");
        } else {
            // ── Iniciar lectura múltiple ──────────────────────────────────
            isMultiReading = true;
            isSingleReading = false;
            multiReadTags.clear();
            logInfo("[MULTI] Iniciando Lectura MÚLTIPLE → tagInventoryRawStartReading()");
            logInfo("[MULTI] Inventario continuo activo. Presione \"Detener\" para finalizar.");
            boolean started = iminDevice.startInventory();
            logInfo("[MULTI] startInventory() → " + (started ? "OK ✓" : "FALLÓ ✗"));
            btnMulti.setText("Detener (0)");
            tvTags.setText("Escaneando... (0 tags)");
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
        logInfo("[SDK] ✅ iMin CONECTADO: " + readerName);
        logInfo("[SDK] RFIDHelper listo — puede iniciar lecturas");
        if (iminDevice instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) {
            logInfo(((com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) iminDevice).getDiagnosticInfo());
        }
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onDisconnected() {
        logWarn("[SDK] iMin DESCONECTADO.");
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onConnectionError(String message) {
        logError("[SDK] ERROR iMin: " + message);
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (tags == null || tags.isEmpty()) {
            Log.w(TAG, "[onTagRead] Lista de tags vacía — ignorado");
            return;
        }
        ReaderTag first = tags.get(0);
        String epc = first.getEpc();
        short rssi = first.getRssi();
        String mode = isSingleReading ? "SENCILLA" : (isMultiReading ? "MÚLTIPLE" : "IDLE");
        logInfo("[TAG] ══ Modo=" + mode + " | EPC=" + epc + " | RSSI=" + rssi + " | Total en callback=" + tags.size());

        runOnUiThread(() -> {
            tvTags.setText(epc);
            if (isSingleReading) {
                // El IminReaderImpl ya hace auto-stop, pero lo marcamos aquí también
                isSingleReading = false;
                logInfo("[SINGLE] Tag recibido → showing dialog");
                new AlertDialog.Builder(this)
                        .setTitle("Lectura Sencilla iMin ✓")
                        .setMessage("EPC : " + epc + "\nRSSI: " + rssi + " dBm\n\n" +
                                    "(Lectura Sencilla - tagInventoryRawStartReading)")
                        .setPositiveButton("OK", null)
                        .show();
            } else if (isMultiReading) {
                // Acumular todos los tags del callback
                for (ReaderTag t : tags) {
                    multiReadTags.put(t.getEpc(),
                            multiReadTags.getOrDefault(t.getEpc(), 0) + 1);
                }
                int unique = multiReadTags.size();
                btnMulti.setText("Detener (" + unique + ")");
                tvTags.setText(epc + "\n(" + unique + " únicos)");
                logInfo("[MULTI] Tags acumulados: " + unique + " únicos | Último EPC: " + epc);
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

    // ─── Hardware Trigger (Gatillo) ──────────────────────────────────────────

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        // En dispositivos iMin, el gatillo suele reportarse como uno de estos KeyCodes
        // Dependiendo del modelo podría ser distinto (280, F1, F2, F3, F4, BUTTON_L1/R1)
        if (event.getRepeatCount() == 0) {
            logInfo("[TRIGGER] onKeyDown: KeyCode=" + keyCode);
            if (isTriggerKey(keyCode)) {
                logInfo("[TRIGGER] Gatillo físico PRESIONADO → Iniciando lectura múltiple...");
                toggleMultiRead(); // o startSingleRead() según se requiera
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (isTriggerKey(keyCode)) {
            logInfo("[TRIGGER] Gatillo físico LIBERADO");
            // Si quieres que pare al soltar el gatillo, puedes llamar a stopInventory() aquí:
            // if (isMultiReading) toggleMultiRead(); 
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean isTriggerKey(int keyCode) {
        return keyCode == android.view.KeyEvent.KEYCODE_F1 ||
               keyCode == android.view.KeyEvent.KEYCODE_F2 ||
               keyCode == android.view.KeyEvent.KEYCODE_F3 ||
               keyCode == android.view.KeyEvent.KEYCODE_F4 ||
               keyCode == android.view.KeyEvent.KEYCODE_BUTTON_L1 ||
               keyCode == android.view.KeyEvent.KEYCODE_BUTTON_R1 ||
               keyCode == android.view.KeyEvent.KEYCODE_BUTTON_L2 ||
               keyCode == android.view.KeyEvent.KEYCODE_BUTTON_R2 ||
               keyCode == 280; // Custom key code for some iMin guns
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
