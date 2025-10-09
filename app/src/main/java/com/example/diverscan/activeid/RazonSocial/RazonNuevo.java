package com.example.diverscan.activeid.RazonSocial;

import android.widget.AdapterView;

public class RazonNuevo {

        private  String _idRazon;
        private String _nombreRazon;

        public RazonNuevo (String idrazon, String nombrerazon){

            _idRazon = idrazon;
            _nombreRazon = nombrerazon;
        }

        public String getIdRazon() {
            return _idRazon;
        }
        public String getNombreRazon(){
            return _nombreRazon;
        }

        public void setIdRazon(String idRazon){
            _idRazon = idRazon;
        }

        public void setNombreRazon (String nombreRazon){
            _nombreRazon = nombreRazon;
        }

        @Override
        public String toString(){
            return _nombreRazon;
        }

    }

