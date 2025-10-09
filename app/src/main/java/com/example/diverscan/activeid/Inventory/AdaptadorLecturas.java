package com.example.diverscan.activeid.Inventory;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.diverscan.activeid.R;

import java.util.ArrayList;

public class AdaptadorLecturas extends RecyclerView.Adapter<AdaptadorLecturas.ViewHolder> {
    private ArrayList<InventarioVisual> _inventarioVisuals;
    private AdapterView.OnItemClickListener Seleccion;


    public AdaptadorLecturas(ArrayList<InventarioVisual> inventarioVisuals) {
        this._inventarioVisuals = inventarioVisuals;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numero, descripcion, status, Epc, Sector;
        private CardView Tarjeta;

        public ViewHolder(View item) {
            super(item);
            numero = (TextView) item.findViewById(R.id.txtNumActivo);
            descripcion = (TextView) item.findViewById(R.id.txtDescripcion);
            Epc = (TextView) item.findViewById(R.id.txtEPC);
            Sector = (TextView) item.findViewById(R.id.txtSector);
            Tarjeta = item.findViewById(R.id.tarjeta);
        }
    }

    @NonNull
    @Override
    public AdaptadorLecturas.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.campos_lectura_inventario, viewGroup, false);
        AdaptadorLecturas.ViewHolder viewHolder = new AdaptadorLecturas.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorLecturas.ViewHolder viewHolder, int i) {

        viewHolder.numero.setText(_inventarioVisuals.get(i).getNumero());
        viewHolder.descripcion.setText(_inventarioVisuals.get(i).getDescripcion());
        viewHolder.Epc.setText(_inventarioVisuals.get(i).getEPC());
        viewHolder.Sector.setText(_inventarioVisuals.get(i).getOficina());

        String status = _inventarioVisuals.get(i).getStatus();
        if (status.equals("Faltante")) {
            viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(229, 68, 68));
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.Tarjeta.setClickable(true);
        }

        if (status.equals("Encontrado")) {
            viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(85, 223, 129));
            viewHolder.Tarjeta.setClickable(true);
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (status.equals("No Pertenece")) {
            viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(248, 243, 43));
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.Tarjeta.setClickable(true);
        }

    }

    @Override
    public int getItemCount() {
        return _inventarioVisuals.size();
    }
}




