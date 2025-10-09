package com.example.diverscan.activeid.Inventory;

public class EntidadTomasInventario {
    public String idTakeInventory;
    public String dateTakeInventory;
    public String Usuario;
    public String Oficina;
    public String tiposDeInventario;

    public EntidadTomasInventario(String IdTakeInventory, String DateTakeInventory,  String usuario, String oficina,String TiposDeInventario){
        this.idTakeInventory = IdTakeInventory;
        this.dateTakeInventory = DateTakeInventory;
        this.Oficina = oficina;
        this.Usuario = usuario;
        this.tiposDeInventario= TiposDeInventario;
    }

    public String getIdTakeInventory() {
        return idTakeInventory;
    }


    public void setIdTakeInventory(String idTakeInventory) {
        this.idTakeInventory = idTakeInventory;
    }

    public String getDateTakeInventory() {
        return dateTakeInventory;
    }

    public void setDateTakeInventory(String dateTakeInventory) {
        this.dateTakeInventory = dateTakeInventory;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getOficina() {
        return Oficina;
    }

    public void setOficina(String oficina) {
        Oficina = oficina;
    }
    public String getTiposDeInventario() {
        return tiposDeInventario;
    }

    public void setTiposDeInventario(String tiposDeInventario) {
        this.tiposDeInventario = tiposDeInventario;
    }
}
