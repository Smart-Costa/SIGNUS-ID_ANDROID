package com.example.diverscan.activeid.Inventory;

import androidx.annotation.NonNull;

public class EntidadTiposInventarios {

    String idTipoToma;
    String nombreTipoToma;
    String descripcionTipoToma;
    String fechaInicio;
    String fechaFinal;
    String estado;

    public EntidadTiposInventarios (String IdTipoToma, String NombreTipoToma, String DescripcionTipoToma){

        this.idTipoToma = IdTipoToma;
        this.nombreTipoToma = NombreTipoToma;
        this.descripcionTipoToma = DescripcionTipoToma;
    }

    public EntidadTiposInventarios() {
        // Constructor vacío, se pueden inicializar valores por defecto aquí si es necesario
    }

    public String getidTipoToma(){
        return idTipoToma;
    }
    public String getnombreTipoToma (){ return nombreTipoToma; }
    public String getdescripcionTipoToma (){ return descripcionTipoToma; }
    public String getfechaInicio (){ return fechaInicio; }
    public String getfechaFinal (){ return fechaFinal; }
    public String getestado (){ return estado; }

    public void setidTipoToma(String IdTipoToma){
        idTipoToma = IdTipoToma;
    }
    public void setnombreTipoToma (String NombreTipoToma){
        nombreTipoToma = NombreTipoToma;
    }
    public void setdescripcionTipoToma (String DescripcionTipoToma){ descripcionTipoToma = DescripcionTipoToma ;}
    public void setfechaInicio (String FechaInicio){
        fechaInicio = FechaInicio;
    }
    public void setfechaFinal (String FechaFinal){
        fechaFinal = FechaFinal;
    }
    public void setestado (String Estado){
        estado = Estado;
    }

    @Override
    public String toString() {
        return nombreTipoToma;
    }
}
