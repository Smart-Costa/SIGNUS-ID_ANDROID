package com.example.diverscan.activeid.Tags;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.diverscan.activeid.GeneralTag.ResponseHandlerInterface;
import com.example.diverscan.activeid.GeneralTag.TagWriter;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.UI.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.TagsDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ClasificacionTags extends AppCompatActivity implements ResponseHandlerInterface {

    private View ClasficacionTagsView;
    private Button btnVerResultado;
    private Button btnClasificar;

    private RecyclerView ListaLectura;


    TagWriter rfidHandler;
    private String _lastTag = "";
    private boolean triggerPressed = false;
    private boolean isMultiSelect = false;

    private Spinner cmbTipoTags;

    private boolean _itemSelectedTipoTag;

    private Map<Integer,EntidadClasificarTag> _mapTipoTags = new HashMap<Integer, EntidadClasificarTag>();
    ArrayList<EntidadTagsClasificados> tagsClasificados;
    ArrayList<TagsVisual> tagsVisuals = new ArrayList<TagsVisual>();
    EntidadTagsClasificados entidadTagsClasificados;
    ArrayList<EntidadClasificacionTags> entidadClasificacionTags = new ArrayList<EntidadClasificacionTags>();
    private String tagSysId, Epc, tagTypeSysId, Name, cmbTipoTag, cmbIdTipoTag;
    private CheckBox ckReclasificar;
    ChequearTags _chequearTags;
    TagsDBHelper tagsDBHelper;

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clasificar_tags);
        _context = this;
        controles();
        eventos();
        cargarTipoTags();
        /*rfidHandler = new TagWriter();
        rfidHandler.onCreate(this);
        rfidHandler.Defaults();
        Toast.makeText(getApplicationContext(), rfidHandler.Defaults(), Toast.LENGTH_LONG).show();*/
        rfidHandler.onCreate(this);
        rfidHandler.Defaults();


        tagsClasificados = tagsDBHelper.ObtenerTagsTipos();
        _chequearTags = new ChequearTags(tagsClasificados, this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(ClasificacionTags.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
    }


    //region Controles
    public void controles(){
        ClasficacionTagsView = findViewById(R.id.ClasificarTags);
        cmbTipoTags = findViewById(R.id.cmbTagCategoria);
        btnVerResultado =findViewById(R.id.btn_VerResultados);
        tagsDBHelper = new TagsDBHelper(ClasficacionTagsView.getContext());
        ListaLectura = findViewById(R.id.listTags);
        ckReclasificar = findViewById(R.id.ckReclasifica);
        ckReclasificar.setChecked(false);
        btnClasificar = findViewById(R.id.btn_Clasificar);
    }
    //endregion

    //region Eventos
    public void eventos(){
        btnVerResultado.setOnClickListener(OnClickListenerResultados);
        btnClasificar.setOnClickListener(OnClickListenerClasificar);
        cmbTipoTags.setOnItemSelectedListener(onItemSpinnerListenerTipoTags);

    }
    public static void Message(){
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 24);
    }
    //endregion

    //region LLenado Spinner Tags
    private void cargarTipoTags(){
        TagsDBHelper tagsDBHelper = new TagsDBHelper(ClasficacionTagsView.getContext());
        _mapTipoTags = (Map<Integer, EntidadClasificarTag>) tagsDBHelper.ObtenerTipoTags();
        EntidadClasificarTag[] tipoTag = _mapTipoTags.values().toArray(new EntidadClasificarTag[0]);
        fillSpinnerTags(tipoTag);
    }

    private void fillSpinnerTags(EntidadClasificarTag[] entidadClasificarTags){
        cmbTipoTags = findViewById(R.id.cmbTagCategoria);
        cmbTipoTags.setAdapter(new ArrayAdapter<>(ClasficacionTagsView.getContext().getApplicationContext()
                ,R.layout.spinner_layaout, entidadClasificarTags));
    }
    private AdapterView.OnItemSelectedListener onItemSpinnerListenerTipoTags = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_itemSelectedTipoTag) {
                EntidadClasificarTag entidadClasificarTags= (EntidadClasificarTag) cmbTipoTags.getSelectedItem();
                }
            _itemSelectedTipoTag = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    //endregion

    //region Resultados de Lectura

    private void VerResultado(View view){
        _chequearTags.InsertarActivosEncontrados();
        _chequearTags.ClearActivosSobrantes();

        if (_chequearTags.GetActivos().size() == 0) {
            Toast.makeText(getApplicationContext(), "La lista se encuentra vacía", Toast.LENGTH_LONG).show();
        } else {

            for (TagsVisual tag : _chequearTags.GetActivos()) {
                if (tag.getEstado().equals("No existe")) {

                }else if(tag.getEstado().equals("SClasificado")){

                } else if(tag.getEstado().equals("No Encontrado")){

                }else {

                    String tagSysId = tag.getTagSysId();
                    String epc = tag.getTagID();
                    String tagTypeSysId = tag.getTagTypeSysId();
                    String name = tag.getName();
                    String Status = tag.getEstado();


                    TagsVisual visual = new TagsVisual(tagSysId, epc, tagTypeSysId, name, Status);
                    tagsVisuals.add(visual);

                }
            }
            fillRecyclerView(tagsVisuals);
        }
    }

    private void  fillRecyclerView(ArrayList<TagsVisual> tagsVisuals){
        try {
            ListaLectura.setLayoutManager(new LinearLayoutManager(this));
            AdaptadorClasificacionTags adaptadorClasificacionTags = new AdaptadorClasificacionTags(tagsVisuals);
            ListaLectura.setAdapter(adaptadorClasificacionTags);

        }
        catch(Exception ex){
            ex.printStackTrace();
        }


    }
   //endregion

    //region Metodos Clasificacion

    private void Clasificar(View view){

        try{
            for(int position = 0;position < tagsVisuals.size(); position++ ) {
                String estado = tagsVisuals.get(position).getEstado();
                if (estado.equals("No Clasificado")){
                    tagSysId = tagsVisuals.get(position).getTagSysId();
                    Epc = tagsVisuals.get(position).getTagID();
                    tagTypeSysId = tagsVisuals.get(position).getTagTypeSysId();
                    Name = tagsVisuals.get(position).getName();

                    EntidadClasificacionTags entidadClasificacionTag = new EntidadClasificacionTags(tagSysId, Epc,tagTypeSysId,Name);
                    entidadClasificacionTags.add(entidadClasificacionTag);
                }
            }

            EntidadClasificarTag entidadClasificarTag = (EntidadClasificarTag) cmbTipoTags.getSelectedItem();
            cmbTipoTag  = entidadClasificarTag.getName();
            cmbIdTipoTag = entidadClasificarTag.getTagTypeSysId();

            if(cmbIdTipoTag.equals("") || cmbTipoTag.equals("Sin Asignar")){
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("Debe seleccionar una categoría para el tag")
                        .setCancelable(false)

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                        }}).show();

            }else if( entidadClasificacionTags.size() <= 0){
                Message();
                AlertDialog.Builder builder = new AlertDialog.Builder(ClasficacionTagsView.getContext());
                builder.setIcon(R.drawable.alertaicono);
                builder.setTitle("ATENCIÓN");
                builder.setMessage("Todos los tags ya se encuentran clasificados, si desea reclasficar " +
                        "tags, marque la opción de reclasificar");
                builder.setCancelable(true);
                final AlertDialog closedialog = builder.create();
                closedialog.show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        closedialog.dismiss();
                        timer.cancel();
                    }
                }, 4000);

            }else{
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("CLASIFICAR TAGS")
                        .setMessage("Desea clasificar los tags como: " + cmbTipoTag )
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean respuesta = tagsDBHelper.ReclasificarTags( cmbIdTipoTag, entidadClasificacionTags);
                                if(respuesta){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ClasficacionTagsView.getContext());
                                    builder.setIcon(R.drawable.alertaicono);
                                    builder.setTitle("ACTUALIZADO");
                                    builder.setMessage("Se han clasificado correctamente los tags.");
                                    builder.setCancelable(true);
                                    final AlertDialog closedialog = builder.create();
                                    closedialog.show();
                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            closedialog.dismiss();
                                            timer.cancel();
                                        }
                                    }, 4000);
                                }else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ClasficacionTagsView.getContext());
                                    builder.setIcon(R.drawable.alertaicono);
                                    builder.setTitle("ACTUALIZADO");
                                    builder.setMessage("No se han podido clasificar los tags correctamente.");
                                    builder.setCancelable(true);
                                    final AlertDialog closedialog = builder.create();
                                    closedialog.show();
                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            closedialog.dismiss();
                                            timer.cancel();
                                        }
                                    }, 4000);
                                }
                            }
                        }).show();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }


    }
    private void Reclasificar(View view){

        try{
            for(int position = 0;position < tagsVisuals.size(); position++ ){

                tagSysId = tagsVisuals.get(position).getTagSysId();
                Epc = tagsVisuals.get(position).getTagID();
                tagTypeSysId = tagsVisuals.get(position).getTagTypeSysId();
                Name = tagsVisuals.get(position).getName();

                EntidadClasificacionTags entidadClasificacionTag = new EntidadClasificacionTags(tagSysId, Epc,tagTypeSysId,Name);
                entidadClasificacionTags.add(entidadClasificacionTag);
            }

            EntidadClasificarTag entidadClasificarTag = (EntidadClasificarTag) cmbTipoTags.getSelectedItem();
            cmbTipoTag  = entidadClasificarTag.getName();
            cmbIdTipoTag = entidadClasificarTag.getTagTypeSysId();

            if(cmbIdTipoTag.equals("") || cmbTipoTag.equals("Sin Asignar")){
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("Debe seleccionar una categoría para el tag")
                        .setCancelable(false)

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }}).show();

            }else if( entidadClasificacionTags.size() <= 0){
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("ALERTA")
                        .setMessage("No ha realizado la lectura de tags")
                        .setCancelable(false)

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }}).show();

            }else{
                Message();
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.alertaicono)
                        .setTitle("RECLASIFICAR TAGS")
                        .setMessage("Desea reclasificar los tags como: " + cmbTipoTag )
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean respuesta = tagsDBHelper.ReclasificarTags( cmbIdTipoTag, entidadClasificacionTags);
                                if(respuesta){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ClasficacionTagsView.getContext());
                                    builder.setIcon(R.drawable.alertaicono);
                                    builder.setTitle("ACTUALIZADO");
                                    builder.setMessage("Se han clasificado correctamente los tags.");
                                    builder.setCancelable(true);
                                    final AlertDialog closedialog = builder.create();
                                    closedialog.show();
                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            closedialog.dismiss();
                                            timer.cancel();
                                        }
                                    }, 4000);
                                }else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ClasficacionTagsView.getContext());
                                    builder.setIcon(R.drawable.alertaicono);
                                    builder.setTitle("ACTUALIZADO");
                                    builder.setMessage("No se han podido clasificar los los tags.");
                                    builder.setCancelable(true);
                                    final AlertDialog closedialog = builder.create();
                                    closedialog.show();
                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            closedialog.dismiss();
                                            timer.cancel();
                                        }
                                    }, 4000);
                                }
                            }
                        }).show();
            }
        }catch(Exception ex){
            ex.printStackTrace();

    }}

    //endregion

    //region Botones

    private  Button.OnClickListener OnClickListenerResultados = new View.OnClickListener(){
        @Override
        public void onClick(View view){
           VerResultado(ClasficacionTagsView);
        }
    };

    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            VerResultado(ClasficacionTagsView);
        }
    };

    private  Button.OnClickListener OnClickListenerClasificar = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(ckReclasificar.isChecked()){
                Reclasificar(ClasficacionTagsView);
            }else{
                Clasificar(ClasficacionTagsView);
            }
        }
    };
    //endregion

    //region Metodos de Lectura
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

    @Override
    public void handleTagdata(ReaderTag[] tagData) {
        ArrayList<String> tags = new ArrayList<String>();
        for (int index = 0; index < tagData.length; index++) {
            //_lastTag = tagData[index].getTagID();
            tags.add(tagData[index].getEpc());

        }
        if(_chequearTags.CheckActivos(tags)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message();
                }
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        triggerPressed = pressed;
        if (pressed) {
            rfidHandler.performInventory();
        }else
            rfidHandler.stopInventory();
            //handler.postDelayed(r, 1000);
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
    //endregion
}
