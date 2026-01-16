package com.example.diverscan.activeid.UI.tomasfisicas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collections;
import java.util.UUID;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.FotoActivo.EFotoActivo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDao;
import com.example.diverscan.activeid.data.local.dao.UbicacionDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.example.diverscan.activeid.sqlite.FotoDBHelper;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistroConteosActivity extends AppCompatActivity {

    private Button tabResumen;
    private Button tabActivos;
    private View tabsView;
    private View viewResumen;
    private View viewActivos;
    private View layoutActivosResumen;
    private View layoutActivosListado;
    private Button btnVolverListado;
    private RecyclerView recyclerActivos;
    private Categoria currentFilter = null;

    private TextView txtTotales;
    private TextView txtNoEncontrados;
    private TextView txtEncontrados;
    private TextView txtNoPertenecen;
    private TextView txtNoInventariados;
    private TextView txtTitulo;
    private TextView txtEscaneados;
    private TextView txtDeTotal;
    private CircularProgressIndicator kpiProgressCircle;
    private CircularProgressIndicator progressActivos;
    
    private LinearLayout containerTomasTabs;
    private Spinner spinnerActivos;
    private Button btnAgregarLectura;
    private List<ActivoEntity> listaActivosSpinner;
    private ActivoEntity activoSeleccionado;

    private String tomaFisicaId;
    private String idToma;
    private String numeroToma;
    private String estadoFiltro;
    private String tituloFiltro;

    private TomaFisicaDetallesDao detallesDao;
    private ActivoDao activoDao;
    private TomaFisicaTomasDao tomasDao;
    private TomaFisicaDao tomaFisicaDao;
    private UbicacionDao ubicacionDao;

    // Filter Logic
    private Spinner spinnerUbicacionA;
    private Spinner spinnerUbicacionB;
    private Spinner spinnerUbicacionC;
    private Spinner spinnerUbicacionD;
    private Spinner spinnerUbicacionSecundaria;

    private TextView txtCountActivosFiltrados;
    private TextView txtCountUbicacionA;
    private TextView txtCountUbicacionB;
    private TextView txtCountUbicacionC;
    private TextView txtCountUbicacionD;
    private TextView txtCountUbicacionSecundaria;

    private List<SpinnerItem> listUbicacionA = new ArrayList<>();
    private List<SpinnerItem> listUbicacionB = new ArrayList<>();
    private List<SpinnerItem> listUbicacionC = new ArrayList<>();
    private List<SpinnerItem> listUbicacionD = new ArrayList<>();
    private List<String> listUbicacionSecundaria = new ArrayList<>();

    private String selectedUbicacionAId = null;
    private String selectedUbicacionBId = null;
    private String selectedUbicacionCId = null;
    private String selectedUbicacionDId = null;
    private String selectedUbicacionSecundariaId = null;

    private String baseUbicacionAId = null;
    private String baseUbicacionBId = null;
    private String baseUbicacionCId = null;
    private String baseUbicacionDId = null;
    
    private boolean isUpdatingSpinners = false;

    private static class SpinnerItem {
        String id;
        String text;
        public SpinnerItem(String id, String text) { this.id = id; this.text = text; }
        @Override public String toString() { return text; }
    }

    private final ActivosAdapter adapter = new ActivosAdapter();
    private final List<TomaFisicaDetallesEntity> filasRemotas = new ArrayList<>();
    private boolean remotoCargando;
    private String remotoIdToma;
    private Categoria remotoCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_conteos);

        tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        idToma = getIntent().getStringExtra("idToma");
        numeroToma = getIntent().getStringExtra("numeroToma");
        estadoFiltro = getIntent().getStringExtra("estadoFiltro");
        tituloFiltro = getIntent().getStringExtra("tituloFiltro");
        currentFilter = filtroCategoria();

        Log.d("RegistroConteos", "onCreate: tomaFisicaId=" + tomaFisicaId + ", idToma=" + idToma + ", estadoFiltro=" + estadoFiltro);

        detallesDao = new TomaFisicaDetallesDao(this);
        activoDao = new ActivoDao(this);
        tomasDao = new TomaFisicaTomasDao(this);
        tomaFisicaDao = new TomaFisicaDao(this);
        ubicacionDao = new UbicacionDao(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        txtTitulo = findViewById(R.id.txtTitulo);
        /*
        if (numeroToma != null && !numeroToma.trim().isEmpty()) {
            txtTitulo.setText("Resumen de conteos - Toma " + numeroToma.trim());
        }
        if (tituloFiltro != null && !tituloFiltro.trim().isEmpty()) {
            txtTitulo.setText(tituloFiltro.trim());
        }
        */

        tabResumen = findViewById(R.id.tabResumen);
        tabActivos = findViewById(R.id.tabActivos);
        tabsView = findViewById(R.id.tabsView);
        containerTomasTabs = findViewById(R.id.containerTomasTabs);
        viewResumen = findViewById(R.id.viewResumen);
        viewActivos = findViewById(R.id.viewActivos);
        layoutActivosResumen = findViewById(R.id.layoutActivosResumen);
        layoutActivosListado = findViewById(R.id.layoutActivosListado);
        btnVolverListado = findViewById(R.id.btnVolverListado);
        recyclerActivos = findViewById(R.id.recyclerActivos);

        if (btnVolverListado != null) {
            btnVolverListado.setOnClickListener(v -> {
                currentFilter = null;
                mostrarVistaActivos(false);
                cargarDatos();
            });
        }

        txtTotales = findViewById(R.id.txtTotales);
        txtNoEncontrados = findViewById(R.id.txtNoEncontrados);
        txtEncontrados = findViewById(R.id.txtEncontrados);
        txtNoPertenecen = findViewById(R.id.txtNoPertenecen);
        txtNoInventariados = findViewById(R.id.txtNoInventariados);
        txtEscaneados = findViewById(R.id.txtEscaneados);
        txtDeTotal = findViewById(R.id.txtDeTotal);
        kpiProgressCircle = findViewById(R.id.kpiProgressCircle);
        progressActivos = findViewById(R.id.progressActivos);

        recyclerActivos.setLayoutManager(new LinearLayoutManager(this));
        recyclerActivos.setAdapter(adapter);

        // Bind Filters UI
        spinnerUbicacionA = findViewById(R.id.spinnerUbicacionA);
        spinnerUbicacionB = findViewById(R.id.spinnerUbicacionB);
        spinnerUbicacionC = findViewById(R.id.spinnerUbicacionC);
        spinnerUbicacionD = findViewById(R.id.spinnerUbicacionD);
        spinnerUbicacionSecundaria = findViewById(R.id.spinnerUbicacionSecundaria);

        txtCountActivosFiltrados = findViewById(R.id.txtCountActivosFiltrados);
        txtCountUbicacionA = findViewById(R.id.txtCountUbicacionA);
        txtCountUbicacionB = findViewById(R.id.txtCountUbicacionB);
        txtCountUbicacionC = findViewById(R.id.txtCountUbicacionC);
        txtCountUbicacionD = findViewById(R.id.txtCountUbicacionD);
        txtCountUbicacionSecundaria = findViewById(R.id.txtCountUbicacionSecundaria);

        initFilters();

        spinnerActivos = findViewById(R.id.spinnerActivos);
        btnAgregarLectura = findViewById(R.id.btnAgregarLectura);
        if (btnAgregarLectura != null) {
            btnAgregarLectura.setOnClickListener(v -> agregarLecturaManual());
            // Hide for now as requested
            // if (lyAgregarLecturaManual != null) lyAgregarLecturaManual.setVisibility(View.GONE);
        }

        tabResumen.setOnClickListener(v -> setTab(true));
        tabActivos.setOnClickListener(v -> setTab(false));

        View btnVerNoEncontrados = findViewById(R.id.btnVerNoEncontrados);
        View btnVerEncontrados = findViewById(R.id.btnVerEncontrados);
        View btnVerNoPertenecen = findViewById(R.id.btnVerNoPertenecen);
        View btnVerNoInventariados = findViewById(R.id.btnVerNoInventariados);

        btnVerNoEncontrados.setOnClickListener(v -> abrirListado(Categoria.ROJO, "Faltantes"));
        btnVerEncontrados.setOnClickListener(v -> abrirListado(Categoria.VERDE, "Encontrados"));
        btnVerNoPertenecen.setOnClickListener(v -> abrirListado(Categoria.AMARILLO, "Sobrantes"));
        btnVerNoInventariados.setOnClickListener(v -> abrirListado(Categoria.BLANCO, "No inventariados"));

        if (filtroCategoria() != null) {
            if (tabsView != null) tabsView.setVisibility(View.GONE);
            setTab(false);
        } else {
            setTab(true);
        }
        
        validarBaseDeDatosLocal();
    }

    private void mostrarVistaActivos(boolean verListado) {
        if (layoutActivosResumen != null) layoutActivosResumen.setVisibility(verListado ? View.GONE : View.VISIBLE);
        if (layoutActivosListado != null) layoutActivosListado.setVisibility(verListado ? View.VISIBLE : View.GONE);
    }

    private void validarBaseDeDatosLocal() {
        new Thread(() -> {
            int count = activoDao.getActivosCount();
            if (count == 0) {
                runOnUiThread(() -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Base de datos vacía")
                        .setMessage("No se encontraron activos en la base de datos local. Por favor, sincronice para obtener los registros.")
                        .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
        cargarActivosSpinner();
        updateHeaderTitle();
        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
            tomasDao.fetchAndSyncFromApi(tomaFisicaId.trim(), () -> runOnUiThread(this::cargarDatos));
        }
        detallesDao.fetchAndSyncFromApi(idToma, () -> runOnUiThread(this::cargarDatos));
        updateTomasTabs();
    }

    private void updateHeaderTitle() {
        if (tomaFisicaDao == null || tomaFisicaId == null) return;
        new Thread(() -> {
            Log.d("RegistroConteos", "Updating header for tomaFisicaId: " + tomaFisicaId);
            TomaFisicaEntity parent = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
            if (parent != null) {
                String catId = parent.getCategoria();
                Log.d("RegistroConteos", "Found parent entity. Categoria ID: " + catId);
                
                String catName = "";
                if (catId != null && !catId.trim().isEmpty()) {
                    catName = tomaFisicaDao.getCategoryNameById(catId);
                    Log.d("RegistroConteos", "Fetched Category Name: " + catName);
                }
                
                if (catName == null || catName.isEmpty()) {
                    catName = (catId != null) ? catId : "";
                    Log.d("RegistroConteos", "Using ID as fallback name");
                }
                
                String title = "Hacer Inventario";
                runOnUiThread(() -> {
                    if (txtTitulo != null) txtTitulo.setText(title);
                });
            } else {
                Log.e("RegistroConteos", "Parent TomaFisicaEntity not found for ID: " + tomaFisicaId);
            }
        }).start();
    }



    private static String normalizeGuidFilter(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        if (v.equalsIgnoreCase("NULL")) return null;
        if (v.equals("00000000-0000-0000-0000-000000000000")) return null;
        return v;
    }

    private void initFilters() {
        new Thread(() -> {
            TomaFisicaEntity toma = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
            baseUbicacionAId = toma != null ? normalizeGuidFilter(toma.getUbicacionA()) : null;
            baseUbicacionBId = toma != null ? normalizeGuidFilter(toma.getUbicacionB()) : null;
            baseUbicacionCId = toma != null ? normalizeGuidFilter(toma.getUbicacionC()) : null;
            baseUbicacionDId = toma != null ? normalizeGuidFilter(toma.getUbicacionD()) : null;

            runOnUiThread(() -> {
                setupLocationSpinners();
            });
        }).start();
    }

    private void setSpinnerItems(Spinner spinner, List<SpinnerItem> items) {
        if (spinner == null) return;
        isUpdatingSpinners = true;
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        isUpdatingSpinners = false;
    }

    private void setSpinnerStrings(Spinner spinner, List<String> items) {
        if (spinner == null) return;
        isUpdatingSpinners = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        isUpdatingSpinners = false;
    }

    private void setSoloTodasForSpinnerItem(Spinner spinner) {
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(null, "Todas"));
        setSpinnerItems(spinner, list);
    }

    private void setSoloTodasForString(Spinner spinner) {
        List<String> list = new ArrayList<>();
        list.add("Todas");
        setSpinnerStrings(spinner, list);
    }
    
    private void setupLocationSpinners() {
        if (spinnerUbicacionA == null) return;

        setSoloTodasForSpinnerItem(spinnerUbicacionB);
        setSoloTodasForSpinnerItem(spinnerUbicacionC);
        setSoloTodasForSpinnerItem(spinnerUbicacionD);
        setSoloTodasForString(spinnerUbicacionSecundaria);

        spinnerUbicacionA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionAId = (item != null && item.id != null) ? item.id : null;
                
                selectedUbicacionBId = null;
                selectedUbicacionCId = null;
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;
                
                if (selectedUbicacionAId != null) {
                    loadUbicacionB(selectedUbicacionAId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionB);
                        setSoloTodasForSpinnerItem(spinnerUbicacionC);
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshSummaryCounts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionB.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionBId = (item != null && item.id != null) ? item.id : null;
                
                selectedUbicacionCId = null;
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionBId != null) {
                    loadUbicacionC(selectedUbicacionBId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionC);
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshSummaryCounts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionCId = (item != null && item.id != null) ? item.id : null;
                
                selectedUbicacionDId = null;
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionCId != null) {
                    loadUbicacionD(selectedUbicacionCId);
                } else {
                    runOnUiThread(() -> {
                        setSoloTodasForSpinnerItem(spinnerUbicacionD);
                        setSoloTodasForString(spinnerUbicacionSecundaria);
                    });
                }
                refreshSummaryCounts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUbicacionD.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
                selectedUbicacionDId = (item != null && item.id != null) ? item.id : null;
                
                selectedUbicacionSecundariaId = null;

                if (selectedUbicacionDId != null) {
                    loadUbicacionSecundaria(selectedUbicacionDId);
                } else {
                    runOnUiThread(() -> setSoloTodasForString(spinnerUbicacionSecundaria));
                }
                refreshSummaryCounts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerUbicacionSecundaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;
                String item = (String) parent.getItemAtPosition(position);
                selectedUbicacionSecundariaId = (item != null && !item.equals("Todas")) ? item : null;
                refreshSummaryCounts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadUbicacionA();
    }

    private void loadUbicacionA() {
        new Thread(() -> {
            listUbicacionA = new ArrayList<>();
            if (baseUbicacionAId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByASysId(baseUbicacionAId);
                String label = (u != null && u.getUbicacionA() != null && !u.getUbicacionA().trim().isEmpty()) ? u.getUbicacionA().trim() : baseUbicacionAId;
                listUbicacionA.add(new SpinnerItem(baseUbicacionAId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionA();
                listUbicacionA.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionA.add(new SpinnerItem(u.getASysId(), u.getUbicacionA()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionA, listUbicacionA);
                if (baseUbicacionAId != null) {
                    selectedUbicacionAId = baseUbicacionAId;
                    loadUbicacionB(baseUbicacionAId);
                } else {
                    selectedUbicacionAId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionB);
                    setSoloTodasForSpinnerItem(spinnerUbicacionC);
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshSummaryCounts();
                }
            });
        }).start();
    }

    private void loadUbicacionB(String parentId) {
        new Thread(() -> {
            listUbicacionB = new ArrayList<>();
            if (baseUbicacionBId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByBSysId(baseUbicacionBId);
                String label = (u != null && u.getUbicacionB() != null && !u.getUbicacionB().trim().isEmpty()) ? u.getUbicacionB().trim() : baseUbicacionBId;
                listUbicacionB.add(new SpinnerItem(baseUbicacionBId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionB(parentId);
                listUbicacionB.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionB.add(new SpinnerItem(u.getBSysId(), u.getUbicacionB()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionB, listUbicacionB);
                if (baseUbicacionBId != null) {
                    selectedUbicacionBId = baseUbicacionBId;
                    loadUbicacionC(baseUbicacionBId);
                } else {
                    selectedUbicacionBId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionC);
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshSummaryCounts();
                }
            });
        }).start();
    }

    private void loadUbicacionC(String parentId) {
        new Thread(() -> {
            listUbicacionC = new ArrayList<>();
            if (baseUbicacionCId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByCSysId(baseUbicacionCId);
                String label = (u != null && u.getUbicacionC() != null && !u.getUbicacionC().trim().isEmpty()) ? u.getUbicacionC().trim() : baseUbicacionCId;
                listUbicacionC.add(new SpinnerItem(baseUbicacionCId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionC(parentId);
                listUbicacionC.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionC.add(new SpinnerItem(u.getCSysId(), u.getUbicacionC()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionC, listUbicacionC);
                if (baseUbicacionCId != null) {
                    selectedUbicacionCId = baseUbicacionCId;
                    loadUbicacionD(baseUbicacionCId);
                } else {
                    selectedUbicacionCId = null;
                    setSoloTodasForSpinnerItem(spinnerUbicacionD);
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshSummaryCounts();
                }
            });
        }).start();
    }

    private void loadUbicacionD(String parentId) {
        new Thread(() -> {
            listUbicacionD = new ArrayList<>();
            if (baseUbicacionDId != null) {
                UbicacionEntity u = ubicacionDao.getUbicacionByDSysId(baseUbicacionDId);
                String label = (u != null && u.getUbicacionD() != null && !u.getUbicacionD().trim().isEmpty()) ? u.getUbicacionD().trim() : baseUbicacionDId;
                listUbicacionD.add(new SpinnerItem(baseUbicacionDId, label));
            } else {
                List<UbicacionEntity> list = ubicacionDao.getDistinctUbicacionD(parentId);
                listUbicacionD.add(new SpinnerItem(null, "Todas"));
                for (UbicacionEntity u : list) listUbicacionD.add(new SpinnerItem(u.getDSysId(), u.getUbicacionD()));
            }

            runOnUiThread(() -> {
                setSpinnerItems(spinnerUbicacionD, listUbicacionD);
                if (baseUbicacionDId != null) {
                    selectedUbicacionDId = baseUbicacionDId;
                    loadUbicacionSecundaria(baseUbicacionDId);
                } else {
                    selectedUbicacionDId = null;
                    setSoloTodasForString(spinnerUbicacionSecundaria);
                    refreshSummaryCounts();
                }
            });
        }).start();
    }

    private void loadUbicacionSecundaria(String parentId) {
        new Thread(() -> {
            List<String> list = activoDao.getDistinctUbicacionSecundaria(parentId);
            listUbicacionSecundaria = new ArrayList<>();
            listUbicacionSecundaria.add("Todas");
            listUbicacionSecundaria.addAll(list);

            runOnUiThread(() -> {
                setSpinnerStrings(spinnerUbicacionSecundaria, listUbicacionSecundaria);
                selectedUbicacionSecundariaId = null;
                refreshSummaryCounts();
            });
        }).start();
    }

    private void refreshSummaryCounts() {
        String ua = selectedUbicacionAId != null ? selectedUbicacionAId : baseUbicacionAId;
        String ub = selectedUbicacionBId != null ? selectedUbicacionBId : baseUbicacionBId;
        String uc = selectedUbicacionCId != null ? selectedUbicacionCId : baseUbicacionCId;
        String ud = selectedUbicacionDId != null ? selectedUbicacionDId : baseUbicacionDId;
        String us = selectedUbicacionSecundariaId;

        // Count expected actives based on filters
        int countFiltrados = activoDao.countActivosByFiltros(ua, ub, uc, ud, us);
        if (txtCountActivosFiltrados != null) txtCountActivosFiltrados.setText("Activos filtrados: " + countFiltrados);

        // Update counts per filter level
        String ubForA = baseUbicacionBId;
        String ucForA = baseUbicacionCId;
        String udForA = baseUbicacionDId;

        String ubForB = selectedUbicacionBId != null ? selectedUbicacionBId : baseUbicacionBId;
        String ucForB = baseUbicacionCId;
        String udForB = baseUbicacionDId;

        String ucForC = selectedUbicacionCId != null ? selectedUbicacionCId : baseUbicacionCId;
        String udForC = baseUbicacionDId;

        String udForD = selectedUbicacionDId != null ? selectedUbicacionDId : baseUbicacionDId;

        int countA = activoDao.countActivosByFiltros(ua, ubForA, ucForA, udForA);
        int countB = activoDao.countActivosByFiltros(ua, ubForB, ucForB, udForB);
        int countC = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForC);
        int countD = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForD);
        int countS = activoDao.countActivosByFiltros(ua, ubForB, ucForC, udForD, us);

        if (txtCountUbicacionA != null) txtCountUbicacionA.setText("Activos: " + countA);
        if (txtCountUbicacionB != null) txtCountUbicacionB.setText("Activos: " + countB);
        if (txtCountUbicacionC != null) txtCountUbicacionC.setText("Activos: " + countC);
        if (txtCountUbicacionD != null) txtCountUbicacionD.setText("Activos: " + countD);
        if (txtCountUbicacionSecundaria != null) txtCountUbicacionSecundaria.setText("Activos: " + countS);

        // Update Main Summary (KPIs)
        cargarDatos();
    }

    private void updateTomasTabs() {
        if (containerTomasTabs == null) return;
        new Thread(() -> {
            List<TomaFisicaTomasEntity> subtomas = tomasDao.getByTomaFisicaId(tomaFisicaId);
            if (subtomas == null) subtomas = new ArrayList<>();
            Collections.sort(subtomas, (o1, o2) -> {
                try {
                    return Integer.compare(Integer.parseInt(o1.getNumeroToma()), Integer.parseInt(o2.getNumeroToma()));
                } catch (Exception e) { return 0; }
            });

            final List<TomaFisicaTomasEntity> finalList = subtomas;
            runOnUiThread(() -> {
                containerTomasTabs.removeAllViews();
                for (TomaFisicaTomasEntity t : finalList) {
                    TextView tab = new TextView(this);
                    tab.setText("Toma " + t.getNumeroToma());
                    boolean isSelected = t.getIdToma().equals(idToma);
                    
                    if (isSelected) {
                         tab.setTextColor(ContextCompat.getColor(this, R.color.blanco));
                         tab.setBackgroundResource(R.drawable.btn_dark_gray);
                    } else {
                         tab.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
                         tab.setBackgroundResource(R.drawable.btn_secondary_gray);
                    }
                    tab.setPadding(48, 16, 48, 16);
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 16, 0);
                    tab.setLayoutParams(params);
                    
                    tab.setOnClickListener(v -> {
                        if (!isSelected) {
                            Intent intent = new Intent(this, RegistroConteosActivity.class);
                            intent.putExtra("tomaFisicaId", tomaFisicaId);
                            intent.putExtra("idToma", t.getIdToma());
                            intent.putExtra("numeroToma", t.getNumeroToma());
                            startActivity(intent);
                            finish();
                        }
                    });
                    containerTomasTabs.addView(tab);
                }

                /*
                TextView tabAdd = new TextView(this);
                tabAdd.setText("+");
                tabAdd.setTextColor(ContextCompat.getColor(this, R.color.nav_item_text_tint));
                tabAdd.setBackgroundResource(R.drawable.btn_secondary_gray);
                tabAdd.setPadding(48, 16, 48, 16);
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                tabAdd.setLayoutParams(params);

                boolean canAdd = finalList.size() < 5;
                tabAdd.setAlpha(canAdd ? 1f : 0.45f);
                tabAdd.setOnClickListener(v -> {
                    if (!canAdd) {
                        Toast.makeText(this, "Límite de 5 tomas alcanzado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, NuevaTomaActivity.class);
                    intent.putExtra("tomaFisicaId", tomaFisicaId);
                    startActivity(intent);
                    finish();
                });
                containerTomasTabs.addView(tabAdd);
                */
            });
        }).start();
    }

    private void cargarActivosSpinner() {
        if (btnAgregarLectura != null) btnAgregarLectura.setEnabled(false);
        new Thread(() -> {
            try {
                // OPTIMIZACIÓN: Cargar solo activos de la ubicación de la toma para evitar OOM/ANR
                // Cargar todos los activos (getAllLocalActivos) puede causar crashes si hay muchos registros.
                if (tomaFisicaDao != null && tomaFisicaId != null) {
                    TomaFisicaEntity toma = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
                    if (toma != null && toma.getUbicacionD() != null) {
                        listaActivosSpinner = activoDao.getActivosByUbicacion(toma.getUbicacionD());
                    } else {
                        // Si no hay ubicación definida, intentamos cargar pero con precaución
                        // Idealmente deberíamos tener un método con LIMIT en el DAO
                        // Por ahora, para evitar crash, dejamos lista vacía o manejamos error
                        listaActivosSpinner = new ArrayList<>();
                        Log.w("RegistroConteos", "Toma sin UbicacionD, no se cargan activos para manual");
                    }
                } else {
                    listaActivosSpinner = new ArrayList<>();
                }
            } catch (Exception e) {
                Log.e("RegistroConteos", "Error cargando activos para spinner", e);
                listaActivosSpinner = new ArrayList<>();
            }

            runOnUiThread(() -> {
                if (listaActivosSpinner != null && !listaActivosSpinner.isEmpty()) {
                    List<String> descripciones = new ArrayList<>();
                    for (ActivoEntity a : listaActivosSpinner) {
                        descripciones.add(buildActivoDisplay(a));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, descripciones);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    if (spinnerActivos != null) spinnerActivos.setAdapter(adapter);
                    if (btnAgregarLectura != null) btnAgregarLectura.setEnabled(true);
                } else {
                    List<String> empty = new ArrayList<>();
                    empty.add("Sin activos en esta ubicación");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, empty);
                    if (spinnerActivos != null) spinnerActivos.setAdapter(adapter);
                }
            });
        }).start();
    }

    private static String buildActivoDisplay(ActivoEntity a) {
        if (a == null) return "Activo";

        String placa = a.getNumeroActivo();
        String numeroEtiqueta = a.getNumeroEtiqueta();
        String serie = a.getNumeroSerie();
        String nombre = a.getDescripcionCorta();

        String id;
        if (placa != null && !placa.trim().isEmpty()) {
            id = placa.trim();
        } else if (numeroEtiqueta != null && !numeroEtiqueta.trim().isEmpty()) {
            id = numeroEtiqueta.trim();
        } else {
            id = a.getIdActivo() != null ? a.getIdActivo().trim() : "";
        }

        StringBuilder sb = new StringBuilder();
        if (id != null && !id.isEmpty()) sb.append(id);
        if (serie != null && !serie.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(serie.trim());
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(nombre.trim());
        }

        if (sb.length() == 0) {
            return "Sin Descripción";
        }
        return sb.toString();
    }

    private void agregarLecturaManual() {
        if (listaActivosSpinner == null || listaActivosSpinner.isEmpty()) {
             Toast.makeText(this, "No hay activos cargados", Toast.LENGTH_SHORT).show();
             return;
        }
        int pos = spinnerActivos.getSelectedItemPosition();
        if (pos < 0 || pos >= listaActivosSpinner.size()) return;
        
        btnAgregarLectura.setEnabled(false);
        btnAgregarLectura.setText("Guardando...");

        ActivoEntity activo = listaActivosSpinner.get(pos);
        String epc = activo.getTagEpc() != null ? activo.getTagEpc() : "MANUAL-" + System.currentTimeMillis();
        
        new Thread(() -> {
             try {
                 String activoId = activo.getIdActivo() != null ? activo.getIdActivo().trim() : "";
                 if (activoId.isEmpty()) {
                     runOnUiThread(() -> {
                         Toast.makeText(RegistroConteosActivity.this, "Activo inválido", Toast.LENGTH_SHORT).show();
                         restaurarBoton();
                     });
                     return;
                 }

                 List<TomaFisicaDetallesEntity> existentes = detallesDao.getByIdToma(idToma);
                 if (existentes != null) {
                     for (TomaFisicaDetallesEntity d : existentes) {
                         if (d == null) continue;
                         String dActivoId = d.getActivoId() != null ? d.getActivoId().trim() : "";
                         String dEpc = d.getEpc() != null ? d.getEpc().trim() : "";
                         if (!dActivoId.isEmpty() && dActivoId.equalsIgnoreCase(activoId)) {
                             runOnUiThread(() -> {
                                 Toast.makeText(RegistroConteosActivity.this, "Este activo ya fue leído", Toast.LENGTH_SHORT).show();
                                 restaurarBoton();
                             });
                             return;
                         }
                         if (!dEpc.isEmpty() && dEpc.equalsIgnoreCase(epc)) {
                             runOnUiThread(() -> {
                                 Toast.makeText(RegistroConteosActivity.this, "Este activo ya fue leído", Toast.LENGTH_SHORT).show();
                                 restaurarBoton();
                             });
                             return;
                         }
                     }
                 }

                 String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                 
                 // Determinar estado correcto
                 TomaFisicaEntity tomaFisica = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
                 boolean esEsperado = false;
                 if (tomaFisica != null && tomaFisica.getUbicacionD() != null) {
                     List<ActivoEntity> expected = activoDao.getActivosByUbicacion(tomaFisica.getUbicacionD());
                     if (expected != null) {
                         for(ActivoEntity a : expected) {
                             if (a.getIdActivo() != null && a.getIdActivo().equalsIgnoreCase(activoId)) {
                                 esEsperado = true;
                                 break;
                             }
                         }
                     }
                 }
                 
                 String estado = esEsperado ? "ENCONTRADO" : "SOBRANTE";

                 TomaFisicaDetallesEntity detail = new TomaFisicaDetallesEntity();
                 detail.setIdTakeDetail(UUID.randomUUID().toString());
                 detail.setIdToma(idToma);
                 detail.setNumeroToma(numeroToma);
                 detail.setEpc(epc);
                 detail.setDateRead(now);
                 detail.setActivoId(activoId);
                 detail.setEstadoInventario(estado);
                 
                 detallesDao.saveLocal(Collections.singletonList(detail));
                 recalcularResumenCompleto(idToma);

                 runOnUiThread(() -> {
                     Toast.makeText(RegistroConteosActivity.this, "Guardada localmente", Toast.LENGTH_SHORT).show();
                     cargarDatos();
                     restaurarBoton();
                 });
             } catch (Exception e) {
                 Log.e("RegistroConteos", "Error agregando lectura manual", e);
                 runOnUiThread(() -> {
                     Toast.makeText(RegistroConteosActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                     restaurarBoton();
                 });
             }
        }).start();
    }

    private void restaurarBoton() {
        if (btnAgregarLectura != null) {
            btnAgregarLectura.setEnabled(true);
            btnAgregarLectura.setText("Añadir Lectura");
        }
    }

    private void setTab(boolean mostrarResumen) {
        viewResumen.setVisibility(mostrarResumen ? View.VISIBLE : View.GONE);
        viewActivos.setVisibility(mostrarResumen ? View.GONE : View.VISIBLE);

        tabResumen.setBackgroundResource(mostrarResumen ? R.drawable.btn_primary : R.drawable.btn_secondary_gray);
        tabActivos.setBackgroundResource(mostrarResumen ? R.drawable.btn_secondary_gray : R.drawable.btn_primary);

        tabResumen.setTextColor(ContextCompat.getColor(this, mostrarResumen ? R.color.blanco : R.color.nav_item_text_tint));
        tabActivos.setTextColor(ContextCompat.getColor(this, mostrarResumen ? R.color.nav_item_text_tint : R.color.blanco));
        
        if (!mostrarResumen) {
            mostrarVistaActivos(currentFilter != null);
        }
    }

    private void cargarDatos() {
        if (idToma == null || idToma.trim().isEmpty()) {
            adapter.setItems(new ArrayList<>());
            // ... (resto del código de empty state)
            return;
        }

        Categoria filtro = currentFilter;
        Log.d("RegistroConteos", "cargarDatos: filtro=" + (filtro != null ? filtro.name() : "null") + ", idToma=" + idToma);

        // Lógica de visibilidad de filas de resumen
        int visibilityResumen = (filtro != null) ? View.GONE : View.VISIBLE;
        
        if (txtTotales != null) txtTotales.setVisibility(visibilityResumen);
        
        View summaryContainer = findViewById(R.id.summaryContainer);
        if (summaryContainer != null) {
            summaryContainer.setVisibility(visibilityResumen);
        } else {
            // Fallback para compatibilidad si el XML no se actualizó correctamente (aunque debería)
            View rowNoEncontrados = findViewById(R.id.rowNoEncontrados);
            if (rowNoEncontrados != null) rowNoEncontrados.setVisibility(visibilityResumen);
            
            View rowEncontrados = findViewById(R.id.rowEncontrados);
            if (rowEncontrados != null) rowEncontrados.setVisibility(visibilityResumen);
            
            View rowNoPertenecen = findViewById(R.id.rowNoPertenecen);
            if (rowNoPertenecen != null) rowNoPertenecen.setVisibility(visibilityResumen);
            
            View rowNoInventariados = findViewById(R.id.rowNoInventariados);
            if (rowNoInventariados != null) rowNoInventariados.setVisibility(visibilityResumen);
        }

        if (filtro != null) {
            String idTomaValue = idToma.trim();
            boolean sameToma = remotoIdToma != null && remotoIdToma.equalsIgnoreCase(idTomaValue);
            boolean sameCat = remotoCategoria == filtro;

            if (!remotoCargando && (!sameToma || !sameCat)) {
                remotoCargando = true;
                filasRemotas.clear();
                adapter.setDao(activoDao);
                adapter.setItems(new ArrayList<>());
                setCargando(true);

                if (filtro == Categoria.BLANCO) {
                    // Carga combinada: Primero local, luego API si está disponible
                    new Thread(() -> {
                        List<TomaFisicaDetallesEntity> filasLocales = cargarFilasBlancoDesdeLocal(idTomaValue);
                        runOnUiThread(() -> {
                            // Mostrar locales primero mientras cargan remotos
                            adapter.setDao(activoDao);
                            adapter.setItems(new ArrayList<>(filasLocales));
                            
                            detallesDao.fetchAndSyncFromApi(idTomaValue, () -> runOnUiThread(() -> {
                                List<TomaFisicaDetallesEntity> filasActualizadas = cargarFilasBlancoDesdeLocal(idTomaValue);
                                remotoIdToma = idTomaValue;
                                remotoCategoria = filtro;
                                remotoCargando = false;
                                filasRemotas.clear();
                                filasRemotas.addAll(filasActualizadas);
                                setCargando(false);
                                cargarDatos();
                            }));
                        });
                    }).start();
                } else {
                    // Fallback local primero para categorías distintas de Blanco
                    new Thread(() -> {
                         // Buscar locales que coincidan con el filtro
                         List<TomaFisicaDetallesEntity> localesFiltrados = new ArrayList<>();
                         List<TomaFisicaDetallesEntity> locales = detallesDao.getByIdToma(idTomaValue);
                         if (locales != null) {
                             for (TomaFisicaDetallesEntity d : locales) {
                                 if (d != null && categoriaDe(d.getEstadoInventario()) == filtro) {
                                     localesFiltrados.add(d);
                                 }
                             }
                         }
                         
                         runOnUiThread(() -> {
                             if (!localesFiltrados.isEmpty()) {
                                 adapter.setDao(activoDao);
                                 adapter.setItems(new ArrayList<>(localesFiltrados));
                             }
                             
                             activoDao.fetchAndSyncActivosPorEstado(idTomaValue, filtro.name(), new ApiCallback<List<ActivoEntity>>() {
                                @Override
                                public void onComplete(ApiResponse<List<ActivoEntity>> response) {
                                    List<TomaFisicaDetallesEntity> filas = new ArrayList<>();
                                    if (response.success && response.data != null) {
                                        for (ActivoEntity a : response.data) {
                                            if (a == null) continue;
                                            String activoId = a.getIdActivo();
                                            if (activoId == null || activoId.trim().isEmpty()) continue;
                                            TomaFisicaDetallesEntity d = new TomaFisicaDetallesEntity();
                                            d.setIdToma(idTomaValue);
                                            d.setActivoId(activoId.trim());
                                            d.setEstadoInventario(estadoInventarioDeCategoria(filtro));
                                            filas.add(d);
                                        }
                                    }
                                    runOnUiThread(() -> {
                                        Log.d("RegistroConteos", "Filas remotas recibidas: " + filas.size());
                                        remotoIdToma = idTomaValue;
                                        remotoCategoria = filtro;
                                        remotoCargando = false;
                                        filasRemotas.clear();
                                        filasRemotas.addAll(filas);
                                        setCargando(false);
                                        cargarDatos();
                                    });
                                }
                            });
                         });
                    }).start();
                }
                return;
            }

            if (remotoCargando) {
                adapter.setDao(activoDao);
                adapter.setItems(new ArrayList<>(mergeFilasRemotasConLocales(idTomaValue, filtro, filasRemotas)));
                setCargando(true);
                return;
            }

            adapter.setDao(activoDao);
            adapter.setItems(new ArrayList<>(mergeFilasRemotasConLocales(idTomaValue, filtro, filasRemotas)));
            setCargando(false);
            return;
        }

        List<TomaFisicaDetallesEntity> detalles = detallesDao.getByIdToma(idToma.trim());
        if (detalles == null) detalles = new ArrayList<>();

        int total = 0;
        int rojo = 0;
        int verde = 0;
        int amarillo = 0;
        int blanco = 0;

        TomaFisicaTomasEntity resumen = tomasDao != null ? tomasDao.getByIdToma(idToma.trim()) : null;
        if (resumen != null) {
            int totalActivos = parseIntOrZero(resumen.getTotalActivos());
            int faltantes = parseIntOrZero(resumen.getFaltantes());
            int sobrantes = parseIntOrZero(resumen.getSobrantes());
            int escaneados = parseIntOrZero(resumen.getActivosLeidos());

            int encontrados = totalActivos - faltantes;
            if (encontrados < 0) encontrados = 0;

            total = totalActivos + sobrantes;
            if (total < 0) total = 0;

            rojo = Math.max(0, faltantes);
            // verde = Math.max(0, encontrados); // Se recalcula desde detalles
            // amarillo = Math.max(0, sobrantes); // Se recalcula desde detalles para separar de Blanco
            // blanco = Math.max(0, total - (rojo + verde + amarillo)); 

            // Reiniciar contadores para calcular con precisión desde los detalles
            verde = 0;
            amarillo = 0;
            blanco = 0;

            if (txtEscaneados != null) txtEscaneados.setText(String.valueOf(Math.max(0, encontrados)));
            if (txtDeTotal != null) txtDeTotal.setText("De " + Math.max(0, totalActivos));
            if (kpiProgressCircle != null) {
                int progress = 0;
                if (totalActivos > 0) {
                    progress = (int) Math.round((Math.min(encontrados, totalActivos) * 100.0) / totalActivos);
                    if (progress < 0) progress = 0;
                    if (progress > 100) progress = 100;
                }
                kpiProgressCircle.setProgress(progress);
            }
        } else {
            if (txtEscaneados != null) txtEscaneados.setText("0");
            if (txtDeTotal != null) txtDeTotal.setText("De 0");
            if (kpiProgressCircle != null) kpiProgressCircle.setProgress(0);
        }

        List<TomaFisicaDetallesEntity> filas = new ArrayList<>();
        for (TomaFisicaDetallesEntity d : detalles) {
            if (d == null) continue;
            if (d.getActivoId() == null && d.getEpc() == null) continue;
            if (filtro == null || categoriaDe(d.getEstadoInventario()) == filtro) {
                filas.add(d);
            }
            
            // Contar siempre desde detalles para tener la separación correcta
            if (resumen != null) {
                Categoria c = categoriaDe(d.getEstadoInventario());
                if (c == Categoria.VERDE) verde++;
                else if (c == Categoria.AMARILLO) amarillo++;
                else if (c == Categoria.BLANCO) blanco++;
            } else {
                total++;
                Categoria c = categoriaDe(d.getEstadoInventario());
                if (c == Categoria.ROJO) rojo++;
                else if (c == Categoria.VERDE) verde++;
                else if (c == Categoria.AMARILLO) amarillo++;
                else blanco++;
            }
        }

        int totalExpected = 0;
        if (resumen != null) {
            totalExpected = parseIntOrZero(resumen.getTotalActivos());
        }
        String totalStr = String.valueOf(totalExpected);

        txtTotales.setText("Total: " + total);
        txtNoEncontrados.setText(rojo + "/" + totalStr);
        txtEncontrados.setText(verde + "/" + totalStr);
        txtNoPertenecen.setText(amarillo + "/" + totalStr);
        txtNoInventariados.setText(String.valueOf(blanco));

        adapter.setDao(activoDao);
        adapter.setItems(filas);
    }

    private static int parseIntOrZero(String value) {
        if (value == null) return 0;
        String s = value.trim();
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private Categoria filtroCategoria() {
        if (estadoFiltro == null) return null;
        String s = estadoFiltro.trim();
        if (s.isEmpty()) return null;
        try {
            return Categoria.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return null;
        }
    }

    private void abrirListado(Categoria categoria, String titulo) {
        if (categoria == null) return;
        
        Log.d("RegistroConteos", "abrirListado: " + categoria.name() + ", idToma=" + idToma);

        // Switch to Activos tab
        setTab(false);
        
        // Update filter state
        currentFilter = categoria;
        
        // Show List View
        mostrarVistaActivos(true);
        
        // Reload data with new filter
        cargarDatos();
    }

    private enum Categoria { ROJO, VERDE, AMARILLO, BLANCO }

    private static Categoria categoriaDe(String estado) {
        if (estado == null) return Categoria.BLANCO;
        String s = estado.trim();
        if (s.isEmpty()) return Categoria.BLANCO;
        s = s.toUpperCase(Locale.ROOT);

        if (s.contains("ROJO") || s.contains("FALTANTE") || s.contains("NO ENCONTRADO") || s.contains("NO_ENCONTRADO"))
            return Categoria.ROJO;
        if (s.contains("AMARILLO") || s.contains("SOBRANTE") || s.contains("NO PERTENECE") || s.contains("NO_PERTENECE") || s.contains("NOPERTENECE"))
            return Categoria.AMARILLO;
        if (s.contains("VERDE") || s.contains("ENCONTRADO")) return Categoria.VERDE;
        if (s.contains("BLANCO") || s.contains("NO INVENTARIADO") || s.contains("PENDIENTE"))
            return Categoria.BLANCO;

        return Categoria.BLANCO;
    }

    private static String estadoInventarioDeCategoria(Categoria categoria) {
        if (categoria == Categoria.VERDE) return "ENCONTRADO";
        if (categoria == Categoria.AMARILLO) return "SOBRANTE";
        if (categoria == Categoria.ROJO) return "NO_ENCONTRADO";
        return "NO_INVENTARIADO";
    }

    private void setCargando(boolean cargando) {
        if (progressActivos != null) {
            progressActivos.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
    }

    private List<TomaFisicaDetallesEntity> cargarFilasBlancoDesdeLocal(String idTomaValue) {
        List<TomaFisicaDetallesEntity> list = new ArrayList<>();
        List<TomaFisicaDetallesEntity> detalles = detallesDao.getByIdToma(idTomaValue);
        if (detalles == null) return list;

        for (TomaFisicaDetallesEntity d : detalles) {
            if (d == null) continue;
            String epc = d.getEpc();
            if (epc == null) continue;
            String epcTrim = epc.trim();
            if (epcTrim.isEmpty()) continue;
            if (epcTrim.equalsIgnoreCase("EPC Asignado")) continue;

            String activoId = d.getActivoId();
            if (activoId != null && !activoId.trim().isEmpty()) continue;

            TomaFisicaDetallesEntity row = new TomaFisicaDetallesEntity();
            row.setIdToma(idTomaValue);
            row.setEpc(epcTrim);
            row.setEstadoInventario(estadoInventarioDeCategoria(Categoria.BLANCO));
            list.add(row);
        }

        return list;
    }

    private List<TomaFisicaDetallesEntity> mergeFilasRemotasConLocales(String idTomaValue, Categoria filtro, List<TomaFisicaDetallesEntity> filasRemotasValue) {
        List<TomaFisicaDetallesEntity> locales = detallesDao.getByIdToma(idTomaValue);
        List<TomaFisicaDetallesEntity> localesFiltradas = new ArrayList<>();
        if (locales != null) {
            for (TomaFisicaDetallesEntity d : locales) {
                if (d == null) continue;
                if (categoriaDe(d.getEstadoInventario()) != filtro) continue;
                if (d.getActivoId() == null && d.getEpc() == null) continue;
                localesFiltradas.add(d);
            }
        }

        List<TomaFisicaDetallesEntity> merged = new ArrayList<>();
        java.util.HashSet<String> keys = new java.util.HashSet<>();

        for (TomaFisicaDetallesEntity d : localesFiltradas) {
            String key = detalleKey(d);
            if (key == null) continue;
            if (keys.add(key)) merged.add(d);
        }

        if (filasRemotasValue != null) {
            for (TomaFisicaDetallesEntity d : filasRemotasValue) {
                String key = detalleKey(d);
                if (key == null) continue;
                if (keys.add(key)) merged.add(d);
            }
        }

        return merged;
    }

    private static String detalleKey(TomaFisicaDetallesEntity d) {
        if (d == null) return null;
        String activoId = d.getActivoId();
        if (activoId != null && !activoId.trim().isEmpty()) return "A:" + activoId.trim().toLowerCase(Locale.ROOT);
        String epc = d.getEpc();
        if (epc != null && !epc.trim().isEmpty()) return "E:" + epc.trim().toLowerCase(Locale.ROOT);
        return null;
    }

    private void recalcularResumenCompleto(String idTomaValue) {
        if (idTomaValue == null || idTomaValue.trim().isEmpty()) return;
        TomaFisicaTomasEntity header = tomasDao.getByIdToma(idTomaValue.trim());
        if (header == null) return;

        List<TomaFisicaDetallesEntity> detalles = detallesDao.getByIdToma(idTomaValue.trim());
        
        // 2. Obtener esperados usando los filtros actuales si existen
        TomaFisicaEntity tomaFisica = tomaFisicaDao.getTomaFisicaById(tomaFisicaId);
        java.util.Set<String> expectedEpcs = new java.util.HashSet<>();
        java.util.Set<String> expectedIds = new java.util.HashSet<>();
        int totalExpectedCount = 0;
        
        if (tomaFisica != null) {
            String ua = selectedUbicacionAId != null ? selectedUbicacionAId : (baseUbicacionAId != null ? baseUbicacionAId : tomaFisica.getUbicacionA());
            String ub = selectedUbicacionBId != null ? selectedUbicacionBId : (baseUbicacionBId != null ? baseUbicacionBId : tomaFisica.getUbicacionB());
            String uc = selectedUbicacionCId != null ? selectedUbicacionCId : (baseUbicacionCId != null ? baseUbicacionCId : tomaFisica.getUbicacionC());
            String ud = selectedUbicacionDId != null ? selectedUbicacionDId : (baseUbicacionDId != null ? baseUbicacionDId : tomaFisica.getUbicacionD());
            String us = selectedUbicacionSecundariaId;
            
            List<ActivoEntity> expected = activoDao.getActivosByFiltros(ua, ub, uc, ud, us);
            if (expected != null) {
                totalExpectedCount = expected.size();
                for (ActivoEntity a : expected) {
                    if (a.getTagEpc() != null && !a.getTagEpc().trim().isEmpty()) 
                        expectedEpcs.add(a.getTagEpc().trim().toUpperCase());
                    if (a.getIdActivo() != null && !a.getIdActivo().trim().isEmpty()) 
                        expectedIds.add(a.getIdActivo().trim().toUpperCase());
                }
            }
        }

        // 3. Calcular contadores
        int encontrados = 0;
        int sobrantes = 0;
        
        // Deduplicate scans
        java.util.Set<String> processedEpcs = new java.util.HashSet<>();
        
        if (detalles != null) {
            for (TomaFisicaDetallesEntity d : detalles) {
                String epc = d.getEpc() != null ? d.getEpc().trim() : "";
                if (epc.isEmpty()) continue;
                
                if (processedEpcs.contains(epc)) continue; // Already processed this EPC
                processedEpcs.add(epc);
                
                String activoId = d.getActivoId() != null ? d.getActivoId().trim() : "";
                
                boolean isFound = false;
                if (expectedEpcs.contains(epc.toUpperCase())) {
                    isFound = true;
                } else if (!activoId.isEmpty() && expectedIds.contains(activoId.toUpperCase())) {
                    isFound = true;
                }
                
                if (isFound) encontrados++;
                else sobrantes++;
            }
        }

        int totalLecturas = processedEpcs.size();
        int faltantes = totalExpectedCount - encontrados;
        if (faltantes < 0) faltantes = 0;

        // 4. Actualizar Header
        header.setTotalActivos(String.valueOf(totalExpectedCount));
        header.setActivosLeidos(String.valueOf(totalLecturas));
        header.setTotalLecturas(String.valueOf(totalLecturas));
        header.setFaltantes(String.valueOf(faltantes));
        header.setSobrantes(String.valueOf(sobrantes));

        // 5. Guardar
        List<TomaFisicaTomasEntity> list = new ArrayList<>();
        list.add(header);
        tomasDao.saveLocal(list);
    }

    private static class ActivosAdapter extends RecyclerView.Adapter<ActivosAdapter.VH> {

        private final List<TomaFisicaDetallesEntity> items = new ArrayList<>();
        private final Map<String, ActivoEntity> cacheByKey = new HashMap<>();
        private final Map<String, Bitmap> bitmapCacheByKey = new HashMap<>();
        private ActivoDao activoDao;
        private FotoDBHelper fotoDb;

        void setDao(ActivoDao dao) {
            this.activoDao = dao;
        }

        void setItems(List<TomaFisicaDetallesEntity> nuevos) {
            items.clear();
            cacheByKey.clear();
            bitmapCacheByKey.clear();
            if (nuevos != null) items.addAll(nuevos);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (fotoDb == null) {
                fotoDb = new FotoDBHelper(parent.getContext());
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activo_estado, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TomaFisicaDetallesEntity d = items.get(position);
            String epc = d.getEpc();
            String activoId = d.getActivoId();

            ActivoEntity activo = null;
            String lookupKey = null;
            if (activoId != null && !activoId.trim().isEmpty()) {
                lookupKey = "ID_ACTIVO:" + activoId.trim();
            } else if (epc != null && !epc.trim().isEmpty()) {
                lookupKey = "EPC:" + epc.trim();
            }

            if (activoDao != null && lookupKey != null) {
                if (cacheByKey.containsKey(lookupKey)) {
                    activo = cacheByKey.get(lookupKey);
                } else {
                    if (lookupKey.startsWith("EPC:")) {
                        activo = activoDao.getActivoByEpc(epc.trim());
                    } else {
                        activo = activoDao.getActivoByIdActivo(activoId.trim());
                    }
                    cacheByKey.put(lookupKey, activo);
                }
            }

            String nombre = normalizar(activo != null ? activo.getDescripcionCorta() : null);
            String valNumeroActivo = normalizar(activo != null ? activo.getNumeroActivo() : null);
            String valNumeroEtiqueta = normalizar(activo != null ? activo.getNumeroEtiqueta() : null);
            String serie = normalizar(activo != null ? activo.getNumeroSerie() : null);

            // Web Mapping: Placa -> NUMERO_ETIQUETA, Activo No -> NUMERO_ACTIVO
            
            holder.txtNombre.setText(nombre.isEmpty() ? "Sin nombre" : nombre);
            // Mostrar NumeroActivo en "Activo No:"
            holder.txtNumeroActivo.setText("Activo No: " + (valNumeroActivo.isEmpty() ? "-" : valNumeroActivo));
            // Mostrar Etiqueta en "Placa:"
            holder.txtPlaca.setText("Placa: " + (valNumeroEtiqueta.isEmpty() ? "-" : valNumeroEtiqueta));
            holder.txtSerie.setText("S/N: " + (serie.isEmpty() ? "-" : serie));

            holder.imgFoto.setImageDrawable(null);
            boolean tieneFoto = false;
            
            if (activo != null && lookupKey != null) {
                Bitmap cached = bitmapCacheByKey.get(lookupKey);
                if (cached != null) {
                    holder.imgFoto.setImageBitmap(cached);
                    tieneFoto = true;
                } else {
                    String fotoBase64 = extraerPrimeraFotoBase64(activo.getFotos());
                    Bitmap bmp = decodeBase64ToBitmap(fotoBase64);
                    if (bmp == null) {
                        String ruta = extraerPrimeraRutaFoto(activo.getIdActivo());
                        bmp = decodeFileToBitmap(ruta);
                    }
                    if (bmp != null) {
                        bitmapCacheByKey.put(lookupKey, bmp);
                        holder.imgFoto.setImageBitmap(bmp);
                        tieneFoto = true;
                    }
                }
            }
            
            if (!tieneFoto) {
                holder.imgFoto.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.imgFoto.setImageDrawable(null);
            }

            Categoria c = categoriaDe(d.getEstadoInventario());
            int colorRes;
            if (c == Categoria.ROJO) {
                colorRes = R.color.rojo;
            } else if (c == Categoria.VERDE) {
                colorRes = R.color.verde;
            } else if (c == Categoria.AMARILLO) {
                colorRes = R.color.amarillo;
            } else {
                colorRes = android.R.color.darker_gray;
            }
            
            if (holder.imgEstadoIndicator != null) {
                holder.imgEstadoIndicator.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView imgFoto;
            final TextView txtNombre;
            final TextView txtNumeroActivo;
            final TextView txtSerie;
            final TextView txtPlaca;
            final ImageView imgEstadoIndicator;

            VH(@NonNull View itemView) {
                super(itemView);
                imgFoto = itemView.findViewById(R.id.imgFoto);
                txtNombre = itemView.findViewById(R.id.txtNombre);
                txtNumeroActivo = itemView.findViewById(R.id.txtNumeroActivo);
                txtSerie = itemView.findViewById(R.id.txtSerie);
                txtPlaca = itemView.findViewById(R.id.txtPlaca);
                imgEstadoIndicator = itemView.findViewById(R.id.imgEstadoIndicator);
            }
        }

        private static String normalizar(String value) {
            if (value == null) return "";
            String s = value.trim();
            return s.isEmpty() ? "" : s;
        }

        private static String extraerPrimeraFotoBase64(String fotos) {
            if (fotos == null) return null;
            String s = fotos.trim();
            if (s.isEmpty()) return null;

            if (s.startsWith("data:")) {
                int idx = s.indexOf("base64,");
                if (idx >= 0) {
                    s = s.substring(idx + "base64,".length()).trim();
                }
                return s.isEmpty() ? null : s;
            }

            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1).trim();
            }

            String[] candidates = s.split("[,;|\\n\\r]+");
            if (candidates.length == 0) return null;
            String c = candidates[0] != null ? candidates[0].trim() : "";
            if (c.startsWith("\"") && c.endsWith("\"") && c.length() >= 2) {
                c = c.substring(1, c.length() - 1).trim();
            }
            if (c.startsWith("data:")) {
                int idx = c.indexOf("base64,");
                if (idx >= 0) {
                    c = c.substring(idx + "base64,".length()).trim();
                }
            }
            return c.isEmpty() ? null : c;
        }

        private static Bitmap decodeBase64ToBitmap(String b64) {
            if (b64 == null) return null;
            String s = b64.trim();
            if (s.isEmpty()) return null;
            try {
                byte[] decoded = Base64.decode(s, Base64.DEFAULT);
                if (decoded == null || decoded.length == 0) return null;
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } catch (Exception e) {
                return null;
            }
        }

        private String extraerPrimeraRutaFoto(String assetSysId) {
            if (fotoDb == null || assetSysId == null) return null;
            String key = assetSysId.trim();
            if (key.isEmpty()) return null;
            try {
                ArrayList<EFotoActivo> fotos = fotoDb.ObtenerFotoActivo(key);
                if (fotos == null || fotos.isEmpty()) return null;
                EFotoActivo f = fotos.get(0);
                if (f == null) return null;
                String ruta = f.getRutaFoto();
                if (ruta == null) return null;
                String s = ruta.trim();
                return s.isEmpty() ? null : s;
            } catch (Exception e) {
                return null;
            }
        }

        private static Bitmap decodeFileToBitmap(String ruta) {
            if (ruta == null) return null;
            String s = ruta.trim();
            if (s.isEmpty()) return null;
            try {
                return BitmapFactory.decodeFile(s);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
