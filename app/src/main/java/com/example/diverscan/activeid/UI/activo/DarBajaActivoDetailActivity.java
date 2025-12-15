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

        binding.btnSimular.setOnClickListener(v ->
                procesarLecturaRFID("800474453240000000016145"));

        initEvents();
    }

    public void onConnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Lector conectado", Toast.LENGTH_SHORT).show()
        );
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
        ActivoEntity activo = activoDAO.getActivoByEpc(epc);
        if (activo != null) {
            binding.txtNumeroActivo.setText(activo.getNumeroActivo());
            binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
            binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
            binding.txtDescripcionRazon.setText("Lectura RFID exitosa");
        } else {
            Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
        }
    }

    private void initEvents() {
        binding.btnGuardar.setOnClickListener(view -> {
            Toast.makeText(this, "Activo dado de baja", Toast.LENGTH_SHORT).show();

            ActivoEntity activo = new ActivoEntity();
            activo.setNumeroActivo(activo.getNumeroActivo());
            activo.setEstadoActivo(Boolean.FALSE);

            activoDAO.updateEstadoActivo(activo);
        });
    }
}
