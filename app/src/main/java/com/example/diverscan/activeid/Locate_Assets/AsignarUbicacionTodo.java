package com.example.diverscan.activeid.Locate_Assets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.TomasFisicas.TomasFisias;
import com.example.diverscan.activeid.TomasFisicas.AjustarActivoUbicacion;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivos;
import com.example.diverscan.activeid.TomasFisicas.entidadaAjustarUbicacion;
import com.example.diverscan.activeid.AssetStatus.AssetStatusDBHerlper;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Employees.EmployeesDBHelper;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Inventory.InventarioVisual;
import com.example.diverscan.activeid.Oficina.ActualizarUbicacionActivo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AsignarUbicacionTodo extends AppCompatActivity implements ResponseHandlerInterface {


    private View UbicacionFinalView;

    private String Faltante = "Faltante";
    private String NoPertenece = "No Pertenece";
    private String Encontrado = "Encontrado";

    private RecyclerView ListaLectura;
    private RecyclerView.Adapter recycle;
    private ArrayList<TomasFisias> activoInventarios = new ArrayList<TomasFisias>();

    ArrayList<InventarioVisual> inventarioVisuals = new ArrayList<InventarioVisual>();
    ArrayList<InventarioVisual> tagSinPertenencia = new ArrayList<InventarioVisual>();
    List<Model_Seleccion_CardView> mModel;
    ArrayList<ActualizarUbicacionActivo> actualizarUbicacionActivos;

    private ArrayList<String> _activosNoExiste = new  ArrayList<String>();
    private ArrayList<String> _activosSobrantes = new  ArrayList<String>();
    private ArrayList<String> _activosEncontrado = new ArrayList<String>();

    OfficesDBHelper OfficesDBHelper;
    AssetsDBHelper AssetsDBHelper;
    private Button VerResultado;
    private Button btnAjustarTodo;
    private Button btnAjustarSeleccion;
    private CheckBox ckAjustarSeleccion;
    private TextView txtEcontrados, txtFaltantes, txtSobrantes;
    private Spinner spAssetStatus;
    private Spinner spEmpleados;
    private EditText txtCodigoEmpleado;
    private Button btn_AjustarEstado;
    private Button btn_AjustarEmpleado;
    Context context;
    private EditText txtBarcode;
    private CheckBox ckActivarRfid;
    public String AcIdoficina, AcOficinaNombre, AcIdPiso, AcPisoNombre, AcIdEdificio, AcNombreEdificio, AcIdCompania, AcNombreCompania,idOficina;
    ChequearUbicacion _chequearUbicacion;
    private ArrayList<entidadaAjustarUbicacion> entidadaAjustarUbicacions = new ArrayList<>();
    private ArrayList<AjustarActivoUbicacion> ajustarActivoUbicacions = new ArrayList<>();
    Map<String, AjustarActivoUbicacion> _activosSeleccionado = new HashMap<String, AjustarActivoUbicacion>();
    Map<String, AjustarActivoUbicacion> _activosSinSeleccionar = new HashMap<String, AjustarActivoUbicacion>();
    Map<String, AjustarActivoUbicacion> _tagSinPertenencia = new HashMap<String, AjustarActivoUbicacion>();

    private Map<Integer, EntidadAssetStatus> _mapEntidadAssetStatus = new HashMap<Integer, EntidadAssetStatus>();
    private Map<Integer, EntidadEmployees>_mapEntidadEmployees = new HashMap<Integer, EntidadEmployees>();
    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false;
    private boolean isMultiSelect = false;
    private boolean scannerActivate = false;
    private Activity _activity;
    AlertDialog alertDialog;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    AdaptadorAjusteUbicacion adaptadorAjusteUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuste_ubicacion_final);
        context= this.getApplicationContext();

        controles();
        eventos();
        _activity = this;
        ListaLectura = findViewById(R.id.listView);
        UbicacionFinalView.post(Load);

        rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(AsignarUbicacionTodo.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
        cargarEstado();
    }
    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }
   //region Recepcion de datos del activity Antecesor
    Runnable Load = new Runnable() {
        @Override
        public void run()
        {
            RecibirTakesInfo();
        }
    };

    public void RecibirTakesInfo() {

        activoInventarios = (ArrayList<TomasFisias>) getIntent().getSerializableExtra("inventarioVisual");
        idOficina = getIntent().getExtras().getString("idOficina");

        try {
            _chequearUbicacion = new ChequearUbicacion(activoInventarios, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //endregion

   //region Controles
    public void controles() {
        UbicacionFinalView = findViewById(R.id.UbicacionFinal);
        VerResultado = findViewById(R.id.btn_VerResultados);
        btnAjustarTodo = findViewById(R.id.btn_AjustarTodo);
        btnAjustarSeleccion = findViewById(R.id.btn_AjustarSeleccionado);
        ckAjustarSeleccion = findViewById(R.id.ckSeleccion);
        ckAjustarSeleccion.setChecked(false);
        btnAjustarSeleccion.setEnabled(false);
        OfficesDBHelper = new OfficesDBHelper(UbicacionFinalView.getContext());
        AssetsDBHelper = new AssetsDBHelper(UbicacionFinalView.getContext());

        txtEcontrados = findViewById(R.id.txtAEncontrados);
        txtFaltantes = findViewById(R.id.txtAFaltantes);
        txtSobrantes = findViewById(R.id.txtASobrantes);

        spAssetStatus = findViewById(R.id.spEstado);
        spEmpleados =   findViewById(R.id.spEmpleados);
        txtCodigoEmpleado= findViewById(R.id.txtCodigoEmpleado);

        btn_AjustarEstado =  findViewById(R.id.btn_AjustarEstado);
        btn_AjustarEmpleado = findViewById(R.id.btn_AjustarEmpleado);

        ckActivarRfid = findViewById(R.id.ckActivarRFID);
        ckActivarRfid.setChecked(true);

        txtBarcode = findViewById(R.id.txt_barcode);
        btnAjustarTodo.setEnabled(false);
        btnAjustarTodo.setBackgroundColor(Color.rgb(170,170,170));
    }


    public void onCheckBoxClicked(View view){
        boolean selected = ((CheckBox) view).isChecked();
        switch (view.getId()){
            case R.id.ckSeleccion:
                if(selected)
                {
                    isMultiSelect = true;
                    Toast.makeText(getApplicationContext(), "Selección multiple activa", Toast.LENGTH_LONG).show();
                }
                else
                {
                    isMultiSelect = false;
                    Toast.makeText(getApplicationContext(), "Selección multiple desactivada", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ckActivarRFID:
                if(selected)
                {
                    scannerActivate = false;
                    rfidHandler.EncenderRFID();
                    Toast.makeText(getApplicationContext(), "RFID Encendido", Toast.LENGTH_LONG).show();
                }
                else
                {
                    scannerActivate = true;
                    rfidHandler.ApagarRFID();
                    txtBarcode.requestFocus();
                    Toast.makeText(getApplicationContext(), "RFID Apagado", Toast.LENGTH_LONG).show();
                }
             break;
        }
    }

    private void iniciarRFID()
    {
        try
        {
            rfidHandler.setTriggerMode("RFID");
            scannerActivate = false;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    private void desconectarRFID()
    {
        try{
            rfidHandler.setTriggerMode("BARCODE");
            scannerActivate = true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //endregion


   //region Eventos
    public void eventos()
    {

        VerResultado.setOnClickListener(OnClickListenerResultados);
        btnAjustarTodo.setOnClickListener(OnClickListenerAjustarTodo);
        btnAjustarSeleccion.setOnClickListener(OnClickListenerAjustarSeleccion);
        //ckAjustarSeleccion.setOnClickListener(ckAjustarSeleccionHabilita);
        btn_AjustarEstado.setOnClickListener(btnAjustarEstado);
        btn_AjustarEmpleado.setOnClickListener(btnAjustarEmpleado);
        //*****************************************************************************
        // cambios realizados por andrey sanchez.
        //estaba comentado por algun motivo en especial.
        ckActivarRfid.setOnClickListener(ckActivarRfidOnlClickListener);
        //*****************************************************************************
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
                if(txtCodigoEmpleado.getText().length() > 2){
                    buscarEmpleadoPorDescripcion();
                }
            }
        });

        txtBarcode.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(txtBarcode.getText().length() > 5){
                   Busqueda(txtBarcode.getText().toString());
                }
            }
        });

    }

    private View.OnClickListener ckActivarRfidOnlClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            //******************************************************************************
            //cambio realizado por andrey sanchez zuñiga
            //20/03/2023
            // validacion de datos
            //******************************************************************************
            try
            {
                if(ckActivarRfid.isChecked()==true)
                {
                    iniciarRFID();
                }
                else if(ckActivarRfid.isChecked()==false)
                {
                    desconectarRFID();
                    //txtBarcode.requestFocus();
                }
            }
            catch(Exception ex)
            {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    };



    /*Sonido de lectura*/
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    public void Message() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    //endregion

   //region Carga de activos

    private void VerResultado()
    {
        int EPCEncontrados, EPCFaltantes, EPCSobrantes = 0;
        EPCEncontrados = _chequearUbicacion.cantidadActivosEncontrados() + _activosEncontrado.size();
        txtEcontrados.setText(String.valueOf(EPCEncontrados));

        EPCFaltantes = ((_chequearUbicacion.cantidadActivosFaltantes() - _activosEncontrado.size()) - _activosNoExiste.size());
        txtFaltantes.setText(String.valueOf(EPCFaltantes));

        EPCSobrantes = (_chequearUbicacion.cantidadActivosSobrantes() - tagSinPertenencia.size()) + _activosSobrantes.size();
        txtSobrantes.setText(String.valueOf(EPCSobrantes));

    }
    private void BuscarEPCS(View view) {

        _chequearUbicacion.InsertarActivosEncontrados();
        _chequearUbicacion.ClearActivosSobrantes();


        if (_chequearUbicacion.GetActivos().size() == 0) {
            Toast.makeText(getApplicationContext(), "La lista se encuentra vacía", Toast.LENGTH_LONG).show();
        } else {

            for (InventarioVisual activo : _chequearUbicacion.GetActivos()) {
                if (activo.getStatus().equals("Sin Asignar")) {
                    String idActivo = activo.getAssetSysId();
                    String numeroActivo = activo.getNumero();
                    String descripcionActivo = activo.getDescripcion();
                    String Status = activo.getStatus();
                    String Epc = activo.getEPC();
                    String sectorActivo = activo.getOficina();
                    String idOficina = activo.getIdOficina();
                    InventarioVisual visual = new InventarioVisual(numeroActivo, descripcionActivo,
                            Status, Epc, idActivo, sectorActivo, idOficina);
                    tagSinPertenencia.add(visual);
                }else if(activo.getDescripcion().equals("No existe") && activo.getStatus().equals("Faltante")){
                    _activosNoExiste.add(activo.getDescripcion());
                }else if(activo.getStatus().equals("Encontrado")){

                        String idActivo = activo.getAssetSysId();
                        String numeroActivo = activo.getNumero();
                        String descripcionActivo = activo.getDescripcion();
                        String Status = activo.getStatus();
                        String Epc = activo.getEPC();
                        String sectorActivo = activo.getOficina();
                        String idOficina = activo.getIdOficina();

                        InventarioVisual visual = new InventarioVisual(numeroActivo, descripcionActivo,
                                Status, Epc, idActivo, sectorActivo, idOficina);

                        inventarioVisuals.add(visual);
                }else if(_activosScanner.containsKey(activo.getAssetSysId())){
                        if(activo.getIdOficina().equals(idOficina)){
                            _activosEncontrado.add(activo.getAssetSysId());

                            activo.setStatus("Encontrado");
                            String idActivo = activo.getAssetSysId();
                            String numeroActivo = activo.getNumero();
                            String descripcionActivo = activo.getDescripcion();
                            String Status = activo.getStatus();
                            String Epc = activo.getEPC();
                            String sectorActivo = activo.getOficina();
                            String idOficina = activo.getIdOficina();

                            InventarioVisual visualEncontrado = new InventarioVisual(numeroActivo, descripcionActivo,
                                    Status, Epc, idActivo, sectorActivo, idOficina);
                            inventarioVisuals.add(visualEncontrado);

                            _activosScanner.remove(activo.getAssetSysId());
                        }
                }else {
                        String idActivo = activo.getAssetSysId();
                        String numeroActivo = activo.getNumero();
                        String descripcionActivo = activo.getDescripcion();
                        String Status = activo.getStatus();
                        String Epc = activo.getEPC();
                        String sectorActivo = activo.getOficina();
                        String idOficina = activo.getIdOficina();

                        InventarioVisual visual = new InventarioVisual(numeroActivo, descripcionActivo,
                                Status, Epc, idActivo, sectorActivo, idOficina);

                        inventarioVisuals.add(visual);
                    }
                }

            ArrayList<AjustarActivoUbicacion> activoUbicacions = new ArrayList<AjustarActivoUbicacion>();
            activoUbicacions.addAll(_activosScanner.values());
            for (AjustarActivoUbicacion activo : activoUbicacions){
                String idActivo = activo.getAssetSysId();
                String numeroActivo = activo.getNumero();
                String descripcionActivo = activo.getDescripcion();
                String Status = "No Pertenece";
                String Epc = "Sin Asignar";
                String sectorActivo = activo.getNombreOficina();
                String idOficina = activo.getIdOficina();
                _activosSobrantes.add(activo.getAssetSysId());
                InventarioVisual visual = new InventarioVisual(numeroActivo, descripcionActivo,
                        Status, Epc, idActivo, sectorActivo, idOficina);
                inventarioVisuals.add(visual);
            }
            fillRecyclerView(inventarioVisuals);

        }
    }

    public void fillRecyclerView (ArrayList<InventarioVisual> response) {

        try{

                ListaLectura.setLayoutManager( new LinearLayoutManager( this));
                adaptadorAjusteUbicacion = new AdaptadorAjusteUbicacion(response);
                ListaLectura.setAdapter(adaptadorAjusteUbicacion);

            ListaLectura.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int i = 0; i < inventarioVisuals.size(); i++ ) {
                        if (inventarioVisuals.get(i).isSelected()) {
                            _activosSeleccionado.put(inventarioVisuals.get(i).getAssetSysId(), new AjustarActivoUbicacion(
                                    inventarioVisuals.get(i).getNumero(),
                                    inventarioVisuals.get(i).getAssetSysId(),
                                    inventarioVisuals.get(i).getIdOficina(),
                                    inventarioVisuals.get(i).getOficina(),
                                    inventarioVisuals.get(i).getDescripcion()
                            ));
                            int test = _activosSeleccionado.size();
                            Toast.makeText(getApplicationContext(), String.valueOf(test), Toast.LENGTH_LONG).show();
                            ajustarActivoUbicacions.addAll(_activosSeleccionado.values());
                        } else {
                            _activosSeleccionado.remove(inventarioVisuals.get(i).getAssetSysId());
                            ajustarActivoUbicacions.addAll(_activosSeleccionado.values());
                            int test = _activosSeleccionado.size();
                            Toast.makeText(getApplicationContext(), String.valueOf(test), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });


            ListaLectura.addOnItemTouchListener(new RecyclerTouchListener(this,
                    ListaLectura, new AdaptadorAjusteUbicacion.OnLongPress(){
                @Override
                public void onClicPress(View view, int position) {
                    if(ckAjustarSeleccion.isChecked()){
                        if(!inventarioVisuals.get(position).isSelected()){
                            //for (InventarioVisual inventarioVisual : _productList) {

                            _activosSeleccionado.put(inventarioVisuals.get(position).getAssetSysId(), new AjustarActivoUbicacion(
                                    inventarioVisuals.get(position).getNumero(),
                                    inventarioVisuals.get(position).getAssetSysId(),
                                    inventarioVisuals.get(position).getIdOficina(),
                                    inventarioVisuals.get(position).getOficina(),
                                    inventarioVisuals.get(position).getDescripcion()
                            ));
                            _activosSinSeleccionar.remove(inventarioVisuals.get(position).getAssetSysId());
                            Toast.makeText(getApplicationContext(), "Activo Seleccionado", Toast.LENGTH_LONG).show();
                            //}
                        }else{

                            _activosSinSeleccionar.put(inventarioVisuals.get(position).getAssetSysId(), new AjustarActivoUbicacion(
                                    inventarioVisuals.get(position).getNumero(),
                                    inventarioVisuals.get(position).getAssetSysId(),
                                    inventarioVisuals.get(position).getIdOficina(),
                                    inventarioVisuals.get(position).getOficina(),
                                    inventarioVisuals.get(position).getDescripcion()
                            ));
                            _activosSeleccionado.remove(inventarioVisuals.get(position).getAssetSysId());

                            Toast.makeText(getApplicationContext(), "Activo Deseleccionado", Toast.LENGTH_LONG).show();
                        }
                    }

                }

                @Override
                public void onLongPress(View view, int position) {
                    if(!ckAjustarSeleccion.isChecked()){

                        String Fillnumero, FillidActivo, FilldOficina, FillnombreOficina, Filldescripcion;
                        Fillnumero = inventarioVisuals.get(position).getNumero();
                        FillidActivo = inventarioVisuals.get(position).getAssetSysId();
                        FilldOficina = inventarioVisuals.get(position).getIdOficina();
                        FillnombreOficina = inventarioVisuals.get(position).getOficina();
                        Filldescripcion = inventarioVisuals.get(position).getDescripcion();
                        entidadaAjustarUbicacion entidadAjustarUbicacion = new entidadaAjustarUbicacion(Fillnumero,
                                FillidActivo, FilldOficina,FillnombreOficina, Filldescripcion);
                        entidadaAjustarUbicacions.add(entidadAjustarUbicacion);

                        AlertaUbicacion(Fillnumero,
                                FillidActivo, FilldOficina,FillnombreOficina, Filldescripcion);
                    }
                }
            }));

        }catch(Exception ex){
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }
    //endregion

   //region Metodos de Ajuste

    public EntidadActivos entidadActivos;
    public HashMap<String, AjustarActivoUbicacion> _activosScanner = new HashMap<String, AjustarActivoUbicacion>();
    private void Busqueda(String Prueba){
        ArrayList<String> tags = new ArrayList<String>();
        ArrayList<String> placa = new ArrayList<String>();
        String EPC="";
        String Descripcion="";
        String Placa="";
        entidadActivos = AssetsDBHelper.VerActivoPlaca(Prueba);

        if(entidadActivos == null){

        }else{
            EPC = entidadActivos.getTag();
            Descripcion = entidadActivos.getDescripcion();
            Placa = entidadActivos.getCodeBar();

            if(!_activosScanner.containsKey(entidadActivos.getIdActivo())){
                _activosScanner.put(entidadActivos.getIdActivo(), new AjustarActivoUbicacion(
                        entidadActivos.getCodeBar(),
                        entidadActivos.getIdActivo(),
                        entidadActivos.getIdOficina(),
                        entidadActivos.getOficina(),
                        entidadActivos.getDescripcion()
                ));

                AlertDialog.Builder builder = new AlertDialog.Builder(UbicacionFinalView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("AÑADIDO");
                builder.setMessage("Activo Escaneado:" + Descripcion + ";" + "\n" + "Placa No.: " + Placa);
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
                }, 2000);
                txtBarcode.setText("");

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(UbicacionFinalView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ALERTA");
                builder.setMessage("Activo Escaneado:" + Descripcion + ";" + "\n" + "Placa No.: " + Placa + "\n" + "Ya se ha leído anteriormente");
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
                }, 2000);
                txtBarcode.setText("");
            }
        }

    }

    public String Fillnumero, FillidActivo, FilldOficina, FillnombreOficina, Filldescripcion;
    private void AjustarActivosSeleccionados(View v){
        try{
            final ArrayList<AjustarActivoUbicacion> ajustarActivoUbicacions = new ArrayList<>();
            ajustarActivoUbicacions.addAll(_activosSeleccionado.values());
            int tamanoLista = ajustarActivoUbicacions.size();
            if(tamanoLista <= 0){
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("Se debe seleccionar los activos")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }}).show();
            }
            else {
                String Oficina = idOficina;
                actualizarUbicacionActivos = OfficesDBHelper.UbicacionCompleta(Oficina);
                String NombreOficinaNueva = OfficesDBHelper.NombreSectorActualiza(Oficina);

                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("REUBICAR ACTIVO")
                        .setMessage("Desea asignar todos los activos al sector: " + NombreOficinaNueva)
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ajustarEstado(UbicacionFinalView);
                            }
                        })
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (ActualizarUbicacionActivo ubicacionActivo : actualizarUbicacionActivos) {
                                    AcIdCompania = ubicacionActivo.get_IdCompania();
                                    AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                    AcIdoficina = ubicacionActivo.get_IdOficina();
                                    AcIdPiso = ubicacionActivo.get_IdPiso();
                                    AcNombreCompania = ubicacionActivo.getCompania();
                                    AcNombreEdificio = ubicacionActivo.getEdificio();
                                    AcOficinaNombre = ubicacionActivo.getOficina();
                                    AcPisoNombre = ubicacionActivo.getPiso();
                                }

                                boolean respuesta = AssetsDBHelper.ActualizarUbicacionSeleccionado(AcIdCompania, AcNombreCompania, AcIdEdificio, AcNombreEdificio, AcIdPiso, AcPisoNombre, AcIdoficina, AcOficinaNombre, ajustarActivoUbicacions);
                                if (respuesta) {
                                    Toast.makeText(getApplicationContext(), "Se ha actualizado la ubicación de los activos", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "No se han podido actualizar los activos", Toast.LENGTH_SHORT).show();
                                }
                                ajustarEstado(UbicacionFinalView);
                            }
                        }).show();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void AlertaUbicacion(String Fillnumero, final String FillidActivo, String FilldOficina, String FillnombreOficina, String Filldescripcion){

        String Oficina = idOficina;
        actualizarUbicacionActivos = OfficesDBHelper.UbicacionCompleta(Oficina);
        String NombreOficinaNueva = OfficesDBHelper.NombreSectorActualiza(Oficina);

        if(FillidActivo.isEmpty()){
            Message();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("ALERTA")
                    .setMessage("No ha seleccionado un activo")
                    .setCancelable(false)

                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }}).show();
        }else{
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("REUBICAR ACTIVO")
                    .setMessage("Ha seleccionado al activo: "+ Filldescripcion+ " con el número de activo: "+ Fillnumero +"."+ "\n"+"Desea asignar asignarlo al sector: " + NombreOficinaNueva )
                    .setCancelable(false)
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (ActualizarUbicacionActivo ubicacionActivo: actualizarUbicacionActivos) {
                                AcIdCompania = ubicacionActivo.get_IdCompania();
                                AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                AcIdoficina  = ubicacionActivo.get_IdOficina();
                                AcIdPiso     = ubicacionActivo.get_IdPiso();
                                AcNombreCompania = ubicacionActivo.getCompania();
                                AcNombreEdificio = ubicacionActivo.getEdificio();
                                AcOficinaNombre  = ubicacionActivo.getOficina();
                                AcPisoNombre     = ubicacionActivo.getPiso();
                            }
                            boolean respuesta = AssetsDBHelper.ActualizarUbicacionActivo(AcIdCompania, AcNombreCompania, AcIdEdificio, AcNombreEdificio, AcIdPiso, AcPisoNombre, AcIdoficina, AcOficinaNombre, FillidActivo);
                            if(respuesta){
                                Toast.makeText(getApplicationContext(), "Se ha actualizado la ubicación del activo",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "No se ha podido actualizar el activo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }
    }

    private void ajustarTodo(final View view){
        try{

            if(inventarioVisuals.size() <= 0){
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("No ha realizado la lectura de tags")
                        .setCancelable(false)

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }}).show();
            }
            else{
                for(int position = 0; position < inventarioVisuals.size(); position++ ) {

                    Fillnumero = inventarioVisuals.get(position).getNumero();
                    FillidActivo = inventarioVisuals.get(position).getAssetSysId();
                    FilldOficina = inventarioVisuals.get(position).getIdOficina();
                    FillnombreOficina = inventarioVisuals.get(position).getOficina();
                    Filldescripcion = inventarioVisuals.get(position).getDescripcion();
                    entidadaAjustarUbicacion entidadAjustarUbicacion = new entidadaAjustarUbicacion(Fillnumero,
                            FillidActivo, FilldOficina, FillnombreOficina, Filldescripcion);
                    entidadaAjustarUbicacions.add(entidadAjustarUbicacion);
                }
                final String Oficina = idOficina;
                actualizarUbicacionActivos = OfficesDBHelper.UbicacionCompleta(Oficina);
                String NombreOficinaNueva = OfficesDBHelper.NombreSectorActualiza(Oficina);

                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("REUBICAR ACTIVO")
                        .setMessage("Desea asignar asignar todos los activos al sector: " + NombreOficinaNueva )
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ajustarEstado(UbicacionFinalView);
                            }
                        })
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (ActualizarUbicacionActivo ubicacionActivo: actualizarUbicacionActivos) {
                                    AcIdCompania = ubicacionActivo.get_IdCompania();
                                    AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                    AcIdoficina  = ubicacionActivo.get_IdOficina();
                                    AcIdPiso     = ubicacionActivo.get_IdPiso();
                                    AcNombreCompania = ubicacionActivo.getCompania();
                                    AcNombreEdificio = ubicacionActivo.getEdificio();
                                    AcOficinaNombre  = ubicacionActivo.getOficina();
                                    AcPisoNombre     = ubicacionActivo.getPiso();
                                }

                                boolean respuesta = AssetsDBHelper.ActualizarUbicacionTodo(AcIdCompania, AcNombreCompania, AcIdEdificio, AcNombreEdificio, AcIdPiso, AcPisoNombre, Oficina, AcOficinaNombre, entidadaAjustarUbicacions);
                                if(respuesta){
                                    Toast.makeText(getApplicationContext(), "Se ha actualizado la ubicación de los activos",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getApplicationContext(), "No se han podido actualizar los activos", Toast.LENGTH_SHORT).show();
                                }
                                ajustarEstado(UbicacionFinalView);
                            }
                        }).show();
            }


        }catch (Exception ex){
            ex.printStackTrace();
        }

    }



    private void ajustarResponsable(View view){
        try{
            final EntidadEmployees entidadEmployees = (EntidadEmployees) spEmpleados.getSelectedItem();
            final AssetsDBHelper assetsDBHelper = new AssetsDBHelper(context);
            if(_mapEntidadEmployees.size() > 0) {
                if (isMultiSelect) {
                    final ArrayList<AjustarActivoUbicacion> activosSeleccionados = new ArrayList(_activosSeleccionado.values());
                    if (activosSeleccionados.size() > 0) {

                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.alertaicono)
                                .setTitle("CAMBIAR RESPONSABLE")
                                .setMessage("Desea asignar los activos seleccionados al responsable: " + entidadEmployees.getName())
                                .setCancelable(false)
                                .setNegativeButton("No", null)
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        boolean respuesta = assetsDBHelper.ActualizarEmpleadoAsignado(entidadEmployees.getEmployeeSysId(), activosSeleccionados);
                                        if (respuesta) {
                                            Toast.makeText(getApplicationContext(), "Se ha actualizado el responsable de los activo", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No se han podido actualizar los activos", Toast.LENGTH_SHORT).show();
                                        }
                                        adaptadorAjusteUbicacion.updateData();
                                        txtEcontrados.setText("0");
                                        txtFaltantes.setText("0");
                                        txtSobrantes.setText("0");
                                    }
                                }).show();
                    } else {
                        MensajeAlerta2();
                    }
                } else {
                    if (inventarioVisuals.size() <= 0) {
                        Message();
                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.alertaicono)
                                .setTitle("ALERTA")
                                .setMessage("No ha realizado la lectura de tags")
                                .setCancelable(false)

                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    } else {
                        for (int position = 0; position < inventarioVisuals.size(); position++) {

                            Fillnumero = inventarioVisuals.get(position).getNumero();
                            FillidActivo = inventarioVisuals.get(position).getAssetSysId();
                            FilldOficina = inventarioVisuals.get(position).getIdOficina();
                            FillnombreOficina = inventarioVisuals.get(position).getOficina();
                            Filldescripcion = inventarioVisuals.get(position).getDescripcion();
                            entidadaAjustarUbicacion entidadAjustarUbicacion = new entidadaAjustarUbicacion(Fillnumero,
                                    FillidActivo, FilldOficina, FillnombreOficina, Filldescripcion);
                            entidadaAjustarUbicacions.add(entidadAjustarUbicacion);
                        }

                        Message();
                        new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alertaicono)
                            .setTitle("CAMBIAR RESPONSABLE")
                            .setMessage("Desea asignar asignar todos los activos al responsable: " + entidadEmployees.getName())
                            .setCancelable(false)
                            .setNegativeButton("No", null)
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (ActualizarUbicacionActivo ubicacionActivo : actualizarUbicacionActivos) {
                                        AcIdCompania = ubicacionActivo.get_IdCompania();
                                        AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                        AcIdoficina = ubicacionActivo.get_IdOficina();
                                        AcIdPiso = ubicacionActivo.get_IdPiso();
                                        AcNombreCompania = ubicacionActivo.getCompania();
                                        AcNombreEdificio = ubicacionActivo.getEdificio();
                                        AcOficinaNombre = ubicacionActivo.getOficina();
                                        AcPisoNombre = ubicacionActivo.getPiso();
                                    }

                                    boolean respuesta = assetsDBHelper.ActualizarTodosEmpleados(entidadEmployees.getEmployeeSysId(), entidadaAjustarUbicacions);
                                    if (respuesta) {
                                        Toast.makeText(getApplicationContext(), "Se ha actualizado el responsable de los activos", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No se han podido actualizar los activos", Toast.LENGTH_SHORT).show();
                                    }
                                    adaptadorAjusteUbicacion.updateData();
                                    txtEcontrados.setText("0");
                                    txtFaltantes.setText("0");
                                    txtSobrantes.setText("0");
                                }
                            }).show();
                    }
                }
            }else{
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("No ha seleccionado un responsable")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void ajustarEstado(View view){
        try{
            final EntidadAssetStatus entidadAssetStatus= (EntidadAssetStatus) spAssetStatus.getSelectedItem();
            final AssetsDBHelper assetsDBHelper = new AssetsDBHelper(context);

            if(isMultiSelect) {
                final ArrayList<AjustarActivoUbicacion> activosSeleccionados = new ArrayList(_activosSeleccionado.values());
                if(activosSeleccionados.size()>0){
                    Message();
                    new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("CAMBIAR ESTADO")
                        .setMessage("Desea asignar los activos seleccionados al estado: " + entidadAssetStatus.getName())
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(_mapEntidadEmployees.size()>0){
                                    ajustarResponsable(UbicacionFinalView);
                                }else{
                                    adaptadorAjusteUbicacion.updateData();
                                    txtEcontrados.setText("0");
                                    txtFaltantes.setText("0");
                                    txtSobrantes.setText("0");
                                }
                            }
                        })
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (ActualizarUbicacionActivo ubicacionActivo : actualizarUbicacionActivos) {
                                    AcIdCompania = ubicacionActivo.get_IdCompania();
                                    AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                    AcIdoficina = ubicacionActivo.get_IdOficina();
                                    AcIdPiso = ubicacionActivo.get_IdPiso();
                                    AcNombreCompania = ubicacionActivo.getCompania();
                                    AcNombreEdificio = ubicacionActivo.getEdificio();
                                    AcOficinaNombre = ubicacionActivo.getOficina();
                                    AcPisoNombre = ubicacionActivo.getPiso();
                                }

                                boolean respuesta = assetsDBHelper.ActualizarEstadoDelActivo(entidadAssetStatus.getId(),activosSeleccionados);
                                if (respuesta) {
                                    Toast.makeText(getApplicationContext(), "Se ha actualizado el estado del activo", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "No se ha podido actualizar el activo", Toast.LENGTH_SHORT).show();
                                }
                                if(_mapEntidadEmployees.size()>0){
                                    ajustarResponsable(UbicacionFinalView);
                                }else{
                                    adaptadorAjusteUbicacion.updateData();
                                    txtEcontrados.setText("0");
                                    txtFaltantes.setText("0");
                                    txtSobrantes.setText("0");
                                }
                            }
                        }).show();
                }else{
                    MensajeAlerta();

                }
            }else {
                if (inventarioVisuals.size() <= 0) {
                    Message();
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alertaicono)
                            .setTitle("ALERTA")
                            .setMessage("No ha realizado la lectura de tags")
                            .setCancelable(false)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else {
                    for (int position = 0; position < inventarioVisuals.size(); position++) {

                        Fillnumero = inventarioVisuals.get(position).getNumero();
                        FillidActivo = inventarioVisuals.get(position).getAssetSysId();
                        FilldOficina = inventarioVisuals.get(position).getIdOficina();
                        FillnombreOficina = inventarioVisuals.get(position).getOficina();
                        Filldescripcion = inventarioVisuals.get(position).getDescripcion();
                        entidadaAjustarUbicacion entidadAjustarUbicacion = new entidadaAjustarUbicacion(Fillnumero,
                                FillidActivo, FilldOficina, FillnombreOficina, Filldescripcion);
                        entidadaAjustarUbicacions.add(entidadAjustarUbicacion);
                    }
                    Message();
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alertaicono)
                            .setTitle("CAMBIAR ESTADO")
                            .setMessage("Desea asignar todos los activos al estado: " + entidadAssetStatus.getName())
                            .setCancelable(false)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(_mapEntidadEmployees.size()>0){
                                        ajustarResponsable(UbicacionFinalView);
                                    }else{
                                        adaptadorAjusteUbicacion.updateData();
                                        txtEcontrados.setText("0");
                                        txtFaltantes.setText("0");
                                        txtSobrantes.setText("0");
                                    }
                                }
                            })
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (ActualizarUbicacionActivo ubicacionActivo : actualizarUbicacionActivos) {
                                        AcIdCompania = ubicacionActivo.get_IdCompania();
                                        AcIdEdificio = ubicacionActivo.get_IdEdificio();
                                        AcIdoficina = ubicacionActivo.get_IdOficina();
                                        AcIdPiso = ubicacionActivo.get_IdPiso();
                                        AcNombreCompania = ubicacionActivo.getCompania();
                                        AcNombreEdificio = ubicacionActivo.getEdificio();
                                        AcOficinaNombre = ubicacionActivo.getOficina();
                                        AcPisoNombre = ubicacionActivo.getPiso();
                                    }

                                    boolean respuesta = assetsDBHelper.ActualizarTodoEstadoDelActivo(entidadAssetStatus.getId(),entidadaAjustarUbicacions);
                                    if (respuesta) {
                                        Toast.makeText(getApplicationContext(), "Se ha actualizado el estado de los activos", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No se han podido actualizar los activos", Toast.LENGTH_SHORT).show();
                                    }
                                    if(_mapEntidadEmployees.size()>0){
                                        ajustarResponsable(UbicacionFinalView);
                                    }else{
                                        adaptadorAjusteUbicacion.updateData();
                                        txtEcontrados.setText("0");
                                        txtFaltantes.setText("0");
                                        txtSobrantes.setText("0");
                                    }
                                }
                            }).show();
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    //endregion




   //region Clase de LongPress
   private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private AdaptadorAjusteUbicacion.OnLongPress onLongPress;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final AdaptadorAjusteUbicacion.OnLongPress onLongPress){
            this.onLongPress= onLongPress;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && onLongPress!=null){
                        onLongPress.onLongPress(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }


        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && onLongPress!=null && gestureDetector.onTouchEvent(e)){
                onLongPress.onClicPress(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    }
    //endregion

   // region Botones
    private View.OnClickListener ckAjustarSeleccionHabilita = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ckAjustarSeleccion.isChecked()){
                btnAjustarSeleccion.setEnabled(true);
            }
            else{
                btnAjustarSeleccion.setEnabled(false);
            }

        }
    };

    private  Button.OnClickListener OnClickListenerResultados = new View.OnClickListener(){
        @Override
        public void onClick(View view)
        {
           /* ArrayList<String> tags = new ArrayList<String>();
            tags.add("800474400000000000000079");
            tags.add("800474400000000000000088");
            tags.add("800474400000000000000059");
            _chequearUbicacion.CheckActivos(tags);*/
            VerResultado.setEnabled(false);
            VerResultado.setBackgroundColor(Color.rgb(170,170,170));
            BuscarEPCS(UbicacionFinalView);
            VerResultado();
            btnAjustarTodo.setEnabled(true);
            btnAjustarTodo.setBackgroundColor(Color.rgb(235,183,30));
           // BuscarEPCS(UbicacionFinalView);
        }
    };



    private  Button.OnClickListener OnClickListenerAjustarTodo = new View.OnClickListener()
    {
        @Override
        public void onClick(View view){
            btnAjustarTodo.setEnabled(false);
            btnAjustarTodo.setBackgroundColor(Color.rgb(170,170,170));
           if(_mapEntidadEmployees.size() > 0){
               if(isMultiSelect){
                   AjustarActivosSeleccionados(UbicacionFinalView);

               }else{
                   ajustarTodo(UbicacionFinalView);
               }
               VerResultado.setEnabled(true);
               VerResultado.setBackgroundColor(Color.rgb(235,183,30));
           }
           else
           {
               AlertasInformacion("Advertencia", "No ha seleccionado un responsable."+"\n"+"¿Desea continuar?");
           }

        }
    };

    private  Button.OnClickListener OnClickListenerAjustarSeleccion = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            AjustarActivosSeleccionados(UbicacionFinalView);
        }
    };
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            BuscarEPCS(UbicacionFinalView);
        }
    };

     //endregion

   //region Lectura
    /*Acción de lectura de tags con la HH*/

    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
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

    @Override
    public void handleTagdata(TagData[] tagData) {

        if(!scannerActivate){
            ArrayList<String> tags = new ArrayList<String>();
            for (int index = 0; index < tagData.length; index++) {
                _lastTag = tagData[index].getTagID();
                tags.add(tagData[index].getTagID());

            }
            if(_chequearUbicacion.CheckActivos(tags)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message();
                        //Toast.makeText(getApplicationContext(), _lastTag , Toast.LENGTH_LONG).show();
                    }


                });
            }
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        try{
            triggerPressed = pressed;
            if (pressed) {
                rfidHandler.performInventory();
            } else{
                rfidHandler.stopInventory();
                //handler.postDelayed(r, 1000);
            }
        }catch (Exception ex){
            Log.d(ex.getMessage(), ex.getStackTrace().toString());
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

            }
        });
    }

    //endregion


    //region AssetStatus

    private void cargarEstado() {
        AssetStatusDBHerlper assetStatusDBHerlper = new AssetStatusDBHerlper(this.getApplicationContext());
        _mapEntidadAssetStatus = assetStatusDBHerlper.GetAssetStatus();
        EntidadAssetStatus[] assetStatus = _mapEntidadAssetStatus.values().toArray(new EntidadAssetStatus[0]);
        fillSpinnerEstado(assetStatus);
    }

    private void fillSpinnerEstado(EntidadAssetStatus[] entidadAssetStatus){
        spAssetStatus.setAdapter( new ArrayAdapter<>(this.getApplicationContext()
                ,R.layout.spinner_layaout,entidadAssetStatus));
    }

    private View.OnClickListener btnAjustarEstado = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //ajustarEstado(UbicacionFinalView);


        }
    };

    //endregion

    //region Employees
    private void buscarEmpleadoPorDescripcion(){
        String nombreBusqueda = txtCodigoEmpleado.getText().toString();
        cargarEmpleadoPorDescripcion(nombreBusqueda);

    }

    private void cargarEmpleadoPorDescripcion(String descripcion) {
        EmployeesDBHelper employeesDBHelper = new EmployeesDBHelper(this.getApplicationContext());
        _mapEntidadEmployees = (Map<Integer, EntidadEmployees>) employeesDBHelper.GetEmployees(descripcion);
        EntidadEmployees[] entidadEmployees = _mapEntidadEmployees.values().toArray(new EntidadEmployees[0]);
        fillSpinnerEmployees(entidadEmployees);
    }

    private void fillSpinnerEmployees(EntidadEmployees[] entidadEmployees){
        spEmpleados.setAdapter( new ArrayAdapter<>(this.getApplicationContext()
                ,R.layout.spinner_layaout,entidadEmployees));
    }

    private View.OnClickListener btnAjustarEmpleado = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           //ajustarResponsable(UbicacionFinalView);
        }
    };
    private void MensajeAlerta2(){
        Message();
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.alertaicono)
                .setTitle("ALERTA")
                .setMessage("No ha seleccionado activos")
                .setCancelable(false)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }}).show();
    }
    private void MensajeAlerta(){
        Message();
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.alertaicono)
                .setTitle("ALERTA")
                .setMessage("No ha seleccionado activos")
                .setCancelable(false)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(_mapEntidadEmployees.size()>0){
                            ajustarResponsable(UbicacionFinalView);
                        }else{
                            adaptadorAjusteUbicacion.updateData();
                            txtEcontrados.setText("0");
                            txtFaltantes.setText("0");
                            txtSobrantes.setText("0");
                        }
                    }}).show();
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
                finish();
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
    public boolean AlertasInformacion(String titulo, String Mensaje){
        final boolean respuesta = false;
        LayoutInflater inflater = _activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones_error, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(titulo);
        txvMessage.setText(Mensaje);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(_activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Sí</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(isMultiSelect){
                    AjustarActivosSeleccionados(UbicacionFinalView);

                }else{
                    ajustarTodo(UbicacionFinalView);

                }
                VerResultado.setEnabled(true);
                VerResultado.setBackgroundColor(Color.rgb(235,183,30));
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>No</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                btnAjustarTodo.setEnabled(true);
                btnAjustarTodo.setBackgroundColor(Color.rgb(235,183,30));
                VerResultado.setEnabled(false);
                VerResultado.setBackgroundColor(Color.rgb(170,170,170));

            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();
        return  true;
    }
    //endregion
}
