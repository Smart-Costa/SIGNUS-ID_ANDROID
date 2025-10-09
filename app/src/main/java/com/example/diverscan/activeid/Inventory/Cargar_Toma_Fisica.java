package com.example.diverscan.activeid.Inventory;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.Utilities.Fechas;
import com.example.diverscan.activeid.login.LoginActivity;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Cargar_Toma_Fisica extends AppCompatActivity {

    private static Context _context;
    private View Cargar_Toma_FisicaView;
    //private EditText txt_fecha_tomaView;
    private ListView ListTomas;
    private String FechaIngresada;
    private DatePickerDialog picker;
    Button btn_buscar;
    String sFechaInicio = "";

    InventoryDBHelper InventoryDBHelper;

    ArrayList<Inventories> inventories = new ArrayList<Inventories>();

    private long startTime=1*60*15000;
    private final long interval = 1*1000;
    CountDownTimer sessionActivate;

    //****************************************************************************************************
    //Tomás físicas
    public void eventos(){
        //txt_fecha_tomaView.setOnClickListener(TomarFecha);
        btn_buscar.setOnClickListener(OnClickListenerBuscarTomas);
    }

    //****************************************************************************************************
    //Tomás físicas
    private Button.OnClickListener OnClickListenerBuscarTomas = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            inventories.clear();
            VerTomasFisicas();
        }
    };

    //****************************************************************************************************
    //Tomás físicas
    public void VerTomasFisicas() {
        Cursor cursor = InventoryDBHelper.VerTomasFisicas();
        if (cursor == null) {
            Toast.makeText(getApplicationContext(), "No se pudieron leer las tomas.", Toast.LENGTH_LONG).show();
            return;
        }

        inventories.clear();

        try {
            if (cursor.getCount() == 0) {
                Toast.makeText(getApplicationContext(), "No se encontraron tomas para la fecha ingresada!", Toast.LENGTH_LONG).show();
                return;
            }

            // Calcula los índices UNA sola vez; si un nombre no existe lanza IllegalArgumentException
            final int colId          = cursor.getColumnIndexOrThrow("_id");
            final int colNombre      = cursor.getColumnIndexOrThrow("Nombre");
            final int colDescripcion = cursor.getColumnIndexOrThrow("Descripcion");
            final int colFechaIni    = cursor.getColumnIndexOrThrow("fechaInicio");
            final int colFechaFin    = cursor.getColumnIndexOrThrow("fechaFinal");
            final int colEstado      = cursor.getColumnIndexOrThrow("estado");

            while (cursor.moveToNext()) {
                String _id            = cursor.getString(colId);
                String takeName       = cursor.isNull(colNombre)      ? "" : cursor.getString(colNombre).trim();
                String takeDesc       = cursor.isNull(colDescripcion) ? "" : cursor.getString(colDescripcion).trim();
                String takeDate       = cursor.isNull(colFechaIni)    ? "" : cursor.getString(colFechaIni);
                String fechaFinal     = cursor.isNull(colFechaFin)    ? "" : cursor.getString(colFechaFin);

                // Si 'estado' es INT en la tabla, léelo como int
                int estadoNumInt = cursor.getType(colEstado) == Cursor.FIELD_TYPE_STRING
                        ? safeParseInt(cursor.getString(colEstado))
                        : cursor.getInt(colEstado);

                String estado;
                switch (estadoNumInt) {
                    case 1: estado = "Pendiente";   break;
                    case 2: estado = "Completada";  break;
                    case 3: estado = "Atrasada";    break;
                    case 4: estado = "Por aprobar"; break;
                    default: estado = "Desconocido"; break;
                }

                Inventories inventory = new Inventories();
                inventory.set_id(_id);
                inventory.setTakeName(takeName);
                inventory.setTakeDescription(takeDesc);
                inventory.setTakeDate(takeDate);
                inventory.setfechaFinal(fechaFinal);
                inventory.setEstado(estado);

                inventories.add(inventory);
            }

            inflateListViewInventories(inventories);

        } catch (IllegalArgumentException badColumn) {
            // Esto te dirá si algún nombre de columna no coincide con la tabla real
            Log.e("VerTomasFisicas", "Columna no encontrada: " + badColumn.getMessage()
                    + " | columnas: " + Arrays.toString(cursor.getColumnNames()));
            Toast.makeText(getApplicationContext(), "Error de columnas en la base local.", Toast.LENGTH_LONG).show();
        } finally {
            if (!cursor.isClosed()) cursor.close();
        }
    }

    // Helper para convertir seguro String->int
    private int safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }


    //****************************************************************************************************
    //Tomás físicas
    public void inflateListViewInventories (ArrayList<Inventories> inventory)
    {
        ItemAdapterInventories adapter = new ItemAdapterInventories(this, inventory);
        ListTomas.setAdapter(adapter);
        ListTomas.setOnItemClickListener(ObtenerTomaFisica);
    }

    //****************************************************************************************************
    //Tomás físicas
    private ListView.OnItemClickListener ObtenerTomaFisica = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3){
            Intent intent = new Intent(Cargar_Toma_Fisica.this, ElegirUbicacion_TomaFisica.class);
            intent.putExtra("Take_ID", inventories.get(position)._id);
            intent.putExtra("Take_Name", inventories.get(position).TakeName);
            intent.putExtra("Take_Description", inventories.get(position).TakeDescription);
            intent.putExtra("Take_Date", inventories.get(position).TakeDate);
            //intent.putExtra("fechaFinal", inventories.get(position).fechaFinal);
            //intent.putExtra("estado", inventories.get(position).estado);
            startActivity(intent);
        }
    };

    //****************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar__toma__fisica);

        ListTomas = (ListView) findViewById(R.id.listViewTomas);

        controles();
        eventos();

        _context = this;
        //setCurrentDate();

        sessionActivate = new CountDownTimer(startTime, interval){

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Intent intent = new Intent(Cargar_Toma_Fisica.this, LoginActivity.class);
                startActivity(intent);
            }
        }.start();
    }

    //****************************************************************************************************
    @Override
    public void onUserInteraction(){
        super.onUserInteraction();
        sessionActivate.cancel();
        sessionActivate.start();
    }

    //****************************************************************************************************

    public void controles(){
        Cargar_Toma_FisicaView = findViewById(R.id.Cargar_Toma_Fisica);
        btn_buscar = (Button) findViewById(R.id.btn_buscar);
        //txt_fecha_tomaView = (EditText) findViewById(R.id.txt_fecha_toma);
        InventoryDBHelper = new InventoryDBHelper(Cargar_Toma_FisicaView.getContext());
    }

    //****************************************************************************************************

