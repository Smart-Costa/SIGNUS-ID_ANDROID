package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
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
    private TextView txtCategoriaTitulo;
    private TextView txtGaugeCount;
    private TextView txtSinSubtomas;
    private CircularProgressIndicator gaugeResumen;
    private RecyclerView recyclerTomas;
    private FloatingActionButton btnNuevaToma;
    private Button btnTabTomas, btnTabCompletas;
    private ImageView btnBack;
    
    private TomaFisicaTomasDao dao;
    private com.example.diverscan.activeid.data.local.dao.TomaFisicaDao tomaFisicaDao;
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
        txtCategoriaTitulo = findViewById(R.id.txtCategoriaTitulo);
        txtGaugeCount = findViewById(R.id.txtGaugeCount);
        txtSinSubtomas = findViewById(R.id.txtSinSubtomas);
        gaugeResumen = findViewById(R.id.gaugeResumen);
        recyclerTomas = findViewById(R.id.recyclerTomas);
        btnNuevaToma = findViewById(R.id.btnNuevaToma);
        btnTabTomas = findViewById(R.id.btnTabTomas);
        btnTabCompletas = findViewById(R.id.btnTabCompletas);
        btnBack = findViewById(R.id.btnBack);

        if (nombreInventario != null) {
            // txtNombreInventario.setText(nombreInventario);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Init DAO
        dao = new TomaFisicaTomasDao(this);
        tomaFisicaDao = new com.example.diverscan.activeid.data.local.dao.TomaFisicaDao(this);

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
        
        // Tabs logic
        updateTabs(true); // Default to Tomas active
        
        btnTabTomas.setOnClickListener(v -> updateTabs(true));
        btnTabCompletas.setOnClickListener(v -> updateTabs(false));
    }

    private void updateTabs(boolean isTomasActive) {
        if (isTomasActive) {
            btnTabTomas.setBackgroundResource(R.drawable.btn_primary);
            btnTabTomas.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            
            btnTabCompletas.setBackgroundResource(R.drawable.btn_secondary_gray);
            btnTabCompletas.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
            
            // Show Tomas content logic here if needed (e.g., filter list)
            // For now, assuming list is always Tomas
        } else {
            btnTabTomas.setBackgroundResource(R.drawable.btn_secondary_gray);
            btnTabTomas.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
            
            btnTabCompletas.setBackgroundResource(R.drawable.btn_primary);
            btnTabCompletas.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            
            // Show Completas content logic here
        }
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
            // Cargar Header con Categoria
            if (tomaFisicaDao != null && tomaFisicaId != null) {
                com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity parent = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
                if (parent != null) {
                    String catId = parent.getCategoria();
                    String catName = "";
                    if (catId != null && !catId.trim().isEmpty()) {
                        catName = tomaFisicaDao.getCategoryNameById(catId);
                    }
                    
                    // Fallback to ID if name is empty, or just empty string
                    if (catName == null || catName.isEmpty()) {
                        catName = (catId != null) ? catId : "";
                    }
                    
                    String title = "Inventario " + catName.trim();
                    runOnUiThread(() -> {
                        if (txtNombreInventario != null) txtNombreInventario.setText("Hacer inventario");
                        if (txtCategoriaTitulo != null) txtCategoriaTitulo.setText(title);
                    });
                }
            }

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
