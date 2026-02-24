package com.example.diverscan.activeid.TomasFisicas;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.AssetStatus.AssetStatusDBHerlper;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioNuevo;
import com.example.diverscan.activeid.Employees.EmployeesDBHelper;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Piso.PisoNuevo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RazonSocial.RazonNuevo;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.sqlite.newAssets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.diverscan.activeid.R.id.lbl_numero_activo_crea;

public class createAssets extends AppCompatActivity implements ResponseHandlerInterface
{

    private RadioButton radio_PlacaView;
    private RadioButton radio_NumeroView;
    private EditText NumeroingresadoView;
    private EditText NumeroactivoView;
    private EditText PlacaView;
    private EditText DescripcionView;
    private Spinner CompaniaView;
    private Spinner EdificioView;
    private Spinner PisoView;
    private Spinner OficinaView;
    private Spinner PreDescripciones;
    private SimpleCursorAdapter CompaniaSpinner;
    private SimpleCursorAdapter EdificioSpinner;
    private SimpleCursorAdapter PisoSpinner;
    private SimpleCursorAdapter OficinaSpinner;
    private EditText EncargadoView;
    private EditText MarcaView;
    private EditText ModeloView;
    private EditText SerieView;
    private EditText EPCView;
    private TextView LabelNumero;
    private TextView LabelPlaca;
    private TextView txtUbicacion;
    String idCompaniaActivo, idedificioActivo, idpisoActivo, idoficinaActivo;
    EntidadActivos entidadActivos;
    private EditText txtCodigoEmpleado;
    private View mCrearView;
    private EditText text_anno;
    private EditText text_cap;
    private EditText txtDetalleEstado;
    private Spinner spEstadoConservacion;
    Button btn_Crear;
    private TagWriter rfidHandler;

    private String[] strEstadoConservacion;
    private List<String> listaEstadoConservacion;
    private ArrayAdapter<String> adapterEstadoConservacion;
    private String preEstadoConservacion;

    private Map<Integer, RazonNuevo> _mapRazonSociales = new HashMap<Integer, RazonNuevo>();
    private Map<Integer, EdificioNuevo> _mapEdificios = new HashMap<Integer, EdificioNuevo>();
    private Map<Integer, PisoNuevo> _mapPisos = new HashMap<Integer, PisoNuevo>();
    private Map<Integer, oficinaNuevo> _mapOficinas = new HashMap<Integer, oficinaNuevo>();
    private Map<Integer, EntidadEmployees>_mapEntidadEmployees = new HashMap<Integer, EntidadEmployees>();
    private Map<Integer, EntidadAssetStatus> _mapEntidadAssetStatus = new HashMap<Integer, EntidadAssetStatus>();
    EmployeesDBHelper employeesDBHelper;
    AssetStatusDBHerlper assetStatusDBHerlper;
    private boolean _itemSelectedUserCompania, _itemSelectedUserEdificio, _itemSelectedUserPiso = true;
    private Spinner spEmpleados;
    private String numeroingresado, numero, placa, descripcion, compania, idCompania, idEdificio, edificio,
            idPiso, piso, idOficina, oficina, encargado, marca, modelo, serie, epc,AnoFabricacion, Capacidad, _detalleEstado, _estadoConservacion;
    private Spinner spAssetStatus;
    String idCompaniaCrea, idedificioCrea, idpisoCrea, idoficinaCrea;
    newAssets newAssets;
    RazonSocialDBHelper dataSource;
    PisoDBHelper dataSourcepiso;
    OficinaDBHelper dataSourceOficina;
    EdificioDBHelper dataSourceEdificio;
    private Activity _activity;
    AlertDialog alertDialog;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;

    @Override
    protected void onCreate(Bundle savedStateInstance)
    {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_nuevo_activo);
        _activity = this;
        controles();
        
        // Inicializar RFID
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        eventos();
        cargarRazonesSociales();
        cargarUbicaciones();
        cargarEstado();
        CargarEstadoConservacion();
        //CargarDescripciones();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

