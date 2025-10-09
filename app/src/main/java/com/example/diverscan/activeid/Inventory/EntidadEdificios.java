package com.example.diverscan.activeid.Inventory;

public class EntidadEdificios {

    String IdEdificio;
    String NombreEdificio;
    String IdRazonSocial;
    String RazonSocial;

    public EntidadEdificios (String idedificio, String nombreedificio, String idrazon, String razon){

        this.IdEdificio = idedificio;
        this.NombreEdificio = nombreedificio;
        this.IdRazonSocial = idrazon;
        this.RazonSocial = razon;
    }

    public String getIdEdificio(){
        return IdEdificio;
    }

    public String getNombreEdificio (){
        return NombreEdificio;
    }

    public String getIdRazonSocial (){
        return IdRazonSocial;
    }

    public String getRazonSocial (){
        return RazonSocial;
    }

    public void setIdEdificio(String idEdificio){
        IdEdificio = idEdificio;
    }

    public void setNombreEdificio (String nombreedificio){
        NombreEdificio = nombreedificio;
    }

    public void setIdRazonSocial (String idRazonSocial){
        IdRazonSocial  = idRazonSocial;
    }

    public void setRazonSocial (String razonsocial){
        RazonSocial  = razonsocial;
    }

    @Override
    public String toString(){
        return this.NombreEdificio;
    }


}



