package com.example.diverscan.activeid.Locate_Assets;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.Activo.EntidadActivos;
import com.example.diverscan.activeid.Activo.EntidadCategoriaActivos;
import com.example.diverscan.activeid.Activo.EntidadSubActivo;
import com.example.diverscan.activeid.Activo.UpdateAssetAdapter;
import com.example.diverscan.activeid.AssetStatus.AssetStatusDBHerlper;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioRecord;
import com.example.diverscan.activeid.Employees.EmployeesDBHelper;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.FotoActivo.FotosActivosActivity;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import com.example.diverscan.activeid.Oficina.OficinaRecord;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Piso.PisoRecord;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;
import com.example.diverscan.activeid.RazonSocial.RazonSocialRecord;
import com.example.diverscan.activeid.SubActivos.SubActivoActivity;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;
import com.example.diverscan.activeid.Utilities.Fechas;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;
import com.zebra.rfid.api3.TagData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class Actualizar_activo extends AppCompatActivity implements ResponseHandlerInterface {

    //region Variables
    private RadioButton radio_PlacaView;
    private RadioButton radio_NumeroView;
    private RadioButton radio_EPCView;
    private RadioButton radio_Serie;
    private RadioButton radio_Descripcion;
    private EditText NumeroingresadoView;
    private EditText NumeroactivoView;
    private EditText PlacaView;
    private EditText DescripcionView;
    private Spinner CompaniaView;
    private Spinner EdificioView;
    private Spinner PisoView;
    private Spinner OficinaView;
    private EditText EncargadoView;
    private EditText MarcaView;
    private EditText ModeloView;
    private EditText SerieView;
    private EditText DetalleEstado;
    private EditText EPCView;
    private EditText categoriaView;
    private TextView txtSubActivos, txtUbicacion;
    private View mActualizarView;
    private View PanelTabla;
    private View PanelForm;
    private CheckBox checkBoxLocalizar;
    private CheckBox checkBoxRFID;
    private View pnlSubActivo;
    private View pnlActualizaActivo;
    private View pnlVerSubActivo;
    private Spinner spAssetStatus;
    private Spinner spEstadoConservacion;
    private Spinner spEmpleados;
    private EditText txtCodigoEmpleado;
    private EditText text_anno;
    private EditText text_cap;
    private EditText txtDetalleEstado;


    private String[] strEstadoConservacion;
    private List<String> listaEstadoConservacion;
    private ArrayAdapter<String> adapterEstadoConservacion;
    private String preEstadoConservacion;

    Button btn_buscar;
    Button btn_guardar;
    Button btn_reescribir;
    ImageButton btn_VerSubActivo;
    ImageButton btn_VerFotoActivo;
    ImageButton btn_AddSubActivo;
    ImageButton btn_scanner;
    Button btnPruebaLectura;
    Button btn_Antena;
    Button btn_descripcion;
    TagWriter rfidHandler;
    private RecyclerView listDescriptions;
    private RecyclerView listaSubActivos;
    ArrayList<EntidadActivos> activoRecords = new ArrayList<EntidadActivos>();
    Map<String, EntidadActivos> _activosSeleccionado = new HashMap<String, EntidadActivos>();
    Map<String, EntidadActivos> _activosSinSeleccionar = new HashMap<String, EntidadActivos>();
    ArrayList<EntidadCategoriaActivos>  categoriaActivos = new ArrayList<EntidadCategoriaActivos>();
    ArrayList<EntidadSubActivo> _entidadSubActivos = new ArrayList<EntidadSubActivo>();
    //ArrayList<EntidadActivos> activosDescripcion = new ArrayList<EntidadActivos>();

    private Map<Integer, RazonSocialRecord> _mapRazonSociales = new HashMap<Integer, RazonSocialRecord>();
    private Map<Integer, EdificioRecord> _mapEdificios = new HashMap<Integer, EdificioRecord>();
    private Map<Integer, PisoRecord> _mapPisos = new HashMap<Integer, PisoRecord>();
    private Map<Integer, OficinaRecord> _mapOficinas = new HashMap<Integer, OficinaRecord>();
    private String txtDescripcion;
    private boolean _itemSelectedUserCompania, _itemSelectedUserEdificio, _itemSelectedUserPiso = true;
    private Map<Integer, EntidadAssetStatus> _mapEntidadAssetStatus = new HashMap<Integer, EntidadAssetStatus>();
    private Map<Integer, EntidadEmployees>_mapEntidadEmployees = new HashMap<Integer, EntidadEmployees>();
    AssetsDBHelper AssetsDBHelper;
    EmployeesDBHelper employeesDBHelper;
    TagsDBHelper TagsDBHelper;
    AssetStatusDBHerlper assetStatusDBHerlper;
    private RadioButton radio_placa, radio_numero;
    private boolean scannerActivate = false;
    private String numeroingresado, numero, placa, descripcion, compania, idCompania, idEdificio, edificio,
            idPiso, piso, idOficina, oficina, encargado, marca, modelo, serie, epc, tagVerifica, idCategoria, idActivoPadre,
            regrabarTag, AnoFabricacion, Capacidad, _detalleEstado, _estadoConservacion;

    String idCompaniaActivo, idedificioActivo, idpisoActivo, idoficinaActivo;

    private boolean SubActivoOn = false;
    private String _lastTag = "";
    private String _locateTag= "";
    private boolean triggerPressed = false;
    private boolean respuestaActualizado= false;
    private String Power;

    private DatePickerDialog picker;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    private Activity _activity;
    AlertDialog alertDialog;
    //endregion

    //region  Override

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_activo);
        _activity = this;
        controles();
        eventos();
        cargarRazonesSociales();
        cargarUbicaciones();
        cargarEstado();
        CargarEstadoConservacion();
        listDescriptions =  findViewById(R.id.listView);

        rfidHandler = TagWriter.getInstance();

        if (rfidHandler != null && !rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this); // usa GetContext() internamente
        }

        rfidHandler.setResponseHandler(this);
        rfidHandler.onResume();



        /*rfidHandler = new TagWriter() ;

        rfidHandler.onCreate(this);
        rfidHandler.Defaults();
        Toast.makeText(getApplicationContext(), rfidHandler.Defaults(), Toast.LENGTH_LONG).show();*/

        EPCView.setEnabled(false);
        PanelTabla.setVisibility(View.GONE);
        //pnlSubActivo.setVisibility(View.GONE);
        //pnlVerSubActivo.setVisibility(View.GONE);
        //btn_AddSubActivo.setBackgroundResource(R.drawable.ic_add_to_photos_black_24dp);
        //btn_VerSubActivo.setBackgroundResource(R.drawable.ic_visibility_black_24dp);Se comenta por solicitud de Arturo -- Comentado por Jeison
        //btn_DeleteActivo.setBackgroundResource(R.drawable.ic_delete_black_24dp);
        btn_VerFotoActivo.setBackgroundResource(R.drawable.ic_photo_library);
        btn_scanner.setBackgroundResource(R.drawable.verinfo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    //endregion

    //region Controles
    public void controles(){

        mActualizarView = findViewById(R.id.ActualizaForm);
        radio_PlacaView = findViewById(R.id.radio_Placa);
        radio_EPCView = findViewById(R.id.radio_EPC);
        radio_Descripcion = findViewById(R.id.radio_Descripcion);
        radio_Serie = findViewById(R.id.radio_Serie);
        radio_NumeroView = findViewById(R.id.radio_Numero);
        NumeroingresadoView = findViewById(R.id.txt_numero_ingresado);
        NumeroactivoView = findViewById(R.id.txt_numero_activo_actualiza);
        PlacaView = findViewById(R.id.txt_placa_actualiza);
        DescripcionView = findViewById(R.id.txt_descripcion_actualiza);
        CompaniaView = findViewById(R.id.compania_actualiza);
        EdificioView = findViewById(R.id.edificio_actualiza);
        PisoView = findViewById(R.id.piso_actualiza);
        OficinaView = findViewById(R.id.oficina_actualiza);
        btn_scanner = findViewById(R.id.imageButton);
        //EncargadoView = findViewById(R.id.txt_encargado_actualiza);
        MarcaView = findViewById(R.id.txt_marca_actualiza);
        ModeloView = findViewById(R.id.txt_modelo_actualiza);
        SerieView = findViewById(R.id.txt_serie_actualiza);
        EPCView = findViewById(R.id.txt_epc);
        categoriaView = findViewById(R.id.txt_categoria_actualiza);
        //txtSubActivos = findViewById(R.id.txtPoseeSubactivo); Se comenta por solicitud de Arturo -- Comentado por Jeison
        checkBoxRFID = findViewById(R.id.ckRFID);
        AssetsDBHelper = new AssetsDBHelper(mActualizarView.getContext());
        TagsDBHelper = new TagsDBHelper(mActualizarView.getContext());
        //pnlSubActivo = findViewById(R.id.CreaSubActivo);
        //pnlActualizaActivo = findViewById(R.id.ActualizaActivo);
        //pnlVerSubActivo = findViewById(R.id.VerSubActivo);
        txtUbicacion = findViewById(R.id.UptxtSectorBusqueda);
        text_anno = findViewById(R.id.txt_anno);
        text_cap = findViewById(R.id.txt_cap);
        txtDetalleEstado = findViewById(R.id.DetEstado);
        assetStatusDBHerlper = new AssetStatusDBHerlper(mActualizarView.getContext());
        employeesDBHelper = new EmployeesDBHelper(mActualizarView.getContext());
        spAssetStatus = findViewById(R.id.spEstado);
        spEstadoConservacion = findViewById(R.id.spEstadoConservacion);
        spEmpleados =   findViewById(R.id.spEmpleados);
        txtCodigoEmpleado= findViewById(R.id.txtCodigoEmpleado);

        btn_buscar = findViewById(R.id.btn_buscaractivo);
        btn_guardar = findViewById(R.id.btn_Guardaractivo);
        btn_reescribir = findViewById(R.id.btn_Reescribir);
        //btn_AddSubActivo = findViewById(R.id.btn_AgregarSubActivos);
        //btn_VerSubActivo = findViewById(R.id.btn_VerSubActivos);Se comenta por solicitud de Arturo -- Comentado por Jeison
        btn_VerFotoActivo = findViewById(R.id.btn_VerFotos);
        btnPruebaLectura = findViewById(R.id.btnPruebaLectura);
        // btn_descripcion = findViewById(R.id.btn_descripcion);
        // btn_Antena = findViewById(R.id.btn_Antena);

        PanelTabla = findViewById(R.id.buscarActivoDescripcion);

        checkBoxLocalizar = findViewById(R.id.ckLocalizarActivo);
        checkBoxLocalizar.setChecked(false);
        checkBoxRFID.setChecked(true);

    }

    //endregion

    //region eventos
    public void eventos (){
        btn_reescribir.setOnClickListener(OnClickListenerReescribir);
        btn_buscar.setOnClickListener(OnClickListenerBuscarActivo);
        btn_guardar.setOnClickListener(OnClickListenerGuardar);
        CompaniaView.setOnItemSelectedListener(onItemSpinnerListenerCompania);
        EdificioView.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        PisoView.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        //btn_Antena.setOnClickListener(OnClickListenerAntena);
        //btn_descripcion.setOnClickListener(OnClickListenerBuscarDescripcion);
        checkBoxRFID.setOnClickListener(ckActivarRfidOnlClickListener);
        checkBoxLocalizar.setOnClickListener(checkBoxLocalizarOnlClickListener);
        //btn_VerSubActivo.setOnClickListener(OnClickListenerVerSubAsset);Se comenta por solicitud de Arturo -- Comentado por Jeison
        btn_VerFotoActivo.setOnClickListener(OnClickListenerVerFoto);

        text_anno.setOnTouchListener(onTouchListenerYear);
        txtUbicacion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(txtUbicacion.getText().length() > 3){
                    buscarOficinaPorIdPisoNombre();
                }else if(txtUbicacion.getText().length() == 0){
                    PisoRecord pisoRecord= (PisoRecord) PisoView.getSelectedItem();
                    cargarOficinas(pisoRecord.getIdPiso());
                }
            }
        });

        txtCodigoEmpleado.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(txtCodigoEmpleado.getText().length() > 2){
                    buscarEmpleadoPorDescripcion();
                }
            }
        });

    }

    private View.OnClickListener checkBoxLocalizarOnlClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                if(checkBoxLocalizar.isChecked()){
                    if(entidadActivos!=null){
                        rfidHandler.LocateTag(entidadActivos.getTag());
                    }
                }
                else{
                    rfidHandler.StopLocateTag();
                }
            }catch (Exception ex){
                Log.d("Error", ex.getMessage());
            }
        }
    };
    //endregion

    //region ComboBox de Estado de conservacion
     private void CargarEstadoConservacion(){
        listaEstadoConservacion = new ArrayList<>();
        strEstadoConservacion = new String[]{"Conservación Normal", "Como Nuevo", "Necesita Reparos Simples",
                "Necesita Reparos Importantes", "Obsoleto"};
        fillEstadoConservacion(strEstadoConservacion);
    }


     /*private AdapterView.OnItemSelectedListener onItemSelectedListenerEstadoConservacion = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()){
                case R.id.spEstadoConservacion:
                preEstadoConservacion = strEstadoConservacion[position];
                break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };*/

    private void fillEstadoConservacion(String[] EstadoConservacion){
        spEstadoConservacion = findViewById(R.id.spEstadoConservacion);
        spEstadoConservacion.setAdapter(new ArrayAdapter<>(mActualizarView.getContext().getApplicationContext(),
                R.layout.spinner_layaout, EstadoConservacion));
    }
    //endregion

    //region ComboBox de Ubicaciones
    private void cargarOficinasPorPisoNombre(String idPiso,String nombre) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mActualizarView.getContext());
        _mapOficinas = (Map<Integer, OficinaRecord>) oficinaDBHelper.ObtenerOficinaPorPisoDescripcion2(idPiso,nombre);
        OficinaRecord[] oficinas = _mapOficinas.values().toArray(new OficinaRecord[0]);
        fillSpinnerOficina(oficinas);
    }

    private void buscarOficinaPorIdPisoNombre(){
        if (_itemSelectedUserPiso) {
            PisoRecord pisoRecord= (PisoRecord) PisoView.getSelectedItem();
            String nombreBusqueda = txtUbicacion.getText().toString();
            cargarOficinasPorPisoNombre(pisoRecord.getIdPiso(),nombreBusqueda);
        }
        _itemSelectedUserPiso = true;
    }

    private void cargarRazonesSociales() {
        RazonSocialDBHelper razonSocialDBHelper = new RazonSocialDBHelper(mActualizarView.getContext());
        _mapRazonSociales = razonSocialDBHelper.ObtenerSociedades();
        RazonSocialRecord[] razones = _mapRazonSociales.values().toArray(new RazonSocialRecord[0]);
        fillSpinnerRazon(razones);
    }

    private void cargarEdificios(String idCompania) {
        EdificioDBHelper edificioDBHelper = new EdificioDBHelper(mActualizarView.getContext());
        _mapEdificios = edificioDBHelper.ObtenerEdificiosxCompania(idCompania);
        EdificioRecord[] edificios = _mapEdificios.values().toArray(new EdificioRecord[0]);
        fillSpinnerEdificio(edificios);
    }

    private void cargarPisos(String idEdificio) {
        PisoDBHelper pisoDBHelper = new PisoDBHelper(mActualizarView.getContext());
        _mapPisos = pisoDBHelper.ObtenerPisosxEdificio(idEdificio);
        PisoRecord[] pisos = _mapPisos.values().toArray(new PisoRecord[0]);
        fillSpinnerPiso(pisos);
    }

    private void cargarOficinas(String idPiso) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mActualizarView.getContext());
        _mapOficinas = oficinaDBHelper.ObtenerOficinasxPiso(idPiso);
        OficinaRecord[] oficinas = _mapOficinas.values().toArray(new OficinaRecord[0]);
        fillSpinnerOficina(oficinas);
    }

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerCompania = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (_itemSelectedUserCompania) {
                RazonSocialRecord razonSocialRecord= (RazonSocialRecord)CompaniaView.getSelectedItem();
                cargarEdificios(razonSocialRecord.getIdRazon());
                EdificioView.requestFocus();
            }else{
                _itemSelectedUserCompania = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerEdificio = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserEdificio) {
                EdificioRecord edificioRecord= (EdificioRecord)EdificioView.getSelectedItem();
                cargarPisos(edificioRecord.getIdEdificio());
                PisoView.requestFocus();
            }else{
                _itemSelectedUserEdificio = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener onItemSpinnerListenerPiso = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedUserPiso) {
                PisoRecord pisoRecord= (PisoRecord) PisoView.getSelectedItem();
                cargarOficinas(pisoRecord.getIdPiso());
                OficinaView.requestFocus();
            }else{
                _itemSelectedUserPiso = true;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void fillSpinnerRazon(RazonSocialRecord[] razonSocialRecords){
        CompaniaView = findViewById(R.id.compania_actualiza);
        CompaniaView.setAdapter( new ArrayAdapter<>(mActualizarView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,razonSocialRecords));
    }

    private void fillSpinnerEdificio(EdificioRecord[] edificioRecords){
        EdificioView = findViewById(R.id.edificio_actualiza);
        EdificioView.setAdapter( new ArrayAdapter<>(mActualizarView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,edificioRecords));
    }

    private void fillSpinnerPiso(PisoRecord[] pisoRecords){
        PisoView = findViewById(R.id.piso_actualiza);
        PisoView.setAdapter( new ArrayAdapter<>(mActualizarView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,pisoRecords));
    }

    private void fillSpinnerOficina(OficinaRecord[] oficinaRecords){
        OficinaView = findViewById(R.id.oficina_actualiza);
        OficinaView.setAdapter( new ArrayAdapter<>(mActualizarView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,oficinaRecords));
    }
    //endregion

    //region Busqueda de activos
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

    private void buscarEmpleadoPorDescripcion(){
        String nombreBusqueda = txtCodigoEmpleado.getText().toString();
        cargarEmpleadoPorDescripcion(nombreBusqueda);
    }

    private void cargarEmpleadoPorDescripcion(String descripcion) {
        _mapEntidadEmployees = (Map<Integer, EntidadEmployees>) employeesDBHelper.GetEmployees(descripcion);
        EntidadEmployees[] entidadEmployees = _mapEntidadEmployees.values().toArray(new EntidadEmployees[0]);
        fillSpinnerEmployees(entidadEmployees);
    }

    private void fillSpinnerEmployees(EntidadEmployees[] entidadEmployees){
        spEmpleados.setAdapter( new ArrayAdapter<>(this.getApplicationContext()
                ,R.layout.spinner_layaout,entidadEmployees));
        spEmpleados.requestFocus();
    }

    private void cargarUbicaciones() {
        RazonSocialRecord razonSocialRecord = (RazonSocialRecord) CompaniaView.getSelectedItem();
        if (razonSocialRecord != null) {
            idCompania = razonSocialRecord.getIdRazon();
            cargarEdificios(idCompania);

            EdificioRecord edificioRecord = (EdificioRecord) EdificioView.getSelectedItem();
            if (edificioRecord != null) {
                idedificioActivo = edificioRecord.getIdEdificio();
                cargarPisos(idedificioActivo);

                PisoRecord pisoRecord = (PisoRecord) PisoView.getSelectedItem();
                if (pisoRecord != null) {
                    idpisoActivo = pisoRecord.getIdPiso();
                    cargarOficinas(idpisoActivo);
                } else {
                    Log.e("cargarUbicaciones", "pisoRecord es null");
                }
            } else {
                Log.e("cargarUbicaciones", "edificioRecord es null");
            }
        } else {
            Log.e("cargarUbicaciones", "razonSocialRecord es null");
        }
    }


    public EntidadCategoriaActivos categoriaActivosn;
    public EntidadAssetStatus assetStatus;
    public EntidadActivos entidadActivos;
    public EntidadEmployees entidadEmployees;
    public void BuscarActivo (View view) {
        _entidadSubActivos.clear();
        numeroingresado = NumeroingresadoView.getText().toString();
        try {
            if (TextUtils.isEmpty(numeroingresado)) {
                NumeroingresadoView.setError(getString(R.string.activo_required));
                return;
            }

            if (radio_PlacaView.isChecked()) {
                entidadActivos = AssetsDBHelper.VerActivoPlaca(numeroingresado);
            }
            else if(radio_NumeroView.isChecked())
            {
                entidadActivos = AssetsDBHelper.VerActivoNumero(numeroingresado);

            }else if(radio_EPCView.isChecked()){

                entidadActivos = AssetsDBHelper.VerActivoEpc(numeroingresado);
                NumeroingresadoView.getText().clear();
                radio_EPCView.setChecked(false);
                radio_PlacaView.setChecked(true);

            }else if (radio_Serie.isChecked()){
                entidadActivos = AssetsDBHelper.VerActivoSerie(numeroingresado);
            }
            if (entidadActivos == null) {
                NumeroingresadoView.setError(getString(R.string.activo_required));
                return;
            }

            entidadEmployees = employeesDBHelper.GetIdEmployees(entidadActivos.getEmployeeRelatedSysId());
            assetStatus = assetStatusDBHerlper.GetAssetStatusbyId(entidadActivos.getAssetStatusSysId());
            Cursor cursor = AssetsDBHelper.ObtenerSubActivos(entidadActivos.getIdActivo());
            int count = cursor.getCount();

            if (count != 0){
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    String _id = cursor.getString(cursor.getColumnIndex("_assetSysId"));
                    String numero = cursor.getString(cursor.getColumnIndex("_assetNumber"));
                    String placa = cursor.getString(cursor.getColumnIndex("_assetBarcode"));
                    String descripcion = cursor.getString(cursor.getColumnIndex("_assetDescription"));
                    String encargado = cursor.getString(cursor.getColumnIndex("_assetAttendant"));
                    String epc = cursor.getString(cursor.getColumnIndex("_assetTag"));
                    String parentAssetSysId = cursor.getString(cursor.getColumnIndex("parentAssetSysId"));
                    EntidadSubActivo entidadSubActivos = new EntidadSubActivo(_id, parentAssetSysId, encargado,
                            descripcion, epc, numero, placa);
                    _entidadSubActivos.add(entidadSubActivos);
                }
            }

            idActivoPadre= entidadActivos.getIdActivo();
            // if(_entidadSubActivos.size() > 0) {
            //   String totalSubActivos = String.valueOf(_entidadSubActivos.size());
            // txtSubActivos.setText("Posee " + totalSubActivos + " subactivos");
            //}else{
            //  txtSubActivos.setText("No posee subactivos");
            //}
            //NumeroingresadoView.setText(entidadActivos.getNumero());
            NumeroactivoView.setText(entidadActivos.getNumero());
            PlacaView.setText(entidadActivos.getCodeBar());
            DescripcionView.setText(entidadActivos.getDescripcion());
            txtCodigoEmpleado.setText(entidadEmployees.getId());
            String nombreCategoria;
            idCategoria = entidadActivos.getIdCategoria(); //Se obtiene el id de la categoría del activo buscado
            categoriaActivosn = AssetsDBHelper.NombreCategoria(idCategoria);// se busca el nombre de la categoría
            nombreCategoria = categoriaActivosn.getName();
            categoriaView.setText(nombreCategoria);

            txtCodigoEmpleado.setText("");
            //EncargadoView.setText(entidadActivos.getAlias());
            MarcaView.setText(entidadActivos.getMarca());
            ModeloView.setText(entidadActivos.getModelo());
            SerieView.setText(entidadActivos.getSerial());
            EPCView.setText(entidadActivos.getTag());
            tagVerifica = EPCView.getText().toString();
            text_anno.setText(entidadActivos.getAnoFabricacion());
            text_cap.setText(entidadActivos.getCapacidad());
            txtDetalleEstado.setText(entidadActivos.getEstadoDescripcion());


            idCategoria = entidadActivos.getIdCategoria();

            String estado = assetStatus.getId();

            for (int i = 0; i < strEstadoConservacion.length; i++) {
                if (spEstadoConservacion.getItemAtPosition(i).equals(entidadActivos.getEstadoConservacion())) {
                    spEstadoConservacion.setSelection(i);
                    break;
                }
            }

            for(Entry<Integer, EntidadAssetStatus> item : _mapEntidadAssetStatus.entrySet()){
                if(item.getValue().getId().equals(estado)){
                    spAssetStatus.setSelection(item.getKey());
                }
            }

            idCompaniaActivo = entidadActivos.getIdCompania();
            for(Entry<Integer, RazonSocialRecord> item : _mapRazonSociales.entrySet()){
                if(item.getValue().getIdRazon().equals(idCompaniaActivo)){
                    CompaniaView.setSelection(item.getKey());
                }
            }

            idedificioActivo = entidadActivos.getIdEdificio();
            cargarEdificios(idCompaniaActivo);
            for(Entry<Integer, EdificioRecord> item : _mapEdificios.entrySet()){
                if(item.getValue().getIdEdificio().equals(idedificioActivo)){
                    EdificioView.setSelection(item.getKey());
                }
            }

            idpisoActivo = entidadActivos.getIdPiso();
            cargarPisos(idedificioActivo);
            for(Entry<Integer, PisoRecord> item : _mapPisos.entrySet()){
                if(item.getValue().getIdPiso().equals(idpisoActivo)){
                    PisoView.setSelection(item.getKey());
                }
            }

            idoficinaActivo = entidadActivos.getIdOficina();
            cargarOficinas(idpisoActivo);
            for(Entry<Integer, OficinaRecord> item : _mapOficinas.entrySet()){
                if(item.getValue().getIdOficina().equals(idoficinaActivo)){
                    OficinaView.setSelection(item.getKey());
                }
            }

            NumeroactivoView.setEnabled(false);
            PlacaView.setEnabled(true);
            DescripcionView.setEnabled(true);
            CompaniaView.setEnabled(true);
            EdificioView.setEnabled(true);
            PisoView.setEnabled(true);
            OficinaView.setEnabled(true);
         //   EncargadoView.setEnabled(true);
            MarcaView.setEnabled(true);
            ModeloView.setEnabled(true);
            SerieView.setEnabled(true);
            EPCView.setEnabled(false);
        }
        catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Error al buscar el Activo", Toast.LENGTH_LONG).show();
        }
    }



    public void inflateListViewActivos(ArrayList<EntidadActivos> response){
       // ItemAdapterAssets itemAdapterAssets = new ItemAdapterAssets(this, response);
        //int aux = itemAdapterAssets.getCount();
        try{
            listDescriptions.setLayoutManager(new LinearLayoutManager(this));
            UpdateAssetAdapter itemAdapterAssets = new UpdateAssetAdapter(response);
            listDescriptions.setAdapter(itemAdapterAssets);
            itemAdapterAssets.setOnItemClickListener(new UpdateAssetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {

                    String id = activoRecords.get(position).getIdActivo();
                    String descripcion = activoRecords.get(position).getDescripcion();
                    String idCompania = activoRecords.get(position).getIdCompania();
                    String Compania = activoRecords.get(position).getCompania();
                    String idEdificio = activoRecords.get(position).getIdEdificio();
                    String Edificio = activoRecords.get(position).getEdificio();
                    String idPiso = activoRecords.get(position).getIdPiso();
                    String Piso = activoRecords.get(position).getPiso();
                    String idOficina = activoRecords.get(position).getIdOficina();
                    String Oficina = activoRecords.get(position).getOficina();
                    String EPC = activoRecords.get(position).getTag();
                    String numero = activoRecords.get(position).getNumero();
                    String placa = activoRecords.get(position).getCodeBar();
                    String marca = activoRecords.get(position).getMarca();
                    String modelo = activoRecords.get(position).getModelo();
                    String serie = activoRecords.get(position).getSerial();
                    String encargado = activoRecords.get(position).getAlias();
                    String annoFbricacion = activoRecords.get(position).getAnoFabricacion();
                    String capacidad = activoRecords.get(position).getCapacidad();
                    String EstadoConservacion = activoRecords.get(position).getEstadoConservacion();
                    String DetalleEstado = activoRecords.get(position).getEstadoDescripcion();



                    idActivoPadre = id;
                    String nombreCategoria;
                    idCategoria = activoRecords.get(position).getIdCategoria(); //Se obtiene el id de la categoría del activo buscado
                    categoriaActivosn = AssetsDBHelper.NombreCategoria(idCategoria);// se busca el nombre de la categoría
                    nombreCategoria = categoriaActivosn.getName();
                    categoriaView.setText(nombreCategoria);


                    entidadEmployees = employeesDBHelper.GetIdEmployees(activoRecords.get(position).getEmployeeRelatedSysId());
                    assetStatus = assetStatusDBHerlper.GetAssetStatusbyId(activoRecords.get(position).getAssetStatusSysId());
                    Cursor cursor = AssetsDBHelper.ObtenerSubActivos(activoRecords.get(position).getIdActivo());
                    int count = cursor.getCount();

                    if (count != 0){
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                            String _idsa = cursor.getString(cursor.getColumnIndex("_assetSysId"));
                            String numerosa = cursor.getString(cursor.getColumnIndex("_assetNumber"));
                            String placasa = cursor.getString(cursor.getColumnIndex("_assetBarcode"));
                            String descripcionsa = cursor.getString(cursor.getColumnIndex("_assetDescription"));
                            String encargadosa = cursor.getString(cursor.getColumnIndex("_assetAttendant"));
                            String epcsa = cursor.getString(cursor.getColumnIndex("_assetTag"));
                            String parentAssetSysId = cursor.getString(cursor.getColumnIndex("parentAssetSysId"));
                            EntidadSubActivo entidadSubActivos = new EntidadSubActivo(_idsa, parentAssetSysId, encargadosa,
                                    descripcionsa, epcsa, numerosa, placasa);
                            _entidadSubActivos.add(entidadSubActivos);
                        }
                    }

                    /*if(_entidadSubActivos.size() > 1) {
                        String totalSubActivos = String.valueOf(_entidadSubActivos.size());
                        txtSubActivos.setText("Posee " + totalSubActivos + " subactivos");
                    }else{
                        txtSubActivos.setText("No posee subactivos");
                    }*/

                    text_anno.setText(annoFbricacion);
                    text_cap.setText(capacidad);
                    txtDetalleEstado.setText(DetalleEstado);
                    NumeroactivoView.setText(numero);
                    PlacaView.setText(placa);
                    DescripcionView.setText(descripcion);
                    //EncargadoView.setText(encargado);
                    MarcaView.setText(marca);
                    ModeloView.setText(modelo);
                    SerieView.setText(serie);
                    EPCView.setVisibility(View.GONE);
                    tagVerifica = EPCView.getText().toString();
                    txtCodigoEmpleado.setText(entidadEmployees.getId());

                    for (int i = 0; i < strEstadoConservacion.length; i++) {
                        if (spEstadoConservacion.getItemAtPosition(i).equals(EstadoConservacion)) {
                            spEstadoConservacion.setSelection(i);
                            break;
                        }
                    }

                    String estado = assetStatus.getId();
                    for(Entry<Integer, EntidadAssetStatus> item : _mapEntidadAssetStatus.entrySet()){
                        if(item.getValue().getId().equals(estado)){
                            spAssetStatus.setSelection(item.getKey());
                        }
                    }

                    idCompaniaActivo = idCompania;
                    for (Entry<Integer, RazonSocialRecord> item : _mapRazonSociales.entrySet()) {
                        if (item.getValue().getIdRazon().equals(idCompaniaActivo)) {
                            CompaniaView.setSelection(item.getKey());
                        }
                    }

                    idedificioActivo = idEdificio;
                    cargarEdificios(idCompaniaActivo);
                    for (Entry<Integer, EdificioRecord> item : _mapEdificios.entrySet()) {
                        if (item.getValue().getIdEdificio().equals(idedificioActivo)) {
                            _itemSelectedUserCompania = false;
                            EdificioView.setSelection(item.getKey());
                        }
                    }

                    idpisoActivo = idPiso;
                    cargarPisos(idedificioActivo);
                    for (Entry<Integer, PisoRecord> item : _mapPisos.entrySet()) {
                        if (item.getValue().getIdPiso().equals(idpisoActivo)) {
                            _itemSelectedUserEdificio = false;
                            PisoView.setSelection(item.getKey());
                        }
                    }

                    idoficinaActivo = idOficina;
                    cargarOficinas(idpisoActivo);
                    for (Entry<Integer, OficinaRecord> item : _mapOficinas.entrySet()) {
                        if (item.getValue().getIdOficina().equals(idoficinaActivo)) {
                            _itemSelectedUserPiso = false;
                            OficinaView.setSelection(item.getKey());
                        }
                    }

                    PlacaView.setEnabled(true);
                    DescripcionView.setEnabled(true);
                    CompaniaView.setEnabled(true);
                    EdificioView.setEnabled(true);
                    PisoView.setEnabled(true);
                    OficinaView.setEnabled(true);
                    //EncargadoView.setEnabled(true);
                    MarcaView.setEnabled(true);
                    ModeloView.setEnabled(true);
                    SerieView.setEnabled(true);
                    EPCView.setVisibility(View.GONE);
                    PanelTabla.setVisibility(View.GONE);

                    activoRecords.clear();

                }
            });

            /*listDescriptions.addOnItemTouchListener(new RecyclerTouchListener(this, listDescriptions, new UpdateAssetAdapter.OnLongPress() {
                @Override
                public void onClicPress(View view, int position) {

            }

                @Override
                public void onLongPress(View view, int position) {
                    int pos = position;

                }
            }));*/

        }catch(Exception ex) {
          ex.printStackTrace();
        }
    }


    public void CargarActivos(View view){
        activoRecords.clear();
        String txtDescripcion = NumeroingresadoView.getText().toString();

        if(TextUtils.isEmpty(txtDescripcion)){
            NumeroingresadoView.setError("Debe ingresar una descripción correcta");
        }else{
            Cursor cursor = AssetsDBHelper.TraerActivos(txtDescripcion);
            int count = cursor.getCount();
            if (count != 0){
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

                    String _id = cursor.getString(cursor.getColumnIndex("_id"));
                    String numero = cursor.getString(cursor.getColumnIndex("Numero"));
                    String placa = cursor.getString(cursor.getColumnIndex("CodeBar"));
                    String descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
                    String idCompania = cursor.getString(cursor.getColumnIndex("IdCompania"));
                    String compania = cursor.getString(cursor.getColumnIndex("Compania"));
                    String idEdicio = cursor.getString(cursor.getColumnIndex("IdEdificio"));
                    String edificio = cursor.getString(cursor.getColumnIndex("Edificio"));
                    String idPiso = cursor.getString(cursor.getColumnIndex("IdPiso"));
                    String piso = cursor.getString(cursor.getColumnIndex("Piso"));
                    String idOficina = cursor.getString(cursor.getColumnIndex("IdOficina"));
                    String oficina = cursor.getString(cursor.getColumnIndex("Oficina"));
                    String encargado = cursor.getString(cursor.getColumnIndex("Alias"));
                    String marca = cursor.getString(cursor.getColumnIndex("Marca"));
                    String modelo = cursor.getString(cursor.getColumnIndex("Modelo"));
                    String serie = cursor.getString(cursor.getColumnIndex("Serial"));
                    String epc = cursor.getString(cursor.getColumnIndex("Tag"));
                    String IdCategoria = cursor.getString(cursor.getColumnIndex("IdCategoria"));
                    String employeeRelated = cursor.getString(cursor.getColumnIndex("EmployeeRelatedSysId"));
                    String assetStatusSysId = cursor.getString(cursor.getColumnIndex("AssetStatusSysId"));
                    String parentAssetSysId = cursor.getString(cursor.getColumnIndex("parentAssetSysId"));
                    String anoFabricacion = cursor.getString(cursor.getColumnIndex("AnoFabricacion"));
                    String capacidad = cursor.getString(cursor.getColumnIndex("Capacidad"));
                    String estadoDescripcion = cursor.getString(cursor.getColumnIndex("EstadoDescripcion"));
                    String estadoConservacion = cursor.getString(cursor.getColumnIndex("EstadoConservacion"));
                    EntidadActivos entidadActivos = new EntidadActivos(_id, descripcion, compania,
                            idCompania, edificio, idEdicio, piso, idPiso, oficina, idOficina, epc,
                            numero, placa, marca, modelo, serie, encargado,IdCategoria, employeeRelated,
                            assetStatusSysId, parentAssetSysId,anoFabricacion,capacidad,estadoDescripcion,estadoConservacion);
                    activoRecords.add(entidadActivos);
                }
                inflateListViewActivos(activoRecords);
            }else{
                Toast.makeText(getApplicationContext(), "El activo no existe", Toast.LENGTH_LONG).show();
            }
            if(!cursor.isClosed()){
                cursor.close();
            }
        }
    }

    //endregion

    //region reescribirTag
    public void ReescribirEpc (View view){
        try{
            if (!triggerPressed) {
                try{
                    EPCView.requestFocus();
                    rfidHandler.setAccessOperationConfiguration();
                    //String SourceEPC = "FF3620180831145052519399";
                    String Consecutivo = PlacaView.getText().toString();
                    int TamConsec = Consecutivo.length();

                    String EPCToWrite =  "A00000000000000000000000";// + PlacaView.getText().toString();
                    int TamEPCToWrite = (EPCToWrite.length()) - TamConsec;

                    String EPCToWriteEnd = EPCToWrite.substring(0, TamEPCToWrite);

                    String EPCnew = tagVerifica;// NumeroingresadoView.getText().toString();//EPCToWriteEnd + Consecutivo;

                    boolean result = rfidHandler.WriteTag(_lastTag, EPCnew);
                    if(result == false){
                        Toast.makeText(view.getContext(), "No se ha leído ningún tag", Toast.LENGTH_LONG).show();
                    }else if (result) {
                        Toast.makeText(view.getContext(), _lastTag + ", "+ "este Tag ha cambiado por: " + EPCnew, Toast.LENGTH_LONG).show();
                        EPCView.setText(EPCnew);
                    }else{
                        Toast.makeText(view.getContext(), "Error al grabar", Toast.LENGTH_LONG).show();
                    }
                }catch(Exception ex){
                    Toast.makeText(getApplicationContext(), "Se ha presentado un error intente nuevamente", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(view.getContext(), "Debe de soltar el gatillo para poder grabar", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Se ha presentado un error", Toast.LENGTH_LONG).show();
        }
    }
    //endregion

    //region Actualizar Activo
    public EntidadTiposTags entidadTiposTags;
    public void ActualizarActivo (View view) {
        try{

            //String EpcActivo = entidadActivos.getTag();
            String tag = EPCView.getText().toString(); //Epc que se encuentra en la vista, puede ser el último que leyó o el que se buscó

            //if(tag.equals(EpcActivo)){

                numero = NumeroactivoView.getText().toString();
                placa = PlacaView.getText().toString();

                descripcion = DescripcionView.getText().toString();

                RazonSocialRecord razonSocialRecord = (RazonSocialRecord)CompaniaView.getSelectedItem();
                idCompania = razonSocialRecord.getIdRazon();
                compania = razonSocialRecord.getNombreRazon();

                EdificioRecord edificioRecord = (EdificioRecord)EdificioView.getSelectedItem();
                idEdificio = edificioRecord.getIdEdificio();
                edificio = edificioRecord.getNombreEdificio();

                PisoRecord pisoRecord = (PisoRecord)PisoView.getSelectedItem();
                idPiso = pisoRecord.getIdPiso();
                piso = pisoRecord.getNombrePiso();

                OficinaRecord oficinaRecord = (OficinaRecord)OficinaView.getSelectedItem();
                idOficina = oficinaRecord.getIdOficina();
                oficina = oficinaRecord.getNombreOficina();

                EntidadEmployees employees = (EntidadEmployees)spEmpleados.getSelectedItem();
                String idEmployees = employees.getEmployeeSysId();
                String nameEmployees = employees.getName();

                EntidadAssetStatus entidadAssetStatus = (EntidadAssetStatus)spAssetStatus.getSelectedItem();
                String idStatus = entidadAssetStatus.getId();

                encargado = "";//EncargadoView.getText().toString();
                marca = MarcaView.getText().toString();
                modelo = ModeloView.getText().toString();
                serie = SerieView.getText().toString();
                epc = EPCView.getText().toString();
                AnoFabricacion = text_anno.getText().toString();
                Capacidad = text_cap.getText().toString();
                _detalleEstado = txtDetalleEstado.getText().toString();
                _estadoConservacion = spEstadoConservacion.getSelectedItem().toString();

                int tamanoPlaca = placa.length();
                if(!TextUtils.isEmpty(PlacaView.getText().toString()) || TextUtils.isEmpty(NumeroactivoView.getText().toString())) {
                   // if(tamanoPlaca == 7) {
                        respuestaActualizado = AssetsDBHelper.ActualizarActivo(numero, placa, descripcion, idCompania, compania,
                                idEdificio, edificio, idPiso, piso, idOficina, oficina, marca, modelo, serie, epc,
                                idEmployees, idStatus, nameEmployees, AnoFabricacion, Capacidad, _detalleEstado, _estadoConservacion);

                        if (respuestaActualizado) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                            builder.setIcon(R.drawable.alertaicono);
                            builder.setTitle("ACTUALIZADO");
                            builder.setMessage("El activo:" + descripcion + "." + "\n" + "Placa No: " + placa + ";" + "\n"
                                    + "Ha sido actualizado correctamente!");
                            builder.setCancelable(true);
                            final AlertDialog closedialog = builder.create();
                            closedialog.show();
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    closedialog.dismiss();
                                    timer.cancel();
                                }
                            }, 4000);

                            NumeroactivoView.setText("");
                            PlacaView.setText("");
                            DescripcionView.setText("");
                            cargarRazonesSociales();
                           // EncargadoView.setText("");
                            MarcaView.setText("");
                            ModeloView.setText("");
                            SerieView.setText("");
                            categoriaView.setText("");
                            EPCView.setText("");
                            NumeroingresadoView.requestFocus();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                            builder.setIcon(R.drawable.alertaicono);
                            builder.setTitle("ALERTA");
                            builder.setMessage("El activo:" + descripcion + "." + "\n" + "Placa No: " + placa + ";" + "\n"
                                    + "No se ha podido actualizar, verifique los campos");
                            builder.setCancelable(true);
                            final AlertDialog closedialog = builder.create();
                            closedialog.show();
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    closedialog.dismiss();
                                    timer.cancel();
                                }
                            }, 4000);
                        }
                   /* }
                    else{
                        MessageError();
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                        builder.setIcon(R.drawable.alertaicono);
                        builder.setTitle("ALERTA");
                        builder.setMessage("Verifique el número placa");
                        builder.setCancelable(true);
                        final AlertDialog closedialog = builder.create();
                        closedialog.show();
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                closedialog.dismiss();
                                timer.cancel();
                            }
                        }, 4000);

                    }*/
                }else{
                    MessageError();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                    builder.setIcon(R.drawable.alertaicono);
                    builder.setTitle("ALERTA");
                    builder.setMessage("Debe ingresar información en los campos antes de actualizar");
                    builder.setCancelable(true);
                    final AlertDialog closedialog = builder.create();
                    closedialog.show();
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            closedialog.dismiss();
                            timer.cancel();
                        }
                    }, 4000);
                }
            /*}else{
                String nombreTipoTag;
                String nombreCategoria;
                idCategoria = entidadActivos.getIdCategoria(); //Se obtiene el id de la categoría del activo buscado
                categoriaActivosn = AssetsDBHelper.NombreCategoria(idCategoria);// se busca el nombre de la categoría
                nombreCategoria = categoriaActivosn.getName();

                entidadTiposTags = AssetsDBHelper.NombreTipoTag(tag);//se busca el nombre del tipo de tag, por medio del epc que se encuentra en el epc view
                nombreTipoTag = entidadTiposTags.getName();

                if (nombreTipoTag.equals("Sin Asignar")){
                   AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                   builder.setIcon(R.drawable.alertaicono);
                   builder.setTitle("ALERTA");
                   builder.setMessage("El tag tag debe estar clasificado.");
                   builder.setCancelable(true);
                   final AlertDialog closedialog = builder.create();
                   closedialog.show();
                   final Timer timer = new Timer();
                   timer.schedule(new TimerTask() {
                       @Override
                       public void run() {
                           closedialog.dismiss();
                           timer.cancel();
                       }
                   }, 4000);
                }else{

                    if(nombreCategoria.equals(nombreTipoTag)){

                        numero = NumeroactivoView.getText().toString();
                        placa = PlacaView.getText().toString();

                        descripcion = DescripcionView.getText().toString();

                        RazonSocialRecord razonSocialRecord = (RazonSocialRecord)CompaniaView.getSelectedItem();
                        idCompania = razonSocialRecord.getIdRazon();
                        compania = razonSocialRecord.getNombreRazon();

                        EdificioRecord edificioRecord = (EdificioRecord)EdificioView.getSelectedItem();
                        idEdificio = edificioRecord.getIdEdificio();
                        edificio = edificioRecord.getNombreEdificio();

                        PisoRecord pisoRecord = (PisoRecord)PisoView.getSelectedItem();
                        idPiso = pisoRecord.getIdPiso();
                        piso = pisoRecord.getNombrePiso();

                        OficinaRecord oficinaRecord = (OficinaRecord)OficinaView.getSelectedItem();
                        idOficina = oficinaRecord.getIdOficina();
                        oficina = oficinaRecord.getNombreOficina();

                        encargado = EncargadoView.getText().toString();
                        marca = MarcaView.getText().toString();
                        modelo = ModeloView.getText().toString();
                        serie = SerieView.getText().toString();
                        epc = EPCView.getText().toString();

                        Boolean respuestaActualiza = AssetsDBHelper.ActualizarActivo(numero, placa, descripcion, idCompania, compania,
                                idEdificio, edificio, idPiso, piso, idOficina, oficina, encargado, marca, modelo, serie, epc);

                        if (respuestaActualiza) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                            builder.setIcon(R.drawable.alertaicono);
                            builder.setTitle("ACTUALIZADO");
                            builder.setMessage("El activo:" + descripcion + ";" + "\n" + "Placa No.: " + placa + ";" + "\n"
                                    + "Ha sido actualizado correctamente");
                            builder.setCancelable(true);
                            final AlertDialog closedialog = builder.create();
                            closedialog.show();
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    closedialog.dismiss();
                                    timer.cancel();
                                }
                            }, 4000);

                            NumeroactivoView.setText("");
                            PlacaView.setText("");
                            DescripcionView.setText("");
                            cargarRazonesSociales();
                            EncargadoView.setText("");
                            MarcaView.setText("");
                            ModeloView.setText("");
                            SerieView.setText("");
                            categoriaView.setText("");
                            EPCView.setText("");
                            NumeroingresadoView.requestFocus();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                            builder.setIcon(R.drawable.alertaicono);
                            builder.setTitle("ALERTA");
                            builder.setMessage("El activo:" + descripcion + ";" + "\n" + "Placa No.: " + placa + ";" + "\n"
                                    + "No se ha podido actualizar correctamente, verifique los campos");
                            builder.setCancelable(true);
                            final AlertDialog closedialog = builder.create();
                            closedialog.show();
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    closedialog.dismiss();
                                    timer.cancel();
                                }
                            }, 4000);
                        }
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                        builder.setIcon(R.drawable.alertaicono);
                        builder.setTitle("ALERTA");
                        builder.setMessage("La categoría tag:" + nombreTipoTag +" , no pertenecen a " +
                                "la misma categoría del activo: "+ nombreCategoria +".");
                        builder.setCancelable(true);
                        final AlertDialog closedialog = builder.create();
                        closedialog.show();
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                closedialog.dismiss();
                                timer.cancel();
                            }
                        }, 4000);
                    }
                }
            }*/
        }catch(Exception e){
            AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
            builder.setIcon(R.drawable.alertaicono);
            builder.setTitle("ALERTA");
            builder.setMessage("Ha ocurrido un error inesperado al actualizar el activo, verifique la " +
                    "información ingresada e intente nuevamente.");
            builder.setCancelable(true);
            final AlertDialog closedialog = builder.create();
            closedialog.show();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    closedialog.dismiss();
                    timer.cancel();
                }
            }, 5000);
        }
    }
    //endregion

    //region Botones y Alertas

    private View.OnClickListener ckActivarRfidOnlClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkBoxRFID.isChecked()){
                iniciarRFID();
            }
            else{
                desconectarRFID();
                NumeroingresadoView.requestFocus();
            }
        }
    };

    private Button.OnClickListener OnClickListenerBuscarActivo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

           if(radio_Descripcion.isChecked()){
              if(activoRecords.isEmpty()){
                  NumeroactivoView.setText("");
                  PlacaView.setText("");
                  DescripcionView.setText("");
//                EncargadoView.setText("");
                  MarcaView.setText("");
                  ModeloView.setText("");
                  SerieView.setText("");
                  EPCView.setText("");
                  categoriaView.setText("");
                  CargarActivos(mActualizarView);
                  PanelTabla.setVisibility(View.VISIBLE);
              }else{
                  activoRecords.clear();
                  NumeroactivoView.setText("");
                  PlacaView.setText("");
                  DescripcionView.setText("");
                 // EncargadoView.setText("");
                  MarcaView.setText("");
                  ModeloView.setText("");
                  SerieView.setText("");
                  EPCView.setText("");
                  categoriaView.setText("");
                  CargarActivos(mActualizarView);
                  PanelTabla.setVisibility(View.VISIBLE);
              }
           }
           else {
               PanelTabla.setVisibility(View.GONE);
               NumeroactivoView.setText("");
               PlacaView.setText("");
               DescripcionView.setText("");
//               EncargadoView.setText("");
               MarcaView.setText("");
               ModeloView.setText("");
               SerieView.setText("");
               EPCView.setText("");
               categoriaView.setText("");
               _itemSelectedUserCompania = false;
               _itemSelectedUserEdificio = false;
               _itemSelectedUserPiso = false;
               BuscarActivo(mActualizarView);

           }
        }};

    private Button.OnClickListener OnClickListenerGuardar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(TextUtils.isEmpty(NumeroactivoView.getText()) && TextUtils.isEmpty(NumeroactivoView.getText())
             && TextUtils.isEmpty(PlacaView.getText())){
                AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ALERTA");
                builder.setMessage("No hay activos para actualizar.");
                builder.setCancelable(true);
                final AlertDialog closedialog = builder.create();
                closedialog.show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        closedialog.dismiss();
                        timer.cancel();
                    }
                }, 4000);
            }else{
                ActualizarActivo(mActualizarView);
            }
        }};




    private Button.OnClickListener OnClickListenerVerFoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(PlacaView.getText().toString())){

                    Intent intent = new Intent(Actualizar_activo.this, FotosActivosActivity.class);
                    intent.putExtra("placaActivo", PlacaView.getText().toString());
                    intent.putExtra("_idActivo", idActivoPadre);
                    intent.putExtra("_assetDescription", DescripcionView.getText().toString());
                    startActivity(intent);

            }else{
                MessageError();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ALERTA");
                builder.setMessage("Debe buscar un activo antes de visualizar o agregar una foto.");
                builder.setCancelable(true);
                final AlertDialog closedialog = builder.create();
                closedialog.show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        closedialog.dismiss();
                        timer.cancel();
                    }
                }, 5000);
            }
        }
    };



    /*private Button.OnClickListener OnClickListenerVerSubAsset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(PlacaView.getText().toString())){
                    Intent intent = new Intent(Actualizar_activo.this, SubActivoActivity.class);
                    intent.putExtra("SubActivos", _entidadSubActivos);
                    intent.putExtra("_idActivo", idActivoPadre);
                    startActivity(intent);
            }else{
                MessageError();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActualizarView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ALERTA");
                builder.setMessage("Debe buscar un activo antes de visualizar o agregar un subactivo.");
                builder.setCancelable(true);
                final AlertDialog closedialog = builder.create();
                closedialog.show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        closedialog.dismiss();
                        timer.cancel();
                    }
                }, 5000);
            }
        }
    };*/

    private void Reescribe() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.alertaicono)
                .setTitle("Advertencia")
                .setMessage("Este activo ya cuenta con un tag. ¿Desea continuar?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReescribirEpc(mActualizarView);
                    }
                }).show();
    }


    private Button.OnClickListener OnClickListenerReescribir =  new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            String verifica = "Sin Asignar";

            if(tagVerifica.equals(verifica)){

             ReescribirEpc(mActualizarView);

            }else{
                Reescribe();
            }
        }};

    public View.OnTouchListener onTouchListenerYear  = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                callDatePicker();
            }
            return true;
        }
    };


    //endregion

    //region Metodos RFID
    private void iniciarRFID(){
        try{

            rfidHandler.setTriggerMode("RFID");
            scannerActivate = false;
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    private void desconectarRFID(){
        try{

            rfidHandler.setTriggerMode("BARCODE");
            scannerActivate = true;
            //sdkHandler.dcssdkGetActiveScannersList();
        }catch (Exception ex){
            ex.printStackTrace();
        }
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

    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    public void Message(){
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    public void MessageError(){
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 94);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
       String status = rfidHandler.onResume();
       Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

     int count = 0 ;
     short _distance = 0;
     int quantityToSound = 10;
    @Override
    public void handleTagdata(TagData[] tagData) {
        Log.d("RFID_SAMPLE", "handleTagdata invocado, cantidad de tags: " + tagData.length);

        if(!scannerActivate){
            final StringBuilder sb = new StringBuilder();
            for (int index = 0; index < tagData.length; index++) {
                _lastTag = tagData[index].getTagID();
                Log.d("RFID_SAMPLE", "Tag leído: " + _lastTag); // ← log por cada tag

                if (tagData[index].isContainsLocationInfo()) {
                    int tag = index;
                    short distance = tagData[tag].LocationInfo.getRelativeDistance();
                    Log.d("RFID_SAMPLE", "Distancia relativa: " + distance);

                    if (distance > 0) {
                        sb.append(distance + "\n");
                        _distance = distance;
                    }
                }
            }

            double duracion = ((double)14)-(((double)14/(double)100)*(double)_distance);
            quantityToSound = (int) duracion;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(sb.length() > 0) {
                        count++;
                        if(count > quantityToSound) {
                            Message();
                            count = 0;
                        }
                    } else {
                        if(radio_EPCView.isChecked()){
                            NumeroingresadoView.setInputType(InputType.TYPE_NULL);
                            NumeroingresadoView.setText(_lastTag);
                        } else {
                            EPCView.setText(_lastTag);
                            EPCView.requestFocus();
                        }
                    }
                }
            });
        } else {
            Log.d("BARCODE-Actualizar_activo-handleTagdata", "Tag ID :" + tagData[0].getTagID());
        }
    }
    @Override
    public void handleTriggerPress(boolean pressed) {
        try{
            triggerPressed = pressed;
            if (pressed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        EPCView.setText("");
                    }
                });
                rfidHandler.performInventory();
            } else
                rfidHandler.stopInventory();
        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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
                //EPCView.setText(text);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
    //endregion

}