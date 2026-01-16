package com.example.diverscan.activeid.UI.config;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.AppDatabaseHelper;

public class LimpiarBaseDatosActivity extends AppCompatActivity {

    private Button btnLimpiarBD;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limpiar_base_datos);
        context = this;

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Limpiar Base de Datos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnLimpiarBD = findViewById(R.id.btnLimpiarBD);
        btnLimpiarBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarConfirmacion();
            }
        });
    }

    private void mostrarConfirmacion() {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar limpieza")
                .setMessage("¿Estás SEGURO de que quieres eliminar TODOS los datos locales? Esta acción NO se puede deshacer.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("SÍ, ELIMINAR TODO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ejecutarLimpieza();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarLimpieza() {
        try {
            AppDatabaseHelper helper = new AppDatabaseHelper(context);
            helper.clearAllData();
            Toast.makeText(context, "Base de datos limpiada correctamente.", Toast.LENGTH_LONG).show();
            finish(); // Cerrar actividad
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al limpiar la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
