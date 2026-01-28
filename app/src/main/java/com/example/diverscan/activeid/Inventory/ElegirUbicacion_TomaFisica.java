package com.example.diverscan.activeid.Inventory;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.diverscan.activeid.Inventory.EUbicacionActivo;
import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioNuevo;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Piso.PisoNuevo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RazonSocial.RazonNuevo;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;
import com.example.diverscan.activeid.Utilities.AlertasPersonalizadas;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.UI.tomasfisicas.TomaFisicaTomasAdapter;
import com.example.diverscan.activeid.UI.tomasfisicas.RegistroConteosActivity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.diverscan.activeid.data.local.dao.UbicacionDao;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import java.util.HashSet;
import java.util.Set;

public class ElegirUbicacion_TomaFisica extends AppCompatActivity implements ResponseHandlerInterface {

    private View ElegirUbicacionView;
    InventoryDBHelper InventoryDBHelper;
    // OfficesDBHelper OfficesDBHelper; // Deprecated
    List<String> listSpinner;
    Context _context;
    Activity _activity;
    EUbicacionActivo eUbicacionActivo = new EUbicacionActivo();
    private Button btn_continuar;
    private Spinner CompaniaView, EdificioView, PisoView, OficinaView, TipoInventarioView;

    // Maps now store selection logic but are filled from UbicacionEntity
    private Map<String, RazonNuevo> _mapRazonSociales = new HashMap<>();
    private Map<String, EdificioNuevo> _mapEdificios = new HashMap<>();
    private Map<String, PisoNuevo> _mapPisos = new HashMap<>();
    private Map<String, oficinaNuevo> _mapOficinas = new HashMap<>();
    
    private Map<Integer, EntidadTiposInventarios> _mapTipoInventarios = new HashMap<Integer, EntidadTiposInventarios>();

    private boolean _itemSelectedUserCompania, _itemSelectedUserEdificio, _itemSelectedUserPiso = true;
    EditText txtAjusteOficina;
    public String idTake, takeName, takeDescription, takeDate, idOficina, idedificioActivo, idpisoActivo, idCompania;
    String idTipoInventario = "";
    ConstraintLayout clsnackbar;
    Snackbar _snackbar;
    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false;

    private RecyclerView recyclerSubTomas;
    private TomaFisicaTomasAdapter adapterSubTomas;
    private TomaFisicaTomasDao subTomasDao;
    private android.widget.TextView lblTituloDinamico;
    private android.widget.TextView txtProgressCircle;
    
    // New DAO access
    private UbicacionDao ubicacionDao;
    private ActivoDao activoDao;
    private List<UbicacionEntity> allUbicaciones;

    private static final String TAG = "ElegirUbicacion";

