package com.example.diverscan.activeid.SubActivos;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.Activo.EntidadSubActivo;
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
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.newAssets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.diverscan.activeid.R.id.lbl_numero_activo_crea;

public class SubActivoActivity extends AppCompatActivity implements CallBackAssetTouch{

    private View pnlSubActivo;
    private View rcvSubActivo;
    private View pnlActualizaActivo;
    private View pnlVerSubActivo;
    private View mSubActivoView;

    private EditText txtCodigoEmpleado;
    private EditText NumeroingresadoView;
    private EditText NumeroactivoView;
    private EditText PlacaView;
    private EditText DescripcionView;
    private Spinner CompaniaView;
    private Spinner EdificioView;
    private Spinner PisoView;
    private Spinner OficinaView;
    private Spinner PreDescripciones;
    private Spinner spEmpleados;
    private Spinner spAssetStatus;

    private EditText EncargadoView;
    private EditText MarcaView;
    private EditText ModeloView;
    private EditText SerieView;
    private EditText EPCView;
    private TextView LabelNumero;
    private TextView LabelPlaca;


    ImageButton btn_VerSubActivo;
    ImageButton btn_AddSubActivo;
    ImageButton btn_DeleteActivo;

    RelativeLayout rlsnackbar;
    TextView txtNotificacion;

    private boolean SubActivoOn = false;
    private Boolean press = false;
    private RecyclerView listaSubActivos;
    Button btn_Crear;
    private String _idActivoPadre;

    ArrayList<EntidadSubActivo> _entidadSubActivos = new ArrayList<EntidadSubActivo>();
    AdapterSubActivoSwipe _adapterSubActivoSwipe =  new AdapterSubActivoSwipe();

    private String preDescripcion;

    private Map<Integer, RazonNuevo> _mapRazonSociales = new HashMap<Integer, RazonNuevo>();
    private Map<Integer, EdificioNuevo> _mapEdificios = new HashMap<Integer, EdificioNuevo>();
    private Map<Integer, PisoNuevo> _mapPisos = new HashMap<Integer, PisoNuevo>();
    private Map<Integer, oficinaNuevo> _mapOficinas = new HashMap<Integer, oficinaNuevo>();
    private Map<Integer, EntidadEmployees>_mapEntidadEmployees = new HashMap<Integer, EntidadEmployees>();
    private Map<Integer, EntidadAssetStatus> _mapEntidadAssetStatus = new HashMap<Integer, EntidadAssetStatus>();
    EmployeesDBHelper employeesDBHelper;
    AssetStatusDBHerlper assetStatusDBHerlper;



    private boolean _itemSelectedUserCompania, _itemSelectedUserEdificio, _itemSelectedUserPiso = true;
    String idCompaniaActivo, idedificioActivo, idpisoActivo, idoficinaActivo;
    private String numeroingresado, numero, placa, descripcion, compania, idCompania, idEdificio, edificio,
            idPiso, piso, idOficina, oficina, encargado, marca, modelo, serie, epc;

    String idCompaniaCrea, idedificioCrea, idpisoCrea, idoficinaCrea;
    com.example.diverscan.activeid.sqlite.newAssets newAssets;
    RazonSocialDBHelper dataSource;
    PisoDBHelper dataSourcepiso;
    OficinaDBHelper dataSourceOficina;
    EdificioDBHelper dataSourceEdificio;
    AssetsDBHelper AssetsDBHelper;

