package com.example.diverscan.activeid.login;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.diverscan.activeid.Conexion.ACTIVEID_API;
import com.example.diverscan.activeid.Conexion.NetworkConnection;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.Inventory.EntidadUsuarios;
import com.example.diverscan.activeid.Main.MainActivity;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Roles.EntidadDatosRol;
import com.example.diverscan.activeid.sqlite.DatabaseHelper;
import com.example.diverscan.activeid.sqlite.LoginDBHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>
{
    private static final int REQUEST_READ_CONTACTS = 0;
    //private UserLoginTask mAuthTask = null;
    // UI references.
    //Variables de los text
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    View mLoginFormView;
    Button button_login;
    private Context _context;
    //Pruebas
    private String email, gender, hobbies, zodiac;
    private String username, pass, _id, bloqueado, aprobado, sesionActiva, contrasenaFallida,
            UltimaActividad, UltimoInicio,FechaBloqueo;
    DatabaseHelper sqliteHelper;
    LoginDBHelper SQLHelper;
    LinearLayout rlsnackbar;
    Snackbar _snackbar;
    ArrayList<EntidadDatosRol> RolHH;
    ArrayList<EntidadUsuarios> Usuario;
    private ProgressDialog dialog;
    private Activity _activity;
    ProgressDialog progressDialog;
    AlertDialog alertDialog;

    //*********************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserView = findViewById(R.id.user); // Inicializar mUserView

        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            changeStatusBarColor();
        }
        populateAutoComplete();
        _activity = this;
        _context = this;
        button_login=(Button) findViewById(R.id.btn_login);
        mUserView= (AutoCompleteTextView) findViewById(R.id.user);
        mPasswordView = (EditText) findViewById(R.id.password);

        button_login.setOnClickListener(OnClickListenerEscanerAceptar);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        sqliteHelper = new DatabaseHelper(mLoginFormView.getContext());
        SQLHelper = new LoginDBHelper(mLoginFormView.getContext()); // es este el de login
        rlsnackbar = findViewById(R.id.login_activity);
        try
        {
            //prueba de insert alejandra
            /*
                _id = "3A8E10C2-8FB5-4309-9752-AE2A3099371C";
                username = "admin";
                pass = "none";
                email = "test_admin@acces-o.com";
                bloqueado = "false";
                aprobado  = "true";
                sesionActiva = "true";
                contrasenaFallida = "0";
                UltimaActividad = "2012-06-29 13:18:45.697";
                UltimoInicio = "2012-06-29 13:18:45.697";
                FechaBloqueo = "2012-06-29 13:18:45.697";
            */
            boolean result = SQLHelper.guardarusuario(_id, username, pass, email, bloqueado, aprobado
                    , sesionActiva, contrasenaFallida ,UltimaActividad, UltimoInicio, FechaBloqueo);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        }
        catch (Exception ex)
        {
            ex.toString();
        }
    }

    //*********************************************************************************************
    //region Permisos De la Aplicacion

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#CE9D0C"));
    }

    //*********************************************************************************************

    private boolean contactsAndBluetoothPermissionsGranted = false;

    private void populateAutoComplete()
    {
        if (!contactsAndBluetoothPermissionsGranted) {
            mayRequestContactsAndBluetooth();
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    //*********************************************************************************************
    //Método que solicita permisos a los contactos y al bluetooth

    private boolean mayRequestContactsAndBluetooth() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_CONTACTS) &&
                shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) &&
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN) &&
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) &&
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            Snackbar.make(mUserView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS, READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_ADVERTISE}, 1903);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS, READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE}, 1903);
        }
        return false;
    }

    //*********************************************************************************************

    /**
     * Callback received when a permissions request has been completed.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1903) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactsAndBluetoothPermissionsGranted = true;
                populateAutoComplete();
            }
        }
    }

    //endregion Permisos De la Aplicacion

    //*********************************************************************************************
    //region Roles Y Usuarios
    //Consumir web service Usuarios

    public void getUsuarios()
    {
        ACTIVEID_API activeid_api = new ACTIVEID_API();
        //validacion de datos
        try
        {
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(_context, "/ObtenerUsuario",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    boolean respuesta = deserializeUsuarios(new String(responseBody));
                    if(respuesta)
                    {
                        _snackbar = Snackbar.make(rlsnackbar, "Usuarios sincronizados con éxito.", 3000);
                        _snackbar.setActionTextColor(Color.rgb(179,179,179));
                        View snackBarView = _snackbar.getView();
                        snackBarView.setBackgroundColor(Color.rgb(4,165,77));
                        _snackbar.show();
                    }
                    else
                    {
                        _snackbar = Snackbar.make(rlsnackbar, "No se han sincronizado usuarios nuevos", 6000);
                        View snackBarView = _snackbar.getView();
                        snackBarView.setBackgroundColor(Color.rgb(224,22,22));
                        TextView message = (TextView) snackBarView.findViewById(R.id.snackbar_text);
                        message.setTextColor(getResources().getColor(R.color.color2));
                        _snackbar.show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,Throwable error)
                {
                    _snackbar = Snackbar.make(rlsnackbar, "Fallo al sincronizar, verifique su conexión a internet", 6000);
                    View snackBarView = _snackbar.getView();
                    snackBarView.setBackgroundColor(Color.rgb(224,22,22));
                    TextView message = (TextView) snackBarView.findViewById(R.id.snackbar_text);
                    message.setTextColor(getResources().getColor(R.color.color2));
                    _snackbar.show();
                }
            });
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    //***********************************************************************************************
    //Obtener respuesta Web service Usuarios

    public boolean deserializeUsuarios(String response)
    {
        //validacion de datos
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
            return InsertOrReplaceUsuarios(Usuario);
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
            return false;
        }
    }

    //***************************************************************************************************
    //consumir web service de Roles Hand

    public void  getRolHH()  {

        ACTIVEID_API activeid_api = new ACTIVEID_API();
        try{
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(_context, "/ObtenerRolHH", entity, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                {
                    deserializeRolHH(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                {
                    Toast.makeText(_context, "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException  e)
        {
            e.printStackTrace();
        }
    }

    //*********************************************************************************************
    //Obtener respuesta Web service Rol Hand Held

    public void deserializeRolHH(String response)
    {
        try
        {
            RolHH = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response).getJSONObject("ObtenerRolHHResult");
            boolean state = jsonObject.getBoolean("State");
            String Mensaje =jsonObject.getString("Description");
            if(state)
            {
                JSONArray jsonArray = jsonObject.getJSONArray("Data");

                //esto solo funciona si el web services devuelve una lista
                for (int i = 0; i < jsonArray.length(); i++)
                {
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
            }else{
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("Advertencia")
                        .setMessage(Mensaje)
                        .setCancelable(false)
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        }).show();
            }
        }
        catch (JSONException e)
        {
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }

    //*********************************************************************************************

    public boolean InsertOrReplaceUsuarios (ArrayList<EntidadUsuarios> usuario){
        return  SQLHelper.InsertOrReplaceUsuarios(usuario);
    }
    public boolean InsertOrReplaceRolHH(ArrayList<EntidadDatosRol> RolHH){

        return SQLHelper.InsertOrReplaceRolHH(RolHH);
    }

    //endregion

    //*********************************************************************************************
    //region No Usa Actualmente
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isEmailValid(user)) {
            mUserView.setError(getString(R.string.error_invalid_email));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(user, password);
            mAuthTask.execute((Void) null);
        }
    }*/

    //*********************************************************************************************

    private boolean isEmailValid(String username) {
        //TODO: Replace this with your own logic
        return username.contains("@");
    }

    //*********************************************************************************************

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    //*********************************************************************************************

    /**
     * Shows the progress UI and hides the login form.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    //*********************************************************************************************

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    //*********************************************************************************************

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    //*********************************************************************************************

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {}

    //*********************************************************************************************

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserView.setAdapter(adapter);
    }

    //*********************************************************************************************

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    //*********************************************************************************************

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    /*public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUser)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(true);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
*/

    //*********************************************************************************************

    public void onBackPressed() {
        // do not call super onBackPressed.
        if (TextUtils.isEmpty(pass)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
        }

        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_field_required));
        }
    }
    //endregion

    //*********************************************************************************************

    @SuppressLint("Range")
    public void Login (View view) {

        //View focusView = null;
        username = mUserView.getText().toString();
        pass = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(pass)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
        }

        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_field_required));
        }else{
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Espere un moemento..."); // Setting Message
            progressDialog.setTitle("Validando Información"); // Setting Title
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.show(); // Display Progress Dialog
            progressDialog.setCancelable(false);

            String bloqueado ="";
            Cursor cursor = SQLHelper.Login(username, pass);

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                String userId = cursor.getString(cursor.getColumnIndex("_id"));
                username = cursor.getString(cursor.getColumnIndex("username"));
                pass = cursor.getString(cursor.getColumnIndex("pass"));
                bloqueado = cursor.getString(cursor.getColumnIndex("bloqueado"));
                int potencia = 80;
                if(bloqueado.compareToIgnoreCase("false")==0){
                    String potenciaAntena = String.valueOf(potencia);
                    SharedPreferencesGetSet.guardar_local("username", username,getApplicationContext());
                    SharedPreferencesGetSet.guardar_local("_userId", userId, getApplicationContext());
                    SharedPreferencesGetSet.guardar_local("potenciaAntena", potenciaAntena , getApplicationContext());
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(10000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            progressDialog.dismiss();
                        }
                    }).start();
                    Intent newform = new Intent(LoginActivity.this, MainActivity.class);
                    newform.putExtra("_Username",username);
                    startActivity(newform);
                }else{
                    progressDialog.dismiss();
                    AlertasError("Usuario Bloqueado","Su usuario se encuentra deshabilitado, " +
                            "si es un error comuníquese con el administrador del sistema");
                }

            }else{
                progressDialog.dismiss();
                _snackbar = Snackbar.make(rlsnackbar, "El usuario o la contraseña son incorrectos", 6000);
                View snackBarView = _snackbar.getView();

                snackBarView.setBackgroundColor(Color.rgb(224,22,22));
                TextView message = (TextView) snackBarView.findViewById(R.id.snackbar_text);
                message.setTextColor(getResources().getColor(R.color.color2));
                _snackbar.show();

            }
        }
    }

    //*********************************************************************************************

    //region Alertas y botones
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Salir("Advertencia",
                    "Está apunto de salir. ¿Realmente desea cerrar la aplicación?");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //*********************************************************************************************

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

    //*********************************************************************************************

    public boolean Salir(String titulo, String Mensaje){
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

    //*********************************************************************************************

    private Button.OnClickListener OnClickListenerEscanerAceptar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                Login(mLoginFormView);
                mUserView.setText("");
                mPasswordView.setText("");
        }};

    //endregion

    //*********************************************************************************************
    //region Validación Red

    NetworkConnection isAvailable = new NetworkConnection(this, new NetworkConnection.EntoncesHacer() {
        @Override
        public void cuandoHayInternet() {
            _snackbar = Snackbar.make(rlsnackbar, "Se ha conectado con el servidor.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(4,165,77));
            _snackbar.show();
        }

        //*********************************************************************************************

        @Override
        public void cuandoNOHayInternet() {
            _snackbar = Snackbar.make(rlsnackbar, "No hay conexión con el servidor.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(242,59,59));
            _snackbar.show();
        }
    });

    //*********************************************************************************************

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
            int     exitValue = ipProcess.waitFor();

            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    //*********************************************************************************************

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
    };

    //*********************************************************************************************

    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    //*********************************************************************************************

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    //*********************************************************************************************

    private void onNetworkChange(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.isAvailable()&& networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                getUsuarios();
                getRolHH();
            }
        } else {
            _snackbar = Snackbar.make(rlsnackbar, "Se ha perdido la conexión a internet.", 3000);
            _snackbar.setActionTextColor(Color.rgb(179,179,179));
            View snackBarView = _snackbar.getView();
            snackBarView.setBackgroundColor(Color.rgb(242,59,59));
            _snackbar.show();
        }
    }
    //endregion
}

