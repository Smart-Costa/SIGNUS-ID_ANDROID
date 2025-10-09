package com.example.diverscan.activeid.Assign_tag_Assets;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

public class GrabarTagActivo extends AppCompatActivity {

    private EditText NumerodeactivoView;
    private EditText DescripcionView;
    private EditText CompaniaView;
    private EditText EdificioView;
    private EditText PisoView;
    private EditText OficinaView;
    private EditText EPCView;
    private EditText EPCnuevoView;
    private View mGrabartagView;

    Button btn_Obtener;
    Button btn_Grabar;
    Button btn_guardar;

    AssetsDBHelper AssetsDBHelper;

    private String Numactivo, descripcion, compania, edificio, piso, oficina, epc, epcnuevo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabar_tag_activo);

        controles();
        eventos();

         mGrabartagView.post(Load);
    }

    public void controles(){

        mGrabartagView = findViewById(R.id.GrabarTagForm);

        NumerodeactivoView = (EditText) findViewById(R.id.txtg_numero_activo);
        DescripcionView = (EditText) findViewById(R.id.txtg_descripcion);
        CompaniaView = (EditText) findViewById(R.id.txtg_compania);
        EdificioView = (EditText) findViewById(R.id.txtg_edificio);
        PisoView = (EditText) findViewById(R.id.txtg_piso);
        OficinaView = (EditText) findViewById(R.id.txtg_oficina);
        EPCView = (EditText) findViewById(R.id.txtg_epc);
        EPCnuevoView = (EditText) findViewById(R.id.txtg_epc_nuevo);

        AssetsDBHelper = new AssetsDBHelper(mGrabartagView.getContext());

        btn_Obtener = (Button) findViewById(R.id.btn_obtener);
        btn_Grabar = (Button) findViewById(R.id.btn_grabar);
        btn_guardar = (Button) findViewById(R.id.btn_guardar);
    }

    public void eventos(){

        btn_Obtener.setOnClickListener(OnClickListenerObtenerEPC);
        btn_Grabar.setOnClickListener(OnClickListenerGrabarEPCnuevo);
        btn_guardar.setOnClickListener(OnClickListenerGuardarEPC);


    }

    private Button.OnClickListener prueba = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String aux="Hola Mundo";
        }};

    Runnable Load = new Runnable(){
        @Override
        public void run() {

           InfoActivo(); //llamar al metodo que ocupas
        }
    };

    public void InfoActivo () { //en load cargue apenas abra la pantalla //(View view)

        //Traer datos de activo consultado en asignar tag
        Numactivo = getIntent().getExtras().getString("NumerodeactivoView");
        descripcion = getIntent().getExtras().getString("DescripcionView");
        compania = getIntent().getExtras().getString("CompaniaView");
        edificio = getIntent().getExtras().getString("EdificioView");
        piso = getIntent().getExtras().getString("PisoView");
        oficina = getIntent().getExtras().getString("OficinaView");
        epc = getIntent().getExtras().getString("EPCView");

        NumerodeactivoView.setText(Numactivo);
        DescripcionView.setText(descripcion);
        CompaniaView.setText(compania);
        EdificioView.setText(edificio);
        PisoView.setText(piso);
        OficinaView.setText(oficina);
        EPCView.setText(epc);

        NumerodeactivoView.setEnabled(false);
        DescripcionView.setEnabled(false);
        CompaniaView.setEnabled(false);
        EdificioView.setEnabled(false);
        PisoView.setEnabled(false);
        OficinaView.setEnabled(false);
        EPCView.setEnabled(false);
    }


    public void obtenerEPC(){

    }

    public void grabarEPC(){

    }

    public void GuardarEPC(){

        //Guardar o actualizar EPC
        Numactivo = NumerodeactivoView.getText().toString();
        epcnuevo = EPCnuevoView.getText().toString();

        Boolean respuesta = AssetsDBHelper.ActualizarEPC(Numactivo, epcnuevo);

        if(respuesta){
            Toast.makeText(getApplicationContext(), "EPC guardado correctamente!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "EPC no se guardo correctamente", Toast.LENGTH_LONG).show();
        }

    }

    private Button.OnClickListener OnClickListenerObtenerEPC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            obtenerEPC();
        }};

    private Button.OnClickListener OnClickListenerGrabarEPCnuevo = new View.OnClickListener() {
        @Override
        public void onClick(View v) { grabarEPC();
        }};

    private Button.OnClickListener OnClickListenerGuardarEPC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            GuardarEPC();
        }};
}
