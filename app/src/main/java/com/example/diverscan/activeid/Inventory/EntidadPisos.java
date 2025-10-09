package com.example.diverscan.activeid.Inventory;

public class EntidadPisos {

    String IdPiso;
    String NombrePiso;
    String IdEdificio;
    String Edificio;

    public EntidadPisos (String idPiso, String nombrePiso, String idEdificio, String edificio){

        this.IdPiso = idPiso;
        this.NombrePiso = nombrePiso;
        this.IdEdificio = idEdificio;
        this.Edificio = edificio;
    }

    public String getIdPiso(){
        return IdPiso;
    }

    public String getNombrePiso(){
        return NombrePiso;
    }

    public String getIdEdificio(){
        return IdEdificio;
    }

    public String getEdificio(){
        return Edificio;
    }

    public void setIdPiso(String idpiso){
        IdPiso = idpiso;
    }

    public void setNombrePiso(String nombrepiso){
        NombrePiso = nombrepiso;
    }

    public void setIdEdificio(String idedificio){
        IdEdificio = idedificio;
    }

    public void setEdificio(String edificio){
        IdEdificio = edificio;
    }

    @Override
    public String toString(){
        return this.NombrePiso;
    }


}
