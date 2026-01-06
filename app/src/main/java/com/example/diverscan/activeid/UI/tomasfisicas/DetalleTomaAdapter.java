package com.example.diverscan.activeid.UI.tomasfisicas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;

import java.util.List;

public class DetalleTomaAdapter extends RecyclerView.Adapter<DetalleTomaAdapter.ViewHolder> {

    private List<ActivoEntity> lista;

    public DetalleTomaAdapter(List<ActivoEntity> lista) {
        this.lista = lista;
    }

    public void updateList(List<ActivoEntity> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_toma_fisica_detalle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivoEntity item = lista.get(position);

        holder.txtNombreActivo.setText(item.getDescripcionCorta() != null ? item.getDescripcionCorta() : "Sin Nombre");
        holder.txtSerie.setText("S/N: " + (item.getNumeroSerie() != null ? item.getNumeroSerie() : "-"));
        holder.txtPlaca.setText("Placa: " + (item.getNumeroActivo() != null ? item.getNumeroActivo() : "-"));
        holder.txtActivoNo.setText("Activo No: " + (item.getNumeroEtiqueta() != null ? item.getNumeroEtiqueta() : "-"));
        
        // Conteo simulado 1 de 1 ya que son unicos en la lista visual por ahora
        holder.txtConteo.setText("1 de 1");

        // Estado: Asumimos verde si está inventariado, o lógica según corresponda. 
        // Por ahora hardcodeamos verde o rojo según lógica de negocio.
        // El usuario pidió "punto rojo" en la imagen, quizás por defecto o si falta algo.
        // Pondremos rojo por defecto como en la imagen.
        holder.imgEstado.setImageResource(R.drawable.shape_circle_red);
        
        // TODO: Cargar imagen real si existe en item.getFotos()
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreActivo, txtSerie, txtPlaca, txtActivoNo, txtConteo;
        ImageView imgFoto, imgEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreActivo = itemView.findViewById(R.id.txtNombreActivo);
            txtSerie = itemView.findViewById(R.id.txtSerie);
            txtPlaca = itemView.findViewById(R.id.txtPlaca);
            txtActivoNo = itemView.findViewById(R.id.txtActivoNo);
            txtConteo = itemView.findViewById(R.id.txtConteo);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            imgEstado = itemView.findViewById(R.id.imgEstado);
        }
    }
}
