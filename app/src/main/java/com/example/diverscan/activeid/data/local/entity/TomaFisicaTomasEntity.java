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

    public String getTomaFisicaId() { return tomaFisicaId; }
    public void setTomaFisicaId(String tomaFisicaId) { this.tomaFisicaId = tomaFisicaId; }

    public String getNumeroToma() { return numeroToma; }
    public void setNumeroToma(String numeroToma) { this.numeroToma = numeroToma; }

    public String getIdToma() { return idToma; }
    public void setIdToma(String idToma) { this.idToma = idToma; }

    public String getTotalLecturas() { return totalLecturas; }
    public void setTotalLecturas(String totalLecturas) { this.totalLecturas = totalLecturas; }
}
