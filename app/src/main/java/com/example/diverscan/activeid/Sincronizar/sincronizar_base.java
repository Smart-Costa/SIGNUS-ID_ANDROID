package com.example.diverscan.activeid.Sincronizar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.diverscan.activeid.Activo.ActivoRecord;
import com.example.diverscan.activeid.Activo.EntidadActivos;
import com.example.diverscan.activeid.Activo.EntidadCategoriaActivos;
import com.example.diverscan.activeid.Activo.NuevoActivo;
import com.example.diverscan.activeid.AssetStatus.EntidadAssetStatus;
import com.example.diverscan.activeid.Assign_tag_Offices.sincronizarTag;
import com.example.diverscan.activeid.Conexion.ACTIVEID_API;
import com.example.diverscan.activeid.Conexion.NetworkConnection;
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
import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.diverscan.activeid.Utilities.Fechas.parserJSONDate;

//Librerias de consumir el web services
public class sincronizar_base extends AppCompatActivity
{

    public final ArrayList<Entidad_TomaFisica>        listTomaUpdate        = new ArrayList<>();
    public final ArrayList<Entidad_TomaDetalle>       listTomaDetalle       = new ArrayList<>();

    public final ArrayList<EntidadTomasInventario>    listTomasInventario   = new ArrayList<>();
    public final ArrayList<EntidadInventario>         listInventario        = new ArrayList<>();
    public final ArrayList<EntidadDetalleInventario>  listDetalleInventario = new ArrayList<>();

    private View mSincronizarView;
    Button btn_enviar;
    Button btn_obtener;
    RadioButton radio_enviar;
    RadioButton radio_sincro;
    RadioButton radio_Tags;
    TextView Mensaje;
    private boolean obtenerActivo = true;
    private boolean enviarActivo = true;
    ConstraintLayout rlsnackbar;
    Snackbar _snackbar;
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
    private Map<Integer, ActivoRecord> _mapActivos = new HashMap<Integer, ActivoRecord>();
    private ProgressDialog dialog;
    private ProgressDialog dialogEnvio;
    private ProgressDialog dialog2;
    int Exitos=0;
    int exitosEnviados = 0;
    int exitosRecibidos = 0;
    int enviadosSinExito =0;
    int noHay = 0;
    //private String IP = "196.40.23.183";
    private String IP = "www.google.com";
    SincronizarDBHelper SincronizarDBHelper;
    private Context _context;
    private Spinner PreOpcionesSincr;
    private String[] strOpcionesSincr;
    private List<String> listaOpcionesSincr;
    private ArrayAdapter<String> adapterOpcionesSincr;
    private String preOpcionesSincr;
    private Activity _activity;
    private ArrayList<String> enviados = new ArrayList<String>();
    private ArrayList<String> Noenviados = new ArrayList<String>();
    private Boolean _isConnected = true;
    ProgressDialog progressDialog;
    android.app.AlertDialog alertDialog;
    private BroadcastReceiver receiver;

