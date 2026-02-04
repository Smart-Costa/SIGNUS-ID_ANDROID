package com.example.diverscan.activeid.Locate_Assets;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;

public class Localizacion_activo extends AppCompatActivity implements ResponseHandlerInterface {

    private EditText epcView;
    private View mlocalizacionView;
    private TagWriter rfidHandler;

    Button btn_iniciar;
    Button btn_detener;

    private String EPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion_activo);

        controles();
        eventos();

        mlocalizacionView.post(Load);
        
        initRFID();
    }
    
    private void initRFID() {
        try {
            rfidHandler = TagWriter.getInstance();
            if (!rfidHandler.isInitialized()) {
                rfidHandler.onCreate(this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error RFID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.setResponseHandler(this);
            rfidHandler.updateContext(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.StopLocateTag();
        }
    }

    public void controles() {

        mlocalizacionView = findViewById(R.id.LocalizacionForm);

        epcView = (EditText) findViewById(R.id.txt_numero_epc);

        btn_iniciar = (Button) findViewById(R.id.btn_iniciar);
        btn_detener = (Button) findViewById(R.id.btn_detener);

    }

    public void eventos() {

        btn_iniciar.setOnClickListener(OnClickListenerIniciar);
        btn_detener.setOnClickListener(OnClickListenerDetener);
    }

    Runnable Load = new Runnable(){
        @Override
        public void run() {

            RecibirInfo();
        }
    };

    public void RecibirInfo () {
        if (getIntent().getExtras() != null) {
            EPC = getIntent().getExtras().getString("Dato_Tag");
            epcView.setText(EPC);
            epcView.setEnabled(false);
        }
    }

    private Button.OnClickListener OnClickListenerIniciar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (EPC != null && !EPC.isEmpty() && rfidHandler != null) {
                rfidHandler.LocateTag(EPC);
            }
        }};

    private Button.OnClickListener OnClickListenerDetener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (rfidHandler != null) {
                rfidHandler.StopLocateTag();
            }
        }};

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        // Handle distance data if needed in future
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
             if (EPC != null && !EPC.isEmpty() && rfidHandler != null) {
                rfidHandler.LocateTag(EPC);
            }
        } else {
            if (rfidHandler != null) {
                rfidHandler.StopLocateTag();
            }
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        // Optional: Update UI with status
    }
}
