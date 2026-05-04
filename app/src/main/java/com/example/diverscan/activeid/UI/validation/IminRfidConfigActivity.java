package com.example.diverscan.activeid.UI.validation;

import android.app.AlertDialog;
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
import com.imin.rfid.RFIDManager;
import com.imin.rfid.RFIDHelper;
import com.imin.rfid.ReaderCall;
import com.imin.rfid.constant.CMD;
import com.imin.rfid.constant.ParamCts;
import com.imin.rfid.entity.DataParameter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * iMin RFID Config & Test Activity.
 *
 * SECCIÓN PRINCIPAL — Prueba Directa SDK (sin patrón):
 *   btn_direct_connect    → RFIDManager.getInstance().connect()
 *   btn_direct_disconnect → unregisterReaderCall() + stopReading()
 *   btn_direct_single     → tagInventoryAsyncFastStartReading()
 *   btn_direct_start      → tagInventoryRawStartReading()
 *   btn_direct_stop       → tagInventoryRawStopReading()
 *
 * SECCIÓN SECUNDARIA — Con patrón ReaderFactory/IReaderDevice:
 *   btn_imin_connect / btn_imin_disconnect / btn_imin_single / btn_imin_multi / btn_imin_stop
 */
public class IminRfidConfigActivity extends AppCompatActivity implements IReaderListener {

    private static final String TAG = "IminRfidConfig";

    // ── UI ────────────────────────────────────────────────────────────────────
    private TextView tvServiceStatus, tvStatus, tvDeviceInfo, tvTags, tvLog, tvPowerLabel;
    private SeekBar sbPower;

    // Sección DIRECTA (sin patrón)
    private Button btnDirectConnect, btnDirectDisconnect;
    private Button btnDirectSingle, btnDirectStart, btnDirectStop;

    // Sección PATRÓN (ReaderFactory)
    private Button btnConnect, btnDisconnect, btnSingleRead, btnStartInventory, btnStopInventory;
    private Button btnSetPower, btnDiagnostic, btnCheckService;

    // ── RFID State (DIRECTA) ──────────────────────────────────────────────────
    private RFIDHelper directHelper = null;
    private boolean directConnected = false;

    // ── RFID State (PATRÓN) ───────────────────────────────────────────────────
    private IReaderDevice iminDevice;
    private boolean isSingleReading = false;
    private boolean isMultiReading = false;
    private final Map<String, Integer> multiReadTags = new HashMap<>();

    // ── Utils ─────────────────────────────────────────────────────────────────
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private int logLineCount = 0;

    // ══════════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_config_imin);

        bindViews();
        setupListeners();

        tvLog.setMovementMethod(new ScrollingMovementMethod());

        // SeekBar: 5–33 dBm
        sbPower.setMax(28);
        sbPower.setProgress(25); // default 30 dBm
        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPowerLabel.setText((p + 5) + " dBm");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        logInfo("╔══════════════════════════════════════════╗");
        logInfo("║   SIGNUS iMin RFID Test — SDK v1.0.3    ║");
        logInfo("╚══════════════════════════════════════════╝");
        logInfo("Modelo: " + Build.MODEL + " | Android SDK: " + Build.VERSION.SDK_INT);
        logWarn("Use SECCIÓN DIRECTA para probar sin patrón de abstracción.");

