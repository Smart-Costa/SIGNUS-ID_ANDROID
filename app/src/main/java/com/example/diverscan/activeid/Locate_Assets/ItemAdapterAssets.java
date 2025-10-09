package com.example.diverscan.activeid.Locate_Assets;

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
    private ArrayList<Assets> assets;

    public ItemAdapterAssets(Context context, ArrayList assets) {
        super(context, R.layout.campos_locate, assets);
        // Guardamos los parámetros en variables de clase.
        this.context = context;
        this.assets = assets;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){

        LayoutInflater inflaterAssets = LayoutInflater.from(context);
        View item = inflaterAssets.inflate(R.layout.campos_locate, null);

        TextView numeroactivo = (TextView) item.findViewById(R.id.NumActivoL);
        numeroactivo.setText(assets.get(position).getNumeroActivo());

        TextView descripcion = (TextView) item.findViewById(R.id.DescripcionL);
        descripcion.setText(assets.get(position).getDescripcion());

        TextView razons = (TextView) item.findViewById(R.id.RazonL);
        razons.setText(assets.get(position).getRazonS());

        TextView edificio = (TextView) item.findViewById(R.id.edificioL);
        edificio.setText(assets.get(position).getEdificio());

        TextView piso = (TextView) item.findViewById(R.id.pisoL);
        piso.setText(assets.get(position).getPiso());

        TextView oficina = (TextView) item.findViewById(R.id.oficinaL);
        oficina.setText(assets.get(position).getOficina());

        TextView tag = (TextView) item.findViewById(R.id.tagL);
        tag.setText(assets.get(position).getTag());

        return item;

    }
}
