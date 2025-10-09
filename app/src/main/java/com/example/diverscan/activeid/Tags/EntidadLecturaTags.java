package com.example.diverscan.activeid.Tags;

import com.example.diverscan.activeid.Inventory.EntidadUsuarios;

public class EntidadLecturaTags {
    private String tagID;

    public EntidadLecturaTags(){

    }

    public EntidadLecturaTags(String tagId){

        this.tagID = tagId;

    }

    public String getTagID(){
        return tagID;
    }

    public void setTagID(String tagId){
        tagID = tagId;
    }


}
