package com.example.diverscan.activeid.Activo;

public class EntidadCategoriaActivos {
    private String assetCategorySysId;
    private String name;
    private String description;

    public EntidadCategoriaActivos(String AssetCategorySysId, String Name, String Description){

        assetCategorySysId = AssetCategorySysId;
        name = Name;
        description = Description;

    }

    public String getAssetCategorySysId() {
        return assetCategorySysId;
    }

    public void setAssetCategorySysId(String assetCategorySysId) {
        this.assetCategorySysId = assetCategorySysId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
