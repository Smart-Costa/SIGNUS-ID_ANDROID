package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class UbicacionEntity {
    @SerializedName(value = "aSysId", alternate = {"ASysId"})
    public String ASysId;

    @SerializedName(value = "ubicacionA", alternate = {"UbicacionA"})
    public String UbicacionA;

    @SerializedName(value = "bSysId", alternate = {"BSysId"})
    public String BSysId;

    @SerializedName(value = "ubicacionB", alternate = {"UbicacionB"})
    public String UbicacionB;

    @SerializedName(value = "cSysId", alternate = {"CSysId"})
    public String CSysId;

    @SerializedName(value = "ubicacionC", alternate = {"UbicacionC"})
    public String UbicacionC;

    @SerializedName(value = "dSysId", alternate = {"DSysId"})
    public String DSysId;

    @SerializedName(value = "ubicacionD", alternate = {"UbicacionD"})
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