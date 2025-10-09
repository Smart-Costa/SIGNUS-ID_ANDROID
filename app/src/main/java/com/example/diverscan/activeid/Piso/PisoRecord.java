package com.example.diverscan.activeid.Piso;

public class PisoRecord {

    private String _IdPiso;
    private String _NombrePiso;
    private String _IdEdificio;

    public PisoRecord (String idpiso, String nombrepiso, String idEdificio){

        _IdPiso = idpiso;
        _NombrePiso = nombrepiso;
        _IdEdificio = idEdificio;
    }

    public String getIdPiso(){
        return _IdPiso;
    }

    public String getNombrePiso (){
        return _NombrePiso;
    }

    public String getIdEdificio (){
        return _IdEdificio;
    }

    public void setIdPiso(String idPiso){
        _IdPiso = idPiso;
    }

    public void setNombrePiso (String nombrePiso){
        _NombrePiso = nombrePiso;
    }

    public void setIdEdificio (String IdEdificio){
        _IdEdificio  = IdEdificio;
    }

    @Override
    public String toString(){
        return _NombrePiso;
    }
}
