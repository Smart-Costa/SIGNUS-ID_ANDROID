package com.example.diverscan.activeid.UI.activo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.Scanner.ScannerFactory;
import com.example.diverscan.activeid.Scanner.ScannerService;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityDarBajaActivoDetailBinding;

public class DarBajaActivoDetailActivity extends AppCompatActivity {
    private ActivityDarBajaActivoDetailBinding binding;
    private ScannerService scannerService;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDarBajaActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        
        // Inicializar Scanner Service
        scannerService = ScannerFactory.createScanner(this);
        scannerService.setListener(new ScannerService.ScannerListener() {
            @Override
            public void onScanResult(String data, String type) {
                runOnUiThread(() -> {
                    // Si el modo seleccionado no es Scanner ni RFID (ej. Teclado), ignorar o preguntar
                    if (binding.opcTeclado.isChecked()) return;

                    procesarLectura(data);
                    Toast.makeText(DarBajaActivoDetailActivity.this, "Leído: " + data, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onStatusMessage(String message) {
                // Opcional: mostrar logs o estado en UI
            }
        });
        scannerService.connect();

        initEvents();
        // Estado inicial: RFID seleccionado por defecto
        setInputMode(3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerService != null) {
            scannerService.disconnect();
        }
    }

    private void initEvents() {
        // Checkbox listeners para exclusividad
        binding.opcTeclado.setOnClickListener(v -> setInputMode(1));
        binding.opcScanner.setOnClickListener(v -> setInputMode(2));
        binding.opcRFID.setOnClickListener(v -> setInputMode(3));

        // Listener para búsqueda manual al perder foco o dar Enter (IME Action)
        binding.txtNumeroActivo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                buscarActivoPorNumero(binding.txtNumeroActivo.getText().toString());
                return true;
            }
            return false;
        });

        binding.txtNumeroActivo.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && binding.opcTeclado.isChecked()) {
                buscarActivoPorNumero(binding.txtNumeroActivo.getText().toString());
            }
        });

        binding.btnGuardar.setOnClickListener(view -> confirmarBaja());
    }

    private void setInputMode(int mode) {
        // 1: Teclado, 2: Scanner, 3: RFID
        binding.opcTeclado.setChecked(mode == 1);
        binding.opcScanner.setChecked(mode == 2);
        binding.opcRFID.setChecked(mode == 3);

        boolean isKeyboard = (mode == 1);
        binding.txtNumeroActivo.setEnabled(isKeyboard);
        binding.txtNumeroActivo.setFocusable(isKeyboard);
        binding.txtNumeroActivo.setFocusableInTouchMode(isKeyboard);
        
        if (!isKeyboard) {
            binding.txtNumeroActivo.clearFocus();
            // Ocultar teclado
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.txtNumeroActivo.getWindowToken(), 0);
        }
    }

    private void procesarLectura(String data) {
        if (data == null || data.trim().isEmpty()) return;

        // Intentar buscar por EPC primero (RFID) o por Código (Scanner)
        // Asumimos que 'data' puede ser cualquiera.
        
        // Estrategia: Buscar por EPC. Si no encuentra, buscar por ID/Número Activo.
        ActivoEntity activo = activoDAO.getActivoByEpc(data);
        if (activo == null) {
            activo = activoDAO.getActivoById(data);
        }

        if (activo != null) {
            cargarDatosActivo(activo);
        } else {
            mostrarNotificacionActivo(data, "Desconocido", "No encontrado", false);
        }
    }

    private void buscarActivoPorNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) return;
        
        ActivoEntity activo = activoDAO.getActivoById(numero.trim());
        if (activo != null) {
            cargarDatosActivo(activo);
        } else {
            Toast.makeText(this, "Activo no encontrado con número: " + numero, Toast.LENGTH_SHORT).show();
            limpiarCampos(false);
        }
    }

    private void cargarDatosActivo(ActivoEntity activo) {
        if (activo.getEstadoActivo() != null && !activo.getEstadoActivo()) {
             Toast.makeText(this, "El activo ya se encuentra dado de baja", Toast.LENGTH_LONG).show();
        }
        
        activoLeido = activo;
        binding.txtNumeroActivo.setText(activo.getNumeroActivo());
        binding.txtNumeroEtiqueta.setText(activo.getNumeroEtiqueta());
        binding.txtDescripcionCorta.setText(activo.getDescripcionCorta());
        binding.txtDescripcionRazon.setText(""); 
        
        if (binding.opcRFID.isChecked()) {
            binding.txtDescripcionRazon.setText("Lectura Exitosa");
        } else if (binding.opcTeclado.isChecked()) {
            binding.txtDescripcionRazon.requestFocus();
        }
    }

    private void confirmarBaja() {
        if (activoLeido == null || activoLeido.getIdActivo() == null) {
            Toast.makeText(this, "Primero debe identificar un activo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String razon = binding.txtDescripcionRazon.getText().toString();
        if (razon.trim().isEmpty()) {
            binding.txtDescripcionRazon.setError("Debe ingresar un motivo para la baja");
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Confirmar Baja")
            .setMessage("¿Está seguro de dar de baja el activo " + activoLeido.getNumeroActivo() + "?")
            .setPositiveButton("Sí", (dialog, which) -> ejecutarBaja(razon))
            .setNegativeButton("No", null)
            .show();
    }

    private void ejecutarBaja(String razon) {
        activoLeido.setEstadoActivo(Boolean.FALSE);
        activoLeido.setObservaciones(razon);

        int updated = activoDAO.updateEstadoActivo(activoLeido);
        if (updated > 0) {
            Toast.makeText(this, "Activo dado de baja correctamente", Toast.LENGTH_SHORT).show();
            limpiarCampos(true);
        } else {
            Toast.makeText(this, "Error al dar de baja el activo", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos(boolean full) {
        activoLeido = null;
        if (full) binding.txtNumeroActivo.setText("");
        binding.txtNumeroEtiqueta.setText("");
        binding.txtDescripcionCorta.setText("");
        binding.txtDescripcionRazon.setText("");
    }

    private void mostrarNotificacionActivo(String epc, String numeroActivo, String descripcion, boolean encontrado) {
        String mensaje = "Datos: " + epc + "\n" +
                "Número: " + numeroActivo + "\n" +
                "Descripción: " + descripcion + "\n" +
                "Estado en BD: " + (encontrado ? "ENCONTRADO" : "NO REGISTRADO");

        new AlertDialog.Builder(this)
                .setTitle("Activo Leído")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }
}
