package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.zebra.rfid.api3.TagData;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityDarBajaActivoDetailBinding;

public class DarBajaActivoDetailActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityDarBajaActivoDetailBinding binding;
    private TagWriter rfidHandler;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDarBajaActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        
        // Usar TagWriter (Singleton) en lugar de RfidManager para mantener conexión global
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        binding.opcRFID.setOnClickListener(v -> {
            if (!rfidHandler.isConnected()) {
                rfidHandler.InitSDK();
            } else {
                Toast.makeText(this, "Lector ya conectado", Toast.LENGTH_SHORT).show();
                rfidHandler.startRead();
            }
        });

        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
            // Si ya está conectado, asegurar que el gatillo esté configurado o listo
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.stopRead();
            // No desconectamos para mantener la sesión
        }
    }

    // --- ResponseHandlerInterface Implementation ---

    @Override
    public void handleTagdata(TagData[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        
        // Validar si vienen múltiples tags en la misma lectura
        if (tagData.length > 1) {
            runOnUiThread(() -> {
                rfidHandler.stopRead();
                Toast.makeText(this, "Múltiples activos detectados. Por favor acerque solo uno.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        // Ejecutar en UI Thread porque TagWriter llama desde AsyncTask
        runOnUiThread(() -> {
            for (TagData tag : tagData) {
                if (tag.getTagID() != null) {
                    procesarLecturaRFID(tag.getTagID());
                    // Procesar solo el primero válido de este lote
                    break; 
                }
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            if (pressed) {
                rfidHandler.startRead();
            } else {
                rfidHandler.stopRead();
            }
        });
    }

    @Override
    public void SetMessage(String message) {
        runOnUiThread(() -> 
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public Context GetContext() {
        return this;
    }

    // --- Fin Interface ---

    private void procesarLecturaRFID(String epc) {
        if (epc == null || epc.trim().isEmpty()) {
            return;
        }
        if (activoLeido != null) {
            // Verificar contra el EPC real, no contra el TAG_EPC que puede ser "EPC Asignado"
            String storedEpc = activoLeido.getEpc();
            if (storedEpc == null || !epc.equals(storedEpc)) {
                rfidHandler.stopRead();
                Toast.makeText(this, "Se detectaron múltiples TAGs. Acerque solo 1 y reintente.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ActivoEntity activo = activoDAO.getActivoByEpc(epc);
        if (activo != null) {
            mostrarNotificacionActivo(epc, activo.getNumeroActivo(), activo.getDescripcionCorta(), true);
            if (activo.getEstadoActivo() != null && !activo.getEstadoActivo()) {
                Toast.makeText(this, "El activo ya se encuentra dado de baja", Toast.LENGTH_LONG).show();
                rfidHandler.stopRead();
                return;
            }

            activoLeido = activo;
            binding.txtNumeroActivo.setText(activo.getNumeroActivo());
            binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
            binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
            binding.txtDescripcionRazon.setText("Lectura RFID exitosa");
            rfidHandler.stopRead();
        } else {
            rfidHandler.stopRead();
            mostrarNotificacionActivo(epc, "Desconocido", "No encontrado", false);
            Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarNotificacionActivo(String epc, String numeroActivo, String descripcion, boolean encontrado) {
        String mensaje = "EPC: " + epc + "\n" +
                "Número: " + numeroActivo + "\n" +
                "Descripción: " + descripcion + "\n" +
                "Estado en BD: " + (encontrado ? "ENCONTRADO" : "NO REGISTRADO");

        new AlertDialog.Builder(this)
                .setTitle("Activo Leído")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
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
                rfidHandler.startRead();
            } else {
                Toast.makeText(this, "No se pudo dar de baja el activo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
