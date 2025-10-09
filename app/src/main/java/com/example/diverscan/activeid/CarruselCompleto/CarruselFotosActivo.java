package com.example.diverscan.activeid.CarruselCompleto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diverscan.activeid.FotoActivo.EFotoActivo;
import com.example.diverscan.activeid.FotoActivo.FotosActivosActivity;
import com.example.diverscan.activeid.FotoActivo.IFotoActivo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Utilities.MyMessageDialog;
import com.example.diverscan.activeid.sqlite.FotoDBHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarruselFotosActivo extends AppCompatActivity implements View.OnClickListener,
        CurrentFragmentListener, IFotoActivo {
    private List<Step> steps;
    private StepPagerAdapter adapter;
    private Activity _activity;
    private Context _context;

    private ViewPager pager;
    private Button next, prev, between;
    private LinearLayout indicatorLayout;
    private FrameLayout containerLayout;
    private RelativeLayout buttonContainer;
    private String assetBarcode, assetSysId, assetDescription;
    ImageView imageView;
    private ImageView imgView;

    private int currentItem;
    FotoDBHelper fotoDBHelper;
    private CurrentFragmentListener currentFragmentListener;
    private String prevText, nextText, finishText, cancelText, givePermissionText, betweenText;
    private int selectedIndicator = R.drawable.circle_black, indicator = R.drawable.circle_white;
    MyMessageDialog myMessageDialog =new MyMessageDialog();
    AlertDialog alertDialog;
    private boolean _reescribir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrusel_fotos_activo);
        getSupportActionBar().hide();
        _context = this;
        _activity = this;
        currentFragmentListener = this;
        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            changeStatusBarColor();
        }
        imgView = new ImageView(_context);
    }

    private void init() {
        steps = new ArrayList<>();
        initTexts();
        initViews();
        initAdapter();
        fotoDBHelper = new FotoDBHelper(this);
    }

    private void initTexts() {
        prevText = "Atrás";
        cancelText = "Atrás";
        finishText = "Foto Nueva";
        nextText = "Foto Nueva";
        givePermissionText = "Aceptar";
        betweenText = "Reescribir Foto";
    }

    private void initAdapter() {
        adapter = new StepPagerAdapter(getSupportFragmentManager(), steps);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentItem = position;
                currentFragmentListener.currentFragmentPosition(position);
                controlPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#CE9D0C"));
    }

    private void controlPosition(int position) {

        if (position == steps.size() - 1) {
            next.setText(finishText);
            between.setText(betweenText);
            prev.setText(prevText);
        } else if (position == 0) {
            prev.setText(cancelText);
            between.setText(betweenText);
            next.setText(nextText);
        } else {
            prev.setText(prevText);
            between.setText(betweenText);
            next.setText(nextText);
        }
        if (controlPermission()) {
            prepareNormalView();
        } else {
            preparePermissionView();
        }
        if (!steps.isEmpty()) {
            containerLayout.setBackgroundColor(steps.get(position).getBackgroundColor());
            buttonContainer.setBackgroundColor(Color.parseColor("#CE9D0C"));
        }
    }

    private void prepareNormalView() {
        pager.setOnTouchListener(null);
    }

    private void preparePermissionView() {
        next.setText(givePermissionText);

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void initViews() {
        currentItem = 0;

        pager = (ViewPager) findViewById(R.id.viewPager);
        next = (Button) findViewById(R.id.next);
        prev = (Button) findViewById(R.id.prev);
        between =(Button) findViewById(R.id.between);
        indicatorLayout = (LinearLayout) findViewById(R.id.indicatorLayout);
        containerLayout = (FrameLayout) findViewById(R.id.containerLayout);
        buttonContainer = (RelativeLayout) findViewById(R.id.buttonContainer);

        between.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AbrirCamara();
                _reescribir = true;
            }
        });
        next.setOnClickListener(this);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void addFragment(Step step) {
        steps.add(step);
        adapter.notifyDataSetChanged();
        notifyIndicator();
        controlPosition(currentItem);
    }

    public void addFragment(Step step, int position) {
        steps.add(position, step);
        adapter.notifyDataSetChanged();
        notifyIndicator();
    }

    public void notifyIndicator() {
        if (indicatorLayout.getChildCount() > 0)
            indicatorLayout.removeAllViews();

        for (int i = 0; i < steps.size(); i++) {
            imageView = new ImageView(this);
            imageView.setPadding(8, 8, 8, 8);
            int drawable = indicator;
            if (i == currentItem)
                drawable = selectedIndicator;

            byte[] decodedString = Base64.decode(String.valueOf(drawable), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);

            final int finalI = i;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeFragment(finalI);
                }
            });

            indicatorLayout.addView(imageView);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentItem == 0) {
            super.onBackPressed();
        } else {
            changeFragment(false);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            if (controlPermission())
                changeFragment(true);
            else
                requestPermissions(((PermissionStep) steps.get(pager.getCurrentItem())).getPermissions(), 1903);
        } else if (v.getId() == R.id.prev) {
            changeFragment(false);
        }
    }

    private void changeFragment(int position) {
        if (controlPermission())
            pager.setCurrentItem(position, true);
    }

    private boolean controlPermission() {
        if (!steps.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && steps.get(pager.getCurrentItem()) instanceof PermissionStep) {

            for (String permission : ((PermissionStep) steps.get(pager.getCurrentItem())).getPermissions()) {
                int permissionResult = checkSelfPermission(permission);

                if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void changeFragment(boolean isNext) {
        int item = currentItem;
        if (isNext) {
            item++;
        } else {
            item--;
        }

        int stepSize =steps.size() ;
        if (item < 0) {
            regresar();
        }else if(item < stepSize ){
            finishCamera();
        } else
            finishCamera();
    }

    private void AbrirCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public int CantidadFotos(String AssetSysId) {
        return 0;
    }

    @Override
    public ArrayList<EFotoActivo> ObtenerFotoActivo(String AssetSysID) {
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        assetBarcode = getIntent().getStringExtra("placaActivo");
        assetSysId = getIntent().getStringExtra("_idActivo");
        assetDescription = getIntent().getStringExtra("_assetDescription");
        String nombreCarpeta = assetBarcode;
        final String nombreArchivo = "Foto_" + assetBarcode;
        int _currentItem = currentItem;
        int varItem = 0;
        for(Step item : steps){
            if(item.getContent().contains("No posee fotos")){
                if(_currentItem == 0){
                    _currentItem = (_currentItem+1);
                }else if(_currentItem == 1){
                    _currentItem = (_currentItem+1);
                }else if(_currentItem == 2){
                    _currentItem = (_currentItem+1);
                }else if(_currentItem == 3){
                    _currentItem = (_currentItem+1);
                }else if(_currentItem == 4){
                    _currentItem = (_currentItem+1);
                }else {
                    if(_reescribir){
                        continue;
                    }else{
                        showAlertDialogUnsuccessful(_activity, "Atención", "Ya no se " +
                                "pueden ingresar fotos nuevas, debe reescribir las existentes");
                        return;
                    }
                }
            }else {
                varItem = Integer.parseInt(item.getSummary());
                if(varItem == 1){
                    _currentItem = (varItem + 1);
                }else if(varItem == 2){
                    _currentItem = (varItem + 1);
                }else if(varItem == 3){
                    _currentItem = (varItem + 1);
                }else {
                    if(_reescribir){
                        continue;
                    }else{
                        showAlertDialogUnsuccessful(_activity, "Atención", "Ya no se " +
                                "pueden ingresar fotos nuevas, debe reescribir las existentes");
                        return;
                    }
                }
                continue;
            }
        }

        String varCurrent = steps.get(currentItem).getSummary();

        if(requestCode == 1 && resultCode == RESULT_OK){

            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imgBitmap);
            Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();

            if(bitmap != null){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 30, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                final String fotoABase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                try{
                    //File root = new File(Environment.getExternalStorageDirectory() + "/FotosActivos");
                    File root = new File(_context.getExternalFilesDir(null) + "/FotosActivos");
                    final File assetRoot = new File(root+"/", nombreCarpeta);

                    if(!_reescribir){
                        if(!root.exists()){
                            if(root.mkdirs()){
                                if(!assetRoot.exists()){
                                    if(assetRoot.mkdirs()){
                                        boolean creado = EscribirArchivo(assetRoot, nombreArchivo,fotoABase64, this, _currentItem);
                                        if(creado){
                                            addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                                                    .setContent("")
                                                    .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                                                    .setDrawable(fotoABase64) // int top drawable
                                                    .setSummary(String.valueOf(_currentItem))
                                                    .build());
                                            Toast.makeText(_context, "Se ha guardado la foto", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(_context, "No se ha guardado la foto", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                        }else{
                            if(!assetRoot.exists()){
                                if(assetRoot.mkdirs()){
                                    boolean creado = EscribirArchivo(assetRoot, nombreArchivo,fotoABase64, this, _currentItem);
                                    if(creado){
                                        addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                                                .setContent("")
                                                .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                                                .setDrawable(fotoABase64) // int top drawable
                                                .setSummary(String.valueOf(_currentItem))
                                                .build());
                                        Toast.makeText(_context, "Se ha guardado la foto", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(_context, "No se ha guardado la foto", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }else{
                                boolean creado = EscribirArchivo(assetRoot, nombreArchivo,fotoABase64, this, _currentItem);
                                if(creado){
                                    addFragment(new Step.Builder().setTitle("Activo número: " + assetBarcode)
                                            .setContent("")
                                            .setBackgroundColor(Color.parseColor("#F4F4F4")) // int background color
                                            .setDrawable(fotoABase64) // int top drawable
                                            .setSummary(String.valueOf(_currentItem))
                                            .build());
                                    Toast.makeText(_context, "Se ha guardado la foto", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(_context, "No se ha guardado la foto", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }else{
                        if(!root.exists()){
                            if(root.mkdirs()){
                                if(!assetRoot.exists()){
                                    if(assetRoot.mkdirs()){
                                        ReescribirFotoMessage(assetRoot,nombreArchivo,fotoABase64,Integer.parseInt(varCurrent));
                                    }
                                }
                            }
                        }else{
                            if(!assetRoot.exists()){
                                if(assetRoot.mkdirs()){
                                    ReescribirFotoMessage(assetRoot,nombreArchivo,fotoABase64,Integer.parseInt(varCurrent));
                                }
                            }else{

                                ReescribirFotoMessage(assetRoot,nombreArchivo,fotoABase64, Integer.parseInt(varCurrent));

                            }
                        }
                    }
                    _currentItem = 0;

                }catch(Exception ex){
                    _currentItem = 0;
                    ex.printStackTrace();
                }
            }
        }
    }

    private boolean EscribirArchivo(File assetRoot, String nombreArchivo, String FotoBase,final IFotoActivo iFotoActivo,int currentItem) throws IOException {
        String nombreFoto1 = nombreArchivo+"_"+String.valueOf(currentItem)+".txt";
        String nombreFoto2 = nombreArchivo+"_"+String.valueOf(currentItem)+".txt";
        String nombreFoto3 = nombreArchivo+"_"+String.valueOf(currentItem)+".txt";
        String nombreFoto4 = nombreArchivo+"_"+String.valueOf(currentItem)+".txt";

        File gpxFile = new File(assetRoot, nombreFoto1);
        File gpxFile2 = new File(assetRoot, nombreFoto2);
        File gpxFile3 = new File(assetRoot, nombreFoto3);
        File gpxFile4 = new File(assetRoot, nombreFoto4);
        if(!gpxFile.exists()){
            FileWriter writer = new FileWriter(gpxFile);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            String uniqueID = UUID.randomUUID().toString();
            boolean save = iFotoActivo.InsertarFotoDB(uniqueID, String.valueOf(assetRoot), nombreFoto1,String.valueOf(currentItem), assetSysId);
            return save;
        }else if(!gpxFile2.exists()){
            FileWriter writer = new FileWriter(gpxFile2);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            String uniqueID = UUID.randomUUID().toString();
            boolean save = iFotoActivo.InsertarFotoDB(uniqueID, String.valueOf(assetRoot), nombreFoto2,String.valueOf(currentItem), assetSysId);
            return save;
        }else if (!gpxFile3.exists()){
            FileWriter writer = new FileWriter(gpxFile3);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            String uniqueID = UUID.randomUUID().toString();
            boolean save = iFotoActivo.InsertarFotoDB(uniqueID, String.valueOf(assetRoot), nombreFoto3,String.valueOf(currentItem), assetSysId);
            return save;
        }else if (!gpxFile4.exists()){
            FileWriter writer = new FileWriter(gpxFile4);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            String uniqueID = UUID.randomUUID().toString();
            boolean save = iFotoActivo.InsertarFotoDB(uniqueID, String.valueOf(assetRoot), nombreFoto4,String.valueOf(currentItem), assetSysId);
            return save;
        }else{
            showAlertDialogUnsuccessful(_activity, "Atención", "Ya no se pueden ingresar fotos nuevas, " +
                    "debe reescribir las existentes");
            return false;
        }
    }

    private boolean ReescribirArchivo(File assetRoot, String nombreArchivo, String FotoBase,int CurrentItem) throws IOException {
        String nombreFoto1 = nombreArchivo+"_"+String.valueOf(CurrentItem)+".txt";
        String nombreFoto2 = nombreArchivo+"_"+String.valueOf(CurrentItem)+".txt";
        String nombreFoto3 = nombreArchivo+"_"+String.valueOf(CurrentItem)+".txt";
        String nombreFoto4 = nombreArchivo+"_"+String.valueOf(CurrentItem)+".txt";

        File gpxFile = new File(assetRoot, nombreFoto1);
        File gpxFile2 = new File(assetRoot, nombreFoto2);
        File gpxFile3 = new File(assetRoot, nombreFoto3);
        File gpxFile4 = new File(assetRoot, nombreFoto4);

        if(gpxFile4.exists()){
            FileWriter writer = new FileWriter(gpxFile4);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            _reescribir = false;
            return true;
        }else if(gpxFile3.exists()){
            FileWriter writer = new FileWriter(gpxFile3);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            _reescribir = false;
            return true;
        }else if(gpxFile2.exists()){
            FileWriter writer = new FileWriter(gpxFile2);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            _reescribir = false;
            return true;
        }else if (gpxFile.exists()){
            FileWriter writer = new FileWriter(gpxFile);
            writer.append(FotoBase);
            writer.flush();
            writer.close();
            _reescribir = false;
            return true;
        }else{
            return false;
        }
    }

    public void finishCamera() {
        AbrirCamara();
    }
    public void regresar() {
        finish();
    }

    public void ReescribirFotoMessage(final File assetRoot,
                                             final String nombreArchivo,final String fotoABase64,
                                             final int _currentItem){

        final int final_currentItem = _currentItem;
        LayoutInflater inflater = _activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_alertas_informacion, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText("¡Alerta!");
        txvMessage.setText("Se reescribirá la foto. ¿Está seguro de esto?");
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(_activity);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Boolean reescrito = ReescribirArchivo(assetRoot, nombreArchivo,fotoABase64, final_currentItem);
                    if(reescrito){
                        Intent intent = new Intent(CarruselFotosActivo.this, FotosActivosActivity.class);
                        intent.putExtra("placaActivo", assetBarcode);
                        intent.putExtra("_idActivo", assetSysId);
                        intent.putExtra("_assetDescription", assetDescription);
                        finish();
                        startActivity(intent);
                        //showAlertDialogSuccesful(_activity, "Exitoso", "Se ha reescrito la imagen con exito");
                    }else{
                        showAlertDialogUnsuccessful(_activity, "Error", "No se ha podido reescribir la imagen");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();
    }

    public boolean showAlertDialogSuccesful(Activity activity, String title, String message){

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#0C9E0C'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();

        return  true;
    }

    public boolean showAlertDialogUnsuccessful(Activity activity, String title, String message){

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones_error, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#D81622'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.alertaicono);
        alertDialog = builder.show();
        return  true;
    }

    public void setPrevText(String text) {
        prevText = text;
        controlPosition(0);
    }

    public void setNextText(String text) {
        nextText = text;
        controlPosition(0);
    }

    public void setFinishText(String text) {
        finishText = text;
        controlPosition(0);
    }

    public void setCancelText(String text) {
        cancelText = text;
        controlPosition(0);
    }

    public void setGivePermissionText(String text) {
        givePermissionText = text;
        controlPosition(0);
    }

    public void setIndicatorSelected(int drawable) {
        selectedIndicator = drawable;
    }

    public void setIndicator(int drawable) {
        indicator = drawable;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            changeFragment(true);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}

    @Override
    public void currentFragmentPosition(int position) {}

    @Override
    public boolean InsertarFotoDB(String fotoID, String rutaFoto, String nombreFoto,
                                  String fotoDescripcion, String assetSysId) {return false;}

    @Override
    public boolean TieneFoto(String assetSysId) {
        return false;
    }
}
