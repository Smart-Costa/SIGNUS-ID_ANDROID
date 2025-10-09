package com.example.diverscan.activeid.Oficina;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diverscan.activeid.Activo.ActivoInventario;
import com.example.diverscan.activeid.R;

import java.util.List;

public class AdaptadorActivoXSector extends RecyclerView.Adapter<AdaptadorActivoXSector.ViewHolder> {
    private List<ActivoInventario> _activoInventarios;

    public AdaptadorActivoXSector(List<ActivoInventario> assetList) {
        this._activoInventarios = assetList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ActivoInventario activo;
        private TextView txtnumActivo,txtdescripcion,txtstatus, txtePC,txtsector;
        private CardView Tarjeta;

        public ViewHolder(View itemView){
            super(itemView);
            txtnumActivo = itemView.findViewById(R.id.txtNumActivo);
            txtdescripcion = itemView.findViewById(R.id.txtDescripcion);
            txtstatus = itemView.findViewById(R.id.txtStatus);
            txtePC=itemView.findViewById(R.id.txtEPC);
            txtsector=itemView.findViewById(R.id.txtSector);
            Tarjeta = itemView.findViewById(R.id.tarjeta);
        }
        public ActivoInventario getActivo() {
            return activo;
        }

        public void setActivo(ActivoInventario product) {
            this.activo = product;
        }
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.campos_activos_sector, viewGroup, false);
        AdaptadorActivoXSector.ViewHolder viewHolder = new AdaptadorActivoXSector.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.txtnumActivo.setText(_activoInventarios.get(i).getNumero());
        viewHolder.txtdescripcion.setText(""+_activoInventarios.get(i).getDescripcion());
        viewHolder.txtePC.setText(_activoInventarios.get(i).getEPC());
        viewHolder.txtsector.setText(""+_activoInventarios.get(i).getOficina());
        viewHolder.setActivo(_activoInventarios.get(i));
    }

    @Override
    public int getItemCount() {
        return _activoInventarios.size();
    }


}
