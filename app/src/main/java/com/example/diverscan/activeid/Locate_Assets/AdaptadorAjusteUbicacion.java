package com.example.diverscan.activeid.Locate_Assets;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diverscan.activeid.Activo.AjustarActivoUbicacion;
import com.example.diverscan.activeid.Inventory.InventarioVisual;
import com.example.diverscan.activeid.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptadorAjusteUbicacion extends RecyclerView.Adapter<AdaptadorAjusteUbicacion.ViewHolder>{

    private List<InventarioVisual> _productList;
    private SparseBooleanArray Seleccionados;

    public interface OnLongPress{
        void onClicPress(View view, int position);
        void onLongPress(View view, int position);
    }
    public AdaptadorAjusteUbicacion(List<InventarioVisual> productList) {
        this._productList = productList;
        Seleccionados = new SparseBooleanArray();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements com.example.diverscan.activeid.Locate_Assets.ViewHolder {
        private InventarioVisual product;
        private boolean _clicked;
        private TextView txtnumActivo,txtdescripcion,txtstatus, txtePC,txtsector;
        private CardView Tarjeta;

        public ViewHolder(View itemView) {
            super(itemView);
            txtnumActivo = itemView.findViewById(R.id.txtNumActivo);
            txtdescripcion = itemView.findViewById(R.id.txtDescripcion);
            txtstatus = itemView.findViewById(R.id.txtStatus);
            txtePC=itemView.findViewById(R.id.txtEPC);
            txtsector=itemView.findViewById(R.id.txtSector);
            Tarjeta = itemView.findViewById(R.id.tarjeta);
        }

        @Override
        public void onClick(View view) {

        }


        public InventarioVisual getInventario() {
            return product;
        }

        public void setInventario(InventarioVisual product) {
            this.product = product;
        }

    }

    public void updateData() {
        _productList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.camposlecturaajuste,viewGroup,false);
        AdaptadorAjusteUbicacion.ViewHolder viewHolder = new AdaptadorAjusteUbicacion.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        final InventarioVisual inventarioVisual = _productList.get(i);
        final ArrayList<AjustarActivoUbicacion> ajustarActivoUbicacions = new ArrayList<>();
        final Map<String, AjustarActivoUbicacion> _activosSeleccionado = new HashMap<String, AjustarActivoUbicacion>();
        final Map<String, AjustarActivoUbicacion> _activosSinSeleccionar = new HashMap<String, AjustarActivoUbicacion>();
        viewHolder.txtnumActivo.setText(_productList.get(i).getNumero());
        viewHolder.txtdescripcion.setText(""+_productList.get(i).getDescripcion());
        viewHolder.txtstatus.setText(_productList.get(i).getStatus());
        viewHolder.txtePC.setText(_productList.get(i).getEPC());
        viewHolder.txtsector.setText(""+_productList.get(i).getOficina());
        viewHolder.setInventario(_productList.get(i));

        String status = _productList.get(i).getStatus();
        if (status.equals("Sin Asignar"))
        {
             viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(98, 217, 215));
             viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
             viewHolder.Tarjeta.setClickable(true);
        }
        else if (status.equals("Faltante"))
        {

             viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(229, 68, 68));
             viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
             viewHolder.Tarjeta.setClickable(true);
        }
        else if (status.equals("Encontrado"))
        {

             viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(85, 223, 129 ));
             viewHolder.Tarjeta.setClickable(true);
             viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
             viewHolder.Tarjeta.setCardBackgroundColor(Color.rgb(248,243,43 ));
             viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
             viewHolder.Tarjeta.setClickable(true);
        }

        viewHolder.Tarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int  colorPrevio = viewHolder.Tarjeta.getCardBackgroundColor().getDefaultColor();
                    inventarioVisual.setSelected(!inventarioVisual.isSelected());
                    viewHolder.Tarjeta.setBackgroundColor(inventarioVisual.isSelected() ?
                                Color.rgb(227, 227, 227) : colorPrevio);
                    AsignarUbicacionTodo asignarUbicacionTodo = new AsignarUbicacionTodo();

                    if(inventarioVisual.isSelected()) {
                        asignarUbicacionTodo._activosSeleccionado.put(_productList.get(i).getAssetSysId(), new AjustarActivoUbicacion(
                                _productList.get(i).getNumero(),
                                _productList.get(i).getAssetSysId(),
                                _productList.get(i).getIdOficina(),
                                _productList.get(i).getOficina(),
                                _productList.get(i).getDescripcion()
                        ));
                        if(asignarUbicacionTodo._activosSinSeleccionar.containsValue(_productList.get(i).getAssetSysId())){
                            asignarUbicacionTodo._activosSinSeleccionar.remove(_productList.get(i).getAssetSysId());
                        }
                    }else{
                        asignarUbicacionTodo._activosSinSeleccionar.put(_productList.get(i).getAssetSysId(), new AjustarActivoUbicacion(
                                _productList.get(i).getNumero(),
                                _productList.get(i).getAssetSysId(),
                                _productList.get(i).getIdOficina(),
                                _productList.get(i).getOficina(),
                                _productList.get(i).getDescripcion()
                        ));
                        if(asignarUbicacionTodo._activosSeleccionado.containsValue(_productList.get(i).getAssetSysId())){
                            asignarUbicacionTodo._activosSeleccionado.remove(_productList.get(i).getAssetSysId());
                        }
                    }
                }catch(Exception ex){
                   ex.printStackTrace();
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return _productList.size();
    }





}