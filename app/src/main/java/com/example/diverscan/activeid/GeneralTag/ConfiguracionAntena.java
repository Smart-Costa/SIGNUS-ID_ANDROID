package com.example.diverscan.activeid.GeneralTag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.Utilities.PermissionUtils;

import java.util.List;

public class ConfiguracionAntena extends AppCompatActivity implements ResponseHandlerInterface{
    private static final String TAG = "RFID_CONFIG_ANTENA";
    private TextView txtPotencia;
    private TextView txtCnfActual;
    private SeekBar skPotencia;
    private ProgressBar pgPotencia;
    private TextView txtPorcentaje;
    private View mConfigurarAntena;
    private String Power;
    public String potenciaAntena;
    private int potenciaInicial;
    private Spinner spReaders;
    private Spinner spConexion;
    private Button btnTest;
    private TextView txtResultados;
    private boolean isScanning = false;
    private java.util.Set<String> uniqueTags = new java.util.HashSet<>();
    Context _context;
    Activity _activity;

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    TagWriter rfidHandler;
    private boolean setupCompleted = false;

    private RadioGroup rgReadingMode;
    private RadioButton rbSingle;
    private RadioButton rbMultiple;
    private TextView txtLogView;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_configurar_antena);
        _context = this;
        _activity = this;
        
        controles();

        // UI Initialization
        txtLogView = findViewById(R.id.txtLogView);
        rgReadingMode = findViewById(R.id.rgReadingMode);
        rbSingle = findViewById(R.id.rbSingle);
        rbMultiple = findViewById(R.id.rbMultiple);

        // Configuración de Scroll para Logs
        if (txtLogView != null) {
            txtLogView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        }

        validarYLoguearPermisos();
        eventos();
        try{
            Power = SharedPreferencesGetSet.leer_local("potenciaAntena", this);
            txtCnfActual.setText("Potencia actual: " + Power);
            skPotencia.setMax(300);
            potenciaInicial = Integer.parseInt(Power);

            if (potenciaInicial == 0) {
                skPotencia.setProgress(80);
            } else{
                skPotencia.setProgress(potenciaInicial);
            }
        }catch(Exception e){
        }

        try {
            txtPotencia.setText("Versión. " + _context.getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Configuración Zebra RFID");
        }

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(ConfiguracionAntena.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();

        initializeRfidSetupIfPermitted();

        /*new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                return TagWriter.getInstance().getAvailableReaderNames();
            }
            // ... (rest of old commented code)
        }.execute();*/
    }

    private void configurarSpinnerConexion() {
        if (spConexion == null) return;

        final List<String> connectionTypes = new java.util.ArrayList<>();
        for (ConnectionType type : ConnectionType.values()) {
            connectionTypes.add(type.name());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, connectionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConexion.setAdapter(adapter);

        // Set selection
        String savedConn = SharedPreferencesGetSet.leer_local("connection_type", _context);
        if (savedConn != null) {
            int index = connectionTypes.indexOf(savedConn);
            if (index >= 0) spConexion.setSelection(index);
        }

        spConexion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = connectionTypes.get(position);
                Log.d(TAG, "Selección de tipo de conexión cambiada a: " + selected);
                ConnectionType connType = ConnectionType.valueOf(selected);
                
                // Get current reader type from spinner if possible, or from handler
                String readerTypeStr = (spReaders != null && spReaders.getSelectedItem() != null) 
                                        ? (String) spReaders.getSelectedItem() 
                                        : "ZEBRA";
                
                com.example.diverscan.activeid.DeviceInterface.ReaderType readerType = 
                    com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA;
                try {
                    readerType = com.example.diverscan.activeid.DeviceInterface.ReaderType.valueOf(readerTypeStr);
                } catch (Exception e) {}

                // Save and update
                SharedPreferencesGetSet.guardar_local("connection_type", selected, _context);

                Log.d(TAG, "Actualizando ReaderType: " + readerType + " Conexión: " + connType);
                rfidHandler.setReaderType(readerType, connType);
                conectarLector();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void configurarSpinnerLectores() {
        if (spReaders == null) return;

        final List<String> readerTypes = java.util.Arrays.asList("ZEBRA", "IMIN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, readerTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReaders.setAdapter(adapter);

        // Set selection based on current TagWriter state
        try {
            String currentType = rfidHandler.getCurrentReaderType().name();
            int index = readerTypes.indexOf(currentType);
            if (index >= 0) {
                spReaders.setSelection(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listener for changes
        spReaders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = readerTypes.get(position);
                Log.d(TAG, "Selección de lector cambiada a: " + selectedType);
                try {
                    com.example.diverscan.activeid.DeviceInterface.ReaderType type = 
                        com.example.diverscan.activeid.DeviceInterface.ReaderType.valueOf(selectedType);
                    
                    if (rfidHandler.getCurrentReaderType() != type) {
                        Toast.makeText(_context, "Cambiando a " + selectedType + "...", Toast.LENGTH_SHORT).show();
                        
                        // Get current connection type
                        String connStr = (spConexion != null && spConexion.getSelectedItem() != null) 
                                         ? (String) spConexion.getSelectedItem() 
                                         : "AUTO";
                        ConnectionType connType = ConnectionType.valueOf(connStr);

                        Log.d(TAG, "Re-configurando TagWriter con Lector: " + type + " Conexión: " + connType);
                        rfidHandler.setReaderType(type, connType);
                        conectarLector();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al cambiar lector: " + e.getMessage(), e);
                    Toast.makeText(_context, "Error al cambiar lector: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> {
             Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
             if (msg.startsWith("Conectado")) {
                 txtCnfActual.setText("Estado: " + msg);
             }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            Log.d(TAG, "Gatillo presionado: " + pressed + " | Escaneando: " + isScanning);
            if (pressed) {
                if (!isScanning) {
                    uniqueTags.clear();
                    txtResultados.setText("Tags leídos: 0");
                    Log.d(TAG, "Iniciando inventario por gatillo");
                    rfidHandler.performInventory();
                    isScanning = true;
                    btnTest.setText("Detener");
                }
            } else {
                if (isScanning) {
                    Log.d(TAG, "Deteniendo inventario por gatillo");
                    rfidHandler.stopInventory();
                    isScanning = false;
                    btnTest.setText("Probar Lectura");
                }
            }
        });
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        
        boolean newTagsFound = false;
        for (ReaderTag tag : tagData) {
            String epc = tag.getEpc();
            if (epc != null && !epc.isEmpty()) {
                if (uniqueTags.add(epc)) {
                    newTagsFound = true;
                    Log.v(TAG, "Nuevo Tag encontrado: " + epc + " RSSI: " + tag.getRssi());
                }
            }
        }

        if (newTagsFound) {
            final int count = uniqueTags.size();
            runOnUiThread(() -> {
                txtResultados.setText("Tags leídos: " + count);
                Log.d(TAG, "Total tags únicos: " + count);
            });
        }

        // Handle Single Read Mode
        if (rgReadingMode.getCheckedRadioButtonId() == R.id.rbSingle) {
             runOnUiThread(() -> {
                 if (isScanning) {
                     Log.d(TAG, "Lectura Sencilla: Deteniendo inventario tras leer tags.");
                     rfidHandler.stopInventory();
                     isScanning = false;
                     btnTest.setText("Probar Lectura");
                 }
             });
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }
    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        validarYLoguearPermisos();
        initializeRfidSetupIfPermitted();
        if (!PermissionUtils.checkPermissions(this)) {
            return;
        }
        if (rfidHandler != null) {
            rfidHandler.updateContext(this);
            String status = rfidHandler.onResume(); // Check connection / Reconnect
            txtCnfActual.setText("Estado: " + status);
        }
        
        if (sessionActivate != null) {
            sessionActivate.cancel();
            sessionActivate.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.stopInventory(); // Stop scanning when backgrounded
        }
        
        if (sessionActivate != null) {
            sessionActivate.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionActivate != null) {
            sessionActivate.cancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initializeRfidSetupIfPermitted();
            } else {
                txtCnfActual.setText("Estado: permisos denegados");
                Toast.makeText(this, "Permisos Bluetooth/Ubicación denegados", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void conectarLector() {
        if (!PermissionUtils.checkPermissions(this)) {
            txtCnfActual.setText("Estado: permisos pendientes");
            logInfo("Intento de conexión fallido: permisos pendientes");
            Toast.makeText(_context, "Permisos pendientes para conexión RFID", Toast.LENGTH_SHORT).show();
            PermissionUtils.requestPermissions(this);
            return;
        }
        logInfo("Conectando lector...");
        if (rfidHandler != null) {
             String result = rfidHandler.onResume();
             Toast.makeText(_context, result, Toast.LENGTH_SHORT).show();
             txtCnfActual.setText("Estado: " + result);
        }
    }

    private void initializeRfidSetupIfPermitted() {
        if (!PermissionUtils.checkPermissions(this)) {
            PermissionUtils.requestPermissions(this);
            txtCnfActual.setText("Estado: permisos pendientes");
            logInfo("RFID Setup detenido: permisos pendientes. Solicitando...");
            return;
        }
        if (setupCompleted) return;
        logInfo("Inicializando Setup RFID...");
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }
        configurarSpinnerLectores();
        configurarSpinnerConexion();
        conectarLector();
        setupCompleted = true;
    }

    private void logInfo(String message) {
        Log.d(TAG, message);
        if (txtLogView != null) {
            runOnUiThread(() -> {
                txtLogView.append(message + "\n");
                int scrollAmount = txtLogView.getLayout() != null 
                    ? txtLogView.getLayout().getLineTop(txtLogView.getLineCount()) - txtLogView.getHeight()
                    : 0;
                if (scrollAmount > 0) {
                    txtLogView.scrollTo(0, scrollAmount);
                }
            });
        }
    }

    private void validarYLoguearPermisos() {
        logInfo("=== VALIDACIÓN DE PERMISOS AL INGRESAR A CONFIGURACIÓN ===");
        logInfo("Dispositivo Android API SDK: " + android.os.Build.VERSION.SDK_INT);
        String[] permissions = PermissionUtils.getRequiredPermissions();
        boolean allGranted = true;
        for (String permission : permissions) {
            int status = androidx.core.content.ContextCompat.checkSelfPermission(this, permission);
            boolean isGranted = (status == android.content.pm.PackageManager.PERMISSION_GRANTED);
            logInfo("Permiso: " + permission + " -> " + (isGranted ? "CONCEDIDO" : "DENEGADO"));
            if (!isGranted) {
                allGranted = false;
            }
        }
        logInfo("Resultado final de permisos: " + (allGranted ? "APROBADO" : "PENDIENTE"));
        logInfo("=========================================================");
    }

    public void controles(){
         mConfigurarAntena = findViewById(R.id.FConfigurarAntena);
         txtPotencia = findViewById(R.id.txtPotencia);
         skPotencia = findViewById(R.id.skPotencia);
         pgPotencia = findViewById(R.id.progressBar);
         txtPorcentaje = findViewById(R.id.txtPorcentaje);
         txtCnfActual = findViewById(R.id.txtUltimaConfiguracion);
         spReaders =   findViewById(R.id.spinnerLectores);
         spConexion = findViewById(R.id.spinnerConexion);
         btnTest = findViewById(R.id.btnTestLectura);
         txtResultados = findViewById(R.id.txtResultados);
    }

    public void eventos(){

        skPotencia.setOnSeekBarChangeListener(OnSeekPotencia);
        
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón Test presionado. Estado escaneo: " + isScanning);
                if (isScanning) {
                    rfidHandler.stopInventory();
                    isScanning = false;
                    btnTest.setText("Probar Lectura");
                } else {
                    uniqueTags.clear();
                    txtResultados.setText("Tags leídos: 0");
                    rfidHandler.performInventory();
                    isScanning = true;
                    btnTest.setText("Detener");
                }
            }
        });

    }

    public SeekBar.OnSeekBarChangeListener OnSeekPotencia = new SeekBar.OnSeekBarChangeListener(){

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser){

            txtPorcentaje.setText("" + progress);
            potenciaAntena = txtPorcentaje.getText().toString();
            SharedPreferencesGetSet.guardar_local("potenciaAntena", potenciaAntena, getApplicationContext());

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            txtCnfActual.setText("Potencia actual: " + seekBar.getProgress());
            if (rfidHandler != null) {
                rfidHandler.setAntennaPower(seekBar.getProgress());
            }
        }

    };





}
