package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.repository.TomaFisicaTomasRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class RegistroInventarioElectronicosActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnTomas, btnTomasCompletas;
    FloatingActionButton btnNuevaToma;
    TextView kpiValor, kpiTotal;
    CircularProgressIndicator kpiProgress;

    TomaFisicaTomasRepository repository;
    TomaFisicaTomasAdapter adapter;
    String tomaFisicaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_inventario_electronicos);

        if (getIntent().hasExtra("tomaFisicaId")) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        }

        repository = new TomaFisicaTomasRepository(this);

        recyclerView = findViewById(R.id.recyclerTomas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TomaFisicaTomasAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        btnTomas = findViewById(R.id.btnTomas);
        btnTomasCompletas = findViewById(R.id.btnTomasCompletas);
        btnNuevaToma = findViewById(R.id.btnNuevaToma);

        kpiProgress = findViewById(R.id.kpiProgress);
        kpiValor = findViewById(R.id.kpiValor);
        kpiTotal = findViewById(R.id.kpiTotal);

        cargarKpi();
        cargarListeners();
        cargarTomas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTomas();
    }

    private void cargarListeners() {

        btnTomas.setOnClickListener(v -> {
            btnTomas.setBackgroundResource(R.drawable.btn_primary);
            btnTomasCompletas.setBackgroundResource(R.drawable.btn_secondary_gray);
            cargarTomas();
        });

        btnTomasCompletas.setOnClickListener(v -> {
            btnTomasCompletas.setBackgroundResource(R.drawable.btn_primary);
            btnTomas.setBackgroundResource(R.drawable.btn_secondary_gray);
            cargarTomasCompletas();
        });

        btnNuevaToma.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaTomaActivity.class);
            intent.putExtra("tomaFisicaId", tomaFisicaId);
            startActivity(intent);
        });
    }

    private void cargarTomasCompletas() {
        List<TomaFisicaTomasEntity> lista = repository.getCompletas();
        adapter.actualizar(lista);
    }

    private void cargarKpi() {
        // Corrección: El límite de 5 aplica al total de subtomas creadas para esta Toma Física.
        // Usamos getPendientes(tomaFisicaId) que retorna todas las subtomas asociadas.
        List<TomaFisicaTomasEntity> todasLasTomas = repository.getPendientes(tomaFisicaId);
        int completadas = todasLasTomas != null ? todasLasTomas.size() : 0;
        int total = 5;

        float progreso = (completadas * 100f) / total;

        kpiValor.setText(String.valueOf(completadas));
        kpiTotal.setText("de " + total);

        kpiProgress.setProgress((int) progreso);

        if (completadas >= total) {
            btnNuevaToma.setEnabled(false);
            btnNuevaToma.setAlpha(0.5f);
        } else {
            btnNuevaToma.setEnabled(true);
            btnNuevaToma.setAlpha(1.0f);
        }
    }

    private void cargarTomas() {
        List<TomaFisicaTomasEntity> lista = repository.getPendientes(tomaFisicaId);
        adapter.actualizar(lista);
    }

}


