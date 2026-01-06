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
    private OnItemClickListener itemClickListener;
    private OnGoToCountsClickListener goToCountsClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(TomaFisicaTomasEntity item);
    }

    public interface OnGoToCountsClickListener {
        void onGoToCountsClick(TomaFisicaTomasEntity item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(TomaFisicaTomasEntity item);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public TomaFisicaTomasAdapter(List<TomaFisicaTomasEntity> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.itemClickListener = listener;
        this.goToCountsClickListener = item -> listener.onItemClick(item);
    }

    public TomaFisicaTomasAdapter(List<TomaFisicaTomasEntity> lista) {
        this.lista = lista;
        this.itemClickListener = null;
        this.goToCountsClickListener = null;
    }

    public TomaFisicaTomasAdapter(
            List<TomaFisicaTomasEntity> lista,
            OnItemClickListener itemClickListener,
            OnGoToCountsClickListener goToCountsClickListener
    ) {
        this.lista = lista;
        this.itemClickListener = itemClickListener;
        this.goToCountsClickListener = goToCountsClickListener;
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

        holder.txtNumeroToma.setText("Toma " + item.getNumeroToma());
        
        // Fecha dd/mm/yyyy
        String fechaOriginal = item.getFechaCreacion(); // e.g., 2023-10-27T10:00:00
        String fechaFormateada = "";
        if (fechaOriginal != null && fechaOriginal.contains("T")) {
             try {
                 String[] parts = fechaOriginal.split("T")[0].split("-");
                 if (parts.length == 3) {
                     fechaFormateada = parts[2] + "/" + parts[1] + "/" + parts[0];
                 } else {
                     fechaFormateada = fechaOriginal.split("T")[0];
                 }
             } catch (Exception e) {
                 fechaFormateada = fechaOriginal;
             }
        } else {
            fechaFormateada = fechaOriginal;
        }
        holder.txtFecha.setText(fechaFormateada);

        // Uploaded status (Assuming if it's in the list it is uploaded/synced)
        // If needed, check a specific field. For now, always visible if item exists.
        holder.imgUploaded.setVisibility(View.VISIBLE);
        
        // Checkbox logic (if needed, currently just visual)
        holder.chkSeleccion.setChecked(false); 
        
        holder.btnGoToCounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goToCountsClickListener != null) {
                    goToCountsClickListener.onGoToCountsClick(item);
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("DEBUG_DELETE", "Click en eliminar: " + item.getIdToma());
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(item);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        });
    }

    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumeroToma, txtFecha;
        android.widget.CheckBox chkSeleccion;
        android.widget.ImageView imgUploaded;
        android.widget.ImageView btnGoToCounts;
        android.widget.ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumeroToma = itemView.findViewById(R.id.txtNumeroToma);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            chkSeleccion = itemView.findViewById(R.id.chkSeleccion);
            imgUploaded = itemView.findViewById(R.id.imgUploaded);
            btnGoToCounts = itemView.findViewById(R.id.btnGoToCounts);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
