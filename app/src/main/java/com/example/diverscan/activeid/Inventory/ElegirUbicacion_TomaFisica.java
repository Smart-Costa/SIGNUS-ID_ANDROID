package com.example.diverscan.activeid.Inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.zebra.rfid.api3.TagData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ElegirUbicacion_TomaFisica extends AppCompatActivity implements ResponseHandlerInterface {

    private View ElegirUbicacionView;
    InventoryDBHelper InventoryDBHelper;
    OfficesDBHelper OfficesDBHelper;
    List<String> listSpinner;
    Context _context;
    Activity _activity;
    EUbicacionActivo eUbicacionActivo = new EUbicacionActivo();
    private Button btn_continuar;
    private Spinner CompaniaView, EdificioView, PisoView, OficinaView, TipoInventarioView;

    private Map<Integer, RazonNuevo> _mapRazonSociales = new HashMap<Integer, RazonNuevo>();
    private Map<Integer, EdificioNuevo> _mapEdificios = new HashMap<Integer, EdificioNuevo>();
    private Map<Integer, PisoNuevo> _mapPisos = new HashMap<Integer, PisoNuevo>();
    private Map<Integer, oficinaNuevo> _mapOficinas = new HashMap<Integer, oficinaNuevo>();
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

    public ElegirUbicacion_TomaFisica() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_ubicacion__toma_fisica);
        _context = this;
        _activity = this;
        controles();
        eventos();
        cargarTiposInventarios();
        cargarRazonesSociales();
        cargarUbicaciones();
        RecibirTakesInfo();
        rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);
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

        OfficesDBHelper = new OfficesDBHelper(ElegirUbicacionView.getContext());
        txtAjusteOficina = findViewById(R.id.txtSectorBusquedaAS);
        clsnackbar = findViewById(R.id.clActivosToma);
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
                    cargarOficinas(pisoRecord.getIdPiso());
                }
            }
        });
    }

    private void cargarUbicaciones() {
        RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
        idCompania = razonSocialRecord.getIdRazon();
        cargarEdificios(idCompania);

        EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
        idedificioActivo = edificioRecord.getIdEdificio();
        cargarPisos(idedificioActivo);

        PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
        idpisoActivo = pisoRecord.getIdPiso();
        cargarOficinas(idpisoActivo);
    }

    public void RecibirTakesInfo() {
        idTake = getIntent().getExtras().getString("Take_ID");
        takeName = getIntent().getExtras().getString("Take_Name");
        takeDescription = getIntent().getExtras().getString("Take_Description");
        takeDate = getIntent().getExtras().getString("Take_Date");
    }


    //region Se rellenan los spinners

    private void buscarOficinaPorIdPisoNombre() {
        if (_itemSelectedUserPiso) {
            PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
            String nombreBusqueda = txtAjusteOficina.getText().toString();
            cargarOficinasPorPisoNombre(pisoRecord.getIdPiso(), nombreBusqueda);
        }
        _itemSelectedUserPiso = true;
    }

    private void cargarOficinasPorPisoNombre(String idPiso, String nombre) {

        _mapOficinas = (Map<Integer, oficinaNuevo>) OfficesDBHelper.ObtenerOficinaPorPisoDescripcion3(idPiso, nombre);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }
    private void cargarTiposInventarios() {
        InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(ElegirUbicacionView.getContext());
        _mapTipoInventarios = (Map<Integer, EntidadTiposInventarios>) inventoryDBHelper.ObtenerTiposInventario();
        EntidadTiposInventarios[] tiposInventarios = _mapTipoInventarios.values().toArray(new EntidadTiposInventarios[0]);
        fillTiposInventarios(tiposInventarios);
    }

    private void cargarRazonesSociales() {
        RazonSocialDBHelper razonSocialDBHelper = new RazonSocialDBHelper(ElegirUbicacionView.getContext());
        _mapRazonSociales = (Map<Integer, RazonNuevo>) razonSocialDBHelper.ObtenerRazon();
        RazonNuevo[] razones = _mapRazonSociales.values().toArray(new RazonNuevo[0]);
        fillSpinnerRazon(razones);
    }

    private void cargarEdificios(String idCompania) {
        EdificioDBHelper edificioDBHelper = new EdificioDBHelper(ElegirUbicacionView.getContext());
        _mapEdificios = (Map<Integer, EdificioNuevo>) edificioDBHelper.ObtenerEdificio(idCompania);
        EdificioNuevo[] edificios = _mapEdificios.values().toArray(new EdificioNuevo[0]);
        fillSpinnerEdificio(edificios);
    }

    private void cargarPisos(String idEdificio) {
        PisoDBHelper pisoDBHelper = new PisoDBHelper(ElegirUbicacionView.getContext());
        _mapPisos = (Map<Integer, PisoNuevo>) pisoDBHelper.ObtenerPiso(idEdificio);
        PisoNuevo[] pisos = _mapPisos.values().toArray(new PisoNuevo[0]);
        fillSpinnerPiso(pisos);
    }

    private void cargarOficinas(String idPiso) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(ElegirUbicacionView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficina(idPiso);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerCompania = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserCompania) {
                RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
                cargarEdificios(razonSocialRecord.getIdRazon());
            }
            _itemSelectedUserCompania = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerEdificio = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserEdificio) {
                EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
                cargarPisos(edificioRecord.getIdEdificio());
            }
            _itemSelectedUserEdificio = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerPiso = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserPiso) {
                PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
                cargarOficinas(pisoRecord.getIdPiso());
            }
            _itemSelectedUserPiso = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
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

            boolean respuesta = OfficesDBHelper.ActivosEnUbicacion(idOficina);
            if (!respuesta) {
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
        eUbicacionActivo = OfficesDBHelper.VerSectorEPC2(EPC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Objects.isNull(eUbicacionActivo)) {
                AlertasPersonalizadas.showAlertDialog(_activity, "Atención",
                        "El tag leído no le pertenece a una oficina!" + "\n" + "" +
                                "Seleccione la ubicación manualmente o lea nuevamente el tag.");
                return;
            }
        }
        for (Map.Entry<Integer, RazonNuevo> item : _mapRazonSociales.entrySet()) {
            if (item.getValue().getIdRazon().equals(eUbicacionActivo.getIdRazonSocial())) {
                CompaniaView.setSelection(item.getKey());
            }
        }

        for (Map.Entry<Integer, EdificioNuevo> item : _mapEdificios.entrySet()) {
            if (item.getValue().getIdEdificio().equals(eUbicacionActivo.getIdEdificio())) {
                EdificioView.setSelection(item.getKey());
            }
        }

        for (Map.Entry<Integer, PisoNuevo> item : _mapPisos.entrySet()) {
            if (item.getValue().getIdPiso().equals(eUbicacionActivo.getIdPiso())) {
                PisoView.setSelection(item.getKey());
            }
        }

        for (Map.Entry<Integer, oficinaNuevo> item : _mapOficinas.entrySet()) {
            if (item.getValue().getIdOficina().equals(eUbicacionActivo.getIdOficina())) {
                OficinaView.setSelection(item.getKey());
            }
        }
    }

    //region Acción de lectura de tags con la HH
    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        rfidHandler.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

    @Override
    public void handleTagdata(TagData[] tagData) {
        for (int index = 0; index < tagData.length; index++) {
            _lastTag = tagData[index].getTagID();
        }
    }

    public static void Message() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        triggerPressed = pressed;
        if (pressed) {
            rfidHandler.performInventory();
        } else {
            rfidHandler.stopInventory();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cargarUbicacionPorEPC(_lastTag);
                    Message();
                }
            });
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
    }
    //endregion

    private Button.OnClickListener OnClickListenerIrToma = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LeerInventarioManual();
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
            snackBarView.setBackgroundColor(Color.rgb(242, 59, 59));
        }
        _snackbar.show();
    }

    //region Respuestas Runnable
    final Runnable RespuestaNegativa = new Runnable() {
        @Override
        public void run() {
        }
    };

    final Runnable CerrarVentana = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    final Runnable IrInventario = new Runnable() {
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
    //endregion
}