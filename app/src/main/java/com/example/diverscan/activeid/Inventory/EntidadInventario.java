package com.example.diverscan.activeid.Inventory;

public class EntidadInventario {
    private String Id;
    private String IdTomasdeInventario;
    private String Numero;
    private String Leidos;
    private String Total;
    private String Encontrados;
    private String Faltantes;
    private String Sobrantes;
    private String Fecha;

    public EntidadInventario(String id, String idTomasdeInventario, String numero, String leidos, String total, String encontrados, String faltantes, String sobrantes, String fecha) {
        this.Id = id;
        this.IdTomasdeInventario = idTomasdeInventario;
        this.Numero = numero;
        this.Leidos = leidos;
        this.Total = total;
        this.Encontrados = encontrados;
        this.Faltantes = faltantes;
        this.Sobrantes = sobrantes;
        this.Fecha = fecha;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getIdTomasdeInventario() {
        return IdTomasdeInventario;
    }

    public void setIdTomasdeInventario(String idTomasdeInventario) {
        IdTomasdeInventario = idTomasdeInventario;
    }

    public String getNumero() {
        return Numero;
    }

    public void setNumero(String numero) {
        Numero = numero;
    }

    public String getLeidos() {
        return Leidos;
    }

    public void setLeidos(String leidos) {
        Leidos = leidos;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getEncontrados() {
        return Encontrados;
    }

    public void setEncontrados(String encontrados) {
        Encontrados = encontrados;
    }

    public String getFaltantes() {
        return Faltantes;
    }

    public void setFaltantes(String faltantes) {
        Faltantes = faltantes;
    }

    public String getSobrantes() {
        return Sobrantes;
    }

    public void setSobrantes(String sobrantes) {
        Sobrantes = sobrantes;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }
}
