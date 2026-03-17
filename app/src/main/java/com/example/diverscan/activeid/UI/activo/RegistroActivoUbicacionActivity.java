package com.example.diverscan.activeid.UI.activo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistroActivoUbicacionActivity extends AppCompatActivity {

    AutoCompleteTextView spUbicacionA, spUbicacionB, spUbicacionC, spUbicacionD;
    Button btnGuardarUbicacion;
    private ComboItem itemSeleccionA;
    private ComboItem itemSeleccionB;
    private ComboItem itemSeleccionC;
    private ComboItem itemSeleccionD;

    RegistroActivoUbicacionViewModel viewModel;
    List<UbicacionEntity> listaUbicaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_activo_ubicacion);

        spUbicacionA = findViewById(R.id.spUbicacionA);
        spUbicacionB = findViewById(R.id.spUbicacionB);
        spUbicacionC = findViewById(R.id.spUbicacionC);
        spUbicacionD = findViewById(R.id.spUbicacionD);
        btnGuardarUbicacion = findViewById(R.id.btnGuardar);
        findViewById(R.id.btnRefreshUbicaciones).setOnClickListener(v -> {
            Toast.makeText(this, "Actualizando ubicaciones...", Toast.LENGTH_SHORT).show();
            viewModel.forzarSincronizacion();
        });

        // Asegurar que abran al hacer click (necesario para AutoCompleteTextView como Spinner)
        spUbicacionA.setOnClickListener(v -> spUbicacionA.showDropDown());
        spUbicacionB.setOnClickListener(v -> spUbicacionB.showDropDown());
        spUbicacionC.setOnClickListener(v -> spUbicacionC.showDropDown());
        spUbicacionD.setOnClickListener(v -> spUbicacionD.showDropDown());

        viewModel = new ViewModelProvider(this).get(RegistroActivoUbicacionViewModel.class);

        viewModel.getUbicaciones().observe(this, ubicaciones -> {
            if (ubicaciones != null && !ubicaciones.isEmpty()) {
                listaUbicaciones = ubicaciones;
                cargarUbicacionesA(ubicaciones);
            } else {
                Toast.makeText(this, "No se encontraron ubicaciones", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.cargarUbicaciones();

        spUbicacionA.setOnItemClickListener((adapterView, view, i, l) -> {
            itemSeleccionA = (ComboItem) spUbicacionA.getAdapter().getItem(i);

            cargarUbicacionesB(itemSeleccionA.getId());

            itemSeleccionB = null;
            itemSeleccionC = null;
            itemSeleccionD = null;

            spUbicacionB.setText("");
            spUbicacionC.setText("");
            spUbicacionD.setText("");
        });

        spUbicacionB.setOnItemClickListener((adapterView, view, i, l) -> {
            itemSeleccionB = (ComboItem) spUbicacionB.getAdapter().getItem(i);

            cargarUbicacionesC(itemSeleccionB.getId());

            itemSeleccionC = null;
            itemSeleccionD = null;

            spUbicacionC.setText("");
            spUbicacionD.setText("");
        });

        spUbicacionC.setOnItemClickListener((adapterView, view, i, l) -> {
            itemSeleccionC = (ComboItem) spUbicacionC.getAdapter().getItem(i);

            cargarUbicacionesD(itemSeleccionC.getId());

            itemSeleccionD = null;
            spUbicacionD.setText("");
        });

        spUbicacionD.setOnItemClickListener((adapterView, view, i, l) -> {
            itemSeleccionD = (ComboItem) spUbicacionD.getAdapter().getItem(i);
        });

        btnGuardarUbicacion.setOnClickListener(v -> {
            if (itemSeleccionA == null) {
                Toast.makeText(this, "La Ubicación A es obligatoria", Toast.LENGTH_SHORT).show();
                return;
            }
            String idActivo = java.util.UUID.randomUUID().toString();

            getSharedPreferences("RegistroActivo", MODE_PRIVATE)
                    .edit()
                    .putString("idActivo", idActivo)
                    .putString("UbicacionA_ID", itemSeleccionA.getId())
                    .putString("UbicacionA", itemSeleccionA.getNombre())
                    .putString("UbicacionB_ID", itemSeleccionB != null ? itemSeleccionB.getId() : null)
                    .putString("UbicacionB", itemSeleccionB != null ? itemSeleccionB.getNombre() : null)
                    .putString("UbicacionC_ID", itemSeleccionC != null ? itemSeleccionC.getId() : null)
                    .putString("UbicacionC", itemSeleccionC != null ? itemSeleccionC.getNombre() : null)
                    .putString("UbicacionD_ID", itemSeleccionD != null ? itemSeleccionD.getId() : null)
                    .putString("UbicacionD", itemSeleccionD != null ? itemSeleccionD.getNombre() : null)
                    .apply();
            Intent intent = new Intent(this, RegistroActivoDetailActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /** -------------------- MÉTODOS DE CARGA -------------------- **/
    public class ComboItem {
        private final String id;
        private final String nombre;

        public ComboItem(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre;
        }
    }

    private void cargarUbicacionesA(List<UbicacionEntity> ubicaciones) {
        List<ComboItem> listaA = new ArrayList<>();
        Set<String> repetidos = new HashSet<>();
        android.util.Log.d("RegistroActivoUbicacion", "Procesando " + ubicaciones.size() + " ubicaciones para Nivel A");
        for (UbicacionEntity item : ubicaciones) {
            if (item.getUbicacionA() != null && repetidos.add(item.getUbicacionA())) {
                listaA.add(new ComboItem(item.getASysId(), item.getUbicacionA()));
            }
        }
        android.util.Log.d("RegistroActivoUbicacion", "Elementos encontrados para Nivel A: " + listaA.size());
        spUbicacionA.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listaA));
    }

    private void cargarUbicacionesB(String idA) {
        List<ComboItem> listaB = new ArrayList<>();
        Set<String> repetidos = new HashSet<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getASysId() != null && item.getASysId().equals(idA)) {
                if (item.getBSysId() != null && repetidos.add(item.getBSysId())) {
                    listaB.add(new ComboItem(item.getBSysId(), item.getUbicacionB()));
                }
            }
        }
        spUbicacionB.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listaB));
    }

    private void cargarUbicacionesC(String idB) {
        List<ComboItem> listaC = new ArrayList<>();
        Set<String> repetidos = new HashSet<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getBSysId() != null && item.getBSysId().equals(idB)) {
                if (item.getCSysId() != null && repetidos.add(item.getCSysId())) {
                    listaC.add(new ComboItem(item.getCSysId(), item.getUbicacionC()));
                }
            }
        }
        spUbicacionC.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listaC));
    }

    private void cargarUbicacionesD(String idC) {
        List<ComboItem> listaD = new ArrayList<>();
        Set<String> repetidos = new HashSet<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getCSysId() != null && item.getCSysId().equals(idC)) {
                if (item.getDSysId() != null && repetidos.add(item.getDSysId())) {
                    listaD.add(new ComboItem(item.getDSysId(), item.getUbicacionD()));
                }
            }
        }
        spUbicacionD.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listaD));
    }
}
