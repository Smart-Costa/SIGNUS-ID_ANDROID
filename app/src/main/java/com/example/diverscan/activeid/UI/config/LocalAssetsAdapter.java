package com.example.diverscan.activeid.UI.config;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import java.util.List;
import java.util.ArrayList;

public class LocalAssetsAdapter extends RecyclerView.Adapter<LocalAssetsAdapter.ViewHolder> {

    private List<ActivoEntity> originalList;
    private List<ActivoEntity> filteredList;

    public LocalAssetsAdapter(List<ActivoEntity> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void updateList(List<ActivoEntity> newList) {
        this.originalList = newList;
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            filteredList = new ArrayList<>(originalList);
        } else {
            String q = query.toLowerCase();
            List<ActivoEntity> temp = new ArrayList<>();
            for (ActivoEntity a : originalList) {
                boolean match = false;
                if (a.getEpc() != null && a.getEpc().toLowerCase().contains(q)) match = true;
                if (a.getNumeroActivo() != null && a.getNumeroActivo().toLowerCase().contains(q)) match = true;
                if (a.getDescripcionCorta() != null && a.getDescripcionCorta().toLowerCase().contains(q)) match = true;
                
                if (match) temp.add(a);
            }
            filteredList = temp;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivoEntity activo = filteredList.get(position);
        holder.tvDescripcion.setText(activo.getDescripcionCorta() != null ? activo.getDescripcionCorta() : "Sin descripción");
        holder.tvEpc.setText(activo.getEpc() != null ? activo.getEpc() : "N/A");
        holder.tvPlaca.setText(activo.getNumeroActivo() != null ? activo.getNumeroActivo() : "N/A");
        
        // Estado Sync (no tenemos campo explicito visible en Entity facil, pero asumimos synced si esta aqui y no es nuevo)
        // Por ahora hardcodeamos o usamos logica simple
        holder.tvEstadoSync.setVisibility(View.GONE); 
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvEpc, tvPlaca, tvEstadoSync;

        ViewHolder(View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            tvEpc = itemView.findViewById(R.id.tv_epc);
            tvPlaca = itemView.findViewById(R.id.tv_placa);
            tvEstadoSync = itemView.findViewById(R.id.tv_estado_sync);
        }
    }
}