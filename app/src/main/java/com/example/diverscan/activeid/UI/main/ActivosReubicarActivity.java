package com.example.diverscan.activeid.UI.main;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ActivoReubicacionDto;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.example.diverscan.activeid.data.remote.response.NovedadesResponse;

import java.util.ArrayList;
import java.util.List;

public class ActivosReubicarActivity extends AppCompatActivity {

    private TextView tvStatus, tvTomaId;
    private ListView lvActivos;
    private Button btnRefresh;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activos_reubicar);

        tvStatus = findViewById(R.id.tv_status);
        tvTomaId = findViewById(R.id.tv_toma_id);
        lvActivos = findViewById(R.id.lv_activos);
        btnRefresh = findViewById(R.id.btn_refresh);

        sessionManager = new SessionManager(this);

        btnRefresh.setOnClickListener(v -> cargarNovedades());

        cargarNovedades();
    }

    private void cargarNovedades() {
        tvStatus.setText("Consultando...");
        String username = sessionManager.getUsername();

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getInstance(this).get("ConsolidacionApi/ConsultarNovedades?username=" + username,
                NovedadesResponse.class,
                new ApiCallback<NovedadesResponse>() {
                    @Override
                    public void onComplete(ApiResponse<NovedadesResponse> response) {
                        if (response.success && response.data != null && response.data.ok) {
                            actualizarUI(response.data.data);
                        } else {
                            tvStatus.setText("Error al consultar");
                            Toast.makeText(ActivosReubicarActivity.this,
                                    "Error: " + (response.errorMessage != null ? response.errorMessage : "Desconocido"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void actualizarUI(NovedadesResponse.NovedadesData data) {
        if (data == null) return;

        if (data.tieneAsignacion) {
            tvStatus.setText("Asignación Activa\n(Tareas Pendientes: " + data.cantidadTareasPendientes + ")");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvTomaId.setText("Toma Física ID: " + data.tomaFisicaId);
        } else {
            tvStatus.setText("Sin Asignación");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvTomaId.setText("Toma Física: -");
        }

        List<String> displayList = new ArrayList<>();
        if (data.activosParaReubicar != null) {
            for (ActivoReubicacionDto dto : data.activosParaReubicar) {
                String item = "Placa: " + dto.placa + "\n" +
                              "Desc: " + dto.descripcion + "\n" +
                              "Serial: " + dto.serial + "\n" +
                              "Destino: " + (dto.ubicacionDestino != null ? dto.ubicacionDestino : "No especificado");
                displayList.add(item);
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        lvActivos.setAdapter(adapter);
        
        if (displayList.isEmpty()) {
            Toast.makeText(this, "No hay activos para reubicar", Toast.LENGTH_SHORT).show();
        }
    }
}
