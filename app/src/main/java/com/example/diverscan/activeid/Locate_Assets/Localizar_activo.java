package com.example.diverscan.activeid.Locate_Assets;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.diverscan.activeid.Assign_tag_Offices.Asignar_tag_sector;
import com.example.diverscan.activeid.Assign_tag_Offices.Grabar_Tag_Sector;
import com.example.diverscan.activeid.Assign_tag_Offices.Offices;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

import java.util.ArrayList;

public class Localizar_activo extends AppCompatActivity {

    private EditText NumIngresadoView;
    private EditText NumActivoView;
    private EditText DescripcionView;
    private EditText RazonView;
    private EditText EdificioView;
    private EditText PisoView;
    private EditText OficinaView;
    private EditText TagView;
    private View mLocateAssetsView;
    private ListView listActivos;

    ArrayList<Assets> assets = new ArrayList<Assets>();

    Button Btn_ver;

    private String NumIngresado;

    AssetsDBHelper AssetsDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizar_activo);

        listActivos = (ListView) findViewById(R.id.listViewLocate);

        controles();
        eventos();
    }

    private void controles (){

        mLocateAssetsView = findViewById(R.id.LocateAssetForm);

        NumIngresadoView = (EditText) findViewById(R.id.txt_Numeroactivo);
        Btn_ver = (Button) findViewById(R.id.btn_Veractivo);

        AssetsDBHelper = new AssetsDBHelper(mLocateAssetsView.getContext());

    }

    private void eventos(){

        Btn_ver.setOnClickListener(OnClickListenerVerActivo);

    }

    public void VerActivos(View view){

        NumIngresado = NumIngresadoView.getText().toString();

        if (TextUtils.isEmpty(NumIngresado)){

            NumIngresadoView.setError(getString(R.string.num_required));

         } else {

            // Carga del cursor
            Cursor cursor = AssetsDBHelper.ConsultarActivosLocate(NumIngresado);

            int count = cursor.getCount();

            if (count != 0){

                //Recorrer el cursor
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

                     String NumeroActivo = cursor.getString(cursor.getColumnIndex("NumeroActivo"));
                     String Descripcion = cursor.getString(cursor.getColumnIndex("Descripcion"));
                     String RazonSocial = cursor.getString(cursor.getColumnIndex("RazonSocial"));
                     String Edificio = cursor.getString(cursor.getColumnIndex("Edificio"));
                     String Piso = cursor.getString(cursor.getColumnIndex("Piso"));
                     String Oficina = cursor.getString(cursor.getColumnIndex("Oficina"));
                     String Tag = cursor.getString(cursor.getColumnIndex("Tag"));

                     Assets Activos = new Assets(NumeroActivo, Descripcion,RazonSocial,Edificio,Piso, Oficina, Tag );
                     assets.add(Activos);
                }

                inflateListViewAssets(assets);

            }else{

                Toast.makeText(getApplicationContext(), "El Activo ingresado no existe!", Toast.LENGTH_LONG).show();
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void inflateListViewAssets (ArrayList<Assets> response){

        ItemAdapterAssets adapter = new ItemAdapterAssets(this, response);
        int aux=adapter.getCount();

        listActivos.setAdapter(adapter);
        listActivos.setOnItemClickListener(ObtenerSector);
    }

    private ListView.OnItemClickListener ObtenerSector = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3){

            Intent intent = new Intent(Localizar_activo.this, Localizacion_activo.class);
            intent.putExtra("Dato_Tag", assets.get(position).getTag());
            startActivity(intent);
        }
    };

    private Button.OnClickListener OnClickListenerVerActivo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            VerActivos(mLocateAssetsView);
        }};
}