    // --- ResponseHandlerInterface Implementation ---

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Validar si vienen múltiples tags (opcional, pero para creación suele ser uno a uno)
        if (tagData.length > 1) {
            runOnUiThread(() -> {
                rfidHandler.stopRead();
                Toast.makeText(this, "Múltiples etiquetas detectadas. Acerque solo el activo a etiquetar.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        runOnUiThread(() -> {
            for (ReaderTag tag : tagData) {
                if (tag.getEpc() != null && !tag.getEpc().isEmpty()) {
                    // Lógica de lectura ÚNICA
                    EPCView.setText(tag.getEpc());
                    rfidHandler.stopRead();
                    Toast.makeText(this, "Etiqueta leída correctamente", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            if (pressed) {
                // Limpiar campo previo si se desea
                EPCView.setText("");
                rfidHandler.startRead();
            } else {
                rfidHandler.stopRead();
            }
        });
    }

    @Override
    public void SetMessage(String message) {
         // Opcional: Mostrar logs o toasts de depuración
    }

    @Override
    public android.content.Context GetContext() {
        return this;
    }
    // --- Fin Interface ---
    
    //*******************************************************************************
    private void controles()
    {
        mCrearView = findViewById(R.id.CreateForm);

        //radio_PlacaView =  (RadioButton) findViewById(R.id.radio_Placa_crea);
        //radio_NumeroView = (RadioButton) findViewById(R.id.radio_Numero_crea);
        CompaniaView = findViewById(R.id.compania_crea);
        EdificioView = findViewById(R.id.edificio_crea);
        PisoView = findViewById(R.id.piso_crea);
        OficinaView = findViewById(R.id.oficina_crea);
        //PreDescripciones = findViewById(R.id.SpinnerDescripciones);
        NumeroactivoView = findViewById(R.id.txt_numero_activo_crea);
        LabelNumero = findViewById(lbl_numero_activo_crea);
        PlacaView = findViewById(R.id.txt_placa_crea);
        LabelPlaca= findViewById(R.id.lbl_placa_crea);
        DescripcionView = findViewById(R.id.txt_descripcion_crea);
        //EncargadoView = findViewById(R.id.txt_encargado_crea);
        ModeloView = findViewById(R.id.txt_modelo_crea);
        MarcaView = findViewById(R.id.txt_marca_crea);
        SerieView = findViewById(R.id.txt_serie_crea);
        EPCView = findViewById(R.id.txt_epc_crea);
        txtUbicacion = findViewById(R.id.UptxtSectorBusqueda);
        newAssets = new newAssets(mCrearView.getContext());
        dataSource = new RazonSocialDBHelper(mCrearView.getContext());
        dataSourceEdificio = new EdificioDBHelper(mCrearView.getContext());
        dataSourcepiso = new PisoDBHelper(mCrearView.getContext());
        dataSourceOficina = new OficinaDBHelper(mCrearView.getContext());
        btn_Crear = findViewById(R.id.btn_Ingresaractivo);
        text_anno = findViewById(R.id.txt_anno);
        text_cap = findViewById(R.id.txt_cap);
        txtDetalleEstado = findViewById(R.id.DetEstado);
        spEstadoConservacion = findViewById(R.id.spEstadoConservacion);
        /*NumeroactivoView.setVisibility(View.INVISIBLE);
        LabelNumero.setVisibility(View.INVISIBLE);*/
        txtCodigoEmpleado= findViewById(R.id.txtCodigoEmpleado);
        assetStatusDBHerlper = new AssetStatusDBHerlper(mCrearView.getContext());
        employeesDBHelper = new EmployeesDBHelper(mCrearView.getContext());
        spEmpleados =   findViewById(R.id.spEmpleados);
        spAssetStatus = findViewById(R.id.spEstado) ;
        txtCodigoEmpleado= findViewById(R.id.txtCodigoEmpleado);
    }

    private void eventos()
    {
        btn_Crear.setOnClickListener(OnClickListenerIngresar);
        //radio_PlacaView.setOnClickListener(OnClickListenerPlaca);
        //radio_NumeroView.setOnClickListener(OnClickListenerNumero);
        CompaniaView.setOnItemSelectedListener(onItemSpinnerListenerCompania);
        EdificioView.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        PisoView.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        //PreDescripciones.setOnItemSelectedListener(onItemSelectedListenerDescripcion);
        text_anno.setOnTouchListener(onTouchListenerYear);
        txtUbicacion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
                if(txtUbicacion.getText().length() > 3)
                {
                    buscarOficinaPorIdPisoNombre();
                }
                else if(txtUbicacion.getText().length() == 0)
                {
                    PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
                    cargarOficinas(pisoRecord.getIdPiso());
                }
            }
        });
        txtCodigoEmpleado.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(txtCodigoEmpleado.getText().length() > 3){
                    buscarEmpleadoPorDescripcion();
                }
            }
        });
    }
    private void buscarEmpleadoPorDescripcion()
    {
        String nombreBusqueda = txtCodigoEmpleado.getText().toString();
        cargarEmpleadoPorDescripcion(nombreBusqueda);
    }
    private void cargarEmpleadoPorDescripcion(String descripcion)
    {
        _mapEntidadEmployees = (Map<Integer, EntidadEmployees>) employeesDBHelper.GetEmployees(descripcion);
        EntidadEmployees[] entidadEmployees = _mapEntidadEmployees.values().toArray(new EntidadEmployees[0]);
        fillSpinnerEmployees(entidadEmployees);
    }


    private void fillSpinnerEmployees(EntidadEmployees[] entidadEmployees)
    {
        spEmpleados.setAdapter( new ArrayAdapter<>(this.getApplicationContext()
                ,R.layout.spinner_layaout,entidadEmployees));
        spEmpleados.requestFocus();
    }
    private void buscarOficinaPorIdPisoNombre()
    {
        if (_itemSelectedUserPiso)
        {
            PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
            String nombreBusqueda = txtUbicacion.getText().toString();
            cargarOficinasPorPisoNombre(pisoRecord.getIdPiso(),nombreBusqueda);
        }
        _itemSelectedUserPiso = true;
    }
    private void cargarEstado() {
        _mapEntidadAssetStatus = assetStatusDBHerlper.GetAssetStatus();
        EntidadAssetStatus[] assetStatus = _mapEntidadAssetStatus.values().toArray(new EntidadAssetStatus[0]);
        fillSpinnerEstado(assetStatus);
        spAssetStatus.requestFocus();
    }
    private void fillSpinnerEstado(EntidadAssetStatus[] entidadAssetStatus){
        spAssetStatus.setAdapter( new ArrayAdapter<>(this.getApplicationContext()
                ,R.layout.spinner_layaout,entidadAssetStatus));

    }

    private void cargarOficinasPorPisoNombre(String idPiso,String nombre) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mCrearView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficinaPorPisoDescripcion3(idPiso,nombre);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }

   /* private void CargarDescripciones(){
        listaDescripciones = new ArrayList<>();
        strDescripciones = new String[]{"Laptops", "Silla", "Mesas"};
        fillSpinnerDescripciones(strDescripciones);
    }*/

    private void cargarRazonesSociales() {
        RazonSocialDBHelper razonSocialDBHelper = new RazonSocialDBHelper(mCrearView.getContext());
        _mapRazonSociales = (Map<Integer, RazonNuevo>) razonSocialDBHelper.ObtenerRazon();
        RazonNuevo[] razones = _mapRazonSociales.values().toArray(new RazonNuevo[0]);
        fillSpinnerRazon(razones);
    }

    private void cargarUbicaciones(){

        RazonNuevo razonSocialRecord= (RazonNuevo) CompaniaView.getSelectedItem();
        idCompania = razonSocialRecord.getIdRazon();
        cargarEdificios(idCompania);

        EdificioNuevo edificioRecord = (EdificioNuevo)EdificioView.getSelectedItem();
        idedificioActivo = edificioRecord.getIdEdificio();
        cargarPisos(idedificioActivo);

        PisoNuevo pisoRecord = (PisoNuevo)PisoView.getSelectedItem();
        idpisoActivo = pisoRecord.getIdPiso();
        cargarOficinas(idpisoActivo);

    }

    private void cargarEdificios(String idCompania) {
        EdificioDBHelper edificioDBHelper = new EdificioDBHelper(mCrearView.getContext());
        _mapEdificios = (Map<Integer, EdificioNuevo>) edificioDBHelper.ObtenerEdificio(idCompania);
        EdificioNuevo[] edificios = _mapEdificios.values().toArray(new EdificioNuevo[0]);
        fillSpinnerEdificio(edificios);
    }

    private void cargarPisos(String idEdificio) {
        PisoDBHelper pisoDBHelper = new PisoDBHelper(mCrearView.getContext());
        _mapPisos = (Map<Integer, PisoNuevo>) pisoDBHelper.ObtenerPiso(idEdificio);
        PisoNuevo[] pisos = _mapPisos.values().toArray(new PisoNuevo[0]);
        fillSpinnerPiso(pisos);
    }

    private void cargarOficinas(String idPiso) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mCrearView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficina(idPiso);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }

    //region ComboBox de Estado de conservacion
    private void CargarEstadoConservacion(){
        listaEstadoConservacion = new ArrayList<>();
        strEstadoConservacion = new String[]{"Conservación Normal", "Como Nuevo", "Necesita Reparos Simples",
                "Necesita Reparos Importantes", "Obsoleto"};
        fillEstadoConservacion(strEstadoConservacion);
    }

    private void fillEstadoConservacion(String[] EstadoConservacion){
        spEstadoConservacion = findViewById(R.id.spEstadoConservacion);
        spEstadoConservacion.setAdapter(new ArrayAdapter<>(mCrearView.getContext().getApplicationContext(),
                R.layout.spinner_layaout, EstadoConservacion));
    }
    private AdapterView.OnItemSelectedListener onItemSpinnerListenerCompania = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserCompania) {
                RazonNuevo razonSocialRecord= (RazonNuevo) CompaniaView.getSelectedItem();
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
                EdificioNuevo edificioRecord= (EdificioNuevo)EdificioView.getSelectedItem();
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
                PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
                cargarOficinas(pisoRecord.getIdPiso());
            }
            _itemSelectedUserPiso = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /*private AdapterView.OnItemSelectedListener onItemSelectedListenerDescripcion = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()){
                case R.id.SpinnerDescripciones:
                preDescripcion = strDescripciones[position];
                DescripcionView.setText(preDescripcion);
                break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };*/

    /*private void fillSpinnerDescripciones(String[] descripciones){
        PreDescripciones = findViewById(R.id.SpinnerDescripciones);
        PreDescripciones.setAdapter(new ArrayAdapter<>(mCrearView.getContext().getApplicationContext(),
                R.layout.spinner_layaout, descripciones));
    }*/


    private void fillSpinnerRazon(RazonNuevo[] razonSocialRecords){
        CompaniaView = findViewById(R.id.compania_crea);
        CompaniaView.setAdapter( new ArrayAdapter<>(mCrearView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,razonSocialRecords));

    }

    private void fillSpinnerEdificio(EdificioNuevo[] edificioRecords){
        EdificioView = findViewById(R.id.edificio_crea);
        EdificioView.setAdapter( new ArrayAdapter<>(mCrearView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,edificioRecords));
    }

    private void fillSpinnerPiso(PisoNuevo[] pisoRecords){
        PisoView = findViewById(R.id.piso_crea);
        PisoView.setAdapter( new ArrayAdapter<>(mCrearView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,pisoRecords));
    }

    private void fillSpinnerOficina(oficinaNuevo[] oficinaRecords){
        OficinaView = findViewById(R.id.oficina_crea);
        OficinaView.setAdapter( new ArrayAdapter<>(mCrearView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,oficinaRecords));
    }

    private void ComprobarPlaca(View view){
        if(radio_PlacaView.isChecked()){
            NumeroactivoView.setVisibility(View.INVISIBLE);
            LabelNumero.setVisibility(View.INVISIBLE);
            PlacaView.setVisibility(View.VISIBLE);
            LabelPlaca.setVisibility(View.VISIBLE);
            final String text = "Ingrese una placa";
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }


    private void ComprobarNumero(View view){
        if(radio_NumeroView.isChecked()){
            PlacaView.setVisibility(View.INVISIBLE);
            LabelPlaca.setVisibility(View.INVISIBLE);
            NumeroactivoView.setVisibility(View.VISIBLE);
            LabelNumero.setVisibility(View.VISIBLE);
            final String text = "Ingrese un número";
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }


    private void SeleccionarItems (View view){
        try {
            //Rellenar Spinner Compania

            for(Map.Entry<Integer, RazonNuevo> item : _mapRazonSociales.entrySet()){
                if(item.getValue().getIdRazon().equals(idCompaniaCrea)){
                    CompaniaView.setSelection(item.getKey());
                }
            }

            idedificioCrea = entidadActivos.getIdEdificio();
            cargarEdificios(idCompaniaCrea);
            for(Map.Entry<Integer, EdificioNuevo> item : _mapEdificios.entrySet()){
                if(item.getValue().getIdEdificio().equals(idedificioCrea)){
                    EdificioView.setSelection(item.getKey());
                }
            }

            idpisoCrea = entidadActivos.getIdPiso();
            cargarPisos(idedificioCrea);
            for(Map.Entry<Integer, PisoNuevo> item : _mapPisos.entrySet()){
                if(item.getValue().getIdPiso().equals(idpisoCrea)){
                    PisoView.setSelection(item.getKey());
                }
            }

            idoficinaCrea=entidadActivos.getIdOficina();
            cargarOficinas(idpisoCrea);
            for(Map.Entry<Integer, oficinaNuevo> item : _mapOficinas.entrySet()){
                if(item.getValue().getIdOficina().equals(idoficinaCrea)){
                    OficinaView.setSelection(item.getKey());
                }
            }

            CompaniaView.setEnabled(true);
            EdificioView.setEnabled(true);
            PisoView.setEnabled(true);
            OficinaView.setEnabled(true);

        }catch (Exception e){
            Toast.makeText(this, "No se han encontraron más ubicaciones", Toast.LENGTH_LONG).show();
        }
    }


    public void IngresarActivo (View view)
    {
        String numero = NumeroactivoView.getText().toString();
        String placa = PlacaView.getText().toString();
        String descripcion = DescripcionView.getText().toString();
        String idCompania = "";
        String compania = "";
        String idEdificio = "";
        String edificio = "";
        String idPiso = "";
        String piso = "";
        String idOficina = "";
        String oficina = "";
        String marca = MarcaView.getText().toString();
        String modelo = ModeloView.getText().toString();
        String serie = SerieView.getText().toString();
        String epc = EPCView.getText().toString();
        String AnoFabricacion = text_anno.getText().toString();
        String Capacidad = text_cap.getText().toString();
        String _detalleEstado = txtDetalleEstado.getText().toString();
        String _estadoConservacion = "";
        
        if (spEstadoConservacion.getSelectedItem() != null) {
            _estadoConservacion = spEstadoConservacion.getSelectedItem().toString();
        }

        if(CompaniaView.getSelectedItem() != null) {
            RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
            idCompania = razonSocialRecord.getIdRazon();
            compania = razonSocialRecord.getNombreRazon();
        }
        if(EdificioView.getSelectedItem() != null) {
            EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
            idEdificio = edificioRecord.getIdEdificio();
            edificio = edificioRecord.getNombreEdificio();
        }
        if(PisoView.getSelectedItem() != null) {
            PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
            idPiso = pisoRecord.getIdPiso();
            piso = pisoRecord.getNombrePiso();
        }
        if(OficinaView.getSelectedItem() != null) {
            oficinaNuevo oficinaRecord = (oficinaNuevo) OficinaView.getSelectedItem();
            idOficina = oficinaRecord.getIdOficina();
            oficina = oficinaRecord.getNombreOficina();
        }

        String idEmployees = "0";
        String assetStatusSysId = "";
        if (spAssetStatus.getSelectedItem() != null) {
            EntidadAssetStatus entidadAssetStatus = (EntidadAssetStatus)spAssetStatus.getSelectedItem();
            assetStatusSysId = entidadAssetStatus.getId();
        }

        try {
            boolean respuestaPlaca = newAssets.VerificarPlaca(placa);
            if(placa.isEmpty()){
                Toast.makeText(getApplicationContext(), "Ingrese el número de placa", Toast.LENGTH_LONG).show();
            }
            else if(respuestaPlaca){
                Toast.makeText(getApplicationContext(), "El Activo ya existe", Toast.LENGTH_LONG).show();
            }
            else if(descripcion.isEmpty()){
                Toast.makeText(getApplicationContext(), "Ingrese una descripción", Toast.LENGTH_LONG).show();
            }
            else if(idCompania.isEmpty()){
                Toast.makeText(getApplicationContext(), "Seleccione una compañía", Toast.LENGTH_LONG).show();
            }
            else if(idEdificio.isEmpty()){
                Toast.makeText(getApplicationContext(), "Seleccione un edificio", Toast.LENGTH_LONG).show();
            }
            else if(idPiso.isEmpty()){
                Toast.makeText(getApplicationContext(), "Seleccione un piso", Toast.LENGTH_LONG).show();
            }
            else if(idOficina.isEmpty()){
                Toast.makeText(getApplicationContext(), "Seleccione una oficina", Toast.LENGTH_LONG).show();
            } else {
                
                android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
                progressDialog.setMessage("Guardando activo...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

                String finalIdCompania = idCompania;
                String finalCompania = compania;
                String finalIdEdificio = idEdificio;
                String finalEdificio = edificio;
                String finalIdPiso = idPiso;
                String finalPiso = piso;
                String finalIdOficina = idOficina;
                String finalOficina = oficina;
                String finalAssetStatusSysId = assetStatusSysId;
                String finalDetalleEstado = _detalleEstado;
                String finalEstadoConservacion = _estadoConservacion;

                executor.execute(() -> {
                    boolean respuesta = newAssets.InsertarActivo(numero, placa, descripcion, finalIdCompania,
                            finalCompania, finalIdEdificio, finalEdificio, finalIdPiso, finalPiso, finalIdOficina, finalOficina,"0",idEmployees,
                            marca, modelo, serie, epc, finalAssetStatusSysId, finalDetalleEstado, finalEstadoConservacion, AnoFabricacion, Capacidad);

                    handler.post(() -> {
                        progressDialog.dismiss();
                        if(respuesta){
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                            builder.setTitle("Exito");
                            builder.setMessage("Activo creado exitosamente");
                            builder.setPositiveButton("Aceptar", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            builder.show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error al guardar el activo", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Ha ocurrido un error, intente nuevamente", Toast.LENGTH_LONG).show();
        }
    }

        private Button.OnClickListener OnClickListenerIngresar = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                   IngresarActivo(mCrearView);
               }catch (Exception e){
                   e.printStackTrace();
                   Toast.makeText(getApplicationContext(), "Debe ingresar información en los campos", Toast.LENGTH_LONG).show();
               }
            }
        };


   /* private RadioGroup.OnClickListener OnClickListenerPlaca = new View.OnClickListener(){
      @Override
      public void onClick(View v){
          ComprobarPlaca(mCrearView);

      }
    };

    private RadioGroup.OnClickListener OnClickListenerNumero = new View.OnClickListener(){
        @Override
        public void onClick(View v){
             ComprobarNumero(mCrearView);

        }
    };*/
   public View.OnTouchListener onTouchListenerYear  = new View.OnTouchListener() {
       @Override
       public boolean onTouch(View view, MotionEvent motionEvent) {
           if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
               callDatePicker();
           }
           return true;
       }
   };
    private void callDatePicker(){
        final Calendar _myCalendar = Calendar.getInstance();
        final SimpleDateFormat _sdf;
        int yearNow = _myCalendar.get(Calendar.YEAR);
        int monthNow = _myCalendar.get(Calendar.MONTH);
        int dayNow = _myCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog monthDatePickerDialog = new DatePickerDialog(this,
                AlertDialog.THEME_HOLO_DARK , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                text_anno.setText(""+year);
            }
        }, yearNow, monthNow, dayNow){
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getDatePicker().findViewById(getResources().getIdentifier("day","id","android")).setVisibility(View.GONE);
                getDatePicker().findViewById(getResources().getIdentifier("month","id","android")).setVisibility(View.GONE);
            }
        };
        monthDatePickerDialog.setTitle(getString(R.string.title_datepicker_year));
        monthDatePickerDialog.show();


    }
    private void cerrarActivity() {

        try{

            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("¿Realmente desea salir de esta sección?")
                    .setCancelable(false)
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void Message(){
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Message();
            AlertasError("ATENCION", "Si continua podría perder su progreso."+ "\n"+ "¿Realmente desea salir?");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onSupportNavigateUp() {
        Message();
        AlertasError("ATENCION", "Si continua podría perder su progreso."+ "\n"+ "¿Realmente desea salir?");
        return false;
    }

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
}
