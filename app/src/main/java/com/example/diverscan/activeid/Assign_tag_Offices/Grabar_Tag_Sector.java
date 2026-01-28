package com.example.diverscan.activeid.Assign_tag_Offices;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.Locate_Assets.Actualizar_activo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Tags.EntidadTiposTags;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.OfficesDBHelper;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;
import com.zebra.rfid.api3.TagData;

public class Grabar_Tag_Sector extends AppCompatActivity implements ResponseHandlerInterface {

    private EditText OfficeView;
    private EditText floorView;
    private EditText buildView;
    private EditText epcView;
    private EditText statusview;
    private View mgrabarTagSectorView;
    private CheckBox checkBoxEstandard;

    Button btn_obtener;
    Button btn_guardar;
    Button btn_atras;

    OfficesDBHelper OfficesDBHelper;
    TagsDBHelper TagsDBHelper;
    AssetsDBHelper AssetsDBHelper;

    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false;

    private String oficina, piso, edificio, epc, estado, idOficina;

    private String officeSysId, tagSysId, tagId, Sync, oficinaNombre, floorSysId, pisoNombre, buildingSysId, edificioNombre, companySysId, razonNombre;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabar__tag__sector);
        _context = this;

        controles();
        eventos();

        mgrabarTagSectorView.post(Load);
        rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);


        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(Grabar_Tag_Sector.this, LoginActivity.class);
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
    public void controles() {

        mgrabarTagSectorView = findViewById(R.id.GrabarTagSectorForm);

        OfficeView = (EditText) findViewById(R.id.txt_sectorS);
        floorView = (EditText) findViewById(R.id.txt_pisoS);
        buildView = (EditText) findViewById(R.id.txt_edificioS);
        epcView = (EditText) findViewById(R.id.txt_epcS);
        statusview = (EditText) findViewById(R.id.txt_statusS);

        OfficesDBHelper = new OfficesDBHelper(mgrabarTagSectorView.getContext());
        TagsDBHelper = new TagsDBHelper(mgrabarTagSectorView.getContext());
        AssetsDBHelper = new AssetsDBHelper(mgrabarTagSectorView.getContext());

        btn_obtener = (Button) findViewById(R.id.btn_obtenerS);
        btn_guardar = (Button) findViewById(R.id.btn_guardarS);
        btn_atras   = (Button) findViewById(R.id.btn_atrasS);

        checkBoxEstandard = findViewById(R.id.ckEstandardGS1);
        checkBoxEstandard.setChecked(false);
    }

    public void eventos() {

        btn_obtener.setOnClickListener(OnClickListenerObtenerEPC);
        btn_guardar.setOnClickListener(OnClickListenerGuardarEPC);
        btn_atras.setOnClickListener(OnClickListenerAtras);


    }

    Runnable Load = new Runnable(){
        @Override
        public void run() {

            RecibirInfoOffices();
        }
    };

    public void RecibirInfoOffices () {

        oficina = getIntent().getExtras().getString("Dato_oficina");
        piso = getIntent().getExtras().getString("Dato_piso");
        edificio = getIntent().getExtras().getString("Dato_edificio");
        epc = getIntent().getExtras().getString("Dato_epc");
        idOficina = getIntent().getExtras().getString("Dato_IdOficina");

        OfficeView.setText(oficina);
        floorView.setText(piso);
        buildView.setText(edificio);
        epcView.setText(epc);

        OfficeView.setEnabled(false);
        floorView.setEnabled(false);
        buildView.setEnabled(false);
    }

    public void obtenerEPC(){
        epcView.setText(_lastTag);
    }



    public EntidadTiposTags entidadTiposTags;
    public void guardarEPC(View view){

        try {

            if (checkBoxEstandard.isChecked()){

                oficinaNombre = OfficeView.getText().toString();
                tagId =epcView.getText().toString();//;"003620140510213429766153"

                String nombreCategoria;
                entidadTiposTags = AssetsDBHelper.NombreTipoTag(tagId);
                nombreCategoria = entidadTiposTags.getName();

                if(epc.contains("414")){

                    boolean respuesta = OfficesDBHelper.ActualizarEPC(idOficina,  tagId);

                    if(respuesta){
                        Toast.makeText(getApplicationContext(), "Epc asignado correctamente", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "No se ha podido asignar el EPC", Toast.LENGTH_LONG).show();
                    }
                }else{

                    if(epc.contains("414")){

                        boolean respuesta = OfficesDBHelper.ActualizarEPC(idOficina,  tagId);

                        if(respuesta){
                            Toast.makeText(getApplicationContext(), "Epc asignado correctamente", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "No se ha podido asignar el EPC", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "El tag debe ser de ubicación", Toast.LENGTH_LONG).show();
                    }

                }

            }

            else{
                oficinaNombre = OfficeView.getText().toString();
                tagId = epcView.getText().toString();

                boolean respuesta = OfficesDBHelper.ActualizarEPC(idOficina,  tagId);

                if(respuesta){
                    Toast.makeText(getApplicationContext(), "Epc asignado correctamente", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "No se ha podido asignar el EPC", Toast.LENGTH_LONG).show();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "EL tag debe estar asignado a una categoría", Toast.LENGTH_LONG).show();
        }
    }



    public void GuardarEPC(){
    }

    public static boolean MessageBeep(){
        try{
            ToneGenerator MessageBeep = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            MessageBeep.startTone(ToneGenerator.TONE_CDMA_ANSWER, 42);
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return MessageBeep();
    }

    private Button.OnClickListener OnClickListenerObtenerEPC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            obtenerEPC();
        }};

    private Button.OnClickListener OnClickListenerGuardarEPC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            guardarEPC(mgrabarTagSectorView);
        }};

    private Button.OnClickListener OnClickListenerAtras = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            GuardarEPC();
        }};

    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }
    public static void Message(){
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tagData.length; index++) {
            sb.append(tagData[index].getEpc() + "\n");
            _lastTag = tagData[index].getEpc();

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //epcView.append(sb.toString());
                epcView.setText(_lastTag);
            }
        });
       }

    @Override
    public void handleTriggerPress(boolean pressed) {
        triggerPressed = pressed;
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   epcView.setText("");
                    Message();
                }
            });
            rfidHandler.performInventory();

        } else
            rfidHandler.stopInventory();
    }

    @Override
    public Context GetContext() {
        return this;
    }

    @Override
    public void SetMessage(String Text) {
        final String text = Text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //EPCView.setText(text);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

            }
        });
    }
}


