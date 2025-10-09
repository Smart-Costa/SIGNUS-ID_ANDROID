package com.example.diverscan.activeid.Inventory;

public class EntidadTomaFisicaEPC {

   public String idOficina;
public String NombreOficina;

    public EntidadTomaFisicaEPC( String nombreOficina, String IdOficina){

        this.idOficina = IdOficina;
        this.NombreOficina = nombreOficina;
    }

    public String getIdOficina() {
        return idOficina;
    }

    public void setIdOficina(String idOficina) {
        this.idOficina = idOficina;
    }

    public String getNombreOficina() {
        return NombreOficina;
    }

    public void setNombreOficina(String nombreOficina) {
        NombreOficina = nombreOficina;
    }
}
