package com.example.diverscan.activeid.Tags;

public class EntidadTags {
    private String tagSysId;
    private String tagID;
    private String tagTypeSysId;

    public EntidadTags( String TagSysId, String TagID, String TagTypeSysId){

        tagSysId = TagSysId;
        tagID = TagID;
        tagTypeSysId = TagTypeSysId;

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
