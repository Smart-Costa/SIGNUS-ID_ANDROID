package com.example.diverscan.activeid.CreateAsset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.diverscan.activeid.Edificio.EdificioDBHelper;
import com.example.diverscan.activeid.Edificio.EdificioNuevo;
import com.example.diverscan.activeid.Oficina.OficinaDBHelper;
import com.example.diverscan.activeid.Oficina.oficinaNuevo;
import com.example.diverscan.activeid.Piso.PisoDBHelper;
import com.example.diverscan.activeid.Piso.PisoNuevo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.RazonSocial.RazonNuevo;
import com.example.diverscan.activeid.RazonSocial.RazonSocialDBHelper;

import java.util.Map;

public class SelectLocationActivity extends AppCompatActivity{
    private Spinner companiaSpinner, edificioSpinner, pisoSpinner, oficinaSpinner;
    private Map<Integer, RazonNuevo> mapCompanias;
    private Map<Integer, EdificioNuevo> mapEdificios;
    private Map<Integer, PisoNuevo> mapPisos;
    private Map<Integer, oficinaNuevo> mapOficinas;

    private RazonSocialDBHelper companiaHelper;
    private EdificioDBHelper edificioHelper;
    private PisoDBHelper pisoHelper;
    private OficinaDBHelper oficinaHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        companiaSpinner = findViewById(R.id.compania_crea);
        edificioSpinner = findViewById(R.id.edificio_crea);
        pisoSpinner = findViewById(R.id.piso_crea);
        oficinaSpinner = findViewById(R.id.oficina_crea);

        companiaHelper = new RazonSocialDBHelper(this);
        edificioHelper = new EdificioDBHelper(this);
        pisoHelper = new PisoDBHelper(this);
        oficinaHelper = new OficinaDBHelper(this);

        cargarCompanias();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        companiaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RazonNuevo compania = (RazonNuevo) companiaSpinner.getSelectedItem();
                cargarEdificios(compania.getIdRazon());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        edificioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EdificioNuevo edificio = (EdificioNuevo) edificioSpinner.getSelectedItem();
                cargarPisos(edificio.getIdEdificio());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        pisoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PisoNuevo piso = (PisoNuevo) pisoSpinner.getSelectedItem();
                cargarOficinas(piso.getIdPiso());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btn_siguiente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarUbicacion()) {
                    RazonNuevo compania = (RazonNuevo) companiaSpinner.getSelectedItem();
                    EdificioNuevo edificio = (EdificioNuevo) edificioSpinner.getSelectedItem();
                    PisoNuevo piso = (PisoNuevo) pisoSpinner.getSelectedItem();
                    oficinaNuevo oficina = (oficinaNuevo) oficinaSpinner.getSelectedItem();

                    Intent intent = new Intent(SelectLocationActivity.this, RegisterAssetPrimaryActivity.class);
                    intent.putExtra("idCompania", compania.getIdRazon());
                    intent.putExtra("nombreCompania", compania.getNombreRazon());
                    intent.putExtra("idEdificio", edificio.getIdEdificio());
                    intent.putExtra("nombreEdificio", edificio.getNombreEdificio());
                    intent.putExtra("idPiso", piso.getIdPiso());
                    intent.putExtra("nombrePiso", piso.getNombrePiso());
                    intent.putExtra("idOficina", oficina.getIdOficina());
                    intent.putExtra("nombreOficina", oficina.getNombreOficina());
                    startActivity(intent);
                } else {
                    Toast.makeText(SelectLocationActivity.this, "Debe seleccionar todos los campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validarUbicacion() {
        return companiaSpinner.getSelectedItem() != null &&
                edificioSpinner.getSelectedItem() != null &&
                pisoSpinner.getSelectedItem() != null &&
                oficinaSpinner.getSelectedItem() != null;
    }

    private void cargarCompanias() {
        mapCompanias = companiaHelper.ObtenerRazon();
        RazonNuevo[] items = mapCompanias.values().toArray(new RazonNuevo[0]);
        companiaSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layaout, items));
    }

    private void cargarEdificios(String idCompania) {
        mapEdificios = edificioHelper.ObtenerEdificio(idCompania);
        EdificioNuevo[] items = mapEdificios.values().toArray(new EdificioNuevo[0]);
        edificioSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layaout, items));
    }

    private void cargarPisos(String idEdificio) {
        mapPisos = pisoHelper.ObtenerPiso(idEdificio);
        PisoNuevo[] items = mapPisos.values().toArray(new PisoNuevo[0]);
        pisoSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layaout, items));
    }

    private void cargarOficinas(String idPiso) {
        mapOficinas = oficinaHelper.ObtenerOficina(idPiso);
        oficinaNuevo[] items = mapOficinas.values().toArray(new oficinaNuevo[0]);
        oficinaSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layaout, items));
    }
}
