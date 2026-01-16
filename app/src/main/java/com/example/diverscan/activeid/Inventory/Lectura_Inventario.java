package com.example.diverscan.activeid.Inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.TomasFisicas.TomasFisias;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivos;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.zebra.rfid.api3.TagData;

import com.example.diverscan.activeid.RazonSocial.RazonNuevo;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioNuevo;
import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Piso.PisoNuevo;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Lectura_Inventario extends AppCompatActivity implements ResponseHandlerInterface, IChequearInventario {
    AssetsDBHelper AssetsDBHelper;
    private TextView txt_TagsLeidos;
    private String Faltante = "Faltante", NoPertenece = "No Pertenece", Encontrado = "Encontrado",
            idTake, takeName, takeDescription, takeDate, idOficina, idTypeInventory;
    int contadorActivosFaltantes = 0, contActivosEncontrados = 0, contadorActivosSobrantes = 0;
    private RecyclerView ListaLectura;
    private EditText txtEncontrados, txtSobrantes, txtFaltantes, txtLeidas, txtBarcode, txtUbicacion;
    private EditText etManualInput;
    private Button btnManualAdd;
    public int CantidadEPCLeida, EPCEncontrados, EPCSobrantes, EPCActivosUbicacion, EPCFaltantes;
    OfficesDBHelper OfficesDBHelper;
    private Button GuardarRessultado;
    AdaptadorLecturas itemAdapterAssets;
    private View LecturaInventarioView;
    private ArrayList<String> _activosEncontrados, _activosFaltantes, _activosActivoNoPertenece = new ArrayList<String>();
    private ArrayList<EntidadDetalleInventario> _detalleInventario = new ArrayList<EntidadDetalleInventario>();
    private ArrayList<EntidadInventario> _inventario = new ArrayList<EntidadInventario>();
    private ArrayList<TomasFisias> activoInventarios = new ArrayList<TomasFisias>();
    ArrayList<InventarioVisual> inventarioVisuals = new ArrayList<InventarioVisual>();
    private ArrayList<InventarioVisual> masterInventarioVisuals = new ArrayList<>();

    private ArrayList<String> _activosSobrantes = new ArrayList<String>();
    private ArrayList<String> _activosEncontrado = new ArrayList<String>();
    private ArrayList<String> _activosNoExiste = new ArrayList<String>();
    InventoryDBHelper inventoryDBHelper;
    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false, scannerActivate = false;
    ChequearInventario _chequearInventario;
    Activity _activity;
    Context _context;
    AlertDialog alertDialog;
    private long startTime = 1 * 60 * 15000;
    private final long interval = 1 * 1000;
    CountDownTimer sessionActivate;

    private ProgressDialog dialog;
    ConstraintLayout rlsnackbar;
    Snackbar _snackbar;
    private Switch OnRfid;
    public EntidadActivos entidadActivos;
    //*************************************************************************************************************

    // Nuevos controles de UI
    private android.widget.RadioGroup tabGroupTomas;
    private Button btnResumen, btnActivos, btnAgregarManual;
    private View viewResumen, viewActivos;
    private android.widget.ProgressBar gaugeProgress;
    private TextView txtGaugeCount, txtGaugeTotal;
    private android.widget.Spinner spinnerUbicacionA, spinnerUbicacionB, spinnerUbicacionC, spinnerUbicacionD, spinnerUbicacionSecundaria;
    private Button btnVerEncontrados, btnVerFaltantes, btnVerSobrantes;
    private String currentStatusFilter = "Todos";
    
    // Bottom Navbar
    private android.widget.LinearLayout btnPotencia, btnIniciar, btnSubir;
    private android.widget.ImageView iconIniciar;
    private TextView txtIniciar;

    private String currentIdSubToma;
    private com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao subTomasDao;
    private com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao detallesDao;
    private ActivoDao activoDao;

    // Mapas para control de selección de filtros
    private Map<Integer, RazonNuevo> _mapRazonSociales = new HashMap<>();
    private Map<Integer, EdificioNuevo> _mapEdificios = new HashMap<>();
    private Map<Integer, PisoNuevo> _mapPisos = new HashMap<>();
    private Map<Integer, oficinaNuevo> _mapOficinas = new HashMap<>();
    
    // Flags para control de listeners
    private boolean _itemSelectedUserCompania = false;
    private boolean _itemSelectedUserEdificio = false;
    private boolean _itemSelectedUserPiso = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectura_inventario);
        getSupportActionBar().hide();
        _activity = this;
        _context = this;
        
        // Inicializar DAO
        subTomasDao = new com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao(this);
        detallesDao = new com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao(this);
        activoDao = new ActivoDao(this);
        
        // Inicializar nuevos controles
        tabGroupTomas = findViewById(R.id.tabGroupTomas);
        btnResumen = findViewById(R.id.btnResumen);
        btnActivos = findViewById(R.id.btnActivos);
        viewResumen = findViewById(R.id.viewResumen);
        viewActivos = findViewById(R.id.viewActivos);
        gaugeProgress = findViewById(R.id.gaugeProgress);
        btnAgregarManual = findViewById(R.id.btnAgregarManual);
        txtGaugeCount = findViewById(R.id.txtGaugeCount);
        txtGaugeTotal = findViewById(R.id.txtGaugeTotal);
        
        spinnerUbicacionA = findViewById(R.id.spinnerUbicacionA);
        spinnerUbicacionB = findViewById(R.id.spinnerUbicacionB);
        spinnerUbicacionC = findViewById(R.id.spinnerUbicacionC);
        spinnerUbicacionD = findViewById(R.id.spinnerUbicacionD);
        spinnerUbicacionSecundaria = findViewById(R.id.spinnerUbicacionSecundaria);
        
        setupToggle();

        controles();
        
        // Cargar filtros al inicio (independiente de la toma)
        loadFilters();
        
        eventos();
        
        try {
            rfidHandler = TagWriter.getInstance();
            rfidHandler.onCreate(this);
        } catch (Exception e) {
            Log.e("Lectura_Inventario", "Error initializing RFID", e);
            Toast.makeText(this, "Advertencia: No se pudo iniciar el lector RFID", Toast.LENGTH_SHORT).show();
        }
        
        // Obtener Extras y configurar Tabs
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idTake = extras.getString("IdTake");
            currentIdSubToma = extras.getString("IdSubToma");
        }
        
        if (idTake != null) {
            setupTabs();
        }

        hideSoftKeyboard();
        RecibirTakesInfo();
        sessionActivate = new CountDownTimer(startTime, interval) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(Lectura_Inventario.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
    }
    
    private void setupTabs() {
        java.util.List<com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity> subTomas = subTomasDao.getByTomaFisicaId(idTake);
        
        tabGroupTomas.removeAllViews();
        for (com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity toma : subTomas) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setText("Toma " + toma.getNumeroToma());
            rb.setTag(toma.getIdToma());
            rb.setId(View.generateViewId());
            
            // Estilo básico para Tabs (fondo gris claro por defecto)
            android.widget.RadioGroup.LayoutParams params = new android.widget.RadioGroup.LayoutParams(
                android.widget.RadioGroup.LayoutParams.WRAP_CONTENT,
                android.widget.RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            rb.setLayoutParams(params);
            
            rb.setBackgroundColor(Color.parseColor("#E0E0E0"));
            rb.setPadding(32, 16, 32, 16);
            rb.setButtonDrawable(null); // Quitar el círculo del radio button
            rb.setGravity(android.view.Gravity.CENTER);
            
            if (toma.getIdToma().equals(currentIdSubToma)) {
                rb.setChecked(true);
                rb.setBackgroundColor(Color.parseColor("#555555"));
                rb.setTextColor(Color.WHITE);
            } else {
                rb.setTextColor(Color.BLACK);
            }
            
            tabGroupTomas.addView(rb);
        }
        
        tabGroupTomas.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                android.widget.RadioButton btn = (android.widget.RadioButton) group.getChildAt(i);
                if (btn.getId() == checkedId) {
                    btn.setBackgroundColor(Color.parseColor("#555555"));
                    btn.setTextColor(Color.WHITE);
                    currentIdSubToma = (String) btn.getTag();
                    // Aquí se debería recargar la información para la nueva sub-toma seleccionada
                    loadResumenData();
                } else {
                    btn.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    btn.setTextColor(Color.BLACK);
                }
            }
        });
        
        loadResumenData();
    }
    
    private void setupToggle() {
        btnResumen.setOnClickListener(v -> {
            viewResumen.setVisibility(View.VISIBLE);
            viewActivos.setVisibility(View.GONE);
            btnResumen.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5722")));
            btnActivos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnResumen.setTextColor(Color.WHITE);
            btnActivos.setTextColor(Color.BLACK);
        });
        
        btnActivos.setOnClickListener(v -> {
            viewResumen.setVisibility(View.GONE);
            viewActivos.setVisibility(View.VISIBLE);
            btnResumen.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnActivos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5722")));
            btnResumen.setTextColor(Color.BLACK);
            btnActivos.setTextColor(Color.WHITE);
            
            // Reset status filter when manually switching to list view
            currentStatusFilter = "Todos";
            applyFilters();
        });
    }
    
    private void loadResumenData() {
        if (currentIdSubToma == null) return;

        // 1. Cargar datos del Gauge desde la Sub-Toma seleccionada
        java.util.List<com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity> subTomas = subTomasDao.getByTomaFisicaId(idTake);
        com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity currentToma = null;
        
        for (com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity toma : subTomas) {
            if (toma.getIdToma().equals(currentIdSubToma)) {
                currentToma = toma;
                break;
            }
        }
        
        if (currentToma != null && txtGaugeCount != null) {
            int total = 0;
            int leidos = 0;
            try {
                total = Integer.parseInt(currentToma.getTotalActivos());
                leidos = Integer.parseInt(currentToma.getActivosLeidos());
            } catch (NumberFormatException e) {
                Log.e("Lectura_Inventario", "Error parsing numbers", e);
            }
            
            txtGaugeCount.setText(String.valueOf(leidos));
            txtGaugeTotal.setText("De " + total);
            gaugeProgress.setMax(total);
            gaugeProgress.setProgress(leidos);
        }
    }

    private void loadFilters() {
        Log.d("Lectura_Inventario", "loadFilters: Iniciando carga de filtros...");
        // Cargar Ubicación A (Compania)
        try {
            RazonSocialDBHelper razonHelper = new RazonSocialDBHelper(this);
            _mapRazonSociales = razonHelper.ObtenerRazon();
            Log.d("Lectura_Inventario", "loadFilters: Razones sociales cargadas: " + (_mapRazonSociales != null ? _mapRazonSociales.size() : "null"));
            
            List<RazonNuevo> razonList = new ArrayList<>();
            razonList.add(new RazonNuevo("-1", "Todas"));
            if (_mapRazonSociales != null) {
                razonList.addAll(_mapRazonSociales.values());
            }
            
            ArrayAdapter<RazonNuevo> adapterA = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, razonList);
            adapterA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUbicacionA.setAdapter(adapterA);
            spinnerUbicacionA.setOnItemSelectedListener(onItemSpinnerListenerCompania);

            // Inicializar los demás con "Todas"
            initSpinnerWithDefault(spinnerUbicacionB);
            initSpinnerWithDefault(spinnerUbicacionC);
            initSpinnerWithDefault(spinnerUbicacionD);
            loadUbicacionSecundaria();
        } catch (Exception e) {
            Log.e("Lectura_Inventario", "Error en loadFilters", e);
        }
    }
    
    private void initSpinnerWithDefault(android.widget.Spinner spinner) {
        if (spinner == null) {
            Log.e("Lectura_Inventario", "initSpinnerWithDefault: Spinner is null");
            return;
        }
        List<String> list = new ArrayList<>();
        list.add("Todas");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(null);
    }

    private void loadEdificios(String idCompania) {
        EdificioDBHelper edificioHelper = new EdificioDBHelper(this);
        _mapEdificios = edificioHelper.ObtenerEdificio(idCompania);
        
        List<EdificioNuevo> edificioList = new ArrayList<>();
        edificioList.add(new EdificioNuevo("-1", "Todas", idCompania));
        if (_mapEdificios != null) {
            edificioList.addAll(_mapEdificios.values());
        }
        
        ArrayAdapter<EdificioNuevo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, edificioList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUbicacionB.setAdapter(adapter);
        spinnerUbicacionB.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        
        // Resetear hijos
        initSpinnerWithDefault(spinnerUbicacionC);
        initSpinnerWithDefault(spinnerUbicacionD);
        
        // Resetear flags
        _itemSelectedUserEdificio = false;
    }

    private void loadPisos(String idEdificio) {
        PisoDBHelper pisoHelper = new PisoDBHelper(this);
        _mapPisos = pisoHelper.ObtenerPiso(idEdificio);
        
        List<PisoNuevo> pisoList = new ArrayList<>();
        pisoList.add(new PisoNuevo("-1", "Todas", idEdificio));
        if (_mapPisos != null) {
            pisoList.addAll(_mapPisos.values());
        }
        
        ArrayAdapter<PisoNuevo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pisoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUbicacionC.setAdapter(adapter);
        spinnerUbicacionC.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        
        // Resetear hijos
        initSpinnerWithDefault(spinnerUbicacionD);
        
        // Resetear flags
        _itemSelectedUserPiso = false;
    }

    private void loadOficinas(String idPiso) {
        OficinaDBHelper oficinaHelper = new OficinaDBHelper(this);
        _mapOficinas = oficinaHelper.ObtenerOficina(idPiso);
        
        List<oficinaNuevo> oficinaList = new ArrayList<>();
        oficinaList.add(new oficinaNuevo("-1", "Todas", idPiso));
        if (_mapOficinas != null) {
            oficinaList.addAll(_mapOficinas.values());
        }
        
        ArrayAdapter<oficinaNuevo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, oficinaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUbicacionD.setAdapter(adapter);
        spinnerUbicacionD.setOnItemSelectedListener(onItemSpinnerListenerOficina);
    }

    private void loadUbicacionSecundaria() {
        String idCompania = getSelectedId(spinnerUbicacionA);
        String idEdificio = getSelectedId(spinnerUbicacionB);
        String idPiso = getSelectedId(spinnerUbicacionC);
        String idOficina = getSelectedId(spinnerUbicacionD);

        ArrayList<String> ubicaciones = AssetsDBHelper.ObtenerUbicacionesSecundarias(idCompania, idEdificio, idPiso, idOficina);
        List<String> list = new ArrayList<>();
        list.add("Todas");
        if (ubicaciones != null) {
            list.addAll(ubicaciones);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUbicacionSecundaria.setAdapter(adapter);
        spinnerUbicacionSecundaria.setOnItemSelectedListener(onItemSpinnerListenerUbicacionSecundaria);
    }

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerCompania = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserCompania) {
                RazonNuevo selected = (RazonNuevo) parent.getItemAtPosition(position);
                if (selected.getIdRazon().equals("-1")) {
                    initSpinnerWithDefault(spinnerUbicacionB);
                    initSpinnerWithDefault(spinnerUbicacionC);
                    initSpinnerWithDefault(spinnerUbicacionD);
                } else {
                    loadEdificios(selected.getIdRazon());
                }
                loadUbicacionSecundaria();
                applyFilters();
            }
            _itemSelectedUserCompania = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerEdificio = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserEdificio) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof EdificioNuevo) {
                    EdificioNuevo selected = (EdificioNuevo) item;
                    if (selected.getIdEdificio().equals("-1")) {
                        initSpinnerWithDefault(spinnerUbicacionC);
                        initSpinnerWithDefault(spinnerUbicacionD);
                    } else {
                        loadPisos(selected.getIdEdificio());
                    }
                }
                loadUbicacionSecundaria();
                applyFilters();
            }
            _itemSelectedUserEdificio = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerPiso = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserPiso) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof PisoNuevo) {
                    PisoNuevo selected = (PisoNuevo) item;
                    if (selected.getIdPiso().equals("-1")) {
                        initSpinnerWithDefault(spinnerUbicacionD);
                    } else {
                        loadOficinas(selected.getIdPiso());
                    }
                }
                loadUbicacionSecundaria();
                applyFilters();
            }
            _itemSelectedUserPiso = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerOficina = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
             loadUbicacionSecundaria();
             applyFilters();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerUbicacionSecundaria = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            applyFilters();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void switchToActivosView() {
        viewResumen.setVisibility(View.GONE);
        viewActivos.setVisibility(View.VISIBLE);
        btnResumen.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        btnActivos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5722")));
        btnResumen.setTextColor(Color.BLACK);
        btnActivos.setTextColor(Color.WHITE);
    }

    private void applyFilters() {
        inventarioVisuals.clear();
        int countEncontrados = 0;
        int countFaltantes = 0;
        int countSobrantes = 0;

        String idCompania = getSelectedId(spinnerUbicacionA);
        String idEdificio = getSelectedId(spinnerUbicacionB);
        String idPiso = getSelectedId(spinnerUbicacionC);
        String idOficina = getSelectedId(spinnerUbicacionD);
        String ubicacionSecundaria = getSelectedId(spinnerUbicacionSecundaria);

        for (InventarioVisual item : masterInventarioVisuals) {
            if (matchesFilter(item, idCompania, idEdificio, idPiso, idOficina, ubicacionSecundaria)) {
                // Count items by status for the current location
                if ("Encontrado".equals(item.getStatus())) {
                    countEncontrados++;
                } else if ("Faltante".equals(item.getStatus())) {
                    countFaltantes++;
                } else if ("No Pertenece".equals(item.getStatus())) {
                    countSobrantes++;
                }

                // Apply Status Filter
                if (currentStatusFilter.equals("Todos") || 
                   (item.getStatus() != null && item.getStatus().equals(currentStatusFilter))) {
                    inventarioVisuals.add(item);
                }
            }
        }

        itemAdapterAssets.notifyDataSetChanged();
        
        // Update Button Texts
        if (btnVerEncontrados != null) btnVerEncontrados.setText("Encontrados\n" + countEncontrados);
        if (btnVerFaltantes != null) btnVerFaltantes.setText("Faltantes\n" + countFaltantes);
        if (btnVerSobrantes != null) btnVerSobrantes.setText("Sobrantes\n" + countSobrantes);
        
        updateCounters();
    }

    private String getSelectedId(android.widget.Spinner spinner) {
        if (spinner == null || spinner.getSelectedItem() == null) return "-1";
        Object item = spinner.getSelectedItem();
        if (item instanceof RazonNuevo) return ((RazonNuevo) item).getIdRazon();
        if (item instanceof EdificioNuevo) return ((EdificioNuevo) item).getIdEdificio();
        if (item instanceof PisoNuevo) return ((PisoNuevo) item).getIdPiso();
        if (item instanceof oficinaNuevo) return ((oficinaNuevo) item).getIdOficina();
        if (item instanceof String) {
             String s = (String) item;
             if ("Todas".equals(s)) return "-1";
             return s;
        }
        return "-1";
    }

    private boolean matchesFilter(InventarioVisual item, String idCompania, String idEdificio, String idPiso, String idOficina, String ubicacionSecundaria) {
        if (!idCompania.equals("-1") && (item.getIdCompania() == null || !item.getIdCompania().equals(idCompania))) return false;
        if (!idEdificio.equals("-1") && (item.getIdEdificio() == null || !item.getIdEdificio().equals(idEdificio))) return false;
        if (!idPiso.equals("-1") && (item.getIdPiso() == null || !item.getIdPiso().equals(idPiso))) return false;
        if (!idOficina.equals("-1") && (item.getIdOficina() == null || !item.getIdOficina().equals(idOficina))) return false;
        if (!ubicacionSecundaria.equals("-1") && (item.getUbicacionSecundaria() == null || !item.getUbicacionSecundaria().equals(ubicacionSecundaria))) return false;
        return true;
    }

    private void updateCounters() {
        contadorActivosFaltantes = 0;
        contActivosEncontrados = 0;
        contadorActivosSobrantes = 0;

        for (InventarioVisual item : inventarioVisuals) {
            if ("Faltante".equals(item.getStatus())) contadorActivosFaltantes++;
            else if ("Encontrado".equals(item.getStatus())) contActivosEncontrados++;
            else if ("No Pertenece".equals(item.getStatus()) || "Sin Asignar".equals(item.getStatus()) || "No Inventariado".equals(item.getStatus())) contadorActivosSobrantes++;
        }

        txtFaltantes.setText(String.valueOf(contadorActivosFaltantes));
        txtEncontrados.setText(String.valueOf(contActivosEncontrados));
        txtSobrantes.setText(String.valueOf(contadorActivosSobrantes));
    }


    private void inicializarRFID() {
        rfidHandler = TagWriter.getInstance();
        if (rfidHandler != null && !rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this); // usa GetContext() internamente
        }
        rfidHandler.setResponseHandler(this);
        rfidHandler.onResume();
    }

    //*************************************************************************************************************

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            try {
                rfidHandler.onPause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionActivate != null) {
            sessionActivate.cancel();
        }
        if (rfidHandler != null) {
            rfidHandler.onDestroy();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }

    //*************************************************************************************************************

    public void controles() {

        LecturaInventarioView = findViewById(R.id.LecturaInventario);
        txtEncontrados = findViewById(R.id.txtEncontrados);
        txtFaltantes = findViewById(R.id.txtFaltantes);
        txtSobrantes = findViewById(R.id.txtSobrantes);
        txtLeidas = findViewById(R.id.txtLeidos);
        txtUbicacion = findViewById(R.id.txtUbicacion);
        GuardarRessultado = findViewById(R.id.btn_GuardarToma);
        txtBarcode = findViewById(R.id.txt_barcode);
        etManualInput = findViewById(R.id.etManualInput);
        btnManualAdd = findViewById(R.id.btnManualAdd);
        inventoryDBHelper = new InventoryDBHelper(LecturaInventarioView.getContext());
        AssetsDBHelper = new AssetsDBHelper(_context);
        OfficesDBHelper = new OfficesDBHelper(_context);
        ListaLectura = findViewById(R.id.listView);
        itemAdapterAssets = new AdaptadorLecturas(inventarioVisuals);
        rlsnackbar = findViewById(R.id.clLecturaInventario);
        OnRfid = findViewById(R.id.swRfid);
    }


    //*************************************************************************************************************

    public void eventos() {
        if (btnAgregarManual != null) {
            btnAgregarManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoSimulacion();
                }
            });
        }
        
        if (btnManualAdd != null) {
            btnManualAdd.setOnClickListener(v -> {
                String input = etManualInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    AgregarActivoManual(input);
                    etManualInput.setText("");
                } else {
                    Toast.makeText(_context, "Ingrese Placa o EPC", Toast.LENGTH_SHORT).show();
                }
            });
        }

        GuardarRessultado.setOnClickListener(OnClickListenerGuardarResultado);
        
        // New Bottom Nav Listeners
        if (btnSubir != null) {
            btnSubir.setOnClickListener(v -> {
                if (GuardarRessultado != null) GuardarRessultado.performClick();
            });
        }

        if (btnPotencia != null) {
            btnPotencia.setOnClickListener(v -> {
                Intent intent = new Intent(Lectura_Inventario.this, com.example.diverscan.activeid.GeneralTag.ConfiguracionAntena.class);
                startActivity(intent);
            });
        }

        if (btnIniciar != null) {
            btnIniciar.setOnClickListener(v -> {
                if (OnRfid != null) {
                    OnRfid.setChecked(!OnRfid.isChecked());
                }
            });
        }

        txtFaltantes.setText(String.valueOf(contadorActivosFaltantes));
        txtSobrantes.setText(String.valueOf(contadorActivosSobrantes));
        txtEncontrados.setText(String.valueOf(contActivosEncontrados));
        OnRfid.setOnCheckedChangeListener(AccionRFID);

        txtBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (txtBarcode.getText().length() > 5) {
                    AgregarActivoPlaca(txtBarcode.getText().toString());
                    txtBarcode.setText("");
                    txtBarcode.requestFocus();
                }
            }
        });
    }

    private void updateIniciarButton(boolean isRunning) {
        if (txtIniciar != null && iconIniciar != null) {
            if (isRunning) {
                txtIniciar.setText("Detener");
                iconIniciar.setImageResource(R.drawable.ic_nfc); // Or stop icon
            } else {
                txtIniciar.setText("Iniciar");
                iconIniciar.setImageResource(R.drawable.ic_nfc);
            }
        }
    }

    private void AgregarActivoPlaca(String placa) {
        // Validar si el activo existe en la base de datos local (por Placa o EPC)
        ActivoEntity activo = activoDao.getActivoById(placa); // Busca por NUMERO_ACTIVO
        if (activo == null) {
            activo = activoDao.getActivoByEpc(placa); // Busca por EPC
        }

        if (activo != null) {
            _chequearInventario.AgregarActivosBarcode(placa, this);
        } else {
            // Notificar al usuario y reproducir sonido de error
            MostrarSnackBar(false, "El activo " + placa + " no existe en la base de datos local.");
            try {
                ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void mostrarDialogoSimulacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Simulación de Lecturas");
        String[] options = {"Simular Faltante (Random)", "Simular Sobrante (Random)", "Ingresar Manualmente"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Simular Faltante
                        simularFaltanteRandom();
                        break;
                    case 1: // Simular Sobrante
                        simularSobranteRandom();
                        break;
                    case 2: // Manual
                        mostrarDialogoManual();
                        break;
                }
            }
        });
        builder.show();
    }

    private void simularFaltanteRandom() {
        // Opción A: Usar masterInventarioVisuals si contiene los faltantes
        List<InventarioVisual> faltantes = new ArrayList<>();
        if (masterInventarioVisuals != null) {
            for (InventarioVisual item : masterInventarioVisuals) {
                if ("Faltante".equals(item.getStatus())) {
                    faltantes.add(item);
                }
            }
        }
        
        // Opción B: Si masterInventarioVisuals está vacío (inicio), usar activoInventarios
        if (faltantes.isEmpty() && activoInventarios != null) {
             // Necesitamos saber cuáles ya fueron encontrados para no repetirlos
             // inventarioVisuals tiene los encontrados? No, tiene lo visual.
             // ChequearInventario tiene _activosEncontrado (privado).
             // Pero masterInventarioVisuals debería tener el estado actual.
             // Si está vacío, quizás no se cargó aún.
             // Intentemos usar activoInventarios.
             for (TomasFisias tf : activoInventarios) {
                 boolean yaEncontrado = false;
                 if (masterInventarioVisuals != null) {
                     for (InventarioVisual iv : masterInventarioVisuals) {
                         if (iv.getEPC().equals(tf.getEPC()) && "Encontrado".equals(iv.getStatus())) {
                             yaEncontrado = true;
                             break;
                         }
                     }
                 }
                 if (!yaEncontrado) {
                     // Convertir TomasFisias a un candidato simple (usamos EPC)
                     InventarioVisual iv = new InventarioVisual();
                     iv.setEPC(tf.getEPC());
                     iv.setDescripcion(tf.getDescripcion());
                     faltantes.add(iv);
                 }
             }
        }

        if (faltantes.isEmpty()) {
            Toast.makeText(this, "No hay activos faltantes disponibles para simular.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pick random
        int randomIndex = new java.util.Random().nextInt(faltantes.size());
        InventarioVisual selected = faltantes.get(randomIndex);
        AgregarActivoManual(selected.getEPC());
        Toast.makeText(this, "Simulado: " + selected.getDescripcion(), Toast.LENGTH_SHORT).show();
    }

    private void simularSobranteRandom() {
        // Buscar activos en la BD que NO estén en la lista esperada
        // Esto es un poco pesado, así que limitaremos la búsqueda
        List<ActivoEntity> todos = activoDao.getAllLocalActivos(); // Asegurar que este método existe o usar query raw
        // Si getAllLocalActivos no existe, usaremos una lista vacía y fallback
        if (todos == null) todos = new ArrayList<>();
        
        // Filtrar los que ya están en inventario (esperados o encontrados)
        java.util.HashSet<String> epcExcluidos = new java.util.HashSet<>();
        if (activoInventarios != null) {
            for (TomasFisias tf : activoInventarios) epcExcluidos.add(tf.getEPC());
        }
        if (masterInventarioVisuals != null) {
            for (InventarioVisual iv : masterInventarioVisuals) epcExcluidos.add(iv.getEPC());
        }

        List<ActivoEntity> candidatos = new ArrayList<>();
        for (ActivoEntity a : todos) {
            // Usar getEpc() en lugar de getTagEpc() ya que TAG_EPC es un flag
            String epcReal = a.getEpc();
            if (epcReal != null && !epcExcluidos.contains(epcReal)) {
                candidatos.add(a);
            }
        }

        String epcSimulado;
        if (!candidatos.isEmpty()) {
            int randomIndex = new java.util.Random().nextInt(candidatos.size());
            epcSimulado = candidatos.get(randomIndex).getEpc();
        } else {
            // Generar uno totalmente ficticio
            epcSimulado = "E200" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
            Toast.makeText(this, "Generando Sobrante Ficticio (No en BD)", Toast.LENGTH_SHORT).show();
        }
        
        AgregarActivoManual(epcSimulado);
    }

    private void mostrarDialogoManual() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Activo Manualmente");
        builder.setMessage("Ingrese EPC o Placa del activo:");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String texto = input.getText().toString().trim();
                if (!texto.isEmpty()) {
                    AgregarActivoManual(texto);
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void AgregarActivoManual(String input) {
        // Intentar buscar por EPC primero
        ActivoEntity activo = activoDao.getActivoByEpc(input);
        
        // Si no existe y es un sobrante ficticio, igual intentamos agregarlo
        // ChequearInventario maneja la lógica de validación
        
        if (activo != null) {
            _chequearInventario.AgregarActivos(input, this);
             // Feedback visual
            return;
        }
        
        // Intentar buscar por Placa
        activo = activoDao.getActivoById(input);
        if (activo != null) {
            _chequearInventario.AgregarActivosBarcode(input, this);
            return;
        }

        // Si no existe en BD, pero queremos simular sobrante ficticio:
        // ChequearInventario probablemente rechace si no encuentra el activo en BD local (depende de su implementación)
        // Pero intentemos pasarlo como EPC
        _chequearInventario.AgregarActivos(input, this);
        // Si ChequearInventario no lo agrega, mostrar error
        // Pero no tenemos callback de error fácil aquí, asumimos que si no está en BD, ChequearInventario lo ignorará o lo marcará desconocido.
    }
    //*************************************************************************************************************

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //*************************************************************************************************************
    Switch.OnCheckedChangeListener AccionRFID = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
            updateIniciarButton(isChecked);
            if (isChecked) {
                MostrarProgressDialog("Encendiendo RFID");
            } else {
                MostrarProgressDialog("Apagando RFID");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        if (isChecked) {
                            iniciarRFID();
                        } else {
                            desconectarRFID();
                            txtBarcode.requestFocus();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    };

    private void MostrarProgressDialog(String mensaje) {
        dialog = new ProgressDialog(_context);
        dialog.setMessage(mensaje);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void iniciarRFID() {
        try {
            scannerActivate = false;
            if(!rfidHandler.setTriggerMode("RFID")){
                MostrarSnackBar(false, "No se ha podido iniciar el RFID");
            }
            dialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
            dialog.dismiss();
        }
    }

    private void desconectarRFID() {
        try {
            scannerActivate = true;
            if(!rfidHandler.setTriggerMode("BARCODE")){
                MostrarSnackBar(false, "No se ha podido apagar el RFID");
            }
            dialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
            dialog.dismiss();
        }
    }

    //*************************************************************************************************************

    public void RecibirTakesInfo() {
        try {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Log.e("Lectura_Inventario", "No extras provided");
                Toast.makeText(this, "Error: Datos de inventario no disponibles", Toast.LENGTH_LONG).show();
                return;
            }

            idTake = extras.getString("IdTake");
            takeName = extras.getString("takeName");
            takeDescription = extras.getString("takeDescription");
            takeDate = extras.getString("takeDate");
            idOficina = extras.getString("idOficina");
            idTypeInventory = extras.getString("tipoInventario");
            
            // Si idOficina es nulo o vacío (caso de sub-toma sin filtro previo), 
            // inicializamos activoInventarios vacío y NO llamamos a la DB de ubicaciones.
            // Esto cumple con el requisito de "no hacer peticiones" innecesarias.
            if (idOficina != null && !idOficina.isEmpty()) {
                // USAR ActivoDao para obtener activos sincronizados en ActivosApi
                // activoInventarios = OfficesDBHelper.ActivosUbicacion(idOficina);
                
                activoInventarios = new ArrayList<>();
                List<ActivoEntity> lista = activoDao.getActivosByUbicacion(idOficina);
                for (ActivoEntity a : lista) {
                    TomasFisias tf = new TomasFisias();
                    tf.setNumero(a.getNumeroActivo());
                    tf.setDescripcion(a.getDescripcionCorta());
                    tf.setEPC(a.getTagEpc());
                    tf.setAssetSysId(a.getIdActivo());
                    // Nota: a.getUbicacionD() devuelve el ID, no el nombre. 
                    // Si se requiere nombre, se necesitaria un lookup adicional o join.
                    // Por ahora usamos el ID para que funcione la logica.
                    tf.setOficina(a.getUbicacionD()); 
                    tf.setIdOficina(a.getUbicacionD());
                    tf.setIdPiso(a.getUbicacionC());
                    tf.setIdEdificio(a.getUbicacionB());
                    tf.setIdCompania(a.getUbicacionA());
                    tf.setUbicacionSecundaria(a.getUbicacionSecundaria());
                    tf.setStatus(""); // Inicialmente vacio o segun logica
                    
                    activoInventarios.add(tf);
                }
            } else {
                activoInventarios = new ArrayList<>();
            }

            _chequearInventario = new ChequearInventario(activoInventarios, idTake, this, this);
        } catch (Exception e) {
            Log.e("Lectura_Inventario", "Error in RecibirTakesInfo", e);
            Toast.makeText(this, "Error inicializando datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //*************************************************************************************************************
    private boolean guardadoOk = false;
    /*Comienza lectura de inventario*/
    private void GuardarResultados() {
        try {
            if (inventarioVisuals == null || inventarioVisuals.isEmpty()) {
                MostrarSnackBar(false, "No hay lecturas para guardar.");
                return;
            }

            // Limpia contenedores para evitar duplicados si el usuario guarda varias veces
            _detalleInventario.clear();
            _inventario.clear();

            // IDs coherentes
            String idInventory = UUID.randomUUID().toString();      // = uniqueID (cabecera Inventario)
            String idTomasDelInventario = UUID.randomUUID().toString();

            // Usuario actual
            String idUsuario = SharedPreferencesGetSet.leer_local("_userId", getApplicationContext());

            // Fecha en ISO (recomendado para el WCF)
            String fechaIso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    .format(new java.util.Date());

            // Cabecera: TomasDelInventario
            EntidadTomasInventario entidadTomasInventario =
                    new EntidadTomasInventario(
                            idTomasDelInventario,
                            fechaIso,
                            idOficina,
                            idUsuario,
                            idTypeInventory
                    );

            // DetalleInventario (uno por cada item visual leído)
            for (InventarioVisual item : inventarioVisuals) {
                _detalleInventario.add(
                        new EntidadDetalleInventario(
                                UUID.randomUUID().toString(), // IdDetalleInventario
                                idInventory,                   // FK_idInventory (apunta a la cabecera Inventario)
                                item.getNumero(),
                                item.getDescripcion(),
                                item.getEPC(),
                                item.getStatus(),
                                "0"                            // Excluido
                        )
                );
            }

            // Totales
            int activosLeidos     = contadorActivosSobrantes + contActivosEncontrados;
            int activosUbicacion  = contActivosEncontrados + contadorActivosFaltantes;

            // Cabecera Inventario
            _inventario.add(
                    new EntidadInventario(
                            idInventory,               // idInventory
                            idTomasDelInventario,      // IdTomaInventario (relación con TomasDelInventario)
                            "2",                       // Numero (como en tu código original)
                            String.valueOf(activosLeidos),
                            String.valueOf(activosUbicacion),
                            String.valueOf(contActivosEncontrados),
                            String.valueOf(contadorActivosFaltantes),
                            String.valueOf(contadorActivosSobrantes),
                            fechaIso                   // Fecha
                    )
            );

            // -------------------------------------------------------------------------------------
            // NUEVO: Guardar en tablas compatibles con la sincronización (Push)
            // -------------------------------------------------------------------------------------
            if (currentIdSubToma != null) {
                // 1. Actualizar Resumen (Subtoma)
                TomaFisicaTomasEntity subtoma = subTomasDao.getByIdToma(currentIdSubToma);
                if (subtoma != null) {
                    subtoma.setActivosLeidos(String.valueOf(contActivosEncontrados + contadorActivosSobrantes));
                    subtoma.setFaltantes(String.valueOf(contadorActivosFaltantes));
                    subtoma.setSobrantes(String.valueOf(contadorActivosSobrantes));
                    // Guardar localmente
                    subTomasDao.saveLocal(java.util.Collections.singletonList(subtoma));
                }

                // 2. Guardar Detalles
                ArrayList<TomaFisicaDetallesEntity> nuevosDetalles = new ArrayList<>();
                for (InventarioVisual item : inventarioVisuals) {
                    TomaFisicaDetallesEntity det = new TomaFisicaDetallesEntity();
                    // ID único para el detalle
                    det.setIdTakeDetail(UUID.randomUUID().toString());
                    det.setIdToma(currentIdSubToma);
                    det.setNumeroToma(subtoma != null ? subtoma.getNumeroToma() : "0"); 
                    det.setFechaToma(fechaIso);
                    det.setEpc(item.getEPC());
                    det.setDateRead(fechaIso);
                    
                    // Mapeo de estado
                    String estado = item.getStatus(); // "Encontrado", "Faltante", "Sobrante"
                    if ("Encontrado".equalsIgnoreCase(estado)) det.setEstadoInventario("VERDE");
                    else if ("Faltante".equalsIgnoreCase(estado)) det.setEstadoInventario("ROJO");
                    else if ("Sobrante".equalsIgnoreCase(estado) || "No Pertenece".equalsIgnoreCase(estado)) det.setEstadoInventario("AMARILLO");
                    else det.setEstadoInventario(estado);

                    // IDs de activos si los tenemos (item.getAssetSysId() a veces viene, a veces no)
                    det.setActivoId(item.getAssetSysId()); 
                    
                    nuevosDetalles.add(det);
                }
                
                if (!nuevosDetalles.isEmpty()) {
                    detallesDao.saveLocal(nuevosDetalles);
                }
            }
            // -------------------------------------------------------------------------------------

            // Persistencia cada metodo pone Sync=1)
            boolean ok1 = inventoryDBHelper.InsertOrReplaceTomaInventario(entidadTomasInventario);
            boolean ok2 = inventoryDBHelper.InsertOrReplaceDetalleInventario(_detalleInventario);
            boolean ok3 = inventoryDBHelper.InsertOrReplaceInventario(_inventario);

            if (!ok1 || !ok2 || !ok3) {
                MostrarSnackBar(false, "No se han podido guardar los resultados del inventario, intente nuevamente.");
                return;
            }

            guardadoOk = true; // ← para que tu onBackPressed sepa que ya quedó guardado
            MostrarSnackBar(true, "Resultados guardados. Puedes sincronizar ahora.");
        } catch (Exception ex) {
            ex.printStackTrace();
            MostrarSnackBar(false, "Error guardando: " + ex.getMessage());
        }
    }



    //*************************************************************************************************************
    private Button.OnClickListener OnClickListenerGuardarResultado = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GuardarResultados();
        }
    };

    //*************************************************************************************************************
    //region Lecturas
    private void CargaInicialRecyclerView(InventarioVisual response) {
        masterInventarioVisuals.add(response);
        inventarioVisuals.add(response);
        ListaLectura.setLayoutManager(new LinearLayoutManager(_context));
        ListaLectura.setAdapter(itemAdapterAssets);
        contadorActivosFaltantes++;
        txtFaltantes.setText(String.valueOf(contadorActivosFaltantes));
    }


    public void fillRecyclerView(InventarioVisual response) {
        try {
            boolean foundInMaster = false;
            for (int i = 0; i < masterInventarioVisuals.size(); i++) {
                if (masterInventarioVisuals.get(i).getAssetSysId().equals(response.getAssetSysId())) {
                    masterInventarioVisuals.set(i, response);
                    foundInMaster = true;
                    break;
                }
            }
            if (!foundInMaster) {
                masterInventarioVisuals.add(response);
            }

            applyFilters();

        } catch (final Exception ex) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    //endregion

    //*************************************************************************************************************

    /*Terminan metodos de lectura*/
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    /*Acción de lectura de tags con la HH*/
    public void Message() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }

    //*************************************************************************************************************

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (rfidHandler != null) {
            String status = rfidHandler.onResume();
            // Toast.makeText(_context, status, Toast.LENGTH_LONG).show();
        }
    }

    //*************************************************************************************************************

    //*************************************************************************************************************

    @Override
    public void handleTagdata(TagData[] tagData) {
        if (!scannerActivate) {
            if (_chequearInventario.CheckTagsInventario(tagData, this)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message();
                    }
                });
            }
        }
    }

    /*public void ProbarEPCManual(String epc) {
        ArrayList<String> tagsSimulados = new ArrayList<>();
        tagsSimulados.add(epc);

        boolean encontrado = _chequearInventario.CheckTagsInventario(tagsSimulados, this);
        if (encontrado) {
            runOnUiThread(() -> Message()); // beep si fue válido
        } else {
            runOnUiThread(() -> Toast.makeText(this, "No se encontró el EPC", Toast.LENGTH_SHORT).show());
        }
    }*/

    //*************************************************************************************************************

    @Override
    public void handleTriggerPress(boolean pressed) {
        try{
            triggerPressed = pressed;
            if (pressed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                rfidHandler.performInventory();
            } else{
                rfidHandler.stopInventory();
                //ProbarEPCManual("800474453240000000031607");
                //handler.postDelayed(r, 1000);
            }
        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            Toast.makeText(_context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //*************************************************************************************************************

    @Override
    public Context GetContext() {
        return this;
    }

    //*************************************************************************************************************

    @Override
    public void SetMessage(String Text) {
        final String text = Text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_context, text, Toast.LENGTH_LONG).show();

            }
        });
    }

    //*************************************************************************************************************

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Message();
            AlertasError("ATENCION", "Si continua podría perder su progreso."+ "\n"+ "¿Realmente desea salir?");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //*************************************************************************************************************

    @Override
    public boolean onSupportNavigateUp() {
        Message();
        AlertasError("ATENCION", "Si continua podría perder su progreso."+ "\n"+ "¿Realmente desea salir?");
        return false;
    }

    //*************************************************************************************************************

    public boolean AlertasError(String titulo, String Mensaje){
        final boolean respuesta = false;
        LayoutInflater inflater = _activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones_error, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(titulo);
        txvMessage.setText(Mensaje);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(_activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Cancelar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();
        return  true;
    }

    //region Recepcion Activos
    @Override
    public void RetornarActivo(final InventarioVisual eInventarioVisual) {

        if(eInventarioVisual == null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillRecyclerView(eInventarioVisual);
            }
        });
    }

    @Override
    public void RetornarCargaInicial(final InventarioVisual eInventarioVisual) {
        if(eInventarioVisual == null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CargaInicialRecyclerView(eInventarioVisual);
            }
        });
    }

    private void MostrarSnackBar(boolean tipoSnack, String mensaje) {
        if (tipoSnack) {
            _snackbar = Snackbar.make(rlsnackbar, mensaje, 3000);
            _snackbar.setActionTextColor(Color.rgb(179, 179, 179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(4, 165, 77));
        } else {
            _snackbar = Snackbar.make(rlsnackbar, mensaje, 4000);
            _snackbar.setActionTextColor(Color.rgb(179, 179, 179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(242, 59, 59));
        }
        _snackbar.show();
    }
    //endregion

    //*************************************************************************************************************

}
