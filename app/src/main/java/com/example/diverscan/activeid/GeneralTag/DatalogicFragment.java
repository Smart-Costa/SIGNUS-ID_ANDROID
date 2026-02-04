package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.diverscan.activeid.DeviceInterface.ReaderType;
import com.example.diverscan.activeid.R;

import java.util.List;

public class DatalogicFragment extends Fragment implements ResponseHandlerInterface {
    private static final String TAG = "RFID_CONFIG_DATALOGIC";
    private TextView txtPotencia;
    private TextView txtCnfActual;
    private SeekBar skPotencia;
    private ProgressBar pgPotencia;
    private TextView txtPorcentaje;
    private String Power;
    public String potenciaAntena;
    private int potenciaInicial;
    private Spinner spConexion;
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
        return inflater.inflate(R.layout.fragment_datalogic_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        controles(view);

        // UI Initialization
        if (txtLogView != null) {
            txtLogView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        }

        eventos();
        
        // Simular configuración de potencia (aunque no aplica igual que RFID, mantenemos UI consistente)
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
        if (rfidHandler != null) {
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.updateContext(this);
            }
            rfidHandler.setResponseHandler(this);
            
            // Forzar tipo de lector a DATALOGIC cuando estamos en esta pestaña
            try {
                if (rfidHandler.getCurrentReaderType() != ReaderType.DATALOGIC) {
                     Log.d(TAG, "Cambiando ReaderType a DATALOGIC");
                     rfidHandler.setReaderType(ReaderType.DATALOGIC, ConnectionType.AUTO);
                }
            } catch (Exception e) {}
        }
        
        // Intentar conectar automáticamente
        conectarLector();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isScanning) {
            rfidHandler.stopInventory();
            isScanning = false;
            btnTest.setText("Probar Lectura");
        }
        // Desconectar al salir para liberar BarcodeManager?
        // rfidHandler.disconnect(); // Opcional, depende de si queremos mantenerlo vivo
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
    }

    private void conectarLector() {
        logToView("Inicializando Datalogic...");
        if (rfidHandler != null) {
            rfidHandler.setReaderType(ReaderType.DATALOGIC, ConnectionType.AUTO);
            // La conexión se maneja internamente en el TagWriter -> ReaderImpl
            // Podemos forzar un "connect" explícito si es necesario, o confiar en el onResume del Impl
            // En este diseño, TagWriter.onResume() llama a connect()
             rfidHandler.onResume(); 
        }
    }

    public void controles(View view){
         txtPotencia = view.findViewById(R.id.txtPotencia);
         skPotencia = view.findViewById(R.id.skPotencia);
         pgPotencia = view.findViewById(R.id.progressBar);
         txtPorcentaje = view.findViewById(R.id.txtPorcentaje);
         txtCnfActual = view.findViewById(R.id.txtUltimaConfiguracion);
         spConexion = view.findViewById(R.id.spinnerConexion);
         btnTest = view.findViewById(R.id.btnTestLectura);
         txtResultados = view.findViewById(R.id.txtResultados);
         
         txtLogView = view.findViewById(R.id.txtLogView);
         rgReadingMode = view.findViewById(R.id.rgReadingMode);
         rbSingle = view.findViewById(R.id.rbSingle);
         rbMultiple = view.findViewById(R.id.rbMultiple);
    }

    public void eventos(){
        skPotencia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                txtPorcentaje.setText("" + progress);
                potenciaAntena = txtPorcentaje.getText().toString();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txtCnfActual.setText("Potencia: " + seekBar.getProgress() + " (Simulado)");
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón Test presionado. Estado escaneo: " + isScanning);
                if (isScanning) {
                    rfidHandler.stopInventory();
                    isScanning = false;
                    btnTest.setText("Probar Lectura");
                    logToView("Lectura detenida.");
                } else {
                    uniqueTags.clear();
                    txtResultados.setText("Tags leídos: 0");
                    rfidHandler.performInventory();
                    isScanning = true;
                    btnTest.setText("Detener");
                    logToView("Esperando lectura (Use el gatillo)...");
                }
            }
        });
    }

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
                 if (msg.startsWith("Error")) {
                     Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                 }
                 if (msg.startsWith("Conectado")) {
                     txtCnfActual.setText("Estado: " + msg);
                 }
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        // Datalogic maneja el gatillo por hardware, pero si recibimos evento:
        logToView("Gatillo: " + pressed);
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
                    logToView("SCAN: " + epc);
                    Log.v(TAG, "Nuevo código: " + epc);
                }
            }
        }

        if (newTagsFound) {
            final int count = uniqueTags.size();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtResultados.setText("Tags leídos: " + count);
                });
            }
        }

        // Modo Sencillo: Detener tras leer
        if (rgReadingMode.getCheckedRadioButtonId() == R.id.rbSingle && newTagsFound) {
             if (getActivity() != null) {
                 getActivity().runOnUiThread(() -> {
                     // rfidHandler.stopInventory(); // No necesario si es gatillo hardware, pero actualizamos UI
                     isScanning = false;
                     btnTest.setText("Probar Lectura");
                     logToView("Lectura completada (Modo Sencillo)");
                 });
             }
        }
    }

    @Override
    public Context GetContext() {
        return getActivity();
    }
}
