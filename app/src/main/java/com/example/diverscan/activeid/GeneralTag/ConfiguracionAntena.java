package com.example.diverscan.activeid.GeneralTag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.zebra.rfid.api3.TagData;

import java.util.List;

public class ConfiguracionAntena extends AppCompatActivity implements ResponseHandlerInterface{
    private TextView txtPotencia;
    private TextView txtCnfActual;
    private SeekBar skPotencia;
    private ProgressBar pgPotencia;
    private TextView txtPorcentaje;
    private View mConfigurarAntena;
    private String Power;
    public String potenciaAntena;
    private int potenciaInicial;
    private Spinner spReaders;
    Context _context;
    Activity _activity;

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    TagWriter rfidHandler;
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_configurar_antena);
        _context = this;
        _activity = this;
        controles();
        eventos();
        try{
            Power = SharedPreferencesGetSet.leer_local("potenciaAntena", this);
            txtCnfActual.setText("Potencia actual: " + Power);
            skPotencia.setMax(300);
            potenciaInicial = Integer.parseInt(Power);

            if (potenciaInicial == 0) {
                skPotencia.setProgress(80);
            } else{
                skPotencia.setProgress(potenciaInicial);
            }
        }catch(Exception e){
        }

        try {
            txtPotencia.setText("Versión. " + _context.getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(ConfiguracionAntena.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();


        /*new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                return TagWriter.getInstance().getAvailableReaderNames();
            }

            @Override
            protected void onPostExecute(List<String> readerNames) {
                if (readerNames != null && !readerNames.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(_context,
                            android.R.layout.simple_spinner_item, readerNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spReaders.setAdapter(adapter);

                    // Opción: seleccionar por defecto el primero
                    spReaders.setSelection(0);

                    // Configurar TagWriter con el primer lector



                    TagWriter.getInstance(ConfiguracionAntena.this, new TagWriter.ReaderReadyCallback() {
                        @Override
                        public void onReaderReady(boolean success, String message) {
                            if (success) {
                                String result = TagWriter.getInstance(ConfiguracionAntena.this).Defaults();
                                Toast.makeText(_context, result, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(_context, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }).setReaderName(readerNames.get(0));
                    // Intentar conectar con el primero automáticamente
                    conectarLector();

                    // Escuchar cambios de selección
                    spReaders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String lectorSeleccionado = readerNames.get(position);

                            // Reiniciás por completo la instancia
                            TagWriter.resetInstance();

                            // Ahora volvés a crearla con el lector actualizado
                            TagWriter.getInstance(ConfiguracionAntena.this, new TagWriter.ReaderReadyCallback() {
                                @Override
                                public void onReaderReady(boolean success, String message) {
                                    if (success) {
                                        String result = TagWriter.getInstance(ConfiguracionAntena.this).Defaults();
                                        Toast.makeText(_context, result, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(_context, "Error: " + message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).setReaderName(lectorSeleccionado);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                } else {
                    Toast.makeText(_context, "No hay lectores disponibles", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();*/
    }
    @Override
    public void SetMessage(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        runOnUiThread(() -> {
            if (pressed) {
                rfidHandler.performInventory();
            } else {
                rfidHandler.stopInventory();
            }
        });
    }

    @Override
    public void handleTagdata(TagData[] tagData) {
        // Procesar los tags leídos, mostrarlos en pantalla o lo que ocupés
    }

    @Override
    public Context GetContext() {
        return this;
    }
    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }
    private void conectarLector() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return rfidHandler.onResume(); // Llama connect()
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(_context,
                        result.isEmpty() ? "Lector ya conectado" : result,
                        Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
    public void controles(){
         mConfigurarAntena = findViewById(R.id.FConfigurarAntena);
         txtPotencia = findViewById(R.id.txtPotencia);
         skPotencia = findViewById(R.id.skPotencia);
         pgPotencia = findViewById(R.id.progressBar);
         txtPorcentaje = findViewById(R.id.txtPorcentaje);
         txtCnfActual = findViewById(R.id.txtUltimaConfiguracion);
         spReaders =   findViewById(R.id.spinnerLectores);
    }

    public void eventos(){

        skPotencia.setOnSeekBarChangeListener(OnSeekPotencia);

    }

    public SeekBar.OnSeekBarChangeListener OnSeekPotencia = new SeekBar.OnSeekBarChangeListener(){

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser){

            txtPorcentaje.setText("" + progress);
            potenciaAntena = txtPorcentaje.getText().toString();
            SharedPreferencesGetSet.guardar_local("potenciaAntena", potenciaAntena, getApplicationContext());

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            txtCnfActual.setText("Potencia actual: "+  + seekBar.getProgress());
        }

    };





}
