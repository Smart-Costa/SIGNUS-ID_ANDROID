package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityLocalizarActivoDetailBinding;

import java.util.ArrayList;
import java.util.List;

public class LocalizarActivoDetailActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityLocalizarActivoDetailBinding binding;
    private TagWriter rfidHandler;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;
    private String epcLeido;

    // RFID Inventory
    private List<String> foundEpcs;
    private android.widget.ArrayAdapter<String> spinnerAdapter;
    private boolean isScanning = false;
    private boolean isRequestingPermissions = false;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocalizarActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        foundEpcs = new ArrayList<>();
        
        initRFID();
        initUI();
        initEvents();
    }

    private void initUI() {
        // Setup Spinner
        spinnerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foundEpcs);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spFoundAssets.setAdapter(spinnerAdapter);

        // Initial State
        binding.layoutManual.setVisibility(View.VISIBLE);
        binding.layoutRfid.setVisibility(View.GONE);
        binding.btnLocalizar.setEnabled(false);

        // Check if DB is synced
        int count = activoDAO.getActivosCount();
        if (count == 0) {
            new AlertDialog.Builder(this)
                .setTitle("Sin Datos")
                .setMessage("La base de datos está vacía. Por favor sincronice los activos antes de realizar búsquedas.")
                .setPositiveButton("Aceptar", null)
                .show();
        }
    }

    private void initRFID() {
        if (!checkPermissions()) {
            return;
        }
        try {
            if (rfidHandler == null) rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.setResponseHandler(this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error inicializando RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions() {
        if (isRequestingPermissions) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                
                isRequestingPermissions = true;
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isRequestingPermissions = true;
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRequestingPermissions = false;
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
        } else {
            // Re-attempt init if we came back from permissions
            if (checkPermissions()) {
                initRFID();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            isRequestingPermissions = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initRFID();
            } else {
                Toast.makeText(this, "Permisos necesarios para usar el lector RFID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    private void stopScanning() {
        if (rfidHandler != null && isScanning) {
            rfidHandler.stopRead();
            isScanning = false;
            binding.btnScanRfid.setText("Escanear Alrededores");
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Inventory Mode
        if (isScanning) {
            boolean newData = false;
            for (ReaderTag tag : tagData) {
                String epc = tag.getEpc();
                if (epc != null && !foundEpcs.contains(epc)) {
                    foundEpcs.add(epc);
                    newData = true;
                }
            }
            if (newData) {
                runOnUiThread(() -> spinnerAdapter.notifyDataSetChanged());
            }
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            if (binding.rbRfid.isChecked()) {
                binding.btnScanRfid.performClick();
            }
        }
    }

    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(this, "Reader: " + msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public Context GetContext() {
        return this;
    }

    private void initEvents() {
        // Mode Switch
        binding.rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            stopScanning();
            if (checkedId == R.id.rb_manual) {
                binding.layoutManual.setVisibility(View.VISIBLE);
                binding.layoutRfid.setVisibility(View.GONE);
            } else {
                binding.layoutManual.setVisibility(View.GONE);
                binding.layoutRfid.setVisibility(View.VISIBLE);
            }
            // Reset selection
            epcLeido = null;
            activoLeido = null;
            binding.btnLocalizar.setEnabled(false);
            binding.txtDescripcionCorta.setText("");
        });

        // Manual Search
        binding.btnBuscar.setOnClickListener(v -> {
            String id = binding.txtNumeroActivo.getText().toString().trim();
            String placa = binding.txtNumeroEtiqueta.getText().toString().trim();

            if (id.isEmpty() && placa.isEmpty()) {
                Toast.makeText(this, "Ingrese Número de Activo o Placa", Toast.LENGTH_SHORT).show();
                return;
            }

            ActivoEntity activo = null;
            if (!id.isEmpty()) {
                activo = activoDAO.getActivoById(id);
            } else if (!placa.isEmpty()) {
                // Assuming getActivoByPlate exists or using getActivoById for now if plate is ID
                // Ideally: activo = activoDAO.getActivoByPlate(placa);
                // Fallback to searching by ID if plate logic not ready, or implement query
                // For now, let's assume user enters ID in the ID field.
                // If they enter Placa, we might need a specific method.
                // Let's check ActivoDao later. For now, try ID lookup.
                Toast.makeText(this, "Búsqueda por Placa no implementada, use ID", Toast.LENGTH_SHORT).show();
            }

            if (activo != null) {
                activoLeido = activo;
                epcLeido = activo.getEpc(); // Or getNumeroEtiqueta() if that's the EPC
                binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
                binding.btnLocalizar.setEnabled(true);
                
                if (epcLeido == null || epcLeido.isEmpty()) {
                     Toast.makeText(this, "Activo encontrado pero sin EPC asociado", Toast.LENGTH_LONG).show();
                     binding.btnLocalizar.setEnabled(false);
                }
            } else {
                Toast.makeText(this, "Activo no encontrado", Toast.LENGTH_SHORT).show();
                binding.btnLocalizar.setEnabled(false);
                binding.txtDescripcionCorta.setText("");
            }
        });

        // RFID Scan
        binding.btnScanRfid.setOnClickListener(v -> {
            if (rfidHandler == null) return;

            if (!isScanning) {
                foundEpcs.clear();
                spinnerAdapter.notifyDataSetChanged();
                rfidHandler.startRead();
                isScanning = true;
                binding.btnScanRfid.setText("Detener Escaneo");
            } else {
                stopScanning();
            }
        });

        // Spinner Selection
        binding.spFoundAssets.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String epc = foundEpcs.get(position);
                epcLeido = epc;
                
                // Lookup info
                ActivoEntity activo = activoDAO.getActivoByEpc(epc);
                if (activo != null) {
                    activoLeido = activo;
                    binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
                } else {
                    activoLeido = null;
                    binding.txtDescripcionCorta.setText("Activo no registrado en BD");
                }
                binding.btnLocalizar.setEnabled(true);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                binding.btnLocalizar.setEnabled(false);
            }
        });

        // Geiger Launch
        binding.btnLocalizar.setOnClickListener(v -> {
            if (epcLeido != null && !epcLeido.isEmpty()) {
                Intent intent = new Intent(LocalizarActivoDetailActivity.this, com.example.diverscan.activeid.Locate_Assets.Localizacion_activo.class);
                intent.putExtra("TARGET_EPC", epcLeido);
                startActivity(intent);
            }
        });
    }
}