//    private void setCurrentDate()
//    {
//        txt_fecha_tomaView.setText(Fechas.FechaActual());
//    }

    //****************************************************************************************************

//    private void ObtenerFechaInicio()
//    {
//        try{
//            int day = Fechas.fechora_dia_int(txt_fecha_tomaView.getText().toString());
//            int month = (Fechas.fechora_mes_int(txt_fecha_tomaView.getText().toString()) - 1);
//            int year = Fechas.fechora_ano_int(txt_fecha_tomaView.getText().toString());
//
//            picker = new DatePickerDialog(_context, new DatePickerDialog.OnDateSetListener() {
//                @Override
//                public void onDateSet(DatePicker view, int ano, int mes, int dia) {
//                    txt_fecha_tomaView.setText(Fechas.fecha_pantalla(dia, (mes + 1), ano));
//                }
//            }, year, month, day);
//
//            picker.show();
//        }
//        catch (Exception ex)
//        {
//            Log.w(ex.getMessage(), ex.getStackTrace().toString());
//        }
//    }

    //****************************************************************************************************

    public Date ObtenerFecha(String strfecha){
        Date date;

        try {
            String str_date =strfecha;
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            date = formatter.parse(str_date);
            return date;
        }
        catch (ParseException ex) {

            ex.printStackTrace();
            return null;
        }
    }

    //****************************************************************************************************

    private Button.OnClickListener TomarFecha = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //ObtenerFechaInicio();
        }};
    }
