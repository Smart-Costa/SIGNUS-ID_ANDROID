package com.example.diverscan.activeid.Oficina;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.diverscan.activeid.Activo.ActivoInventario;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;

import java.util.ArrayList;

public class MostrarActivosSector extends AppCompatActivity {

    private View UbicacionFinalView;

    private RecyclerView ListaLectura;
    private ArrayList<ActivoInventario> activoInventarios = new ArrayList<ActivoInventario>();


    com.example.diverscan.activeid.sqlite.OfficesDBHelper OfficesDBHelper;
    com.example.diverscan.activeid.sqlite.AssetsDBHelper AssetsDBHelper;

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activos_sector);

    /*  Líneas para personalizar el ActionBar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.activity_ajuste_ubicacion_final);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        controles();
        eventos();

        ListaLectura = findViewById(R.id.listView);
        UbicacionFinalView.post(Load);
        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(MostrarActivosSector.this, LoginActivity.class);
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

    //region Recepcion de datos del activity Antecesor
    Runnable Load = new Runnable() {
        @Override
        public void run() {
            RecibirTakesInfo();
        }
    };

    public void RecibirTakesInfo() {

        activoInventarios = (ArrayList<ActivoInventario>) getIntent().getSerializableExtra("inventarioVisual");

        fillRecyclerView(activoInventarios);

    }
    //endregion
    //region Controles
    public void controles() {
        UbicacionFinalView = findViewById(R.id.activosSector);
        OfficesDBHelper = new OfficesDBHelper(UbicacionFinalView.getContext());
        AssetsDBHelper = new AssetsDBHelper(UbicacionFinalView.getContext());
    }
    //endregion
    //region Eventos
    public void eventos() {



    }

    /*Sonido de lectura*/
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    public void Message() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    //endregion
    //region Fill Adapter

    public void fillRecyclerView (ArrayList<ActivoInventario> response) {

        try{

            ListaLectura.setLayoutManager( new LinearLayoutManager( this));
            AdaptadorActivoXSector adaptadorAjusteUbicacion = new AdaptadorActivoXSector(response);
            ListaLectura.setAdapter(adaptadorAjusteUbicacion);

        }catch(Exception ex){
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //endregion

}
