package com.example.diverscan.activeid.AssetStatus;

import java.util.jar.Attributes;

public class EntidadAssetStatus {

    private String id;
    private String name;
    private String description;

    public EntidadAssetStatus( String pid, String pname, String pdescription){

        id = pid;
        name= pname;
        description = pdescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String pid) {
        this.id = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String pname) {
        this.name = pname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pdescription) {
        this.description = pdescription;
    }

    @Override
    public String toString(){
        return name;
    }
}
