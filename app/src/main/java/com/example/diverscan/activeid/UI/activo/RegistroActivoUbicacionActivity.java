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
import java.util.List;

public class RegistroActivoUbicacionActivity extends AppCompatActivity {

    AutoCompleteTextView spUbicacionA, spUbicacionB, spUbicacionC, spUbicacionD;
    Button btnGuardarUbicacion;

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

        viewModel = new ViewModelProvider(this).get(RegistroActivoUbicacionViewModel.class);

        viewModel.getUbicaciones().observe(this, ubicaciones -> {
            if (ubicaciones != null && !ubicaciones.isEmpty()) {
                listaUbicaciones = ubicaciones;
                cargarUbicacionesA(ubicaciones);
                Toast.makeText(this, "Ubicaciones cargadas: " + ubicaciones.size(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se encontraron ubicaciones", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.cargarUbicaciones();

        spUbicacionA.setOnItemClickListener((adapterView, view, i, l) -> {
            String seleccionA = spUbicacionA.getText().toString();
            cargarUbicacionesB(seleccionA);
        });

        spUbicacionB.setOnItemClickListener((adapterView, view, i, l) -> {
            String seleccionB = spUbicacionB.getText().toString();
            cargarUbicacionesC(seleccionB);
        });

        spUbicacionC.setOnItemClickListener((adapterView, view, i, l) -> {
            String seleccionC = spUbicacionC.getText().toString();
            cargarUbicacionesD(seleccionC);
        });

        btnGuardarUbicacion.setOnClickListener(v -> {
            String ubicacionA = spUbicacionA.getText().toString();
            String ubicacionB = spUbicacionB.getText().toString();
            String ubicacionC = spUbicacionC.getText().toString();
            String ubicacionD = spUbicacionD.getText().toString();

            if (ubicacionA.isEmpty() || ubicacionB.isEmpty()) {
                Toast.makeText(this, "Por favor completa las ubicaciones A y B obligatorias", Toast.LENGTH_SHORT).show();
                return;
            }

            String idActivo = java.util.UUID.randomUUID().toString();

            getSharedPreferences("RegistroActivo", MODE_PRIVATE)
                    .edit()
                    .putString("idActivo", idActivo)
                    .putString("UbicacionA", ubicacionA)
                    .putString("UbicacionB", ubicacionB)
                    .putString("UbicacionC", ubicacionC)
                    .putString("UbicacionD", ubicacionD)
                    .apply();

            Intent intent = new Intent(this, RegistroActivoDetailActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /** -------------------- MÉTODOS DE CARGA -------------------- **/
    private void cargarUbicacionesA(List<UbicacionEntity> ubicaciones) {
        List<String> listaA = new ArrayList<>();
        for (UbicacionEntity item : ubicaciones) {
            if (item.getUbicacionA() != null && !listaA.contains(item.getUbicacionA())) {
                listaA.add(item.getUbicacionA());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaA);
        spUbicacionA.setAdapter(adapter);
    }

    private void cargarUbicacionesB(String ubicacionA) {
        List<String> listaB = new ArrayList<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getUbicacionA() != null && item.getUbicacionA().equals(ubicacionA)) {
                if (item.getUbicacionB() != null && !listaB.contains(item.getUbicacionB())) {
                    listaB.add(item.getUbicacionB());
                }
            }
        }
        ArrayAdapter<String> adapterB = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaB);
        spUbicacionB.setAdapter(adapterB);
    }

    private void cargarUbicacionesC(String ubicacionB) {
        List<String> listaC = new ArrayList<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getUbicacionB() != null && item.getUbicacionB().equals(ubicacionB)) {
                if (item.getUbicacionC() != null && !listaC.contains(item.getUbicacionC())) {
                    listaC.add(item.getUbicacionC());
                }
            }
        }
        ArrayAdapter<String> adapterC = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaC);
        spUbicacionC.setAdapter(adapterC);
    }

    private void cargarUbicacionesD(String ubicacionC) {
        List<String> listaD = new ArrayList<>();
        for (UbicacionEntity item : listaUbicaciones) {
            if (item.getUbicacionC() != null && item.getUbicacionC().equals(ubicacionC)) {
                if (item.getUbicacionD() != null && !listaD.contains(item.getUbicacionD())) {
                    listaD.add(item.getUbicacionD());
                }
            }
        }
        ArrayAdapter<String> adapterD = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaD);
        spUbicacionD.setAdapter(adapterD);
    }
}
