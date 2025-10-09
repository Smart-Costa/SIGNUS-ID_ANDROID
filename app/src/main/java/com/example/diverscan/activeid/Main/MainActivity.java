package com.example.diverscan.activeid.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.diverscan.activeid.Activo.createAssets;
import com.example.diverscan.activeid.Assign_tag_Offices.Asignar_tag_sector;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.CreateAsset.SelectLocationActivity;
import com.example.diverscan.activeid.GeneralTag.ConfiguracionAntena;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Inventory.Cargar_Toma_Fisica;
import com.example.diverscan.activeid.Locate_Assets.Actualizar_activo;
import com.example.diverscan.activeid.Locate_Assets.AsignarUbicacion;
import com.example.diverscan.activeid.Oficina.ActivosPorSector;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Sincronizar.sincronizar_base;
import com.example.diverscan.activeid.Tags.ClasificacionTags;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.HHRolHelper;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.List;

//import com.example.diverscan.activeid.TestEscritura;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ResponseHandlerInterface
{
    HHRolHelper CargarRolHH = new HHRolHelper(this);
    static  ArrayList<String> Roles = new ArrayList<String>();
    NavigationView navigationView;
    AssetsDBHelper AssetsDBHelper;
    TagsDBHelper TagsDBHelper;
    private TextView txtusername;
    private String username, _userSysId, _rolSysId;
    private MenuItem itemSincronizar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private Activity _activity;
    AlertDialog alertDialog;
    private Context _context;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    TagWriter rfidHandler;
    CountDownTimer sessionActivate;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        username = SharedPreferencesGetSet.leer_local("username", this);
        _userSysId = SharedPreferencesGetSet.leer_local("_userId", this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _activity = this;
        _context = this;
        AssetsDBHelper = new AssetsDBHelper(this);
        TagsDBHelper = new TagsDBHelper(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try
                {
                    Message();
                }
                catch (Exception ex)
                {

                }
                    Snackbar.make(view, "No se ha conectado a un correo!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.post(loadUserInfo);
        // buscamos el usuario y obtenemos el rol:
        //_rolSysId
        Roles = RolesHH(_userSysId);
        showHideItemMenu(Roles);

    }
    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            if (pressed) {
                // Acción cuando se presiona el gatillo RFID
            } else {
                // Acción cuando se suelta
            }
        });
    }

    @Override
    public void handleTagdata(TagData[] tagData) {
        // Procesar los tags leídos, mostrarlos en pantalla o lo que ocupés
    }

    @Override
    public Context GetContext() {
        return this;
    }
    private void showHideItemMenu(List<String> permisosHH) {
        try{
            for (String permiso : permisosHH) {
                Menu menu = navigationView.getMenu();
                MenuItem itemMenu;
                SubMenu subMenu;
                switch (permiso) {
                    case "28 - HH Creación de Activo":
                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_CrearActivo);
                        itemMenu.setVisible(false);
                        break;

                    case "29 - Creación de Inventario":
                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_HacerInventario);
                        itemMenu.setVisible(true);
                        break;

                    case "30 - HH Actualización de Activo":
                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_ActualizarActivo);
                        itemMenu.setVisible(true);
                        break;

                    case "31 - HH Asignación de tag a sector":
                        subMenu = menu.findItem(R.id.SectoresMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_AsignarTagSector);
                        itemMenu.setVisible(true);
                        break;

                    case "32 - HH Sincronizador":
                        subMenu = menu.findItem(R.id.SincronizacionMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_Sincronizar);
                        itemMenu.setVisible(true);
                        break;

                    /*case "33 - HH Localizar Activo":
                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_LocalizarActivo);
                        itemMenu.setVisible(false);
                        break;*/

                    case "34 - HH Ajustar Ubicación":
                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_AjusteUbicacion);
                        itemMenu.setVisible(true);
                        break;

                    case "35 - HH Configuración de antenas":
                        subMenu = menu.findItem(R.id.ConfiguracionMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_ConfigurarAntena);
                        itemMenu.setVisible(true);
                        break;

                    /*case "36 - HH Clasificar Tags":
                        subMenu = menu.findItem(R.id.subClasificarTag).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_Clasificar);
                        itemMenu.setVisible(true);*/

                    case "37 - HH Activos Por Sector":
                        subMenu = menu.findItem(R.id.SectoresMenu).getSubMenu();
                        itemMenu = subMenu.findItem(R.id.sub_ActivosXSector);
                        itemMenu.setVisible(true);
                        break;