        handler.postDelayed(this::checkIminService, 500);
    }

    private void bindViews() {
        tvServiceStatus  = findViewById(R.id.tv_imin_service_status);
        tvStatus         = findViewById(R.id.tv_imin_status);
        tvDeviceInfo     = findViewById(R.id.tv_imin_device_info);
        tvTags           = findViewById(R.id.tv_imin_tags);
        tvLog            = findViewById(R.id.tv_imin_log);
        tvPowerLabel     = findViewById(R.id.tv_imin_power_label);
        sbPower          = findViewById(R.id.sb_imin_power);

        btnCheckService  = findViewById(R.id.btn_imin_check_service);
        btnDiagnostic    = findViewById(R.id.btn_imin_diagnostic);

        // Sección DIRECTA
        btnDirectConnect    = findViewById(R.id.btn_direct_connect);
        btnDirectDisconnect = findViewById(R.id.btn_direct_disconnect);
        btnDirectSingle     = findViewById(R.id.btn_direct_single);
        btnDirectStart      = findViewById(R.id.btn_direct_start);
        btnDirectStop       = findViewById(R.id.btn_direct_stop);

        // Sección PATRÓN
        btnConnect          = findViewById(R.id.btn_imin_connect);
        btnDisconnect       = findViewById(R.id.btn_imin_disconnect);
        btnSingleRead       = findViewById(R.id.btn_imin_single);
        btnStartInventory   = findViewById(R.id.btn_imin_multi);
        btnStopInventory    = findViewById(R.id.btn_imin_stop);
        btnSetPower         = findViewById(R.id.btn_imin_set_power);
    }

    private void setupListeners() {
        // ── Service & diagnostics ──────────────────────────────────────────────
        btnCheckService.setOnClickListener(v -> checkIminService());
        btnDiagnostic.setOnClickListener(v -> showDiagnostic());

        // ── SECCIÓN DIRECTA ────────────────────────────────────────────────────
        btnDirectConnect.setOnClickListener(v -> directConnect());
        btnDirectDisconnect.setOnClickListener(v -> directDisconnect());
        btnDirectSingle.setOnClickListener(v -> directSingleRead());
        btnDirectStart.setOnClickListener(v -> directStartInventory());
        btnDirectStop.setOnClickListener(v -> directStopInventory());

        // ── SECCIÓN PATRÓN ─────────────────────────────────────────────────────
        btnConnect.setOnClickListener(v -> connectImin());
        btnDisconnect.setOnClickListener(v -> disconnectImin());
        btnSingleRead.setOnClickListener(v -> startSingleRead());
        btnStartInventory.setOnClickListener(v -> startMultiRead());
        btnStopInventory.setOnClickListener(v -> stopMultiRead());
        btnSetPower.setOnClickListener(v -> applyPower());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        directDisconnect();
        if (iminDevice != null) {
            try { iminDevice.dispose(); } catch (Exception ignored) {}
            iminDevice = null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SECCIÓN DIRECTA — Llamadas directas al SDK sin patrón
    // ══════════════════════════════════════════════════════════════════════════

    /** PASO 1: Conectar directamente al servicio iMin */
    private void directConnect() {
        logInfo("[DIRECT] ── Conectando al SDK iMin...");
        if (!isPackageInstalled("com.imin.peripherservice")) {
            logError("[DIRECT] ❌ com.imin.peripherservice NO encontrado en este dispositivo.");
            logError("[DIRECT] Este dispositivo NO es un iMin Lark 1. No puede usar el SDK.");
            return;
        }
        try {
            RFIDManager manager = RFIDManager.getInstance();
            manager.setPrintLog(true);
            manager.connect(this);
            logInfo("[DIRECT] rfidManager.connect() invocado → bind asíncrono en curso...");

            // Esperar 2s al bind y luego obtener helper + registrar callback
            handler.postDelayed(() -> {
                RFIDHelper helper = RFIDManager.getInstance().getHelper();
                if (helper != null) {
                    directHelper = helper;
                    directConnected = true;
                    directHelper.registerReaderCall(directReaderCall);
                    logInfo("[DIRECT] ✅ RFIDHelper obtenido y ReaderCall registrado.");
                    logInfo("[DIRECT] Listo para leer. Use los botones START / SINGLE.");
                    runOnUiThread(() -> {
                        tvStatus.setText("● CONECTADO (Directo)");
                        tvStatus.setTextColor(0xFF1B5E20);
                        tvDeviceInfo.setText("SDK Directo: RFIDManager.getHelper() ✓");
                    });
                } else {
                    logError("[DIRECT] ❌ RFIDHelper es NULL tras 2s — el servicio no respondió.");
                    logWarn("[DIRECT] Intente nuevamente o verifique que el servicio iMin esté activo.");
                }
            }, 2000);

        } catch (Exception e) {
            logError("[DIRECT] Excepción en connect: " + e.getMessage());
        }
    }

    /** Desconectar y limpiar helper directo */
    private void directDisconnect() {
        logInfo("[DIRECT] ── Desconectando...");
        try {
            if (directHelper != null) {
                try { directHelper.tagInventoryRawStopReading(); } catch (Exception ignored) {}
                try { directHelper.tagInventoryAsyncFastStopReading(); } catch (Exception ignored) {}
                directHelper.unregisterReaderCall();
                logInfo("[DIRECT] unregisterReaderCall() ✓");
            }
        } catch (Exception e) {
            logWarn("[DIRECT] Error en desconexión: " + e.getMessage());
        } finally {
            directHelper = null;
            directConnected = false;
            runOnUiThread(() -> {
                tvStatus.setText("● DESCONECTADO");
                tvStatus.setTextColor(0xFFD32F2F);
                tvDeviceInfo.setText("Dispositivo: --");
            });
            logInfo("[DIRECT] Estado: DESCONECTADO");
        }
    }

    /** LECTURA SENCILLA — tagInventoryAsyncFastStartReading() → auto-stop al primer tag */
    private void directSingleRead() {
        logInfo("[DIRECT-SINGLE] ── Iniciando lectura SENCILLA...");
        RFIDHelper helper = getDirectHelper();
        if (helper == null) return;
        try {
            helper.extendOperation(CMD.CLEAR_TAG, "");
            Thread.sleep(50);
            helper.tagInventoryAsyncFastStartReading();
            logInfo("[DIRECT-SINGLE] tagInventoryAsyncFastStartReading() → ENVIADO ✓");
            logInfo("[DIRECT-SINGLE] Se detendrá automáticamente al leer el primer tag.");
            // Timeout de seguridad: 5s
            handler.postDelayed(() -> {
                try {
                    RFIDHelper h = RFIDManager.getInstance().getHelper();
                    if (h != null) h.tagInventoryAsyncFastStopReading();
                } catch (Exception ignored) {}
                logWarn("[DIRECT-SINGLE] Timeout 5s — sin tags detectados.");
            }, 5000);
        } catch (Exception e) {
            logError("[DIRECT-SINGLE] Error: " + e.getMessage());
        }
    }

    /** LECTURA MÚLTIPLE START — tagInventoryRawStartReading() */
    private void directStartInventory() {
        logInfo("[DIRECT-START] ── Iniciando lectura MÚLTIPLE continua...");
        RFIDHelper helper = getDirectHelper();
        if (helper == null) return;
        try {
            helper.extendOperation(CMD.CLEAR_TAG, "");
            Thread.sleep(50);
            helper.tagInventoryRawStartReading();
            logInfo("[DIRECT-START] tagInventoryRawStartReading() → ENVIADO ✓");
            logInfo("[DIRECT-START] Inventario activo. Presione STOP para detener.");
            runOnUiThread(() -> tvTags.setText("Escaneando... (0 tags)"));
        } catch (Exception e) {
            logError("[DIRECT-START] Error: " + e.getMessage());
        }
    }

    /** STOP MÚLTIPLE — tagInventoryRawStopReading() */
    private void directStopInventory() {
        logInfo("[DIRECT-STOP] ── Deteniendo lectura múltiple...");
        RFIDHelper helper = getDirectHelper();
        if (helper == null) {
            // Intentar parar incluso si no tenemos referencia local
            try {
                RFIDHelper h = RFIDManager.getInstance().getHelper();
                if (h != null) {
                    h.tagInventoryRawStopReading();
                    h.tagInventoryAsyncFastStopReading();
                    logInfo("[DIRECT-STOP] Stop enviado (via getInstance) ✓");
                }
            } catch (Exception e) {
                logWarn("[DIRECT-STOP] Sin helper — " + e.getMessage());
            }
            return;
        }
        try {
            helper.tagInventoryRawStopReading();
            logInfo("[DIRECT-STOP] tagInventoryRawStopReading() → OK ✓");
            try { helper.tagInventoryAsyncFastStopReading(); } catch (Exception ignored) {}
        } catch (Exception e) {
            logError("[DIRECT-STOP] Error: " + e.getMessage());
        }
    }

    /** Obtiene el helper directo con validación */
    private RFIDHelper getDirectHelper() {
        if (directHelper != null) return directHelper;
        // Intentar recuperar
        try {
            RFIDHelper h = RFIDManager.getInstance().getHelper();
            if (h != null) {
                directHelper = h;
                return h;
            }
        } catch (Exception ignored) {}
        logWarn("[DIRECT] Helper es NULL — Presione CONECTAR primero.");
        return null;
    }

    /** Callback de tags para la sección DIRECTA */
    private int directTagCount = 0;
    private final ReaderCall directReaderCall = new ReaderCall() {
        @Override
        public void onSuccess(byte cmd, DataParameter data) {
            logInfo("[DIRECT-CB] onSuccess: CMD=0x" + String.format("%02X", cmd));
        }

        @Override
        public void onTag(byte cmd, byte state, DataParameter data) {
            if (data == null) return;
            String epc  = data.getString(ParamCts.TAG_EPC);
            String rssi = data.getString(ParamCts.TAG_RSSI);
            if (epc == null || epc.isEmpty()) return;

            directTagCount++;
            logInfo("[DIRECT-TAG] #" + directTagCount + " EPC=" + epc + " | RSSI=" + rssi + " dBm");
            runOnUiThread(() -> tvTags.setText("#" + directTagCount + " | " + epc + "\nRSSI: " + rssi + " dBm"));
        }

        @Override
        public void onFiled(byte cmd, byte error, String msg) {
            logError("[DIRECT-CB] onFailed: CMD=0x" + String.format("%02X", cmd)
                    + " | Error=0x" + String.format("%02X", error) + " | " + msg);
        }
    };

    // ══════════════════════════════════════════════════════════════════════════
    // SECCIÓN PATRÓN — Via ReaderFactory / IReaderDevice
    // ══════════════════════════════════════════════════════════════════════════

    private void connectImin() {
        logInfo("[PATTERN] Conectando via ReaderFactory...");
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
            logInfo("[PATTERN] Solicitando desconexión...");
        } else {
            logWarn("[PATTERN] No hay dispositivo activo.");
        }
    }

    private void startSingleRead() {
        if (iminDevice == null || !iminDevice.isConnected()) {
            logWarn("[PATTERN-SINGLE] Dispositivo no conectado.");
            return;
        }
        isSingleReading = true;
        isMultiReading  = false;
        tvTags.setText("Esperando tag único...");
        logInfo("[PATTERN-SINGLE] startSingleRead() (AsyncFast mode)...");
        if (iminDevice instanceof IminReaderImpl) {
            boolean ok = ((IminReaderImpl) iminDevice).startSingleRead();
            logInfo("[PATTERN-SINGLE] → " + (ok ? "OK ✓" : "FALLÓ ✗"));
        } else {
            iminDevice.startInventory();
        }
    }

    private void startMultiRead() {
        if (iminDevice == null || !iminDevice.isConnected()) {
            logWarn("[PATTERN-MULTI] Dispositivo no conectado.");
            return;
        }
        isMultiReading = true;
        isSingleReading = false;
        multiReadTags.clear();
        logInfo("[PATTERN-MULTI] Iniciando lectura múltiple...");
        boolean ok = iminDevice.startInventory();
        logInfo("[PATTERN-MULTI] startInventory() → " + (ok ? "OK ✓" : "FALLÓ ✗"));
        tvTags.setText("Escaneando... (0 tags)");
    }

    private void stopMultiRead() {
        if (iminDevice == null || !iminDevice.isConnected()) {
            logWarn("[PATTERN-STOP] Dispositivo no conectado.");
            return;
        }
        logInfo("[PATTERN-STOP] stopInventory()...");
        iminDevice.stopInventory();
        isMultiReading = false;
        logInfo("[PATTERN-STOP] Detenido. Tags únicos: " + multiReadTags.size());
        if (!multiReadTags.isEmpty()) showMultiSummary();
    }

    private void applyPower() {
        if (iminDevice == null || !iminDevice.isConnected()) { toast("Conecte primero."); return; }
        int dBm = sbPower.getProgress() + 5;
        logInfo("[POWER] Aplicando: " + dBm + " dBm");
        iminDevice.setPower(dBm);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IReaderListener (callbacks del PATRÓN)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onConnected(String readerName) {
        logInfo("[PATTERN] ✅ CONECTADO: " + readerName);
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onDisconnected() {
        logWarn("[PATTERN] DESCONECTADO.");
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onConnectionError(String message) {
        logError("[PATTERN] ERROR: " + message);
        runOnUiThread(this::updateConnectionUI);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (tags == null || tags.isEmpty()) return;
        ReaderTag first = tags.get(0);
        String epc  = first.getEpc();
        short  rssi = first.getRssi();
        logInfo("[TAG] EPC=" + epc + " | RSSI=" + rssi);

        runOnUiThread(() -> {
            tvTags.setText(epc);
            if (isSingleReading) {
                isSingleReading = false;
                new AlertDialog.Builder(this)
                        .setTitle("Lectura Sencilla ✓")
                        .setMessage("EPC : " + epc + "\nRSSI: " + rssi + " dBm")
                        .setPositiveButton("OK", null).show();
            } else if (isMultiReading) {
                multiReadTags.put(epc, multiReadTags.getOrDefault(epc, 0) + 1);
                int unique = multiReadTags.size();
                tvTags.setText(epc + "\n(" + unique + " únicos)");
            }
        });
    }

    @Override public void onTrigger(boolean pressed) { logInfo("Trigger: " + (pressed ? "▼" : "▲")); }
    @Override public void onStatusMessage(String message) { logInfo("[SDK] " + message); }

    // ══════════════════════════════════════════════════════════════════════════
    // Service Check & Diagnostics
    // ══════════════════════════════════════════════════════════════════════════

    private void checkIminService() {
        boolean svc  = isPackageInstalled("com.imin.peripherservice");
        boolean rfid = isPackageInstalled("com.imin.rfid");
        String status = "com.imin.peripherservice: " + (svc ? "✅ INSTALADO" : "❌ AUSENTE") + "\n"
                      + "com.imin.rfid: " + (rfid ? "✅ INSTALADO" : "⚠ Opcional");
        tvServiceStatus.setText(status);
        if (svc) logInfo("✅ Servicio iMin detectado. Puede conectar.");
        else     logError("❌ com.imin.peripherservice NO encontrado. Este dispositivo no es iMin.");
    }

    private void showDiagnostic() {
        StringBuilder d = new StringBuilder("=== DIAGNÓSTICO iMin ===\n");
        d.append("Modelo Android   : ").append(Build.MODEL).append("\n");
        d.append("SDK Android      : ").append(Build.VERSION.SDK_INT).append("\n");
        d.append("peripherservice  : ").append(isPackageInstalled("com.imin.peripherservice") ? "✅" : "❌").append("\n");
        d.append("[DIRECTO] helper : ").append(directHelper != null ? "✅ OK" : "❌ NULL").append("\n");
        d.append("[DIRECTO] conectado: ").append(directConnected ? "✅ SÍ" : "❌ NO").append("\n");
        d.append("[PATRÓN]  device : ").append(iminDevice != null ? "✅ OK" : "❌ NULL").append("\n");
        d.append("[PATRÓN]  conectado: ").append(iminDevice != null && iminDevice.isConnected() ? "✅ SÍ" : "❌ NO").append("\n");
        if (iminDevice instanceof IminReaderImpl) {
            d.append(((IminReaderImpl) iminDevice).getDiagnosticInfo());
        }
        new AlertDialog.Builder(this).setTitle("Diagnóstico iMin").setMessage(d.toString())
                .setPositiveButton("OK", null).show();
    }

    private boolean isPackageInstalled(String pkg) {
        try { getPackageManager().getPackageInfo(pkg, 0); return true; }
        catch (android.content.pm.PackageManager.NameNotFoundException e) { return false; }
    }

    private void updateConnectionUI() {
        boolean c = iminDevice != null && iminDevice.isConnected();
        tvStatus.setText(c ? "● CONECTADO (Patrón)" : "● DESCONECTADO");
        tvStatus.setTextColor(c ? 0xFF1B5E20 : 0xFFD32F2F);
        tvDeviceInfo.setText(c ? "Dispositivo: " + iminDevice.getDeviceName() : "Dispositivo: --");
    }

    private void showMultiSummary() {
        StringBuilder sb = new StringBuilder("Tags Únicos: " + multiReadTags.size() + "\n\n");
        for (String epc : multiReadTags.keySet())
            sb.append("• ").append(epc).append(" (").append(multiReadTags.get(epc)).append("x)\n");
        new AlertDialog.Builder(this).setTitle("Resumen Multi-Read")
                .setMessage(sb.toString()).setPositiveButton("OK", null).show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Hardware Trigger (gatillo físico iMin)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (event.getRepeatCount() == 0 && isTriggerKey(keyCode)) {
            logInfo("[TRIGGER] Gatillo PRESIONADO (keyCode=" + keyCode + ") → StartInventory");
            if (directConnected) directStartInventory();
            else if (iminDevice != null && iminDevice.isConnected()) startMultiRead();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (isTriggerKey(keyCode)) {
            logInfo("[TRIGGER] Gatillo LIBERADO → StopInventory");
            if (directConnected) directStopInventory();
            else if (iminDevice != null && iminDevice.isConnected()) stopMultiRead();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean isTriggerKey(int keyCode) {
        return keyCode == android.view.KeyEvent.KEYCODE_F1
            || keyCode == android.view.KeyEvent.KEYCODE_F2
            || keyCode == android.view.KeyEvent.KEYCODE_F3
            || keyCode == android.view.KeyEvent.KEYCODE_F4
            || keyCode == android.view.KeyEvent.KEYCODE_BUTTON_L1
            || keyCode == android.view.KeyEvent.KEYCODE_BUTTON_R1
            || keyCode == 280;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Logging
    // ══════════════════════════════════════════════════════════════════════════

    private void logInfo(String msg)  { log("INFO",  msg); }
    private void logWarn(String msg)  { log("WARN",  msg); }
    private void logError(String msg) { log("ERROR", msg); }

    private void log(String level, String msg) {
        if (isFinishing() || isDestroyed()) return;
        if ("ERROR".equals(level)) Log.e(TAG, msg);
        else if ("WARN".equals(level)) Log.w(TAG, msg);
        else Log.i(TAG, msg);
        String ts = timeFormat.format(new Date());
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
