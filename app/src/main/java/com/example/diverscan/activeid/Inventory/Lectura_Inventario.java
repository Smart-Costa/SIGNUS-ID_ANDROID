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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.RequiresApi;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.Activo.ActivoInventario;
import com.example.diverscan.activeid.Activo.AjustarActivoUbicacion;
import com.example.diverscan.activeid.Activo.EntidadActivos;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.zebra.rfid.api3.TagData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Lectura_Inventario extends AppCompatActivity implements ResponseHandlerInterface, IChequearInventario {
    AssetsDBHelper AssetsDBHelper;
    private TextView txt_TagsLeidos;
    private String Faltante = "Faltante", NoPertenece = "No Pertenece", Encontrado = "Encontrado",
            idTake, takeName, takeDescription, takeDate, idOficina, idTypeInventory;
    int contadorActivosFaltantes = 0, contActivosEncontrados = 0, contadorActivosSobrantes = 0;
    private RecyclerView ListaLectura;
    private EditText txtEncontrados, txtSobrantes, txtFaltantes, txtLeidas, txtBarcode, txtUbicacion;
    public int CantidadEPCLeida, EPCEncontrados, EPCSobrantes, EPCActivosUbicacion, EPCFaltantes;
    OfficesDBHelper OfficesDBHelper;
    private Button GuardarRessultado;
    AdaptadorLecturas itemAdapterAssets;
    private View LecturaInventarioView;
    private ArrayList<String> _activosEncontrados, _activosFaltantes, _activosActivoNoPertenece = new ArrayList<String>();
    private ArrayList<EntidadDetalleInventario> _detalleInventario = new ArrayList<EntidadDetalleInventario>();
    private ArrayList<EntidadInventario> _inventario = new ArrayList<EntidadInventario>();
    private ArrayList<ActivoInventario> activoInventarios = new ArrayList<ActivoInventario>();
    ArrayList<InventarioVisual> inventarioVisuals = new ArrayList<InventarioVisual>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectura_inventario);
        getSupportActionBar().hide();
        _activity = this;
        _context = this;
        controles();
        eventos();
        rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);
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
        GuardarRessultado.setOnClickListener(OnClickListenerGuardarResultado);
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

    private void AgregarActivoPlaca(String placa) {
        _chequearInventario.AgregarActivosBarcode(placa, this);
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

        idTake = getIntent().getExtras().getString("IdTake");
        takeName = getIntent().getExtras().getString("takeName");
        takeDescription = getIntent().getExtras().getString("takeDescription");
        takeDate = getIntent().getExtras().getString("takeDate");
        idOficina = getIntent().getExtras().getString("idOficina");
        idTypeInventory = getIntent().getExtras().getString("tipoInventario");
        activoInventarios = OfficesDBHelper.ActivosUbicacion(idOficina);

        _chequearInventario = new ChequearInventario(activoInventarios, idTake, this, this);

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
        inventarioVisuals.add(response);
        ListaLectura.setLayoutManager(new LinearLayoutManager(_context));
        ListaLectura.setAdapter(itemAdapterAssets);
        contadorActivosFaltantes++;
        txtFaltantes.setText(String.valueOf(contadorActivosFaltantes));
    }


    public void fillRecyclerView(InventarioVisual response) {
        try {
            int TamanoLista = inventarioVisuals.size();
            if (TamanoLista > 0) {
                for (int item = 0; item < inventarioVisuals.size(); item++) {
                    if (inventarioVisuals.get(item).getAssetSysId().equals(response.getAssetSysId()) &&
                            !inventarioVisuals.get(item).getStatus().equals(response.getStatus())) {
                        inventarioVisuals.get(item).setStatus(response.getStatus());
                        itemAdapterAssets.notifyItemChanged(item);
                        contActivosEncontrados++;
                        contadorActivosFaltantes--;
                        txtFaltantes.setText(String.valueOf(contadorActivosFaltantes));
                        txtEncontrados.setText(String.valueOf(contActivosEncontrados));
                        return;
                    }
                }
                if (!inventarioVisuals.contains(response.getAssetSysId())) {
                    inventarioVisuals.add(response);
                    itemAdapterAssets.notifyItemInserted((TamanoLista));
                    contadorActivosSobrantes++;
                    txtSobrantes.setText(String.valueOf(contadorActivosSobrantes));
                    return;
                }
            }
            inventarioVisuals.add(response);
            ListaLectura.setLayoutManager(new LinearLayoutManager(_context));
            ListaLectura.setAdapter(itemAdapterAssets);
            contadorActivosSobrantes++;
            txtSobrantes.setText(String.valueOf(contadorActivosSobrantes));
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
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    //*************************************************************************************************************

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        Toast.makeText(_context, status, Toast.LENGTH_LONG).show();
    }

    //*************************************************************************************************************

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

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
