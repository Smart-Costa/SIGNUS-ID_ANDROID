package com.example.diverscan.activeid.UI.tomasfisicas;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.zebra.rfid.api3.TagData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class NuevaTomaActivity extends AppCompatActivity implements ResponseHandlerInterface {

    private static final String TAG = "NuevaTomaActivity";
    private String tomaFisicaId;
    private String idToma;
    private String numeroToma;

    private TextView txtStatus, txtCount;
    private Button btnToggleScan, btnSave;
    private ListView listScannedTags;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> scannedTagsList;
    private Set<String> uniqueTags;

    private TagWriter rfidHandler;
    private boolean isScanning = false;

    private TomaFisicaTomasDao tomasDao;
    private TomaFisicaDetallesDao detallesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_toma);

        if (getIntent().hasExtra("tomaFisicaId")) {
            tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        } else {
            Toast.makeText(this, "Error: No se recibió ID de Toma Física", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize DAO
        tomasDao = new TomaFisicaTomasDao(this);
        detallesDao = new TomaFisicaDetallesDao(this);

        // Generate new ID for this Take
        idToma = UUID.randomUUID().toString();
        // Generate a simple number (in real app this might need to be queried)
        numeroToma = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        initUI();
        initRFID();
    }

    private void initUI() {
        txtStatus = findViewById(R.id.txtStatus);
        txtCount = findViewById(R.id.txtCount);
        btnToggleScan = findViewById(R.id.btnToggleScan);
        btnSave = findViewById(R.id.btnSave);
        listScannedTags = findViewById(R.id.listScannedTags);

        scannedTagsList = new ArrayList<>();
        uniqueTags = new HashSet<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedTagsList);
        listScannedTags.setAdapter(adapter);

        btnToggleScan.setOnClickListener(v -> toggleScan());
        btnSave.setOnClickListener(v -> saveTake());
    }

    private void initRFID() {
        rfidHandler = TagWriter.getInstance();
        if (!rfidHandler.isInitialized()) {
            rfidHandler.onCreate(this);
        }
        rfidHandler.setResponseHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rfidHandler != null) {
            rfidHandler.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rfidHandler != null) {
            rfidHandler.onPause();
        }
    }

    private void toggleScan() {
        if (isScanning) {
            stopScan();
        } else {
            startScan();
        }
    }

    private void startScan() {
        if (rfidHandler != null) {
            rfidHandler.performInventory();
            isScanning = true;
            updateUIState();
        }
    }

    private void stopScan() {
        if (rfidHandler != null) {
            rfidHandler.stopInventory();
            isScanning = false;
            updateUIState();
        }
    }

    private void updateUIState() {
        runOnUiThread(() -> {
            if (isScanning) {
                btnToggleScan.setText("Detener Escaneo");
                txtStatus.setText("Estado: Escaneando...");
                btnSave.setEnabled(false);
            } else {
                btnToggleScan.setText("Iniciar Escaneo");
                txtStatus.setText("Estado: Detenido");
                btnSave.setEnabled(!uniqueTags.isEmpty());
            }
        });
    }

    private void saveTake() {
        // Save header
        TomaFisicaTomasEntity header = new TomaFisicaTomasEntity();
        header.setTomaFisicaId(tomaFisicaId);
        header.setIdToma(idToma);
        header.setNumeroToma(numeroToma);
        header.setTotalLecturas(String.valueOf(uniqueTags.size()));
        
        // I added insert method to DAO previously? No, I need to check if I did.
        // I planned to add it. Let's check TomaFisicaTomasDao again.
        // If not, I will add it here (or rather, assume I added it or use a raw query if needed, but better to add it to DAO).
        
        // Save details
        List<TomaFisicaDetallesEntity> detalles = new ArrayList<>();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        for (String epc : uniqueTags) {
            TomaFisicaDetallesEntity detail = new TomaFisicaDetallesEntity();
            detail.setIdTakeDetail(UUID.randomUUID().toString());
            detail.setIdToma(idToma);
            detail.setNumeroToma(numeroToma);
            detail.setEpc(epc);
            detail.setDateRead(now);
            // Other fields can be null or empty for now
            detalles.add(detail);
        }

        new SaveTask(header, detalles).execute();
    }

    private class SaveTask extends AsyncTask<Void, Void, Boolean> {
        private TomaFisicaTomasEntity header;
        private List<TomaFisicaDetallesEntity> detalles;

        public SaveTask(TomaFisicaTomasEntity header, List<TomaFisicaDetallesEntity> detalles) {
            this.header = header;
            this.detalles = detalles;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // I need to ensure insert exists. 
                // Since I cannot edit the DAO from inside here, I will assume I edit the DAO file next.
                // For now I will call a method I will create in DAO.
                tomasDao.insert(header);
                detallesDao.syncDetalle(detalles); // syncDetalle uses insertWithOnConflict, so it works for new items too
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error saving take", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(NuevaTomaActivity.this, "Toma guardada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(NuevaTomaActivity.this, "Error al guardar la toma", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ResponseHandlerInterface methods

    @Override
    public void handleTagdata(TagData[] tagData) {
        if (tagData == null) return;

        boolean newTagsFound = false;
        for (TagData tag : tagData) {
            String epc = tag.getTagID();
            if (epc != null && !uniqueTags.contains(epc)) {
                uniqueTags.add(epc);
                scannedTagsList.add(epc);
                newTagsFound = true;
            }
        }

        if (newTagsFound) {
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                txtCount.setText("Leídos: " + uniqueTags.size());
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            startScan();
        } else {
            stopScan();
        }
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        runOnUiThread(() -> Toast.makeText(this, Text, Toast.LENGTH_SHORT).show());
    }
}
