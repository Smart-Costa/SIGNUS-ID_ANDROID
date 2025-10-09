package com.example.diverscan.activeid.Activo;

public class AjustarActivoUbicacion {

    public String Numero;
    public String AssetSysId;
    public String IdOficina;
    public String NombreOficina;
    public String Descripcion;

    public AjustarActivoUbicacion(String numero, String assetSysId, String idOficina,String oficinanombre,String descripcion) {
        this.Numero = numero;
        this.AssetSysId = assetSysId;
        this.IdOficina = idOficina;
        this.NombreOficina = oficinanombre;
        this.Descripcion = descripcion;
    }

    public String getNumero() {
        return Numero;
    }

    public void setNumero(String numero) {
        Numero = numero;
    }

    public String getAssetSysId() {
        return AssetSysId;
    }

    public void setAssetSysId(String assetSysId) {
        AssetSysId = assetSysId;
    }

    public String getIdOficina() {
        return IdOficina;
    }

    public void setIdOficina(String idOficina) {
        IdOficina = idOficina;
    }

    public String getNombreOficina() {
        return NombreOficina;
    }

    public void setNombreOficina(String nombreOficina) {
        NombreOficina = nombreOficina;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }
}
