package com.example.diverscan.activeid.data.local.entity;


import com.google.gson.annotations.SerializedName;

public class UbicacionSecundariaEntity {
    @SerializedName("usSysId")
    private String USSysId;

    @SerializedName("ubicacionS")
    private String UbicacionS;

    public String getUSSysId() { return USSysId; }
    public void setUSSysId(String uSSysId) { USSysId = uSSysId; }

    public String getUbicacionS() { return UbicacionS; }
    public void setUbicacionS(String ubicacionS) { UbicacionS = ubicacionS; }
}
