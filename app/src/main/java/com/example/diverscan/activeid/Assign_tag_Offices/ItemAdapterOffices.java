package com.example.diverscan.activeid.Assign_tag_Offices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.diverscan.activeid.R;

import java.util.ArrayList;

public class ItemAdapterOffices extends BaseAdapter {
    private Context context;
    private ArrayList<Offices> offices;
    protected LayoutInflater _Inflater;
    protected Activity myActivity;


    public ItemAdapterOffices(LayoutInflater _Inflater, ArrayList<Offices> offices_, Activity activity){
        offices=offices_;
        this._Inflater=_Inflater;
        this.myActivity=activity;
    }

    public ArrayList<Offices> offices() {
        return offices;
    }

    public void setOffices(ArrayList<Offices> offices_) {
        offices = offices_;
    }

    public LayoutInflater get_Inflater() {
        return _Inflater;
    }

    public void set_Inflater(LayoutInflater _Inflater) {
        this._Inflater = _Inflater;
    }

    @Override
    public int getCount() {
        return this.offices.size();
    }

    @Override
    public Object getItem(int position) {
        return this.offices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (convertView == null) {
            // Create a new view into the list.
          //  LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //rowView = inflater.inflate(R.layout.campossector, parent, false);
            rowView = this._Inflater.inflate(R.layout.campossector, null);
        }

        TextView oficina= (TextView) rowView.findViewById(R.id.oficina);

        TextView piso= (TextView) rowView.findViewById(R.id.piso);
        TextView edificio= (TextView) rowView.findViewById(R.id.edificio);
        TextView razonsocial= (TextView) rowView.findViewById(R.id.razonsocial);
        TextView tag= (TextView) rowView.findViewById(R.id.tag);

        Offices office = this.offices.get(position);
        oficina.setText(office.getOficina());
       // piso.setText(office.getPiso());
       // edificio.setText(office.getEdificio());
       // razonsocial.setText(office.getRazonSocial());
       // tag.setText(office.getTag());

        return rowView;

    }
}
