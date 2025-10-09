package com.example.diverscan.activeid.Roles;

import java.util.List;
import java.util.ArrayList;

public class EntidadRolHH  {
    private List<EntidadDatosRol> data = new ArrayList<EntidadDatosRol>();
    private String description;
    private String state;

    public EntidadRolHH(){
        this.data = new ArrayList<EntidadDatosRol>();
    }

    public EntidadRolHH(String Data, String State, String Description){

        state = State;
        description = Description;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String code) {
        this.description= code;
    }

    public String getState() {
        return state;
    }

    public void setState(String code) {
        this.state = code;
    }

    public List<EntidadDatosRol> getDataRol() {
        return data;
    }

    public void setDataRol(List<EntidadDatosRol> DataRol) {
        this.data = DataRol;
    }

}
