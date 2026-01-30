package com.example.diverscan.activeid.GeneralTag;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConfiguracionLarkActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private static final String TAG = "ConfigLarkActivity";
    private TextView tvLastScanData;
    private TextView tvScanHistory;
    private TextView tvPowerValue;
    private Button btnInitScanner;
    private Button btnClearHistory;
    private RadioGroup rgReaderMode;
    private LinearLayout layoutPower;
    private SeekBar sbPower;
    private Button btnValidateConnection;
    private Button btnSingleRead;
    private Button btnMultiRead;
    
    private TagWriter tagWriter;
    private StringBuilder historyLog = new StringBuilder();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    private boolean isScanning = false;
    private boolean isSingleRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_lark);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración iMin Lark 1");
        }

        initViews();
        setupListeners();

        tagWriter = TagWriter.getInstance();
        
        // Auto-select based on saved preference or default to Scanner
        updateUIBasedOnSelection();
    }

    private void initViews() {
        tvLastScanData = findViewById(R.id.tvLastScanData);
        tvScanHistory = findViewById(R.id.tvScanHistory);
        tvPowerValue = findViewById(R.id.tvPowerValue);
        btnInitScanner = findViewById(R.id.btnInitScanner);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        rgReaderMode = findViewById(R.id.rgReaderMode);
        layoutPower = findViewById(R.id.layoutPower);
        sbPower = findViewById(R.id.sbPower);
        btnValidateConnection = findViewById(R.id.btnValidateConnection);
        btnSingleRead = findViewById(R.id.btnSingleRead);
        btnMultiRead = findViewById(R.id.btnMultiRead);
        
        // Scroll for history
        tvScanHistory.setMovementMethod(new android.text.method.ScrollingMovementMethod());
    }

    private void setupListeners() {
        btnInitScanner.setOnClickListener(v -> toggleReading());
        
        btnValidateConnection.setOnClickListener(v -> {
            logToHistory("Validando Conexión...");
            initializeReader();
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
            layoutPower.setVisibility(android.view.View.GONE);
            btnInitScanner.setText("Activar Scanner");
            logToHistory("Modo seleccionado: Scanner (Barcode/QR)");
        } else {
            layoutPower.setVisibility(android.view.View.VISIBLE);
            btnInitScanner.setText("Iniciar Inventario RFID");
            logToHistory("Modo seleccionado: RFID (UHF)");
        }
    }

    private void initializeReader() {
        int selectedId = rgReaderMode.getCheckedRadioButtonId();
        ReaderType targetType;
        if (selectedId == R.id.rbScanner) {
            targetType = ReaderType.IMIN_SCANNER;
        } else {
            targetType = ReaderType.IMIN;
        }

        logToHistory("Inicializando Reader: " + targetType);
        
        try {
            // Use updateContext if already initialized to avoid full re-init which might trigger auto-detect logic
            if (tagWriter.isInitialized()) {
                tagWriter.updateContext(this);
            } else {
                tagWriter.onCreate(this);
            }
            
            // Force set type
            tagWriter.setReaderType(targetType, ConnectionType.AUTO);
            
            // Connect
            String result = tagWriter.onResume(); // Calls connect()
            logToHistory("Resultado Conexión: " + result);
            
            // Validation Logic
            if (targetType == ReaderType.IMIN) {
                if (result != null && result.toLowerCase().contains("conectado")) {
                     logToHistory("Verificación: Módulo RFID responde correctamente.");
                }
            } else if (targetType == ReaderType.IMIN_SCANNER) {
                if (result != null && result.toLowerCase().contains("conectado")) {
                     logToHistory("Verificación: Scanner activo y escuchando.");
                }
            }
            
        } catch (Exception e) {
            logToHistory("Error inicializando: " + e.getMessage());
            Log.e(TAG, "Error initializing reader", e);
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
    protected void onResume() {
        super.onResume();
        // Avoid blind re-initialization which resets everything
        // Only initialize if TagWriter has no active reader or if context needs update
        if (tagWriter != null && tagWriter.isInitialized()) {
             // Just update context
             tagWriter.updateContext(this);
             
             // Check if we need to set specific type based on UI, 
             // but only if it mismatches current state to avoid loop.
             // For now, we trust user interaction to change type.
             // Just refreshing status.
             logToHistory("Retomando actividad...");
        } else {
             initializeReader();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReading();
        // We don't necessarily disconnect here to avoid reconnection lag, 
        // but if we switch activities we might want to.
    }

    // ResponseHandlerInterface Implementation

    @Override
    public void SetMessage(String message) {
        logToHistory("INFO: " + message);
    }

    // Helper to unify UI updates and show source
    private void updateScanUI(String data, String source, String rssi) {
        runOnUiThread(() -> {
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
            // For RFID usually hold-to-read, so stop when released?
            // Depends on logic.
            int selectedId = rgReaderMode.getCheckedRadioButtonId();
            if (selectedId == R.id.rbRfid && isScanning) {
                 stopReading();
            }
        }
    }

    @Override
    public android.content.Context GetContext() {
        return this;
    }

    private void logToHistory(String msg) {
        String time = sdf.format(new Date());
        String line = time + " - " + msg + "\n";
        runOnUiThread(() -> {
            historyLog.insert(0, line);
            tvScanHistory.setText(historyLog.toString());
            Log.d(TAG, msg);
        });
    }

    // Keyboard Scan Buffer
    private StringBuilder scanBuffer = new StringBuilder();

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
            // Log key code for debugging
            Log.d(TAG, "Key Event received: Code=" + event.getKeyCode());

            char pressedKey = (char) event.getUnicodeChar();
            
            // If it's Enter, we assume end of barcode
            if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER) {
                String scannedData = scanBuffer.toString().trim();
                if (!scannedData.isEmpty()) {
                    logToHistory("Barcode detected via Keyboard: " + scannedData);
                    
                    // Directly update UI with "Teclado" source
                    updateScanUI(scannedData, "Teclado", "");
                } else {
                     Log.d(TAG, "Enter pressed but buffer empty");
                }
                scanBuffer.setLength(0); // Clear buffer
            } else {
                // Append printable characters
                if (pressedKey != 0) {
                    scanBuffer.append(pressedKey);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}