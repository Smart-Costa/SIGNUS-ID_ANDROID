package com.example.diverscan.activeid.Locate_Assets;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.diverscan.activeid.R;

public class Localizacion_activo extends AppCompatActivity {

    private EditText epcView;
    private View mlocalizacionView;

    Button btn_iniciar;
    Button btn_detener;

    private String EPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion_activo);

        controles();
        eventos();

        mlocalizacionView.post(Load);
    }

    public void controles() {

        mlocalizacionView = findViewById(R.id.LocalizacionForm);

        epcView = (EditText) findViewById(R.id.txt_numero_epc);

        btn_iniciar = (Button) findViewById(R.id.btn_iniciar);
        btn_detener = (Button) findViewById(R.id.btn_detener);

    }

    public void eventos() {

        btn_iniciar.setOnClickListener(OnClickListenerIniciar);
        btn_detener.setOnClickListener(OnClickListenerDetener);
    }

    Runnable Load = new Runnable(){
        @Override
        public void run() {

            RecibirInfo();
        }
    };

    public void RecibirInfo () {

        EPC = getIntent().getExtras().getString("Dato_Tag");

        epcView.setText(EPC);

        epcView.setEnabled(false);
    }

    private Button.OnClickListener OnClickListenerIniciar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }};

    private Button.OnClickListener OnClickListenerDetener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }};
}
