package com.example.diverscan.activeid.Locate_Assets;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.Activo.ActivoInventario;
import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioNuevo;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Inventory.EntidadTomaFisicaEPC;
import com.example.diverscan.activeid.Inventory.Entidad_TomaDetalle;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Piso.PisoNuevo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RazonSocial.RazonNuevo;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AsignarUbicacion extends AppCompatActivity implements ResponseHandlerInterface {
    private View ElegirUbicacionView;
    Spinner spinnerTiposInventariosView;
    com.example.diverscan.activeid.sqlite.InventoryDBHelper InventoryDBHelper;
    com.example.diverscan.activeid.sqlite.OfficesDBHelper OfficesDBHelper;
    ChequearUbicacion Inventario;
    ArrayList<ActivoInventario> inventarioVisual;
    List<String> listSpinner;
    ArrayList<com.example.diverscan.activeid.Inventory.Inventories> Inventories;
    ArrayList<Entidad_TomaDetalle> _entidadTomaDetalle;
    Button btn_elegirubicacionmanual;
    private EditText EPCView;
    private RadioButton radUbicManual;
    private RadioButton radUbicEpc;
    private Button btn_ver;
    private EditText txtSector;
    private TextView lblInventario;
    private View contenedorEpc;
    private View contenedorSpinner;
    private View contenedorTipoInventario;
    private View contenedorVista;
    private Button btn_continuar;
    private EditText txtSectorBusqueda;
    private View mElegirUbicacion;
    private View contenedorLblRazon;
    private View contenedorSpinnerRazon;
    private View contenedorLblEdificio;
    private View contenedorSpinnerEdificio;
    private View contenedorLblPiso;
    private View contenedorSpinnerPiso;
    private View contenedorLblSector;
    private View contenedorSpinnerSector;
    private View Espacio1;
    private View Espacio2;
    private View Espacio3;
    private View Espacio4;
    private View Espacio5;
    private View Espacio6;
    private View Espacio7;
    private View Espacio8;
    private View Espacio9;
    private View Espacio10;
    private View Espacio11;
    private Spinner CompaniaView;
    private Spinner EdificioView;
    private Spinner PisoView;
    private Spinner OficinaView;
    private Map<Integer, RazonNuevo> _mapRazonSociales = new HashMap<Integer, RazonNuevo>();
    private Map<Integer, EdificioNuevo> _mapEdificios = new HashMap<Integer, EdificioNuevo>();
    private Map<Integer, PisoNuevo> _mapPisos = new HashMap<Integer, PisoNuevo>();
    private Map<Integer, oficinaNuevo> _mapOficinas = new HashMap<Integer, oficinaNuevo>();
    String idCompaniaActivo, idedificioActivo, idpisoActivo, idoficinaActivo;
    private String epcView, oficinaNombre, Sector;
    private boolean _itemSelectedUserCompania, _itemSelectedUserEdificio, _itemSelectedUserPiso = true;
    private String idOficinaEPC;
    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    Context _context;

    //***************************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_ubicacion);
        _context = this;
        controles();
        eventos();

        rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);

        //Toast.makeText(getApplicationContext(), rfidHandler.Defaults(), Toast.LENGTH_LONG).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(AsignarUbicacion.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
    }

    //***************************************************************************************************************

    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }

    //***************************************************************************************************************

    //region Controles
    public void controles(){
        EPCView = findViewById(R.id.txt_epcInventario);
        ElegirUbicacionView = findViewById(R.id.elegirubicacion);
        InventoryDBHelper = new InventoryDBHelper(ElegirUbicacionView.getContext());
        radUbicEpc = findViewById(R.id.rbt_ubicPorEpc);
        radUbicManual = findViewById(R.id.rbt_ubicManual);
        btn_ver = findViewById(R.id.btn_ver);
        txtSector = findViewById(R.id.SectorTexto);
        btn_continuar=findViewById(R.id.btn_continua);
        CompaniaView = findViewById(R.id.compania_toma);
        EdificioView = findViewById(R.id.edificio_toma);
        PisoView = findViewById(R.id.piso_toma);
        OficinaView = findViewById(R.id.oficina_toma);
        contenedorEpc = findViewById(R.id.PanelEpc);
        contenedorVista = findViewById(R.id.panelVista);
        contenedorLblRazon = findViewById(R.id.PanelTituloRazon);
        contenedorSpinnerRazon = findViewById(R.id.PanelRazonSpinner);
        contenedorLblEdificio = findViewById(R.id.PanelTituloEdificio);
        contenedorSpinnerEdificio = findViewById(R.id.PanelEdificioSpinner);
        contenedorLblPiso = findViewById(R.id.PanelTituloPiso);
        contenedorSpinnerPiso = findViewById(R.id.PanelPisoSpinner);
        contenedorLblSector = findViewById(R.id.PanelTituloSector);
        contenedorSpinnerSector = findViewById(R.id.PanelSectorSpinner);
        Espacio1 = findViewById(R.id.Espacio1);
        Espacio2 = findViewById(R.id.Espacio2);
        Espacio3 = findViewById(R.id.Espacio3);
        Espacio4 = findViewById(R.id.Espacio4);
        Espacio5 = findViewById(R.id.Espacio5);
        Espacio6 = findViewById(R.id.Espacio6);
        Espacio7 = findViewById(R.id.Espacio7);
        Espacio8 = findViewById(R.id.Espacio8);
        Espacio9 = findViewById(R.id.Espacio9);
        Espacio10 = findViewById(R.id.Espacio10);
        Espacio11 = findViewById(R.id.Espacio11);
        OfficesDBHelper = new OfficesDBHelper(ElegirUbicacionView.getContext());
        txtSectorBusqueda = findViewById(R.id.etxtSectorBusqueda);
    }
    //endregion

    //***************************************************************************************************************

    //region Eventos
    public void eventos(){
        CompaniaView.setOnItemSelectedListener(onItemSpinnerListenerCompania);
        EdificioView.setOnItemSelectedListener(onItemSpinnerListenerEdificio);
        PisoView.setOnItemSelectedListener(onItemSpinnerListenerPiso);
        btn_ver.setOnClickListener(OnClickListenerSectorPorTag);
        btn_continuar.setOnClickListener(OnClickListenerIrToma);

        verPorEPC(false);
        limpiarCampos();
        cargarRazonesSociales();
        cargarUbicaciones();

//        txtSectorBusqueda.addTextChangedListener(new TextWatcher() {
//                                         @Override
//                                         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                                         }
//                                         @Override
//                                         public void onTextChanged(CharSequence s, int start, int before, int count) {
//                                         }
//                                         @Override
//                                         public void afterTextChanged(Editable s) {
//                                             if(txtSectorBusqueda.getText().length() > 3){
//                                                 buscarOficinaPorIdPisoNombre();
//                                             }else if(txtSectorBusqueda.getText().length() == 0){
//                                                 PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
//                                                 cargarOficinas(pisoRecord.getIdPiso());
//                                             }
//                                         }
//        });
    }
    //endregion

    //***************************************************************************************************************

    //region Spinners
    /*Se rellenan los spinners*/
    private void cargarRazonesSociales() {
        RazonSocialDBHelper razonSocialDBHelper = new RazonSocialDBHelper(ElegirUbicacionView.getContext());
        _mapRazonSociales = (Map<Integer, RazonNuevo>) razonSocialDBHelper.ObtenerRazon();
        RazonNuevo[] razones = _mapRazonSociales.values().toArray(new RazonNuevo[0]);
        fillSpinnerRazon(razones);
    }

    //***************************************************************************************************************

    private void cargarEdificios(String idCompania) {
        EdificioDBHelper edificioDBHelper = new EdificioDBHelper(ElegirUbicacionView.getContext());
        _mapEdificios = (Map<Integer, EdificioNuevo>) edificioDBHelper.ObtenerEdificio(idCompania);
        EdificioNuevo[] edificios = _mapEdificios.values().toArray(new EdificioNuevo[0]);
        fillSpinnerEdificio(edificios);
    }

    //***************************************************************************************************************

    private void cargarPisos(String idEdificio) {
        PisoDBHelper pisoDBHelper = new PisoDBHelper(ElegirUbicacionView.getContext());
        _mapPisos = (Map<Integer, PisoNuevo>) pisoDBHelper.ObtenerPiso(idEdificio);
        PisoNuevo[] pisos = _mapPisos.values().toArray(new PisoNuevo[0]);
        fillSpinnerPiso(pisos);
    }

    //***************************************************************************************************************

    private void cargarOficinas(String idPiso) {
        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(ElegirUbicacionView.getContext());
        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficina(idPiso);
        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
        fillSpinnerOficina(oficinas);
    }

    //***************************************************************************************************************

