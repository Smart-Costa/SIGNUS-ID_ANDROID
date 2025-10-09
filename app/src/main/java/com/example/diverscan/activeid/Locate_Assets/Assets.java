package com.example.diverscan.activeid.Locate_Assets;

import java.io.Serializable;

public class Assets implements Serializable {
    String NumeroActivo;
    String Descripcion;
    String RazonS;
    String Edificio;
    String Piso;
    String Oficina;
    String Tag;

    public Assets (String numeroactivo, String descripcion, String razons,  String edificio, String piso, String oficina, String tag) {
        this.NumeroActivo = numeroactivo;
        this.Descripcion = descripcion;
        this.RazonS = razons;
        this.Edificio = edificio;
        this.Piso = piso;
        this.Oficina = oficina;
        this.Tag = tag;
    }

    public String getNumeroActivo() {
        String numeroAct;
        numeroAct = this.NumeroActivo;
        return numeroAct;
    }

    public void setNumeroActivo(String numeroAct) {

        this.NumeroActivo = numeroAct;
    }


    public String getDescripcion() {
        String descripcion;
        descripcion = this.Descripcion;
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.Descripcion = descripcion;
    }


    public String getRazonS() {
        String razons;
        razons = this.RazonS;
        return razons;
    }

    public void setRazonS (String razons) {
        this.RazonS = razons;
    }


    public String getEdificio() {
        String edificio;
        edificio = this.Edificio;
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.Edificio = edificio;
    }

    public String getPiso() {
        String piso;
        piso = this.Piso;
        return piso;
    }

    public void setPiso(String piso) {
        this.Piso = piso;
    }

    public String getOficina() {
        String oficina;
        oficina = this.Oficina;
        return oficina;
    }

    public void setOficina(String oficina) {
        this.Oficina = oficina;
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
