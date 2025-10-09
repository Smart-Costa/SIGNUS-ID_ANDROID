package com.example.diverscan.activeid.Inventory;

public class EntidadOficina2 {

    String IdOficina;
    String NombreOficina;
    String IdPiso;
    String Piso;
    String IdTag;

    public EntidadOficina2 (String idOficina, String nombreOficina, String idPiso, String piso, String idTag){

        this.IdOficina = idOficina;
        this.NombreOficina = nombreOficina;
        this.IdPiso = idPiso;
        this.Piso = piso;
        this.IdTag = idTag;
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

    public String getPiso (){
        return Piso;
    }

    public String getIdTag (){
        return IdTag;
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

    public void setPiso (String  piso){
        Piso = piso;
    }

    public void setIdTag (String  idtag){
        IdTag = idtag;
    }

    @Override
    public String toString(){
        return this.NombreOficina;
    }

}
