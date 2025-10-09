package com.example.diverscan.activeid.Inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.diverscan.activeid.R;
import java.util.ArrayList;

public class ItemAdapterInventories extends ArrayAdapter{

    private Context context;
    private ArrayList<Inventories> inventories;

    public ItemAdapterInventories (Context context, ArrayList inventories){
        super(context, R.layout.campos_tomafisica, inventories);

        this.context = context;
        this.inventories = inventories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.campos_tomafisica, null);

        TextView nombre = (TextView) item.findViewById(R.id.nombre_toma);
        nombre.setText(inventories.get(position).getTakeName());

        TextView descripcion = (TextView) item.findViewById(R.id.descripcion_toma);
        descripcion.setText(inventories.get(position).getTakeDescription());

        TextView fecha = (TextView) item.findViewById(R.id.fecha_toma);
        fecha.setText(inventories.get(position).getTakeDate());

        TextView fechaFinal = (TextView) item.findViewById(R.id.fecha_final);
        fechaFinal.setText(inventories.get(position).getfechaFinal());

        TextView estado = (TextView) item.findViewById(R.id.estado);
        estado.setText(inventories.get(position).getEstado());

        //Devolvemos la vista para que se muestre en el ListView.
        return item;
    }

}
