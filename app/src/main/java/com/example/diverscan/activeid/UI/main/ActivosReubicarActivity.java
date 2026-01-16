package com.example.diverscan.activeid.UI.main;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ActivoReubicacionDto;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.example.diverscan.activeid.data.local.entity.EmpresaEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionSecundariaEntity;
import com.example.diverscan.activeid.data.remote.response.NovedadesResponse;

import com.example.diverscan.activeid.data.remote.response.SobranteResueltoDto;
import com.example.diverscan.activeid.data.remote.response.TomaNovedadDto;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class ActivosReubicarActivity extends AppCompatActivity {

    private TextView tvStatus, tvTomaId;
    private LinearLayout llListUbicacionHh, llListCambioUbicacion;
    private Button btnRefresh;
    private SessionManager sessionManager;
    
    // State for status update
    private int mCurrentTaskCount = 0;
    private boolean mHasAssignment = false;
    private String mTomaHeaderTitle = "";
    
    // Cached lists for spinners
    private List<UbicacionEntity> ubicacionesList = new ArrayList<>();
    private List<UbicacionSecundariaEntity> ubicacionesSecList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activos_reubicar);

        tvStatus = findViewById(R.id.tv_status);
        // tvTomaId = findViewById(R.id.tv_toma_id); // Removed
        llListUbicacionHh = findViewById(R.id.ll_list_ubicacion_hh);
        llListCambioUbicacion = findViewById(R.id.ll_list_cambio_ubicacion);
        btnRefresh = findViewById(R.id.btn_refresh);

        sessionManager = new SessionManager(this);

        btnRefresh.setOnClickListener(v -> cargarNovedades());

        cargarNovedades();
        cargarDatosSpinners();
    }
    
    private void cargarDatosSpinners() {
        // Load Ubicaciones
        ApiClient.getInstance(this).get("GetUbicacionesHH", new TypeToken<List<UbicacionEntity>>(){}.getType(), new ApiCallback<List<UbicacionEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<UbicacionEntity>> response) {
                if (response.success && response.data != null) ubicacionesList = response.data;
            }
        });
        
        // Load Ubicaciones Secundarias
        ApiClient.getInstance(this).get("ActivosDetail/UbicacionesS", new TypeToken<List<UbicacionSecundariaEntity>>(){}.getType(), new ApiCallback<List<UbicacionSecundariaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<UbicacionSecundariaEntity>> response) {
                if (response.success && response.data != null) ubicacionesSecList = response.data;
            }
        });
    }

    // Helper class for Spinners
    public class ComboItem {
        private final String id;
        private final String nombre;

        public ComboItem(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre;
        }
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

        mHasAssignment = data.tieneAsignacion;
        mCurrentTaskCount = data.cantidadTareasPendientes;
        mTomaHeaderTitle = ""; // Reset
        
        updateStatusHeader();

        List<String> displayList = new ArrayList<>();
        
        llListUbicacionHh.removeAllViews();
        llListCambioUbicacion.removeAllViews();

        if (data.tomas != null && !data.tomas.isEmpty()) {
            boolean singleToma = data.tomas.size() == 1;

            for (TomaNovedadDto toma : data.tomas) {
                // Add Header for each Toma if multiple or just to separate
                String headerTitle = (toma.nombreToma != null && !toma.nombreToma.isEmpty()) 
                                     ? toma.nombreToma 
                                     : "Toma: " + toma.tomaFisicaId;
                
                if (singleToma) {
                    mTomaHeaderTitle = "\n" + headerTitle;
                    updateStatusHeader();
                }
                
                // Separate items by destination
                List<ActivoReubicacionDto> itemsWithoutDest = new ArrayList<>();
                List<ActivoReubicacionDto> itemsWithDest = new ArrayList<>();
                
                if (toma.activos != null) {
                    for (ActivoReubicacionDto dto : toma.activos) {
                        boolean hasDestination = dto.ubicacionDestino != null && !dto.ubicacionDestino.isEmpty() && !dto.ubicacionDestino.equals("Sin Destino");
                        if (hasDestination) {
                            itemsWithDest.add(dto);
                        } else {
                            itemsWithoutDest.add(dto);
                        }
                    }
                }

                // Add header and items to UBICACION HH only if there are items
                if (!itemsWithoutDest.isEmpty()) {
                    ViewGroup target = llListUbicacionHh;
                    if (!singleToma) {
                        LinearLayout section = new LinearLayout(this);
                        section.setOrientation(LinearLayout.VERTICAL);
                        addHeaderView(section, headerTitle);
                        llListUbicacionHh.addView(section);
                        target = section;
                    }
                    for (ActivoReubicacionDto dto : itemsWithoutDest) {
                        processActivoItem(dto, target);
                    }
                }

                // Add header and items to CAMBIO DE UBICACION only if there are items
                if (!itemsWithDest.isEmpty()) {
                    ViewGroup target = llListCambioUbicacion;
                    if (!singleToma) {
                        LinearLayout section = new LinearLayout(this);
                        section.setOrientation(LinearLayout.VERTICAL);
                        addHeaderView(section, headerTitle);
                        llListCambioUbicacion.addView(section);
                        target = section;
                    }
                    for (ActivoReubicacionDto dto : itemsWithDest) {
                        processActivoItem(dto, target);
                    }
                }
            }
        } else if (data.activosParaReubicar != null) {
            // Fallback to flat list if tomas is empty (legacy support)
            for (ActivoReubicacionDto dto : data.activosParaReubicar) {
                // Determine category based on destination
                boolean hasDestination = dto.ubicacionDestino != null && !dto.ubicacionDestino.isEmpty() && !dto.ubicacionDestino.equals("Sin Destino");
                ViewGroup target = hasDestination ? llListCambioUbicacion : llListUbicacionHh;
                processActivoItem(dto, target);
            }
        }
        
        if ((data.activosParaReubicar == null || data.activosParaReubicar.isEmpty()) && (data.tomas == null || data.tomas.isEmpty())) {
            Toast.makeText(this, "No hay activos para reubicar", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateStatusHeader() {
        if (mHasAssignment) {
            tvStatus.setText("Asignación Activa\n(Tareas Pendientes: " + mCurrentTaskCount + ")" + mTomaHeaderTitle);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("Sin Asignación");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    private void addHeaderView(LinearLayout container, String title) {
        TextView tvHeader = new TextView(this);
        tvHeader.setText(title);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeader.setPadding(16, 16, 16, 8);
        tvHeader.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        tvHeader.setTextColor(getResources().getColor(android.R.color.white));
        container.addView(tvHeader);
    }

    private void processActivoItem(ActivoReubicacionDto dto, ViewGroup container) {
        // Create item view dynamically
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        
        // Apply background first, then padding to avoid overwrite
        itemLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        itemLayout.setPadding(40, 40, 40, 40); // Increased padding
        
        // Add margin to the card
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 32); // Increased margins
        itemLayout.setLayoutParams(params);

        TextView tvContent = new TextView(this);
        String content = "Placa: " + dto.placa + "\n" +
                         "Desc: " + dto.descripcion + "\n" +
                         "Serial: " + dto.serial + "\n" +
                         "Actual: " + (dto.ubicacionActual != null ? dto.ubicacionActual : "Desconocida");
        
        // Determine if has destination just for display logic or logic consistency
        boolean hasDestination = dto.ubicacionDestino != null && !dto.ubicacionDestino.isEmpty() && !dto.ubicacionDestino.equals("Sin Destino");
        
        if (hasDestination) {
            content += "\nDestino: " + dto.ubicacionDestino;
        }
        
        tvContent.setText(content);
        itemLayout.addView(tvContent);

        // Add button to resolve
        Button btnResolve = new Button(this);
        btnResolve.setText("Confirmar Reubicación");
        btnResolve.setOnClickListener(v -> resolverReubicacion(dto, itemLayout, hasDestination));
        itemLayout.addView(btnResolve);

        // Add to appropriate container
        container.addView(itemLayout);
    }

    private void resolverReubicacion(ActivoReubicacionDto dto, LinearLayout itemLayout, boolean hasDestination) {
        if (!hasDestination) {
            // Flow: Ubicacion HH -> Must specify destination
            mostrarDialogoSeleccionUbicacion(dto, itemLayout);
        } else {
            // Flow: Cambio de Ubicacion -> Destination already set in task, just confirm
            enviarResolucion(dto, itemLayout, true, null, null, null, null, null);
        }
    }
    
    private void mostrarDialogoSeleccionUbicacion(ActivoReubicacionDto dto, LinearLayout itemLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Nueva Ubicación");
        
        // Inflate custom layout for dialog
        // Since we don't have a layout XML handy, we'll create one programmatically or use a simple one if available.
        // Creating programmatically to avoid file creation if possible, but a layout is better.
        // Let's create a linear layout with spinners.
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        
        final Spinner spUbicacionA = new Spinner(this);
        final Spinner spUbicacionB = new Spinner(this);
        final Spinner spUbicacionC = new Spinner(this);
        final Spinner spUbicacionD = new Spinner(this);
        final Spinner spSecundaria = new Spinner(this);
        
        // Helper to add label and spinner
        addSpinnerWithLabel(layout, "Ubicación A:", spUbicacionA);
        addSpinnerWithLabel(layout, "Ubicación B:", spUbicacionB);
        addSpinnerWithLabel(layout, "Ubicación C:", spUbicacionC);
        addSpinnerWithLabel(layout, "Ubicación D:", spUbicacionD);
        addSpinnerWithLabel(layout, "Ubicación Secundaria:", spSecundaria);
        
        builder.setView(layout);
        
        // Populate Spinners logic
        // Ubicacion A
        List<ComboItem> ubicacionACombo = new ArrayList<>();
        Set<String> repetidosA = new HashSet<>();
        for (UbicacionEntity item : ubicacionesList) {
            if (item.getUbicacionA() != null && repetidosA.add(item.getUbicacionA())) {
                ubicacionACombo.add(new ComboItem(item.getASysId(), item.getUbicacionA()));
            }
        }
        ArrayAdapter<ComboItem> ubicacionAAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ubicacionACombo);
        ubicacionAAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUbicacionA.setAdapter(ubicacionAAdapter);
        
        // Setup listeners for cascading
        setupCascadingSpinners(spUbicacionA, spUbicacionB, spUbicacionC, spUbicacionD);
        
        List<ComboItem> secCombo = new ArrayList<>();
        // Agregar opción nula por defecto
        secCombo.add(new ComboItem(null, "Ninguna"));
        for (UbicacionSecundariaEntity s : ubicacionesSecList) {
            secCombo.add(new ComboItem(s.getUSSysId(), s.getUbicacionS()));
        }
        ArrayAdapter<ComboItem> secAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, secCombo);
        secAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecundaria.setAdapter(secAdapter);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            // Get selected IDs
            String ua = getSelectedId(spUbicacionA);
            String ubId = getSelectedId(spUbicacionB);
            String ucId = getSelectedId(spUbicacionC);
            String udId = getSelectedId(spUbicacionD);
            String secId = getSelectedId(spSecundaria);
            
            enviarResolucion(dto, itemLayout, false, ua, ubId, ucId, udId, secId);
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    private String getSelectedId(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        if (item instanceof ComboItem) {
            return ((ComboItem) item).getId();
        }
        return null;
    }
    
    private void addSpinnerWithLabel(LinearLayout parent, String label, Spinner spinner) {
        TextView tv = new TextView(this);
        tv.setText(label);
        parent.addView(tv);
        parent.addView(spinner);
    }
    
    private void setupCascadingSpinners(Spinner spA, Spinner spB, Spinner spC, Spinner spD) {
        spA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ComboItem itemA = (ComboItem) parent.getItemAtPosition(position);
                cargarUbicacionesB(itemA.getId(), spB);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spB.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ComboItem itemB = (ComboItem) parent.getItemAtPosition(position);
                cargarUbicacionesC(itemB.getId(), spC);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ComboItem itemC = (ComboItem) parent.getItemAtPosition(position);
                cargarUbicacionesD(itemC.getId(), spD);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void cargarUbicacionesB(String idA, Spinner sp) {
        List<ComboItem> lista = new ArrayList<>();
        lista.add(new ComboItem(null, "Ninguna"));
        
        if (idA != null) {
            Set<String> repetidos = new HashSet<>();
            for (UbicacionEntity item : ubicacionesList) {
                if (item.getASysId() != null && item.getASysId().equals(idA)) {
                    if (item.getBSysId() != null && repetidos.add(item.getBSysId())) {
                        lista.add(new ComboItem(item.getBSysId(), item.getUbicacionB()));
                    }
                }
            }
        }
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista));
    }

    private void cargarUbicacionesC(String idB, Spinner sp) {
        List<ComboItem> lista = new ArrayList<>();
        lista.add(new ComboItem(null, "Ninguna"));
        
        if (idB != null) {
            Set<String> repetidos = new HashSet<>();
            for (UbicacionEntity item : ubicacionesList) {
                if (item.getBSysId() != null && item.getBSysId().equals(idB)) {
                    if (item.getCSysId() != null && repetidos.add(item.getCSysId())) {
                        lista.add(new ComboItem(item.getCSysId(), item.getUbicacionC()));
                    }
                }
            }
        }
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista));
    }

    private void cargarUbicacionesD(String idC, Spinner sp) {
        List<ComboItem> lista = new ArrayList<>();
        lista.add(new ComboItem(null, "Ninguna"));
        
        if (idC != null) {
            Set<String> repetidos = new HashSet<>();
            for (UbicacionEntity item : ubicacionesList) {
                if (item.getCSysId() != null && item.getCSysId().equals(idC)) {
                    if (item.getDSysId() != null && repetidos.add(item.getDSysId())) {
                        lista.add(new ComboItem(item.getDSysId(), item.getUbicacionD()));
                    }
                }
            }
        }
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lista));
    }

    private void enviarResolucion(ActivoReubicacionDto dto, LinearLayout itemLayout, boolean hasDestination, 
                                  String ua, String ub, String uc, String ud, String us) {
        SobranteResueltoDto request = new SobranteResueltoDto();
        request.tomaFisicaId = dto.tomaFisicaId;
        request.activoId = dto.idActivo;
        
        if (!hasDestination) {
            request.ubicacionA = ua;
            request.ubicacionB = ub;
            request.ubicacionC = uc;
            request.ubicacionD = ud;
            request.ubicacionSecundaria = us;
        }
        
        ApiClient.getInstance(this).post("ConsolidacionApi/ResolverReubicacion", request, JsonObject.class, new ApiCallback<JsonObject>() {
            @Override
            public void onComplete(ApiResponse<JsonObject> response) {
                if (response.success) {
                    Toast.makeText(ActivosReubicarActivity.this, "Reubicación confirmada", Toast.LENGTH_SHORT).show();
                    
                    if (mCurrentTaskCount > 0) {
                        mCurrentTaskCount--;
                        updateStatusHeader();
                    }

                    // Remove from UI
                    if (itemLayout.getParent() != null) {
                        ViewGroup parent = (ViewGroup) itemLayout.getParent();
                        parent.removeView(itemLayout);
                        
                        // Check if we need to clean up empty section (Header only)
                        // If parent is not the main container, it's a section wrapper
                        if (parent != llListUbicacionHh && parent != llListCambioUbicacion) {
                            // It's a section wrapper. 
                            // If only header remains (1 child), remove the wrapper itself.
                            if (parent.getChildCount() == 1) {
                                ViewGroup grandParent = (ViewGroup) parent.getParent();
                                if (grandParent != null) {
                                    grandParent.removeView(parent);
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(ActivosReubicarActivity.this, "Error: " + response.errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}