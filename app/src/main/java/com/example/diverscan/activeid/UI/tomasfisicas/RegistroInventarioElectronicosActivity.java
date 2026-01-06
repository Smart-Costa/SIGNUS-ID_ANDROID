package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.repository.TomaFisicaTomasRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;
import java.util.List;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RegistroInventarioElectronicosActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnTomas, btnTomasCompletas;
    FloatingActionButton btnNuevaToma;
    TextView txtTitulo;
    TextView kpiValor, kpiTotal;
    CircularProgressIndicator kpiProgress;
    
    ActivoDao activoDao;
    TomaFisicaDetallesDao detallesDao;
    TomaFisicaTomasDao tomasDao;

    TomaFisicaTomasRepository repository;
    TomaFisicaTomasAdapter adapter;
    String tomaFisicaId;
    boolean mostrandoCompletas = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_inventario_electronicos);

        if (savedInstanceState != null) {
            mostrandoCompletas = savedInstanceState.getBoolean("mostrandoCompletas", false);
        }

        if (getIntent().hasExtra("tomaFisicaId")) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        }

        repository = new TomaFisicaTomasRepository(this);
        activoDao = new ActivoDao(this);
        detallesDao = new TomaFisicaDetallesDao(this);
        tomasDao = new TomaFisicaTomasDao(this);

        txtTitulo = findViewById(R.id.txtTitulo);
        String nombre = getIntent().getStringExtra("nombre");
        if (nombre != null && !nombre.trim().isEmpty()) {
            txtTitulo.setText(nombre.trim());
        }

        recyclerView = findViewById(R.id.recyclerTomas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TomaFisicaTomasAdapter(
                new ArrayList<>(),
                this::abrirResumen,
                this::abrirResumen
        );
        adapter.setOnDeleteClickListener(this::confirmarEliminacion);
        recyclerView.setAdapter(adapter);

        btnTomas = findViewById(R.id.btnTomas);
        btnTomasCompletas = findViewById(R.id.btnTomasCompletas);
        btnNuevaToma = findViewById(R.id.btnNuevaToma);
        
        kpiProgress = findViewById(R.id.kpiProgress);
        kpiValor = findViewById(R.id.kpiValor);
        kpiTotal = findViewById(R.id.kpiTotal);

        cargarKpi();
        cargarListeners();
        setToggleMostrandoCompletas(mostrandoCompletas);
        repository.syncFromApi(tomaFisicaId, () -> runOnUiThread(() -> {
            cargarKpi();
            setToggleMostrandoCompletas(mostrandoCompletas);
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        repository.syncFromApi(tomaFisicaId, () -> runOnUiThread(() -> {
            cargarKpi();
            setToggleMostrandoCompletas(mostrandoCompletas);
        }));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mostrandoCompletas", mostrandoCompletas);
    }

    private void cargarListeners() {

        btnTomas.setOnClickListener(v -> {
            if (mostrandoCompletas) {
                setToggleMostrandoCompletas(false);
            }
        });

        btnTomasCompletas.setOnClickListener(v -> {
            if (!mostrandoCompletas) {
                setToggleMostrandoCompletas(true);
            }
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

    private void setToggleMostrandoCompletas(boolean completas) {
        mostrandoCompletas = completas;
        btnTomas.setSelected(!completas);
        btnTomasCompletas.setSelected(completas);

        if (completas) {
            btnTomasCompletas.setBackgroundResource(R.drawable.btn_primary);
            btnTomas.setBackgroundResource(R.drawable.btn_secondary_gray);

            btnTomasCompletas.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            btnTomas.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));

            cargarTomasCompletas();
        } else {
            btnTomas.setBackgroundResource(R.drawable.btn_primary);
            btnTomasCompletas.setBackgroundResource(R.drawable.btn_secondary_gray);

            btnTomas.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            btnTomasCompletas.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));

            cargarTomas();
        }
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

    private void abrirResumen(TomaFisicaTomasEntity item) {
        Intent intent = new Intent(this, RegistroConteosActivity.class);
        intent.putExtra("tomaFisicaId", tomaFisicaId);
        intent.putExtra("idToma", item.getIdToma());
        intent.putExtra("numeroToma", item.getNumeroToma());
        startActivity(intent);
    }

    private void confirmarEliminacion(TomaFisicaTomasEntity item) {
        android.util.Log.d("DEBUG_DELETE", "Mostrando diálogo de confirmación para: " + item.getIdToma());
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Toma")
                .setMessage("¿Estás seguro de eliminar la Toma " + item.getNumeroToma() + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    android.util.Log.d("DEBUG_DELETE", "Usuario confirmó eliminación de: " + item.getIdToma());
                    eliminarToma(item);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    android.util.Log.d("DEBUG_DELETE", "Usuario canceló eliminación");
                })
                .show();
    }

    private void eliminarToma(TomaFisicaTomasEntity item) {
        android.util.Log.d("DEBUG_DELETE", "Iniciando eliminación de toma: " + item.getIdToma());
        new Thread(() -> {
            try {
                tomasDao.deleteToma(item.getIdToma());
                detallesDao.deleteByToma(item.getIdToma());
                android.util.Log.d("DEBUG_DELETE", "Eliminación exitosa en BD para: " + item.getIdToma());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Toma eliminada", Toast.LENGTH_SHORT).show();
                    setToggleMostrandoCompletas(mostrandoCompletas);
                    cargarKpi();
                });
            } catch (Exception e) {
                android.util.Log.e("DEBUG_DELETE", "Excepción al eliminar", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

}
