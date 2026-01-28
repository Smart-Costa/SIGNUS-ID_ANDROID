package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocalizarActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        
        initRFID();

        binding.opcRFID.setOnClickListener(v -> {
            if (rfidHandler != null) {
                // Si el handler ya está inicializado, aseguramos que esté en modo lectura o listo
                if (!binding.opcRFID.isChecked()) {
                   // Si era un toggle, aquí manejaríamos lógica, pero es un radiobutton/checkbox probablemente?
                   // Asumimos que es para activar el modo
                }
                // Iniciar lectura si se selecciona
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
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;

        // Validar si vienen múltiples tags
        if (tagData.length > 1) {
            runOnUiThread(() -> {
                if (rfidHandler != null) rfidHandler.stopRead();
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
        if (pressed) {
            if (rfidHandler != null) {
                rfidHandler.startRead();
                runOnUiThread(() -> Toast.makeText(this, "Leyendo...", Toast.LENGTH_SHORT).show());
            }
        } else {
            if (rfidHandler != null) {
                rfidHandler.stopRead();
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
                Toast.makeText(this, "Se detectaron múltiples TAGs. Acerque solo 1 y reintente.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ActivoEntity activo = activoDAO.getActivoByEpc(epc);

        if (activo != null) {
            activoLeido = activo;
            epcLeido = epc;
            binding.txtNumeroActivo.setText(activo.getNumeroActivo());
            binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
            binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
            if (rfidHandler != null) rfidHandler.stopRead();
        } else {
            Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
        }
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

            ActivoEntity activo = activoDAO.getActivoById(id);

            if (activo != null) {
                // Usar getEpc() en lugar de getTagEpc() para mostrar el valor real
                String epcReal = activo.getEpc();
                if (epcReal == null || epcReal.isEmpty()) {
                    epcReal = "Sin EPC";
                }
                mostrarPopupActivo(activo, epcReal);
            } else {
                Toast.makeText(this, "Activo no encontrado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
