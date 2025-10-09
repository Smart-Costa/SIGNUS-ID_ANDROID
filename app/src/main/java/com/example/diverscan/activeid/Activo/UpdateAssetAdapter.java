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

public class UpdateAssetAdapter extends RecyclerView.Adapter<UpdateAssetAdapter.ViewHolder>  {
    private List<EntidadActivos> _activosActualizar;
    private OnItemClickListener _listener;

    public UpdateAssetAdapter(List<EntidadActivos> assetList) {
        this._activosActualizar = assetList;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this._listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private EntidadActivos activo;
        private TextView txtnumActivo,txtdescripcion, txtEPC,txtsector, txtRazonSocial, txtPlaca;
        private TextView txtEdificio, txtPiso, txtMarca, txtModelo, txtSerie, txtEncargado;
        private CardView Tarjeta;

        public ViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);

            txtdescripcion =  itemView.findViewById(R.id.descripcionActivo);
            txtRazonSocial =  itemView.findViewById(R.id.razonsocial);
            txtEdificio =  itemView.findViewById(R.id.edificio);
            txtPiso =  itemView.findViewById(R.id.piso);
            txtsector =  itemView.findViewById(R.id.oficina);
            txtEPC =  itemView.findViewById(R.id.tag);
            txtnumActivo =  itemView.findViewById(R.id.numeroActivo);
            txtPlaca =  itemView.findViewById(R.id.placaActivo);
            txtMarca =  itemView.findViewById(R.id.marcaActivo);
            txtModelo =  itemView.findViewById(R.id.modeloActivo);
            txtSerie =  itemView.findViewById(R.id.serieActivo);
            txtEncargado = itemView.findViewById(R.id.encargadoActivo);
            Tarjeta = itemView.findViewById(R.id.tarjeta2);

            Tarjeta.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
        public EntidadActivos getActivo() {
            return activo;
        }

        public void setActivo(EntidadActivos product) {
            this.activo = product;
        }

        @Override
        public void onClick(View v) {
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.camposactivos, viewGroup, false);
        UpdateAssetAdapter.ViewHolder viewHolder = new UpdateAssetAdapter.ViewHolder(view, _listener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        viewHolder.txtdescripcion.setText(_activosActualizar.get(i).getDescripcion());
        viewHolder.txtRazonSocial.setText(_activosActualizar.get(i).getCompania());
        viewHolder.txtEdificio.setText(_activosActualizar.get(i).getEdificio());
        viewHolder.txtPiso.setText(_activosActualizar.get(i).getPiso());
        viewHolder.txtsector.setText(_activosActualizar.get(i).getOficina());
        viewHolder.txtEPC.setText(_activosActualizar.get(i).getTag());
        viewHolder.txtnumActivo.setText(_activosActualizar.get(i).getNumero());
        viewHolder.txtPlaca.setText(_activosActualizar.get(i).getCodeBar());
        viewHolder.txtMarca.setText(_activosActualizar.get(i).getMarca());
        viewHolder.txtModelo.setText(_activosActualizar.get(i).getModelo());
        viewHolder.txtSerie.setText(_activosActualizar.get(i).getSerial());
        viewHolder.txtEncargado.setText(_activosActualizar.get(i).getAlias());

        viewHolder.setActivo(_activosActualizar.get(i));
    }

    @Override
    public int getItemCount() {
        return _activosActualizar.size();
    }

}
