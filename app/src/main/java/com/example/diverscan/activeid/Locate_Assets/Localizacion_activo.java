package com.example.diverscan.activeid.Locate_Assets;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Utilities.PermissionUtils;

public class Localizacion_activo extends AppCompatActivity implements ResponseHandlerInterface {

    private EditText epcView;
    private View mlocalizacionView;

    Button btn_iniciar;
    Button btn_detener;

    private String targetEPC;
    private TagWriter rfidHandler;
    private ToneGenerator toneGenerator;
    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion_activo);

        PermissionUtils.requestPermissions(this);
        
        controles();
        eventos();
        initRFID();

        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        if (getIntent().getExtras() != null) {
            targetEPC = getIntent().getExtras().getString("Dato_Tag");
            if (targetEPC != null) {
                epcView.setText(targetEPC);
                epcView.setEnabled(false);
            }
        }
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

    public void controles() {
        mlocalizacionView = findViewById(R.id.LocalizacionForm);
        epcView = (EditText) findViewById(R.id.txt_numero_epc);
        btn_iniciar = (Button) findViewById(R.id.btn_iniciar);
        btn_detener = (Button) findViewById(R.id.btn_detener);
    }

    public void eventos() {
        btn_iniciar.setOnClickListener(v -> startSearch());
        btn_detener.setOnClickListener(v -> stopSearch());
    }

    private void startSearch() {
        if (targetEPC == null || targetEPC.isEmpty()) {
            targetEPC = epcView.getText().toString().trim();
        }

        if (targetEPC.isEmpty()) {
            Toast.makeText(this, "Ingrese un EPC para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rfidHandler != null && rfidHandler.isConnected()) {
            isSearching = true;
            rfidHandler.startRead();
            btn_iniciar.setEnabled(false);
            btn_detener.setEnabled(true);
            Toast.makeText(this, "Buscando activo...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lector no conectado", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSearch() {
        isSearching = false;
        if (rfidHandler != null) {
            rfidHandler.stopRead();
        }
        btn_iniciar.setEnabled(true);
        btn_detener.setEnabled(false);
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
        stopSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        if (!isSearching || tagData == null || targetEPC == null) return;

        for (ReaderTag tag : tagData) {
            if (tag.getEpc().equalsIgnoreCase(targetEPC)) {
                // Pitido de proximidad
                runOnUiThread(() -> {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    Toast.makeText(this, "¡Activo detectado!", Toast.LENGTH_SHORT).show();
                });
                break;
            }
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            startSearch();
        } else {
            stopSearch();
        }
    }

    @Override
    public void SetMessage(String Text) {
        runOnUiThread(() -> Toast.makeText(this, Text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public Context GetContext() {
        return this;
    }
}
