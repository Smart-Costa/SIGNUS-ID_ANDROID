package com.example.diverscan.activeid.Tags;

public class EntidadClasificarTag {
   private String tagTypeSysId;
    private String name;


    public EntidadClasificarTag(String TagTypeSysId, String Name) {
        tagTypeSysId = TagTypeSysId;
        name = Name;
    }

    public String getTagTypeSysId() {
        return tagTypeSysId;
    }

    public void setTagTypeSysId(String tagTypeSysId) {
        this.tagTypeSysId = tagTypeSysId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
