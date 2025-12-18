package com.example.diverscan.activeid.UI.validation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.zebra.rfid.api3.TagData;
import com.example.diverscan.activeid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.method.ScrollingMovementMethod;

public class ConnectionValidationActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private TextView tvStatus;
    private TextView tvDevice;
    private TextView tvLog;
    private Button btnReconnect;
    private TagWriter rfidHandler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_validation);

        tvStatus = findViewById(R.id.tv_connection_status);
        tvDevice = findViewById(R.id.tv_device_info);
        tvLog = findViewById(R.id.tv_log);
        tvLog.setMovementMethod(new ScrollingMovementMethod()); // Habilitar scroll

        btnReconnect = findViewById(R.id.btn_reconnect);

        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        } else {
            rfidHandler.setResponseHandler(this);
        }

        btnReconnect.setOnClickListener(v -> {
            log("Reiniciando conexión...");
            // Usamos InitSDK para forzar una nueva búsqueda de lectores si es necesario
            rfidHandler.InitSDK();
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rfidHandler.setResponseHandler(this);
        updateUI();
    }

    private void updateUI() {
        boolean connected = rfidHandler.isConnected();
        tvStatus.setText(connected ? "CONECTADO" : "DESCONECTADO");
        tvStatus.setTextColor(connected ? 0xFF00AA00 : 0xFFFF0000); // Green / Red

        if (connected) {
            String name = rfidHandler.getReaderName();
            String model = rfidHandler.getReaderModel();
            tvDevice.setText("Nombre: " + name + "\nModelo: " + model);
        } else {
            tvDevice.setText("Dispositivo: --");
        }
    }

    private void log(String msg) {
        String timestamp = timeFormat.format(new Date());
        runOnUiThread(() -> {
            tvLog.append("\n[" + timestamp + "] " + msg);
            // Auto-scroll al final
            final int scrollAmount = tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight();
            if (scrollAmount > 0)
                tvLog.scrollTo(0, scrollAmount);
        });
    }

    // ResponseHandlerInterface implementation
    @Override
    public void handleTagdata(TagData[] tagData) {
        // Not used here
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        log("Gatillo: " + (pressed ? "Presionado" : "Liberado"));
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        log(Text);
    }
}
