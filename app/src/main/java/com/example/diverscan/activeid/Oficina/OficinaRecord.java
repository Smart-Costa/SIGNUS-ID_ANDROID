package com.example.diverscan.activeid.Oficina;

public class OficinaRecord {

    private String _IdOficina;
    private String _NombreOficina;
    private String _IdPiso;

    public OficinaRecord (String idoficina, String nombreoficina, String idPiso){

        _IdOficina = idoficina;
        _NombreOficina = nombreoficina;
        _IdPiso = idPiso;
    }

    public String getIdOficina(){
        return _IdOficina;
    }

    public String getNombreOficina (){
        return _NombreOficina;
    }

    public String getIdPiso (){
        return _IdPiso;
    }

    public void setIdOficina(String idoficina){
        _IdOficina = idoficina;
    }

    public void setNombreOficina (String nombrePiso){
        _NombreOficina = nombrePiso;
    }

    public void setIdPiso (String idPiso){
        _IdPiso  = idPiso;
    }

    @Override
    public String toString(){
        return _NombreOficina;
    }
}
