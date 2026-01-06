package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class ModeloEntity {
    @SerializedName("mSysId")
    private String MSysId;

    @SerializedName("modelo")
    private String Modelo;

    public String getMSysId() { return MSysId; }
    public void setMSysId(String mSysId) { MSysId = mSysId; }

    public String getModelo() { return Modelo; }
    public void setModelo(String modelo) { Modelo = modelo; }
}