package com.example.diverscan.activeid.Activo;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diverscan.activeid.R;

import java.util.List;

public class AdaptadorSubActivo extends RecyclerView.Adapter<AdaptadorSubActivo.ViewHolder> {

    private List<EntidadSubActivo> _subActivo;
    private String _Notificacion;



    public AdaptadorSubActivo(List<EntidadSubActivo> subAssets){
        this._subActivo = subAssets;
    }
    public AdaptadorSubActivo(String texto){
        this._Notificacion = texto;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private EntidadSubActivo subActivo;
        private TextView txtnumActivo,txtdescripcion,txtEncargado, txtEPc,txtPlaca, txtSubAssetsAvailables;
        private CardView TarjetaSubActivos;
        private String Notifica;



        public ViewHolder(View itemView){
            super(itemView);
            txtnumActivo = itemView.findViewById(R.id.txtNumSubActivo);
            txtdescripcion = itemView.findViewById(R.id.txtDescripcionSubActivo);
            txtPlaca = itemView.findViewById(R.id.txt_PlacaSubActivo);
            txtEPc=itemView.findViewById(R.id.txtEPCSubActivo);
            txtEncargado=itemView.findViewById(R.id.txtEncargadoSubActivo);
            TarjetaSubActivos = itemView.findViewById(R.id.crvSubActivo);
            txtSubAssetsAvailables = itemView.findViewById(R.id.txtSubAssetsAvailables);


        }

        public EntidadSubActivo getSubActivo(){
            return subActivo;
        }

        public void setSubActivo(EntidadSubActivo subActivos){
            this.subActivo = subActivos;
        }



    }

    @NonNull
    @Override
    public AdaptadorSubActivo.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subactivoadapter, viewGroup, false);
        AdaptadorSubActivo.ViewHolder viewHolder = new AdaptadorSubActivo.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorSubActivo.ViewHolder viewHolder, int i)
    {
        viewHolder.txtnumActivo.setText(_subActivo.get(i).getNumero());
        viewHolder.txtPlaca.setText(_subActivo.get(i).getCodeBar());
        viewHolder.txtdescripcion.setText(_subActivo.get(i).getDescripcion());
        viewHolder.txtEPc.setText(_subActivo.get(i).getTag());
        viewHolder.txtEncargado.setText(_subActivo.get(i).getAlias());
        viewHolder.txtSubAssetsAvailables.setVisibility(View.GONE);
        viewHolder.setSubActivo(_subActivo.get(i));
    }

    @Override
    public int getItemCount()
    {
        return _subActivo.size();
    }
}
