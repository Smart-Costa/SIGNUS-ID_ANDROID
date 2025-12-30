package com.example.diverscan.activeid.UI.activo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;

public class RegistroActivoDetailActivity extends AppCompatActivity {

    private EditText txtNumeroActivo, txtNumeroEtiqueta, txtDescripcionCorta;
    private AutoCompleteTextView spCategoria, spEstado, spEmpresa, spMarca, spModelo;
    private Button btnGuardarDetail;
    private RegistroActivoDetailViewModel viewModel;

    private String ubicacionA, ubicacionB, ubicacionC, ubicacionD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_activo_detail);

        inicializarVistas();
        viewModel = new ViewModelProvider(this).get(RegistroActivoDetailViewModel.class);

        SharedPreferences prefs = getSharedPreferences("RegistroActivo", MODE_PRIVATE);
        ubicacionA = prefs.getString("UbicacionA", "");
        ubicacionB = prefs.getString("UbicacionB", "");
        ubicacionC = prefs.getString("UbicacionC", "");
        ubicacionD = prefs.getString("UbicacionD", "");

        viewModel.cargarCatalogos(this);

        viewModel.getCategorias().observe(this, categorias -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
            spCategoria.setAdapter(adapter);
        });

        viewModel.getEstados().observe(this, estados -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados);
            spEstado.setAdapter(adapter);
        });

        viewModel.getEmpresas().observe(this, empresas -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, empresas);
            spEmpresa.setAdapter(adapter);
        });

        viewModel.getMarcas().observe(this, marcas -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, marcas);
            spMarca.setAdapter(adapter);
        });

        viewModel.getModelos().observe(this, modelos -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, modelos);
            spModelo.setAdapter(adapter);
        });

        btnGuardarDetail.setOnClickListener(v -> guardarActivo());
    }

    private void inicializarVistas() {
        txtNumeroActivo = findViewById(R.id.txtNumeroActivo);
        txtNumeroEtiqueta = findViewById(R.id.txtNumeroEtiqueta);
        txtDescripcionCorta = findViewById(R.id.txtDescripcionCorta);
        spCategoria = findViewById(R.id.spCategoria);
        spEstado = findViewById(R.id.spEstado);
        spEmpresa = findViewById(R.id.spEmpresa);
        spMarca = findViewById(R.id.spMarca);
        spModelo = findViewById(R.id.spModelo);
        btnGuardarDetail = findViewById(R.id.btnGuardarParte2);
    }

    private void guardarActivo() {
        String numeroActivo = txtNumeroActivo.getText().toString();
        String etiqueta = txtNumeroEtiqueta.getText().toString();
        String descripcion = txtDescripcionCorta.getText().toString();
        String categoria = spCategoria.getText().toString();
        String estado = spEstado.getText().toString();
        String empresa = spEmpresa.getText().toString();
        String marca = spMarca.getText().toString();
        String modelo = spModelo.getText().toString();

        if (numeroActivo.isEmpty() || etiqueta.isEmpty() || categoria.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("RegistroActivo", MODE_PRIVATE);
        String idActivo = prefs.getString("idActivo", java.util.UUID.randomUUID().toString());

        ActivoEntity activo = new ActivoEntity();
        activo.setIdActivo(idActivo);
        activo.setNumeroActivo(numeroActivo);
        activo.setNumeroEtiqueta(etiqueta);
        activo.setDescripcionCorta(descripcion);
        activo.setCategoria(categoria);
        activo.setEstado(estado);
        activo.setEmpresa(empresa);
        activo.setMarca(marca);
        activo.setModelo(modelo);
        activo.setUbicacionA(ubicacionA);
        activo.setUbicacionB(ubicacionB);
        activo.setUbicacionC(ubicacionC);
        activo.setUbicacionD(ubicacionD);

        viewModel.guardarActivo(this, activo);

        prefs.edit()
                .putString("idActivo", idActivo)
                .putString("NumeroActivo", numeroActivo)
                .putString("NumeroEtiqueta", etiqueta)
                .putString("Descripcion", descripcion)
                .putString("Categoria", categoria)
                .putString("Estado", estado)
                .putString("Empresa", empresa)
                .putString("Marca", marca)
                .putString("Modelo", modelo)
                .apply();

        Intent intent = new Intent(this, RegistroActivoFotoTagActivity.class);
        intent.putExtra("idActivo", idActivo);
        startActivity(intent);
        finish();
    }
}
