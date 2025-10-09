package com.example.diverscan.activeid.Assign_tag_Offices;

public class sincronizarTag {

    String tagId;
    String officeSysId;
    String oficinaNombre;

    public void sincronizarTag(String TagId, String OfficeSysId, String OficinaNombre){
        this.tagId = TagId;
        this.officeSysId = OfficeSysId;
        this.oficinaNombre = OficinaNombre;
    }

    public sincronizarTag(String TagId, String OfficeSysId, String OficinaNombre){
        this.tagId = TagId;
        this.officeSysId = OfficeSysId;
        this.oficinaNombre = OficinaNombre;
    }

    public String getOfficeSysId() {
        return officeSysId;
    }

    public String getTagId() {
        return tagId;
    }

    public String getOficinaNombre() {
        return oficinaNombre;
    }

    public void setOfficeSysId(String OfficeSysId) {
        this.officeSysId = OfficeSysId;
    }

    public void setOficinaNombre(String OficinaNombre) {
        this.oficinaNombre = OficinaNombre;
    }

    public void setTagId(String TagId) {
        this.tagId = TagId;
    }
}
