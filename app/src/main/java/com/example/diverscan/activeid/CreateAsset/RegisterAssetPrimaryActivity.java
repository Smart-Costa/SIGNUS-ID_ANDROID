package com.example.diverscan.activeid.CreateAsset;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.diverscan.activeid.Employees.EmployeesDBHelper;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.R;

import java.util.Map;


import android.content.Context;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

public class RegisterAssetPrimaryActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private TagWriter rfidHandler;
    private EditText numeroEtiquetaView, numeroActivoView, serieView, descripcionView, codigoResponsableView;
    private Spinner spResponsables;
    private String idCompania, nombreCompania, idEdificio, nombreEdificio, idPiso, nombrePiso, idOficina, nombreOficina;
    private Map<Integer, EntidadEmployees> mapEmpleados;
    private EmployeesDBHelper employeesDBHelper;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_asset_primary);

        numeroEtiquetaView = findViewById(R.id.txt_numero_etiqueta);
        numeroActivoView = findViewById(R.id.txt_numero_activo);
        serieView = findViewById(R.id.txt_serie);
        descripcionView = findViewById(R.id.txt_descripcion);
        codigoResponsableView = findViewById(R.id.txt_codigo_responsable);
        spResponsables = findViewById(R.id.sp_responsables);

        employeesDBHelper = new EmployeesDBHelper(this);

        // Obtener datos de ubicación del intent
        idCompania = getIntent().getStringExtra("idCompania");
        nombreCompania = getIntent().getStringExtra("nombreCompania");
        idEdificio = getIntent().getStringExtra("idEdificio");
        nombreEdificio = getIntent().getStringExtra("nombreEdificio");
        idPiso = getIntent().getStringExtra("idPiso");
        nombrePiso = getIntent().getStringExtra("nombrePiso");
        idOficina = getIntent().getStringExtra("idOficina");
        nombreOficina = getIntent().getStringExtra("nombreOficina");

        codigoResponsableView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    cargarResponsables(s.toString());
                }
            }
        });

        initRFID();

        findViewById(R.id.btn_siguiente_datos_secundarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    EntidadEmployees empleado = (EntidadEmployees) spResponsables.getSelectedItem();

                    Intent intent = new Intent(RegisterAssetPrimaryActivity.this, RegisterAssetSecondaryActivity.class);
                    intent.putExtras(getIntent()); // pasar ubicación
                    intent.putExtra("numeroEtiqueta", numeroEtiquetaView.getText().toString());
                    intent.putExtra("numeroActivo", numeroActivoView.getText().toString());
                    intent.putExtra("serie", serieView.getText().toString());
                    intent.putExtra("descripcion", descripcionView.getText().toString());
                    intent.putExtra("responsable", empleado != null ? empleado.getName() : "");
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterAssetPrimaryActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarResponsables(String filtro) {
        mapEmpleados = employeesDBHelper.GetEmployees(filtro);
        EntidadEmployees[] empleados = mapEmpleados.values().toArray(new EntidadEmployees[0]);
        spResponsables.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layaout, empleados));
        spResponsables.requestFocus();
    }

    private boolean validarCampos() {
        return !numeroEtiquetaView.getText().toString().isEmpty() &&
                !numeroActivoView.getText().toString().isEmpty() &&
                !serieView.getText().toString().isEmpty() &&
                !descripcionView.getText().toString().isEmpty() &&
                spResponsables.getSelectedItem() != null;
    }

    private void initRFID() {
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

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.stopRead();
            rfidHandler.setResponseHandler(null);
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Lectura simple: validar si hay multiples tags (diferentes)
        java.util.Set<String> uniqueEpcs = new java.util.HashSet<>();
        for (ReaderTag tag : tagData) {
            if (tag.getEpc() != null && !tag.getEpc().isEmpty()) {
                uniqueEpcs.add(tag.getEpc());
            }
        }

        if (uniqueEpcs.size() > 1) {
            runOnUiThread(() -> {
                if (rfidHandler != null) {
                    rfidHandler.stopRead();
                    isScanning = false;
                }
                Toast.makeText(this, "Múltiples etiquetas detectadas. Por favor acerque solo una.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        if (uniqueEpcs.isEmpty()) return;
        String epc = uniqueEpcs.iterator().next();

        if (epc != null && !epc.isEmpty()) {
            runOnUiThread(() -> {
                if (rfidHandler != null) {
                    rfidHandler.stopRead();
                    isScanning = false;
                }
                numeroEtiquetaView.setText(epc);
                Toast.makeText(this, "Etiqueta leída: " + epc, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            if (rfidHandler != null && !isScanning) {
                rfidHandler.startRead();
                isScanning = true;
                runOnUiThread(() -> Toast.makeText(this, "Leyendo...", Toast.LENGTH_SHORT).show());
            }
        } else {
            if (rfidHandler != null && isScanning) {
                rfidHandler.stopRead();
                isScanning = false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(null);
        }
    }
}
