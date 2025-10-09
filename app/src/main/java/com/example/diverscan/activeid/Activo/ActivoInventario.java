package com.example.diverscan.activeid.Activo;

import java.io.Serializable;

public class ActivoInventario implements Serializable
{

    public String Numero;
    public String Descripcion;
    public String Status;
    public String EPC;
    public String AssetSysId;
    public String Oficina;
    public String IdOficina;

    public ActivoInventario()
    {
    }
    public ActivoInventario(String numero, String descripcion, String status, String EPC, String assetSysId, String oficina, String idOficina)
    {
        this.Numero = numero;
        this.Descripcion = descripcion;
        this.Status = status;
        this.EPC = EPC;
        this.AssetSysId = assetSysId;
        this.Oficina = oficina;
        this.IdOficina = idOficina;
    }

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

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
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
