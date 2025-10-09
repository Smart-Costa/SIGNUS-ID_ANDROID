package com.example.diverscan.activeid.Inventory;

public class InventarioVisual {

    String Numero;
    String Descripcion;
    String Status;
    String EPC;
    String AssetSysId;
    String Oficina;
    String IdOficina;
    boolean isSelected = false;
    public InventarioVisual() {

    }

    public InventarioVisual(String numero, String descripcion, String status, String EPC, String assetSysId, String oficina, String idOficina) {
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

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public boolean isSelected() {
        return isSelected;
    }

}