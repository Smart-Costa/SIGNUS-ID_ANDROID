package com.example.diverscan.activeid.TomasFisicas;

public class EntidadActivosInventarios {
    String Numero;
    String Descripcion;
    String EPC;
    String AssetSysId;
    String Oficina;
    String IdOficina;
    String IdPiso;
    String IdEdificio;
    String IdCompania;
    String UbicacionSecundaria;

    public EntidadActivosInventarios(String numero, String descripcion, String EPC, String assetSysId, String oficina, String idOficina, String idPiso, String idEdificio, String idCompania) {
        this(numero, descripcion, EPC, assetSysId, oficina, idOficina, idPiso, idEdificio, idCompania, null);
    }

    public EntidadActivosInventarios(String numero, String descripcion, String EPC, String assetSysId, String oficina, String idOficina, String idPiso, String idEdificio, String idCompania, String ubicacionSecundaria) {
        this.Numero = numero;
        this.Descripcion = descripcion;
        this.EPC = EPC;
        this.AssetSysId = assetSysId;
        this.Oficina = oficina;
        this.IdOficina = idOficina;
        this.IdPiso = idPiso;
        this.IdEdificio = idEdificio;
        this.IdCompania = idCompania;
        this.UbicacionSecundaria = ubicacionSecundaria;
    }

    public String getUbicacionSecundaria() { return UbicacionSecundaria; }
    public void setUbicacionSecundaria(String ubicacionSecundaria) { UbicacionSecundaria = ubicacionSecundaria; }

    public String getIdPiso() { return IdPiso; }
    public void setIdPiso(String idPiso) { IdPiso = idPiso; }

    public String getIdEdificio() { return IdEdificio; }
    public void setIdEdificio(String idEdificio) { IdEdificio = idEdificio; }

    public String getIdCompania() { return IdCompania; }
    public void setIdCompania(String idCompania) { IdCompania = idCompania; }


    public String getNumero() {
        return Numero;
    }

    public void setNumero(String numero) {
        Numero = numero;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getAssetSysId() {
        return AssetSysId;
    }

    public void setAssetSysId(String assetSysId) {
        AssetSysId = assetSysId;
    }

    public String getOficina() {
        return Oficina;
    }

    public void setOficina(String oficina) {
        Oficina = oficina;
    }

    public String getIdOficina() {
        return IdOficina;
    }

    public void setIdOficina(String idOficina) {
        IdOficina = idOficina;
    }
}
