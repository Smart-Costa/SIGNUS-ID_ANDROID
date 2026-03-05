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
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import android.app.AlertDialog;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.zebra.rfid.api3.ENUM_TRANSPORT;
import android.widget.RadioGroup;
import android.widget.RadioButton;

public class ConnectionValidationActivity extends AppCompatActivity implements ResponseHandlerInterface, ActivoDao.LogListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView tvStatus;
    private TextView tvDevice;
    private TextView tvFoundDevices;
    private TextView tvLog;
    private Button btnReconnect;
    private Button btnTestSingle;
    private Button btnTestMulti;
    private RadioGroup rgTransport;
    private TagWriter rfidHandler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    private boolean isMultiReading = false;
    private boolean isSingleReading = false;
    private Map<String, Integer> multiReadTags = new HashMap<>();
    private ActivoDao activoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_validation);

        checkAndRequestPermissions();

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
        rgTransport = findViewById(R.id.rg_transport);

        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        btnReconnect.setOnClickListener(v -> {
            log("Reiniciando conexión...");
            
            // Set transport based on selection
            int checkedId = rgTransport.getCheckedRadioButtonId();
            com.example.diverscan.activeid.DeviceInterface.ConnectionType connType = com.example.diverscan.activeid.DeviceInterface.ConnectionType.BLUETOOTH;
            
            if (checkedId == R.id.rb_serial || checkedId == R.id.rb_usb) {
                connType = com.example.diverscan.activeid.DeviceInterface.ConnectionType.SERIAL_USB;
            }
            
            rfidHandler.setAutoDetect(false); 
            
            // Detect reader type or default to iMin if on iMin device
            com.example.diverscan.activeid.DeviceInterface.ReaderType type = rfidHandler.getCurrentReaderType();
            
            // Logic to determine reader type
            if (Build.MODEL.contains("I24P01") || Build.MODEL.contains("Lark 1")) {
                 // Internal reader usually
                 type = com.example.diverscan.activeid.DeviceInterface.ReaderType.IMIN;
            } else {
                 // Default to Zebra for external readers if not set
                 if (type == com.example.diverscan.activeid.DeviceInterface.ReaderType.UNKNOWN || type == null) {
                     type = com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA;
                 }
            }
            
            log("Reconectando como: " + type + " via " + connType);
            
            // If USB/Serial selected but type is iMin (internal), warn user or force type?
            // Assuming iMin uses internal serial which is fine.
            
            rfidHandler.setReaderType(type, connType);
        });

        btnTestSingle.setOnClickListener(v -> startSingleRead());
        btnTestMulti.setOnClickListener(v -> toggleMultiRead());

        updateUI();
        
        log("INFO: Si usa DataWedge, asegúrese de que el perfil para esta app tenga el Plugin RFID DESHABILITADO para permitir conexión directa por SDK.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
            rfidHandler.setValidationMode(true);
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.setValidationMode(false);
        }
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
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        final String epc = tagData[0].getEpc();

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
                    log("Tag leído: " + id);
                }
                updateMultiReadButton();
            });
        }
    }
    
    private void startSingleRead() {
        if (!rfidHandler.isConnected()) {
            Toast.makeText(this, "Lector desconectado", Toast.LENGTH_SHORT).show();
            return;
        }
        isSingleReading = true;
        isMultiReading = false;
        rfidHandler.startRead();
        log("Esperando lectura sencilla...");
    }

    private void toggleMultiRead() {
        if (!rfidHandler.isConnected()) {
            Toast.makeText(this, "Lector desconectado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isMultiReading) {
            stopReading();
            isMultiReading = false;
            showMultiReadSummary();
            btnTestMulti.setText("Lectura Múltiple");
        } else {
            isMultiReading = true;
            isSingleReading = false;
            multiReadTags.clear();
            rfidHandler.startRead();
            btnTestMulti.setText("Detener (0)");
            log("Iniciando lectura múltiple...");
        }
    }

    private void stopReading() {
        try {
            rfidHandler.stopRead();
        } catch (Exception e) {
            log("Error deteniendo lectura: " + e.getMessage());
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
        // Ejecutar consultas de base de datos en hilo secundario para evitar congelar la UI
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
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Resumen Lectura Múltiple")
                        .setMessage(message)
                        .setPositiveButton("Cerrar", null)
                        .show();
            });
        }).start();
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

    // Implementación de ActivoDao.LogListener
    @Override
    public void onLog(String message) {
        log(message);
    }
}
