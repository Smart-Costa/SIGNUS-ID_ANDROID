package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import java.util.List;
import java.util.ArrayList;

public class IminFragment extends Fragment implements ResponseHandlerInterface, ConfigurationReaderActivity.KeyEventHandler {
    private static final String TAG = "ConfigLarkFragment";
    private TextView tvLastScanData;
    private TextView tvScanHistory;
    private TextView tvPowerValue;
    private android.widget.EditText etScannerInput;
    private android.widget.Spinner spConexion;
    private Button btnInitScanner;
    private Button btnClearHistory;
    private RadioGroup rgReaderMode;
    private LinearLayout layoutPower;
    private SeekBar sbPower;
    private Button btnValidateConnection;
    private Button btnDiagnostic;
    private Button btnSingleRead;
    private Button btnMultiRead;
    
    private TagWriter tagWriter;
    private StringBuilder historyLog = new StringBuilder();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    private boolean isScanning = false;
    private boolean isSingleRead = false;
    private android.content.BroadcastReceiver keyEventReceiver;
    
    // Keyboard Scan Buffer
    private StringBuilder scanBuffer = new StringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_imin_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        tagWriter = TagWriter.getInstance();
        
        // Auto-select based on saved preference or default to Scanner
        updateUIBasedOnSelection();
    }

    private void initViews(View view) {
        tvLastScanData = view.findViewById(R.id.tvLastScanData);
        tvScanHistory = view.findViewById(R.id.tvScanHistory);
        etScannerInput = view.findViewById(R.id.etScannerInput);
        btnInitScanner = view.findViewById(R.id.btnInitScanner);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        rgReaderMode = view.findViewById(R.id.rgReaderMode);
        layoutPower = view.findViewById(R.id.layoutPower);
        sbPower = view.findViewById(R.id.sbPower);
        tvPowerValue = view.findViewById(R.id.tvPowerValue);
        btnValidateConnection = view.findViewById(R.id.btnValidateConnection);
        btnDiagnostic = view.findViewById(R.id.btnDiagnostic);
        btnSingleRead = view.findViewById(R.id.btnSingleRead);
        btnMultiRead = view.findViewById(R.id.btnMultiRead);
        spConexion = view.findViewById(R.id.spinnerConexion);
        
        // Scroll for history
        if (tvScanHistory != null)
            tvScanHistory.setMovementMethod(new android.text.method.ScrollingMovementMethod());
    }

    private void setupListeners() {
        btnInitScanner.setOnClickListener(v -> toggleReading());
        
        btnValidateConnection.setOnClickListener(v -> {
            logToHistory("Validando Conexión...");
            initializeReader();
        });

        btnDiagnostic.setOnClickListener(v -> {
             logToHistory("Ejecutando diagnóstico...");
             if (tagWriter != null) {
                 String diag = tagWriter.getDiagnosticInfo();
                 logToHistory(diag);
                 
                 if (getContext() != null) {
                     new androidx.appcompat.app.AlertDialog.Builder(getContext())
                         .setTitle("Diagnóstico de Conexión")
                         .setMessage(diag)
                         .setPositiveButton("OK", null)
                         .show();
                 }
             }
        });

        btnSingleRead.setOnClickListener(v -> {
             isSingleRead = true;
             startReading();
        });

        btnMultiRead.setOnClickListener(v -> {
             isSingleRead = false;
             startReading();
        });
        
        btnClearHistory.setOnClickListener(v -> {
            historyLog.setLength(0);
            tvScanHistory.setText("");
            tvLastScanData.setText("---");
        });

        rgReaderMode.setOnCheckedChangeListener((group, checkedId) -> {
            stopReading(); // Stop current before switching
            updateUIBasedOnSelection();
            initializeReader(); // Re-init with new type
        });

        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPowerValue.setText(progress + " dBm");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (tagWriter != null) {
                    logToHistory("Set Power: " + seekBar.getProgress());
                    tagWriter.setAntennaPower(seekBar.getProgress());
                }
            }
        });
    }

    private void updateUIBasedOnSelection() {
        int selectedId = rgReaderMode.getCheckedRadioButtonId();
        if (selectedId == R.id.rbScanner) {
            layoutPower.setVisibility(View.GONE);
            btnInitScanner.setText("Activar Scanner");
            logToHistory("Modo seleccionado: Scanner (Barcode/QR)");
        } else {
            layoutPower.setVisibility(View.VISIBLE);
            btnInitScanner.setText("Iniciar Inventario RFID");
            logToHistory("Modo seleccionado: RFID (UHF)");
        }
    }

    private void initializeReader() {
        if (getActivity() == null) return;

        int selectedId = rgReaderMode.getCheckedRadioButtonId();
        ReaderType targetType;
        if (selectedId == R.id.rbScanner) {
            targetType = ReaderType.IMIN_SCANNER;
        } else {
            targetType = ReaderType.IMIN;
        }

        logToHistory("Inicializando Reader: " + targetType);
        
        try {
            if (tagWriter.isInitialized()) {
                tagWriter.updateContext(this);
            } else {
                tagWriter.onCreate(this);
            }
            
            // Force set type
            ConnectionType selectedConn = ConnectionType.AUTO;
            if (spConexion != null && spConexion.getSelectedItem() != null) {
                try {
                    selectedConn = ConnectionType.valueOf(spConexion.getSelectedItem().toString());
                } catch (Exception e) {}
            }
            tagWriter.setReaderType(targetType, selectedConn);
            
            // Connect
            String result = tagWriter.onResume(); // Calls connect()
            logToHistory("Resultado Conexión: " + result);
            
            // Validation Logic
            if (targetType == ReaderType.IMIN) {
                if (result != null && result.toLowerCase().contains("conectado")) {
                     logToHistory("Verificación: Módulo RFID responde correctamente.");
                }
            } else if (targetType == ReaderType.IMIN_SCANNER) {
                checkScannerSystemStatus();
                if (result != null && result.toLowerCase().contains("conectado")) {
                     logToHistory("Verificación: Scanner activo y escuchando.");
                }
            }
            
        } catch (Exception e) {
            logToHistory("Error inicializando: " + e.getMessage());
            Log.e(TAG, "Error initializing reader", e);
        }
    }

    private void checkScannerSystemStatus() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            java.lang.reflect.Method get = c.getMethod("get", String.class, String.class);
            String status = (String) get.invoke(c, "persist.sys.imin.scanner.status", "0");
            String msg = "Estado Sistema (Prop): " + status + " (" + ("1".equals(status) ? "Conectado" : "Desconectado") + ")";
            logToHistory(msg);
            Log.d(TAG, msg);
        } catch (Exception e) {
            logToHistory("Error verificando SystemProperties: " + e.getMessage());
        }
    }

    private void toggleReading() {
        if (isScanning) {
            stopReading();
        } else {
            isSingleRead = false; // Default button is continuous
            startReading();
        }
    }

    private void startReading() {
        logToHistory("Iniciando lectura...");
        if (tagWriter != null) {
            tagWriter.startRead(); // Calls performInventory or equivalent
            isScanning = true;
            
            int selectedId = rgReaderMode.getCheckedRadioButtonId();
            if (selectedId == R.id.rbScanner) {
                btnInitScanner.setText("Scanner Activo (Esperando...)");
                // Request focus to capture keyboard input
                if (etScannerInput != null) {
                    etScannerInput.requestFocus();
                }
            } else {
                btnInitScanner.setText("Detener Inventario RFID");
            }
        }
    }

    private void stopReading() {
        if (!isScanning) return;
        
        logToHistory("Deteniendo lectura...");
        if (tagWriter != null) {
            tagWriter.stopRead();
        }
        isScanning = false;
        
        int selectedId = rgReaderMode.getCheckedRadioButtonId();
        if (selectedId == R.id.rbScanner) {
            btnInitScanner.setText("Activar Scanner");
        } else {
            btnInitScanner.setText("Iniciar Inventario RFID");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Register iMin Key Event Receiver for side buttons
        registerKeyReceiver();
        
        if (tagWriter != null && tagWriter.isInitialized()) {
             tagWriter.updateContext(this);
             // Not forcing re-init to allow tab switching without breaking
             // But if we are in this fragment, we probably want Imin active?
             // Maybe wait for user action or check if current reader matches?
        } else {
             initializeReader();
        }
        tagWriter.setResponseHandler(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopReading();
        if (keyEventReceiver != null) {
            try {
                if (getActivity() != null)
                    getActivity().unregisterReceiver(keyEventReceiver);
                keyEventReceiver = null;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering key receiver", e);
            }
        }
    }
    
    private void registerKeyReceiver() {
        if (keyEventReceiver == null && getActivity() != null) {
            keyEventReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(Context context, android.content.Intent intent) {
                    if ("com.imin.keyevent".equals(intent.getAction())) {
                        int keyCode = intent.getIntExtra("keycode", 0);
                        Log.d(TAG, "iMin Key Event: " + keyCode);
                        logToHistory("Evento Tecla iMin: " + keyCode);
                    }
                }
            };
            android.content.IntentFilter filter = new android.content.IntentFilter();
            filter.addAction("com.imin.keyevent");
            getActivity().registerReceiver(keyEventReceiver, filter);
        }
    }

    @Override
    public void SetMessage(String message) {
        logToHistory("INFO: " + message);
    }

    private void updateScanUI(String data, String source, String rssi) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                String sourceLabel = (source != null && !source.isEmpty()) ? " [" + source + "]" : "";
                tvLastScanData.setText(data + sourceLabel);
                
                String logEntry = data + sourceLabel + (rssi.isEmpty() ? "" : " (" + rssi + " dBm)");
                logToHistory("Lectura: " + logEntry);
                
                if (isSingleRead) {
                    stopReading();
                    logToHistory("Lectura Sencilla completada.");
                }
            });
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData != null && tagData.length > 0) {
            String data = tagData[0].getEpc();
            String rssi = (tagData[0].getRssi() != 0) ? String.valueOf(tagData[0].getRssi()) : "";
            
            // Determine source based on active mode
            String source = "";
            int selectedId = rgReaderMode.getCheckedRadioButtonId();
            if (selectedId == R.id.rbScanner) {
                source = "Botón/Broadcast";
            }
            
            updateScanUI(data, source, rssi);
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        logToHistory("Gatillo: " + (pressed ? "Presionado" : "Liberado"));
        if (pressed) {
            if (!isScanning) startReading();
        } else {
            int selectedId = rgReaderMode.getCheckedRadioButtonId();
            if (selectedId == R.id.rbRfid && isScanning) {
                 stopReading();
            }
        }
    }

    @Override
    public Context GetContext() {
        return getActivity();
    }

    private void logToHistory(String msg) {
        if (getActivity() == null) return;
        String time = sdf.format(new Date());
        String line = time + " - " + msg + "\n";
        getActivity().runOnUiThread(() -> {
            historyLog.insert(0, line);
            tvScanHistory.setText(historyLog.toString());
            Log.d(TAG, msg);
        });
    }

    @Override
    public boolean onDispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "Key Event received: Code=" + event.getKeyCode());
            
            // Handle iMin Scanner Trigger Key
            if (event.getKeyCode() == 170 || event.getKeyCode() == 139 || event.getKeyCode() == 289) {
                 logToHistory("Gatillo Scanner Presionado (Code " + event.getKeyCode() + ")");
                 return true; // Consumed
            }

            char pressedKey = (char) event.getUnicodeChar();
            
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String scannedData = scanBuffer.toString().trim();
                if (!scannedData.isEmpty()) {
                    logToHistory("Barcode detected via Keyboard: " + scannedData);
                    updateScanUI(scannedData, "Teclado", "");
                }
                scanBuffer.setLength(0);
                return true;
            } else {
                if (pressedKey != 0) {
                    scanBuffer.append(pressedKey);
                }
            }
        }
        return false;
    }
}
