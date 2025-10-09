package com.example.diverscan.activeid.Inventory;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import java.util.ArrayList;
import java.util.List;


public class Elegirubicacion_manualtoma  extends AppCompatActivity {

    Spinner spinnerRazonView;
    Spinner spinnerEdificioView;
    Spinner spinnerPisoView;
    Spinner spinneroficinaView;
    Spinner spinnerTiposInventariosView;

    OfficesDBHelper OfficesDBHelper;
    InventoryDBHelper InventoryDBHelper;

    Button btn_continuar;

    ArrayList<EntidadRazonSocial>  listSpinnerRazon = new  ArrayList<EntidadRazonSocial>();
    ArrayList<EntidadEdificios> listSpinnerEdificios = new ArrayList<EntidadEdificios>();
    //ArrayList<EntidadPisos> listSpinnerPisos = new ArrayList<EntidadPisos>();
    //ArrayList<EntidadOficinas> listSpinnerOficina = new ArrayList<EntidadOficinas>();
    ArrayList<EntidadTiposInventarios> listSpinnerTipo = new ArrayList<EntidadTiposInventarios>();

    private View ElegirUbicaciónManualView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_ubicacion_manual_toma);

        controles();
        eventos();

        ElegirUbicaciónManualView.post(Load);
    }

    public void controles(){
        spinnerRazonView = (Spinner) findViewById(R.id.spinner_razon);
        spinnerEdificioView = (Spinner) findViewById(R.id.spinner_edificio);
        spinnerPisoView = (Spinner) findViewById(R.id.spinner_piso);
        spinneroficinaView = (Spinner) findViewById(R.id.spinner_oficina);
        spinnerTiposInventariosView = (Spinner) findViewById(R.id.spinner_tipo);

        ElegirUbicaciónManualView = findViewById(R.id.elegirubicacionmanual);

        OfficesDBHelper = new OfficesDBHelper(ElegirUbicaciónManualView.getContext());
        InventoryDBHelper = new InventoryDBHelper(ElegirUbicaciónManualView.getContext());

        btn_continuar = (Button) findViewById(R.id.btn_continua);
    }




    public void eventos(){

        btn_continuar.setOnClickListener(OnClickListenerContinuar);
        spinnerRazonView.setOnItemSelectedListener(OnItemSelectedListenerRazon);
        spinnerEdificioView.setOnItemSelectedListener(OnItemSelectedListenerEdificio);
    }

    Runnable Load = new Runnable(){
        @Override
        public void run() {

            ListarTipos();
            ListarRazon();

        }
    };


    public void ListarRazon(){

        Cursor cursor = OfficesDBHelper.listarRazonSocial();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

            EntidadRazonSocial razon=new EntidadRazonSocial(cursor.getString(cursor
                    .getColumnIndex("id")),cursor.getString(cursor
                    .getColumnIndex("NombreRazon")));
            listSpinnerRazon.add(razon);
        }
        ArrayAdapter spinnerAdapter = new ArrayAdapter(ElegirUbicaciónManualView.getContext(),android.R.layout.simple_spinner_dropdown_item,listSpinnerRazon);
        spinnerRazonView.setAdapter(spinnerAdapter);

        if (!cursor.isClosed()) {
            cursor.close();
        }
    }


    public void ListarEdificioporRazon (int idRazon) {

        int IdRazon = idRazon;

        Cursor cursor = OfficesDBHelper.listarEdificioporRazon(IdRazon);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

            EntidadEdificios edificios = new EntidadEdificios(cursor.getString(cursor
                    .getColumnIndex("id")),cursor.getString(cursor
                    .getColumnIndex("NombreEdificio")),cursor.getString(cursor
                    .getColumnIndex("idRazon")), cursor.getString(cursor
                    .getColumnIndex("Razon")));

            listSpinnerEdificios.add(edificios);
        }
        ArrayAdapter spinnerAdapter = new ArrayAdapter(ElegirUbicaciónManualView.getContext(),android.R.layout.simple_spinner_dropdown_item,listSpinnerEdificios);
        spinnerEdificioView.setAdapter(spinnerAdapter);

        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    public void ListarTipos(){

        Cursor cursor = InventoryDBHelper.ListarTipoInventario();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

            EntidadTiposInventarios tipos = new EntidadTiposInventarios(cursor.getString(cursor
                    .getColumnIndex("id")), cursor.getString(cursor
                    .getColumnIndex("NombreTipo")),
                    cursor.getString(cursor.getColumnIndex("Descripcion")));
            listSpinnerTipo.add(tipos);
        }

        ArrayAdapter spinnerAdapter = new ArrayAdapter(ElegirUbicaciónManualView.getContext(),android.R.layout.simple_spinner_dropdown_item,listSpinnerTipo);
        spinnerTiposInventariosView.setAdapter(spinnerAdapter);

        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    //Evento boton Continuar
    private Button.OnClickListener OnClickListenerContinuar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }};

    //Evento Spinner Razon
    private AdapterView.OnItemSelectedListener OnItemSelectedListenerRazon=new  AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int posicionRazon = position;
            Toast.makeText(ElegirUbicaciónManualView.getContext(), "La posicion es  " + posicionRazon, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //Evento Spinner Edificio
    private AdapterView.OnItemSelectedListener OnItemSelectedListenerEdificio=new  AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int posicionedificio = position;
            Toast.makeText(ElegirUbicaciónManualView.getContext(), "La posicion es  " + posicionedificio, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

}