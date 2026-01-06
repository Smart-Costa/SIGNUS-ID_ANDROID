package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetalleTomaFisicaActivity extends AppCompatActivity {

    private String tomaFisicaId;
    private String nombreInventario;
    
    private TextView txtNombreInventario;
    private TextView txtGaugeCount;
    private TextView txtSinSubtomas;
    private CircularProgressIndicator gaugeResumen;
    private RecyclerView recyclerTomas;
    private FloatingActionButton btnNuevaToma;
    private Button btnTabTomas, btnTabCompletas;
    private ImageView btnBack;
    
    private TomaFisicaTomasDao dao;
    private TomaFisicaTomasAdapter adapter;
    private List<TomaFisicaTomasEntity> listaTomas = new ArrayList<>();
    
    private static final int MAX_TOMAS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_toma_fisica);

        // Get Intent Extras
        if (getIntent() != null) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
            nombreInventario = getIntent().getStringExtra("nombre");
        }

        // Init UI
        txtNombreInventario = findViewById(R.id.txtNombreInventario);
        txtGaugeCount = findViewById(R.id.txtGaugeCount);
        txtSinSubtomas = findViewById(R.id.txtSinSubtomas);
        gaugeResumen = findViewById(R.id.gaugeResumen);
        recyclerTomas = findViewById(R.id.recyclerTomas);
        btnNuevaToma = findViewById(R.id.btnNuevaToma);
        btnTabTomas = findViewById(R.id.btnTabTomas);
        btnTabCompletas = findViewById(R.id.btnTabCompletas);
        btnBack = findViewById(R.id.btnBack);

        if (nombreInventario != null) {
            txtNombreInventario.setText(nombreInventario);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Init DAO
        dao = new TomaFisicaTomasDao(this);

        // Setup Recycler
        recyclerTomas.setLayoutManager(new LinearLayoutManager(this));
        // Note: Using existing TomaFisicaTomasAdapter. 
        // We need to implement click listener to go to RegistroConteosActivity
        adapter = new TomaFisicaTomasAdapter(listaTomas, item -> {
            // Log click event
            android.util.Log.d("DetalleTomaFisica", "Click en lupa/item subtoma: " + item.getNumeroToma() + " ID: " + item.getIdToma());
            
            // On Item Click -> Go to RegistroConteosActivity (View/Edit)
            Intent intent = new Intent(DetalleTomaFisicaActivity.this, RegistroConteosActivity.class);
            intent.putExtra("tomaFisicaId", tomaFisicaId);
            intent.putExtra("idToma", item.getIdToma());
            intent.putExtra("numeroToma", item.getNumeroToma());
            startActivity(intent);
        });
        
        // Handle Delete in Adapter
        adapter.setOnDeleteClickListener(item -> {
            // Delete logic
            new Thread(() -> {
                dao.deleteToma(item.getIdToma());
                cargarDatosLocal();
            }).start();
        });

        recyclerTomas.setAdapter(adapter);

        // Listeners
        btnNuevaToma.setOnClickListener(v -> {
            if (listaTomas.size() >= MAX_TOMAS) {
                Toast.makeText(this, "Límite de " + MAX_TOMAS + " tomas alcanzado.", Toast.LENGTH_SHORT).show();
            } else {
                // Log click event
                android.util.Log.d("DetalleTomaFisica", "Click en Nueva Toma. Iniciando NuevaTomaActivity para tomaFisicaId: " + tomaFisicaId);
                
                // Go to Create New Toma
                Intent intent = new Intent(DetalleTomaFisicaActivity.this, NuevaTomaActivity.class);
                intent.putExtra("tomaFisicaId", tomaFisicaId);
                startActivity(intent);
            }
        });
        
        // Tabs logic (Visual for now, or filter if needed)
        // User asked for layout primarily, logic might be extended later.
        // For now, assume "Tomas" tab is active.
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("DetalleTomaFisica", "onResume: Iniciando carga de datos y sincronización...");
        cargarDatosLocal();
        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
            android.util.Log.d("DetalleTomaFisica", "onResume: Llamando a fetchAndSyncFromApi para tomaFisicaId=" + tomaFisicaId);
            dao.fetchAndSyncFromApi(tomaFisicaId, this::cargarDatosLocal);
        } else {
            android.util.Log.w("DetalleTomaFisica", "onResume: tomaFisicaId es nulo o vacío, no se sincroniza.");
        }
    }

    private void cargarDatosLocal() {
        new Thread(() -> {
            List<TomaFisicaTomasEntity> result = dao.getByTomaFisicaId(tomaFisicaId);
            if (result == null) result = new ArrayList<>();
            android.util.Log.d("DetalleTomaFisica", "cargarDatosLocal: Encontrados " + result.size() + " registros locales para tomaFisicaId=" + tomaFisicaId);
            
            // Sort by NumeroToma
            Collections.sort(result, (o1, o2) -> {
                try {
                    return Integer.compare(Integer.parseInt(o1.getNumeroToma()), Integer.parseInt(o2.getNumeroToma()));
                } catch (Exception e) { return 0; }
            });
            
            List<TomaFisicaTomasEntity> finalResult = result;
            runOnUiThread(() -> {
                listaTomas = finalResult;
                adapter.actualizar(listaTomas);
                
                if (listaTomas.isEmpty()) {
                    if (txtSinSubtomas != null) txtSinSubtomas.setVisibility(android.view.View.VISIBLE);
                    recyclerTomas.setVisibility(android.view.View.GONE);
                } else {
                    if (txtSinSubtomas != null) txtSinSubtomas.setVisibility(android.view.View.GONE);
                    recyclerTomas.setVisibility(android.view.View.VISIBLE);
                }
                
                actualizarGauge(listaTomas.size());
            });
        }).start();
    }

    private void actualizarGauge(int count) {
        txtGaugeCount.setText(String.valueOf(count));
        if (gaugeResumen != null) {
            gaugeResumen.setProgress(count);
        }
        // Change color based on count if needed, or just standard progress
    }
}
