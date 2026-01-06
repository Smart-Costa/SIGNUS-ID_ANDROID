package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class CategoriaEntity {
    @SerializedName("cSysId")
    private String CSysId;

    @SerializedName("categoria")
    private String Categoria;

    public String getCSysId() { return CSysId; }
    public void setCSysId(String cSysId) { CSysId = cSysId; }

    public String getCategoria() { return Categoria; }
    public void setCategoria(String categoria) { this.Categoria = categoria; }
}
