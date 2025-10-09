package com.example.diverscan.activeid.Tags;

public class EntidadTiposTags {


    private String tagTypeSysId;
    private String code;
    private String name;
    private String description;
    private String category;

    public EntidadTiposTags(String TagTypeSysId, String Code, String Name, String Description, String Category){

        tagTypeSysId = TagTypeSysId;
        code = Code;
        name = Name;
        description = Description;
        category = Category;

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTagTypeSysId() {
        return tagTypeSysId;
    }

    public void setTagTypeSysId(String tagTypeSysId) {
        this.tagTypeSysId = tagTypeSysId;
    }

}
