package com.example.diverscan.activeid.UI.validation;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
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
import android.util.Log;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import android.app.AlertDialog;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import android.widget.RadioGroup;

public class ConnectionValidationActivity extends AppCompatActivity
        implements ResponseHandlerInterface, ActivoDao.LogListener {

    private static final String TAG = "ConnValidation";
    private static final String ACTION_USB_PERMISSION = "com.example.diverscan.activeid.USB_PERMISSION";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MAX_LOG_LINES = 350;
    private TextView tvStatus;
    private TextView tvDevice;
    private TextView tvFoundDevices;
    private TextView tvLog;
    private Button btnReconnect;
    private Button btnTestSingle;
    private Button btnTestMulti;
    private Button btnRfidDiagnostic;
    private RadioGroup rgTransport;
    private TagWriter rfidHandler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private boolean isMultiReading = false;
    private boolean isSingleReading = false;
    private Map<String, Integer> multiReadTags = new HashMap<>();
    private ActivoDao activoDao;
    private int logLineCount = 0;
    private int triggerPressedCount = 0;
    private int triggerReleasedCount = 0;
    private String lastDevicesSnapshot = "";
    private final Handler statusHandler = new Handler(Looper.getMainLooper());
    private String lastStatusHash = null;
    private final Runnable statusProbe = new Runnable() {
        @Override
        public void run() {
            try {
                boolean initialized = rfidHandler != null && rfidHandler.isInitialized();
                boolean connected = rfidHandler != null && rfidHandler.isConnected();
                String name = rfidHandler != null ? rfidHandler.getReaderName() : null;
                String model = rfidHandler != null ? rfidHandler.getReaderModel() : null;
                List<String> devices = rfidHandler != null ? rfidHandler.getFoundDevices() : null;
                int devicesCount = devices != null ? devices.size() : 0;
                String status = "init=" + initialized + " conn=" + connected + " name=" + name + " model=" + model + " devices=" + devicesCount;
                if (!status.equals(lastStatusHash)) {
                    lastStatusHash = status;
                    logInfo("Probe: " + status);
                }
            } catch (Exception e) {
                logError("Probe error: " + e.getMessage());
            } finally {
                statusHandler.postDelayed(this, 5000L);
            }
        }
    };
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (device != null) {
                    if (granted) {
                        logInfo("Permiso USB concedido para: " + device.getDeviceName() + " (" + device.getVendorId() + ":" + device.getProductId() + ")");
                    } else {
                        logWarn("Permiso USB denegado para: " + device.getDeviceName() + " (" + device.getVendorId() + ":" + device.getProductId() + ")");
                    }
                } else {
                    logWarn("Resultado de permiso USB sin dispositivo asociado.");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_validation);

        activoDao = new ActivoDao(this);
        activoDao.setLogListener(this);

        tvStatus = findViewById(R.id.tv_connection_status);
        tvDevice = findViewById(R.id.tv_device_info);
        tvFoundDevices = findViewById(R.id.tv_found_devices);
        tvLog = findViewById(R.id.tv_log);
        tvLog.setMovementMethod(new ScrollingMovementMethod()); // Habilitar scroll

        btnReconnect = findViewById(R.id.btn_reconnect);
        btnTestSingle = findViewById(R.id.btn_test_single);
        btnTestMulti = findViewById(R.id.btn_test_multi);
        btnRfidDiagnostic = findViewById(R.id.btn_rfid_diagnostic);
        rgTransport = findViewById(R.id.rg_transport);
        String lastConn = SharedPreferencesGetSet.leer_local("zebra_connection_type_last_ok", this);
        if ("USB".equalsIgnoreCase(lastConn)) {
            rgTransport.check(R.id.rb_usb);
        } else if ("SERIAL".equalsIgnoreCase(lastConn)) {
            rgTransport.check(R.id.rb_serial);
        } else if ("BLUETOOTH".equalsIgnoreCase(lastConn)) {
            rgTransport.check(R.id.rb_bluetooth);
        } else {
            rgTransport.check(R.id.rb_serial);
        }

        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        logInfo("Sesión iniciada: modelo=" + Build.MODEL + " sdk=" + Build.VERSION.SDK_INT);

        btnReconnect.setOnClickListener(v -> {
            logInfo("Reiniciando conexión...");

            // Set transport based on selection
            int checkedId = rgTransport.getCheckedRadioButtonId();
            com.example.diverscan.activeid.DeviceInterface.ConnectionType connType = com.example.diverscan.activeid.DeviceInterface.ConnectionType.BLUETOOTH;

            if (checkedId == R.id.rb_usb) {
                connType = com.example.diverscan.activeid.DeviceInterface.ConnectionType.USB;
                inspectUsbState(true);
            } else if (checkedId == R.id.rb_serial) {
                connType = com.example.diverscan.activeid.DeviceInterface.ConnectionType.SERIAL;
            }

            com.example.diverscan.activeid.DeviceInterface.ReaderType type = com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA;

            logInfo("Reconectando como: " + type + " via " + connType);
            rfidHandler.setReaderType(type, connType);
            updateUI();
            try {
                List<String> devices = rfidHandler.getFoundDevices();
                logInfo("Dispositivos detectados tras reconexión: " + (devices == null ? 0 : devices.size()));
            } catch (Exception e) {
                logError("Error obteniendo dispositivos tras reconexión: " + e.getMessage());
            }
        });

        btnTestSingle.setOnClickListener(v -> startSingleRead());
        btnTestMulti.setOnClickListener(v -> toggleMultiRead());
        btnRfidDiagnostic.setOnClickListener(v -> runInlineDiagnostic());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        }

        checkAndRequestPermissions();
        updateUI();

        logWarn("Si usa DataWedge, deshabilite Plugin RFID para permitir conexión directa por SDK.");
        if (lastConn != null && !lastConn.isEmpty()) {
            logInfo("Conexión Zebra guardada: " + lastConn);
        } else {
            logInfo("Sin conexión Zebra guardada. Se usa SERIAL por defecto.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
            rfidHandler.setValidationMode(true);
        }
        logInfo("Pantalla en primer plano. Modo validación activado.");
        try {
            Intent i = new Intent();
            i.setAction("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", false);
            sendBroadcast(i);
            logWarn("DataWedge: deshabilitado temporalmente");
        } catch (Exception e) {
            logError("Error deshabilitando DataWedge: " + e.getMessage());
        }
        updateUI();
        statusHandler.removeCallbacks(statusProbe);
        statusHandler.post(statusProbe);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.setValidationMode(false);
        }
        logInfo("Pantalla en segundo plano. Modo validación desactivado.");
        statusHandler.removeCallbacks(statusProbe);
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
            logInfo("Estado conexión: CONECTADO (" + name + " / " + model + ")");
        } else {
            tvDevice.setText("Dispositivo: --");
            logWarn("Estado conexión: DESCONECTADO");
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
                if (!sb.toString().equals(lastDevicesSnapshot)) {
                    lastDevicesSnapshot = sb.toString();
                    logInfo("Dispositivos detectados: " + devices.size());
                }
            } else {
                tvFoundDevices.setText("Buscando... (0 encontrados)");
                if (!"EMPTY".equals(lastDevicesSnapshot)) {
                    lastDevicesSnapshot = "EMPTY";
                    logWarn("No se detectan lectores en este momento.");
                }
            }
        }
    }

    private void logInfo(String msg) {
        log("INFO", msg);
    }

    private void logWarn(String msg) {
        log("WARN", msg);
    }

    private void logError(String msg) {
        log("ERROR", msg);
    }

    private void log(String level, String msg) {
        if (isFinishing() || isDestroyed())
            return;
        String timestamp = timeFormat.format(new Date());
        if ("ERROR".equals(level)) {
            Log.e(TAG, msg);
        } else if ("WARN".equals(level)) {
            Log.w(TAG, msg);
        } else {
            Log.i(TAG, msg);
        }
        runOnUiThread(() -> {
            if (tvLog == null) {
                return;
            }
            tvLog.append("\n[" + timestamp + "][" + level + "] " + msg);
            logLineCount++;
            if (logLineCount > MAX_LOG_LINES) {
                String text = tvLog.getText().toString();
                int firstBreak = text.indexOf('\n');
                if (firstBreak >= 0 && firstBreak + 1 < text.length()) {
                    tvLog.setText(text.substring(firstBreak + 1));
                    logLineCount = MAX_LOG_LINES;
                }
            }
            if (tvLog.getLayout() != null) {
                final int scrollAmount = tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight();
                if (scrollAmount > 0)
                    tvLog.scrollTo(0, scrollAmount);
            }
        });
    }

    private void runInlineDiagnostic() {
        StringBuilder diag = new StringBuilder();
        diag.append("Modelo: ").append(Build.MODEL).append("\n");
        diag.append("SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        String savedConn = SharedPreferencesGetSet.leer_local("zebra_connection_type_last_ok", this);
        diag.append("Conexión guardada Zebra: ").append(savedConn == null || savedConn.isEmpty() ? "N/A" : savedConn).append("\n");
        int checkedId = rgTransport.getCheckedRadioButtonId();
        String selectedConn = "BLUETOOTH";
        if (checkedId == R.id.rb_usb) {
            selectedConn = "USB";
        } else if (checkedId == R.id.rb_serial) {
            selectedConn = "SERIAL";
        }
        diag.append("Conexión seleccionada UI: ").append(selectedConn).append("\n");
        diag.append("Estado conexión: ").append(rfidHandler != null && rfidHandler.isConnected() ? "CONECTADO" : "DESCONECTADO").append("\n");
        if (rfidHandler != null && rfidHandler.isConnected()) {
            diag.append("Reader: ").append(rfidHandler.getReaderName()).append("\n");
            diag.append("Modelo reader: ").append(rfidHandler.getReaderModel()).append("\n");
        }
        List<String> devices = rfidHandler != null ? rfidHandler.getFoundDevices() : null;
        diag.append("Dispositivos detectados: ").append(devices == null ? 0 : devices.size()).append("\n");
        diag.append(inspectUsbState(false)).append("\n");
        logInfo("Diagnóstico ejecutado");
        logInfo(diag.toString().replace("\n", " | "));
        new AlertDialog.Builder(this)
                .setTitle("Diagnóstico RFID")
                .setMessage(diag.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private String inspectUsbState(boolean requestIfMissingPermission) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            String msg = "USB manager: no disponible";
            logWarn(msg);
            return msg;
        }
        Map<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (usbDevices == null || usbDevices.isEmpty()) {
            String msg = "USB devices: 0";
            logWarn(msg);
            return msg;
        }
        int grantedCount = 0;
        int zebraCount = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("USB devices: ").append(usbDevices.size());
        for (UsbDevice d : usbDevices.values()) {
            boolean granted = usbManager.hasPermission(d);
            if (granted) {
                grantedCount++;
            }
            if (d.getVendorId() == 1504) {
                zebraCount++;
            }
            sb.append(" | ")
                    .append(d.getDeviceName())
                    .append(" vid:pid=")
                    .append(d.getVendorId())
                    .append(":")
                    .append(d.getProductId())
                    .append(" perm=")
                    .append(granted);
            if (!granted && requestIfMissingPermission) {
                PendingIntent permissionIntent = PendingIntent.getBroadcast(
                        this,
                        d.getDeviceId(),
                        new Intent(ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_IMMUTABLE
                );
                try {
                    usbManager.requestPermission(d, permissionIntent);
                    logWarn("Solicitando permiso USB para " + d.getDeviceName() + " (" + d.getVendorId() + ":" + d.getProductId() + ")");
                } catch (Exception e) {
                    logError("Error solicitando permiso USB: " + e.getMessage());
                }
            }
        }
        String summary = sb.toString() + " | ZebraUSB=" + zebraCount + " | USBPermisos=" + grantedCount;
        logInfo(summary);
        return summary;
    }

    // ResponseHandlerInterface implementation
    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0)
            return;

        final String epc = tagData[0].getEpc();
        logInfo("Tag recibido. cantidad=" + tagData.length + " epc=" + epc);

        if (isSingleReading) {
            runOnUiThread(() -> {
                stopReading();
                isSingleReading = false;
                showSingleTagDialog(epc);
            });
        } else if (isMultiReading) {
            runOnUiThread(() -> {
                for (ReaderTag tag : tagData) {
                    String id = tag.getEpc();
                    multiReadTags.put(id, multiReadTags.getOrDefault(id, 0) + 1);
                    logInfo("Tag leído: " + id);
                }
                updateMultiReadButton();
            });
        }
    }

    private void startSingleRead() {
        if (!rfidHandler.isConnected()) {
            Toast.makeText(this, "Lector desconectado", Toast.LENGTH_SHORT).show();
            logWarn("Lectura sencilla cancelada: lector desconectado.");
            return;
        }
        isSingleReading = true;
        isMultiReading = false;
        rfidHandler.startRead();
        logInfo("Esperando lectura sencilla...");
    }

    private void toggleMultiRead() {
        if (!rfidHandler.isConnected()) {
            Toast.makeText(this, "Lector desconectado", Toast.LENGTH_SHORT).show();
            logWarn("Lectura múltiple cancelada: lector desconectado.");
            return;
        }

        if (isMultiReading) {
            stopReading();
            isMultiReading = false;
            showMultiReadSummary();
            btnTestMulti.setText("Lectura Múltiple");
            logInfo("Lectura múltiple detenida. tags únicos=" + multiReadTags.size());
        } else {
            isMultiReading = true;
            isSingleReading = false;
            multiReadTags.clear();
            rfidHandler.startRead();
            btnTestMulti.setText("Detener (0)");
            logInfo("Iniciando lectura múltiple...");
        }
    }

    private void stopReading() {
        try {
            rfidHandler.stopRead();
        } catch (Exception e) {
            logError("Error deteniendo lectura: " + e.getMessage());
        }
    }

    private void updateMultiReadButton() {
        btnTestMulti.setText("Detener (" + multiReadTags.size() + ")");
    }

    private void showSingleTagDialog(String epc) {
        new Thread(() -> {
            ActivoEntity activo = activoDao.getActivoByEpc(epc);
            String mensaje = "EPC: " + epc + "\n";

            if (activo != null) {
                mensaje += "Estado: ENCONTRADO\n" +
                        "Activo: " + activo.getNumeroActivo() + "\n" +
                        "Desc: " + activo.getDescripcionCorta();
            } else {
                mensaje += "Estado: NO REGISTRADO EN BD LOCAL";
            }

            final String finalMsg = mensaje;
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Lectura Sencilla")
                        .setMessage(finalMsg)
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    private void showMultiReadSummary() {
        // Ejecutar consultas de base de datos en hilo secundario para evitar congelar
        // la UI
        new Thread(() -> {
            List<String> epcList = new ArrayList<>(multiReadTags.keySet());
            // Uso de consulta masiva optimizada
            List<ActivoEntity> foundAssets = activoDao.getActivosByEpcs(epcList);

            // Mapa para búsqueda rápida O(1)
            Map<String, ActivoEntity> assetMap = new HashMap<>();
            for (ActivoEntity a : foundAssets) {
                if (a.getEpc() != null) {
                    assetMap.put(a.getEpc(), a);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Total Tags Únicos: ").append(multiReadTags.size()).append("\n\n");

            int encontrados = 0;
            int desconocidos = 0;

            for (String epc : multiReadTags.keySet()) {
                ActivoEntity a = assetMap.get(epc);
                if (a != null) {
                    encontrados++;
                    sb.append("[OK] ").append(epc).append(" - ").append(a.getDescripcionCorta()).append("\n");
                } else {
                    desconocidos++;
                    sb.append("[UNK] ").append(epc).append("\n");
                }
            }

            sb.insert(0, "Encontrados: " + encontrados + " | Desconocidos: " + desconocidos + "\n");

            // Actualizar UI en el hilo principal
            String message = sb.toString();
            int totalEncontrados = encontrados;
            int totalDesconocidos = desconocidos;
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Resumen Lectura Múltiple")
                        .setMessage(message)
                        .setPositiveButton("Cerrar", null)
                        .show();
                logInfo("Resumen múltiple: encontrados=" + totalEncontrados + " desconocidos=" + totalDesconocidos);
            });
        }).start();
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            triggerPressedCount++;
        } else {
            triggerReleasedCount++;
        }
        logInfo("Gatillo: " + (pressed ? "Presionado" : "Liberado") + " [P=" + triggerPressedCount + " R=" + triggerReleasedCount + "]");
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Intent i = new Intent();
            i.setAction("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", true);
            sendBroadcast(i);
            logWarn("DataWedge: restaurado");
        } catch (Exception ignored) {}
        try {
            unregisterReceiver(usbPermissionReceiver);
        } catch (Exception ignored) {}
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(null);
        }
        logInfo("Pantalla destruida. Handler de respuesta liberado.");
    }

    @Override
    public void SetMessage(String Text) {
        if (!isFinishing() && !isDestroyed()) {
            String normalized = Text == null ? "" : Text.toLowerCase(Locale.ROOT);
            if (normalized.contains("error") || normalized.contains("fall")) {
                logError(Text);
            } else if (normalized.contains("warning") || normalized.contains("advert")) {
                logWarn(Text);
            } else {
                logInfo(Text);
            }
            runOnUiThread(this::updateDeviceList);
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+):
            // BLUETOOTH_SCAN con neverForLocation en manifest → NO requiere ACCESS_FINE_LOCATION
            // La ubicación SOLO es necesaria si el app la usa para inferirla vía BT/WiFi
            permissions = new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.CAMERA
                    // ACCESS_FINE_LOCATION no requerida para BT en API 31+ con neverForLocation
            };
        } else {
            // Android 6–11: ACCESS_FINE_LOCATION obligatoria para BT scanning
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
            };
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            logWarn("Solicitando permisos: " + listPermissionsNeeded);
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            logInfo("Permisos ya concedidos para conexión RFID.");
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
                logWarn("Permisos no concedidos. La conexión podría fallar.");
                Toast.makeText(this, "Permisos necesarios no concedidos", Toast.LENGTH_LONG).show();
            } else {
                logInfo("Permisos concedidos. Intentando conectar...");
                if (!rfidHandler.isInitialized()) {
                    rfidHandler.onCreate(this);
                } else {
                    rfidHandler.InitSDK();
                }
            }
        }
    }

    // Implementación de ActivoDao.LogListener
    @Override
    public void onLog(String message) {
        logInfo(message);
    }
}
