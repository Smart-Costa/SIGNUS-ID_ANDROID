package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.zebra.rfid.api3.TagData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import android.content.Intent;

public class NuevaTomaActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final String TAG = "NuevaTomaActivity";
    private String tomaFisicaId;
    private String idToma;
    private String numeroToma;
    private int currentTomaIndex = 1;

    // UI Components
    private TextView lblConteos;
    private HorizontalScrollView scrollTomas;
    private LinearLayout containerTomasTabs;
    private Button tabResumen, tabActivos;
    private RelativeLayout viewResumen;
    private LinearLayout viewActivos;
    private CircularProgressIndicator gaugeResumen;
    private TextView txtGaugeCount;
    private ListView listScannedTags;
    private LinearLayout btnPotencia, btnIniciar, btnSubir;
    private TextView txtIniciar;
    private ImageView iconIniciar;
    private ImageView btnBack;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> scannedTagsList;
    private Set<String> uniqueTags;

    private TagWriter rfidHandler;
    private boolean isScanning = false;

    private TomaFisicaTomasDao tomasDao;
    private TomaFisicaDetallesDao detallesDao;
    private TomaFisicaDao tomaFisicaDao;
    private ActivoDao activoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_toma);

        if (getIntent().hasExtra("tomaFisicaId")) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        } else {
            Toast.makeText(this, "Error: No se recibió ID de Toma Física", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize DAO
        tomasDao = new TomaFisicaTomasDao(this);
        detallesDao = new TomaFisicaDetallesDao(this);
        tomaFisicaDao = new TomaFisicaDao(this);
        activoDao = new ActivoDao(this);

        // Calculate next Toma Number
        int existingCount = tomasDao.getPendientesCount(tomaFisicaId);
        currentTomaIndex = existingCount + 1;
        
        // Generate new ID for this Take
        idToma = UUID.randomUUID().toString();
        // numeroToma for DB (string) vs display (int)
        // Usually numeroToma is just "1", "2", etc. or a timestamp. 
        // Based on the user wanting "Toma 1..5", let's use the index as the number for now, or timestamp if preferred.
        // I'll use the index for consistency with the UI.
        numeroToma = String.valueOf(currentTomaIndex);

        initUI();
        initRFID();
    }

    private void initUI() {
        // Bind Views
        lblConteos = findViewById(R.id.lblConteos);
        scrollTomas = findViewById(R.id.scrollTomas);
        containerTomasTabs = findViewById(R.id.containerTomasTabs);
        
        tabResumen = findViewById(R.id.tabResumen);
        tabActivos = findViewById(R.id.tabActivos);
        
        viewResumen = findViewById(R.id.viewResumen);
        viewActivos = findViewById(R.id.viewActivos);
        
        gaugeResumen = findViewById(R.id.gaugeResumen);
        txtGaugeCount = findViewById(R.id.txtGaugeCount);
        
        listScannedTags = findViewById(R.id.listScannedTags);
        
        btnPotencia = findViewById(R.id.btnPotencia);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnSubir = findViewById(R.id.btnSubir);
        
        txtIniciar = findViewById(R.id.txtIniciar);
        iconIniciar = findViewById(R.id.iconIniciar);
        btnBack = findViewById(R.id.btnBack);

        // Setup List Adapter
        scannedTagsList = new ArrayList<>();
        uniqueTags = new HashSet<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedTagsList);
        listScannedTags.setAdapter(adapter);

        // Update Tabs (Tomas 1..5)
        updateTomasTabs();

        // Listeners
        tabResumen.setOnClickListener(v -> switchTab(true));
        tabActivos.setOnClickListener(v -> switchTab(false));

        btnIniciar.setOnClickListener(v -> toggleScan());
        btnSubir.setOnClickListener(v -> uploadTake());
        btnBack.setOnClickListener(v -> finish());
        
        btnPotencia.setOnClickListener(v -> Toast.makeText(this, "Configuración de potencia no disponible", Toast.LENGTH_SHORT).show());
    }
    
    private void updateTomasTabs() {
        containerTomasTabs.removeAllViews();
        // Show at least the current one, maybe previous ones too if we could load them.
        // For "Nueva Toma", we are creating "currentTomaIndex".
        // Let's show "Toma X" as selected.
        
        TextView tab = new TextView(this);
        tab.setText("Toma " + currentTomaIndex);
        tab.setTextColor(getResources().getColor(android.R.color.white));
        tab.setBackgroundResource(R.drawable.tab_selected_bg); // Assume this drawable exists or use btn_primary
        tab.setPadding(48, 16, 48, 16);
        containerTomasTabs.addView(tab);
        
        // Add a placeholder for next if < 5
        if (currentTomaIndex < 5) {
             TextView nextTab = new TextView(this);
            nextTab.setText("Toma " + (currentTomaIndex + 1));
            nextTab.setTextColor(getResources().getColor(R.color.colorAccent));
            nextTab.setBackgroundResource(R.drawable.tab_unselected_bg); // Assume exists
            nextTab.setPadding(48, 16, 48, 16);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 0, 0, 0);
            nextTab.setLayoutParams(params);
            containerTomasTabs.addView(nextTab);
        }
    }

    private void switchTab(boolean isResumen) {
        if (isResumen) {
            new SaveLocalTask().execute();
        } else {
            viewResumen.setVisibility(View.GONE);
            viewActivos.setVisibility(View.VISIBLE);
            tabResumen.setBackgroundResource(R.drawable.btn_secondary_gray);
            tabResumen.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabActivos.setBackgroundResource(R.drawable.btn_primary);
            tabActivos.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private TomaFisicaTomasEntity calculateSummary() {
        TomaFisicaTomasEntity header = new TomaFisicaTomasEntity();
        header.setTomaFisicaId(tomaFisicaId);
        header.setIdToma(idToma);
        header.setNumeroToma(numeroToma);
        header.setTotalLecturas(String.valueOf(uniqueTags.size()));
        header.setFechaCreacion(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        TomaFisicaEntity tomaFisica = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
        if (tomaFisica != null) {
            // Asumimos que la ubicación para validar activos es UbicacionD, ajustar según lógica de negocio
            String ubicacionD = tomaFisica.getUbicacionD();
            List<ActivoEntity> expected = new ArrayList<>();
            if (ubicacionD != null) {
                expected = activoDao.getActivosByUbicacion(ubicacionD);
            }

            int totalActivos = expected.size();
            int encontrados = 0;
            int sobrantes = 0;

            Set<String> expectedEpcs = new HashSet<>();
            for (ActivoEntity a : expected) {
                if (a.getTagEpc() != null) expectedEpcs.add(a.getTagEpc());
            }

            for (String scanned : uniqueTags) {
                if (expectedEpcs.contains(scanned)) {
                    encontrados++;
                } else {
                    sobrantes++;
                }
            }

            int faltantes = totalActivos - encontrados;
            if (faltantes < 0) faltantes = 0;

            header.setTotalActivos(String.valueOf(totalActivos));
            header.setActivosLeidos(String.valueOf(uniqueTags.size()));
            header.setFaltantes(String.valueOf(faltantes));
            header.setSobrantes(String.valueOf(sobrantes));
        } else {
            header.setTotalActivos("0");
            header.setActivosLeidos(String.valueOf(uniqueTags.size()));
            header.setFaltantes("0");
            header.setSobrantes(String.valueOf(uniqueTags.size()));
        }
        return header;
    }

    private class SaveLocalTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(NuevaTomaActivity.this, "Calculando resumen...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                TomaFisicaTomasEntity header = calculateSummary();
                List<TomaFisicaDetallesEntity> detalles = new ArrayList<>();
                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // Obtener detalles existentes para evitar duplicados si es posible
                List<TomaFisicaDetallesEntity> existing = detallesDao.getByIdToma(idToma);
                Set<String> existingEpcs = new HashSet<>();
                if (existing != null) {
                    for (TomaFisicaDetallesEntity d : existing) existingEpcs.add(d.getEpc());
                }

                for (String epc : uniqueTags) {
                    if (!existingEpcs.contains(epc)) {
                        TomaFisicaDetallesEntity detail = new TomaFisicaDetallesEntity();
                        detail.setIdTakeDetail(UUID.randomUUID().toString());
                        detail.setIdToma(idToma);
                        detail.setNumeroToma(numeroToma);
                        detail.setEpc(epc);
                        detail.setDateRead(now);
                        detalles.add(detail);
                    }
                }

                // Guardar Header (Upsert)
                List<TomaFisicaTomasEntity> headers = new ArrayList<>();
                headers.add(header);
                tomasDao.syncResumen(headers);

                // Guardar Detalles
                if (!detalles.isEmpty()) {
                    detallesDao.syncDetalle(detalles);
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error saving local", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Intent intent = new Intent(NuevaTomaActivity.this, RegistroConteosActivity.class);
                intent.putExtra("tomaFisicaId", tomaFisicaId);
                intent.putExtra("idToma", idToma);
                intent.putExtra("numeroToma", numeroToma);
                startActivity(intent);
            } else {
                Toast.makeText(NuevaTomaActivity.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initRFID() {
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        }
        rfidHandler.setResponseHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.onPause();
        }
    }

    private void toggleScan() {
        if (isScanning) {
            stopScan();
        } else {
            startScan();
        }
    }

    private void startScan() {
        if (rfidHandler != null) {
            rfidHandler.performInventory();
            isScanning = true;
            updateUIState();
        }
    }

    private void stopScan() {
        if (rfidHandler != null) {
            rfidHandler.stopInventory();
            isScanning = false;
            updateUIState();
        }
    }

    private void updateUIState() {
        runOnUiThread(() -> {
            if (isScanning) {
                txtIniciar.setText("Detener");
                iconIniciar.setImageResource(android.R.drawable.ic_media_pause); // Or custom stop icon
                btnSubir.setEnabled(false);
                btnSubir.setAlpha(0.5f);
            } else {
                txtIniciar.setText("Iniciar");
                iconIniciar.setImageResource(R.drawable.ic_nfc);
                btnSubir.setEnabled(true);
                btnSubir.setAlpha(1.0f);
            }
        });
    }

    private void uploadTake() {
        if (uniqueTags.isEmpty()) {
            Toast.makeText(this, "No hay lecturas para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Prepare Data
        TomaFisicaTomasEntity header = new TomaFisicaTomasEntity();
        header.setTomaFisicaId(tomaFisicaId);
        header.setIdToma(idToma);
        header.setNumeroToma(numeroToma);
        header.setTotalLecturas(String.valueOf(uniqueTags.size()));
        
        List<TomaFisicaDetallesEntity> detalles = new ArrayList<>();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        for (String epc : uniqueTags) {
            TomaFisicaDetallesEntity detail = new TomaFisicaDetallesEntity();
            detail.setIdTakeDetail(UUID.randomUUID().toString());
            detail.setIdToma(idToma);
            detail.setNumeroToma(numeroToma);
            detail.setEpc(epc);
            detail.setDateRead(now);
            // FK fields need to be set if required by API/DB
            // detail.setFKTomaFisica(tomaFisicaId); // Field not present in entity
            detalles.add(detail);
        }

        // 2. Save Local & Push
        new SaveAndPushTask(header, detalles).execute();
    }

    private class SaveAndPushTask extends AsyncTask<Void, Void, Boolean> {
        private TomaFisicaTomasEntity header;
        private List<TomaFisicaDetallesEntity> detalles;

        public SaveAndPushTask(TomaFisicaTomasEntity header, List<TomaFisicaDetallesEntity> detalles) {
            this.header = header;
            this.detalles = detalles;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(NuevaTomaActivity.this, "Guardando y subiendo...", Toast.LENGTH_SHORT).show();
            btnSubir.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Save Local
                tomasDao.insert(header);
                detallesDao.syncDetalle(detalles);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error saving take locally", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Now Push to API
                pushToApi(header, detalles);
            } else {
                Toast.makeText(NuevaTomaActivity.this, "Error al guardar localmente", Toast.LENGTH_SHORT).show();
                btnSubir.setEnabled(true);
            }
        }
    }

    private void pushToApi(TomaFisicaTomasEntity header, List<TomaFisicaDetallesEntity> detalles) {
        // 1. Push Header
        tomasDao.pushSubtoma(header, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                // Check HTTP success AND API logical success
                boolean apiSuccess = response.success && (response.data == null || response.data.success);
                
                if (apiSuccess) {
                    // 2. Push Details
                    pushDetailsToApi(detalles);
                } else {
                    String errorMsg = response.errorMessage;
                    if (response.data != null && response.data.errorMessage != null) {
                        errorMsg = response.data.errorMessage;
                    }
                    final String finalError = errorMsg;
                    runOnUiThread(() -> {
                        Toast.makeText(NuevaTomaActivity.this, "Error subiendo cabecera: " + finalError, Toast.LENGTH_LONG).show();
                        btnSubir.setEnabled(true);
                    });
                }
            }
        });
    }

    private void pushDetailsToApi(List<TomaFisicaDetallesEntity> detalles) {
        detallesDao.pushDetalle(detalles, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onComplete(ApiResponse<ApiResponse<Void>> response) {
                runOnUiThread(() -> {
                    boolean apiSuccess = response.success && (response.data == null || response.data.success);

                    if (apiSuccess) {
                        Toast.makeText(NuevaTomaActivity.this, "Subtoma subida exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = response.errorMessage;
                        if (response.data != null && response.data.errorMessage != null) {
                            errorMsg = response.data.errorMessage;
                        }
                        Toast.makeText(NuevaTomaActivity.this, "Error subiendo detalles: " + errorMsg, Toast.LENGTH_LONG).show();
                        btnSubir.setEnabled(true);
                    }
                });
            }
        });
    }

    // ResponseHandlerInterface methods

    @Override
    public void handleTagdata(TagData[] tagData) {
        if (tagData == null) return;

        boolean newTagsFound = false;
        for (TagData tag : tagData) {
            String epc = tag.getTagID();
            if (epc != null && !uniqueTags.contains(epc)) {
                uniqueTags.add(epc);
                scannedTagsList.add(epc);
                newTagsFound = true;
            }
        }

        if (newTagsFound) {
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                int count = uniqueTags.size();
                txtGaugeCount.setText(String.valueOf(count));
                gaugeResumen.setProgress(count); // Max default is 100, might need adjustment
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            startScan();
        } else {
            stopScan();
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        runOnUiThread(() -> Toast.makeText(this, Text, Toast.LENGTH_SHORT).show());
    }
}
