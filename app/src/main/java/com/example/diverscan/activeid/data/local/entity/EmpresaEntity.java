package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class EmpresaEntity {
    @SerializedName("eSysId")
    private String ESysId;

    @SerializedName("empresa")
    private String Empresa;

    public String getESysId() { return ESysId; }
    public void setESysId(String eSysId) { ESysId = eSysId; }

    public String getEmpresa() { return Empresa; }
    public void setEmpresa(String empresa) { Empresa = empresa; }
}