    public ElegirUbicacion_TomaFisica() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_ubicacion__toma_fisica);
        
        // Inicializar LoginDBHelper para asegurar creación de tablas
        try {
            com.example.diverscan.activeid.sqlite.LoginDBHelper loginDBHelper = new com.example.diverscan.activeid.sqlite.LoginDBHelper(this);
            loginDBHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing LoginDBHelper", e);
        }

        _context = this;
        _activity = this;
        
        // Initialize new DAO
        ubicacionDao = new UbicacionDao(this);
        activoDao = new ActivoDao(this);
        allUbicaciones = ubicacionDao.getAllUbicaciones();
        
        if (allUbicaciones.isEmpty()) {
            AlertasPersonalizadas.showAlertDialog(_activity, "Datos faltantes", 
                "No se encontraron ubicaciones cargadas. Por favor vaya a 'Sincronizar' y presione 'Obtener Datos' para actualizar la información.");
        }
        
        controles();
        eventos();
        cargarTiposInventarios();
        // Cargar ubicaciones desde nueva estructura
        // cargarRazonesSocialesDesdeEntity(); // COMENTADO: Logica HH desactivada
        
        RecibirTakesInfo();
        configurarSubTomas();
        
        try {
            rfidHandler = TagWriter.getInstance();
            rfidHandler.onCreate(this);
        } catch (SecurityException e) {
            Log.e(TAG, "Error initializing RFID Handler: Permission missing", e);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RFID Handler", e);
        }
    }

    public void controles() {
        ElegirUbicacionView = findViewById(R.id.elegirubicacion);
        InventoryDBHelper = new InventoryDBHelper(ElegirUbicacionView.getContext());
        btn_continuar = findViewById(R.id.btn_continua);

        CompaniaView = findViewById(R.id.compania_toma);
        EdificioView = findViewById(R.id.edificio_toma);
        PisoView = findViewById(R.id.piso_toma);
        OficinaView = findViewById(R.id.oficina_toma);
        TipoInventarioView = findViewById(R.id.sp_tipoInventario);

        // OfficesDBHelper = new OfficesDBHelper(ElegirUbicacionView.getContext()); // Deprecated
        txtAjusteOficina = findViewById(R.id.txtSectorBusquedaAS);
        clsnackbar = findViewById(R.id.clActivosToma);
        
        // OCULTAR LOGICA HH (Paneles de ubicación manual)
        int[] panelesOcultar = {
            R.id.PanelTituloRazon, R.id.PanelRazonSpinner,
            R.id.PanelTituloEdificio, R.id.PanelEdificioSpinner,
            R.id.PanelTituloPiso, R.id.PanelPisoSpinner,
            R.id.PanelTituloSector, R.id.PanelSectorSpinner,
            R.id.Espacio4, R.id.Espacio5, R.id.Espacio6, R.id.Espacio7,
            R.id.Espacio8, R.id.Espacio9, R.id.Espacio10, R.id.Espacio11
        };
        
        for (int id : panelesOcultar) {
            View v = findViewById(id);
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    public void eventos() {
        CompaniaView.setOnItemSelectedListener(onItemSpinnerListenerCompania);
        EdificioView.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        PisoView.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        btn_continuar.setOnClickListener(OnClickListenerIrToma);
        txtAjusteOficina.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (txtAjusteOficina.getText().length() > 3) {
                    buscarOficinaPorIdPisoNombre();
                } else if (txtAjusteOficina.getText().length() == 0) {
                    PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
                    if (pisoRecord != null) {
                        cargarOficinasDesdeEntity(pisoRecord.getIdPiso());
                    }
                }
            }
        });
    }

    private void cargarUbicaciones() {
        if (CompaniaView.getSelectedItem() != null) {
            RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
            idCompania = razonSocialRecord.getIdRazon();
            cargarEdificiosDesdeEntity(idCompania);

            if (EdificioView.getSelectedItem() != null) {
                EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
                idedificioActivo = edificioRecord.getIdEdificio();
                cargarPisosDesdeEntity(idedificioActivo);

                if (PisoView.getSelectedItem() != null) {
                    PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
                    idpisoActivo = pisoRecord.getIdPiso();
                    cargarOficinasDesdeEntity(idpisoActivo);
                }
            }
        }
    }

    public void RecibirTakesInfo() {
        if (getIntent().getExtras() != null) {
            idTake = getIntent().getExtras().getString("Take_ID");
            takeName = getIntent().getExtras().getString("Take_Name");
            takeDescription = getIntent().getExtras().getString("Take_Description");
            takeDate = getIntent().getExtras().getString("Take_Date");
            Log.d(TAG, "RecibirTakesInfo: idTake=" + idTake + ", name=" + takeName);
        } else {
            Log.e(TAG, "RecibirTakesInfo: Extras is NULL");
        }
    }

    private void configurarSubTomas() {
        // Inicializar vistas nuevas
        lblTituloDinamico = findViewById(R.id.lbl_titulo_dinamico);
        txtProgressCircle = findViewById(R.id.txtProgressCircle);

        // Setear título dinámico
        lblTituloDinamico.setText("Hacer Inventario");

        recyclerSubTomas = findViewById(R.id.recyclerSubTomas);
            recyclerSubTomas.setLayoutManager(new LinearLayoutManager(this));
            adapterSubTomas = new TomaFisicaTomasAdapter(
                    new ArrayList<>(),
                    new TomaFisicaTomasAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(TomaFisicaTomasEntity item) {
                            Log.d(TAG, "onItemClick: Clicked on take " + item.getNumeroToma());

                            try {
                                if (OficinaView != null && OficinaView.getSelectedItem() != null) {
                                    oficinaNuevo oficinaRecord = (oficinaNuevo) OficinaView.getSelectedItem();
                                    if (oficinaRecord != null) {
                                        idOficina = oficinaRecord.getIdOficina();
                                    }
                                }

                                if (TipoInventarioView != null && TipoInventarioView.getSelectedItem() != null) {
                                    EntidadTiposInventarios entidadTiposInventarios = (EntidadTiposInventarios) TipoInventarioView.getSelectedItem();
                                    if (entidadTiposInventarios != null) {
                                        idTipoInventario = entidadTiposInventarios.getidTipoToma();
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error capturing spinner values (non-fatal)", e);
                            }

                            Intent intent = new Intent(ElegirUbicacion_TomaFisica.this, Lectura_Inventario.class);
                            intent.putExtra("IdTake", idTake);
                            intent.putExtra("takeName", takeName);
                            intent.putExtra("takeDescription", takeDescription);
                            intent.putExtra("takeDate", takeDate);
                            intent.putExtra("idOficina", idOficina);
                            intent.putExtra("tipoInventario", idTipoInventario);
                            intent.putExtra("IdSubToma", item.getIdToma());
                            intent.putExtra("NumeroToma", item.getNumeroToma());

                            try {
                                Log.d(TAG, "onItemClick: Attempting to start Lectura_Inventario");
                                Toast.makeText(ElegirUbicacion_TomaFisica.this, "Abriendo conteo...", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                Log.d(TAG, "onItemClick: Started Lectura_Inventario activity");
                            } catch (Exception e) {
                                Log.e(TAG, "onItemClick: Error starting activity", e);
                                Toast.makeText(ElegirUbicacion_TomaFisica.this, "Error al abrir inventario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new TomaFisicaTomasAdapter.OnGoToCountsClickListener() {
                        @Override
                        public void onGoToCountsClick(TomaFisicaTomasEntity item) {
                            Intent intent = new Intent(ElegirUbicacion_TomaFisica.this, RegistroConteosActivity.class);
                            intent.putExtra("tomaFisicaId", idTake);
                            intent.putExtra("idToma", item.getIdToma());
                            intent.putExtra("numeroToma", item.getNumeroToma());
                            startActivity(intent);
                        }
                    }
            );
            recyclerSubTomas.setAdapter(adapterSubTomas);
            subTomasDao = new TomaFisicaTomasDao(this);
            cargarSubTomas();
    }

    private void cargarSubTomas() {
        Log.d(TAG, "cargarSubTomas: Iniciando para idTake=" + idTake);
        // Primero cargamos lo que tengamos localmente
        if (idTake != null) {
            List<TomaFisicaTomasEntity> localList = subTomasDao.getByTomaFisicaId(idTake);
            Log.d(TAG, "cargarSubTomas: Encontrados localmente=" + localList.size());
            if (!localList.isEmpty()) {
                actualizarListaYProgreso(localList);
            }
        }

        // Sincronizar con API
        subTomasDao.fetchAndSyncFromApi(idTake, () -> {
            runOnUiThread(() -> {
                if (idTake != null) {
                    List<TomaFisicaTomasEntity> updatedList = subTomasDao.getByTomaFisicaId(idTake);
                    Log.d(TAG, "cargarSubTomas: Encontrados tras sync=" + updatedList.size());
                    actualizarListaYProgreso(updatedList);
                    
                    if (updatedList.isEmpty()) {
                         // Debug: Ver si hay ALGO en la tabla
                         // (Esto es solo para depuración, se puede quitar después)
                         // int totalCount = subTomasDao.getPendientesCount(idTake); // No sirve pq filtra por ID
                         // Log.d(TAG, "Total en tabla para este ID: " + totalCount);
                    }
                }
            });
        });
    }

    private void actualizarListaYProgreso(List<TomaFisicaTomasEntity> lista) {
        adapterSubTomas.actualizar(lista);
        // Actualizar círculo de progreso (X/5)
        int count = lista != null ? lista.size() : 0;
        txtProgressCircle.setText(count + "/5");
    }

    //region Se rellenan los spinners

    private void cargarRazonesSocialesDesdeEntity() {
        _mapRazonSociales.clear();
        Set<String> added = new HashSet<>();
        
        for (UbicacionEntity u : allUbicaciones) {
            if (u.getASysId() != null && u.getUbicacionA() != null && added.add(u.getASysId())) {
                RazonNuevo item = new RazonNuevo(u.getASysId(), u.getUbicacionA());
                _mapRazonSociales.put(u.getASysId(), item);
            }
        }
        
        RazonNuevo[] razones = _mapRazonSociales.values().toArray(new RazonNuevo[0]);
        fillSpinnerRazon(razones);
        
        // Trigger selection if needed or handle nothing selected
        if (razones.length > 0) {
            // Usually first item is selected automatically
        }
    }

    private void cargarEdificiosDesdeEntity(String idCompania) {
        _mapEdificios.clear();
        Set<String> added = new HashSet<>();
        
        for (UbicacionEntity u : allUbicaciones) {
            if (u.getASysId() != null && u.getASysId().equals(idCompania)) {
                if (u.getBSysId() != null && u.getUbicacionB() != null && added.add(u.getBSysId())) {
                    EdificioNuevo item = new EdificioNuevo(u.getBSysId(), u.getUbicacionB(), idCompania);
                    _mapEdificios.put(u.getBSysId(), item);
                }
            }
        }
        
        EdificioNuevo[] edificios = _mapEdificios.values().toArray(new EdificioNuevo[0]);
        fillSpinnerEdificio(edificios);
    }

    private void cargarPisosDesdeEntity(String idEdificio) {
        _mapPisos.clear();
        Set<String> added = new HashSet<>();
        
        for (UbicacionEntity u : allUbicaciones) {
            if (u.getBSysId() != null && u.getBSysId().equals(idEdificio)) {
                if (u.getCSysId() != null && u.getUbicacionC() != null && added.add(u.getCSysId())) {
                    PisoNuevo item = new PisoNuevo(u.getCSysId(), u.getUbicacionC(), idEdificio);
                    _mapPisos.put(u.getCSysId(), item);
                }
            }
        }
        
        PisoNuevo[] pisos = _mapPisos.values().toArray(new PisoNuevo[0]);
        fillSpinnerPiso(pisos);
    }

    private void cargarOficinasDesdeEntity(String idPiso) {
        _mapOficinas.clear();
        Set<String> added = new HashSet<>();
        
        for (UbicacionEntity u : allUbicaciones) {
            if (u.getCSysId() != null && u.getCSysId().equals(idPiso)) {
                if (u.getDSysId() != null && u.getUbicacionD() != null && added.add(u.getDSysId())) {
                    oficinaNuevo item = new oficinaNuevo(u.getDSysId(), u.getUbicacionD(), idPiso);
                    _mapOficinas.put(u.getDSysId(), item);
                }
            }
        }
        
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }

    private void buscarOficinaPorIdPisoNombre() {
        if (_itemSelectedUserPiso && PisoView.getSelectedItem() != null) {
            PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
            String nombreBusqueda = txtAjusteOficina.getText().toString();
            
            _mapOficinas.clear();
            Set<String> added = new HashSet<>();
            String idPiso = pisoRecord.getIdPiso();
            
            for (UbicacionEntity u : allUbicaciones) {
                if (u.getCSysId() != null && u.getCSysId().equals(idPiso)) {
                    if (u.getDSysId() != null && u.getUbicacionD() != null) {
                        if (nombreBusqueda.isEmpty() || u.getUbicacionD().toLowerCase().contains(nombreBusqueda.toLowerCase())) {
                            if (added.add(u.getDSysId())) {
                                oficinaNuevo item = new oficinaNuevo(u.getDSysId(), u.getUbicacionD(), idPiso);
                                _mapOficinas.put(u.getDSysId(), item);
                            }
                        }
                    }
                }
            }
            
            oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
            fillSpinnerOficina(oficinas);
        }
        _itemSelectedUserPiso = true;
    }

    private void cargarTiposInventarios() {
        try {
            InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(ElegirUbicacionView.getContext());
            _mapTipoInventarios = (Map<Integer, EntidadTiposInventarios>) inventoryDBHelper.ObtenerTiposInventario();
            EntidadTiposInventarios[] tiposInventarios = _mapTipoInventarios.values().toArray(new EntidadTiposInventarios[0]);
            fillTiposInventarios(tiposInventarios);
        } catch (Exception e) {
            Log.e(TAG, "Error cargando tipos de inventario", e);
        }
    }

    /* Deprecated methods replaced by above
    private void cargarRazonesSociales() { ... }
    private void cargarEdificios(String idCompania) { ... }
    private void cargarPisos(String idEdificio) { ... }
    private void cargarOficinas(String idPiso) { ... }
    private void cargarOficinasPorPisoNombre(String idPiso, String nombre) { ... }
    */

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerCompania = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserCompania) {
                RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
                if (razonSocialRecord != null) {
                    cargarEdificiosDesdeEntity(razonSocialRecord.getIdRazon());
                }
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
                EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
                if (edificioRecord != null) {
                    cargarPisosDesdeEntity(edificioRecord.getIdEdificio());
                }
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
                PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
                if (pisoRecord != null) {
                    cargarOficinasDesdeEntity(pisoRecord.getIdPiso());
                }
            }
            _itemSelectedUserPiso = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void fillTiposInventarios(EntidadTiposInventarios[] tiposInventarios) {
        TipoInventarioView.setAdapter(new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                , R.layout.spinner_layaout, tiposInventarios));

    }

    private void fillSpinnerRazon(RazonNuevo[] razonSocialRecords) {
        CompaniaView = findViewById(R.id.compania_toma);
        CompaniaView.setAdapter(new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                , R.layout.spinner_layaout, razonSocialRecords));

    }

    private void fillSpinnerEdificio(EdificioNuevo[] edificioRecords) {
        EdificioView = findViewById(R.id.edificio_toma);
        EdificioView.setAdapter(new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                , R.layout.spinner_layaout, edificioRecords));
    }

    private void fillSpinnerPiso(PisoNuevo[] pisoRecords) {
        PisoView = findViewById(R.id.piso_toma);
        PisoView.setAdapter(new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                , R.layout.spinner_layaout, pisoRecords));
    }

    private void fillSpinnerOficina(oficinaNuevo[] oficinaRecords) {
        OficinaView = findViewById(R.id.oficina_toma);
        OficinaView.setAdapter(new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                , R.layout.spinner_layaout, oficinaRecords));
    }
    //endregion

    public void LeerInventarioManual() {
        try {
            if (TextUtils.isEmpty(OficinaView.getSelectedItem().toString())) {
                Toast.makeText(getApplicationContext(), "Seleccione una ubicación", Toast.LENGTH_LONG).show();
                return;
            }

            EntidadTiposInventarios entidadTiposInventarios = (EntidadTiposInventarios) TipoInventarioView.getSelectedItem();
            idTipoInventario = entidadTiposInventarios.getidTipoToma();

            oficinaNuevo oficinaRecord = (oficinaNuevo) OficinaView.getSelectedItem();
            idOficina = oficinaRecord.getIdOficina();

            // boolean respuesta = OfficesDBHelper.ActivosEnUbicacion(idOficina); // Deprecated
            boolean tieneActivos = !activoDao.getActivosByUbicacion(idOficina).isEmpty();
            
            if (!tieneActivos) {
                AlertasPersonalizadas.showAlertDialogAsk(_activity, "ALERTA", "Este sector " +
                        "no posee activos.", IrInventario, RespuestaNegativa);
                return;
            }

            Intent intent = new Intent(ElegirUbicacion_TomaFisica.this, Lectura_Inventario.class);
            intent.putExtra("IdTake", idTake);
            intent.putExtra("takeName", takeName);
            intent.putExtra("takeDescription", takeDescription);
            intent.putExtra("takeDate", takeDate);
            intent.putExtra("idOficina", idOficina);
            intent.putExtra("tipoInventario", idTipoInventario);
            startActivity(intent);

        } catch (Exception e) {
            Log.e("Activos Por Sector", e.getMessage());
            MostrarSnackBar(false, "Ha ocurrido un error, intente nuevamente");
        }
    }

    private void cargarUbicacionPorEPC(String EPC) {
        // eUbicacionActivo = OfficesDBHelper.VerSectorEPC2(EPC); // Deprecated
        eUbicacionActivo = null; 
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Objects.isNull(eUbicacionActivo)) {
                AlertasPersonalizadas.showAlertDialog(_activity, "Atención",
                        "El tag leído no le pertenece a una oficina!" + "\n" + "" +
                                "Seleccione la ubicación manualmente o lea nuevamente el tag.");
                return;
            }
        }
        for (Map.Entry<String, RazonNuevo> item : _mapRazonSociales.entrySet()) {
            if (item.getValue().getIdRazon().equals(eUbicacionActivo.getIdRazonSocial())) {
                CompaniaView.setSelection(((ArrayAdapter) CompaniaView.getAdapter()).getPosition(item.getValue()));
                cargarEdificiosDesdeEntity(eUbicacionActivo.getIdRazonSocial());
            }
        }

        for (Map.Entry<String, EdificioNuevo> item : _mapEdificios.entrySet()) {
            if (item.getValue().getIdEdificio().equals(eUbicacionActivo.getIdEdificio())) {
                EdificioView.setSelection(((ArrayAdapter) EdificioView.getAdapter()).getPosition(item.getValue()));
                cargarPisosDesdeEntity(eUbicacionActivo.getIdEdificio());
            }
        }

        for (Map.Entry<String, PisoNuevo> item : _mapPisos.entrySet()) {
            if (item.getValue().getIdPiso().equals(eUbicacionActivo.getIdPiso())) {
                PisoView.setSelection(((ArrayAdapter) PisoView.getAdapter()).getPosition(item.getValue()));
                cargarOficinasDesdeEntity(eUbicacionActivo.getIdPiso());
            }
        }

        for (Map.Entry<String, oficinaNuevo> item : _mapOficinas.entrySet()) {
            if (item.getValue().getIdOficina().equals(eUbicacionActivo.getIdOficina())) {
                OficinaView.setSelection(((ArrayAdapter) OficinaView.getAdapter()).getPosition(item.getValue()));
            }
        }
    }

    //region Acción de lectura de tags con la HH
    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
             try { rfidHandler.onPause(); } catch (Exception e) {}
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (rfidHandler != null) rfidHandler.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfidHandler != null) rfidHandler.onDestroy();
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        for (int index = 0; index < tagData.length; index++) {
            _lastTag = tagData[index].getEpc();
        }
    }

    public static void Message() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }

    @Override
    public Context GetContext() {
        return this;
    }

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

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (rfidHandler == null) return;
        triggerPressed = pressed;
        if (pressed) {
            rfidHandler.performInventory();
        } else {
            rfidHandler.stopInventory();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
            //ProbarEPCManual("800474453240000000031607");
        }
    }

    //endregion

    //region Metodos de soporte
    private Button.OnClickListener OnClickListenerIrToma = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LeerInventarioManual();
        }
    };

    Runnable IrInventario = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(ElegirUbicacion_TomaFisica.this, Lectura_Inventario.class);
            intent.putExtra("IdTake", idTake);
            intent.putExtra("takeName", takeName);
            intent.putExtra("takeDescription", takeDescription);
            intent.putExtra("takeDate", takeDate);
            intent.putExtra("idOficina", idOficina);
            intent.putExtra("tipoInventario", idTipoInventario);
            startActivity(intent);

        }
    };

    Runnable RespuestaNegativa = new Runnable() {
        @Override
        public void run() {
        }
    };

    private void MostrarSnackBar(boolean tipoSnack, String mensaje) {
        if (tipoSnack) {
            _snackbar = Snackbar.make(clsnackbar, mensaje, 3000);
            _snackbar.setActionTextColor(Color.rgb(179, 179, 179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(4, 165, 77));
        } else {
            _snackbar = Snackbar.make(clsnackbar, mensaje, 4000);
            _snackbar.setActionTextColor(Color.rgb(179, 179, 179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(170, 18, 18));
        }
        _snackbar.show();
    }
    //endregion
}
