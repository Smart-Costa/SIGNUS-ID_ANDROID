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

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diverscan.activeid.R;
import android.content.Context;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;

public class RegistroActivoFotoTagActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private TagWriter rfidHandler;
    private ImageView imgFoto1, imgFoto2, imgFoto3, imgFoto4, imgFoto5;
    private AutoCompleteTextView spUbicacionSecundaria;
    private EditText etRfidTag;
    private Button btnGuardar;

    private RegistroActivoFotoTagViewModel viewModel;
    private boolean isScanning = false;
    private long lastTriggerEventAt = 0L;

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

        // Inicializar Singleton de TagWriter
        rfidHandler = TagWriter.getInstance();

        checkAndRequestPermissions();

        viewModel = new ViewModelProvider(this).get(RegistroActivoFotoTagViewModel.class);

        // Observa catálogo de ubicaciones secundarias
        viewModel.getUbicacionesSecundarias().observe(this, ubicaciones -> {
            if (ubicaciones != null && !ubicaciones.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, ubicaciones);
                spUbicacionSecundaria.setAdapter(adapter);
            }
        });

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

    private void initRFID() {
        try {
            if (rfidHandler == null) rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.setResponseHandler(this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error inicializando RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            initRFID();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permisos necesarios no concedidos. El lector RFID podría no funcionar.", Toast.LENGTH_LONG).show();
            } else {
                initRFID();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.stopRead();
            rfidHandler.setResponseHandler(null);
        }
        isScanning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(null);
        }
    }

    // --- Implementación de ResponseHandlerInterface ---

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        
        if (etRfidTag.getText() != null && !etRfidTag.getText().toString().trim().isEmpty()) {
            return;
        }

        java.util.Set<String> uniqueEpcs = new java.util.HashSet<>();
        for (ReaderTag tag : tagData) {
            if (tag.getEpc() != null && !tag.getEpc().isEmpty()) {
                uniqueEpcs.add(tag.getEpc());
            }
        }

        if (uniqueEpcs.size() > 1) {
            runOnUiThread(() -> {
                if (rfidHandler != null) rfidHandler.stopRead();
                isScanning = false;
                Toast.makeText(this, "Múltiples etiquetas detectadas. Por favor acerque solo una.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        if (uniqueEpcs.isEmpty()) return;
        String epcLeido = uniqueEpcs.iterator().next();

        if (epcLeido != null && !epcLeido.trim().isEmpty()) {
            final String finalEpc = epcLeido;
            runOnUiThread(() -> {
                if (rfidHandler != null) rfidHandler.stopRead();
                isScanning = false;
                etRfidTag.setText(finalEpc);
                Toast.makeText(this, "TAG leído: " + finalEpc, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastTriggerEventAt < 120L) {
            return;
        }
        lastTriggerEventAt = now;

        if (pressed) {
            if (rfidHandler != null) {
                if (etRfidTag.getText() != null && !etRfidTag.getText().toString().trim().isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "TAG ya asignado. Limpie el campo para leer otro.", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (!isScanning) {
                    rfidHandler.startRead();
                    isScanning = true;
                }
            }
        } else {
            if (rfidHandler != null && isScanning) {
                rfidHandler.stopRead();
                isScanning = false;
            }
        }
    }

    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(this, "Reader: " + msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public Context GetContext() {
        return this;
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

        if (!rfid.matches("^[A-Fa-f0-9]+$")) {
             Toast.makeText(this, "El TAG debe contener solo caracteres hexadecimales", Toast.LENGTH_SHORT).show();
             return;
        }

        SharedPreferences prefs = getSharedPreferences("RegistroActivo", MODE_PRIVATE);
        String idActivo = prefs.getString("idActivo", "");

        ActivoEntity activo = new ActivoEntity();
        activo.setIdActivo(idActivo);
        activo.setNumeroActivo(prefs.getString("NumeroActivo", ""));
        activo.setNumeroEtiqueta(prefs.getString("NumeroEtiqueta", ""));
        activo.setDescripcionCorta(prefs.getString("Descripcion", ""));
        activo.setCategoria(prefs.getString("Categoria", ""));
        activo.setEstado(prefs.getString("Estado", ""));
        activo.setEmpresa(prefs.getString("Empresa", ""));
        activo.setMarca(prefs.getString("Marca", ""));
        activo.setModelo(prefs.getString("Modelo", ""));
        activo.setUbicacionA(prefs.getString("UbicacionA", ""));
        activo.setUbicacionB(prefs.getString("UbicacionB", ""));
        activo.setUbicacionC(prefs.getString("UbicacionC", ""));
        activo.setUbicacionD(prefs.getString("UbicacionD", ""));
        activo.setUbicacionSecundaria(ubicacionSec);
        activo.setEpc(rfid);
        activo.setTagEpc(rfid);
        activo.setEstadoActivo(true);

        // Bloquear botón para evitar doble envío
        btnGuardar.setEnabled(false);
        // BUGFIX: Usar callback asíncrono para cerrar la actividad solo tras confirmar el guardado
        viewModel.guardarActivoFinal(this, activo, success -> {
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing()) return;
                btnGuardar.setEnabled(true);
                if (success) {
                    Toast.makeText(this, "Activo registrado correctamente", Toast.LENGTH_LONG).show();
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, com.example.diverscan.activeid.UI.main.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar el activo en la base de datos", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
