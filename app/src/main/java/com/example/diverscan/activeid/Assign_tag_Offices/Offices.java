package com.example.diverscan.activeid.Assign_tag_Offices;

import java.io.Serializable;

public class Offices implements Serializable {
    public String Oficina;
    String Piso;
    String Edificio;
    String RazonSocial;
    String Tag;
    public String IdOficina;

    public Offices (String oficina,  String piso, String edificio, String razonSocial, String tag, String idOficina) {
        this.Oficina = oficina;
        this.Piso = piso;
        this.Edificio = edificio;
        this.RazonSocial = razonSocial;
        this.Tag = tag;
        this.IdOficina = idOficina;
    }

    public Offices(){}

    public String getIdOficina(){
        String idOficina;
        idOficina = this.IdOficina;
        return idOficina;
    }

    public void setIdOficina(String idOficina){
        this.IdOficina = idOficina;
    }

    public String getOficina() {
        String oficina;
        oficina = this.Oficina;
        return oficina;
    }

    public void setOficina(String oficina) {
        this.Oficina = oficina;
    }


    public String getPiso() {
        String piso;
        piso = this.Piso;
        return piso;
    }

    public void setPiso(String piso) {
        this.Piso = piso;
    }


    public String getEdificio() {
        String edificio;
        edificio = this.Edificio;
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.Edificio = edificio;
    }


    public String getRazonSocial() {
        String razonsocial;
        razonsocial = this.RazonSocial;
        return razonsocial;
    }

    public void setRazonSocial(String razonsocial) {
        this.RazonSocial = razonsocial;
    }

    public String getTag() {
        String tag;
        tag = this.Tag;
        return tag;
    }

    public void setTag(String tag) {
        this.Tag = tag;
    }
}

