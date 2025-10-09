package com.example.diverscan.activeid.SubActivos;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.diverscan.activeid.Activo.EntidadSubActivo;
import com.example.diverscan.activeid.R;

import java.util.ArrayList;

public class AdapterSubActivoSwipe extends RecyclerView.Adapter<AdapterSubActivoSwipe.ViewHolder> {

    ArrayList<EntidadSubActivo> _entidadSubActivo;

    public AdapterSubActivoSwipe(ArrayList<EntidadSubActivo> entidadSubActivo) {
        this._entidadSubActivo = entidadSubActivo;
    }
    public AdapterSubActivoSwipe() {
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public EntidadSubActivo getSubActivo() {
            return subActivo;
        }

        public void setSubActivo(EntidadSubActivo subActivo) {
            this.subActivo = subActivo;
        }

        private EntidadSubActivo subActivo;
        private TextView txtnumActivo,txtdescripcion,txtEncargado, txtEPc,txtPlaca, txtSubAssetsAvailables;
        public RelativeLayout deleteView, infoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtnumActivo = itemView.findViewById(R.id.txtSwipedNumero);
            txtdescripcion = itemView.findViewById(R.id.txtSwipedDescrip);
            txtPlaca = itemView.findViewById(R.id.txtSwipedPlaca);
            //txtEPc=itemView.findViewById(R.id.txtEPCSubActivo);
            txtEncargado=itemView.findViewById(R.id.txtSwipedEncargado);
            deleteView = itemView.findViewById(R.id.deleteBackgroundView);
            infoView = itemView.findViewById(R.id.rlInfo);
        }
    }

    public void RemoveAsset(int position){
        _entidadSubActivo.remove(position);
        notifyItemRemoved(position);
    }

    public void RestoreAsset(EntidadSubActivo item, int position){
        _entidadSubActivo.add(position, item);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.lista_subactivos_swipe, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.txtnumActivo.setText("Número: "+_entidadSubActivo.get(i).getNumero());
        viewHolder.txtPlaca.setText("Placa: "+_entidadSubActivo.get(i).getCodeBar());
        viewHolder.txtdescripcion.setText("Descripción: "+_entidadSubActivo.get(i).getDescripcion());
        viewHolder.txtEncargado.setText("Encargado: "+_entidadSubActivo.get(i).getAlias());
        //viewHolder.setSubActivo(_entidadSubActivo.get(i));
    }

    @Override
    public int getItemCount() {
        if(_entidadSubActivo.size() == 0){
            return 0;
        }
        return _entidadSubActivo.size();
    }


}
