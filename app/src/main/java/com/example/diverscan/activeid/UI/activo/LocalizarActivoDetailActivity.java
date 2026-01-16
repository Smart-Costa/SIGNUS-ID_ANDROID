package com.example.diverscan.activeid.UI.activo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RFID.RfidListener;
import com.example.diverscan.activeid.RFID.RfidManager;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityLocalizarActivoDetailBinding;

public class LocalizarActivoDetailActivity extends AppCompatActivity implements RfidListener {
    private ActivityLocalizarActivoDetailBinding binding;
    private RfidManager rfidManager;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;
    private String epcLeido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocalizarActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        rfidManager = new RfidManager(this, this);

        binding.opcRFID.setOnClickListener(v -> rfidManager.connect());
        //binding.btnStart.setOnClickListener(v -> rfidManager.startReading());
        //binding.btnStop.setOnClickListener(v -> rfidManager.stopReading());

        initEvents();
    }

    public void onConnected() {
        runOnUiThread(() -> Toast.makeText(this, "Lector conectado", Toast.LENGTH_SHORT).show());
        rfidManager.startReading();
    }

    @Override
    public void onTagRead(String epc) {
        runOnUiThread(() -> procesarLecturaRFID(epc));
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void onReaderDisconnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Lector desconectado", Toast.LENGTH_LONG).show()
        );
    }

    private void procesarLecturaRFID(String epc) {
        if (epc == null || epc.trim().isEmpty()) {
            return;
        }
        if (epcLeido != null) {
            if (!epc.equals(epcLeido)) {
                rfidManager.stopReading();
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
            rfidManager.stopReading();
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

    @Override
    protected void onPause() {
        super.onPause();
        rfidManager.stopReading();
    }
}
