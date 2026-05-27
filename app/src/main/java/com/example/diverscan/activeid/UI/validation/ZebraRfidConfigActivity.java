package com.example.diverscan.activeid.UI.validation;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Zebra-specific RFID configuration screen.
 * Uses Zebra RFID SDK API3 v2.0.5.226.
 * Tests: USB, Serial & Bluetooth transport, USB device inspection, DataWedge toggle,
 *        single tag read, multi tag read, antenna power index.
 */
public class ZebraRfidConfigActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final String TAG = "ZebraRfidConfig";
    private static final String ACTION_USB_PERMISSION = "com.example.diverscan.activeid.USB_PERMISSION";
    private static final int PERMISSION_REQUEST_CODE = 200;

    // UI
    private TextView tvStatus, tvDeviceInfo, tvFoundDevices, tvLog, tvPowerLabel;
    private Button btnReconnect, btnSingle, btnMulti, btnUsbInspect, btnDiagnostic, btnToggleDW;
    private RadioGroup rgTransport;
    private SeekBar sbPower;

    // RFID
    private TagWriter rfidHandler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    // Read state
    private boolean isSingleReading = false;
    private boolean isMultiReading = false;
    private Map<String, Integer> multiReadTags = new HashMap<>();
    private int logLineCount = 0;

    private boolean dataWedgeEnabled = true;

    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                String name = device != null ? device.getDeviceName() : "unknown";
                if (granted) logInfo("Permiso USB concedido: " + name);
                else logWarn("Permiso USB denegado: " + name);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_config_zebra);

        tvStatus       = findViewById(R.id.tv_zebra_status);
        tvDeviceInfo   = findViewById(R.id.tv_zebra_device_info);
        tvFoundDevices = findViewById(R.id.tv_zebra_found_devices);
        tvLog          = findViewById(R.id.tv_zebra_log);
        tvPowerLabel   = findViewById(R.id.tv_zebra_power_label);
        btnReconnect   = findViewById(R.id.btn_zebra_reconnect);
        btnSingle      = findViewById(R.id.btn_zebra_single);
        btnMulti       = findViewById(R.id.btn_zebra_multi);
        btnUsbInspect  = findViewById(R.id.btn_zebra_usb_inspect);
        btnDiagnostic  = findViewById(R.id.btn_zebra_diagnostic);
        btnToggleDW    = findViewById(R.id.btn_toggle_datawedge);
        rgTransport    = findViewById(R.id.rg_zebra_transport);
        sbPower        = findViewById(R.id.sb_zebra_power);

        tvLog.setMovementMethod(new ScrollingMovementMethod());

        // Restore last known Zebra connection type
        String lastConn = SharedPreferencesGetSet.leer_local("zebra_connection_type_last_ok", this);
        if ("USB".equalsIgnoreCase(lastConn)) {
            rgTransport.check(R.id.rb_zebra_usb);
        } else if ("BLUETOOTH".equalsIgnoreCase(lastConn)) {
            rgTransport.check(R.id.rb_zebra_bluetooth);
        } else {
            rgTransport.check(R.id.rb_zebra_serial);
        }

        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) rfidHandler.onCreate(this);
        else rfidHandler.setResponseHandler(this);

        sbPower.setMax(270);
        sbPower.setProgress(270);
        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPowerLabel.setText("Índice: " + p);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                logInfo("Potencia antena (índice): " + sb.getProgress());
                rfidHandler.setAntennaPower(sb.getProgress());
            }
        });

        btnReconnect.setOnClickListener(v -> reconnect());
        btnSingle.setOnClickListener(v -> startSingleRead());
        btnMulti.setOnClickListener(v -> toggleMultiRead());
        btnUsbInspect.setOnClickListener(v -> inspectUsb(true));
        btnDiagnostic.setOnClickListener(v -> showDiagnostic());
        btnToggleDW.setOnClickListener(v -> toggleDataWedge());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        }

        requestPermissionsIfNeeded();
        updateUI();
        logInfo("Zebra Config UI lista. Modelo dispositivo: " + Build.MODEL);
        logWarn("SDK API3 v2.0.5.226 | Protocolo: SERIAL (TC-Series), USB (RFD-Series), BLUETOOTH (Sleds externos).");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) rfidHandler.setResponseHandler(this);
        disableDataWedge();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        enableDataWedge();
        try { unregisterReceiver(usbPermissionReceiver); } catch (Exception ignored) {}
        if (rfidHandler != null) rfidHandler.setResponseHandler(null);
    }

    // ─── Reconnect ────────────────────────────────────────────────────────────

    private void reconnect() {
        int id = rgTransport.getCheckedRadioButtonId();
        ConnectionType ct;
        if (id == R.id.rb_zebra_usb) {
            ct = ConnectionType.USB;
            inspectUsb(true);
        } else if (id == R.id.rb_zebra_bluetooth) {
            ct = ConnectionType.BLUETOOTH;
            logInfo("Buscando lectores Zebra via Bluetooth...");
        } else {
            ct = ConnectionType.SERIAL;
        }

        logInfo("Reconectando Zebra via " + ct + "...");
        rfidHandler.setReaderType(ReaderType.ZEBRA, ct);
        handler.postDelayed(this::updateUI, 2000);
    }

    // ─── Read Tests ───────────────────────────────────────────────────────────

    private void startSingleRead() {
        if (!rfidHandler.isConnected()) { toast("Zebra desconectada"); return; }
        isSingleReading = true; isMultiReading = false;
        rfidHandler.startRead();
        logInfo("Esperando lectura sencilla Zebra...");
    }

    private void toggleMultiRead() {
        if (!rfidHandler.isConnected()) { toast("Zebra desconectada"); return; }
        if (isMultiReading) {
            rfidHandler.stopRead(); isMultiReading = false;
            showMultiSummary(); btnMulti.setText("Lectura Múltiple");
        } else {
            isMultiReading = true; isSingleReading = false;
            multiReadTags.clear(); rfidHandler.startRead();
            btnMulti.setText("Detener (0)");
            logInfo("Lectura múltiple Zebra iniciada...");
        }
    }

    // ─── Diagnostics ─────────────────────────────────────────────────────────

    private void inspectUsb(boolean request) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) { logWarn("UsbManager no disponible"); return; }
        Map<String, UsbDevice> devs = usbManager.getDeviceList();
        if (devs == null || devs.isEmpty()) { logWarn("USB: 0 dispositivos"); return; }
        StringBuilder sb = new StringBuilder("USB Devices:\n");
        for (UsbDevice d : devs.values()) {
            boolean granted = usbManager.hasPermission(d);
            sb.append(String.format("• %s vid=%d pid=%d perm=%s%n",
                    d.getDeviceName(), d.getVendorId(), d.getProductId(), granted ? "✅" : "❌"));
            if (!granted && request) {
                PendingIntent pi = PendingIntent.getBroadcast(this, d.getDeviceId(),
                        new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                try { usbManager.requestPermission(d, pi); } catch (Exception e) { logError("ReqPerm: " + e.getMessage()); }
            }
        }
        logInfo(sb.toString());
    }

    private void showDiagnostic() {
        StringBuilder diag = new StringBuilder();
        diag.append("Modelo: ").append(Build.MODEL).append("\n");
        diag.append("SDK Android: ").append(Build.VERSION.SDK_INT).append("\n");
        diag.append("SDK Zebra: API3 v2.0.5.226\n");
        diag.append("Estado: ").append(rfidHandler.isConnected() ? "CONECTADO" : "DESCONECTADO").append("\n");
        if (rfidHandler.isConnected()) {
            diag.append("Reader: ").append(rfidHandler.getReaderName()).append("\n");
            diag.append("Transporte: ").append(rfidHandler.getReaderModel()).append("\n");
        }
        diag.append("Índice potencia actual: ").append(sbPower.getProgress()).append("\n");
        String saved = SharedPreferencesGetSet.leer_local("zebra_connection_type_last_ok", this);
        diag.append("Última conexión OK: ").append(saved == null ? "N/A" : saved).append("\n");
        logInfo("Diagnóstico: " + diag.toString().replace("\n", " | "));
        new AlertDialog.Builder(this)
                .setTitle("Diagnóstico Zebra")
                .setMessage(diag.toString())
                .setPositiveButton("OK", null).show();
    }

    private void toggleDataWedge() {
        dataWedgeEnabled = !dataWedgeEnabled;
        if (dataWedgeEnabled) enableDataWedge(); else disableDataWedge();
    }

    private void disableDataWedge() {
        try {
            Intent i = new Intent("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", false);
            sendBroadcast(i); logWarn("DataWedge: deshabilitado.");
        } catch (Exception e) { logError("DataWedge disable failed: " + e.getMessage()); }
    }

    private void enableDataWedge() {
        try {
            Intent i = new Intent("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", true);
            sendBroadcast(i); logWarn("DataWedge: habilitado.");
        } catch (Exception e) { logError("DataWedge enable failed: " + e.getMessage()); }
    }

    // ─── UI ───────────────────────────────────────────────────────────────────

    private void updateUI() {
        boolean c = rfidHandler != null && rfidHandler.isConnected();
        tvStatus.setText(c ? "CONECTADO" : "DESCONECTADO");
        tvStatus.setTextColor(c ? 0xFF00AA00 : 0xFFFF0000);
        tvDeviceInfo.setText(c ? "Reader: " + rfidHandler.getReaderName() : "Dispositivo: --");
        List<String> devices = rfidHandler != null ? rfidHandler.getFoundDevices() : null;
        if (devices != null && !devices.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String d : devices) sb.append("• ").append(d).append("\n");
            tvFoundDevices.setText(sb.toString());
        } else {
            tvFoundDevices.setText("Buscando... (0)");
        }
    }

    private void showMultiSummary() {
        StringBuilder sb = new StringBuilder("Tags Únicos: " + multiReadTags.size() + "\n\n");
        for (String epc : multiReadTags.keySet()) sb.append("• ").append(epc).append(" (").append(multiReadTags.get(epc)).append("x)\n");
        new AlertDialog.Builder(this)
                .setTitle("Resumen Zebra Multi-Read")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null).show();
    }

    // ─── ResponseHandlerInterface ─────────────────────────────────────────────

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        String epc = tagData[0].getEpc();
        logInfo("Tag Zebra: " + epc);
        if (isSingleReading) {
            rfidHandler.stopRead(); isSingleReading = false;
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("Lectura Sencilla Zebra")
                    .setMessage("EPC: " + epc)
                    .setPositiveButton("OK", null).show());
        } else if (isMultiReading) {
            for (ReaderTag tag : tagData) {
                String id = tag.getEpc();
                multiReadTags.put(id, multiReadTags.getOrDefault(id, 0) + 1);
            }
            runOnUiThread(() -> btnMulti.setText("Detener (" + multiReadTags.size() + ")"));
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        logInfo("Trigger Zebra: " + (pressed ? "Presionado" : "Liberado"));
    }

    @Override
    public Context GetContext() { return this; }

    @Override
    public void SetMessage(String text) {
        if (!isFinishing() && !isDestroyed()) {
            String low = text == null ? "" : text.toLowerCase(Locale.ROOT);
            if (low.contains("error")) logError(text);
            else if (low.contains("warn") || low.contains("advert")) logWarn(text);
            else logInfo(text);
            runOnUiThread(this::updateUI);
        }
    }

    // ─── Permissions ──────────────────────────────────────────────────────────

    private void requestPermissionsIfNeeded() {
        List<String> needed = new ArrayList<>();
        
        // Bluetooth Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        
        if (!needed.isEmpty()) ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    // ─── Logging ──────────────────────────────────────────────────────────────

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

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
