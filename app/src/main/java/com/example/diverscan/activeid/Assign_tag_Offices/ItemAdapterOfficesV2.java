package com.example.diverscan.activeid.Assign_tag_Offices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.diverscan.activeid.R;
import java.util.ArrayList;

public class ItemAdapterOfficesV2 extends ArrayAdapter {  // extiende diferente

    private Context context;
    private ArrayList<Offices> offices;

    public ItemAdapterOfficesV2(Context context, ArrayList offices) {
        super(context, R.layout.campossector, offices);
        // Guardamos los parámetros en variables de clase.
        this.context = context;
        this.offices = offices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.campossector, null);

        TextView oficina = (TextView) item.findViewById(R.id.oficina);
        oficina.setText(offices.get(position).getOficina());

        TextView piso = (TextView) item.findViewById(R.id.piso);
        piso.setText(offices.get(position).getPiso());

        TextView edificio = (TextView) item.findViewById(R.id.edificio);
        edificio.setText(offices.get(position).getEdificio());

        TextView razonsocial = (TextView) item.findViewById(R.id.razonsocial);
        razonsocial.setText(offices.get(position).getRazonSocial());

        TextView tag = (TextView) item.findViewById(R.id.tag);
        tag.setText(offices.get(position).getTag());

        // Devolvemos la vista para que se muestre en el ListView.
        return item;
    }



}
