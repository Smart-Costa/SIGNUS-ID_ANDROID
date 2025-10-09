package com.example.diverscan.activeid.Oficina;

public class ActualizarUbicacionActivo {
    String _IdOficina;
    String _IdPiso;
    String _IdEdificio;
    String _IdCompania;
    String Oficina;
    String Piso;
    String Edificio;
    String Compania;

    public ActualizarUbicacionActivo(String compania,String idCompania,String edificio,String idEdificio, String piso,String idPiso,
                        String oficina,String idOficina){


        this.Oficina = oficina;
        this.Piso = piso;
        this.Edificio = edificio;
        this.Compania = compania;
        _IdOficina = idOficina;
        _IdPiso = idPiso;
        _IdEdificio = idEdificio;
        _IdCompania = idCompania;

    }

    public String get_IdOficina() {
        return _IdOficina;
    }

    public void set_IdOficina(String _IdOficina) {
        this._IdOficina = _IdOficina;
    }

    public String get_IdPiso() {
        return _IdPiso;
    }

    public void set_IdPiso(String _IdPiso) {
        this._IdPiso = _IdPiso;
    }

    public String get_IdEdificio() {
        return _IdEdificio;
    }

    public void set_IdEdificio(String _IdEdificio) {
        this._IdEdificio = _IdEdificio;
    }

    public String get_IdCompania() {
        return _IdCompania;
    }

    public void set_IdCompania(String _IdCompania) {
        this._IdCompania = _IdCompania;
    }

    public String getOficina() {
        return Oficina;
    }

    public void setOficina(String oficina) {
        Oficina = oficina;
    }

    public String getPiso() {
        return Piso;
    }

    public void setPiso(String piso) {
        Piso = piso;
    }

    public String getEdificio() {
        return Edificio;
    }

    public void setEdificio(String edificio) {
        Edificio = edificio;
    }

    public String getCompania() {
        return Compania;
    }

    public void setCompania(String compania) {
        Compania = compania;
    }
}