//**************************** ANTES DEL CAMBIO DE LOS PERMISOS ****************************
//                    /*case "26 -HH Creacion de Activo":
//                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_CrearActivo);
//                        itemMenu.setVisible(true);
//                        break;*/
//
//                    case "27 - HH Creacion de Inventario":
//                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_HacerInventario);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    case "28 - HH Actualizacion de Activo":
//                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_ActualizarActivo);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    case "29 - HH Asignacion de tag a sector":
//                        subMenu = menu.findItem(R.id.SectoresMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_AsignarTagSector);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    case "30 - HH Sincronizador":
//                        subMenu = menu.findItem(R.id.SincronizacionMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_Sincronizar);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    /*case "31 - HH Localizar Activo":
//                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_LocalizarActivo);
//                        itemMenu.setVisible(false);
//                        break;*/
//
//                    case "32 - HH Ajustar Ubicacion":
//                        subMenu = menu.findItem(R.id.ActivosMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_AjusteUbicacion);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    case "33 - HH Configuracion de antenas":
//                        subMenu = menu.findItem(R.id.ConfiguracionMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_ConfigurarAntena);
//                        itemMenu.setVisible(true);
//                        break;
//
//                    /*case "34 - HH Clasificar Tags":
//                        subMenu = menu.findItem(R.id.subClasificarTag).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_Clasificar);
//                        itemMenu.setVisible(true);*/
//
//                    case "35 - HH Activos Por Sector":
//                        subMenu = menu.findItem(R.id.SectoresMenu).getSubMenu();
//                        itemMenu = subMenu.findItem(R.id.sub_ActivosXSector);
//                        itemMenu.setVisible(true);
//                        break;
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    Runnable loadUserInfo = new Runnable()
    {
        @Override
        public void run() {
            setUserInfo();
        }
    };

    public void setUserInfo()
    {
        username = SharedPreferencesGetSet.leer_local("username", this);
        TextView profileName = navigationView.getHeaderView(0).findViewById(R.id.txtUserMain);
        profileName.setText(username);
        profileName.setTextColor(getResources().getColor(R.color.blanco));
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void cerrarSesion()
    {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.alertaicono)
                .setTitle("Advertencia")
                .setMessage("Está apunto de salir. ¿Realmente desea cerrar la sesión?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Message();
            AlertasError("Atención", "¿Realmente desea cerrar la sesión?");
        }
        else if(id == R.id.mi_perfil)
        {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("En Construcción")
                    .setMessage("Esta sección aún no está disponible para esta versión")
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item)
    {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        Menu menu = navigationView.getMenu();
        final MenuItem itemMenu;
        SubMenu subMenu;
        subMenu = menu.findItem(R.id.SincronizacionMenu).getSubMenu();
        itemMenu = subMenu.findItem(R.id.sub_Sincronizar);
        if(id == R.id.sub_CrearActivo)
        {
             //Intent crear = new Intent(MainActivity.this, createAssets.class);
             Intent crear = new Intent(MainActivity.this, SelectLocationActivity.class);
             startActivity(crear);
        }
        else if (id == R.id.sub_ActualizarActivo)
        {
            try
            {
                if(AssetsDBHelper.doesRecordExist())
                {
                    Intent actualizar = new Intent(MainActivity.this, Actualizar_activo.class);
                    startActivity(actualizar);
                }
                else
                {
                    Intent actualizar = new Intent(MainActivity.this, Actualizar_activo.class);
                    startActivity(actualizar);
                    //****************************************************************
                    // cambio realizado por andrey sanchez zuñiga
                    // cambio realizado el 20/03/2023
                    //****************************************************************
                    /*LayoutInflater inflater = _activity.getLayoutInflater();
                    View view = inflater.inflate(R.layout.layout_notificaciones_error, null);
                    TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
                    TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
                    txvTitulo.setText("Atención");
                    txvMessage.setText("Antes de ingresar al módulo de actualización, es necesario sincronizar los activos. \n\n¿Desea sincronizar ahora?");
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(_activity);
                    builder.setView(view);
                    builder.setPositiveButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Aceptar</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            *//*SpannableString spannableString = new SpannableString(itemMenu.getTitle().toString());
                            spannableString.setSpan(new BackgroundColorSpan(Color.LTGRAY),0, spannableString.length(),0);*//*
                                    Intent sector = new Intent(MainActivity.this,sincronizar_base.class);
                                    startActivity(sector);
                        }
                    });
                    builder.setNegativeButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Cancelar</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setIcon(R.drawable.alertaicono);
                    alertDialog = builder.show();
                    return  true;*/
                }
            }
            catch(Exception e)
            {
             e.printStackTrace();
            }

        }
        else if (id == R.id.sub_HacerInventario)
        {
             Intent inventario = new Intent(MainActivity.this,Cargar_Toma_Fisica.class);
             startActivity(inventario);
        }
        else if (id == R.id.sub_AjusteUbicacion)
        {
             Intent ajusteUbicacion = new Intent(MainActivity.this, AsignarUbicacion.class);
             startActivity(ajusteUbicacion);
        }
        else if (id == R.id.sub_AsignarTagSector)
        {
             Intent sector = new Intent(MainActivity.this,Asignar_tag_sector.class);
             startActivity(sector);

        }
        else if (id == R.id.sub_ConfigurarAntena)
        {
            Intent configurar = new Intent(MainActivity.this, ConfiguracionAntena.class);
            startActivity(configurar);
        }
        else if (id == R.id.sub_Sincronizar)
        {
             Intent sector = new Intent(MainActivity.this,sincronizar_base.class);
             startActivity(sector);
        }
        else if(id == R.id.sub_Clasificar)
        {
            try
            {
                if(TagsDBHelper.doesRecordExist())
                {
                    Intent tags = new Intent(MainActivity.this, ClasificacionTags.class);
                    startActivity(tags);
                }
                else
                {
                    LayoutInflater inflater = _activity.getLayoutInflater();
                    View view = inflater.inflate(R.layout.layout_notificaciones_error, null);
                    TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
                    TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
                    txvTitulo.setText("Atención");
                    txvMessage.setText("Antes de ingresar al módulo de clasificación de tags, es " +
                            "necesario sincronizar los tags. \n\n¿Desea sincronizar ahora?");
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(_activity);
                    builder.setView(view);
                    builder.setPositiveButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Aceptar</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sector = new Intent(MainActivity.this,sincronizar_base.class);
                            startActivity(sector);

                        }
                    });
                    builder.setNegativeButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Cancelar</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    });
                    builder.setIcon(R.drawable.alertaicono);
                    alertDialog = builder.show();
                    return  true;
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else if(id == R.id.sub_ActivosXSector)
        {
             Intent tags = new Intent(MainActivity.this, ActivosPorSector.class);
             startActivity(tags);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void cerrarAplicacion()
    {
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.alertaicono)
            .setTitle("Advertencia")
            .setMessage("Está apunto de salir. ¿Realmente desea cerrar la sesión?")
            .setCancelable(false)
            .setNegativeButton("No", null)
            .setPositiveButton("Sí", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
    }

    public static void Message()
    {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 21);
    }

    public ArrayList<String> RolesHH(String Username)
    {
        try
        {
            ArrayList<String> Roles = CargarRolHH.GetRolHH(Username);
            return Roles;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Message();
            AlertasError("Atención", "¿Realmente desea cerrar la sesión?");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean AlertasError(String titulo, String Mensaje)
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
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#D81622' background-color'#555555'>Cancelar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();
        return  true;
    }

}
