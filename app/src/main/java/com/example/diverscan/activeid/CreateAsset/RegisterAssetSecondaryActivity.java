package com.example.diverscan.activeid.CreateAsset;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.activo.RegistroActivoDetailViewModel;
import com.example.diverscan.activeid.sqlite.newAssets;

import java.util.Map;

public class RegisterAssetSecondaryActivity extends AppCompatActivity{
    private AutoCompleteTextView marcaView, modeloView;
    private EditText annoView, capacidadView, detalleEstadoView;
    private Spinner estadoView, estadoConservacionView;
    private Button btnCrear;
    private String[] conservacion = {"Como nuevo", "Normal", "Requiere mantenimiento", "Obsoleto"};

    private String idCompania, nombreCompania, idEdificio, nombreEdificio, idPiso, nombrePiso, idOficina, nombreOficina;
    private String numeroEtiqueta, numeroActivo, serie, descripcion, responsable;

    private RegistroActivoDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_asset_secondary);

        marcaView = findViewById(R.id.txt_marca);
        modeloView = findViewById(R.id.txt_modelo);
        annoView = findViewById(R.id.txt_anno);
        capacidadView = findViewById(R.id.txt_capacidad);
        detalleEstadoView = findViewById(R.id.txt_detalle_estado);
        estadoView = findViewById(R.id.sp_estado);
        estadoConservacionView = findViewById(R.id.sp_estado_conservacion);
        btnCrear = findViewById(R.id.btn_crear_activo);

        viewModel = new ViewModelProvider(this).get(RegistroActivoDetailViewModel.class);
        viewModel.cargarCatalogos(this);

        configurarObservadores();

        estadoConservacionView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, conservacion));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idCompania = extras.getString("idCompania");
            nombreCompania = extras.getString("nombreCompania");
            idEdificio = extras.getString("idEdificio");
            nombreEdificio = extras.getString("nombreEdificio");
            idPiso = extras.getString("idPiso");
            nombrePiso = extras.getString("nombrePiso");
            idOficina = extras.getString("idOficina");
            nombreOficina = extras.getString("nombreOficina");

            numeroEtiqueta = extras.getString("numeroEtiqueta");
            numeroActivo = extras.getString("numeroActivo");
            serie = extras.getString("serie");
            descripcion = extras.getString("descripcion");
            responsable = extras.getString("responsable");
        }

        /*btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearActivo();
            }
        });*/
    }

    private void configurarObservadores() {
        viewModel.getEstados().observe(this, estados -> {
            ArrayAdapter<RegistroActivoDetailViewModel.ComboItem> adapter = 
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados);
            estadoView.setAdapter(adapter);
        });

        viewModel.getMarcas().observe(this, marcas -> {
            ArrayAdapter<RegistroActivoDetailViewModel.ComboItem> adapter = 
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, marcas);
            marcaView.setAdapter(adapter);
        });

        viewModel.getModelos().observe(this, modelos -> {
            ArrayAdapter<RegistroActivoDetailViewModel.ComboItem> adapter = 
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, modelos);
            modeloView.setAdapter(adapter);
        });
    }

    /*private void crearActivo() {
        String marca = marcaView.getText().toString();
        String modelo = modeloView.getText().toString();
        String anno = annoView.getText().toString();
        String capacidad = capacidadView.getText().toString();
        String detalleEstado = detalleEstadoView.getText().toString();

        EntidadAssetStatus estadoSeleccionado = (EntidadAssetStatus) estadoView.getSelectedItem();
        String estado = estadoSeleccionado != null ? estadoSeleccionado.getId() : "";
        String estadoConservacion = estadoConservacionView.getSelectedItem().toString();

        boolean result = new newAssets(this).InsertarActivo(numeroEtiqueta, numeroActivo, descripcion, idCompania,
                nombreCompania, idEdificio, nombreEdificio, idPiso, nombrePiso, idOficina, nombreOficina, "0", "0",
                marca, modelo, serie, "", estado, detalleEstado, estadoConservacion, anno, capacidad);

        if (result) {
            new AlertDialog.Builder(this)
                    .setTitle("Activo creado")
                    .setMessage("¿Desea crear otro activo en esta ubicación?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent repetir = new Intent(RegisterAssetSecondaryActivity.this, RegisterAssetPrimaryActivity.class);
                            repetir.putExtra("idCompania", idCompania);
                            repetir.putExtra("nombreCompania", nombreCompania);
                            repetir.putExtra("idEdificio", idEdificio);
                            repetir.putExtra("nombreEdificio", nombreEdificio);
                            repetir.putExtra("idPiso", idPiso);
                            repetir.putExtra("nombrePiso", nombrePiso);
                            repetir.putExtra("idOficina", idOficina);
                            repetir.putExtra("nombreOficina", nombreOficina);
                            startActivity(repetir);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent inicio = new Intent(RegisterAssetSecondaryActivity.this, SelectLocationActivity.class);
                            startActivity(inicio);
                            finish();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, "Error al crear el activo", Toast.LENGTH_LONG).show();
        }
    }*/
}
