package com.example.diverscan.activeid.UI.tomasfisicas;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;

import java.util.ArrayList;
import java.util.List;

import com.example.diverscan.activeid.Scanner.ScannerService;
import com.example.diverscan.activeid.Scanner.ScannerFactory;
import android.widget.Toast;

public class RegistroTomaFisicaActivity extends AppCompatActivity {

    EditText txtBusquedaNombre;
    Button btnBuscar;
    RecyclerView recyclerTomas;

    Button btnRetrocesoRapido, btnRetroceso, btnAvance, btnAvanceRapido;
    TextView txtPagina;

    RegistroTomaFisicaAdapter adapter;
    RegistroTomaFisicaViewModel viewModel;

    List<TomaFisicaEntity> listaCompleta = new ArrayList<>();
    List<TomaFisicaEntity> listaPagina = new ArrayList<>();

    int paginaActual = 1;
    int tamanoPagina = 10;
    int totalPaginas = 1;

    private ScannerService scannerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_toma_fisica);

        // Inicializar Scanner
        scannerService = ScannerFactory.createScanner(this);
        scannerService.setListener(new ScannerService.ScannerListener() {
            @Override
            public void onScanResult(String data, String type) {
                runOnUiThread(() -> {
                    // Acción al escanear: por ejemplo, buscar en la lista
                    txtBusquedaNombre.setText(data);
                    buscar();
                    Toast.makeText(RegistroTomaFisicaActivity.this, "Buscando: " + data, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onStatusMessage(String message) {
                // Logs
            }
        });
        scannerService.connect();

        // UI
        txtBusquedaNombre = findViewById(R.id.txtBusquedaNombre);
        btnBuscar = findViewById(R.id.btnBuscarNombre);
        recyclerTomas = findViewById(R.id.recyclerTomas);

        btnRetrocesoRapido = findViewById(R.id.btnRetrocesoRapido);
        btnRetroceso = findViewById(R.id.btnRetroceso);
        btnAvance = findViewById(R.id.btnAvance);
        btnAvanceRapido = findViewById(R.id.btnAvanceRapido);
        txtPagina = findViewById(R.id.txtPagina);

        // Recycler
        recyclerTomas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistroTomaFisicaAdapter(listaPagina);
        recyclerTomas.setAdapter(adapter);

        // Decorador vertical (línea entre filas)
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_border));
        recyclerTomas.addItemDecoration(divider);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(RegistroTomaFisicaViewModel.class);

        // Observador: cuando ViewModel actualice la lista (carga inicial o búsquedas)
        viewModel.getTomasFisicas().observe(this, lista -> {
            if (lista == null) {
                listaCompleta = new ArrayList<>();
            } else {
                listaCompleta = new ArrayList<>(lista);
            }

            totalPaginas = (int) Math.ceil((double) Math.max(1, listaCompleta.size()) / tamanoPagina);
            if (totalPaginas < 1) totalPaginas = 1;

            if (paginaActual > totalPaginas) paginaActual = totalPaginas;
            if (paginaActual < 1) paginaActual = 1;

            cargarPagina();
        });

        viewModel.cargarTomasFisicas();

        btnBuscar.setOnClickListener(v -> buscar());

        btnRetrocesoRapido.setOnClickListener(v -> {
            paginaActual = 1;
            cargarPagina();
        });

        btnRetroceso.setOnClickListener(v -> {
            if (paginaActual > 1) paginaActual--;
            cargarPagina();
        });

        btnAvance.setOnClickListener(v -> {
            if (paginaActual < totalPaginas) paginaActual++;
            cargarPagina();
        });

        btnAvanceRapido.setOnClickListener(v -> {
            paginaActual = totalPaginas;
            cargarPagina();
        });
    }

    private void buscar() {
        String texto = txtBusquedaNombre.getText().toString().trim();
        if (texto.isEmpty()) {
            viewModel.cargarTomasFisicas();
        } else {
            viewModel.buscarTomasFisicasPorNombre(texto);
        }
        paginaActual = 1;
    }

    private void cargarPagina() {
        if (listaCompleta == null || listaCompleta.isEmpty()) {
            listaPagina.clear();
            txtPagina.setText("0 / 0");
            adapter.notifyDataSetChanged();
            return;
        }

        int inicio = (paginaActual - 1) * tamanoPagina;
        int fin = Math.min(inicio + tamanoPagina, listaCompleta.size());

        listaPagina.clear();
        if (inicio < listaCompleta.size()) {
            listaPagina.addAll(listaCompleta.subList(inicio, fin));
        }

        txtPagina.setText(paginaActual + " / " + totalPaginas);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerService != null) {
            scannerService.disconnect();
        }
    }
}