    private String[] strDescripciones;
    private List<String> listaDescripciones;
    private ArrayAdapter<String> adapterDescripciones;
    private Snackbar _snackbar ;
    private TextView txtUbicacion;
    private String assetSysId ;
    private String parentAsset;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_activo);
        controles();


        listaSubActivos = findViewById(R.id.listSubactivos);

        btn_AddSubActivo.setBackgroundResource(R.drawable.ic_add_to_photos_black_24dp);


        CargandoPanel();
        Eventos();
        //CargarDescripciones();
        cargarRazonesSociales();

        cargarUbicaciones();
        cargarEstado();

        pnlSubActivo.setVisibility(View.GONE);
        txtNotificacion.setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent sector = new Intent(SubActivoActivity.this, LoginActivity.class);
                startActivity(sector);
            }
        }.start();

    }

    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }
    //region Eventos y Controles

    private void CargandoPanel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mSubActivoView.getContext());
        builder.setIcon(R.drawable.alertaicono);
        builder.setTitle("Cargando...");
        builder.setMessage("Ingresando a visualización de sub activos.");
        builder.setCancelable(true);

        final ProgressBar progressBar = new ProgressBar(mSubActivoView.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        progressBar.setLayoutParams(lp);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.rgb(235,183, 30), PorterDuff.Mode.SRC_IN);
        builder.setView(progressBar);
        final AlertDialog closedialog = builder.create();
        closedialog.show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                closedialog.dismiss();
                timer.cancel();
                mSubActivoView.post(Load);

            }
        }, 3000);

    }

    public void controles(){

        mSubActivoView = findViewById(R.id.SubActivoScroll);
        btn_AddSubActivo = findViewById(R.id.btn_AgregarSubActivos);
        //btn_DeleteActivo = findViewById(R.id.btn_Eliminar);
        pnlActualizaActivo = findViewById(R.id.ActualizaActivo);
        pnlVerSubActivo = findViewById(R.id.VerSubActivo);
        rcvSubActivo = findViewById(R.id.rcvSubActivo);
        pnlSubActivo = findViewById(R.id.CreaSubActivo);
        txtNotificacion = findViewById(R.id.txtNotificacion);

        CompaniaView = findViewById(R.id.compania_crea);
        EdificioView = findViewById(R.id.edificio_crea);
        PisoView = findViewById(R.id.piso_crea);
        OficinaView = findViewById(R.id.oficina_crea);

        //PreDescripciones = findViewById(R.id.SpinnerSubDescripcioness);

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
        AssetsDBHelper = new AssetsDBHelper(mSubActivoView.getContext());
        newAssets = new newAssets(mSubActivoView.getContext());
        dataSource = new RazonSocialDBHelper(mSubActivoView.getContext());
        dataSourceEdificio = new EdificioDBHelper(mSubActivoView.getContext());
        dataSourcepiso = new PisoDBHelper(mSubActivoView.getContext());
        dataSourceOficina = new OficinaDBHelper(mSubActivoView.getContext());
        btn_Crear = findViewById(R.id.btn_IngresarSubactivo);
        rlsnackbar = findViewById(R.id.subactivo_activity);

        assetStatusDBHerlper = new AssetStatusDBHerlper(mSubActivoView.getContext());
        employeesDBHelper = new EmployeesDBHelper(mSubActivoView.getContext());
        spEmpleados =   findViewById(R.id.spEmpleados);
        spAssetStatus = findViewById(R.id.spEstado) ;
        txtCodigoEmpleado= findViewById(R.id.txtCodigoEmpleado);
    }

    public void Eventos(){
        btn_AddSubActivo.setOnClickListener(OnClickListenerAddSubAsset);
        btn_Crear.setOnClickListener(OnClickListenerCrearSubActivo);
        CompaniaView.setOnItemSelectedListener(onItemSpinnerListenerCompania);
        EdificioView.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        PisoView.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        //PreDescripciones.setOnItemSelectedListener(onItemSelectedListenerDescripcion);

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
                    PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
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
                if(txtCodigoEmpleado.getText().length() > 3){
                    buscarEmpleadoPorDescripcion();
                }
            }
        });

    }
    public static void Message(){
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    //endregion

    //region Metodos de spinners

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
   /* private void CargarDescripciones(){

        listaDescripciones = new ArrayList<>();
        strDescripciones = new String[]{"Laptops", "Silla", "Mesas"};

        fillSpinnerDescripciones(strDescripciones);
    }*/

    private void cargarRazonesSociales() {
        RazonSocialDBHelper razonSocialDBHelper = new RazonSocialDBHelper(mSubActivoView.getContext());
        _mapRazonSociales = (Map<Integer, RazonNuevo>) razonSocialDBHelper.ObtenerRazon();
        RazonNuevo[] razones = _mapRazonSociales.values().toArray(new RazonNuevo[0]);
        fillSpinnerRazon(razones);
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
    private void buscarOficinaPorIdPisoNombre(){
        if (_itemSelectedUserPiso) {
            PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
            String nombreBusqueda = txtUbicacion.getText().toString();
            cargarOficinasPorPisoNombre(pisoRecord.getIdPiso(),nombreBusqueda);
        }
        _itemSelectedUserPiso = true;
    }
    private void cargarOficinasPorPisoNombre(String idPiso,String nombre) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mSubActivoView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficinaPorPisoDescripcion3(idPiso,nombre);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }
    private void cargarEdificios(String idCompania) {
        EdificioDBHelper edificioDBHelper = new EdificioDBHelper(mSubActivoView.getContext());
        _mapEdificios = (Map<Integer, EdificioNuevo>) edificioDBHelper.ObtenerEdificio(idCompania);
        EdificioNuevo[] edificios = _mapEdificios.values().toArray(new EdificioNuevo[0]);
        fillSpinnerEdificio(edificios);
    }

    private void cargarPisos(String idEdificio) {
        PisoDBHelper pisoDBHelper = new PisoDBHelper(mSubActivoView.getContext());
        _mapPisos = (Map<Integer, PisoNuevo>) pisoDBHelper.ObtenerPiso(idEdificio);
        PisoNuevo[] pisos = _mapPisos.values().toArray(new PisoNuevo[0]);
        fillSpinnerPiso(pisos);
    }

    private void cargarOficinas(String idPiso) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(mSubActivoView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficina(idPiso);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
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
                case R.id.SpinnerSubDescripcioness:
                    preDescripcion = strDescripciones[position];
                    DescripcionView.setText(preDescripcion);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };*/

   /* private void fillSpinnerDescripciones(String[] descripciones){
        PreDescripciones = findViewById(R.id.SpinnerSubDescripcioness);
        PreDescripciones.setAdapter(new ArrayAdapter<>(mSubActivoView.getContext().getApplicationContext(),
                R.layout.spinner_layaout, descripciones));
    }*/


    private void fillSpinnerRazon(RazonNuevo[] razonSocialRecords){
        CompaniaView = findViewById(R.id.compania_crea);
        CompaniaView.setAdapter( new ArrayAdapter<>(mSubActivoView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,razonSocialRecords));

    }

    private void fillSpinnerEdificio(EdificioNuevo[] edificioRecords){
        EdificioView = findViewById(R.id.edificio_crea);
        EdificioView.setAdapter( new ArrayAdapter<>(mSubActivoView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,edificioRecords));
    }

    private void fillSpinnerPiso(PisoNuevo[] pisoRecords){
        PisoView = findViewById(R.id.piso_crea);
        PisoView.setAdapter( new ArrayAdapter<>(mSubActivoView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,pisoRecords));
    }

    private void fillSpinnerOficina(oficinaNuevo[] oficinaRecords){
        OficinaView = findViewById(R.id.oficina_crea);
        OficinaView.setAdapter( new ArrayAdapter<>(mSubActivoView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,oficinaRecords));
    }


    //endregion

    //region Recibir Informacion ActivityAnterior
    Runnable Load = new Runnable() {
        @Override
        public void run() {
            RecibirTakesInfo();
        }
    };

    public void RecibirTakesInfo() {

        _entidadSubActivos = (ArrayList<EntidadSubActivo>) getIntent().getSerializableExtra("SubActivos");
        _idActivoPadre = getIntent().getStringExtra("_idActivo");
        if(_entidadSubActivos.size()> 0) {
            fillRecyclerViewSubActivos(_entidadSubActivos);
            pnlSubActivo.setVisibility(View.GONE);
        }else{
            rcvSubActivo.setVisibility(View.GONE);
            txtNotificacion.setVisibility(View.VISIBLE);
        }
    }
    //endregion

    //region Adaptador RecyclerView
    private void fillRecyclerViewSubActivos(ArrayList<EntidadSubActivo> response){

        try{

            listaSubActivos.setLayoutManager(new LinearLayoutManager(this));
            _adapterSubActivoSwipe = new AdapterSubActivoSwipe(response);
            listaSubActivos.setAdapter(_adapterSubActivoSwipe);

            ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(this);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(listaSubActivos);

        }catch(Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
        }
    }

    //endregion

    //region metodo de deslizar el activo

    @Override
    public void itemTouchOnMode(int oldPosition, int newPosition) {
        _entidadSubActivos.add(newPosition, _entidadSubActivos.remove(oldPosition));
        _adapterSubActivoSwipe.notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int position) {
         assetSysId = _entidadSubActivos.get(viewHolder.getAdapterPosition()).getIdActivo();
         parentAsset = _entidadSubActivos.get(viewHolder.getAdapterPosition()).getPaternId();
        String assetDescription = _entidadSubActivos.get(viewHolder.getAdapterPosition()).getDescripcion();
        String assetBarcode = _entidadSubActivos.get(viewHolder.getAdapterPosition()).getCodeBar();

        //Almacena el activo por si desea deshacer la acción
        final EntidadSubActivo activoEliminado =  _entidadSubActivos.get(viewHolder.getAdapterPosition());
        final int indexDeleted = viewHolder.getAdapterPosition();

        //Elimina el item del recycler
        _adapterSubActivoSwipe.RemoveAsset(viewHolder.getAdapterPosition());

        //Muestra la opción de deshacer
        _snackbar = Snackbar.make(rlsnackbar, "SubActivo removido.", 2000);
        _snackbar.setAction("Deshacer", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                press = true;
                SnackPressed(assetSysId, press, parentAsset);
                _adapterSubActivoSwipe.RestoreAsset(activoEliminado, indexDeleted);
            }
        });
        _snackbar.setActionTextColor(Color.rgb(179,179,179));
        View snackBarView = _snackbar.getView();
        snackBarView.setBackgroundColor(Color.rgb(242,59,59));
        _snackbar.show();

        handler.postDelayed(r, 4000);
    }

    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run(){
            if(!_snackbar.isShown()){
                if(!press){
                    SnackPressed(assetSysId, press, parentAsset);
                }
            }
        }
    };

    private void SnackPressed(String idActivo, boolean pressed, String ParentAsset){
       if(!pressed){
           boolean existe = newAssets.ExisteActivo(idActivo);
           if(existe){
               boolean respuesta = newAssets.EliminarSubActivo(idActivo);
               if(respuesta) {
                   Toast.makeText(getApplicationContext()
                           , "Se ha eliminado correctamente", Toast.LENGTH_LONG);
               }
           }else{
               boolean respuesta2 = AssetsDBHelper.ActualizarSubActivo(idActivo);
               if (respuesta2) {
                   Toast.makeText(getApplicationContext()
                           , "Se ha eliminado correctamente", Toast.LENGTH_LONG);
               }
           }

       }else{
           boolean existe = newAssets.ExisteActivo(idActivo);
           if(existe){
               boolean respuesta = newAssets.RecuperarSubActivo(idActivo, ParentAsset);
               if(respuesta) {
                   Toast.makeText(this, "Activo recuperado correctamente", Toast.LENGTH_SHORT).show();
               }
           }else{
               boolean respuesta2 = AssetsDBHelper.RecuperaSubActivo(idActivo, ParentAsset);
               if (respuesta2) {
                   Toast.makeText(this, "Activo recuperado correctamente",
                           Toast.LENGTH_SHORT).show();
               }
           }
       }
    }
    //endregion

    //region Metodos Principales
    public void IngresarActivo (View view) {

        numero = NumeroactivoView.getText().toString();
        placa = PlacaView.getText().toString();

        try {

           // boolean respuestaNumero = newAssets.VerificarNumero(numero);
            if(TextUtils.isEmpty(placa) || TextUtils.isEmpty(numero)) {
                if(TextUtils.isEmpty(placa) && TextUtils.isEmpty(numero)){
                    PlacaView.setError("Este campo es obligatorio");
                    NumeroactivoView.setError("Este campo es obligatorio");
                }else if(TextUtils.isEmpty(placa)){
                    PlacaView.setError("Este campo es obligatorio");
                }else if(TextUtils.isEmpty(numero)){
                    NumeroactivoView.setError("Este campo es obligatorio");
                }
            }else {

                descripcion = DescripcionView.getText().toString();
                encargado = EncargadoView.getText().toString();
                compania = CompaniaView.getSelectedItem().toString();
                edificio = EdificioView.getSelectedItem().toString();
                piso = PisoView.getSelectedItem().toString();
                oficina = OficinaView.getSelectedItem().toString();
                marca = MarcaView.getText().toString();
                modelo = ModeloView.getText().toString();
                serie = SerieView.getText().toString();
                epc = EPCView.getText().toString();

                RazonNuevo razonSocialRecord = (RazonNuevo) CompaniaView.getSelectedItem();
                idCompania = razonSocialRecord.getIdRazon();
                compania = razonSocialRecord.getNombreRazon();

                EdificioNuevo edificioRecord = (EdificioNuevo) EdificioView.getSelectedItem();
                idEdificio = edificioRecord.getIdEdificio();
                edificio = edificioRecord.getNombreEdificio();

                PisoNuevo pisoRecord = (PisoNuevo) PisoView.getSelectedItem();
                idPiso = pisoRecord.getIdPiso();
                piso = pisoRecord.getNombrePiso();

                oficinaNuevo oficinaRecord = (oficinaNuevo) OficinaView.getSelectedItem();
                idOficina = oficinaRecord.getIdOficina();
                oficina = oficinaRecord.getNombreOficina();

                String idActivoParent =  _idActivoPadre;
                EntidadEmployees employees = (EntidadEmployees)spEmpleados.getSelectedItem();
                String idEmployees = employees.getEmployeeSysId();
                String nameEmployees = employees.getName();

                EntidadAssetStatus entidadAssetStatus = (EntidadAssetStatus)spAssetStatus.getSelectedItem();
                String assetStatusSysId = entidadAssetStatus.getId();

                boolean respuestaPlaca = newAssets.VerificarPlaca(placa);
                if (respuestaPlaca) {
                    Message();
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alertaicono)
                            .setTitle("Advertencia")
                            .setMessage("La placa o el número ya han sidos ingresados, no se puede ingresar nuevamente. Proceda a realizar el cambio.")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    NumeroactivoView.setText("");
                                    NumeroactivoView.requestFocus();
                                    PlacaView.setText("");

                                }
                            }).show();
                } else if (PlacaView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "El campo placa no puede registrarse vacío", Toast.LENGTH_LONG).show();
                } else if (NumeroactivoView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "El campo número de activo no puede registrarse vacío", Toast.LENGTH_LONG).show();
                } else if (CompaniaView.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar una razon social", Toast.LENGTH_LONG).show();
                } else if (EdificioView.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar un edificio", Toast.LENGTH_LONG).show();
                } else if (PisoView.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar un piso", Toast.LENGTH_LONG).show();
                } else if (OficinaView.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar una oficina", Toast.LENGTH_LONG).show();
                } else {
                    boolean respuesta = newAssets.InsertarSubActivo(numero, placa, descripcion, idCompania, compania, idEdificio,
                            edificio, idPiso, piso, idOficina, oficina, nameEmployees,idEmployees, marca, modelo, serie, epc, idActivoParent, assetStatusSysId);

                    if (respuesta) {
                        Message();
                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.alertaicono)
                                .setTitle("Activo ingresado correctamente!")
                                .setMessage("¿Desea crear otro activo?")
                                .setCancelable(false)
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        NumeroactivoView.setText("");
                                        PlacaView.setText("");
                                        DescripcionView.setText("");
                                        //EncargadoView.setText("");
                                        MarcaView.setText("");
                                        ModeloView.setText("");
                                        SerieView.setText("");
                                        EPCView.setText("");
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _entidadSubActivos.clear();
                                        Cursor cursor = AssetsDBHelper.ObtenerSubActivos(_idActivoPadre);
                                        int count = cursor.getCount();

                                        if (count != 0) {
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
                                        fillRecyclerViewSubActivos(_entidadSubActivos);
                                        if(_entidadSubActivos.size()> 0) {
                                            fillRecyclerViewSubActivos(_entidadSubActivos);
                                            pnlSubActivo.setVisibility(View.GONE);
                                            rcvSubActivo.setVisibility(View.VISIBLE);
                                            txtNotificacion.setVisibility(View.GONE);
                                            pnlVerSubActivo.setVisibility(View.VISIBLE);
                                        }
                                        btn_AddSubActivo.setBackgroundResource(R.drawable.ic_add_to_photos_black_24dp);
                                        SubActivoOn = false;
                                    }
                                }).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "El Activo no se ha ingresado correctamente", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }catch(Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
        }

    }


    //endregion

    //region Botones
    private Button.OnClickListener OnClickListenerAddSubAsset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if(!SubActivoOn){
              pnlSubActivo.setVisibility(View.VISIBLE);
              pnlVerSubActivo.setVisibility(View.GONE);
              btn_AddSubActivo.setBackgroundResource(R.drawable.regresar);
              SubActivoOn = true;
          } else{
              pnlSubActivo.setVisibility(View.GONE);
              pnlVerSubActivo.setVisibility(View.VISIBLE);
              btn_AddSubActivo.setBackgroundResource(R.drawable.ic_add_to_photos_black_24dp);
              SubActivoOn = false;
          }
        }
    };

    private Button.OnClickListener OnClickListenerCrearSubActivo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IngresarActivo(mSubActivoView);
        }
    };

    //endregion


}
