package com.example.diverscan.activeid.UI.activo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RFID.RfidListener;
import com.example.diverscan.activeid.RFID.RfidManager;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoFotoEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegistroActivoFotoTagActivity extends AppCompatActivity implements RfidListener {

    private RfidManager rfidManager;
    private ImageView imgFoto1, imgFoto2, imgFoto3, imgFoto4, imgFoto5;
    private AutoCompleteTextView spUbicacionSecundaria;
    private EditText etRfidTag;
    private Button btnGuardar;

    private RegistroActivoFotoTagViewModel viewModel;

    private final List<Bitmap> fotosSeleccionadas = new ArrayList<>();
    private int fotoActual = -1;

    private final ActivityResultLauncher<Intent> launcherGaleria =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imagenUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
                        fotosSeleccionadas.add(bitmap);
                        mostrarFoto(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_activo_foto_tag);

        inicializarVistas();

        rfidManager = new RfidManager(this, this);

        rfidManager.connect();

        viewModel = new ViewModelProvider(this).get(RegistroActivoFotoTagViewModel.class);

        // Observa catálogo de ubicaciones secundarias
        viewModel.getUbicacionesSecundarias().observe(this, ubicaciones -> {
            if (ubicaciones != null && !ubicaciones.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, ubicaciones);
                spUbicacionSecundaria.setAdapter(adapter);
            }
        });

        String idActivo = getIntent().getStringExtra("idActivo");

        if (idActivo == null || idActivo.isEmpty()) {
            idActivo = getSharedPreferences("RegistroActivo", MODE_PRIVATE).getString("idActivo", "");
        }


        // Cargar catálogo
        viewModel.cargarUbicacionesSecundarias(this);

        // Listeners para imágenes
        configurarClickImagenes();

        // Guardar
        btnGuardar.setOnClickListener(v -> guardarInformacion());
    }

    private void inicializarVistas() {
        imgFoto1 = findViewById(R.id.imgFoto1);
        imgFoto2 = findViewById(R.id.imgFoto2);
        imgFoto3 = findViewById(R.id.imgFoto3);
        imgFoto4 = findViewById(R.id.imgFoto4);
        imgFoto5 = findViewById(R.id.imgFoto5);
        spUbicacionSecundaria = findViewById(R.id.spUbicacionSecundaria);
        etRfidTag = findViewById(R.id.etRfidTag);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    @Override
    public void onConnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Lector conectado", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onTagRead(String epc) {
        runOnUiThread(() -> {
            etRfidTag.setText(epc);
            Toast.makeText(this, "TAG leído: " + epc, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, "Error lector: " + message, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void onReaderDisconnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Lector desconectado", Toast.LENGTH_LONG).show()
        );
    }

    private void configurarClickImagenes() {
        imgFoto1.setOnClickListener(v -> abrirGaleria(1));
        imgFoto2.setOnClickListener(v -> abrirGaleria(2));
        imgFoto3.setOnClickListener(v -> abrirGaleria(3));
        imgFoto4.setOnClickListener(v -> abrirGaleria(4));
        imgFoto5.setOnClickListener(v -> abrirGaleria(5));
    }

    private void abrirGaleria(int numeroFoto) {
        fotoActual = numeroFoto;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    private void mostrarFoto(Bitmap bitmap) {
        switch (fotoActual) {
            case 1 -> imgFoto1.setImageBitmap(bitmap);
            case 2 -> imgFoto2.setImageBitmap(bitmap);
            case 3 -> imgFoto3.setImageBitmap(bitmap);
            case 4 -> imgFoto4.setImageBitmap(bitmap);
            case 5 -> imgFoto5.setImageBitmap(bitmap);
        }
    }

    private void guardarInformacion() {
        String ubicacionSec = spUbicacionSecundaria.getText().toString();
        String rfid = etRfidTag.getText().toString();

        if (rfid.isEmpty()) {
            Toast.makeText(this, "Debe escanear o ingresar un TAG RFID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de formato básico de EPC (opcional, ejemplo 24 caracteres hex)
        if (!rfid.matches("^[A-Fa-f0-9]+$")) {
             Toast.makeText(this, "El TAG debe contener solo caracteres hexadecimales", Toast.LENGTH_SHORT).show();
             return;
        }

        SharedPreferences prefs = getSharedPreferences("RegistroActivo", MODE_PRIVATE);
        String idActivo = prefs.getString("idActivo", "");

        // --- Construimos el Activo COMPLETO ---
        ActivoEntity activo = new ActivoEntity();
        activo.setIdActivo(idActivo);

        activo.setNumeroActivo(prefs.getString("NumeroActivo", ""));
        activo.setNumeroEtiqueta(prefs.getString("NumeroEtiqueta", ""));
        activo.setDescripcionCorta(prefs.getString("Descripcion", ""));

        activo.setUbicacionA(prefs.getString("UbicacionA", ""));
        activo.setUbicacionB(prefs.getString("UbicacionB", ""));
        activo.setUbicacionC(prefs.getString("UbicacionC", ""));
        activo.setUbicacionD(prefs.getString("UbicacionD", ""));
        activo.setUbicacionSecundaria(ubicacionSec);

        activo.setTagEpc(rfid);

        viewModel.guardarActivoFinal(this, activo);

        Toast.makeText(this, "Activo registrado correctamente", Toast.LENGTH_LONG).show();

        prefs.edit().clear().apply();
        finish();
    }
}
