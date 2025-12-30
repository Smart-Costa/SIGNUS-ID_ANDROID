package com.example.diverscan.activeid.UI.activo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.RFID.RfidListener;
import com.example.diverscan.activeid.RFID.RfidManager;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityDarBajaActivoDetailBinding;

public class DarBajaActivoDetailActivity extends AppCompatActivity implements RfidListener {
    private ActivityDarBajaActivoDetailBinding binding;
    private RfidManager rfidManager;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDarBajaActivoDetailBinding.inflate(getLayoutInflater());
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
        if (activoLeido != null) {
            if (activoLeido.getTagEpc() == null || !epc.equals(activoLeido.getTagEpc())) {
                rfidManager.stopReading();
                Toast.makeText(this, "Se detectaron múltiples TAGs. Acerque solo 1 y reintente.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ActivoEntity activo = activoDAO.getActivoByEpc(epc);
        if (activo != null) {
            activoLeido = activo;
            binding.txtNumeroActivo.setText(activo.getNumeroActivo());
            binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
            binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
            binding.txtDescripcionRazon.setText("Lectura RFID exitosa");
            rfidManager.stopReading();
        } else {
            Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
        }
    }

    private void initEvents() {
        binding.btnGuardar.setOnClickListener(view -> {
            if (activoLeido == null || activoLeido.getIdActivo() == null || activoLeido.getIdActivo().trim().isEmpty()) {
                Toast.makeText(this, "Primero lea un TAG RFID para buscar el activo", Toast.LENGTH_SHORT).show();
                return;
            }

            activoLeido.setEstadoActivo(Boolean.FALSE);
            int updated = activoDAO.updateEstadoActivo(activoLeido);
            if (updated > 0) {
                Toast.makeText(this, "Activo dado de baja", Toast.LENGTH_SHORT).show();
                activoLeido = null;
                binding.txtNumeroActivo.setText("");
                binding.txtNumeroEtiqueta.setText("");
                binding.txtDescripcionCorta.setText("");
                binding.txtDescripcionRazon.setText("");
                rfidManager.startReading();
            } else {
                Toast.makeText(this, "No se pudo dar de baja el activo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        rfidManager.stopReading();
    }
}
