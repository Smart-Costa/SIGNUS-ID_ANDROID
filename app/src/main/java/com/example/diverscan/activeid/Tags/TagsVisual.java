package com.example.diverscan.activeid.Tags;

public class TagsVisual {
    private String tagSysId;
    private String tagID;
    private String tagTypeSysId;
    private String name;
    private String Estado;

    public TagsVisual() {
    }

    public TagsVisual(String TagSysId, String TagID, String TagTypeSysId, String Name, String estado){

        tagSysId = TagSysId;
        tagID = TagID;
        tagTypeSysId = TagTypeSysId;
        name = Name;
        Estado = estado;
    }
    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagSysId() {
        return tagSysId;
    }

    public void setTagSysId(String tagSysId) {
        this.tagSysId = tagSysId;
    }

    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getTagTypeSysId() {
        return tagTypeSysId;
    }

    public void setTagTypeSysId(String tagTypeSysId) {
        this.tagTypeSysId = tagTypeSysId;
    }
}
