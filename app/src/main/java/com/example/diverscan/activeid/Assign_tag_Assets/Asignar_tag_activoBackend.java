package com.example.diverscan.activeid.Assign_tag_Assets;

import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

public class Asignar_tag_activoBackend {

    EditText NumeroActivo;
    EditText NumerodeActivo;
    EditText Placa;
    EditText Descripcion;
    EditText Compania;
    EditText Edificio;
    EditText Piso;
    EditText Oficina;
    EditText Encargado;
    EditText Epc;
    View v1;
    AssetsDBHelper AssetsDBHelper;
    String numeroingresa;

 public Asignar_tag_activoBackend (EditText numeroactivo, EditText numerodeactivo, EditText placa, EditText descripcion, EditText compania, EditText edificio, EditText piso, EditText oficina, EditText encargado, EditText epc, View V, AssetsDBHelper assetsDBHelper ){

     NumeroActivo =  numeroactivo;
     NumerodeActivo = numerodeactivo;
     Placa = placa;
     Descripcion = descripcion;
     Compania = compania;
     Edificio = edificio;
     Piso = piso;
     Oficina = oficina;
     Encargado = encargado;
     Epc = epc;
     v1 = V;
     AssetsDBHelper = assetsDBHelper;

 }
    public Button.OnClickListener OnClickListenerVerActivo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            VerActivo();
        }};

    public void VerActivo () {

        numeroingresa = NumeroActivo.getText().toString();

        Cursor cursor = AssetsDBHelper.VerActivo(numeroingresa);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();

            NumerodeActivo.setText(cursor.getString(cursor.getColumnIndex("Numero")));
            Placa.setText(cursor.getString(cursor.getColumnIndex("Numero")));
            Descripcion.setText(cursor.getString(cursor.getColumnIndex("Descripcion")));
            Compania.setText(cursor.getString(cursor.getColumnIndex("Compania")));
            Edificio.setText(cursor.getString(cursor.getColumnIndex("Edificio")));
            Piso.setText(cursor.getString(cursor.getColumnIndex("Piso")));
            Oficina.setText(cursor.getString(cursor.getColumnIndex("Oficina")));
            Encargado.setText(cursor.getString(cursor.getColumnIndex("")));
            Epc.setText(cursor.getString(cursor.getColumnIndex("")));

            if (!cursor.isClosed()) {
                cursor.close();

                Toast.makeText(v1.getContext(), "Successfully saved!", Toast.LENGTH_LONG).show();

            }

        } else {

                Toast.makeText(v1.getContext(), "bad saved!", Toast.LENGTH_LONG).show();

        }
    }


}
