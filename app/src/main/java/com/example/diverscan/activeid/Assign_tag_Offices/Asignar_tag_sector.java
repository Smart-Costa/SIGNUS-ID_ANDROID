package com.example.diverscan.activeid.Assign_tag_Offices;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;

import java.util.ArrayList;

public class Asignar_tag_sector extends AppCompatActivity {

    private EditText NombreSectorView;
    private TextView sectorView;
    private TextView edificioView;
    private TextView paisView;
    private View mAsignartagSectorView;
    private ListView listSectores;

    ArrayList<Offices> offices = new ArrayList<Offices>();

    Button btn_ver;

    private String Nombresector;

    OfficesDBHelper OfficesDBHelper;
    Activity myActivity;

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_tag_sector);

        listSectores = (ListView) findViewById(R.id.listView);

        controles();
        eventos();

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(Asignar_tag_sector.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
    }
    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }
    public void controles (){

        mAsignartagSectorView = findViewById(R.id.AsignarTagSectorForm);
        NombreSectorView = (EditText) findViewById(R.id.txt_NombreSector);
        OfficesDBHelper = new OfficesDBHelper(mAsignartagSectorView.getContext());
        btn_ver = (Button) findViewById(R.id.btn_VerSector);
    }

    public void eventos (){

        btn_ver.setOnClickListener(OnClickListenerVerSector);

    }

    public void Cargaroffices(View view) {

        Nombresector = NombreSectorView.getText().toString();

        if (TextUtils.isEmpty(Nombresector)) {

            NombreSectorView.setError(getString(R.string.sector_required));

        } else {

            // Carga del cursor
            Cursor cursor = OfficesDBHelper.TraerSectores(Nombresector);

            //Pasar cursor a ArrayList
            int count =cursor.getCount();

            if (count != 0){

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){

                    String nombreOficina = cursor.getString(cursor.getColumnIndex("oficinaNombre"));
                    String piso = cursor.getString(cursor.getColumnIndex("pisoNombre"));
                    String Edificio = cursor.getString(cursor.getColumnIndex("edificioNombre"));
                    String RazonSocial = cursor.getString(cursor.getColumnIndex("razonNombre"));
                    String Tag = cursor.getString(cursor.getColumnIndex("EPC"));
                    String idOficina = cursor.getString(cursor.getColumnIndex("idOficina"));
                    String codigo = cursor.getString(cursor.getColumnIndex("Piso"));
                    Offices oficina=new Offices(codigo +" - "+nombreOficina,piso,Edificio,RazonSocial,Tag, idOficina);
                    offices.add(oficina);
                }

                inflateListViewOffices(offices);

            }else {

                Toast.makeText(getApplicationContext(), "El Sector ingresado no existe!", Toast.LENGTH_LONG).show();
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void inflateListViewOffices(ArrayList<Offices> response) {

        //version 2 ItemAdapterOfficesV2
        ItemAdapterOfficesV2 adapter = new ItemAdapterOfficesV2(this, response);
        int aux=adapter.getCount();
        listSectores.setAdapter(adapter);
        listSectores.setOnItemClickListener(ObtenerSector);

        /*//Version 1  ItemAdapterOffices
            ItemAdapterOffices adapter = new ItemAdapterOffices(
            (LayoutInflater) mAsignartagSectorView.getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE),
            response, Asignar_tag_sector.this);
            this.listView.setAdapter(adapter);*/
    }


    private Button.OnClickListener OnClickListenerVerSector = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            offices.clear();
            Cargaroffices(mAsignartagSectorView);
        }};


    private ListView.OnItemClickListener ObtenerSector = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3){

           Intent intent = new Intent(Asignar_tag_sector.this, Grabar_Tag_Sector.class);
            intent.putExtra("Dato_oficina", offices.get(position).getOficina());
            intent.putExtra("Dato_piso", offices.get(position).getPiso());
            intent.putExtra("Dato_edificio", offices.get(position).getEdificio());
            intent.putExtra("Dato_epc", offices.get(position).getTag());
            intent.putExtra("Dato_IdOficina", offices.get(position).getIdOficina());
            startActivity(intent);
        }
    };
}
