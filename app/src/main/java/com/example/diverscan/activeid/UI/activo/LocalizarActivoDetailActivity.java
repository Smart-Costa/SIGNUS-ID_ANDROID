package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diverscan.activeid.Utilities.PermissionUtils;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityLocalizarActivoDetailBinding;

public class LocalizarActivoDetailActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityLocalizarActivoDetailBinding binding;
    private TagWriter rfidHandler;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;
    private String epcLeido;
    private boolean isScanning = false;
    private long lastTriggerEventAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocalizarActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        PermissionUtils.requestPermissions(this);

        activoDAO = new ActivoDao(this);
        
        initRFID();

        // BUGFIX: El listener original llamaba rfidHandler.startRead() incondicionalmente,
        // incluso si el usuario des-seleccionaba el checkbox. Ahora solo inicia si está checked.
        binding.opcRFID.setOnClickListener(v -> {
            if (rfidHandler != null && binding.opcRFID.isChecked()) {
                rfidHandler.startRead();
            }
        });
        
        initEvents();
    }

    private void initRFID() {
        try {
            rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.setResponseHandler(this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error inicializando RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Validar si vienen múltiples tags
        if (tagData.length > 1) {
            runOnUiThread(() -> {
                if (rfidHandler != null) rfidHandler.stopRead();
                isScanning = false;
                Toast.makeText(this, "Múltiples etiquetas detectadas. Por favor acerque solo una.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        String epc = tagData[0].getEpc();
        if (epc != null && !epc.isEmpty()) {
            runOnUiThread(() -> procesarLecturaRFID(epc));
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
                if (!isScanning) {
                    rfidHandler.startRead();
                    isScanning = true;
                    runOnUiThread(() -> Toast.makeText(this, "Leyendo...", Toast.LENGTH_SHORT).show());
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

    private void procesarLecturaRFID(String epc) {
        if (epc == null || epc.trim().isEmpty()) {
            return;
        }
        if (epcLeido != null) {
            if (!epc.equals(epcLeido)) {
                if (rfidHandler != null) rfidHandler.stopRead();
                isScanning = false;
                Toast.makeText(this, "Se detectaron múltiples TAGs. Acerque solo 1 y reintente.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // BUGFIX: getActivoByEpc() es una query SQLite que debe ejecutarse en background.
        // Antes se ejecutaba directamente en el UI thread, con riesgo de ANR.
        final String epcFinal = epc;
        new Thread(() -> {
            ActivoEntity activo = activoDAO.getActivoByEpc(epcFinal);
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing()) return;
                if (activo != null) {
                    activoLeido = activo;
                    epcLeido = epcFinal;
                    binding.txtNumeroActivo.setText(activo.getNumeroActivo());
                    binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
                    binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
                    if (rfidHandler != null) rfidHandler.stopRead();
                    isScanning = false;
                } else {
                    Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void mostrarPopupActivo(ActivoEntity activo, String epc) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_info_activo, null);

        TextView txtEpc = view.findViewById(R.id.txtEpcdlg);
        TextView txtPlaca = view.findViewById(R.id.txtPlacadlg);
        TextView txtDescripcion = view.findViewById(R.id.txtDescripciondlg);
        View btnCerrar = view.findViewById(R.id.btnCerrardlg);

        txtEpc.setText(epc);
        txtPlaca.setText(activo.getNumeroEtiqueta());
        txtDescripcion.setText(activo.getDescripcionCorta());

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void initEvents() {

        binding.btnGuardar.setOnClickListener(view -> {

            if (activoLeido != null) {
                mostrarPopupActivo(activoLeido, epcLeido);
                return;
            }

            String id = binding.txtNumeroActivo.getText().toString();

            if (id.isEmpty()) {
                Toast.makeText(this, "No hay activo para buscar", Toast.LENGTH_SHORT).show();
                return;
            }

            // BUGFIX: getActivoById() movido a background thread para evitar ANR
            final String idFinal = id;
            new Thread(() -> {
                ActivoEntity activo = activoDAO.getActivoById(idFinal);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    if (activo != null) {
                        String epcReal = activo.getEpc();
                        if (epcReal == null || epcReal.isEmpty()) {
                            epcReal = "Sin EPC";
                        }
                        mostrarPopupActivo(activo, epcReal);
                    } else {
                        Toast.makeText(this, "Activo no encontrado", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }
}
