package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class UbicacionEntity {
    @SerializedName("aSysId")
    public String ASysId;

    @SerializedName("ubicacionA")
    public String UbicacionA;

    @SerializedName("bSysId")
    public String BSysId;

    @SerializedName("ubicacionB")
    public String UbicacionB;

    @SerializedName("cSysId")
    public String CSysId;

    @SerializedName("ubicacionC")
    public String UbicacionC;

    @SerializedName("dSysId")
    public String DSysId;

    @SerializedName("ubicacionD")
    public String UbicacionD;

    public String getASysId() { return ASysId; }
    public void setASysId(String aSysId) { ASysId = aSysId; }

    public String getUbicacionA() { return UbicacionA; }
    public void setUbicacionA(String ubicacionA) { UbicacionA = ubicacionA; }

    public String getBSysId() { return BSysId; }
    public void setBSysId(String bSysId) { BSysId = bSysId; }

    public String getUbicacionB() { return UbicacionB; }
    public void setUbicacionB(String ubicacionB) { UbicacionB = ubicacionB; }

    public String getCSysId() { return CSysId; }
    public void setCSysId(String cSysId) { CSysId = cSysId; }

    public String getUbicacionC() { return UbicacionC; }
    public void setUbicacionC(String ubicacionC) { UbicacionC = ubicacionC; }

    public String getDSysId() { return DSysId; }
    public void setDSysId(String dSysId) { DSysId = dSysId; }

    public String getUbicacionD() { return UbicacionD; }
    public void setUbicacionD(String ubicacionD) { UbicacionD = ubicacionD; }
}