//    private void cargarOficinasPorPisoNombre(String idPiso,String nombre) {
//        OficinaDBHelper oficinaDBHelper = new OficinaDBHelper(ElegirUbicacionView.getContext());
//        _mapOficinas = (Map<Integer, oficinaNuevo>) oficinaDBHelper.ObtenerOficinaPorPisoDescripcion(idPiso,nombre);
//        oficinaNuevo[] oficinas = _mapOficinas.values().toArray(new oficinaNuevo[0]);
//        fillSpinnerOficina(oficinas);
//    }

    //***************************************************************************************************************

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

    //***************************************************************************************************************

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

    //***************************************************************************************************************

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

    //***************************************************************************************************************

    private void fillSpinnerRazon(RazonNuevo[] razonSocialRecords){
        CompaniaView = findViewById(R.id.compania_toma);
        CompaniaView.setAdapter( new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,razonSocialRecords));
    }

    //***************************************************************************************************************

    private void fillSpinnerEdificio(EdificioNuevo[] edificioRecords){
        EdificioView = findViewById(R.id.edificio_toma);
        EdificioView.setAdapter( new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,edificioRecords));
    }

    //***************************************************************************************************************

    private void fillSpinnerPiso(PisoNuevo[] pisoRecords){
        PisoView = findViewById(R.id.piso_toma);
        PisoView.setAdapter( new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,pisoRecords));
    }

    //***************************************************************************************************************

    private void fillSpinnerOficina(oficinaNuevo[] oficinaRecords){
        OficinaView = findViewById(R.id.oficina_toma);
        OficinaView.setAdapter( new ArrayAdapter<>(ElegirUbicacionView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout,oficinaRecords));
    }
    //endregion

    //***************************************************************************************************************

    //region Limpiar campos
    /*Muestra los campos que se necesiten para la busqueda*/
    public void verPorEPC(boolean b) {
        contenedorVista.setVisibility(b ? View.VISIBLE : View.GONE );
        contenedorEpc.setVisibility(b ? View.VISIBLE : View.GONE );
        Espacio1.setVisibility(b ? View.VISIBLE : View.GONE );
        Espacio4.setVisibility(b ? View.VISIBLE : View.GONE );
        Espacio3.setVisibility(b ? View.VISIBLE : View.GONE );
        Espacio2.setVisibility(b ? View.VISIBLE : View.GONE );
        contenedorLblRazon.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorSpinnerRazon.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorLblEdificio.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorSpinnerEdificio.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorLblPiso.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorSpinnerPiso.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorLblSector.setVisibility(b ? View.GONE: View.VISIBLE );
        contenedorSpinnerSector.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio5.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio6.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio7.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio8.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio9.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio10.setVisibility(b ? View.GONE: View.VISIBLE );
        Espacio11.setVisibility(b ? View.GONE: View.VISIBLE );
    }

    public void limpiarCampos() {
        EPCView.getText().clear();
        txtSector.getText().clear();
    }
    //endregion

    //***************************************************************************************************************

    //region Acción de lectura de tags con la HH
    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    //***************************************************************************************************************

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
    }

    //***************************************************************************************************************

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

    //***************************************************************************************************************

    @Override
    public void handleTagdata(TagData[] tagData) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tagData.length; index++) {
            sb.append(tagData[index].getTagID() + "\n");
            _lastTag = tagData[index].getTagID();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //epcView.append(sb.toString());
                EPCView.setText(_lastTag);
            }
        });
    }

    //***************************************************************************************************************

    public static void Message(){
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }

    //***************************************************************************************************************

    @Override
    public void handleTriggerPress(boolean pressed) {
        triggerPressed = pressed;
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EPCView.setText("");
                    Message();
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }

    //***************************************************************************************************************

    @Override
    public Context GetContext() {
        return this;
    }

    //***************************************************************************************************************

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

    //***************************************************************************************************************

    //region Metodos Principales
    /*Método para buscar los sectores por tag*/
    public void buscarSector (View view){

        epcView = EPCView.getText().toString();

        try{
            if (TextUtils.isEmpty(epcView)){
                EPCView.setError("Debe leer un tag");
                return;
            }

            EntidadTomaFisicaEPC entidadtomaFisicaEPC;
            if (epcView != null){
                entidadtomaFisicaEPC = OfficesDBHelper.VerSectorEPC(epcView);
            }else{
                EPCView.setError("Debe leer un tag");
                return;
            }

            if(entidadtomaFisicaEPC != null){
                txtSector.setText(entidadtomaFisicaEPC.getNombreOficina());
                idOficinaEPC = entidadtomaFisicaEPC.getIdOficina();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ElegirUbicacionView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ATENCIÓN");
                builder.setMessage("El tag leído no se encuentra asociado a una ubicación.");
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
        }catch (Exception e){
            Log.d(e.getMessage(), e.getStackTrace().toString());
        }
    }

    //***************************************************************************************************************

    public String  idOficina;

    public void LeerInventarioManual(){
        try{

            if(TextUtils.isEmpty(OficinaView.getSelectedItem().toString())){
                AlertDialog.Builder builder = new AlertDialog.Builder(mElegirUbicacion.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ATENCIÓN");
                builder.setMessage("Seleccione una ubicación.");
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
            else{
                try{

                    oficinaNuevo oficinaRecord = (oficinaNuevo) OficinaView.getSelectedItem();
                    oficinaNombre = oficinaRecord.getNombreOficina();
                    idOficina = oficinaRecord.getIdOficina();

                    inventarioVisual = OfficesDBHelper.ActivosUbicacion(idOficina);

                    if(inventarioVisual.size() <= 0){

                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.alertaicono)
                                .setTitle("Advertencia")
                                .setMessage("Este sector no posee activos. ¿Desea continuar?")
                                .setCancelable(false)
                                .setNegativeButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivoInventario inventarioVisualq = new ActivoInventario("0000", "No existe","EPC", "Sin Asignar", "00000000-0000-0000-0000-000000000000", "Sin Asignar", "00000000-0000-0000-0000-000000000000" );
                                        ArrayList<ActivoInventario> visuals = new ArrayList<ActivoInventario>();
                                        visuals.add(inventarioVisualq);
                                        Inventario = new ChequearUbicacion(visuals, AsignarUbicacion.this);

                                        Intent intent = new Intent(AsignarUbicacion.this, AsignarUbicacionTodo.class);

                                        intent.putExtra("inventarioVisual", visuals);
                                        intent.putExtra("idOficina", idOficina);
                                        startActivity(intent);
                                    }
                                })
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                    } else {
                        Inventario = new ChequearUbicacion(inventarioVisual, this);

                        Intent intent = new Intent(AsignarUbicacion.this, AsignarUbicacionTodo.class);
                        intent.putExtra("inventarioVisual", inventarioVisual);
                        intent.putExtra("idOficina", idOficina);
                        startActivity(intent);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //***************************************************************************************************************

    public void LeerInventarioEPC(){
        try{

            if(TextUtils.isEmpty(txtSector.getText())) {
                txtSector.setError("Debe leer una ubicación");
                Toast.makeText(getApplicationContext(), "Seleccione una ubicación", Toast.LENGTH_LONG).show();
            }
            else{
                try{

                    Sector = txtSector.getText().toString();
                    String EPC = EPCView.getText().toString();
                    inventarioVisual = OfficesDBHelper.ActivosUbicacionEPC(idOficinaEPC, EPC);

                    if(inventarioVisual.size() <= 0){

                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.alertaicono)
                                .setTitle("Advertencia")
                                .setMessage("Este sector no posee activos. ¿Desea continuar?")
                                .setCancelable(false)
                                .setNegativeButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivoInventario inventarioVisualq = new ActivoInventario("0000", "No existe","EPC", "Sin Asignar", "00000000-0000-0000-0000-000000000000", "Sin Asignar", "00000000-0000-0000-0000-000000000000" );
                                        ArrayList<ActivoInventario> visuals = new ArrayList<ActivoInventario>();
                                        visuals.add(inventarioVisualq);
                                        Inventario = new ChequearUbicacion(visuals, AsignarUbicacion.this);

                                        Intent intent = new Intent(AsignarUbicacion.this , AsignarUbicacionTodo.class);
                                        intent.putExtra("inventarioVisual", visuals);
                                        intent.putExtra("idOficina", idOficinaEPC);
                                        startActivity(intent);
                                    }
                                })
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                    } else {
                        Intent intent = new Intent(AsignarUbicacion.this, AsignarUbicacionTodo.class);
                        intent.putExtra("inventarioVisual", inventarioVisual);
                        intent.putExtra("idOficina", idOficinaEPC);
                        startActivity(intent);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //endregion

    //***************************************************************************************************************

    //region Botones
    private Button.OnClickListener OnClickListenerSectorPorTag = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buscarSector(ElegirUbicacionView);
        }
    };

    //***************************************************************************************************************

    /*Acción de los radio buttons*/
    public void onRadioButtonClicked(View view) {
        boolean marcado = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.rbt_ubicManual:
                if(marcado){
                    verPorEPC(false);
                    limpiarCampos();
                    cargarRazonesSociales();
                    cargarUbicaciones();
                }break;
            case R.id.rbt_ubicPorEpc:
                if(marcado){
                    verPorEPC(true);
                    cargarRazonesSociales();
                    cargarUbicaciones();
                }break;
        }
    }

    //***************************************************************************************************************

    private void cargarUbicaciones(){
        RazonNuevo razonSocialRecord= (RazonNuevo) CompaniaView.getSelectedItem();
        idCompaniaActivo = razonSocialRecord.getIdRazon();
        cargarEdificios(idCompaniaActivo);

        EdificioNuevo edificioRecord = (EdificioNuevo)EdificioView.getSelectedItem();
        idedificioActivo = edificioRecord.getIdEdificio();
        cargarPisos(idedificioActivo);

        PisoNuevo pisoRecord = (PisoNuevo)PisoView.getSelectedItem();
        idpisoActivo = pisoRecord.getIdPiso();
        cargarOficinas(idpisoActivo);
    }

    //***************************************************************************************************************

    private Button.OnClickListener OnClickListenerIrToma = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(radUbicManual.isChecked()) {
                LeerInventarioManual();
            }else if(radUbicEpc.isChecked()){
                LeerInventarioEPC();
            }else{
                Toast.makeText(getApplicationContext(), "Seleccione una opción", Toast.LENGTH_LONG).show();
            }
        }
    };

    //***************************************************************************************************************

//    private void buscarOficinaPorIdPisoNombre(){
//        if (_itemSelectedUserPiso) {
//        PisoNuevo pisoRecord= (PisoNuevo) PisoView.getSelectedItem();
//        String nombreBusqueda = txtSectorBusqueda.getText().toString();
//        cargarOficinasPorPisoNombre(pisoRecord.getIdPiso(),nombreBusqueda);
//        }
//        _itemSelectedUserPiso = true;
//    }
    //endregion

    //***************************************************************************************************************
}
