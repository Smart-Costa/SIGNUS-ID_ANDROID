package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.diverscan.activeid.Utilities.PermissionUtils;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityDarBajaActivoDetailBinding;

public class DarBajaActivoDetailActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityDarBajaActivoDetailBinding binding;
    private TagWriter rfidHandler;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;
    private boolean isScanning = false;
    private long lastTriggerEventAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDarBajaActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PermissionUtils.requestPermissions(this);

        activoDAO = new ActivoDao(this);
        
        // Usar TagWriter (Singleton) en lugar de RfidManager para mantener conexión global
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        initEvents();
        // Estado inicial: RFID seleccionado por defecto
        setInputMode(3);
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
        isScanning = false;
    }

    // --- ResponseHandlerInterface Implementation ---

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        
        // Si no está en modo RFID, ignorar lecturas
        if (!binding.opcRFID.isChecked()) return;

        // Validar si vienen múltiples tags en la misma lectura
        if (tagData.length > 1) {
            runOnUiThread(() -> {
                rfidHandler.stopRead();
                isScanning = false;
                Toast.makeText(this, "Múltiples activos detectados. Por favor acerque solo uno.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        // Ejecutar en UI Thread porque TagWriter llama desde AsyncTask
        runOnUiThread(() -> {
            for (ReaderTag tag : tagData) {
                if (tag.getEpc() != null) {
                    procesarLecturaRFID(tag.getEpc());
                    // Procesar solo el primero válido de este lote
                    break; 
                }
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        // Si no está en modo RFID, ignorar gatillo
        if (!binding.opcRFID.isChecked()) return;
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastTriggerEventAt < 120L) {
            return;
        }
        lastTriggerEventAt = now;

        runOnUiThread(() -> {
            if (pressed) {
                if (!isScanning) {
                    rfidHandler.startRead();
                    isScanning = true;
                }
            } else {
                if (isScanning) {
                    rfidHandler.stopRead();
                    isScanning = false;
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(null);
        }
    }

    private void procesarLecturaRFID(String epc) {
        if (epc == null || epc.trim().isEmpty()) {
            return;
        }
        
        // Validación adicional de modo
        if (!binding.opcRFID.isChecked()) return;

        if (activoLeido != null) {
            // Verificar contra el EPC real, no contra el TAG_EPC que puede ser "EPC Asignado"
            String storedEpc = activoLeido.getEpc();
            if (storedEpc == null || !epc.equals(storedEpc)) {
                rfidHandler.stopRead();
                Toast.makeText(this, "Se detectaron múltiples TAGs. Acerque solo 1 y reintente.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // BUGFIX: query de BD movida a background thread para evitar ANR
        final String epcFinal = epc;
        new Thread(() -> {
            ActivoEntity activo = activoDAO.getActivoByEpc(epcFinal);
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing()) return;
                if (activo != null) {
                    mostrarNotificacionActivo(epcFinal, activo.getNumeroActivo(), activo.getDescripcionCorta(), true);
                    cargarDatosActivo(activo);
                    rfidHandler.stopRead();
                    isScanning = false;
                } else {
                    rfidHandler.stopRead();
                    isScanning = false;
                    mostrarNotificacionActivo(epcFinal, "Desconocido", "No encontrado", false);
                    Toast.makeText(this, "EPC no registrado en BD", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
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

        // Si cambiamos de modo y no es RFID, paramos lectura
        if (mode != 3 && rfidHandler != null) {
            rfidHandler.stopRead();
            isScanning = false;
        }
    }

    private void buscarActivoPorNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) return;
        // BUGFIX: Query de BD movida a background thread para evitar ANR
        final String numTrim = numero.trim();
        new Thread(() -> {
            ActivoEntity activo = activoDAO.getActivoById(numTrim);
            runOnUiThread(() -> {
                if (!isDestroyed() && !isFinishing()) {
                    if (activo != null) {
                        cargarDatosActivo(activo);
                    } else {
                        Toast.makeText(this, "Activo no encontrado con número: " + numTrim, Toast.LENGTH_SHORT).show();
                        limpiarCampos(false);
                    }
                }
            });
        }).start();
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
            binding.txtDescripcionRazon.setText("Lectura RFID exitosa");
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
        // Deshabilitar botón durante operación para evitar doble baja
        binding.btnGuardar.setEnabled(false);
        activoLeido.setEstadoActivo(Boolean.FALSE);
        activoLeido.setObservaciones(razon);
        final ActivoEntity activoParaBaja = activoLeido;
        // BUGFIX: `updateEstadoActivo()` es una operación de BD que debe ir en background
        new Thread(() -> {
            int updated = activoDAO.updateEstadoActivo(activoParaBaja);
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing()) return;
                binding.btnGuardar.setEnabled(true);
                if (updated > 0) {
                    Toast.makeText(this, "Activo dado de baja correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos(true);
                    if (binding.opcRFID.isChecked()) {
                        rfidHandler.startRead();
                    }
                } else {
                    Toast.makeText(this, "Error al dar de baja el activo", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void limpiarCampos(boolean full) {
        activoLeido = null;
        if (full) binding.txtNumeroActivo.setText("");
        binding.txtNumeroEtiqueta.setText("");
        binding.txtDescripcionCorta.setText("");
        binding.txtDescripcionRazon.setText("");
    }
}
