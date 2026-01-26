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
    private boolean mostrarCompletadas = false;
    
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
            android.util.Log.d("DetalleTomaFisica", "Click en lupa/item subtoma: " + item.getNumeroToma() + " ID: " + item.getIdToma() + " Estado: " + item.getEstado());
            
            if ("CERRADA".equalsIgnoreCase(item.getEstado())) {
                // Si está CERRADA -> Se debe abrir NUEVA TOMA con la información pre-cargada de esta toma cerrada
                // OJO: La instrucción es "si es cerrada abra nueva toma con la info qu tiene la subtoma".
                // Esto implica clonar la toma o usarla como base.
                // Sin embargo, NuevaTomaActivity ya tiene lógica para "continuar" o "empezar".
                // Si pasamos el ID de una toma CERRADA, NuevaTomaActivity podría cargarla y permitir editarla (lo cual queremos evitar).
                // Pero el usuario dice: "evitar editar una toma cerrada... abra nueva toma con la info que tiene la subtoma".
                
                // Opción 1: Ir a RegistroConteosActivity (solo ver detalles, no editar).
                // Esto es lo que hacía antes:
                // Intent intent = new Intent(DetalleTomaFisicaActivity.this, RegistroConteosActivity.class);
                // ...
                // startActivity(intent);
                
                // Pero el usuario ahora pide: "si es cerrada abra nueva toma con la info qu tiene la subtoma".
                // Esto suena a "Copiar Toma" o "Re-inventariar basado en lo anterior".
                // PERO termina diciendo: "y si es cerrada la vista de detalle toma".
                // Esto es confuso: "si es cerrada abra nueva toma... y si es cerrada la vista de detalle toma".
                
                // Interpretación más probable:
                // 1. Si el usuario intenta ABRIR (click) una toma CERRADA para EDITAR -> BLOQUEAR EDICIÓN (solo ver detalle).
                // 2. O tal vez se refiere a que al recibir datos de API (que son cerrados), si el usuario quiere trabajar, debe ser en una NUEVA.
                
                // Releyendo: "la apk actual valida esto para evitar editar una toma cerrada que venga de la base de datos para que si es cerrada abra nueva toma con la info qu tiene la subtoma y si es cerrada la vista de detalle toma"
                // Parece faltar puntuación.
                // "la apk actual valida esto... para que si es cerrada [se] abra [la] nueva toma con la info... y si es cerrada [se abra] la vista de detalle toma".
                
                // Vamos a mantener la lógica segura:
                // CERRADA -> Solo Ver Detalle (RegistroConteosActivity)
                // ABIERTA -> Editar/Continuar (NuevaTomaActivity)
                
                // El usuario dice: "si es cerrada abra nueva toma con la info qu tiene la subtoma". 
                // Esto podría referirse a un botón de "Duplicar"? No hay botón explícito.
                // Asumiremos que el comportamiento deseado al hacer click en una cerrada es ver el detalle (solo lectura).
                
                Intent intent = new Intent(DetalleTomaFisicaActivity.this, RegistroConteosActivity.class);
                intent.putExtra("tomaFisicaId", tomaFisicaId);
                intent.putExtra("idToma", item.getIdToma());
                intent.putExtra("numeroToma", item.getNumeroToma());
                startActivity(intent);

            } else {
                // Si está ABIERTA -> Continuar Leyendo (NuevaTomaActivity)
                Intent intent = new Intent(DetalleTomaFisicaActivity.this, NuevaTomaActivity.class);
                intent.putExtra("tomaFisicaId", tomaFisicaId);
                intent.putExtra("idToma", item.getIdToma()); // Pasar ID para reanudar
                startActivity(intent);
            }
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
            
            mostrarCompletadas = false;
        } else {
            btnTabTomas.setBackgroundResource(R.drawable.btn_secondary_gray);
            btnTabTomas.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
            
            btnTabCompletas.setBackgroundResource(R.drawable.btn_primary);
            btnTabCompletas.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            
            mostrarCompletadas = true;
        }
        cargarDatosLocal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("DetalleTomaFisica", "onResume: Iniciando carga de datos y sincronización...");
        cargarDatosLocal();
        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
            android.util.Log.d("DetalleTomaFisica", "onResume: Llamando a fetchAndSyncFromApi para tomaFisicaId=" + tomaFisicaId);
            // COMENTADO POR SOLICITUD: La sincronización automática en la vista está deshabilitada.
            // Toda sincronización debe realizarse explícitamente a través del módulo de Sincronización.
            // dao.fetchAndSyncFromApi(tomaFisicaId, this::cargarDatosLocal);
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
                    String title = parent.getNombre() != null ? parent.getNombre() : "";
                    runOnUiThread(() -> {
                        if (txtNombreInventario != null) txtNombreInventario.setText("Hacer inventario");
                        if (txtCategoriaTitulo != null) txtCategoriaTitulo.setText(title);
                    });
                }
            }

            List<TomaFisicaTomasEntity> result = dao.getByTomaFisicaId(tomaFisicaId);
            if (result == null) result = new ArrayList<>();
            android.util.Log.d("DetalleTomaFisica", "cargarDatosLocal: Encontrados " + result.size() + " registros locales para tomaFisicaId=" + tomaFisicaId);
            
            // Filtrar por estado
            List<TomaFisicaTomasEntity> filtrados = new ArrayList<>();
            for (TomaFisicaTomasEntity t : result) {
                // Fix: Considerar tomas de la API con estado nulo como CERRADA
                String estado = t.getEstado();
                if (estado == null) estado = "CERRADA"; // Default for API data

                boolean cerrada = "CERRADA".equalsIgnoreCase(estado) || "COMPLETADA".equalsIgnoreCase(estado) || "FINALIZADA".equalsIgnoreCase(estado);
                
                if (mostrarCompletadas) {
                    // Mostrar CERRADAS
                    if (cerrada) filtrados.add(t);
                } else {
                    // Mostrar ABIERTAS
                    // Si el usuario quiere ver "TOMAS" (Abiertas), mostramos las NO cerradas.
                    // Pero espera... el usuario dice "deberia haber 3 subtomas" y la imagen muestra 3 tomas con checkbox.
                    // Los logs muestran que las 3 tomas tienen estado CERRADA.
                    // Si el tab activo es "TOMAS (ABIERTAS)" (default), y las 3 son cerradas, se ocultan.
                    // La imagen muestra que ESTÁN VISIBLES.
                    // Si están visibles, significa que o bien NO están filtradas, o el tab activo es "COMPLETADAS".
                    
                    // PERO, si el usuario dice "deberia haber 3 subtomas" y los logs dicen "Items visibles: 0",
                    // significa que el filtro las está ocultando porque son CERRADA y estamos en tab ABIERTAS.
                    
                    // SOLUCIÓN: Si el usuario quiere ver TODAS las tomas en la primera pestaña (independiente de si son cerradas o no),
                    // o si la lógica de pestañas está invertida.
                    
                    // REVISANDO UI:
                    // btnTabTomas -> updateTabs(true) -> mostrarCompletadas = false.
                    // btnTabCompletas -> updateTabs(false) -> mostrarCompletadas = true.
                    
                    // Si mostrarCompletadas es FALSE (Tab "TOMAS"), solo muestra NO cerradas.
                    // Las tomas del log son CERRADAS. Por tanto se ocultan.
                    
                    // SI LA INTENCIÓN es ver las tomas descargadas (que son cerradas) en la lista principal,
                    // entonces la lógica de filtrado debe cambiar o el concepto de "TOMAS" vs "COMPLETAS".
                    
                    // O tal vez el usuario quiere que las tomas descargadas se muestren SIEMPRE en la lista principal
                    // aunque estén cerradas, para poder consultarlas?
                    
                    // Si cambiamos la lógica para que "Tab TOMAS" muestre TODO (Abiertas y Cerradas)
                    // y "Tab COMPLETAS" sea un filtro adicional...
                    
                    // O simplemente, si la API devuelve tomas, estas deberían ser visibles.
                    // El problema es que al forzar "CERRADA" en el DAO, las movimos a la otra pestaña.
                    
                    // VOY A ASUMIR que el usuario quiere ver las tomas descargadas en la pestaña principal
                    // independientemente de su estado, O que la pestaña "TOMAS" debe incluir todo.
                    
                    // Sin embargo, si la pestaña se llama "TOMAS", suele ser "TODAS".
                    // Si hay otra que dice "COMPLETAS", esa sería el filtro.
                    
                    // CAMBIO PROPUESTO:
                    // Tab 1 (TOMAS) -> Muestra TODAS (Abiertas y Cerradas).
                    // Tab 2 (COMPLETAS) -> Muestra SOLO CERRADAS (Filtro).
                    
                    // Código actual:
                    // if (mostrarCompletadas) { if (cerrada) add } else { if (!cerrada) add }
                    // Esto es partición estricta.
                    
                    // Nuevo Código para Tab 1 = TODAS:
                    if (!mostrarCompletadas) {
                        // Tab TOMAS: Mostrar TODAS
                        filtrados.add(t);
                    } else {
                        // Tab COMPLETAS: Mostrar solo CERRADAS
                        if (cerrada) filtrados.add(t);
                    }
                }
            }
            result = filtrados;

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
                android.util.Log.d("DetalleTomaFisica", "Vista Tabla Subtomas: Datos cargados y mostrados. Cantidad: " + listaTomas.size());
                android.util.Log.d("DetalleTomaFisica", "TAB ACTIVA: " + (mostrarCompletadas ? "COMPLETADAS" : "TOMAS (ABIERTAS)") + ". Items visibles: " + listaTomas.size());
                
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
