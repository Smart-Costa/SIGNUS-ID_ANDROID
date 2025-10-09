package com.example.diverscan.activeid.Activo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.Locate_Assets.Actualizar_activo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

import java.util.ArrayList;

public class activoPorDescripcion extends AppCompatActivity {
    private EditText DescripcionActivo;
    private TextView descriptionView;
    private ListView listDescriptions;
    private View mActivoDecripcion;

    ArrayList<EntidadActivos> activoRecords = new ArrayList<EntidadActivos>();

    Button btnVer;

    private String txtDescripcion;

    AssetsDBHelper assetsDBHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busquedadescripcion);

        listDescriptions = (ListView) findViewById(R.id.listView);

        controles();
        eventos();
    }

    public void controles()
    {
        mActivoDecripcion = findViewById(R.id.DescripcionForm);
        // DescripcionActivo = (EditText) findViewById(R.id.txt_DescripcionActivo);
        assetsDBHelper = new AssetsDBHelper(mActivoDecripcion.getContext());
        // btnVer = (Button) findViewById(R.id.btn_VerActivo);
    }

    public void eventos()
    {
        btnVer.setOnClickListener(OnClickListenerVerActivo);
    }

    public void CargarActivos(View view)
    {
        txtDescripcion = DescripcionActivo.getText().toString();

        if(TextUtils.isEmpty(txtDescripcion))
        {
            DescripcionActivo.setError("Debe ingresar una descripción correcta");
        }
        else
        {
            Cursor cursor = assetsDBHelper.TraerActivos(txtDescripcion);
            int count = cursor.getCount();
            if (count != 0)
            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                {
                    String _id = cursor.getString(cursor.getColumnIndex("_id"));
                    String numero = cursor.getString(cursor.getColumnIndex("Numero"));
                    String placa = cursor.getString(cursor.getColumnIndex("CodeBar"));
                    String descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
                    String idCompania = cursor.getString(cursor.getColumnIndex("IdCompania"));
                    String compania = cursor.getString(cursor.getColumnIndex("Compania"));
                    String idEdicio = cursor.getString(cursor.getColumnIndex("IdEdificio"));
                    String edificio = cursor.getString(cursor.getColumnIndex("Edificio"));
                    String idPiso = cursor.getString(cursor.getColumnIndex("IdPiso"));
                    String piso = cursor.getString(cursor.getColumnIndex("Piso"));
                    String idOficina = cursor.getString(cursor.getColumnIndex("IdOficina"));
                    String oficina = cursor.getString(cursor.getColumnIndex("Oficina"));
                    String encargado = cursor.getString(cursor.getColumnIndex("Alias"));
                    String marca = cursor.getString(cursor.getColumnIndex("Marca"));
                    String modelo = cursor.getString(cursor.getColumnIndex("Modelo"));
                    String serie = cursor.getString(cursor.getColumnIndex("Serial"));
                    String epc = cursor.getString(cursor.getColumnIndex("Tag"));
                    String IdCategoria = cursor.getString(cursor.getColumnIndex("IdCategoria"));
                    String employeeRelated = cursor.getString(cursor.getColumnIndex("EmployeeRelatedSysId"));
                    String assetStatusSysId = cursor.getString(cursor.getColumnIndex("AssetStatusSysId"));
                    String parentAssetSysId = cursor.getString(cursor.getColumnIndex("parentAssetSysId"));
                    String anoFabricacion = cursor.getString(cursor.getColumnIndex("AnoFabricacion"));
                    String capacidad = cursor.getString(cursor.getColumnIndex("Capacidad"));
                    String DetalleEstado = cursor.getString(cursor.getColumnIndex("EstadoDescripcion"));
                    String EstadoConservacion = cursor.getString(cursor.getColumnIndex("EstadoConservacion"));


                    EntidadActivos entidadActivos = new EntidadActivos(_id, descripcion, compania,
                            idCompania, edificio, idEdicio, piso, idPiso, oficina, idOficina, epc,
                            numero, placa, marca, modelo, serie, encargado,IdCategoria, employeeRelated,
                            assetStatusSysId, parentAssetSysId, anoFabricacion, capacidad, DetalleEstado,
                            EstadoConservacion);
                    activoRecords.add(entidadActivos);
                }
                inflateListViewActivos(activoRecords);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "El activo no existe", Toast.LENGTH_LONG).show();
            }
            if(!cursor.isClosed())
            {
                cursor.close();
            }
        }
    }
    //**********************************************************************************************
    public void inflateListViewActivos(ArrayList<EntidadActivos> response)
    {
        ItemAdapterAssets itemAdapterAssets = new ItemAdapterAssets(this, response);
        int aux = itemAdapterAssets.getCount();
        listDescriptions.setAdapter(itemAdapterAssets);
        listDescriptions.setOnItemClickListener(ObtenerActivo);
    }
    //**********************************************************************************************
    private ListView.OnItemClickListener ObtenerActivo= new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
        {
            Intent intent = new Intent(activoPorDescripcion.this, Actualizar_activo.class);
            intent.putExtra("_id", activoRecords.get(position).getIdActivo());
            intent.putExtra("descripcion", activoRecords.get(position).getDescripcion());
            intent.putExtra("idCompania", activoRecords.get(position).getIdCompania());
            intent.putExtra("Compania", activoRecords.get(position).getCompania());
            intent.putExtra("idEdificio", activoRecords.get(position).getIdEdificio());
            intent.putExtra("Edificio", activoRecords.get(position).getEdificio());
            intent.putExtra("idPiso", activoRecords.get(position).getIdPiso());
            intent.putExtra("Piso", activoRecords.get(position).getPiso());
            intent.putExtra("idOficina", activoRecords.get(position).getIdOficina());
            intent.putExtra("Oficina", activoRecords.get(position).getOficina());
            intent.putExtra("EPC", activoRecords.get(position).getTag());
            intent.putExtra("numero", activoRecords.get(position).getNumero());
            intent.putExtra("placa", activoRecords.get(position).getCodeBar());
            intent.putExtra("marca", activoRecords.get(position).getMarca());
            intent.putExtra("modelo", activoRecords.get(position).getModelo());
            intent.putExtra("serie", activoRecords.get(position).getSerial());
            intent.putExtra("encargado", activoRecords.get(position).getAlias());
            startActivity(intent);
        }
    };
    //*******************************************************************************************
    private Button.OnClickListener OnClickListenerVerActivo = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            CargarActivos(mActivoDecripcion);
        }
    };
    //*******************************************************************************************
}