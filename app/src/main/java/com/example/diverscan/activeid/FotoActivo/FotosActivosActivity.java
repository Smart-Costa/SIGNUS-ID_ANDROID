package com.example.diverscan.activeid.FotoActivo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diverscan.activeid.CarruselCompleto.CarruselFotosActivo;
import com.example.diverscan.activeid.CarruselCompleto.Step;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.FotoDBHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class FotosActivosActivity extends CarruselFotosActivo implements IFotoActivo {

    private Activity _activity;
    private Context _context;
    private View mFotosActivos;

    private ImageButton btnAgregarFoto;
    private ImageButton btnEliminarFoto;
    private ImageView imgView;
    FotoDBHelper fotoDBHelper;
    private TextView lblDescripcion;
    private EditText txtDescripcionFoto;
    File Archivo = null;
    FileReader fr = null;
    BufferedReader br = null;
    private String assetBarcode, assetSysId, assetDescription;
    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fotos_activos);

        _context = this;
        _activity = this;
        fotoDBHelper = new FotoDBHelper(_context);

        CargarInfo();

        // Permission Step
        if(TieneFoto( this)){
            try {
                AbrirImagen(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                    .setContent("No posee fotos, si desea agreagar fotos presione en"+ '"'+"Abrir cámara"+ '"')
                    .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                    .setDrawable(String.valueOf(R.drawable.no_hay_foto)) // int top drawable
                    .setSummary(assetDescription)
                    .build());
        }

    }



    //region Eventos, controles y permisos de aplicacion

    private boolean TieneFoto(IFotoActivo iFotoActivo){
        return iFotoActivo.TieneFoto(assetSysId);
    }

    private void  AbrirImagen(IFotoActivo iFotoActivo) throws IOException {
        ArrayList<EFotoActivo> eFotoActivos = iFotoActivo.ObtenerFotoActivo(assetSysId);

        for(EFotoActivo fotos : eFotoActivos){
            String nombre = fotos.getNombreArchivo()
                    .replace("_"," ")
                    .replace("_"," ")
                    .replace(".txt", "")
                    .replace(assetBarcode, "");

            File rootFile = new File(fotos.getRutaFoto()+"/"+fotos.getNombreArchivo());
            if(rootFile.exists()){

                FileInputStream fileInputStream = new FileInputStream(rootFile);
                byte[] array = new byte[150000];
                fileInputStream.read(array);
                String textoFinal = new String(array);
                fileInputStream.close();

                addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                        .setContent(assetDescription + "\n" )
                        .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                        .setDrawable(textoFinal) // int top drawable
                        .setSummary(fotos.getObservacionFoto())
                        .build());
            }else{
                addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                        .setContent("No posee fotos")
                        .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                        .setDrawable(String.valueOf(R.drawable.no_hay_foto)) // int top drawable
                        .setSummary("No posee fotos, si desea agreagar fotos presione en"+ '"'+"Abrir cámara+ '")
                        .build());
            }
        }
    }

    private void CargarInfo(){
        assetBarcode = getIntent().getStringExtra("placaActivo");
        assetSysId = getIntent().getStringExtra("_idActivo");
        assetDescription = getIntent().getStringExtra("_assetDescription");
    }

    private boolean TienePermiso(){
        boolean tengoPermiso = true;
        int permiso = ContextCompat.checkSelfPermission(_context, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permiso == 0){
            return tengoPermiso;
        }
        return false;
    }

    //endregion

    //region metodos principales

    @Override
    public void currentFragmentPosition(int position) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean InsertarFotoDB(String fotoID, String rutaFoto, String nombreFoto, String fotoConsecutivo, String assetSysId) {
        return fotoDBHelper.InsertarFotoDB(fotoID, rutaFoto, nombreFoto, fotoConsecutivo, assetSysId);
    }

    @Override
    public boolean TieneFoto(String assetSysId) {
        return fotoDBHelper.TieneFoto(assetSysId);
    }

    @Override
    public int CantidadFotos(String AssetSysId) {
        return fotoDBHelper.CantidadFotos(AssetSysId);
    }

    @Override
    public ArrayList<EFotoActivo> ObtenerFotoActivo(String AssetSysID) {
        return fotoDBHelper.ObtenerFotoActivo(AssetSysID);
    }
    //endregion
}
