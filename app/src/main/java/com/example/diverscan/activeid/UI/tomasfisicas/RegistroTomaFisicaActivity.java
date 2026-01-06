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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_toma_fisica);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargarTomasFisicas();
        }
    }

    private void buscar() {
        String nombre = txtBusquedaNombre.getText().toString().trim();

        if (nombre.isEmpty()) {
            viewModel.cargarTomasFisicas();
            return;
        }

        viewModel.buscarTomasFisicasPorNombre(nombre);
    }

    private void cargarPagina() {
        if (listaCompleta == null || listaCompleta.isEmpty()) {
            listaPagina = new ArrayList<>();
            adapter.update(listaPagina);
            txtPagina.setText("0/0");
            return;
        }

        totalPaginas = (int) Math.ceil((double) listaCompleta.size() / tamanoPagina);
        if (totalPaginas < 1) totalPaginas = 1;
        if (paginaActual < 1) paginaActual = 1;
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;

        int inicio = (paginaActual - 1) * tamanoPagina;
        int fin = Math.min(inicio + tamanoPagina, listaCompleta.size());

        listaPagina = new ArrayList<>(listaCompleta.subList(inicio, fin));
        adapter.update(listaPagina);
        txtPagina.setText(paginaActual + " / " + totalPaginas);
    }
}

