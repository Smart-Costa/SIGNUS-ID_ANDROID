package com.example.diverscan.activeid.UI.tomasfisicas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;

import java.util.List;

public class TomaFisicaTomasAdapter extends RecyclerView.Adapter<TomaFisicaTomasAdapter.ViewHolder> {

    private List<TomaFisicaTomasEntity> lista;

    public TomaFisicaTomasAdapter(List<TomaFisicaTomasEntity> lista) {
        this.lista = lista;
    }

    public void actualizar(List<TomaFisicaTomasEntity> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_toma_fisica_tomas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TomaFisicaTomasEntity item = lista.get(position);

        holder.txtNumeroToma.setText(item.getNumeroToma());
        holder.txtIdToma.setText(item.getIdToma());
        holder.txtTotalLecturas.setText(item.getTotalLecturas());
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumeroToma, txtIdToma, txtTotalLecturas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumeroToma = itemView.findViewById(R.id.txtNumeroToma);
            txtIdToma = itemView.findViewById(R.id.txtIdToma);
            txtTotalLecturas = itemView.findViewById(R.id.txtTotalLecturas);
        }
    }
}
