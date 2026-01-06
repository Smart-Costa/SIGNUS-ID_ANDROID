package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class MarcaEntity {
    @SerializedName("mSysId")
    private String MSysId;

    @SerializedName("marca")
    private String Marca;

    public String getMSysId() { return MSysId; }
    public void setMSysId(String mSysId) { MSysId = mSysId; }

    public String getMarca() { return Marca; }
    public void setMarca(String marca) { Marca = marca; }
}
