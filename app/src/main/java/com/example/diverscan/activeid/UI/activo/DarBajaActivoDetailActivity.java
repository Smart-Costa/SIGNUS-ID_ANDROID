package com.example.diverscan.activeid.UI.activo;

import android.content.Context;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.os.Build;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.os.Build;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.databinding.ActivityDarBajaActivoDetailBinding;

public class DarBajaActivoDetailActivity extends AppCompatActivity implements ResponseHandlerInterface {
    private ActivityDarBajaActivoDetailBinding binding;
    private TagWriter rfidHandler;
    private ActivoDao activoDAO;
    private ActivoEntity activoLeido;

    // Permission handling
    private boolean isRequestingPermissions = false;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDarBajaActivoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activoDAO = new ActivoDao(this);
        
        // Usar TagWriter (Singleton) en lugar de RfidManager para mantener conexión global
        if (checkPermissions()) {
            rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            } else {
                rfidHandler.setResponseHandler(this);
            }
        }

        initEvents();
        // Estado inicial: RFID seleccionado por defecto
        setInputMode(3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        isRequestingPermissions = false; // Reset flag to allow retries
        
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
    
    private boolean checkPermissions() {
        if (isRequestingPermissions) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean missingConnect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
            boolean missingScan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED;

            if (missingConnect || missingScan) {
                isRequestingPermissions = true;
                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                }, PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                 isRequestingPermissions = true;
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                 return false;
             }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            isRequestingPermissions = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 // Retry init
                 try {
                    rfidHandler = TagWriter.getInstance();
                    if (!rfidHandler.isInitialized()) {
                        rfidHandler.onCreate(this);
                    } else {
                        rfidHandler.setResponseHandler(this);
                    }
                 } catch (Exception e) {
                     Log.e("DarBaja", "Error initializing RFID after permission grant", e);
                 }
            } else {
                 Toast.makeText(this, "Permisos necesarios para usar el lector RFID", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (tagData == null || tagData.length == 0) return;
        
        // Si no está en modo RFID, ignorar lecturas
        if (!binding.opcRFID.isChecked()) return;

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

        ActivoEntity activo = activoDAO.getActivoByEpc(epc);
        if (activo != null) {
            mostrarNotificacionActivo(epc, activo.getNumeroActivo(), activo.getDescripcionCorta(), true);
            cargarDatosActivo(activo);
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
            binding.txtDescripcionRazon.setText("Lectura RFID exitosa");
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
            if (binding.opcRFID.isChecked()) {
                rfidHandler.startRead();
            }
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
}
