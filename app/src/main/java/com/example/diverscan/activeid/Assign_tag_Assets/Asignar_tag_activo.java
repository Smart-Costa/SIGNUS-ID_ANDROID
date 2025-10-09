package com.example.diverscan.activeid.Assign_tag_Assets;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

import com.example.diverscan.activeid.Conexion.ACTIVEID_API;
import com.loopj.android.http.AsyncHttpResponseHandler;

import com.example.diverscan.activeid.Inventory.EntidadRazonSocial;
//Librerias de consumir el web services
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Asignar_tag_activo extends AppCompatActivity {

    private EditText NumeroactivoView;
    private EditText NumerodeactivoView;
    private EditText PlacaView;
    private EditText DescripcionView;
    private EditText CompaniaView;
    private EditText EdificioView;
    private EditText PisoView;
    private EditText OficinaView;
    private EditText EncargadoView;
    private EditText EPCView;
    private View mAsignartagView;

    Button btn_ver;
    Button btn_continuar;

    AssetsDBHelper AssetsDBHelper;

    private String numeroingresado, Numero, placa, descripcion, compania, edificio, piso, oficina, encargado, Epc, epc;
    ArrayList<EntidadRazonSocial> Razon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_tag_activo);

        controles();
        eventos();
    }

    public void controles(){

        mAsignartagView = findViewById(R.id.AsignarTagForm);

        NumeroactivoView = (EditText) findViewById(R.id.txt_numero);
        NumerodeactivoView = (EditText) findViewById(R.id.txt_numero_activo);
        PlacaView = (EditText) findViewById(R.id.txt_placa);
        DescripcionView = (EditText) findViewById(R.id.txt_descripcion);
        CompaniaView = (EditText) findViewById(R.id.txt_compania);
        EdificioView = (EditText) findViewById(R.id.txt_edificio);
        PisoView = (EditText) findViewById(R.id.txt_piso);
        OficinaView = (EditText) findViewById(R.id.txt_oficina);
        EncargadoView = (EditText) findViewById(R.id.txt_encargado);
        EPCView = (EditText) findViewById(R.id.txt_epc);

        AssetsDBHelper = new AssetsDBHelper(mAsignartagView.getContext());

        btn_ver = (Button) findViewById(R.id.btn_ver);
        btn_continuar = (Button) findViewById(R.id.btn_continuar);
    }

    public void eventos (){

        btn_ver.setOnClickListener(OnClickListenerVerActivo);
        btn_continuar.setOnClickListener(OnClickListenerContinuar);
    }

    public void VerActivo (View view) {

        numeroingresado = NumeroactivoView.getText().toString();

        if (TextUtils.isEmpty(numeroingresado)) {

            NumeroactivoView.setError(getString(R.string.barcode_required));

        }else {

            Cursor cursor = AssetsDBHelper.VerActivo(numeroingresado);

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                NumerodeactivoView.setText(cursor.getString(cursor.getColumnIndex("Numero")));
                PlacaView.setText(cursor.getString(cursor.getColumnIndex("Numero")));
                DescripcionView.setText(cursor.getString(cursor.getColumnIndex("Descripcion")));
                CompaniaView.setText(cursor.getString(cursor.getColumnIndex("Compania")));
                EdificioView.setText(cursor.getString(cursor.getColumnIndex("Edificio")));
                PisoView.setText(cursor.getString(cursor.getColumnIndex("Piso")));
                OficinaView.setText(cursor.getString(cursor.getColumnIndex("Oficina")));
                EncargadoView.setText(cursor.getString(cursor.getColumnIndex("Alias")));
                EPCView.setText(cursor.getString(cursor.getColumnIndex("Tag")));

                NumerodeactivoView.setEnabled(false);
                PlacaView.setEnabled(false);
                DescripcionView.setEnabled(false);
                CompaniaView.setEnabled(false);
                EdificioView.setEnabled(false);
                PisoView.setEnabled(false);
                OficinaView.setEnabled(false);
                EncargadoView.setEnabled(false);
                EPCView.setEnabled(false);

                if (!cursor.isClosed()) {
                    cursor.close();
                }

                } else if (cursor.getCount() == 0) {

                Toast.makeText(getApplicationContext(), "El Activo ingresado no existe!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void IrGrabar(View view){

        epc = EPCView.getText().toString();

            if(TextUtils.isEmpty(epc)){

                CargarDatosenGrabar();

                Intent grabar = new Intent(Asignar_tag_activo.this,GrabarTagActivo.class);
                startActivity(grabar);

            }else{

                DialogEpc();
        }
    }

    public void DialogContinue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mAsignartagView.getContext());

        builder.setTitle("Alerta").setMessage("Este activo ya tiene un EPC asignado. ¿Desea continuar?")
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                CargarDatosenGrabar();

                                Intent newform = new Intent(Asignar_tag_activo.this,GrabarTagActivo.class);
                                startActivity(newform);
                            }
                        })
                 .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });
    }

    public void DialogEpc()
    {

        AlertDialog.Builder d = new AlertDialog.Builder(mAsignartagView.getContext());

        d.setTitle("Asignar EPC al activo");
        LinearLayout layout = new LinearLayout(mAsignartagView.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        TextView tv_dialog = new TextView(mAsignartagView.getContext());
        tv_dialog.setText("Este activo ya tiene un EPC asignado. ¿Desea continuar?");
        layout.addView(tv_dialog);
        d.setView(layout);
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.cancel();
            }
        });

        d.setNeutralButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {

                CargarDatosenGrabar();

                Intent newform = new Intent(Asignar_tag_activo.this,GrabarTagActivo.class);
                startActivity(newform);
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void CargarDatosenGrabar(){

        numeroingresado = NumeroactivoView.getText().toString();
        descripcion = DescripcionView.getText().toString();
        compania = CompaniaView.getText().toString();
        edificio = EdificioView.getText().toString();
        piso = PisoView.getText().toString();
        oficina = OficinaView.getText().toString();
        epc = EPCView.getText().toString();

        Intent intent = new Intent(Asignar_tag_activo.this, GrabarTagActivo.class);
        intent.putExtra("NumerodeactivoView", numeroingresado);
        intent.putExtra("DescripcionView", descripcion);
        intent.putExtra("CompaniaView", compania);
        intent.putExtra("EdificioView", edificio);
        intent.putExtra("PisoView", piso);
        intent.putExtra("OficinaView", oficina);
        intent.putExtra("EPCView", epc);
        startActivity(intent);
    }

    private Button.OnClickListener OnClickListenerVerActivo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            VerActivo(mAsignartagView);
            //getRazones();

        }};

    private Button.OnClickListener OnClickListenerContinuar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            IrGrabar(mAsignartagView);
        }};

    //PARTE DE CONSUMIR UN WEB SERVICES
    public void getRazones(){
        ACTIVEID_API activeid_api = new ACTIVEID_API();
        try {
            JSONObject jsonObject = new JSONObject();
            //  jsonObject.put("idperfilusuario", "");
            StringEntity entity = new StringEntity(jsonObject.toString());
            activeid_api.post(mAsignartagView.getContext(), "/ObtenerRazones",entity,new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                      deserializeRazones(new String(responseBody));

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Toast.makeText(mAsignartagView.getContext(), "Error al conectar a la API " +
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    String axu="";
                }
            });


        } catch (UnsupportedEncodingException  e) {
            e.printStackTrace();
        }
    }

    public void deserializeRazones(String response) {
        try {

            Razon = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("ObtenerRazonSocialResult");
            //esto solo funciona si el web services devuelve una lista

            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject RazonEncontrados = jsonArray.getJSONObject(i);
                Razon.add(
                        new EntidadRazonSocial(RazonEncontrados.getString("IdRazon"),RazonEncontrados.getString("NombreRazon")));
            }
            Toast.makeText(mAsignartagView.getContext(), "Resultado " +
                    "Fue exitoso", Toast.LENGTH_LONG).show();
        }
        catch (JSONException e){
            Log.w("myApp", "Error 21 " +e.toString()+ " "+e.getStackTrace());
        }
    }



}