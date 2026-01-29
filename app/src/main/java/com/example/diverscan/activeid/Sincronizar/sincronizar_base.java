package com.example.diverscan.activeid.Sincronizar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.dao.RolDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.dao.UbicacionDao;
import com.example.diverscan.activeid.data.local.dao.UserDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.local.entity.EmpresaEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.JsonElement;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.diverscan.activeid.TomasFisicas.ActivoRecord;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivos;
import com.example.diverscan.activeid.TomasFisicas.EntidadCategoriaActivos;
import com.example.diverscan.activeid.TomasFisicas.NuevoActivo;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Assign_tag_Offices.sincronizarTag;
import com.example.diverscan.activeid.Conexion.ACTIVEID_API;
import com.example.diverscan.activeid.Employees.EntidadEmployees;
import com.example.diverscan.activeid.FotoActivo.EFotoActivo;
import com.example.diverscan.activeid.Inventory.EntidadDetalleInventario;
import com.example.diverscan.activeid.Inventory.EntidadEdificios;
import com.example.diverscan.activeid.Inventory.EntidadInventario;
import com.example.diverscan.activeid.Inventory.EntidadOficina2;
import com.example.diverscan.activeid.Inventory.EntidadPisos;
import com.example.diverscan.activeid.Inventory.EntidadRazonSocial;
import com.example.diverscan.activeid.Inventory.EntidadTiposInventarios;
import com.example.diverscan.activeid.Inventory.EntidadTomasInventario;
import com.example.diverscan.activeid.Inventory.EntidadUsuarios;
import com.example.diverscan.activeid.Inventory.Entidad_TomaDetalle;
import com.example.diverscan.activeid.Inventory.Entidad_TomaFisica;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Roles.EntidadDatosRol;
import com.example.diverscan.activeid.Tags.EntidadTags;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.FotoDBHelper;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.example.diverscan.activeid.sqlite.SincronizarDBHelper;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;
import com.example.diverscan.activeid.sqlite.newAssets;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.diverscan.activeid.Utilities.Fechas.parserJSONDate;

//Librerias de consumir el web services
public class sincronizar_base extends AppCompatActivity {

    private UserDao userDao;
    private ActivoDao activoDao;
    private RolDao rolDao;
    private UbicacionDao ubicacionDao;
    private TomaFisicaDao tomafisicaDao;
    private TomaFisicaTomasDao tomafisicatomasDao;
    private TomaFisicaDetallesDao tomafisicadetallesDao;
    private SincronizarDBHelper SincronizarDBHelper;
    private String IP = "www.google.com";
    public final ArrayList<Entidad_TomaFisica>        listTomaUpdate        = new ArrayList<>();
    public final ArrayList<Entidad_TomaDetalle>       listTomaDetalle       = new ArrayList<>();
    public final ArrayList<EntidadTomasInventario>    listTomasInventario   = new ArrayList<>();
    public final ArrayList<EntidadInventario>         listInventario        = new ArrayList<>();
    public final ArrayList<EntidadDetalleInventario>  listDetalleInventario = new ArrayList<>();
    ArrayList<EntidadRazonSocial> Razon;
    ArrayList<EntidadEdificios> Edificio;
    ArrayList<EntidadPisos> Piso;
    ArrayList<EntidadOficina2> Oficina;
    ArrayList<EntidadUsuarios> Usuario;
    ArrayList<Entidad_TomaFisica> TomaFisica;
    ArrayList<EntidadDatosRol> RolHH;
    ArrayList<EntidadTiposInventarios> TipoInventarios;
    ArrayList<Entidad_TomaDetalle> TomaDetalle;
    ArrayList<EntidadTags> Tags;
    ArrayList<EntidadTiposTags> tipoTags;
    ArrayList<EntidadCategoriaActivos> categoriaActivos;

    private ConstraintLayout rlsnackbar;
    private Snackbar _snackbar;
    private View mSincronizarView;
    private Button btn_enviar, btn_obtener, btn_limpiar_bd;
    private Spinner PreOpcionesSincr;
    private RadioButton radio_sincro, radio_Tags;
    private TextView Mensaje;
    private TextView tvLastSyncDate;
    private LinearLayout progressSegmented;
    private View[] pasos;
    private ProgressDialog dialogEnvio;
    private Context _context;
    private ArrayList<String> enviados = new ArrayList<String>();
    private ArrayList<String> Noenviados = new ArrayList<String>();
    private Activity _activity;
    private boolean _isConnected = true;
    android.app.AlertDialog alertDialog;
    private int exitosEnviados = 0, exitosRecibidos = 0;
    private static final int TOTAL_SEGMENTOS = 5;
    int Exitos=0;
    int enviadosSinExito =0;
    int noHay = 0;

