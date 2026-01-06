package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class EstadoEntity {
    @SerializedName("eSysId")
    private String ESysId;

    @SerializedName("estado")
    private String Estado;

    public String getESysId() { return ESysId; }
    public void setESysId(String eSysId) { ESysId = eSysId; }

    public String getEstado() { return Estado; }
    public void setEstado(String estado) { Estado = estado; }
}

