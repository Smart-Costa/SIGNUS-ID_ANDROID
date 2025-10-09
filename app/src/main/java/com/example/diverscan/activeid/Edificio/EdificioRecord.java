package com.example.diverscan.activeid.Edificio;

public class EdificioRecord {

    private String _IdEdificio;
    private String _NombreEdificio;
    private String _IdRazonSocial;

    public EdificioRecord (String idedificio, String nombreedificio, String idRazon){

        _IdEdificio = idedificio;
        _NombreEdificio = nombreedificio;
        _IdRazonSocial = idRazon;
    }

    public String getIdEdificio(){
        return _IdEdificio;
    }

    public String getNombreEdificio (){
        return _NombreEdificio;
    }

    public String getIdRazonSocial (){
        return _IdRazonSocial;
    }

    public void setIdEdificio(String idEdificio){
        _IdEdificio = idEdificio;
    }

    public void setNombreEdificio (String nombreEdificio){
        _NombreEdificio = nombreEdificio;
    }

    public void setIdRazonSocial (String idRazonSocial){
        _IdRazonSocial  = idRazonSocial;
    }

    @Override
    public String toString(){
        return _NombreEdificio;
    }
}
