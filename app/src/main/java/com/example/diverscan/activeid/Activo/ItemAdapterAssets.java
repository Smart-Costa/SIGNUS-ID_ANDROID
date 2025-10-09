package com.example.diverscan.activeid.Activo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.diverscan.activeid.R;
import java.util.ArrayList;

public class ItemAdapterAssets extends ArrayAdapter {
    private Context context;
    private ArrayList<EntidadActivos> activoRecords;

    public ItemAdapterAssets(Context context, ArrayList activoRecords){

        super(context, R.layout.camposactivos, activoRecords);
        this.context = context;
        this.activoRecords = activoRecords;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.camposactivos, null);

        TextView descripcion = (TextView) item.findViewById(R.id.descripcionActivo);
        descripcion.setText(activoRecords.get(position).getDescripcion());

        TextView razonsocial = (TextView) item.findViewById(R.id.razonsocial);
        razonsocial.setText(activoRecords.get(position).getCompania());

        TextView edificio = (TextView) item.findViewById(R.id.edificio);
        edificio.setText(activoRecords.get(position).getEdificio());

        TextView piso = (TextView) item.findViewById(R.id.piso);
        piso.setText(activoRecords.get(position).getPiso());

        TextView oficina = (TextView) item.findViewById(R.id.oficina);
        oficina.setText(activoRecords.get(position).getOficina());

        TextView tag = (TextView) item.findViewById(R.id.tag);
        tag.setText(activoRecords.get(position).getTag());

        TextView numero = (TextView) item.findViewById(R.id.numeroActivo);
        numero.setText(activoRecords.get(position).getNumero());


        TextView placa = (TextView) item.findViewById(R.id.placaActivo);
        placa.setText(activoRecords.get(position).getCodeBar());

        TextView marca = (TextView) item.findViewById(R.id.marcaActivo);
        marca.setText(activoRecords.get(position).getMarca());

        TextView modelo = (TextView) item.findViewById(R.id.modeloActivo);
        modelo.setText(activoRecords.get(position).getModelo());

        TextView serie = (TextView) item.findViewById(R.id.serieActivo);
        serie.setText(activoRecords.get(position).getSerial());

        TextView encargado = (TextView) item.findViewById(R.id.encargadoActivo);
        encargado.setText(activoRecords.get(position).getAlias());

        return item;
    }

}