    // === Helpers de formato ===
    private static String toIso8601(String raw) {
        if (raw == null || raw.trim().isEmpty()) return raw;
        // Ajusta el formato de ENTRADA si tu SQLite guarda otro (p.ej. "yyyy-MM-dd HH:mm:ss")
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date d = in.parse(raw);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            out.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return out.format(d);
        } catch (Exception e) {
            // si ya venía en ISO u otro formato, devuélvelo tal cual
            return raw;
        }
    }

    private static int toIntOrZero(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); } catch (Exception ignore) { return 0; }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar_base_webservice);
        _activity = this;
        _context = this;
        controles();
        eventos();
        CargarOpciones();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        isAvailable.execute();
    }

    private void CargarOpciones()
    {
        listaOpcionesSincr = new ArrayList<>();
        strOpcionesSincr = new String[]{"Seleccionar","Todo","Activos","Usuarios", "Sectores", "Inventarios"};
 
        fillSpinnerOpciones(strOpcionesSincr);
    }

    private void fillSpinnerOpciones(String[] descripciones)
    {
        PreOpcionesSincr = findViewById(R.id.SpinnerSincronizacion);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opc_sincronizacion, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        PreOpcionesSincr.setAdapter(adapter);
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver()
    {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause()
    {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    private void onNetworkChange(NetworkInfo networkInfo)
    {
        if (networkInfo != null && networkInfo.isAvailable()&& networkInfo.isConnected())
        {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                _snackbar = Snackbar.make(rlsnackbar, "Conexión a internet activa!" +
                        "\n Nombre red Wi-Fi: " + networkInfo.getExtraInfo(), 3000);
                _snackbar.setActionTextColor(Color.rgb(179,179,179));
                View snackBarView = _snackbar.getView();
                snackBarView.setBackgroundColor(Color.rgb(4,165,77));
                _snackbar.show();
            }
        }
        else
        {
            dialog.dismiss();
            _snackbar = Snackbar.make(rlsnackbar, "Se ha perdido la conexión a internet.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(242,59,59));
            _snackbar.show();
        }
    }

    NetworkConnection isAvailable = new NetworkConnection(this, new NetworkConnection.EntoncesHacer() {
        @Override
        public void cuandoHayInternet()
        {
            _snackbar = Snackbar.make(rlsnackbar, "Se ha conectado con el servidor.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(4,165,77));
            _snackbar.show();
            _isConnected = true;
        }

        @Override
        public void cuandoNOHayInternet()
        {
            _snackbar = Snackbar.make(rlsnackbar, "No hay conexión con el servidor.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(242,59,59));
            _snackbar.show();
            dialog.dismiss();
            dialogEnvio.dismiss();
            btn_obtener.setEnabled(true);
            btn_enviar.setEnabled(true);
            Alerta("Atención", "Se ha perdido la conexión, intente sincronizar nuevamente");
            _isConnected = false;
        }
    });

    public boolean isOnline()
    {
        Runtime runtime = Runtime.getRuntime();
        try
        {
            dialog2 = new ProgressDialog(_context);
            dialog2.setMessage("Verificando conexión con el servidor");
            dialog2.setCancelable(false);
            dialog2.show();
            Process ipProcess = runtime.exec("ping -c 2 -w 4 www.google.com");
            int     exitValue = ipProcess.waitFor();
            dialog2.dismiss();
            return (exitValue == 0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    //endregion

    //region Controles y Eventos
    public void controles(){
        dialog = new ProgressDialog(_context);
        dialogEnvio = new ProgressDialog(_context);
        mSincronizarView = findViewById(R.id.SincronizarForm);
        SincronizarDBHelper = new SincronizarDBHelper(mSincronizarView.getContext());
        btn_enviar = (Button) findViewById(R.id.btn_enviar);
        btn_obtener = (Button) findViewById(R.id.btn_obtener);
        PreOpcionesSincr = findViewById(R.id.SpinnerSincronizacion);
        //radio_enviar = (RadioButton) findViewById(R.id.radio_Ingresa);
        radio_sincro = (RadioButton) findViewById(R.id.radio_Actualiza);
        radio_Tags = (RadioButton) findViewById(R.id.radio_tagSync);
        Mensaje = findViewById(R.id.mensaje);
        rlsnackbar = findViewById(R.id.sincronizar_view);
    }

    public void eventos()
    {
        btn_enviar.setOnClickListener(OnClickListenerenviar);
        btn_obtener.setOnClickListener(OnClickListenerObtener);
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }
    //endregion

    //region Roles Y Usuarios
    //Consumir web service Usuarios
    public void getUsuarios()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();

        try
        {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mSincronizarView.getContext(), "/ObtenerUsuario",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {deserializeUsuarios(new String(responseBody));}

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mSincronizarView.getContext(), "Error al sincronizar" +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (UnsupportedEncodingException  e) {
            e.printStackTrace();
        }
    }

    //Obtener respuesta Web service Usuarios
    public void deserializeUsuarios(String response) {
        try
        {
            Usuario = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("ObtenerUsuariosResult");
            //esto solo funciona si el web services devuelve una lista

            for(int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject UsuariosEncontrados = jsonArray.getJSONObject(i);
                Usuario.add(
                    new EntidadUsuarios(
                        UsuariosEncontrados.getString("IdUsuario"),
                        UsuariosEncontrados.getString("NombreUsuario"),
                        UsuariosEncontrados.getString("email"),
                        UsuariosEncontrados.getString("pass"),
                        UsuariosEncontrados.getString("bloqueado"),
                        UsuariosEncontrados.getString("aprobado"),
                        UsuariosEncontrados.getString("sesionActiva"),
                        UsuariosEncontrados.getString("contrasenaFallida"),
                        UsuariosEncontrados.getString("UltimaActividad"),
                        UsuariosEncontrados.getString("UltimoInicio"),
                        UsuariosEncontrados.getString("FechaBloqueo")
                    ));
            }

            Exitos++;
            exitosRecibidos++;
            InsertOrReplaceUsuarios(Usuario);

            boolean estado=ObtenerEstadoUbicaciones();

            if(estado)
            {
                Toast.makeText(mSincronizarView.getContext(), "La sincronización de usuarios " + "ha sido exitosa.", Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

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
                    asset.put("assetId", listActivos.get(i).getAssetId());
                    asset.put("numero", listActivos.get(i).getNumero());
                    asset.put("placa", listActivos.get(i).getCodeBar());
                    asset.put("longDescription", listActivos.get(i).getDescripcion());
                    asset.put("companySysId", listActivos.get(i).getIdCompania());
                    asset.put("buildingSysId", listActivos.get(i).getIdEdificio());
                    asset.put("floorSysId", listActivos.get(i).getIdPiso());
                    asset.put("officeSysId", listActivos.get(i).getIdOficina());
                    asset.put("encargado", listActivos.get(i).getEmployeeRelated());
                    asset.put("brand", listActivos.get(i).getMarca());
                    asset.put("modelNo", listActivos.get(i).getModelo());
                    asset.put("serial", listActivos.get(i).getSerial());
                    asset.put("tagId", listActivos.get(i).getTag());
                    asset.put("compania", listActivos.get(i).getCompania());
                    asset.put("edificio", listActivos.get(i).getEdificio());
                    asset.put("piso", listActivos.get(i).getPiso());
                    asset.put("oficina", listActivos.get(i).getOficina());
                    asset.put("parentAssetSysId", listActivos.get(i).getParentAssetSysId());
                    asset.put("assetStatusSysId", listActivos.get(i).getAssetStatusSysId());
                    asset.put("entryUser", listActivos.get(i).getEntryUser());
                    asset.put("AnnoFabricacion", listActivos.get(i).getAnoFabricacion());
                    asset.put("Capacidad", listActivos.get(i).getCapacidad());
                    asset.put("EstadoDescripcion", listActivos.get(i).getEstadoDescripcion());
                    asset.put("EstadoConservacion", listActivos.get(i).getEstadoConservacion());

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

    //region Botones
    private Button.OnClickListener OnClickListenerenviar = new View.OnClickListener()
    {
        Handler handleEnvid = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dialogEnvio.incrementProgressBy(5); // Incremented By Value 2
            }
        };
        @Override
        public void onClick(View v) {
        try {
            btn_enviar.setEnabled(false);
            dialogEnvio.setMax(100);
            dialogEnvio.setTitle("Almacenando Resultado");
            dialogEnvio.setMessage("Espere un momento...");
            dialogEnvio.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialogEnvio.setCancelable(false);
            dialogEnvio.show();
            long id = PreOpcionesSincr.getSelectedItemId();

                if(_isConnected)
                {
                    if (id == 0) { //No seleccionado
                        dialogEnvio.dismiss();
                        Alerta("ATENCIÓN", "Debe seleccionar una opción para sincronizar");
                        btn_enviar.setEnabled(true);
                    }else if(id == 1) { //Todos
                        SincronizarTags();
                        ActualizarToma();
                        SincronizarInventario();
                        InsertarActivos();
                        ActualizarActivos();
                        //SincronizarTagsClasificados();    //No se va a utilizar
                        // EnviarFotos();                   //No se va a utilizar

                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    while (exitosEnviados <= 5 && dialogEnvio.getProgress() <= dialogEnvio.getMax()) //5 porque se utilizan 5 métodos
                                    {
                                        Thread.sleep(300);
                                        handleEnvid.sendMessage(handleEnvid.obtainMessage());
                                        if ( dialogEnvio.getProgress() == dialogEnvio.getMax())
                                        {
                                            exitosEnviados = 0;
                                            dialogEnvio.setProgress(0);
                                            String mensaje ="Sincronización Exitosa";
                                            Mensaje.setText(mensaje);
                                            dialogEnvio.dismiss();
                                            //btn_enviar.setEnabled(true);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    btn_enviar.setEnabled(true);  //Se establece aquí para poder editar la interfaz que pertenece al hilo original
                                                }
                                            });

                                        }
                                    }
                                }
                                catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            }
                        }).start();


                    }
                    else if(id == 2){ //Activos
                        //SincronizarTags();
                        //ActualizarToma();
                        //SincronizarInventario();
                        InsertarActivos();
                        ActualizarActivos();

                        //EnvioFotoVolley();
                        /*String activosHH = Noenviados.get(0);
                        String ActivosNuevos = Noenviados.get(1);
                        Alerta("ATENCION", "No se pudo sincronizar lo siguiente: " + "\n" +  "- " + activosHH + "\n" + "- " + ActivosNuevos);
                        Noenviados.clear();*/

                          new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        while (exitosEnviados <= 2 && dialogEnvio.getProgress() <= dialogEnvio.getMax()) { //2 porque se utilizan 2 métodos
                                            Thread.sleep(200);
                                            handleEnvid.sendMessage(handleEnvid.obtainMessage());
                                            if (dialogEnvio.getProgress() == dialogEnvio.getMax())
                                            {
                                                exitosEnviados = 0;
                                                dialogEnvio.setProgress(0);
                                                String mensaje ="Sincronización Exitosa";
                                                Mensaje.setText(mensaje);
                                                dialogEnvio.dismiss();
                                                //btn_enviar.setEnabled(true);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        btn_enviar.setEnabled(true);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    catch (Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();


                    }
                    else if(id == 3){ //Usuarios
                        /*  No hay una acción en específica por lo cual se actualizará todas*/
                        SincronizarTags();
                        ActualizarToma();
                        SincronizarInventario();
                        InsertarActivos();
                        ActualizarActivos();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    while (exitosEnviados <= 5 && dialogEnvio.getProgress() <= dialogEnvio.getMax()) {  //5 porque se utilizan 5 métodos
                                        handleEnvid.sendMessage(handleEnvid.obtainMessage());
                                        Thread.sleep(200);

                                        if(dialogEnvio.getProgress() == dialogEnvio.getMax()) {
                                            exitosEnviados = 0;
                                            dialogEnvio.setProgress(0);
                                            String mensaje ="Sincronización Exitosa";
                                            Mensaje.setText(mensaje);
                                            dialogEnvio.dismiss();
                                            //btn_enviar.setEnabled(true);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    btn_enviar.setEnabled(true);
                                                }
                                            });
                                        }
                                    }
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        }).start();

                    }
                    else if (id == 4){ //Sectores
                        SincronizarTags();
                        //ActualizarToma();
                        //SincronizarInventario();
                        //InsertarActivos();
                        //ActualizarActivos();

                        /*String mensaje = Noenviados.get(0);
                        if(Alerta("Atención", mensaje)){  Noenviados.clear();  dialogEnvio.show();  }*/

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        while (exitosEnviados <= 1 && dialogEnvio.getProgress() <= dialogEnvio.getMax()) { //1 porque se utiliza 1 método
                                            handleEnvid.sendMessage(handleEnvid.obtainMessage());
                                            Thread.sleep(200);

                                            if(dialogEnvio.getProgress() == dialogEnvio.getMax()) {
                                                exitosEnviados = 0;
                                                dialogEnvio.setProgress(0);
                                                String mensaje ="Sincronización Exitosa";
                                                Mensaje.setText(mensaje);
                                                dialogEnvio.dismiss();
                                                //btn_enviar.setEnabled(true);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        btn_enviar.setEnabled(true);
                                                    }
                                                });
                                            }
                                        }
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();

                    }
                    else if(id == 5) { //Inventario
                        //SincronizarTags();
                        ActualizarToma();
                        SincronizarInventario();
                        //InsertarActivos();
                        //ActualizarActivos();

                            /* String toma = Noenviados.get(0);
                            String detalle = Noenviados.get(1);
                            Alerta("ATENCION", "No se pudo sincronizar lo siguiente: " + "\n" + "- " + toma + "\n" + "- " + detalle);
                            Noenviados.clear(); */
                            new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (exitosEnviados <= 2 && dialogEnvio.getProgress() <= dialogEnvio.getMax()) {  //2 porque se utilizan 2 métodos
                                        Thread.sleep(200);
                                        handleEnvid.sendMessage(handleEnvid.obtainMessage());
                                        if (dialogEnvio.getProgress() == dialogEnvio.getMax()) {
                                            exitosEnviados = 0;
                                            dialogEnvio.setProgress(0);
                                            String mensaje = "Sincronización Exitosa";
                                            Mensaje.setText(mensaje);
                                            dialogEnvio.dismiss();
                                            //btn_enviar.setEnabled(true);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    btn_enviar.setEnabled(true);  //
                                                }
                                            });
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }).start();


                    }
                }
                else
                {
                    dialogEnvio.dismiss();
                    btn_enviar.setEnabled(true);
                    _snackbar = Snackbar.make(rlsnackbar, "No hay conexión con el servidor, " +
                            "verifique su conexión.", 5000);
                    _snackbar.setActionTextColor(Color.rgb(179,179,179));
                    View snackBarView = _snackbar.getView();
                    snackBarView.setBackgroundColor(Color.rgb(242,59,59));
                    _snackbar.show();
                }
             /* NetworkUsages activeid_api = new NetworkUsages();*/
                 /*if(isOnline()) {
                     if(radio_sincro.isChecked()){
                         ActualizarActivos();
                         SincronizarTags();
                         ActualizarToma();
                         SincronizarInventario();
                         Toast.makeText(getApplicationContext(), "Se ha completado la sincronización", Toast.LENGTH_LONG).show();
                         InsertarActivos();
                     }else if(radio_Tags.isChecked()){
                         SincronizarTags();

                         Toast.makeText(getApplicationContext(), "Se ha completado la sincronización", Toast.LENGTH_LONG).show();
                     }else {
                         Toast.makeText(getApplicationContext(), "Debe seleccionar una opción", Toast.LENGTH_LONG).show();
                     }*/

        }catch(Exception e){
            dialogEnvio.dismiss();
            btn_enviar.setEnabled(true);
            e.printStackTrace();
        }
    }};

    public Button.OnClickListener OnClickListenerObtener = new View.OnClickListener()
    {
        Handler handleRecibido = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
               // dialog.incrementProgressBy(2); // Incremented By Value 2
            }
        };
        @Override
        public void onClick(View v)
        {
            btn_obtener.setEnabled(false);

            long id = PreOpcionesSincr.getSelectedItemId();

            if(_isConnected)
            {
                if (id == 0) //No seleccionado
                {
                   // dialog.dismiss();
                    Alerta("ATENCIÓN", "Debe seleccionar una opción para sincronizar");
                    btn_obtener.setEnabled(true);
                }
                else if(id == 1) //Todos
                {
                    getUsuarios();
                    getRolHH();
                    getRazones();
                    getEdificios();
                    getPisos();
                    getOficinas();
                    getTomaFisica();
                    getTipoInventario();
                    getTomaDetalle();
                    getTags();
                    getTagsType();
                    getAssetStatus();
                    getcategoriaActivos();

                    getActivosBySegmentsV3(0, 10000, 150000, 10000);
                    getActivosBySegmentsV3(150000, 160000, 250000, 10000);
                    getActivosBySegmentsV3(250000, 260000, 400000, 10000);

                    getActivosBySegmentsV3(400000, 450000, 500000, 10000);
                    getActivosBySegmentsV3(600000, 650000, 700000, 10000);
                    getActivosBySegmentsV3(700000, 750000, 800000, 10000);
                    getActivosBySegmentsV3(800000, 850000, 900000, 10000);
                    getActivosBySegmentsV3(900000, 950000, 1000000, 10000);
                    getActivosBySegmentsV3(1000000, 1010000, 1100000, 10000);
                    getActivosBySegmentsV3(1100000, 1150000, 1200000, 10000);
                    getActivosBySegmentsV3(1200000, 1250000, 1300000, 10000);

                    getEmployees(0, 10000, 40000, 10000);
                }
                else if(id == 2) //Activos
                {
                    getAssetStatus();
                    getcategoriaActivos();

                    getActivosBySegmentsV3(0, 10000, 150000, 10000);
                    getActivosBySegmentsV3(150000, 160000, 250000, 10000);
                    getActivosBySegmentsV3(250000, 260000, 400000, 10000);

                    getActivosBySegmentsV3(400000, 450000, 500000, 10000);
                    getActivosBySegmentsV3(600000, 650000, 700000, 10000);
                    getActivosBySegmentsV3(700000, 750000, 800000, 10000);
                    getActivosBySegmentsV3(800000, 850000, 900000, 10000);
                    getActivosBySegmentsV3(900000, 950000, 1000000, 10000);
                    getActivosBySegmentsV3(1000000, 1010000, 1100000, 10000);
                    getActivosBySegmentsV3(1100000, 1150000, 1200000, 10000);
                    getActivosBySegmentsV3(1200000, 1250000, 1300000, 10000);

                    getEmployees(0, 10000, 40000, 10000);
                }
                else if(id == 3) //Usuarios
                {
                    getUsuarios();
                    getRolHH();
                }
                else if (id == 4) //Sectores
                {
                    getRazones();
                    getEdificios();
                    getPisos();
                    getOficinas();
                }
                else if(id == 5) //Inventarios
                {
                    getTomaFisica();
                    getTipoInventario();
                    getTomaDetalle();
                }
            }
            else
            {
                btn_obtener.setEnabled(true);
                _snackbar = Snackbar.make(rlsnackbar, "No hay conexión con el servidor, " +
                        "verifique su conexión.", 5000);
                _snackbar.setActionTextColor(Color.rgb(179,179,179));
                View snackBarView = _snackbar.getView();
                snackBarView.setBackgroundColor(Color.rgb(242,59,59));
                _snackbar.show();
            }
    }};

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

    public boolean InsertOrReplaceUsuarios (ArrayList<EntidadUsuarios> usuario){

        return  SincronizarDBHelper.InsertOrReplaceUsuarios(usuario);
    }

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
    //endregion

    }

