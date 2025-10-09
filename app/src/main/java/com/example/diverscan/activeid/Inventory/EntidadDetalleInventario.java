package com.example.diverscan.activeid.Inventory;

public class EntidadDetalleInventario {

    private String id;
    private String IdInventario;
    private String NumeroActivo;
    private String Descripcion;
    private String EPC;
    private String Estado;
    private String Excluido;


    public EntidadDetalleInventario(String Id, String idInventario, String numeroActivo, String descripcion, String epc, String estado, String excluido) {
        id = Id;
        IdInventario = idInventario;
        NumeroActivo = numeroActivo;
        Descripcion = descripcion;
        EPC = epc;
        Estado = estado;
        Excluido = excluido;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdInventario() {
        return IdInventario;
    }

    public void setIdInventario(String idInventario) {
        IdInventario = idInventario;
    }

    public String getNumeroActivo() {
        return NumeroActivo;
    }

    public void setNumeroActivo(String numeroActivo) {
        NumeroActivo = numeroActivo;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripción) {
        Descripcion = descripción;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }

    public String getExcluido() {
        return Excluido;
    }

    public void setExcluido(String excluido) {
        Excluido = excluido;
    }

}
