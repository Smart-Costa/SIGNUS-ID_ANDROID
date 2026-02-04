package com.example.diverscan.activeid.GeneralTag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;

import java.util.List;

public class ZebraFragment extends Fragment implements ResponseHandlerInterface {
    private static final String TAG = "RFID_CONFIG_ZEBRA";
    private TextView txtPotencia;
    private TextView txtCnfActual;
    private SeekBar skPotencia;
    private ProgressBar pgPotencia;
    private TextView txtPorcentaje;
    private String Power;
    public String potenciaAntena;
    private int potenciaInicial;
    private Spinner spConexion;
    private Button btnConectar;
    private Button btnTest;
    private TextView txtResultados;
    private boolean isScanning = false;
    private java.util.Set<String> uniqueTags = new java.util.HashSet<>();
    
    private TagWriter rfidHandler;
    private RadioGroup rgReadingMode;
    private RadioButton rbSingle;
    private RadioButton rbMultiple;
    private TextView txtLogView;
    private StringBuilder logBuilder = new StringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zebra_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        checkPermissions();
        
        controles(view);

        // UI Initialization
        if (txtLogView != null) {
            txtLogView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        }

        eventos();
        
        try {
            Power = SharedPreferencesGetSet.leer_local("potenciaAntena", requireContext());
            txtCnfActual.setText("Potencia actual: " + Power);
            skPotencia.setMax(300);
            if (Power != null && !Power.isEmpty()) {
                potenciaInicial = Integer.parseInt(Power);
            } else {
                potenciaInicial = 270; // Default
            }

            if (potenciaInicial == 0) {
                skPotencia.setProgress(80);
            } else {
                skPotencia.setProgress(potenciaInicial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            txtPotencia.setText("Versión. " + requireContext().getPackageManager().getPackageInfo(
                    requireContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Inicializar TagWriter (Singleton)
        rfidHandler = TagWriter.getInstance();
        
        configurarSpinnerConexion();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (!checkPermissions()) {
            Log.w(TAG, "onResume: Permissions missing, skipping initialization.");
            return;
        }

        if (rfidHandler != null) {
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.updateContext(this);
            }
            rfidHandler.setResponseHandler(this);
            
            // Force set reader type to ZEBRA when on this tab
            try {
                if (rfidHandler.getCurrentReaderType() != com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA) {
                    String savedConn = SharedPreferencesGetSet.leer_local("connection_type", requireContext());
                    ConnectionType type = ConnectionType.AUTO;
                    if (savedConn != null) {
                        try {
                            type = ConnectionType.valueOf(savedConn);
                        } catch (IllegalArgumentException e) {
                            type = ConnectionType.AUTO;
                        }
                    }
                    rfidHandler.setReaderType(com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA, type);
                }
            } catch (Exception e) {}
        }
        
        // Attempt connection if needed
        if (rfidHandler != null && !rfidHandler.isConnected()) {
             // Optional: Auto-connect if preferred, or just let user click Connect
             // conectarLector(); 
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isScanning) {
            rfidHandler.stopInventory();
            isScanning = false;
            btnTest.setText("Probar Lectura");
        }
    }

    private void configurarSpinnerConexion() {
        if (spConexion == null) return;

        final List<String> connectionTypes = new java.util.ArrayList<>();
        for (ConnectionType type : ConnectionType.values()) {
            connectionTypes.add(type.name());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, connectionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConexion.setAdapter(adapter);

        // Set selection
        String savedConn = SharedPreferencesGetSet.leer_local("connection_type", requireContext());
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
                
                // Save
                SharedPreferencesGetSet.guardar_local("connection_type", selected, requireContext());
                
                // Re-configure TagWriter for ZEBRA
                Log.d(TAG, "Configurando TagWriter para ZEBRA con Conexión: " + connType);
                rfidHandler.setReaderType(com.example.diverscan.activeid.DeviceInterface.ReaderType.ZEBRA, connType);
                // conectarLector(); // Removed auto-connect on spinner change to avoid loops. Use Connect button.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void conectarLector() {
        Log.d(TAG, "Iniciando tarea de conexión en segundo plano...");
        logToView("Intentando conectar...");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                if (getActivity() == null) return "Error: Activity is null";
                rfidHandler.updateContext(ZebraFragment.this);
                return rfidHandler.onResume(); // Calls connect()
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, "Resultado conexión: " + result);
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            result.isEmpty() ? "Lector ya conectado" : result,
                            Toast.LENGTH_SHORT).show();
                    if (result.contains("Conectado")) {
                        txtCnfActual.setText("Estado: " + result);
                    }
                }
            }
        }.execute();
    }

    public void controles(View view){
         txtPotencia = view.findViewById(R.id.txtPotencia);
         skPotencia = view.findViewById(R.id.skPotencia);
         pgPotencia = view.findViewById(R.id.progressBar);
         txtPorcentaje = view.findViewById(R.id.txtPorcentaje);
         txtCnfActual = view.findViewById(R.id.txtUltimaConfiguracion);
         spConexion = view.findViewById(R.id.spinnerConexion);
         btnConectar = view.findViewById(R.id.btnConectar);
         btnTest = view.findViewById(R.id.btnTestLectura);
         txtResultados = view.findViewById(R.id.txtResultados);
         
         txtLogView = view.findViewById(R.id.txtLogView);
         rgReadingMode = view.findViewById(R.id.rgReadingMode);
         rbSingle = view.findViewById(R.id.rbSingle);
         rbMultiple = view.findViewById(R.id.rbMultiple);
    }

    public void eventos(){
        skPotencia.setOnSeekBarChangeListener(OnSeekPotencia);
        
        btnConectar.setOnClickListener(v -> conectarLector());

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
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            txtPorcentaje.setText("" + progress);
            potenciaAntena = txtPorcentaje.getText().toString();
            if (getContext() != null) {
                SharedPreferencesGetSet.guardar_local("potenciaAntena", potenciaAntena, getContext());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            txtCnfActual.setText("Potencia actual: " + seekBar.getProgress());
            if (rfidHandler != null) {
                rfidHandler.setAntennaPower(seekBar.getProgress());
            }
        }
    };

    private void logToView(String msg) {
        if (getActivity() == null || txtLogView == null) return;
        getActivity().runOnUiThread(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            logBuilder.insert(0, timestamp + ": " + msg + "\n");
            txtLogView.setText(logBuilder.toString());
        });
    }

    @Override
    public void SetMessage(String msg) {
        logToView(msg);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                 Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                 if (msg.startsWith("Conectado")) {
                     txtCnfActual.setText("Estado: " + msg);
                 }
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
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
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData != null && tagData.length > 0) {
            Log.d(TAG, "handleTagdata: Received " + tagData.length + " tags");
            // Run on UI Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ReaderTag tag : tagData) {
                            String epc = tag.getEpc();
                            Log.d(TAG, "Processing Tag: " + epc);
                            if (!uniqueTags.contains(epc)) {
                                uniqueTags.add(epc);
                                logToView("Tag: " + epc + " RSSI: " + tag.getRssi());
                            }
                        }
                        txtResultados.setText("Tags leídos: " + uniqueTags.size());

                        // Handle Single Read Mode
                        if (rgReadingMode != null && rgReadingMode.getCheckedRadioButtonId() == R.id.rbSingle) {
                             if (isScanning) {
                                 Log.d(TAG, "Lectura Sencilla: Deteniendo inventario tras leer tags.");
                                 rfidHandler.stopInventory();
                                 isScanning = false;
                                 btnTest.setText("Probar Lectura");
                             }
                        }
                    }
                });
            }
        } else {
            Log.d(TAG, "handleTagdata: Received null or empty tagData");
        }
    }

    @Override
    public Context GetContext() {
        return getActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Log.d(TAG, "Permissions granted. Retrying connection...");
                logToView("Permisos otorgados. Reintentando conexión...");
                conectarLector();
            } else {
                Log.w(TAG, "Permissions denied.");
                logToView("Permisos denegados. No se puede conectar.");
                Toast.makeText(getContext(), "Se requieren permisos para conectar el lector", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkPermissions() {
        Log.d(TAG, "Checking permissions...");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            boolean missingConnect = androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED;
            boolean missingScan = androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_SCAN) != android.content.pm.PackageManager.PERMISSION_GRANTED;

            if (missingConnect || missingScan) {
                Log.w(TAG, "Bluetooth permissions missing (Android 12+). Requesting...");
                requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
                }, 100);
                return false;
            }
        } else {
             if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                 Log.w(TAG, "Location permission missing (Legacy Bluetooth). Requesting...");
                 requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                 return false;
             }
        }
        return true;
    }
}
