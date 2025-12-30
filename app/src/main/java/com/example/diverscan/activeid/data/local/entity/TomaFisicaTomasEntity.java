package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class TomaFisicaTomasEntity {
    @SerializedName("TomaFisicaId")
    private String tomaFisicaId;

    @SerializedName("NumeroToma")
    private String numeroToma;

    @SerializedName("IdToma")
    private String idToma;

    @SerializedName("TotalLecturas")
    private String totalLecturas;

    @SerializedName("FechaCreacion")
    private String fechaCreacion;

    @SerializedName("ActivosLeidos")
    private String activosLeidos;

    @SerializedName("Sobrantes")
    private String sobrantes;

    @SerializedName("Faltantes")
    private String faltantes;

    @SerializedName("TotalActivos")
    private String totalActivos;

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
}