    // Debug UI
    private LinearLayout debugContainer;
    private TextView tvDebugUsuarios, tvDebugRoles, tvDebugUbicaciones, tvDebugActivos, tvDebugTomas, tvDebugTomasResumen, tvDebugTomasDetalle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar_base_webservice);

        _context = this;
        _activity = this;
        controles();
        eventos();
        CargarOpciones();

        userDao = new UserDao(this);
        activoDao = new ActivoDao(this);
        rolDao = new RolDao(this);
        ubicacionDao = new UbicacionDao(this);
        tomafisicaDao = new TomaFisicaDao(this);
        tomafisicatomasDao = new TomaFisicaTomasDao(this);
        tomafisicadetallesDao = new TomaFisicaDetallesDao(this);


        // Configuración visual
        pasos = new View[]{
                findViewById(R.id.step1),
                findViewById(R.id.step2),
                findViewById(R.id.step3),
                findViewById(R.id.step4),
                findViewById(R.id.step5)
        };

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        IntentFilter intentFilter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    public void controles() {
        mSincronizarView = findViewById(R.id.SincronizarForm);
        SincronizarDBHelper = new SincronizarDBHelper(mSincronizarView.getContext());
        btn_enviar = findViewById(R.id.btn_enviar);
        btn_obtener = findViewById(R.id.btn_obtener);
        btn_limpiar_bd = findViewById(R.id.btn_limpiar_bd); // Initialized
        
        PreOpcionesSincr = findViewById(R.id.SpinnerSincronizacion);
        Mensaje = findViewById(R.id.mensaje);
        tvLastSyncDate = findViewById(R.id.tv_last_sync_date);
        rlsnackbar = findViewById(R.id.sincronizar_view);
        progressSegmented = findViewById(R.id.progress_segmented);

        loadLastSyncDate();

        // Debug UI initialization
        debugContainer = findViewById(R.id.debug_container);
        tvDebugUsuarios = findViewById(R.id.tv_debug_usuarios);
        tvDebugRoles = findViewById(R.id.tv_debug_roles);
        tvDebugUbicaciones = findViewById(R.id.tv_debug_ubicaciones);
        tvDebugActivos = findViewById(R.id.tv_debug_activos);
        tvDebugTomas = findViewById(R.id.tv_debug_tomas);
        tvDebugTomasResumen = findViewById(R.id.tv_debug_tomas_resumen);
        tvDebugTomasDetalle = findViewById(R.id.tv_debug_tomas_detalle);
    }

    public void eventos() {
        btn_enviar.setOnClickListener(OnClickListenerEnviar);
        btn_obtener.setOnClickListener(OnClickListenerObtener);
        if (btn_limpiar_bd != null) {
            btn_limpiar_bd.setOnClickListener(OnClickListenerLimpiarBD);
        }
    }

    private void loadLastSyncDate() {
        android.content.SharedPreferences prefs = getSharedPreferences("ActiveID_Prefs", MODE_PRIVATE);
        String lastDate = prefs.getString("last_sync_date", "--/--/---- --:--");
        if (tvLastSyncDate != null) {
            tvLastSyncDate.setText("Última actualización: " + lastDate);
        }
    }

    private void saveLastSyncDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        
        android.content.SharedPreferences prefs = getSharedPreferences("ActiveID_Prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_sync_date", currentDate);
        editor.apply();
        
        if (tvLastSyncDate != null) {
            tvLastSyncDate.setText("Última actualización: " + currentDate);
        }
    }

    public final View.OnClickListener OnClickListenerLimpiarBD = v -> {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar limpieza")
                .setMessage("¿Estás seguro de que quieres eliminar TODOS los datos locales? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    com.example.diverscan.activeid.data.local.dao.AppDatabaseHelper helper = new com.example.diverscan.activeid.data.local.dao.AppDatabaseHelper(mSincronizarView.getContext());
                    helper.clearAllData();
                    mostrarSnack("Base de datos limpiada correctamente.", Color.rgb(4, 165, 77));
                    updateDebugSummary();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    };

    private void CargarOpciones() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.opc_sincronizacion, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        PreOpcionesSincr.setAdapter(adapter);
    }

    private final BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
    };

    private void onNetworkChange(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.isConnected()) {
            //mostrarSnack("Conexión activa: " + networkInfo.getExtraInfo(), Color.rgb(4, 165, 77));
            _isConnected = true;
        } else {
            mostrarSnack("Sin conexión a Internet.", Color.rgb(242, 59, 59));
            _isConnected = false;
        }
    }

    private void mostrarSnack(String mensaje, int color) {
        Snackbar snackbar = Snackbar.make(rlsnackbar, mensaje, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(color);
        snackbar.show();
    }

    private void actualizarBarraSegmentada(int progreso) {
        int segmentoActual = (int) ((progreso / 100.0) * TOTAL_SEGMENTOS);
        for (int i = 0; i < TOTAL_SEGMENTOS; i++) {
            int color = (i < segmentoActual) ? Color.parseColor("#FF5100") : Color.parseColor("#CCCCCC");
            cambiarColorSuave(pasos[i], color);
        }
    }

    private void resetBarraSegmentada() {
        for (View paso : pasos) {
            cambiarColorSuave(paso, Color.parseColor("#CCCCCC"));
        }
    }

    private void cambiarColorSuave(View view, int nuevoColor) {
        Drawable background = view.getBackground();

        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(nuevoColor);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(nuevoColor);
        } else {
            view.setBackgroundColor(nuevoColor);
        }
    }

    public final View.OnClickListener OnClickListenerObtener = v -> {
        btn_obtener.setEnabled(false);

        if (!_isConnected) {
            btn_enviar.setEnabled(true);
            return;
        }

        resetBarraSegmentada();
        btn_enviar.setEnabled(false);
        if (debugContainer != null) debugContainer.setVisibility(View.GONE);

        // Limpiar datos sincronizados previos (excepto cambios pendientes)
        // Solo para Activos, ya que los otros (Usuarios, Roles, Ubicaciones) se limpian completos en su DAO
        // o no manejan estado pendiente.
        activoDao.clearSyncedData();

        // Iniciar secuencia de sincronización directamente
        startSyncSequence();
    };

    private void startSyncSequence() {
        // 1. Usuarios
        userDao.fetchAndSyncFromApi(() -> {
            runOnUiThread(() -> actualizarBarraSegmentada(15));
            
            // 2. Roles
            rolDao.fetchAndSyncFromApi(() -> {
                runOnUiThread(() -> actualizarBarraSegmentada(30));

                // 3. Ubicaciones
                ubicacionDao.fetchAndSyncFromApi(() -> {
                    runOnUiThread(() -> actualizarBarraSegmentada(45));

                    // 3.5 Categorias
                    getCategoriaActivos(() -> {

                        // 4. Activos
                        runOnUiThread(() -> Mensaje.setText("Iniciando descarga de Activos..."));
                        
                        activoDao.setSyncProgressListener((page, count, totalPages) -> {
                             runOnUiThread(() -> {
                                 String progressText;
                                 if (totalPages > 0) {
                                     progressText = "Página " + page + " de " + totalPages;
                                 } else {
                                     progressText = "Página " + page;
                                 }

                                 if (count == 0) {
                                     Mensaje.setText("Descargando Activos:\nSolicitando " + progressText + "...");
                                 } else {
                                     Mensaje.setText("Descargando Activos:\nProcesados " + count + " registros (" + progressText + ")...");
                                 }
                             });
                        });

                        activoDao.fetchAndSyncFromApi(() -> {
                            runOnUiThread(() -> {
                                actualizarBarraSegmentada(60);
                                Mensaje.setText("Sincronización de activos completada.");
                            });

                        // 5. Tomas Fisicas (Encabezados)
                        tomafisicaDao.fetchAndSyncFromApi(() -> {
                            runOnUiThread(() -> actualizarBarraSegmentada(75));

                            // 6. Tomas Fisicas (Resumen/Tomas)
                            tomafisicatomasDao.fetchAndSyncFromApi(() -> {
                                runOnUiThread(() -> actualizarBarraSegmentada(90));

                                // 7. Tomas Fisicas (Detalles)
                                // Fetch details ONLY for the currently active/visible Tomas Fisicas
                                List<TomaFisicaEntity> activeTomas = tomafisicaDao.getAllTomasFisicas();
                                if (activeTomas != null && !activeTomas.isEmpty()) {
                                    final int[] processedCount = {0};
                                    final int totalTomas = activeTomas.size();

                                    for (TomaFisicaEntity toma : activeTomas) {
                                        tomafisicadetallesDao.fetchAndSyncFromApi(toma.getTomaFisicaId(), () -> {
                                            processedCount[0]++;
                                            if (processedCount[0] >= totalTomas) {
                                                runOnUiThread(() -> {
                                                    actualizarBarraSegmentada(100);
                                                    updateDebugSummary();
                                                    
                                                    btn_enviar.setEnabled(true);
                                                    btn_obtener.setEnabled(true);
                                                    mostrarSnack("Sincronización completada con éxito.", Color.rgb(4, 165, 77));
                                                    saveLastSyncDate();
                                                    
                                                    new Handler().postDelayed(() -> resetBarraSegmentada(), 2000);
                                                });
                                            }
                                        });
                                    }
                                } else {
                                    // Fallback if no active tomas found, or maybe just finish
                                    runOnUiThread(() -> {
                                        actualizarBarraSegmentada(100);
                                        updateDebugSummary();
                                        btn_enviar.setEnabled(true);
                                        btn_obtener.setEnabled(true);
                                        mostrarSnack("Sincronización completada (sin tomas activas).", Color.rgb(4, 165, 77));
                                        saveLastSyncDate();
                                        new Handler().postDelayed(() -> resetBarraSegmentada(), 2000);
                                    });
                                }
                            });
                        });
                    });
                });
                });
            });
        });
    }

    private void updateDebugSummary() {
        // Debug summary hidden per user request
        /*
        new Thread(() -> {
            // Obtener conteos de SQLite
            // Nota: Se asume que los DAOs tienen métodos para contar o listar. 
            // Si no tienen count directo, usamos size() de getAll().
            
            int countUsers = userDao.getAllLocalUsers().size();
            int countRoles = rolDao.getAllLocalRoles().size();
            int countUbicaciones = ubicacionDao.getAllUbicaciones().size();
            int countActivos = activoDao.getAllLocalActivos().size();
            int countTomas = tomafisicaDao.getAllTomasFisicas().size();
            int countTomasResumen = tomafisicatomasDao.getAll().size(); // Asumiendo que existe getAll(), si no, verificamos
            int countTomasDetalle = tomafisicadetallesDao.getAll().size(); // Asumiendo que existe getAll()

            runOnUiThread(() -> {
                if (debugContainer != null) {
                    debugContainer.setVisibility(View.VISIBLE);
                    tvDebugUsuarios.setText("Usuarios: " + countUsers);
                    tvDebugRoles.setText("Roles: " + countRoles);
                    tvDebugUbicaciones.setText("Ubicaciones: " + countUbicaciones);
                    tvDebugActivos.setText("Activos: " + countActivos);
                    tvDebugTomas.setText("Tomas Físicas: " + countTomas);
                    tvDebugTomasResumen.setText("Tomas Resumen: " + countTomasResumen);
                    tvDebugTomasDetalle.setText("Tomas Detalle: " + countTomasDetalle);
                }
            });
        }).start();
        */
    }

    public final View.OnClickListener OnClickListenerEnviar = v -> {
        btn_enviar.setEnabled(false);

        if (!_isConnected) {
            btn_enviar.setEnabled(true);
            return;
        }

        // Recopilar estadísticas ANTES de enviar (porque el envío limpia los pendientes)
        java.util.Map<String, Integer> activoStats = activoDao.getPendingSummary();
        final int[] syncStats = new int[3]; // 0: Creados, 1: Bajas, 2: Tomas
        
        if (activoStats != null) {
            if (activoStats.containsKey("creados")) syncStats[0] = activoStats.get("creados");
            if (activoStats.containsKey("bajas")) syncStats[1] = activoStats.get("bajas");
        }
        
        // Reset progress
        resetBarraSegmentada();
        actualizarBarraSegmentada(10); // Start

        // Sincronizar Activos primero
        activoDao.pushLocalChangesToApi(new ApiCallback<JsonElement>() {
            @Override
            public void onComplete(ApiResponse<JsonElement> response) {
                runOnUiThread(() -> actualizarBarraSegmentada(40)); // Update progress
                
                if (!response.success) {
                    runOnUiThread(() -> {
                        mostrarSnack("Error enviando Activos: " + response.errorMessage, Color.RED);
                    });
                } else {
                    Log.d("SYNC", "Activos enviados correctamente");
                }

                // Calcular pendientes de Tomas justo antes de enviar para mayor precisión
                syncStats[2] = tomafisicatomasDao.getPendingResumen().size();
                Log.d("SYNC", "Tomas pendientes detectadas para envio: " + syncStats[2]);

                // Sincronizar Resumenes (Tomas)
                tomafisicatomasDao.pushLocalChangesToApi(() -> {
                    runOnUiThread(() -> actualizarBarraSegmentada(70)); // Update progress
                    Log.d("SYNC", "Resumenes enviados");
                    
                    // Sincronizar Detalles
                    tomafisicadetallesDao.pushLocalChangesToApi(() -> {
                        runOnUiThread(() -> {
                            actualizarBarraSegmentada(100); // Finish
                            
                            Log.d("SYNC", "Detalles enviados");
                            
                            resetBarraSegmentada();
                            btn_enviar.setEnabled(true);
                            btn_obtener.setEnabled(true);
                            
                            String finalMsg = "enviados:\n\n" +
                                    "- Activos Creados/Modificados: " + syncStats[0] + "\n" +
                                    "- Activos Dados de Baja: " + syncStats[1] + "\n" +
                                    "- Subtomas de Inventario: " + syncStats[2] + "\n";

                            // Mostrar resumen en diálogo
                            new AlertDialog.Builder(_context)
                                    .setTitle("Sincronización Completada")
                                    .setMessage(finalMsg)
                                    .setPositiveButton("Aceptar", null)
                                    .setIcon(R.drawable.ic_check_circle)
                                    .show();
                                    
                            mostrarSnack("Sincronización completada.", Color.rgb(4, 165, 77));
                        });
                    });
                });
            }
        });
    };

    private void iniciarProgressThreadConResumen(final int totalProcesos, final String resumen) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int progresoActual = msg.arg1;
                actualizarBarraSegmentada(progresoActual);
            }
        };

        new Thread(() -> {
            try {
                int progreso = 0;
                int incremento = 100 / totalProcesos;

                while (progreso <= 100) {
                    Thread.sleep(300);
                    progreso += incremento;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = progreso;
                    handler.sendMessage(msg);
                }

                runOnUiThread(() -> {
                    resetBarraSegmentada();
                    btn_enviar.setEnabled(true);
                    btn_obtener.setEnabled(true);
                    
                    // Mostrar resumen en diálogo
                    new AlertDialog.Builder(_context)
                            .setTitle("Sincronización Completada")
                            .setMessage(resumen)
                            .setPositiveButton("Aceptar", null)
                            .setIcon(R.drawable.ic_check_circle)
                            .show();
                            
                    mostrarSnack("Sincronización completada con éxito.", Color.rgb(4, 165, 77));
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void iniciarProgressThread(final int totalProcesos) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int progresoActual = msg.arg1;
                actualizarBarraSegmentada(progresoActual);
            }
        };

        new Thread(() -> {
            try {
                int progreso = 0;
                int incremento = 100 / totalProcesos;

                while (progreso <= 100) {
                    Thread.sleep(300);
                    progreso += incremento;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = progreso;
                    handler.sendMessage(msg);
                }

                runOnUiThread(() -> {
                    resetBarraSegmentada();
                    btn_enviar.setEnabled(true);
                    btn_obtener.setEnabled(true);
                    mostrarSnack("Sincronización completada con éxito.", Color.rgb(4, 165, 77));
                });
            } catch (Exception ignored) {}
        }).start();
    }

    // BEGIN OLD

    //consumir web service de Roles Hand
    public void  getRolHH()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerRolHH", entity, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    deserializeRolHH(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Rol Hand Held
    public void deserializeRolHH(String response)
    {
        try
        {
            RolHH = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response).getJSONObject("ObtenerRolHHResult");
            boolean state = jsonObject.getBoolean("State");
            String Mensaje =jsonObject.getString("Description");
            if(state){
                JSONArray jsonArray = jsonObject.getJSONArray("Data");

                //esto solo funciona si el web services devuelve una lista
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject RolHHEncontrado = jsonArray.getJSONObject(i);
                    RolHH.add(
                            new EntidadDatosRol(
                                    RolHHEncontrado.getString("IdRol"),
                                    RolHHEncontrado.getString("Description"),
                                    RolHHEncontrado.getString("Page"),
                                    RolHHEncontrado.getString("Username"),
                                    RolHHEncontrado.getString("UserSysId"),
                                    RolHHEncontrado.getString("EstaBloqueado")));
                }
                InsertOrReplaceRolHH(RolHH);
                exitosRecibidos++;
            }
            else
            {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("Advertencia")
                        .setMessage(Mensaje)
                        .setCancelable(false)
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    //region Inventarios
    //consumir web service de tomas físicas
    public void  getTomaFisica()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerTomaFisica", entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeTomaFisica(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service toma físicas
    public void deserializeTomaFisica(String response)
    {
        try
        {
            TomaFisica = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerTomaFisicaResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject TomaEncontrados = jsonArray.getJSONObject(i);
                TomaFisica.add(
                        new Entidad_TomaFisica(
                                TomaEncontrados.getString("IdToma"),
                                parserJSONDate(TomaEncontrados.getString("TakeDate")),
                                TomaEncontrados.getString("TakeDescription"),
                                TomaEncontrados.getString("TakeName"),
                                TomaEncontrados.getString("TakeStatus")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceTomaFisica(TomaFisica);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " +
                        "Fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //consumir web service de tipo inventario
    public void getTipoInventario()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerTipoToma", entity,new AsyncHttpResponseHandler(){

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeTipoInventario(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al sincronizar" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service tipo inventario
    public void deserializeTipoInventario(String response)
    {
        try
        {
            TipoInventarios = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerTipoTomaResult");

            // Itera a través de la lista de objetos JSON
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject TipoEncontrados = jsonArray.getJSONObject(i);

                // Crea una nueva instancia usando el constructor vacío
                EntidadTiposInventarios nuevoInventario = new EntidadTiposInventarios();

                // Establece los valores usando los métodos setter
                nuevoInventario.setidTipoToma(TipoEncontrados.getString("idTipoToma"));
                nuevoInventario.setnombreTipoToma(TipoEncontrados.getString("nombreTipoToma"));
                nuevoInventario.setdescripcionTipoToma(TipoEncontrados.getString("descripcionTipoToma"));
                nuevoInventario.setfechaInicio(TipoEncontrados.getString("FechaInicio"));
                nuevoInventario.setfechaFinal(TipoEncontrados.getString("FechaFinal"));
                nuevoInventario.setestado(TipoEncontrados.getString("estado"));

                // Añade el objeto a la lista
                TipoInventarios.add(nuevoInventario);
            }

            Exitos++;
            exitosRecibidos++;

            // Inserta o reemplaza los inventarios en la base de datos
            InsertOrReplaceTipoInventario(TipoInventarios);

            // Obtiene el estado de las ubicaciones
            boolean estado = ObtenerEstadoUbicaciones();

            // Muestra un mensaje si el estado es exitoso
            if (estado) {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " + e.toString() + " " + e.getStackTrace());
        }
    }


    //consumir web service de detalle inventario
    public void getTomaDetalle()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerTomaDetalle", entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeTomaDetalle(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al sincronizar" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Detalle Inventario
    public void deserializeTomaDetalle(String response)
    {
        try
        {
            TomaDetalle = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerTomaDetalleResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject TomaDetalleEncontrados = jsonArray.getJSONObject(i);
                TomaDetalle.add(
                        new Entidad_TomaDetalle(TomaDetalleEncontrados.getString("IdTakeDetail"),
                                TomaDetalleEncontrados.getString("FK_TomaFisica"),
                                TomaDetalleEncontrados.getString("EPC"),
                                TomaDetalleEncontrados.getString("DateRead")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceTomaDetalle(TomaDetalle);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " +
                        "Fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    //region Tags
    //Consumir Web Service Tags
    public void getTags()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerTags",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeTags(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Tags
    public void deserializeTags(String response)
    {
        try
        {
            Tags = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerTagsResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject TagsEncontrados = jsonArray.getJSONObject(i);
                Tags.add(
                        new EntidadTags(TagsEncontrados.getString("tagSysId"),
                                TagsEncontrados.getString("tagID"),
                                TagsEncontrados.getString("tagTypeSysId")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceTags(Tags);

            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //Consumir Web Service Tipo de Tags
    public void getTagsType()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerTipoTags",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeTagsType(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Tags
    public void deserializeTagsType(String response)
    {
        try
        {
            tipoTags = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerTipoTagsResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject tipoTagsEncontrados = jsonArray.getJSONObject(i);
                tipoTags.add(
                        new EntidadTiposTags(tipoTagsEncontrados.getString("tagTypeSysId"),
                                tipoTagsEncontrados.getString("code"),
                                tipoTagsEncontrados.getString("name"),
                                tipoTagsEncontrados.getString("description"),
                                tipoTagsEncontrados.getString("category")
                        ));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceTipoTags(tipoTags);

            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " +
                        "Fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    //region Ubicaciones
    //Consumir web service Razon Social
    public void getRazones()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerRazones",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeRazones(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Razon Social
    public void deserializeRazones(String response)
    {
        try
        {
            Razon = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerRazonSocialResult");
            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject RazonEncontrados = jsonArray.getJSONObject(i);
                Razon.add(
                        new EntidadRazonSocial(RazonEncontrados.getString("IdRazon"),
                                RazonEncontrados.getString("NombreRazon")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceRazon(Razon);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //Consumir web service Edificio
    public void getEdificios()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerEdificio",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeEdificios(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Edificio
    public void deserializeEdificios(String response)
    {
        try
        {
            Edificio = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("ObtenerEdificiosResult");
            //esto solo funciona si el web services devuelve una lista

            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject EdificiosEncontrados = jsonArray.getJSONObject(i);
                Edificio.add(
                        new EntidadEdificios(EdificiosEncontrados.getString("IdEdificio"),
                                EdificiosEncontrados.getString("NombreEdificio"),
                                EdificiosEncontrados.getString("IdRazon"),
                                EdificiosEncontrados.getString("Razon")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceEdificios(Edificio);

            boolean estado=ObtenerEstadoUbicaciones();

            if(estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //Consumir web service Pisos
    public void getPisos()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerPiso",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializePisos(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Pisos
    public void deserializePisos(String response)
    {
        try
        {
            Piso = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("ObtenerPisosResult");
            //esto solo funciona si el web services devuelve una lista

            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject PisosEncontrados = jsonArray.getJSONObject(i);
                Piso.add(
                        new EntidadPisos(PisosEncontrados.getString("IdPiso"),
                                PisosEncontrados.getString("NombrePiso"),
                                PisosEncontrados.getString("IdEdificio"),
                                PisosEncontrados.getString("Edificio")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplacePisos(Piso);
            boolean estado=ObtenerEstadoUbicaciones();

            if(estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //Consumir web service Oficinas
    public void getOficinas()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerOficina",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeOficinas(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Oficinas
    public void deserializeOficinas(String response)
    {
        try
        {
            Oficina = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("ObtenerOficinasResult");
            //esto solo funciona si el web services devuelve una lista

            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject OficinasEncontrados = jsonArray.getJSONObject(i);
                Oficina.add(
                        new EntidadOficina2(OficinasEncontrados.getString("IdOficina"),
                                OficinasEncontrados.getString("NombreOficina"),
                                OficinasEncontrados.getString("IdPiso"),
                                OficinasEncontrados.getString("Piso"),
                                OficinasEncontrados.getString("IdTag")));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceOficinas(Oficina);
            boolean estado=ObtenerEstadoUbicaciones();

            if(estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    //region Categorias
    public void getCategoriaActivos(final Runnable onSuccess) {
        ACTIVEID_API activeid_api = new ACTIVEID_API();
        try {
            // Use GET and correct endpoint: /ActivosDetail/Categorias
            activeid_api.get("/ActivosDetail/Categorias", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeCategoriaActivos(new String(responseBody));
                    if (onSuccess != null) onSuccess.run();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e("SYNC", "Error getting categories", error);
                    if (onSuccess != null) onSuccess.run();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (onSuccess != null) onSuccess.run();
        }
    }

    public void deserializeCategoriaActivos(String response) {
        try {
            categoriaActivos = new ArrayList<>();
            // Response is a JSON Array, not Object with wrapper
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                // API keys: cSysId, categoria
                categoriaActivos.add(new EntidadCategoriaActivos(
                        item.getString("cSysId"), // assetCategorySysId
                        item.getString("categoria"), // name
                        "" // description (not provided by API)
                ));
            }
            InsertOrReplaceCategoriaActivo(categoriaActivos);
            Log.d("SYNC", "Categorias synced: " + categoriaActivos.size());
        } catch (Exception e) {
            Log.e("SYNC", "Error parsing categories", e);
        }
    }
    //endregion

    //region Activos
    //Consumir web service Activos
    public void getActivos()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerActivo",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeActivos(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Algo ha salido mal, intenta nuevamente " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    boolean hasdata = true;
    boolean continueCall = true;
    int indexinicio =0;
    int indexfinal =5000;
    int cantidadTotal =0;
    int cantidadBloques = 0;

    public void getActivosBySegmentsV3(final int pindexinicio, final int pindexfinal,final int limit, final int cantidadBloque)
    {
        try {
            ACTIVEID_API activeid_api = new ACTIVEID_API();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("indexinicio", pindexinicio);
            jsonObject.put("indexfinal", pindexfinal);
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/Obt_ActivosView", entity, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    cantidadBloques++;

                    deserializeActivos(new String(responseBody));
                    String mansaje ="Cantidad Bloques: " + String.valueOf(cantidadBloques) + "/40";
                    Mensaje.setText(mansaje);
                    //Toast.makeText(getApplicationContext(),mansaje,Toast.LENGTH_LONG).show();

                    if(cantidadBloques >= 40)
                    {
                        String mensaje ="Sincronización Exitosa";
                        Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_LONG).show();
                        Mensaje.setText(mensaje);
                        btn_obtener.setEnabled(true);
                    }

                    if(pindexfinal + cantidadBloque<limit+1)
                        getActivosBySegmentsV3(pindexfinal + 1,pindexfinal + cantidadBloque, limit, cantidadBloque);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Algo ha salido mal, intenta nuevamente " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hasdata = false;
                }
            });
        } catch (Exception  e)
        {
            //e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Algo ha salido mal, intenta nuevamente",Toast.LENGTH_LONG).show();
        }
    }

    //Obtener respuesta Web service Activos
    public boolean deserializeActivos(String response)
    {
        boolean hasDeserializeActivo= false;

        try
        {
            ArrayList<EntidadActivos>  activos = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("Obt_ActivosViewResult");
            //esto solo funciona si el web services devuelve una lista
            int cantidadRegistros =  jsonArray.length();
            if(cantidadRegistros>0)
                hasDeserializeActivo = true;
            for(int i = 0; i < cantidadRegistros; i++){

                JSONObject ActivosEncontrados = jsonArray.getJSONObject(i);
                activos.add(
                        new EntidadActivos(
                                ActivosEncontrados.getString("assetSysId"),
                                ActivosEncontrados.getString("Alias"),
                                ActivosEncontrados.getString("longDescription"),
                                ActivosEncontrados.getString("Departamento"),
                                ActivosEncontrados.getString("Oficina"),
                                ActivosEncontrados.getString("Piso"),
                                ActivosEncontrados.getString("Edificio"),
                                ActivosEncontrados.getString("Compania"),
                                ActivosEncontrados.getString("tagId"),
                                ActivosEncontrados.getString("assetItemNumber"),
                                ActivosEncontrados.getString("Barcode"),
                                ActivosEncontrados.getString("officeSysId"),
                                ActivosEncontrados.getString("IdEstante"),
                                ActivosEncontrados.getString("assetCategorySysId"),
                                ActivosEncontrados.getString("floorSysId"),
                                ActivosEncontrados.getString("buildingSysId"),
                                ActivosEncontrados.getString("companySysId"),
                                ActivosEncontrados.getString("brand"),
                                ActivosEncontrados.getString("modelNo"),
                                ActivosEncontrados.getString("serialNo"),
                                ActivosEncontrados.getString("parentAssetSysId"),
                                ActivosEncontrados.getString("EmployeeRelated"),
                                ActivosEncontrados.getString("AssetStatusSysId"),
                                ActivosEncontrados.getString("AnnoFabricacion"),
                                ActivosEncontrados.getString("Capacidad"),
                                ActivosEncontrados.getString("EstadoDescripcion"),
                                ActivosEncontrados.getString("EstadoConservacion")));
            }

            Exitos++;
            if(!InsertOrReplaceActivos(activos))
                Toast.makeText(mSincronizarView.getContext(), "Error en la insercion SQLite", Toast.LENGTH_LONG).show();

            boolean estado=ObtenerEstadoUbicaciones();

            if(estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "La sincronización " + "ha sido exitosa", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }

        return hasDeserializeActivo;
    }

    public void ActualizarActivos()
    {
        AssetsDBHelper assetsDBHelper = new AssetsDBHelper(mSincronizarView.getContext());
        final ArrayList<ActivoRecord> listActivos = assetsDBHelper.ObtenerActivoSync();

        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            if(listActivos.size() <= 0)
            {
                exitosEnviados++;
                noHay++;
                Noenviados.add("No hay activos pendientes para sincronizar");
            }
            else
            {
                JSONArray listToUpdate = new JSONArray();
                JSONObject activos = new JSONObject();
                for(int i = 0; i < listActivos.size(); i++){
                    JSONObject asset = new JSONObject();

                    asset.put("Alias", listActivos.get(i).getAlias());
                    asset.put("longDescription", listActivos.get(i).getDescripcion());
                    asset.put("tagId", listActivos.get(i).getTag());
                    asset.put("assetSysId", listActivos.get(i).getIdActivo());
                    asset.put("officeSysId", listActivos.get(i).getIdOficina());
                    asset.put("floorSysId", listActivos.get(i).getIdPiso());
                    asset.put("buildingSysId", listActivos.get(i).getIdEdificio());
                    asset.put("companySysId", listActivos.get(i).getIdCompania());
                    asset.put("brand", listActivos.get(i).getMarca());
                    asset.put("modelNo", listActivos.get(i).getModelo());
                    asset.put("serialNo", listActivos.get(i).getSerial());
                    asset.put("Barcode", listActivos.get(i).getCodeBar());
                    asset.put("updateUser", listActivos.get(i).get_UpdateUser());
                    asset.put("parentAssetSysId", listActivos.get(i).get_ParentAssetSysId());
                    asset.put("employeeRelated", listActivos.get(i).getEmployeeRelatedSysId());
                    asset.put("assetStatusSysId", listActivos.get(i).getAssetStatusSysId());
                    asset.put("AnnoFabricacion", listActivos.get(i).getAnoFabricacion());
                    asset.put("Capacidad", listActivos.get(i).getCapacidad());
                    asset.put("estadoDescripcion", listActivos.get(i).get_EstadoDescripcion());
                    asset.put("estadoConservacion", listActivos.get(i).get_EstadoConservacion());
                    //poner todos
                    //al final de todos
                    listToUpdate.put(asset);
                }

                activos.put("assets", listToUpdate);
                StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

                activeid_api.post(mSincronizarView.getContext().getApplicationContext(), "/ActualizarActivo",entity, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        AssetsDBHelper assetsDBHelper = new AssetsDBHelper(mSincronizarView.getContext());
                        assetsDBHelper.ActualizarSync(listActivos);
                        exitosEnviados++;
                        enviados.add("Activos Actualizados");
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        enviadosSinExito++;
                        Noenviados.add("Activos Actualizados");
                    }
                });

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    //region Sincronizacion Tomas Físicas


    public void ActualizarToma() {

        listTomaUpdate.clear();
        listTomaDetalle.clear();

        InventoryDBHelper db = new InventoryDBHelper(mSincronizarView.getContext());
        Cursor cToma  = db.ObtenerTomasInventario();
        Cursor cDet   = db.ObtenerTomaFisicaDetalle();

        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try {
            // --------- Índices para Tomas ----------
            final int i_id         = cToma.getColumnIndexOrThrow("_id");
            final int i_takeDate   = cToma.getColumnIndexOrThrow("TakeDate");
            final int i_takeDesc   = cToma.getColumnIndexOrThrow("TakeDescription");
            final int i_takeName   = cToma.getColumnIndexOrThrow("TakeName");
            final int i_takeStatus = cToma.getColumnIndexOrThrow("TakeStatus");

            for (cToma.moveToFirst(); !cToma.isAfterLast(); cToma.moveToNext()) {
                Entidad_TomaFisica e = new Entidad_TomaFisica(
                        cToma.isNull(i_id)         ? null : cToma.getString(i_id),
                        cToma.isNull(i_takeDate)   ? null : cToma.getString(i_takeDate),
                        cToma.isNull(i_takeDesc)   ? null : cToma.getString(i_takeDesc),
                        cToma.isNull(i_takeName)   ? null : cToma.getString(i_takeName),
                        cToma.isNull(i_takeStatus) ? null : cToma.getString(i_takeStatus)
                );
                listTomaUpdate.add(e);
            }

            // --------- Índices para Detalle ----------
            final int i_detId  = cDet.getColumnIndexOrThrow("idTakeDetail");
            final int i_fkToma = cDet.getColumnIndexOrThrow("FK_TomaFisica");
            final int i_epc    = cDet.getColumnIndexOrThrow("EPC");
            final int i_date   = cDet.getColumnIndexOrThrow("DateRead");

            for (cDet.moveToFirst(); !cDet.isAfterLast(); cDet.moveToNext()) {
                Entidad_TomaDetalle d = new Entidad_TomaDetalle(
                        cDet.isNull(i_detId)  ? null : cDet.getString(i_detId),
                        cDet.isNull(i_fkToma) ? null : cDet.getString(i_fkToma),
                        cDet.isNull(i_epc)    ? null : cDet.getString(i_epc),
                        cDet.isNull(i_date)   ? null : cDet.getString(i_date)
                );
                listTomaDetalle.add(d);
            }

            if (listTomaDetalle.isEmpty()) {
                exitosEnviados++;
                noHay++;
                Noenviados.add("No hay detalles de tomas para sincronizar");
                return;
            }

            // --------- Construcción del JSON ----------
            JSONArray listToUpdate = new JSONArray();
            JSONArray listToUpdateTomas = new JSONArray();
            JSONObject activos = new JSONObject();

            for (int i = 0; i < listTomaUpdate.size(); i++) {
                JSONObject tomaFisica = new JSONObject();
                tomaFisica.put("IdToma",        listTomaUpdate.get(i).getIdToma());
                tomaFisica.put("TakeName",      listTomaUpdate.get(i).getTakeName());
                tomaFisica.put("TakeDescription", listTomaUpdate.get(i).getTakeDescription());
                tomaFisica.put("TakeDate",      listTomaUpdate.get(i).getTakeDate());
                tomaFisica.put("TakeStatus",    listTomaUpdate.get(i).getTakeStatus());
                listToUpdate.put(tomaFisica);
            }

            for (int i = 0; i < listTomaDetalle.size(); i++) {
                JSONObject tomaFisicaDetalle = new JSONObject();
                tomaFisicaDetalle.put("IdTakeDetail",  listTomaDetalle.get(i).getIdTakeDetail());
                tomaFisicaDetalle.put("FK_TomaFisica", listTomaDetalle.get(i).getFk_TomaFisica());
                tomaFisicaDetalle.put("EPC",           listTomaDetalle.get(i).getepc());
                tomaFisicaDetalle.put("DateRead",      listTomaDetalle.get(i).getDateRead());
                listToUpdateTomas.put(tomaFisicaDetalle);
            }

            activos.put("TomaFisica",        listToUpdate);
            activos.put("TomaFisicaDetalle", listToUpdateTomas);

            StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

            activeid_api.post(
                    mSincronizarView.getContext().getApplicationContext(),
                    "/TomaFisica",
                    entity,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            InventoryDBHelper db2 = new InventoryDBHelper(mSincronizarView.getContext());
                            db2.ActualizarTomaDetalleSync(listTomaDetalle);
                            exitosEnviados++;
                            enviados.add("Detalle Toma Física");
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Noenviados.add("Detalle Toma Física");
                            enviadosSinExito++;
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cToma != null) cToma.close();
            if (cDet  != null) cDet.close();
        }
    }

    public void SincronizarInventario() {
        // Limpia listas
        listTomasInventario.clear();
        listInventario.clear();
        listDetalleInventario.clear();

        InventoryDBHelper db = new InventoryDBHelper(mSincronizarView.getContext());

        Cursor cTomasInv = db.ObtenerTomasDelInventario();
        Cursor cInv      = db.ObtenerInventario();
        Cursor cDetInv   = db.ObtenerDetalleInventario();

        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try {
            // ------ Índices Tomas del inventario ------
            final int i_takeId  = cTomasInv.getColumnIndexOrThrow("IdTomasDelInventario");
            final int i_fecha   = cTomasInv.getColumnIndexOrThrow("Fecha");
            final int i_oficina = cTomasInv.getColumnIndexOrThrow("Oficina");
            final int i_usuario = cTomasInv.getColumnIndexOrThrow("Usuario");
            final int i_tipoInv = cTomasInv.getColumnIndexOrThrow("ID_TipoInventario");

            for (cTomasInv.moveToFirst(); !cTomasInv.isAfterLast(); cTomasInv.moveToNext()) {
                EntidadTomasInventario e = new EntidadTomasInventario(
                        cTomasInv.isNull(i_takeId)  ? null : cTomasInv.getString(i_takeId),
                        cTomasInv.isNull(i_fecha)   ? null : cTomasInv.getString(i_fecha),
                        cTomasInv.isNull(i_oficina) ? null : cTomasInv.getString(i_oficina),
                        cTomasInv.isNull(i_usuario) ? null : cTomasInv.getString(i_usuario),
                        cTomasInv.isNull(i_tipoInv) ? null : cTomasInv.getString(i_tipoInv)
                );
                listTomasInventario.add(e);
            }

            // ------ Índices Inventario (cabecera) ------
            final int i_invId       = cInv.getColumnIndexOrThrow("idInventory");
            final int i_invTakeId   = cInv.getColumnIndexOrThrow("IdTomaInventario");
            final int i_numero      = cInv.getColumnIndexOrThrow("Numero");
            final int i_leidos      = cInv.getColumnIndexOrThrow("Leidos");
            final int i_total       = cInv.getColumnIndexOrThrow("Total");
            final int i_ok          = cInv.getColumnIndexOrThrow("Encontrados");
            final int i_missing     = cInv.getColumnIndexOrThrow("Faltantes");
            final int i_extra       = cInv.getColumnIndexOrThrow("Sobrantes");
            final int i_fechaInv    = cInv.getColumnIndexOrThrow("Fecha");

            for (cInv.moveToFirst(); !cInv.isAfterLast(); cInv.moveToNext()) {
                EntidadInventario inv = new EntidadInventario(
                        cInv.isNull(i_invId)     ? null : cInv.getString(i_invId),
                        cInv.isNull(i_invTakeId) ? null : cInv.getString(i_invTakeId),
                        cInv.isNull(i_numero)    ? null : cInv.getString(i_numero),
                        cInv.isNull(i_leidos)    ? null : cInv.getString(i_leidos),
                        cInv.isNull(i_total)     ? null : cInv.getString(i_total),
                        cInv.isNull(i_ok)        ? null : cInv.getString(i_ok),
                        cInv.isNull(i_missing)   ? null : cInv.getString(i_missing),
                        cInv.isNull(i_extra)     ? null : cInv.getString(i_extra),
                        cInv.isNull(i_fechaInv)  ? null : cInv.getString(i_fechaInv)
                );
                listInventario.add(inv);
            }

            // ------ Índices Detalle Inventario ------
            final int i_detId      = cDetInv.getColumnIndexOrThrow("IdDetalleInventario");
            final int i_fkInv      = cDetInv.getColumnIndexOrThrow("FK_idInventory");
            final int i_assetNum   = cDetInv.getColumnIndexOrThrow("NumeroActivo");
            final int i_desc       = cDetInv.getColumnIndexOrThrow("Descripcion");
            final int i_epc        = cDetInv.getColumnIndexOrThrow("EPC");
            final int i_state      = cDetInv.getColumnIndexOrThrow("EstadoActivo");
            final int i_excl       = cDetInv.getColumnIndexOrThrow("Excluido");

            for (cDetInv.moveToFirst(); !cDetInv.isAfterLast(); cDetInv.moveToNext()) {
                EntidadDetalleInventario det = new EntidadDetalleInventario(
                        cDetInv.isNull(i_detId)    ? null : cDetInv.getString(i_detId),
                        cDetInv.isNull(i_fkInv)    ? null : cDetInv.getString(i_fkInv),
                        cDetInv.isNull(i_assetNum) ? null : cDetInv.getString(i_assetNum),
                        cDetInv.isNull(i_desc)     ? null : cDetInv.getString(i_desc),
                        cDetInv.isNull(i_epc)      ? null : cDetInv.getString(i_epc),
                        cDetInv.isNull(i_state)    ? null : cDetInv.getString(i_state),
                        cDetInv.isNull(i_excl)     ? null : cDetInv.getString(i_excl)
                );
                listDetalleInventario.add(det);
            }

            if (listDetalleInventario.isEmpty() || listInventario.isEmpty() || listTomasInventario.isEmpty()) {
                exitosEnviados++;
                noHay++;
                Noenviados.add("No hay detalles de inventarios para sincronizar");
                return;
            }

            // --------- Construcción del JSON ----------
            JSONArray listToUpdate = new JSONArray();          // Tomas del inventario
            JSONArray listToUpdateTomas = new JSONArray();     // Inventarios (cabecera)
            JSONArray listToInventoryDetail = new JSONArray(); // Detalle inventario
            JSONObject activos = new JSONObject();

            for (int i = 0; i < listTomasInventario.size(); i++) {
                JSONObject toma = new JSONObject();
                toma.put("idInventoryTake", listTomasInventario.get(i).getIdTakeInventory());
                toma.put("inventoryDate",  listTomasInventario.get(i).getDateTakeInventory());
                toma.put("userSysId",      listTomasInventario.get(i).getUsuario());
                toma.put("officeSysId",    listTomasInventario.get(i).getOficina());
                toma.put("idInventoryType",listTomasInventario.get(i).getTiposDeInventario());
                listToUpdate.put(toma);
            }

            for (int i = 0; i < listInventario.size(); i++) {
                JSONObject inv = new JSONObject();
                inv.put("idInventory",    listInventario.get(i).getId());
                inv.put("numItems",       listInventario.get(i).getNumero());
                inv.put("readItems",      listInventario.get(i).getLeidos());
                inv.put("readTotal",      listInventario.get(i).getTotal());
                inv.put("readOk",         listInventario.get(i).getEncontrados());
                inv.put("readMissing",    listInventario.get(i).getFaltantes());
                inv.put("readExtra",      listInventario.get(i).getSobrantes());
                inv.put("inventoryDate",  listInventario.get(i).getFecha());
                inv.put("idInventoryTake",listInventario.get(i).getIdTomasdeInventario());
                inv.put("method", "RFID");
                listToUpdateTomas.put(inv);
            }

            for (int i = 0; i < listDetalleInventario.size(); i++) {
                // VALIDACION: No subir activos que no existen en la base de datos ("No Inventariado")
                String estado = listDetalleInventario.get(i).getEstado();
                if (estado != null && "No Inventariado".equalsIgnoreCase(estado)) {
                    continue;
                }

                JSONObject det = new JSONObject();
                det.put("idInventoryDetails", listDetalleInventario.get(i).getId());
                det.put("idInventory",        listDetalleInventario.get(i).getIdInventario());
                det.put("assetNumber",        listDetalleInventario.get(i).getNumeroActivo());
                det.put("assetDescription",   listDetalleInventario.get(i).getDescripcion());
                det.put("EPC",                listDetalleInventario.get(i).getEPC());
                det.put("assetState",         listDetalleInventario.get(i).getEstado());
                det.put("assetExcluded",      listDetalleInventario.get(i).getExcluido());
                listToInventoryDetail.put(det);
            }

            activos.put("TomasDelInventario",  listToUpdate);
            activos.put("inventarios",         listToUpdateTomas);
            activos.put("detalleInventarios",  listToInventoryDetail);

            StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

            activeid_api.post(
                    mSincronizarView.getContext().getApplicationContext(),
                    "/TomaFisicaNueva",
                    entity,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            InventoryDBHelper db2 = new InventoryDBHelper(mSincronizarView.getContext());
                            db2.ActualizarTomaDelInventarioSync(listTomasInventario);
                            db2.ActualizarInventarioSync(listInventario);
                            db2.ActualizarDetalleInventarioSync(listDetalleInventario);
                            exitosEnviados++;
                            enviados.add("Detalle de Inventario");
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            enviadosSinExito++;
                            Noenviados.add("Detalle de Inventario");
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cTomasInv != null) cTomasInv.close();
            if (cInv      != null) cInv.close();
            if (cDetInv   != null) cDetInv.close();
        }
    }
    //endregion

    //region Envío de Fotos
    private String convertImage(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm = Bitmap
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);//imagen codificada

        return encodedImage;
    }

    private void EnvioFotoVolley()
    {
        final String BASE_URL = "http://138.59.16.3/WCFTESTIMAS/Service1.svc/";
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "/EnvioFotoActivo", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                FotoDBHelper fotoDBHelper = new FotoDBHelper(mSincronizarView.getContext());
                final ArrayList<EFotoActivo> listFotos = fotoDBHelper.EnviarFotoActivo();
                Map<String, String> fotoActivo = new HashMap<String, String>();
                try{

                    if(listFotos.size() <= 0)
                    {
                        Toast.makeText(getApplicationContext(), "No hay fotos para sincronizar", Toast.LENGTH_LONG).show();
                        Noenviados.add("No hay fotos nuevas para sincronizar.");
                    }
                    else
                    {
                        int cantidadFotos = listFotos.size();
                        for(int i = 0; i < cantidadFotos; i++){
                            File rootFile = new File(listFotos.get(i).getRutaFoto()+"/"+listFotos.get(i).getNombreArchivo());
                            if(rootFile.exists()){

                                FileInputStream fileInputStream = new FileInputStream(rootFile);

                                byte[] array = new byte[200000];
                                fileInputStream.read(array);
                                String base64Encoding = new String(array);
                                fileInputStream.close();

                                String imagen2 = base64Encoding.trim();
                                String imagen = imagen2.replace(System.getProperty("line.separator"), "");

                                JSONObject fotosActivos = new JSONObject();
                                fotosActivos.put("assetSysId", listFotos.get(i).getAssetSysId());
                                fotosActivos.put("consecutivo", listFotos.get(i).getObservacionFoto());
                                fotosActivos.put("imageSysId", listFotos.get(i).getIdFoto());
                                fotosActivos.put("name", listFotos.get(i).getNombreArchivo());
                                fotosActivos.put("base64Encoding",imagen);


                                fotoActivo.put("fotoActivo", fotosActivos.toString());
                                Toast.makeText(getApplicationContext(), fotoActivo.get("fotoActivo"), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                catch(Exception ex)
                {

                }

                return fotoActivo;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(mSincronizarView.getContext());
        queue.add(request);
    }

    int cantidadFotos = 0;
    int position = 0;

    private void EnviarFotos()
    {
        FotoDBHelper fotoDBHelper = new FotoDBHelper(mSincronizarView.getContext());
        final ArrayList<EFotoActivo> listFotos = fotoDBHelper.EnviarFotoActivo();
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            if(listFotos.size() <= 0)
            {
                Toast.makeText(getApplicationContext(),
                        "No hay fotos para sincronizar",
                        Toast.LENGTH_LONG).show();
                Noenviados.add("No hay fotos nuevas para sincronizar.");
            }
            else
            {
                JSONArray listToUpdate = new JSONArray();
                JSONObject fotos = new JSONObject();
                cantidadFotos = listFotos.size();

                while(position <= cantidadFotos)
                {
                    File rootFile = new File(listFotos.get(position).getRutaFoto()+"/"+listFotos.get(position).getNombreArchivo());
                    if(rootFile.exists()){

                        FileInputStream fileInputStream = new FileInputStream(rootFile);

                        byte[] array = new byte[200000];
                        fileInputStream.read(array);
                        String base64Encoding = new String(array);
                        fileInputStream.close();

                        String imagen2 = base64Encoding.trim();
                        String imagen = imagen2.replace(System.getProperty("line.separator"), "");

                        JSONObject fotosActivos = new JSONObject();
                        fotosActivos.put("assetSysId", listFotos.get(position).getAssetSysId());
                        fotosActivos.put("consecutivo", listFotos.get(position).getObservacionFoto());
                        fotosActivos.put("imageSysId", listFotos.get(position).getIdFoto());
                        fotosActivos.put("name", listFotos.get(position).getNombreArchivo());
                        fotosActivos.put("base64Encoding",imagen);

                        listToUpdate.put(fotosActivos);
                    }

                    fotos.put("fotoActivo",listToUpdate);

                    StringEntity entity = new StringEntity(fotos.toString(), "UTF-8");

                    activeid_api.post(mSincronizarView.getContext().getApplicationContext(), "/EnvioFotoActivo", entity, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Boolean actualizado;
                            actualizado = DeserializeRespuestaFoto(new String(responseBody));
                            if(actualizado){
                                enviados.add("Fotos Enviadas");
                            }else{

                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                              Throwable error) {
                            Alerta("ATENCIÓN","Las fotos no se han podido enviar, intente " +
                                    "sincronizar activos nuevamente.");
                            enviadosSinExito++;
                            Noenviados.add("Fotos No Enviadas");
                        }
                    });
                    int listaFotos = listToUpdate.length();
                    boolean eliminado = fotoDBHelper.EliminarFoto(listFotos.get(position).getIdFoto());
                    Toast.makeText(getApplicationContext(),"Tamaño de la lista: "+ String.valueOf(listaFotos) +"\n"+
                            "Foto: " +String.valueOf(position) + ", de: "+ String.valueOf(cantidadFotos), Toast.LENGTH_LONG).show();
                    listToUpdate.remove(0);
                    position++;
                    if(position == cantidadFotos){
                        exitosEnviados++;
                    }
                }
            }
        }
        catch(Exception ex)
        {

        }
    }

    private static Boolean DeserializeRespuestaFoto(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response).getJSONObject("EnvioFotoActivoResult");
            int state = jsonObject.getInt("Estado");
            if(state == 1){
                String dateState = jsonObject.getString("Mensaje");
                if(dateState.equals("Exitoso")){
                    String respuesta = jsonObject.getString("Respuesta");
                    if(respuesta.equals("Exitoso")){
                        return true;
                    }
                }
            }else{
                return false;
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
            return false;
        }

        return true;
    }
    //endregion

    //region Activos Nuevos
    public void InsertarActivos()
    {
        newAssets NewAssets = new newAssets(mSincronizarView.getContext());
        final ArrayList<NuevoActivo> listActivos = NewAssets.IngresarActivoSync();

        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            if(listActivos.size() <= 0)
            {
                exitosEnviados++;
                noHay++;
                Noenviados.add("No activos nuevos para sincronizar.");
            }
            else
            {
                JSONArray listToUpdate = new JSONArray();
                JSONObject activos = new JSONObject();
                for (int i = 0; i < listActivos.size(); i++) {

                    JSONObject asset = new JSONObject();
                    // Mapping to API ActivosController.cs
                    asset.put("ID_ACTIVO", listActivos.get(i).getAssetId());
                    asset.put("NUMERO_ACTIVO", listActivos.get(i).getNumero());
                    asset.put("NUMERO_ETIQUETA", listActivos.get(i).getCodeBar());
                    asset.put("DESCRIPCION_LARGA", listActivos.get(i).getDescripcion());
                    asset.put("DESCRIPCION_CORTA", listActivos.get(i).getDescripcion());
                    asset.put("EMPRESA", listActivos.get(i).getIdCompania());
                    asset.put("UBICACION_A", listActivos.get(i).getIdEdificio());
                    asset.put("UBICACION_B", listActivos.get(i).getIdPiso());
                    asset.put("UBICACION_D", listActivos.get(i).getIdOficina());
                    asset.put("EMPLEADO", listActivos.get(i).getEmployeeRelated());
                    asset.put("MARCA", listActivos.get(i).getMarca());
                    asset.put("MODELO", listActivos.get(i).getModelo());
                    asset.put("NUMERO_SERIE", listActivos.get(i).getSerial());
                    asset.put("TAG_EPC", listActivos.get(i).getTag());
                    asset.put("EPC", listActivos.get(i).getTag());
                    asset.put("ESTADO", listActivos.get(i).getAssetStatusSysId());
                    asset.put("ANOS_VIDA_UTIL", listActivos.get(i).getAnoFabricacion());
                    asset.put("TAMANIO_MEDIDA", listActivos.get(i).getCapacidad());
                    asset.put("DESCRIPCION_ESTADO_ULTIMO_INVENTARIO", listActivos.get(i).getEstadoDescripcion());
                    asset.put("OBSERVACIONES", listActivos.get(i).getEstadoConservacion());
                    asset.put("ESTADO_ACTIVO", true);

                    //poner todos
                    //al final de todos
                    listToUpdate.put(asset);
                }

                activos.put("newAsset", listToUpdate);

                StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

                activeid_api.post(mSincronizarView.getContext().getApplicationContext(), "/NuevoActivo", entity, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        newAssets assetsDBHelper = new newAssets(mSincronizarView.getContext());
                        assetsDBHelper.IngresarSync(listActivos);  //elimina los datos de la tabla "NewAssets"
                        exitosEnviados++;
                        enviados.add("Activos Nuevos");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,Throwable error)
                    {
                        Toast.makeText(getApplicationContext(), "No se envió el activo nuevo", Toast.LENGTH_LONG).show();
                        enviadosSinExito++;
                        Noenviados.add("Activos Nuevos");
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    //region Categoria de Activos
    public void getcategoriaActivos()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerCategoriaActivo",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializecategoriaActivos(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Tags
    public void deserializecategoriaActivos(String response)
    {
        try
        {
            categoriaActivos = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerCategoriaActivoResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject categoriaActivosEncontrados = jsonArray.getJSONObject(i);
                categoriaActivos.add(
                        new EntidadCategoriaActivos(categoriaActivosEncontrados.getString("assetCategorySysId"),
                                categoriaActivosEncontrados.getString("name"),
                                categoriaActivosEncontrados.getString("description")
                        ));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceCategoriaActivo(categoriaActivos);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "Resultado de localizaciones " + "Fue exitoso", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    //region Sincronizar Tag Sectores
    public void SincronizarTags()
    {
        OfficesDBHelper NuevoTag = new OfficesDBHelper(mSincronizarView.getContext());
        final ArrayList<sincronizarTag> listActivos = NuevoTag.IngresarSectorSync();

        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            if(listActivos.size() <= 0)
            {
                //Toast.makeText(getApplicationContext(), "No hay tags para sincronizar", Toast.LENGTH_LONG).show();
                exitosEnviados++;
                noHay++;
                Noenviados.add("No hay sectores para sincronizar");
            }
            else
            {
                JSONArray listToUpdate = new JSONArray();
                JSONObject activos = new JSONObject();
                for(int i = 0; i < listActivos.size(); i++){
                    JSONObject asset = new JSONObject();

                    asset.put("officeSysId", listActivos.get(i).getOfficeSysId());
                    asset.put("oficinaNombre", listActivos.get(i).getOficinaNombre());
                    asset.put("tagId", listActivos.get(i).getTagId());

                    //poner todos
                    //al final de todos
                    listToUpdate.put(asset);
                }

                activos.put("tagSector", listToUpdate);

                StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

                activeid_api.post(mSincronizarView.getContext().getApplicationContext(), "/ActualizarSector",entity, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        OfficesDBHelper officesDBHelper = new OfficesDBHelper(mSincronizarView.getContext());
                        officesDBHelper.tagSync(listActivos);
                        exitosEnviados++;
                        enviados.add("Sectores");
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        enviadosSinExito++;
                        Noenviados.add("Sectores");

                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    //region Sincronizar Tags Clasificados
    private void SincronizarTagsClasificados()
    {
        TagsDBHelper tagClasificado = new TagsDBHelper(mSincronizarView.getContext());
        final ArrayList<EntidadTags> listTags = tagClasificado.ObtenerTagsClasificadosSync();
        ACTIVEID_API activeid_api = new ACTIVEID_API();
        try
        {
            if(listTags.size() <= 0){
                exitosEnviados++;
                noHay++;
                Noenviados.add("No hay tags para sincronizar");
            }
            else{
                JSONArray listToUpdate = new JSONArray();
                JSONObject activos = new JSONObject();
                for(int i = 0; i < listTags.size(); i++){
                    JSONObject asset = new JSONObject();

                    asset.put("tagSysId", listTags.get(i).getTagSysId());
                    asset.put("tagID", listTags.get(i).getTagID());
                    asset.put("tagTypeSysId", listTags.get(i).getTagTypeSysId());

                    //poner todos
                    //al final de todos
                    listToUpdate.put(asset);
                }

                activos.put("tagsClasificados", listToUpdate);

                StringEntity entity = new StringEntity(activos.toString(), "UTF-8");

                activeid_api.post(mSincronizarView.getContext().getApplicationContext(), "/tagsClasificados",entity, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        TagsDBHelper tagsDBHelper = new TagsDBHelper(mSincronizarView.getContext());
                        tagsDBHelper.tagSync(listTags);
                        exitosEnviados++;
                        enviados.add("Tags");
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {

                        enviadosSinExito++;
                        Noenviados.add("Tags");
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    //region Obtener respuesta Web service AssetStatus
    public void  getAssetStatus()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerAssetStatus", entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deserializeAssetStatus(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    public void deserializeAssetStatus(String response)
    {
        try
        {
            ArrayList<EntidadAssetStatus> entidadAssetStatus = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerAssetStatusResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject assetStatusEncontrados = jsonArray.getJSONObject(i);
                entidadAssetStatus.add(
                        new EntidadAssetStatus(assetStatusEncontrados.getString("AssetStatusSysId"),
                                assetStatusEncontrados.getString("Name"),
                                assetStatusEncontrados.getString("Description")
                        ));
            }

            Exitos++;
            InsertOrReplaceAssetStatus(entidadAssetStatus);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "La sincronización " + "ha sido exitosa", Toast.LENGTH_LONG).show();
            }

        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }
    //endregion

    public void getEmployees(final int pindexinicio, final int pindexfinal,final int limit, final int cantidadBloque)
    {
        try
        {
            ACTIVEID_API activeid_api = new ACTIVEID_API();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("indexinicio", pindexinicio);
            jsonObject.put("indexfinal", pindexfinal);
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerEmpleadosByIndex", entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    deserializeEmployees(new String(responseBody));
                    if(pindexfinal + cantidadBloque<limit+1)
                        getEmployees(pindexfinal + 1,pindexfinal + cantidadBloque, limit, cantidadBloque);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Algo ha salido mal, intenta nuevamente " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hasdata = false;
                }
            });
        }
        catch (Exception  e)
        {
            //e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Algo ha salido mal, intenta nuevamente",Toast.LENGTH_LONG).show();
        }
    }

    public void deserializeEmployees(String response)
    {
        try
        {
            ArrayList<EntidadEmployees> entidadEmployeess = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("ObtenerEmpleadosByIndexResult");

            //esto solo funciona si el web services devuelve una lista
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject employeesEncontrados = jsonArray.getJSONObject(i);
                entidadEmployeess.add(
                        new EntidadEmployees(employeesEncontrados.getString("EmployeeSysId"),
                                employeesEncontrados.getString("Name"),
                                employeesEncontrados.getString("LastName"),
                                employeesEncontrados.getString("Id"),
                                employeesEncontrados.getString("CompanyIdExtern")
                        ));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceEmployees(entidadEmployeess);
            boolean estado = ObtenerEstadoUbicaciones();

            if (estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "La sincronización " + "ha sido exitosa", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    // END OLD

    public boolean Alerta(String titulo, String Mensaje)
    {
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
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();
        return  true;
    }
    public boolean ObtenerEstadoUbicaciones()
    {
        if(Exitos==12)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    //endregion

    //region Insercion Base de datos
    public boolean InsertOrReplaceTipoTags(ArrayList<EntidadTiposTags>  tipoTags)
    {
        return SincronizarDBHelper.InsertOrReplaceTipoTags(tipoTags);
    }
    public boolean InsertOrReplaceTags(ArrayList<EntidadTags>  tags){
        return SincronizarDBHelper.InsertOrReplaceTags(tags);
    }

    public boolean InsertOrReplaceTomaFisica(ArrayList<Entidad_TomaFisica> tomafisica){

        return SincronizarDBHelper.InsertOrReplaceTomaFisica(tomafisica);
    }

    public boolean InsertOrReplaceRolHH(ArrayList<EntidadDatosRol> RolHH){

        return SincronizarDBHelper.InsertOrReplaceRolHH(RolHH);
    }

    public boolean Verificar(){
        return SincronizarDBHelper.doesRecordExist();
    }

    public boolean InsertOrReplaceTipoInventario(ArrayList<EntidadTiposInventarios> tipoInventarios){
        return SincronizarDBHelper.InsertOrReplaceTipoInventario(tipoInventarios);
    }

    public boolean InsertOrReplaceTomaDetalle(ArrayList<Entidad_TomaDetalle> tomaDetalle){
        return SincronizarDBHelper.InsertOrReplaceTomaDetalle(tomaDetalle);
    }

    public boolean InsertOrReplaceRazon(ArrayList<EntidadRazonSocial> razon){

        return SincronizarDBHelper.InsertOrReplaceRazones(razon);
    }

    public boolean InsertOrReplaceEdificios(ArrayList<EntidadEdificios> edificio) {

        return SincronizarDBHelper.InsertOrReplaceEdificios(edificio);
    }

    public boolean InsertOrReplacePisos(ArrayList<EntidadPisos> pisos){

        return  SincronizarDBHelper.InsertOrReplacePisos(pisos);
    }

    public boolean InsertOrReplaceOficinas (ArrayList<EntidadOficina2> oficina){

        return  SincronizarDBHelper.InsertOrReplaceOficinas(oficina);
    }

    /*public boolean InsertOrReplaceUsuarios (ArrayList<EntidadUsuarios> usuario){

        return  SincronizarDBHelper.InsertOrReplaceUsuarios(usuario);
    }*/

    public boolean InsertOrReplaceActivos (ArrayList<EntidadActivos> activo){

        return  SincronizarDBHelper.InsertOrReplaceActivos(activo);
    }

    public boolean InsertOrReplaceCategoriaActivo (ArrayList<EntidadCategoriaActivos> categoriaActivos){

        return  SincronizarDBHelper.InsertOrReplaceCategoriaActivo(categoriaActivos);
    }

    public boolean InsertOrReplaceAssetStatus (ArrayList<EntidadAssetStatus> assetStatusList){

        return  SincronizarDBHelper.InsertOrReplaceAssetStatus(assetStatusList);
    }

    public boolean InsertOrReplaceEmployees (ArrayList<EntidadEmployees> employeesList){

        return  SincronizarDBHelper.InsertOrReplaceEmployees(employeesList);
    }
}

