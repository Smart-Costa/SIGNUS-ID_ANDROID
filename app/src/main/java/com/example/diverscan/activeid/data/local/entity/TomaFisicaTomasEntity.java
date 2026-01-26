package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class TomaFisicaTomasEntity {
    @SerializedName(value = "TomaFisicaId", alternate = {"tomaFisicaId"})
    private String tomaFisicaId;

    @SerializedName(value = "NumeroToma", alternate = {"numeroToma"})
    private String numeroToma;

    @SerializedName(value = "IdToma", alternate = {"idToma"})
    private String idToma;

    @SerializedName(value = "TotalLecturas", alternate = {"totalLecturas"})
    private String totalLecturas;

    @SerializedName(value = "FechaCreacion", alternate = {"fechaCreacion"})
    private String fechaCreacion;

    @SerializedName(value = "ActivosLeidos", alternate = {"activosLeidos"})
    private String activosLeidos;

    @SerializedName(value = "Sobrantes", alternate = {"sobrantes"})
    private String sobrantes;

    @SerializedName(value = "Faltantes", alternate = {"faltantes"})
    private String faltantes;

    @SerializedName(value = "TotalActivos", alternate = {"totalActivos"})
    private String totalActivos;

    @SerializedName(value = "Estado", alternate = {"estado", "Status", "status", "State", "state"})
    private String estado;

    public String getTomaFisicaId() { return tomaFisicaId; }
    public void setTomaFisicaId(String tomaFisicaId) { this.tomaFisicaId = tomaFisicaId; }

    public String getNumeroToma() { return numeroToma; }
    public void setNumeroToma(String numeroToma) { this.numeroToma = numeroToma; }

    public String getIdToma() { return idToma; }
    public void setIdToma(String idToma) { this.idToma = idToma; }

    public String getTotalLecturas() { return totalLecturas; }
    public void setTotalLecturas(String totalLecturas) { this.totalLecturas = totalLecturas; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getActivosLeidos() { return activosLeidos; }
    public void setActivosLeidos(String activosLeidos) { this.activosLeidos = activosLeidos; }

    public String getSobrantes() { return sobrantes; }
    public void setSobrantes(String sobrantes) { this.sobrantes = sobrantes; }

    public String getFaltantes() { return faltantes; }
    public void setFaltantes(String faltantes) { this.faltantes = faltantes; }

    public String getTotalActivos() { return totalActivos; }
    public void setTotalActivos(String totalActivos) { this.totalActivos = totalActivos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
