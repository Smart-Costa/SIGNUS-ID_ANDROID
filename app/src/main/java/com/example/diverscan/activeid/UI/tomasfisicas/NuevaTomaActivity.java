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
import com.example.diverscan.activeid.data.local.dao.UbicacionDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.JsonObject;
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
import java.util.Map;
import java.util.UUID;
import android.content.Intent;
import android.widget.AdapterView;
import androidx.core.content.ContextCompat;

public class NuevaTomaActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final String TAG = "NuevaTomaActivity";
    private String tomaFisicaId;
    private String idToma;
    private String numeroToma;
    private int currentTomaIndex = 1;

    // UI Components
    private android.widget.Spinner spinnerActivos;
    private android.widget.Spinner spinnerUbicacionA;
    private android.widget.Spinner spinnerUbicacionB;
    private android.widget.Spinner spinnerUbicacionC;
    private android.widget.Spinner spinnerUbicacionD;
    private android.widget.Spinner spinnerUbicacionSecundaria;
    private android.widget.CheckBox chkIncluirExternos;

    private android.widget.ProgressBar progressCargaManual;
    private TextView txtSinDatosManual;
    private Button btnAgregarLectura;
    private android.widget.EditText etManualInput;
    private android.widget.Button btnManualAdd;
    private List<ActivoEntity> listaActivosSpinner;
    private TextView lblConteos;
    private HorizontalScrollView scrollTomas;
    private LinearLayout containerTomasTabs;
    private Button tabResumen, tabActivos;
    private LinearLayout viewResumen;
    private LinearLayout viewActivos;
    private CircularProgressIndicator gaugeResumen;
    private TextView txtGaugeCount;
    private TextView txtCountActivosFiltrados;
    private TextView txtCountUbicacionA;
    private TextView txtCountUbicacionB;
    private TextView txtCountUbicacionC;
    private TextView txtCountUbicacionD;
    private TextView txtCountUbicacionSecundaria;
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
    private java.util.Map<String, String> manualEpcToActivoId = new java.util.HashMap<>();
    private java.util.Map<String, String> epcToDisplayName = new java.util.HashMap<>();

    private TomaFisicaTomasDao tomasDao;
    private TomaFisicaDetallesDao detallesDao;
    private TomaFisicaDao tomaFisicaDao;
    private ActivoDao activoDao;
    private UbicacionDao ubicacionDao;

    // Spinner Data
    private List<SpinnerItem> listUbicacionA = new ArrayList<>();
    private List<SpinnerItem> listUbicacionB = new ArrayList<>();
    private List<SpinnerItem> listUbicacionC = new ArrayList<>();
    private List<SpinnerItem> listUbicacionD = new ArrayList<>();
    private List<String> listUbicacionSecundaria = new ArrayList<>();

    private String selectedUbicacionAId = null;
    private String selectedUbicacionBId = null;
    private String selectedUbicacionCId = null;
    private String selectedUbicacionDId = null;
    private String selectedUbicacionSecundariaId = null;

    private String baseUbicacionAId = null;
    private String baseUbicacionBId = null;
    private String baseUbicacionCId = null;
    private String baseUbicacionDId = null;
    private int totalActivosInventario = 0;

    private boolean isUpdatingSpinners = false;

    private static class SpinnerItem {
        String id;
        String text;
        public SpinnerItem(String id, String text) { this.id = id; this.text = text; }
        @Override public String toString() { return text; }
    }

    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_toma);

        Log.d(TAG, "onCreate: Iniciando NuevaTomaActivity");

        if (getIntent().hasExtra("tomaFisicaId")) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
            Log.d(TAG, "onCreate: tomaFisicaId recibido: " + tomaFisicaId);
        } else {
            Log.e(TAG, "onCreate: Error: No se recibiÃ³ ID de Toma FÃ­sica");
            Toast.makeText(this, "Error: No se recibiÃ³ ID de Toma FÃ­sica", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            // Initialize DAO
            tomasDao = new TomaFisicaTomasDao(this);
            detallesDao = new TomaFisicaDetallesDao(this);
            tomaFisicaDao = new TomaFisicaDao(this);
            activoDao = new ActivoDao(this);
            ubicacionDao = new UbicacionDao(this);

            validarBaseDeDatosLocal();

            // Calculate next Toma Number
            int existingCount = tomasDao.getPendientesCount(tomaFisicaId);
            currentTomaIndex = existingCount + 1;
            
            // Generate new ID for this Take
            idToma = UUID.randomUUID().toString();
            numeroToma = String.valueOf(currentTomaIndex);
            
            Log.d(TAG, "onCreate: Configurado idToma=" + idToma + ", numero=" + numeroToma);

            initUI();
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                } else {
                    initRFID();
                }
            } else {
                initRFID();
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error fatal inicializando actividad", e);
            Toast.makeText(this, "Error inicializando: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                initRFID();
            } else {
                Toast.makeText(this, "Permiso Bluetooth necesario para RFID", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void validarBaseDeDatosLocal() {
        new Thread(() -> {
            int count = activoDao.getActivosCount();
            if (count == 0) {
                runOnUiThread(() -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Base de datos vacÃ­a")
                        .setMessage("No se encontraron activos en la base de datos local. Por favor, sincronice para obtener los registros.")
                        .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
                });
            }
        }).start();
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
        txtCountActivosFiltrados = findViewById(R.id.txtCountActivosFiltrados);
        txtCountUbicacionA = findViewById(R.id.txtCountUbicacionA);
        txtCountUbicacionB = findViewById(R.id.txtCountUbicacionB);
        txtCountUbicacionC = findViewById(R.id.txtCountUbicacionC);
        txtCountUbicacionD = findViewById(R.id.txtCountUbicacionD);
        txtCountUbicacionSecundaria = findViewById(R.id.txtCountUbicacionSecundaria);
        
        listScannedTags = findViewById(R.id.listScannedTags);
        
        btnPotencia = findViewById(R.id.btnPotencia);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnSubir = findViewById(R.id.btnSubir);
        
        txtIniciar = findViewById(R.id.txtIniciar);
        iconIniciar = findViewById(R.id.iconIniciar);
        btnBack = findViewById(R.id.btnBack);

        spinnerActivos = findViewById(R.id.spinnerActivos);
        spinnerUbicacionA = findViewById(R.id.spinnerUbicacionA);
        spinnerUbicacionB = findViewById(R.id.spinnerUbicacionB);
        spinnerUbicacionC = findViewById(R.id.spinnerUbicacionC);
        spinnerUbicacionD = findViewById(R.id.spinnerUbicacionD);
        spinnerUbicacionSecundaria = findViewById(R.id.spinnerUbicacionSecundaria);
        chkIncluirExternos = findViewById(R.id.chkIncluirExternos);

        progressCargaManual = findViewById(R.id.progressCargaManual);
        txtSinDatosManual = findViewById(R.id.txtSinDatosManual);
        btnAgregarLectura = findViewById(R.id.btnAgregarLectura);
        etManualInput = findViewById(R.id.etManualInput);
        btnManualAdd = findViewById(R.id.btnManualAdd);
        
        initResumenBaseAndFilters();

        
        if (btnAgregarLectura != null) {
            btnAgregarLectura.setOnClickListener(v -> agregarLecturaManual());
        }
        if (btnManualAdd != null) {
            btnManualAdd.setOnClickListener(v -> agregarLecturaManualTexto());
        }

        if (chkIncluirExternos != null) {
            chkIncluirExternos.setOnCheckedChangeListener((buttonView, isChecked) -> {
                refreshActivosList();
            });
        }

        // Setup List Adapter
        scannedTagsList = new ArrayList<>();
        uniqueTags = new HashSet<>();
        
        // Custom Adapter to handle background color
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scannedTagsList) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                
                String epc = getItem(position);
                if (epc != null) {
                    // Try to resolve display text from cache map
                    String displayText = null;
                    if (manualEpcToActivoId != null && manualEpcToActivoId.containsKey(epc)) {
                        String id = manualEpcToActivoId.get(epc);
                        if (epcToDisplayName != null && epcToDisplayName.containsKey(id)) {
                            displayText = epcToDisplayName.get(id);
                        }
                    }
                    // If not in map (e.g. manual text entry or RFID not yet resolved to name), try direct cache
                    if (displayText == null && epcToDisplayName != null && epcToDisplayName.containsKey(epc)) {
                        displayText = epcToDisplayName.get(epc);
                    }
                    
                    text.setText(displayText != null ? displayText : epc);

                    boolean isSobrante = isActivoSobrante(epc);
                    if (isSobrante) {
                        view.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE")); // Light Red
                        text.setTextColor(android.graphics.Color.RED);
                    } else {
                        view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        text.setTextColor(android.graphics.Color.BLACK);
                    }
                }
                return view;
            }
        };
        listScannedTags.setAdapter(adapter);

        // Update Tabs (Tomas 1..5)
        updateTomasTabs();

        // Listeners
        tabResumen.setOnClickListener(v -> switchTab(true));
        tabActivos.setOnClickListener(v -> switchTab(false));

        btnIniciar.setOnClickListener(v -> toggleScan());
        btnSubir.setOnClickListener(v -> uploadTake());
        btnBack.setOnClickListener(v -> finish());
        
        btnPotencia.setOnClickListener(v -> Toast.makeText(this, "ConfiguraciÃ³n de potencia no disponible", Toast.LENGTH_SHORT).show());
        
        // Initial Tab State
        switchTab(true);
    }

    private static boolean isValidGuidFilter(String value) {
        if (value == null) return false;
        String v = value.trim();
        if (v.isEmpty()) return false;
        if (v.equalsIgnoreCase("NULL")) return false;
        return !v.equals("00000000-0000-0000-0000-000000000000");
    }

    private static String normalizeGuidFilter(String value) {
        return isValidGuidFilter(value) ? value.trim() : null;
    }

    private void updateGaugeDisplay() {
        int leidos = 0;
        
        if (uniqueTags != null) {
            for (String epc : uniqueTags) {
                if (epc == null) continue;
                String epcTrim = epc.trim();
                
                // If the tag matches the current filter (by EPC or ID), it counts towards progress.
                // Otherwise it is surplus/irrelevant for the current view.
                
                boolean isExpected = false;
                
                // 1. Check direct EPC match
                if (currentFilterExpectedEpcs != null && currentFilterExpectedEpcs.contains(epcTrim.toUpperCase())) {
                    isExpected = true;
                }
                
                // 2. If not, check by ID (manual or mapped)
                if (!isExpected) {
                    String actId = manualEpcToActivoId.get(epcTrim);
                    if (actId != null && currentFilterExpectedIds != null && currentFilterExpectedIds.contains(actId.trim().toUpperCase())) {
                        isExpected = true;
                    }
                }
                
                // 3. Fallback: if we haven't mapped it yet but it might be in DB, we should check?
                // But for gauge performance, we rely on the cached sets populated in refreshActivosList.
                // If manualEpcToActivoId misses it, it might be an unmapped tag.
                
                if (isExpected) {
                    leidos++;
                }
            }
        }
        
        if (gaugeResumen != null) {
            int max = totalActivosInventario > 0 ? totalActivosInventario : 1;
            gaugeResumen.setMax(max);
            gaugeResumen.setProgress(Math.min(leidos, max));
        }
        if (txtGaugeCount != null) {
            txtGaugeCount.setText(leidos + " de " + totalActivosInventario);
        }
    }

    private void initResumenBaseAndFilters() {
        if (txtGaugeCount != null) txtGaugeCount.setText("0 de 0");
        if (gaugeResumen != null) {
            gaugeResumen.setMax(1);
            gaugeResumen.setProgress(0);
        }

        new Thread(() -> {
            TomaFisicaEntity toma = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
            baseUbicacionAId = toma != null ? normalizeGuidFilter(toma.getUbicacionA()) : null;
            baseUbicacionBId = toma != null ? normalizeGuidFilter(toma.getUbicacionB()) : null;
            baseUbicacionCId = toma != null ? normalizeGuidFilter(toma.getUbicacionC()) : null;
            baseUbicacionDId = toma != null ? normalizeGuidFilter(toma.getUbicacionD()) : null;

            totalActivosInventario = activoDao.countActivosByFiltros(baseUbicacionAId, baseUbicacionBId, baseUbicacionCId, baseUbicacionDId);
            Log.d(TAG, "Inventario base: tomaFisicaId=" + tomaFisicaId
                + " A=" + baseUbicacionAId
                + " B=" + baseUbicacionBId
                + " C=" + baseUbicacionCId
                + " D=" + baseUbicacionDId
                + " totalActivosInventario=" + totalActivosInventario);

            runOnUiThread(() -> {
                updateGaugeDisplay();
                setupLocationSpinners();
            });
        }).start();
    }

    private void setSpinnerItems(android.widget.Spinner spinner, List<SpinnerItem> items) {
        if (spinner == null) return;
        isUpdatingSpinners = true;
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        isUpdatingSpinners = false;
    }

    private void setSpinnerStrings(android.widget.Spinner spinner, List<String> items) {
        if (spinner == null) return;
        isUpdatingSpinners = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        isUpdatingSpinners = false;
    }

    private void setSoloTodasForSpinnerItem(android.widget.Spinner spinner) {
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(null, "Todas"));
        setSpinnerItems(spinner, list);
    }

    private void setSoloTodasForString(android.widget.Spinner spinner) {
        List<String> list = new ArrayList<>();
        list.add("Todas");
        setSpinnerStrings(spinner, list);
    }

    private boolean isActivoSobrante(String epc) {
        if (epc == null) return false;
        
        // 1. Resolve ActivoId from EPC
        String actId = manualEpcToActivoId.get(epc);
        if (actId == null || actId.trim().isEmpty()) {
            // Fallback: Check if we have cached expected data
            if (cachedExpectedEpcs != null && cachedExpectedIds != null) {
                if (cachedExpectedEpcs.contains(epc)) return false; // Found in expected EPCs -> Not Sobrante
                
                // Try to find Activo to check ID
                // Warning: DB access on UI thread
                ActivoEntity a = activoDao.getActivoByEpc(epc);
                if (a != null && a.getIdActivo() != null) {
                    return !cachedExpectedIds.contains(a.getIdActivo().trim().toUpperCase());
                }
                // If not found in DB but scanned, and not in expected EPCs -> Sobrante
                return true; 
            }
            return false;
        } else {
            if (cachedExpectedIds != null) {
                return !cachedExpectedIds.contains(actId.trim().toUpperCase());
            }
        }
        return false;
    }

    // Cache for validation
    private Set<String> cachedExpectedEpcs = null;
    private Set<String> cachedExpectedIds = null;
    private Set<String> currentFilterExpectedEpcs = new HashSet<>();
    private Set<String> currentFilterExpectedIds = new HashSet<>();

    private void updateExpectedCache() {
        new Thread(() -> {
            TomaFisicaEntity toma = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
            if (toma != null) {
                List<ActivoEntity> expected = activoDao.getActivosByFiltros(
                        toma.getUbicacionA(),
                        toma.getUbicacionB(),
                        toma.getUbicacionC(),
                        toma.getUbicacionD()
                );
                Set<String> epcs = new HashSet<>();
                Set<String> ids = new HashSet<>();
                for (ActivoEntity a : expected) {
                    if (a.getTagEpc() != null) epcs.add(a.getTagEpc().trim());
                    if (a.getIdActivo() != null) ids.add(a.getIdActivo().trim().toUpperCase());
                }
                cachedExpectedEpcs = epcs;
                cachedExpectedIds = ids;
                
                runOnUiThread(() -> {
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }
    
    private void setupLocationSpinners() {
        if (spinnerUbicacionA == null || spinnerUbicacionB == null || spinnerUbicacionC == null || spinnerUbicacionD == null || spinnerUbicacionSecundaria == null) {
            Log.w(TAG, "setupLocationSpinners: spinners no disponibles en layout");
            return;
        }

        updateExpectedCache(); // Trigger cache update

        setSoloTodasForSpinnerItem(spinnerUbicacionB);
        setSoloTodasForSpinnerItem(spinnerUbicacionC);
        setSoloTodasForSpinnerItem(spinnerUbicacionD);
        setSoloTodasForString(spinnerUbicacionSecundaria);

        spinnerUbicacionA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionAId = (item != null && item.id != null) ? item.id : null;

                Log.d(TAG, "Filtro UbicacionA: id=" + selectedUbicacionAId + " text=" + (item != null ? item.text : ""));
                
                selectedUbicacionBId = null;
                selectedUbicacionCId = null;
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;
                
                if (selectedUbicacionAId != null) {
                    loadUbicacionB(selectedUbicacionAId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionB);
                        setSoloTodasForSpinnerItem(spinnerUbicacionC);
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshActivosList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionB.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionBId = (item != null && item.id != null) ? item.id : null;

                Log.d(TAG, "Filtro UbicacionB: id=" + selectedUbicacionBId + " text=" + (item != null ? item.text : ""));
                
                selectedUbicacionCId = null;
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionBId != null) {
                    loadUbicacionC(selectedUbicacionBId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionC);
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshActivosList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionCId = (item != null && item.id != null) ? item.id : null;

                Log.d(TAG, "Filtro UbicacionC: id=" + selectedUbicacionCId + " text=" + (item != null ? item.text : ""));
                
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionCId != null) {
                    loadUbicacionD(selectedUbicacionCId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshActivosList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionD.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionDId = (item != null && item.id != null) ? item.id : null;

                Log.d(TAG, "Filtro UbicacionD: id=" + selectedUbicacionDId + " text=" + (item != null ? item.text : ""));
                
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionDId != null) {
                    loadUbicacionSecundaria(selectedUbicacionDId);
                } else {
                    runOnUiThread(() -> setSoloTodasForString(spinnerUbicacionSecundaria));
                }
                refreshActivosList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerUbicacionSecundaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                String item = (String) parent.getItemAtPosition(position);
                selectedUbicacionSecundariaId = (item != null && !item.equals("Todas")) ? item : null;
                Log.d(TAG, "Filtro UbicacionSecundaria: value=" + selectedUbicacionSecundariaId);
                refreshActivosList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadUbicacionA();
    }

    private void loadUbicacionA() {
        new Thread(() -> {
            listUbicacionA = new ArrayList<>();
            if (baseUbicacionAId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByASysId(baseUbicacionAId);
                String label = (u != null && u.getUbicacionA() != null && !u.getUbicacionA().trim().isEmpty()) ? u.getUbicacionA().trim() : baseUbicacionAId;
                listUbicacionA.add(new SpinnerItem(baseUbicacionAId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionA();
                listUbicacionA.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionA.add(new SpinnerItem(u.getASysId(), u.getUbicacionA()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionA, listUbicacionA);
                if (baseUbicacionAId != null) {
                    selectedUbicacionAId = baseUbicacionAId;
                    loadUbicacionB(baseUbicacionAId);
                } else {
                    selectedUbicacionAId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionB);
                    setSoloTodasForSpinnerItem(spinnerUbicacionC);
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshActivosList();
                }
            });
        }).start();
    }

    private void loadUbicacionB(String parentId) {
        new Thread(() -> {
            listUbicacionB = new ArrayList<>();
            if (baseUbicacionBId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByBSysId(baseUbicacionBId);
                String label = (u != null && u.getUbicacionB() != null && !u.getUbicacionB().trim().isEmpty()) ? u.getUbicacionB().trim() : baseUbicacionBId;
                listUbicacionB.add(new SpinnerItem(baseUbicacionBId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionB(parentId);
                listUbicacionB.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionB.add(new SpinnerItem(u.getBSysId(), u.getUbicacionB()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionB, listUbicacionB);
                if (baseUbicacionBId != null) {
                    selectedUbicacionBId = baseUbicacionBId;
                    loadUbicacionC(baseUbicacionBId);
                } else {
                    selectedUbicacionBId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionC);
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshActivosList();
                }
            });
        }).start();
    }

    private void loadUbicacionC(String parentId) {
        new Thread(() -> {
            listUbicacionC = new ArrayList<>();
            if (baseUbicacionCId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByCSysId(baseUbicacionCId);
                String label = (u != null && u.getUbicacionC() != null && !u.getUbicacionC().trim().isEmpty()) ? u.getUbicacionC().trim() : baseUbicacionCId;
                listUbicacionC.add(new SpinnerItem(baseUbicacionCId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionC(parentId);
                listUbicacionC.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionC.add(new SpinnerItem(u.getCSysId(), u.getUbicacionC()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionC, listUbicacionC);
                if (baseUbicacionCId != null) {
                    selectedUbicacionCId = baseUbicacionCId;
                    loadUbicacionD(baseUbicacionCId);
                } else {
                    selectedUbicacionCId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshActivosList();
                }
            });
        }).start();
    }

    private void loadUbicacionD(String parentId) {
        new Thread(() -> {
            listUbicacionD = new ArrayList<>();
            if (baseUbicacionDId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByDSysId(baseUbicacionDId);
                String label = (u != null && u.getUbicacionD() != null && !u.getUbicacionD().trim().isEmpty()) ? u.getUbicacionD().trim() : baseUbicacionDId;
                listUbicacionD.add(new SpinnerItem(baseUbicacionDId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionD(parentId);
                listUbicacionD.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionD.add(new SpinnerItem(u.getDSysId(), u.getUbicacionD()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionD, listUbicacionD);
                if (baseUbicacionDId != null) {
                    selectedUbicacionDId = baseUbicacionDId;
                    loadUbicacionSecundaria(baseUbicacionDId);
                } else {
                    selectedUbicacionDId = null;
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshActivosList();
                }
            });
        }).start();
    }

    private void loadUbicacionSecundaria(String parentId) {
        new Thread(() -> {
            // NOTE: ActivoDao is used here because UbicacionSecundaria is in Activos table logic for now
            List<String> list = activoDao.getDistinctUbicacionSecundaria(parentId);
            listUbicacionSecundaria = new ArrayList<>();
            listUbicacionSecundaria.add("Todas");
            listUbicacionSecundaria.addAll(list);

            runOnUiThread(() -> {
                setSpinnerStrings(spinnerUbicacionSecundaria, listUbicacionSecundaria);
                selectedUbicacionSecundariaId = null;
                refreshActivosList();
            });
        }).start();
    }

    private void refreshActivosList() {
        String ua = selectedUbicacionAId != null ? selectedUbicacionAId : baseUbicacionAId;
        String ub = selectedUbicacionBId != null ? selectedUbicacionBId : baseUbicacionBId;
        String uc = selectedUbicacionCId != null ? selectedUbicacionCId : baseUbicacionCId;
        String ud = selectedUbicacionDId != null ? selectedUbicacionDId : baseUbicacionDId;
        String us = selectedUbicacionSecundariaId;

        // Perform calculation in background to avoid UI lag and ensure correct filtered count
        new Thread(() -> {
            // 1. Get expected assets for current filter to update Gauge Logic
            List<ActivoEntity> expected = activoDao.getActivosByFiltros(ua, ub, uc, ud, us);
            
            Set<String> expEpcs = new HashSet<>();
            Set<String> expIds = new HashSet<>();
            if (expected != null) {
                for(ActivoEntity a : expected) {
                    if(a.getTagEpc()!=null && !a.getTagEpc().trim().isEmpty()) expEpcs.add(a.getTagEpc().trim().toUpperCase());
                    if(a.getIdActivo()!=null && !a.getIdActivo().trim().isEmpty()) expIds.add(a.getIdActivo().trim().toUpperCase());
                }
            }
            int countFiltrados = expected != null ? expected.size() : 0;

            // 2. Calculate Counts for Location levels
            String ubForA = baseUbicacionBId;
            String ucForA = baseUbicacionCId;
            String udForA = baseUbicacionDId;

            String ubForB = selectedUbicacionBId != null ? selectedUbicacionBId : baseUbicacionBId;
            String ucForB = baseUbicacionCId;
            String udForB = baseUbicacionDId;

            String ucForC = selectedUbicacionCId != null ? selectedUbicacionCId : baseUbicacionCId;
            String udForC = baseUbicacionDId;

            String udForD = selectedUbicacionDId != null ? selectedUbicacionDId : baseUbicacionDId;

            int countA = activoDao.countActivosByFiltros(ua, ubForA, ucForA, udForA);
            int countB = activoDao.countActivosByFiltros(ua, ubForB, ucForB, udForB);
            int countC = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForC);
            int countD = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForD);
            int countS = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForD, us);

            runOnUiThread(() -> {
                // Update Gauge Data
                this.currentFilterExpectedEpcs = expEpcs;
                this.currentFilterExpectedIds = expIds;
                this.totalActivosInventario = countFiltrados;
                
                if (txtCountActivosFiltrados != null) txtCountActivosFiltrados.setText("Activos filtrados: " + countFiltrados);
                updateGaugeDisplay();

                // Update Location Counts
                if (txtCountUbicacionA != null) txtCountUbicacionA.setText("Activos: " + countA);
                if (txtCountUbicacionB != null) txtCountUbicacionB.setText("Activos: " + countB);
                if (txtCountUbicacionC != null) txtCountUbicacionC.setText("Activos: " + countC);
                if (txtCountUbicacionD != null) txtCountUbicacionD.setText("Activos: " + countD);
                if (txtCountUbicacionSecundaria != null) txtCountUbicacionSecundaria.setText("Activos: " + countS);
            });

            // 3. Load Spinner
            
            runOnUiThread(() -> {
                if (chkIncluirExternos != null && chkIncluirExternos.isChecked()) {
                    cargarActivosSpinner(null, null, null, null, null);
                } else {
                    cargarActivosSpinner(ua, ub, uc, ud, us);
                }
            });
            
        }).start();
    }
    
    
    private void cargarActivosSpinner(String ua, String ub, String uc, String ud, String us) {
        Log.d(TAG, "cargarActivosSpinner: Filtros -> A:" + ua + " B:" + ub + " C:" + uc + " D:" + ud + " Sec:" + us);
        
        if (progressCargaManual != null) progressCargaManual.setVisibility(View.VISIBLE);
        if (spinnerActivos != null) spinnerActivos.setVisibility(View.GONE);
        if (txtSinDatosManual != null) txtSinDatosManual.setVisibility(View.GONE);
        if (btnAgregarLectura != null) btnAgregarLectura.setVisibility(View.GONE);

        new Thread(() -> {
            listaActivosSpinner = new ArrayList<>();

            // Si todos son null, podrÃ­amos querer cargar TODO o NADA.
            // Asumimos carga basada en filtros.
            
            listaActivosSpinner = activoDao.getActivosByFiltros(ua, ub, uc, ud, us);
            
            Log.d(TAG, "Total activos cargados para selector manual: " + listaActivosSpinner.size());
            if (listaActivosSpinner != null) {
                for (int i = 0; i < Math.min(3, listaActivosSpinner.size()); i++) {
                    ActivoEntity a = listaActivosSpinner.get(i);
                    Log.d(TAG, "ActivoSample[" + i + "]: id=" + (a != null ? a.getIdActivo() : "")
                        + " num=" + (a != null ? a.getNumeroActivo() : "")
                        + " desc=" + (a != null ? a.getDescripcionCorta() : ""));
                }
            }
            
            runOnUiThread(() -> {
                if (progressCargaManual != null) progressCargaManual.setVisibility(View.GONE);

                Log.d(TAG, "Actualizando UI spinner. Items: " + (listaActivosSpinner != null ? listaActivosSpinner.size() : "null"));
                if (listaActivosSpinner != null && !listaActivosSpinner.isEmpty()) {
                    // Manual entry is visible now
                    if (spinnerActivos != null) spinnerActivos.setVisibility(View.VISIBLE);
                    if (txtSinDatosManual != null) txtSinDatosManual.setVisibility(View.GONE);
                    
                    List<String> nombres = new ArrayList<>();
                    for (ActivoEntity a : listaActivosSpinner) {
                        String nombre = a.getDescripcionCorta();
                        String numero = a.getNumeroActivo();

                        String displayNombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : "Sin Nombre";
                        String displayNumero = (numero != null && !numero.trim().isEmpty() && !numero.equalsIgnoreCase("null")) ? numero.trim() : "S/N";

                        nombres.add(displayNombre + " - " + displayNumero);
                    }
                    ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombres);
                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerActivos.setAdapter(adapterSpinner);
                    
                    if (btnAgregarLectura != null) {
                        btnAgregarLectura.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (spinnerActivos != null) spinnerActivos.setVisibility(View.GONE);
                    if (txtSinDatosManual != null) {
                        txtSinDatosManual.setVisibility(View.VISIBLE);
                        txtSinDatosManual.setText("No se encontraron activos con estos filtros");
                    }
                    if (btnAgregarLectura != null) btnAgregarLectura.setVisibility(View.GONE);
                    
                    spinnerActivos.setAdapter(null);
                }
            });
        }).start();
    }
    

    private boolean isValidLocation(String loc) {
        return loc != null && !loc.trim().isEmpty() && !loc.trim().equalsIgnoreCase("NULL");
    }

    private static String buildActivoDisplay(ActivoEntity a) {
        if (a == null) return "Activo";

        String placa = a.getNumeroActivo();
        String numeroEtiqueta = a.getNumeroEtiqueta();
        String serie = a.getNumeroSerie();
        String nombre = a.getDescripcionCorta();

        String id;
        if (placa != null && !placa.trim().isEmpty()) {
            id = placa.trim();
        } else if (numeroEtiqueta != null && !numeroEtiqueta.trim().isEmpty()) {
            id = numeroEtiqueta.trim();
        } else {
            id = a.getIdActivo() != null ? a.getIdActivo().trim() : "";
        }

        StringBuilder sb = new StringBuilder();
        if (id != null && !id.isEmpty()) sb.append(id);
        if (serie != null && !serie.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(serie.trim());
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(nombre.trim());
        }

        if (sb.length() == 0) {
            return "Sin DescripciÃ³n";
        }
        return sb.toString();
    }

    private void updateEpcDisplayCache(ActivoEntity a, String epc) {
        if (a == null) return;
        String display = buildActivoDisplay(a);
        
        // Cache by ID if available
        if (a.getIdActivo() != null) {
            epcToDisplayName.put(a.getIdActivo(), display);
        }
        // Cache by EPC as well for direct lookup
        if (epc != null) {
            epcToDisplayName.put(epc, display);
        }
    }

    
    private void agregarLecturaManualTexto() {
        if (etManualInput == null) return;
        String input = etManualInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Ingrese Placa o EPC", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Resolve Input to Activo to get real EPC if possible
        ActivoEntity a = null;
        // Try as EPC first
        a = activoDao.getActivoByEpc(input);
        
        if (a == null) {
             // Try as ID (Placa)
             a = activoDao.getActivoById(input);
        }

        String targetEpc = input;
        String activoId = null;

        if (a != null) {
            if (a.getTagEpc() != null && !a.getTagEpc().trim().isEmpty() && !a.getTagEpc().equalsIgnoreCase("EPC ASIGNADO")) {
                targetEpc = a.getTagEpc().trim();
            }
            activoId = a.getIdActivo();
        }

        if (uniqueTags.contains(targetEpc)) {
            Toast.makeText(this, "Este activo ya fue leído (" + targetEpc + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        uniqueTags.add(targetEpc);
        scannedTagsList.add(targetEpc);

        if (activoId != null) {
             manualEpcToActivoId.put(targetEpc, activoId);
             updateEpcDisplayCache(a, targetEpc);
             Toast.makeText(this, "Lectura añadida: " + (a.getDescripcionCorta() != null ? a.getDescripcionCorta() : targetEpc), Toast.LENGTH_SHORT).show();
        } else {
             Toast.makeText(this, "Lectura añadida: " + targetEpc, Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
        updateGaugeDisplay();
        etManualInput.setText("");
        
        // Auto-save
        new SaveLocalTask(false).execute();
    }
    

    
    private void agregarLecturaManual() {
        if (listaActivosSpinner == null || listaActivosSpinner.isEmpty()) {
             Toast.makeText(this, "No hay activos cargados", Toast.LENGTH_SHORT).show();
             return;
        }
        int pos = spinnerActivos.getSelectedItemPosition();
        if (pos >= 0 && pos < listaActivosSpinner.size()) {
            ActivoEntity activo = listaActivosSpinner.get(pos);
            String epc = activo.getTagEpc();

            String epcTrim = epc != null ? epc.trim() : "";
            boolean epcInvalido = epcTrim.isEmpty() || epcTrim.equalsIgnoreCase("EPC ASIGNADO");
            
            if (epcInvalido) {
                String id = activo.getIdActivo() != null ? activo.getIdActivo().trim() : "";
                epc = "MANUAL-" + id + "-" + System.currentTimeMillis();
            } else {
                epc = epcTrim;
            }
            
            // Si el EPC ya existe (ya fue leído o agregado), generamos uno nuevo manual 
            // para permitir agregar varios del mismo tipo (o copias)
            if (uniqueTags.contains(epc)) {
                 String id = activo.getIdActivo() != null ? activo.getIdActivo().trim() : "";
                 epc = "MANUAL-" + id + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0,4);
            }
            
            Log.d(TAG, "Intentando agregar manual: " + activo.getDescripcionCorta() + " EPC: " + epc);
            
            if (!uniqueTags.contains(epc)) {
                uniqueTags.add(epc);
                scannedTagsList.add(epc);
                if (activo.getIdActivo() != null) {
                    manualEpcToActivoId.put(epc, activo.getIdActivo());
                    updateEpcDisplayCache(activo, epc);
                }
                adapter.notifyDataSetChanged();
                updateGaugeDisplay();
                Toast.makeText(this, "Lectura añadida: " + activo.getDescripcionCorta(), Toast.LENGTH_SHORT).show();
                // Auto-save
                new SaveLocalTask(false).execute();
            } else {
                Log.d(TAG, "EPC duplicado: " + epc);
                Toast.makeText(this, "Este activo ya fue leído", Toast.LENGTH_SHORT).show();
            }
        }
    }
    



    private void updateTomasTabs() {
        new Thread(() -> {
            List<TomaFisicaTomasEntity> subtomas = tomasDao.getByTomaFisicaId(tomaFisicaId);
            if (subtomas == null) subtomas = new ArrayList<>();
            Collections.sort(subtomas, (o1, o2) -> {
                try {
                    return Integer.compare(Integer.parseInt(o1.getNumeroToma()), Integer.parseInt(o2.getNumeroToma()));
                } catch (Exception e) { return 0; }
            });

            final List<TomaFisicaTomasEntity> finalList = subtomas;
            runOnUiThread(() -> {
                containerTomasTabs.removeAllViews();
                
                // 1. Add existing tabs
                for (TomaFisicaTomasEntity t : finalList) {
                    TextView tab = new TextView(this);
                    tab.setText("Toma " + t.getNumeroToma());
                    
                    // In NuevaTomaActivity, we are creating a NEW one. 
                    // So existing ones are NEVER selected.
                    tab.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint)); 
                    tab.setBackgroundResource(R.drawable.btn_secondary_gray);
                    
                    tab.setPadding(48, 16, 48, 16);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, 
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 16, 0);
                    tab.setLayoutParams(params);
                    
                    tab.setOnClickListener(v -> {
                        Intent intent = new Intent(this, RegistroConteosActivity.class);
                        intent.putExtra("tomaFisicaId", tomaFisicaId);
                        intent.putExtra("idToma", t.getIdToma());
                        intent.putExtra("numeroToma", t.getNumeroToma());
                        startActivity(intent);
                        // We might finish this activity or keep it in stack?
                        // If we finish, we lose the "New" state if not saved.
                        // Usually navigating away loses unsaved data.
                        // Ideally we prompt or just let it happen. 
                        // I'll finish() to keep stack clean.
                        finish();
                    });
                    containerTomasTabs.addView(tab);
                }

                // 2. Add CURRENT (New) tab
                TextView currentTab = new TextView(this);
                currentTab.setText("Toma " + numeroToma);
                currentTab.setTextColor(ContextCompat.getColor(this, R.color.blanco));
                currentTab.setBackgroundResource(R.drawable.btn_dark_gray);
                currentTab.setPadding(48, 16, 48, 16);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                currentTab.setLayoutParams(params);
                containerTomasTabs.addView(currentTab);
            });
        }).start();
    }

    private void switchTab(boolean isResumen) {
        if (isResumen) {
            viewResumen.setVisibility(View.VISIBLE);
            viewActivos.setVisibility(View.GONE);
            
            // Tab Resumen Activo (Naranja)
            tabResumen.setBackgroundResource(R.drawable.btn_primary);
            tabResumen.setTextColor(ContextCompat.getColor(this, R.color.blanco));
            
            // Tab Activos Inactivo (Gris)
            tabActivos.setBackgroundResource(R.drawable.btn_secondary_gray);
            tabActivos.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
        } else {
            viewResumen.setVisibility(View.GONE);
            viewActivos.setVisibility(View.VISIBLE);
            
            // Tab Resumen Inactivo (Gris)
            tabResumen.setBackgroundResource(R.drawable.btn_secondary_gray);
            tabResumen.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));

            // Tab Activos Activo (Naranja)
            tabActivos.setBackgroundResource(R.drawable.btn_primary);
            tabActivos.setTextColor(ContextCompat.getColor(this, R.color.blanco));
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
            // Usamos getActivosByFiltros para obtener el total correcto basado en UbicacionA, B, C, D
            List<ActivoEntity> expected = activoDao.getActivosByFiltros(
                    tomaFisica.getUbicacionA(),
                    tomaFisica.getUbicacionB(),
                    tomaFisica.getUbicacionC(),
                    tomaFisica.getUbicacionD()
            );

            int totalActivos = expected.size();
            int encontrados = 0;
            int sobrantes = 0;

            Set<String> expectedEpcs = new HashSet<>();
            Set<String> expectedIds = new HashSet<>();
            for (ActivoEntity a : expected) {
                if (a.getTagEpc() != null) expectedEpcs.add(a.getTagEpc().trim());
                if (a.getIdActivo() != null) expectedIds.add(a.getIdActivo().trim().toUpperCase());
            }

            for (String scanned : uniqueTags) {
                String scannedTrim = scanned != null ? scanned.trim() : "";
                String actId = manualEpcToActivoId.get(scannedTrim);
                if (actId == null || actId.trim().isEmpty()) {
                    ActivoEntity a = activoDao.getActivoByEpc(scannedTrim);
                    if (a != null && a.getIdActivo() != null) actId = a.getIdActivo();
                }
                boolean isExpected;
                if (manualEpcToActivoId.containsKey(scannedTrim)) {
                    // Manual: Strict ID check
                    isExpected = actId != null && expectedIds.contains(actId.trim().toUpperCase());
                } else {
                    // Auto: EPC match OR ID match
                    isExpected = expectedEpcs.contains(scannedTrim)
                            || (actId != null && expectedIds.contains(actId.trim().toUpperCase()));
                }

                if (isExpected) {
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
        private boolean navigateAfterSave = false;

        public SaveLocalTask() {
            this.navigateAfterSave = false;
        }

        public SaveLocalTask(boolean navigate) {
            this.navigateAfterSave = navigate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (navigateAfterSave) {
                Toast.makeText(NuevaTomaActivity.this, "Guardando y saliendo...", Toast.LENGTH_SHORT).show();
            }
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
                
                // Preparar lÃ³gica de estados
                TomaFisicaEntity tomaFisica = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
                String baseA = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionA()) : null;
                String baseB = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionB()) : null;
                String baseC = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionC()) : null;
                String baseD = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionD()) : null;
                if (baseA == null) baseA = "00000000-0000-0000-0000-000000000000";
                Set<String> expectedEpcs = new HashSet<>();
                Set<String> expectedIds = new HashSet<>();
                if (tomaFisica != null) {
                    List<ActivoEntity> expected = activoDao.getActivosByFiltros(
                            tomaFisica.getUbicacionA(),
                            tomaFisica.getUbicacionB(),
                            tomaFisica.getUbicacionC(),
                            tomaFisica.getUbicacionD()
                    );
                    for (ActivoEntity a : expected) {
                        if (a.getTagEpc() != null) expectedEpcs.add(a.getTagEpc().trim());
                        if (a.getIdActivo() != null) expectedIds.add(a.getIdActivo().trim().toUpperCase());
                    }
                }

                for (String epc : uniqueTags) {
                    if (!existingEpcs.contains(epc)) {
                        TomaFisicaDetallesEntity detail = new TomaFisicaDetallesEntity();
                        detail.setIdTakeDetail(UUID.randomUUID().toString());
                        detail.setIdToma(idToma);
                        detail.setNumeroToma(numeroToma);
                        detail.setFechaToma(now);
                        detail.setEpc(epc);
                        detail.setDateRead(now);

                        // Resolve ActivoId
                        String activoId = manualEpcToActivoId.get(epc);
                        if (activoId == null) {
                            ActivoEntity a = activoDao.getActivoByEpc(epc);
                            if (a != null) activoId = a.getIdActivo();
                        }
                        if (activoId != null) {
                            detail.setActivoId(activoId);
                            Log.d(TAG, "Asignado ActivoId: " + activoId + " para EPC: " + epc);
                        } else {
                            Log.w(TAG, "No se encontrÃ³ ActivoId para EPC: " + epc);
                        }

                        ActivoEntity activo = null;
                        if (activoId != null && !activoId.trim().isEmpty()) {
                            activo = activoDao.getActivoByIdActivo(activoId);
                        }
                        if (activo == null) {
                            activo = activoDao.getActivoByEpc(epc);
                        }

                        String ua = activo != null ? normalizeGuidFilter(activo.getUbicacionA()) : null;
                        String ub = activo != null ? normalizeGuidFilter(activo.getUbicacionB()) : null;
                        String uc = activo != null ? normalizeGuidFilter(activo.getUbicacionC()) : null;
                        String ud = activo != null ? normalizeGuidFilter(activo.getUbicacionD()) : null;

                        detail.setUbicacionDetalleA(ua != null ? ua : baseA);
                        detail.setUbicacionDetalleB(ub != null ? ub : baseB);
                        detail.setUbicacionDetalleC(uc != null ? uc : baseC);
                        detail.setUbicacionDetalleD(ud != null ? ud : baseD);
                        detail.setObservaciones(detail.getObservaciones() != null ? detail.getObservaciones() : "");
                        
                        boolean isExpected;
                        if (manualEpcToActivoId.containsKey(epc)) {
                             isExpected = activoId != null && expectedIds.contains(activoId.trim().toUpperCase());
                        } else {
                             isExpected = expectedEpcs.contains(epc) || (activoId != null && expectedIds.contains(activoId.trim().toUpperCase()));
                        }

                        if (isExpected) {
                            detail.setEstadoInventario("ENCONTRADO");
                        } else {
                            if (activo != null) {
                                detail.setEstadoInventario("Sobrante");
                            } else {
                                detail.setEstadoInventario("No Inventariado");
                            }
                        }
                        
                        detalles.add(detail);
                    }
                }

                // Guardar Header (Upsert)
                List<TomaFisicaTomasEntity> headers = new ArrayList<>();
                headers.add(header);
                tomasDao.saveLocal(headers);

                // Guardar Detalles
                if (!detalles.isEmpty()) {
                    detallesDao.saveLocal(detalles);
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
                if (navigateAfterSave) {
                    Toast.makeText(NuevaTomaActivity.this, "Guardado localmente. EnvÃ­e desde Sincronizar", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(NuevaTomaActivity.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initRFID() {
        try {
            rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.setResponseHandler(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RFID", e);
            Toast.makeText(this, "Error RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            try {
                rfidHandler.startRead();
                isScanning = true;
                updateUIState();
            } catch (Exception e) {
                Log.e(TAG, "Error starting scan", e);
                Toast.makeText(this, "Error al iniciar lectura: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopScan() {
        if (rfidHandler != null) {
            try {
                rfidHandler.stopRead();
                isScanning = false;
                updateUIState();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping scan", e);
            }
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

        new SaveLocalTask(true).execute();
    }

    private class SaveAndPushTask extends AsyncTask<Void, Void, Boolean> {
        private TomaFisicaTomasEntity header;
        private List<TomaFisicaDetallesEntity> detalles;

        public SaveAndPushTask(TomaFisicaTomasEntity header, List<TomaFisicaDetallesEntity> detalles) {
            this.header = calculateSummary(); // Recalcular con datos actuales para asegurar consistencia
            this.header.setIdToma(header.getIdToma()); // Preservar ID
            this.header.setNumeroToma(header.getNumeroToma()); // Preservar Numero
            
            this.detalles = detalles;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(NuevaTomaActivity.this, "Guardando y subiendo...", Toast.LENGTH_SHORT).show();
            btnSubir.setEnabled(false);
            
            // Mostrar loading si existe
            // if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // 1. Asignar EstadoInventario correcto antes de guardar
                // Usamos la misma lÃ³gica que calculateSummary para determinar si es encontrado o sobrante
                TomaFisicaEntity tomaFisica = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
                String baseA = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionA()) : null;
                String baseB = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionB()) : null;
                String baseC = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionC()) : null;
                String baseD = tomaFisica != null ? normalizeGuidFilter(tomaFisica.getUbicacionD()) : null;
                if (baseA == null) baseA = "00000000-0000-0000-0000-000000000000";
                Set<String> expectedEpcs = new HashSet<>();
                Set<String> expectedIds = new HashSet<>();
                if (tomaFisica != null) {
                    List<ActivoEntity> expected = activoDao.getActivosByFiltros(
                        tomaFisica.getUbicacionA(),
                        tomaFisica.getUbicacionB(),
                        tomaFisica.getUbicacionC(),
                        tomaFisica.getUbicacionD()
                    );
                    for (ActivoEntity a : expected) {
                        if (a.getTagEpc() != null) expectedEpcs.add(a.getTagEpc().trim());
                        if (a.getIdActivo() != null) expectedIds.add(a.getIdActivo().trim().toUpperCase());
                    }
                }

                for (TomaFisicaDetallesEntity d : detalles) {
                    // Resolve ActivoId if missing
                    if (d.getActivoId() == null || d.getActivoId().isEmpty()) {
                        String epc = d.getEpc();
                        String actId = manualEpcToActivoId.get(epc);
                        if (actId == null) {
                            ActivoEntity a = activoDao.getActivoByEpc(epc);
                            if (a != null) actId = a.getIdActivo();
                        }
                        if (actId != null) d.setActivoId(actId);
                    }

                    ActivoEntity activo = null;
                    if (d.getActivoId() != null && !d.getActivoId().trim().isEmpty()) {
                        activo = activoDao.getActivoByIdActivo(d.getActivoId().trim());
                    }
                    if (activo == null && d.getEpc() != null && !d.getEpc().trim().isEmpty()) {
                        activo = activoDao.getActivoByEpc(d.getEpc().trim());
                    }

                    String ua = activo != null ? normalizeGuidFilter(activo.getUbicacionA()) : null;
                    String ub = activo != null ? normalizeGuidFilter(activo.getUbicacionB()) : null;
                    String uc = activo != null ? normalizeGuidFilter(activo.getUbicacionC()) : null;
                    String ud = activo != null ? normalizeGuidFilter(activo.getUbicacionD()) : null;

                    if (d.getUbicacionDetalleA() == null || d.getUbicacionDetalleA().trim().isEmpty() || d.getUbicacionDetalleA().equalsIgnoreCase("NULL")) {
                        d.setUbicacionDetalleA(ua != null ? ua : baseA);
                    }
                    if (d.getUbicacionDetalleB() == null || d.getUbicacionDetalleB().trim().isEmpty() || d.getUbicacionDetalleB().equalsIgnoreCase("NULL")) {
                        d.setUbicacionDetalleB(ub != null ? ub : baseB);
                    }
                    if (d.getUbicacionDetalleC() == null || d.getUbicacionDetalleC().trim().isEmpty() || d.getUbicacionDetalleC().equalsIgnoreCase("NULL")) {
                        d.setUbicacionDetalleC(uc != null ? uc : baseC);
                    }
                    if (d.getUbicacionDetalleD() == null || d.getUbicacionDetalleD().trim().isEmpty() || d.getUbicacionDetalleD().equalsIgnoreCase("NULL")) {
                        d.setUbicacionDetalleD(ud != null ? ud : baseD);
                    }
                    if (d.getObservaciones() == null) d.setObservaciones("");

                    String epcVal = d.getEpc() != null ? d.getEpc().trim() : "";
                    boolean isExpected;
                    String epcKey = d.getEpc();
                    if (epcKey != null && manualEpcToActivoId.containsKey(epcKey)) {
                         isExpected = d.getActivoId() != null && expectedIds.contains(d.getActivoId().trim().toUpperCase());
                    } else {
                         isExpected = expectedEpcs.contains(epcVal) 
                                || (d.getActivoId() != null && expectedIds.contains(d.getActivoId().trim().toUpperCase()));
                    }

                    if (isExpected) {
                        d.setEstadoInventario("ENCONTRADO");
                    } else {
                        if (activo != null) {
                            d.setEstadoInventario("Sobrante");
                        } else {
                            d.setEstadoInventario("No Inventariado");
                        }
                    }
                }

                // 2. Guardar Local
                tomasDao.saveLocal(Collections.singletonList(header));
                detallesDao.saveLocal(detalles);
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
        Log.d(TAG, "pushToApi: Iniciando subida. Header=" + header.getIdToma());
        // 1. Push Header
        tomasDao.pushSubtoma(header, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                Log.d(TAG, "pushToApi: Header response. Success=" + response.success + " Code=" + response.statusCode);
                boolean apiSuccess = response.success
                        && response.data != null
                        && response.data.has("success")
                        && response.data.get("success").getAsBoolean();

                if (apiSuccess) {
                    Log.d(TAG, "pushToApi: Header subido correctamente. Subiendo detalles...");
                    tomasDao.markAsSynced(header.getIdToma());
                    // 2. Push Details
                    pushDetailsToApi(detalles);
                } else {
                    String errorMsg = response.errorMessage;
                    if (response.success && response.data != null && response.data.has("message") && !response.data.get("message").isJsonNull()) {
                        errorMsg = response.data.get("message").getAsString();
                    }
                    Log.e(TAG, "pushToApi: Error subiendo header: " + errorMsg);
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
        Log.d(TAG, "pushDetailsToApi: Iniciando subida detalles. Cantidad=" + (detalles != null ? detalles.size() : 0));
        detallesDao.pushDetalle(detalles, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                Log.d(TAG, "pushDetailsToApi: Response. Success=" + response.success + " Code=" + response.statusCode);
                runOnUiThread(() -> {
                    boolean apiSuccess = response.success
                            && response.data != null
                            && response.data.has("success")
                            && response.data.get("success").getAsBoolean();

                    if (apiSuccess) {
                        Log.d(TAG, "pushDetailsToApi: Detalles subidos correctamente.");
                        for (TomaFisicaDetallesEntity d : detalles) {
                            if (d == null) continue;
                            if (d.getIdTakeDetail() == null || d.getIdTakeDetail().trim().isEmpty()) continue;
                            detallesDao.markAsSynced(d.getIdTakeDetail());
                        }
                        Toast.makeText(NuevaTomaActivity.this, "Subtoma subida exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = response.errorMessage;
                        if (response.success && response.data != null && response.data.has("message") && !response.data.get("message").isJsonNull()) {
                            errorMsg = response.data.get("message").getAsString();
                        }
                        Log.e(TAG, "pushDetailsToApi: Error subiendo detalles: " + errorMsg);
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
        if (tagData == null || tagData.length == 0) return;

        runOnUiThread(() -> {
            List<String> tagsToProcess = new ArrayList<>();
            boolean listChanged = false;

            for (TagData tag : tagData) {
                String epc = tag.getTagID();
                if (epc != null && !uniqueTags.contains(epc)) {
                    uniqueTags.add(epc);
                    scannedTagsList.add(epc);
                    tagsToProcess.add(epc);
                    listChanged = true;
                }
            }

            if (listChanged) {
                adapter.notifyDataSetChanged();
                updateGaugeDisplay();

                if (!tagsToProcess.isEmpty()) {
                    new Thread(() -> {
                        boolean cacheUpdated = false;
                        for (String epc : tagsToProcess) {
                            ActivoEntity activo = activoDao.getActivoByEpc(epc);
                            if (activo != null) {
                                String desc = (activo.getDescripcionCorta() != null) ? activo.getDescripcionCorta() : "Desconocido";
                                String display = desc + " - " + epc;
                                epcToDisplayName.put(epc, display);
                                if (activo.getIdActivo() != null) {
                                    manualEpcToActivoId.put(epc, activo.getIdActivo());
                                }
                                cacheUpdated = true;
                            }
                        }
                        if (cacheUpdated) {
                            runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                updateGaugeDisplay();
                                new SaveLocalTask(false).execute();
                            });
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            if (pressed) {
                startScan();
            } else {
                stopScan();
            }
        });
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

