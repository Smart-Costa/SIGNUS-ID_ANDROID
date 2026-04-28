package com.example.diverscan.activeid.UI.tomasfisicas;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;

import java.util.List;

public class RegistroTomaFisicaAdapter extends RecyclerView.Adapter<RegistroTomaFisicaAdapter.ViewHolder> {

    private List<TomaFisicaEntity> lista;

    public RegistroTomaFisicaAdapter(List<TomaFisicaEntity> lista) {
        this.lista = lista;
    }

    public void update(List<TomaFisicaEntity> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegistroTomaFisicaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_toma_fisica, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroTomaFisicaAdapter.ViewHolder holder, int position) {
        TomaFisicaEntity item = lista.get(position);

        holder.itemNombre.setText(item.getNombre() != null ? item.getNombre() : "");
        holder.itemFechaInicial.setText(formatearFecha(item.getFechaInicial()));
        holder.itemFechaFinal.setText(formatearFecha(item.getFechaFinal()));

        holder.btnLupa.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetalleTomaFisicaActivity.class);
            intent.putExtra("tomaFisicaId", item.getTomaFisicaId());
            intent.putExtra("nombre", item.getNombre());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetalleTomaFisicaActivity.class);
            intent.putExtra("tomaFisicaId", item.getTomaFisicaId());
            intent.putExtra("nombre", item.getNombre());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return "";

        try {
            if (fecha.contains(" ")) {
                return fecha.split(" ")[0];
            }

            if (fecha.contains("T")) {
                return fecha.substring(0, 10);
            }

            if (fecha.length() >= 10) {
                return fecha.substring(0, 10);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fecha;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemNombre, itemFechaInicial, itemFechaFinal;
        ImageView btnLupa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNombre = itemView.findViewById(R.id.itemNombre);
            itemFechaInicial = itemView.findViewById(R.id.itemFechaInicial);
            itemFechaFinal = itemView.findViewById(R.id.itemFechaFinal);
            btnLupa = itemView.findViewById(R.id.btnLupa);
        }
    }
}

