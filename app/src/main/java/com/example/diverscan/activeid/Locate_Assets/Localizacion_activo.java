package com.example.diverscan.activeid.Locate_Assets;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;

import java.util.ArrayList;
import java.util.List;

public class Localizacion_activo extends AppCompatActivity implements ResponseHandlerInterface {

    // UI Components
    private RadioGroup rgMode;
    private LinearLayout layoutManual, layoutRfid;
    private EditText txtInputManual;
    private Button btnBuscarManual, btnScanRfid, btnIniciar, btnDetener;
    private Spinner spFoundAssets;
    private TextView lblTargetInfo, lblProximity;
    private ProgressBar progressBarProximity;

    // RFID & Logic
    private TagWriter rfidHandler;
    private ActivoDao activoDao;
    private String targetEpc;
    private boolean isInventorying = false;
    
    // Spinner Data
    private List<String> foundEpcs;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion_activo);

        activoDao = new ActivoDao(this);
        foundEpcs = new ArrayList<>();

        controles();
        eventos();
        initRFID();

        if (getIntent().hasExtra("TARGET_EPC")) {
            String passedEpc = getIntent().getStringExtra("TARGET_EPC");
            if (passedEpc != null && !passedEpc.isEmpty()) {
                targetEpc = passedEpc;
                updateTargetUI(targetEpc);
                btnIniciar.setEnabled(true);
            }
        }
    }
    
    private void initRFID() {
        try {
            rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
            rfidHandler.updateContext(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopOperations();
    }

    private void stopOperations() {
        if (rfidHandler != null) {
            if (isInventorying) {
                rfidHandler.stopRead();
                isInventorying = false;
                btnScanRfid.setText("Escanear Alrededores");
            }
            rfidHandler.StopLocateTag();
        }
    }

    public void controles() {
        rgMode = findViewById(R.id.rg_mode);
        layoutManual = findViewById(R.id.layout_manual);
        layoutRfid = findViewById(R.id.layout_rfid);
        
        txtInputManual = findViewById(R.id.txt_input_manual);
        btnBuscarManual = findViewById(R.id.btn_buscar_manual);
        
        btnScanRfid = findViewById(R.id.btn_scan_rfid);
        spFoundAssets = findViewById(R.id.sp_found_assets);
        
        lblTargetInfo = findViewById(R.id.lbl_target_info);
        btnIniciar = findViewById(R.id.btn_iniciar);
        btnDetener = findViewById(R.id.btn_detener);
        
        lblProximity = findViewById(R.id.lbl_proximity);
        progressBarProximity = findViewById(R.id.progressBarProximity);

        // Spinner Setup
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foundEpcs);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFoundAssets.setAdapter(spinnerAdapter);
        
        // Initial State
        updateTargetUI(null);
    }

    public void eventos() {
        // Mode Switch
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            stopOperations();
            if (checkedId == R.id.rb_manual) {
                layoutManual.setVisibility(View.VISIBLE);
                layoutRfid.setVisibility(View.GONE);
            } else {
                layoutManual.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.VISIBLE);
            }
        });

        // Manual Search
        btnBuscarManual.setOnClickListener(v -> {
            String input = txtInputManual.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Ingrese una placa o número", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Query DB
            ActivoEntity asset = activoDao.getActivoById(input);
            if (asset != null) {
                String epc = asset.getNumeroEtiqueta();
                String desc = asset.getDescripcionCorta();
                if (epc != null && !epc.isEmpty()) {
                    updateTargetUI(epc + " - " + (desc != null ? desc : "Sin Descripción"));
                    targetEpc = epc;
                    btnIniciar.setEnabled(true);
                } else {
                     Toast.makeText(this, "Activo encontrado pero sin EPC", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Activo no encontrado", Toast.LENGTH_SHORT).show();
            }
        });

        // RFID Scan
        btnScanRfid.setOnClickListener(v -> {
            if (rfidHandler == null) return;
            
            if (!isInventorying) {
                // Start Inventory
                foundEpcs.clear();
                spinnerAdapter.notifyDataSetChanged();
                rfidHandler.startRead();
                isInventorying = true;
                btnScanRfid.setText("Detener Escaneo");
            } else {
                // Stop Inventory
                rfidHandler.stopRead();
                isInventorying = false;
                btnScanRfid.setText("Escanear Alrededores");
            }
        });

        // Spinner Selection
        spFoundAssets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = foundEpcs.get(position);
                // Extract EPC if we add more info later, currently it's just EPC string
                targetEpc = selected;
                updateTargetUI(targetEpc);
                btnIniciar.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Location Controls
        btnIniciar.setOnClickListener(v -> {
            if (targetEpc != null && !targetEpc.isEmpty() && rfidHandler != null) {
                // Stop inventory if running
                if (isInventorying) {
                    rfidHandler.stopRead();
                    isInventorying = false;
                    btnScanRfid.setText("Escanear Alrededores");
                }
                
                rfidHandler.LocateTag(targetEpc);
                btnIniciar.setEnabled(false);
                btnDetener.setEnabled(true);
                Toast.makeText(this, "Localizando: " + targetEpc, Toast.LENGTH_SHORT).show();
            }
        });

        btnDetener.setOnClickListener(v -> {
            if (rfidHandler != null) {
                rfidHandler.StopLocateTag();
                btnIniciar.setEnabled(true);
                btnDetener.setEnabled(false);
                updateProximityUI(0, -999);
            }
        });
    }

    private void updateTargetUI(String text) {
        if (text == null) {
            lblTargetInfo.setText("Objetivo: Ninguno seleccionado");
            btnIniciar.setEnabled(false);
            btnDetener.setEnabled(false);
        } else {
            lblTargetInfo.setText("Objetivo: " + text);
        }
    }

    private void updateProximityUI(int progress, int rssi) {
        if (progressBarProximity != null) {
            progressBarProximity.setProgress(progress);
        }
        if (lblProximity != null) {
            lblProximity.setText("Proximidad: " + progress + "% (" + rssi + " dBm)");
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Mode: Inventory (Scanning for candidates)
        if (isInventorying) {
            boolean newData = false;
            for (ReaderTag tag : tagData) {
                String epc = tag.getEpc();
                if (!foundEpcs.contains(epc)) {
                    foundEpcs.add(epc);
                    newData = true;
                }
            }
            if (newData) {
                runOnUiThread(() -> spinnerAdapter.notifyDataSetChanged());
            }
        }
        // Mode: Location (Geiger)
        else {
             final ReaderTag tag = tagData[0];
             // Filter for target just in case, though reader should filter
             if (targetEpc != null && tag.getEpc().equals(targetEpc)) {
                 final int rssi = tag.getPeakRSSI();
                 int progress = 0;
                 if (rssi >= -30) progress = 100;
                 else if (rssi <= -90) progress = 0;
                 else {
                     progress = (int) ((rssi + 90) * (100.0 / 60.0));
                 }
                 final int finalProgress = progress;
                 runOnUiThread(() -> updateProximityUI(finalProgress, rssi));
             }
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        // Optional: Map trigger to Start/Stop logic depending on mode
        if (pressed) {
            if (btnIniciar.isEnabled()) btnIniciar.performClick();
        } else {
            if (btnDetener.isEnabled()) btnDetener.performClick();
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        // Optional status updates
    }
}
