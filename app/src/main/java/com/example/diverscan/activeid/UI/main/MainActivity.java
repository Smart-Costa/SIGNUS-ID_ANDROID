package com.example.diverscan.activeid.UI.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.diverscan.activeid.Assign_tag_Offices.Asignar_tag_sector;
import com.example.diverscan.activeid.GeneralTag.ConfiguracionAntena;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.Inventory.Cargar_Toma_Fisica;
import com.example.diverscan.activeid.Locate_Assets.Actualizar_activo;
import com.example.diverscan.activeid.Locate_Assets.AsignarUbicacion;
import com.example.diverscan.activeid.Oficina.ActivosPorSector;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Sincronizar.sincronizar_base;
import com.example.diverscan.activeid.UI.activo.RegistroActivoUbicacionActivity;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.databinding.ActivityMainBinding;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.HHRolHelper;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;
import com.google.android.material.snackbar.Snackbar;
import com.zebra.rfid.api3.TagData;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityMainBinding binding;

    private HHRolHelper rolHelper;
    private AssetsDBHelper assetsDB;
    private TagsDBHelper tagsDB;
    private SessionManager sessionManager;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Activity _activity;
    private Context _context;
    private AlertDialog alertDialog;

    private String username, userId;
    private TagWriter rfidHandler;
    private CountDownTimer sessionTimer;

    private static final long SESSION_DURATION = 15 * 60 * 1000L; // 15 minutos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        //rolHelper = new HHRolHelper(this);
        //assetsDB = new AssetsDBHelper(this);
        //tagsDB = new TagsDBHelper(this);

        _activity = this;
        _context = this;

        validateSession();

        setupToolbarAndDrawer();
        setupFab();

        username = sessionManager.getUsername();
        userId = sessionManager.getUserId();
        setUserInfo(username);

        List<String> roles = Arrays.asList(
                "28 - HH Creación de Activo",
                "29 - HH Creación de Inventario",
                "30 - HH Actualización de Activo",
                "31 - HH Asignación de tag a sector",
                "32 - HH Sincronizador",
                "34 - HH Ajustar Ubicación",
                "35 - HH Configuración de antenas",
                "37 - HH Activos Por Sector"
        );
        binding.navView.post(() -> showHideItemMenu(roles));
        //showHideItemMenu(roles);
    }

    /* Session Validation */
    private void validateSession() {
        String token = SessionManager.getToken(this);
        if (token == null || isTokenExpired(token)) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Sesión expirada. Inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
        SessionManager.clearSession(this);
        ApiClient.resetInstance();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;
            String payloadJson = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            JSONObject payload = new JSONObject(payloadJson);
            long exp = payload.optLong("exp", 0);
            long now = System.currentTimeMillis() / 1000;
            return exp < now;
        } catch (Exception e) {
            return true;
        }
    }

    /* Config */
    private void setupToolbarAndDrawer() {
        setSupportActionBar(binding.appBarMain.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = binding.drawerLayout;
        toggle = new ActionBarDrawerToggle(this, drawer, binding.appBarMain.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void setupFab() {
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Acción rápida no configurada", Snackbar.LENGTH_LONG)
                        .setAction("OK", null)
                        .show());
    }

    /* User Menu and Roles */
    private void setUserInfo(String username) {
        View headerView = binding.navView.getHeaderView(0);
        TextView profileName = headerView.findViewById(R.id.txtUserMain);
        profileName.setText(username != null ? username : "Usuario");
    }

    private void showHideItemMenu(List<String> permisosHH) {
        try {
            Menu drawerMenu = binding.navView.getMenu();

            for (String permiso : permisosHH) {
                switch (permiso) {
                    case "28 - HH Creación de Activo":
                        drawerMenu.findItem(R.id.sub_crear_activo).setVisible(true);
                        break;

                    case "29 - HH Creación de Inventario":
                        drawerMenu.findItem(R.id.sub_hacer_inventario).setVisible(true);
                        break;

                    case "30 - HH Actualización de Activo":
                        drawerMenu.findItem(R.id.sub_actualizar_activo).setVisible(true);
                        break;

                    case "31 - HH Asignación de tag a sector":
                        drawerMenu.findItem(R.id.sub_asignar_tag_sector).setVisible(true);
                        break;

                    case "32 - HH Sincronizador":
                        drawerMenu.findItem(R.id.sub_sincronizar).setVisible(true);
                        break;

                    case "34 - HH Ajustar Ubicación":
                        drawerMenu.findItem(R.id.sub_ajuste_ubicacion).setVisible(true);
                        break;

                    case "35 - HH Configuración de antenas":
                        drawerMenu.findItem(R.id.sub_configurar_antena).setVisible(true);
                        break;

                    case "37 - HH Activos Por Sector":
                        drawerMenu.findItem(R.id.sub_activos_sector).setVisible(true);
                        break;
                }
            }
        } catch (Exception ex) {
            Log.e("LOGIN_MENU", "Error al mostrar permisos: " + ex.getMessage());
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.sub_crear_activo) {
            //intent = new Intent(this, SelectLocationActivity.class);
            intent = new Intent(this, RegistroActivoUbicacionActivity.class);
        } else if (id == R.id.sub_hacer_inventario) {
            intent = new Intent(this, Cargar_Toma_Fisica.class);
        } else if (id == R.id.sub_actualizar_activo) {
            intent = new Intent(this, Actualizar_activo.class);
        } else if (id == R.id.sub_ajuste_ubicacion) {
            intent = new Intent(this, AsignarUbicacion.class);
        } else if (id == R.id.sub_asignar_tag_sector) {
            intent = new Intent(this, Asignar_tag_sector.class);
        } else if (id == R.id.sub_configurar_antena) {
            intent = new Intent(this, ConfiguracionAntena.class);
        } else if (id == R.id.sub_sincronizar) {
            intent = new Intent(this, sincronizar_base.class);
        } else if (id == R.id.sub_activos_sector) {
            intent = new Intent(this, ActivosPorSector.class);
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void debugMenuIds() {
        Menu menu = binding.navView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Log.d("LOGIN_MENU", "Item: " + item.getTitle() + " ID: " + item.getItemId());
            if (item.hasSubMenu()) {
                SubMenu sub = item.getSubMenu();
                for (int j = 0; j < sub.size(); j++) {
                    MenuItem subItem = sub.getItem(j);
                    Log.d("LOGIN_MENU", "   SubItem: " + subItem.getTitle() + " ID: " + subItem.getItemId());
                }
            }
        }
    }

    /* Tools */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            cerrarSesion();
            return true;
        } else if (id == R.id.mi_perfil) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("En Construcción")
                    .setMessage("Esta sección aún no está disponible para esta versión.")
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", null)
                    .show();
            return true;
        } else if (id == R.id.action_settings) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.alertaicono)
                    .setTitle("Configuraciones")
                    .setMessage("Aquí podrás ajustar las preferencias de la aplicación.")
                    .setCancelable(true)
                    .setPositiveButton("Aceptar", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.alertaicono)
            .setTitle("Advertencia")
            .setMessage("Está a punto de salir. ¿Realmente desea cerrar la sesión?")
            .setCancelable(false)
            .setNegativeButton("No", null)
            .setPositiveButton("Sí", (dialog, which) -> {
                Log.d("LOGIN_LOGOUT", "Botón Sí presionado, intentando cerrar sesión...");

                // Cierra sesión y limpia sesión
                SessionManager.clearSession(MainActivity.this);
                ApiClient.resetInstance();
                dialog.dismiss();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finishAffinity();

                Log.d("LOGIN_LOGOUT", "Redirigido correctamente a LoginActivity");
            })
            .show();
    }

    /* Helpers UX */
    public static void Message() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 200);
    }

    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void handleTriggerPress(boolean pressed) {}
    @Override
    public void handleTagdata(TagData[] tagData) {}
    @Override
    public Context GetContext() { return this; }


}