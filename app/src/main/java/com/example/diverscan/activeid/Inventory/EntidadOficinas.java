package com.example.diverscan.activeid.Inventory;

public class EntidadOficinas {

    String IdOficina;
    String NombreOficina;
    String IdPiso;
    String IdTag;
    boolean Nuevo;
    boolean Sinc;


    public EntidadOficinas (String idOficina, String nombreOficina, String idPiso, String idTag, boolean nuevo, boolean sinc){

        this.IdOficina = idOficina;
        this.NombreOficina = nombreOficina;
        this.IdPiso = idPiso;
        this.IdTag = idTag;
        this.Nuevo = nuevo;
        this.Sinc = sinc;
    }



    public String getIdOficina (){
        return IdOficina;
    }

    public String getNombreOficina (){
        return NombreOficina;
    }

    public String getIdPiso (){
        return IdPiso;
    }

    public String getIdTag (){
        return IdTag;
    }

    public boolean getNuevo (){
        return Nuevo;
    }

    public boolean getSinc (){
        return Sinc;
    }


    public void setIdOficina(String  idoficina){
        IdOficina = idoficina;
    }

    public void setNombreOficina (String  nombreoficina){
        NombreOficina = nombreoficina;
    }

    public void setIdPiso (String  idpiso){
        IdPiso = idpiso;
    }

    public void setIdTag (String  idtag){
        IdTag = idtag;
    }

    public void setNuevo (boolean nuev){
        Nuevo = nuev;
    }

    public void setSinc (boolean  sin){
        Sinc = sin;
    }

    @Override
    public String toString(){
        return this.NombreOficina;
    }
}
