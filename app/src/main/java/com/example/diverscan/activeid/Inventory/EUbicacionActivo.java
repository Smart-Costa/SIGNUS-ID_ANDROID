package com.example.diverscan.activeid.Inventory;

public class EUbicacionActivo {
    private String IdOficina;
    private String NombreOficina;
    private String IdPiso;
    private String NombrePiso;
    private String IdEdificio;
    private String NombreEdificio;
    private String IdRazonSocial;
    private String NombreRazonSocial;

    public EUbicacionActivo() {
    }

    public EUbicacionActivo(String idOficina, String nombreOficina, String idPiso,
                            String nombrePiso, String idEdificio, String nombreEdificio,
                            String idRazonSocial, String nombreRazonSocial) {
        IdOficina = idOficina;
        NombreOficina = nombreOficina;
        IdPiso = idPiso;
        NombrePiso = nombrePiso;
        IdEdificio = idEdificio;
        NombreEdificio = nombreEdificio;
        IdRazonSocial = idRazonSocial;
        NombreRazonSocial = nombreRazonSocial;
    }

    public String getIdOficina() {
        return IdOficina;
    }

    public void setIdOficina(String idOficina) {
        IdOficina = idOficina;
    }

    public String getNombreOficina() {
        return NombreOficina;
    }

    public void setNombreOficina(String nombreOficina) {
        NombreOficina = nombreOficina;
    }

    public String getIdPiso() {
        return IdPiso;
    }

    public void setIdPiso(String idPiso) {
        IdPiso = idPiso;
    }

    public String getNombrePiso() {
        return NombrePiso;
    }

    public void setNombrePiso(String nombrePiso) {
        NombrePiso = nombrePiso;
    }

    public String getIdEdificio() {
        return IdEdificio;
    }

    public void setIdEdificio(String idEdificio) {
        IdEdificio = idEdificio;
    }

    public String getNombreEdificio() {
        return NombreEdificio;
    }

    public void setNombreEdificio(String nombreEdificio) {
        NombreEdificio = nombreEdificio;
    }

    public String getIdRazonSocial() {
        return IdRazonSocial;
    }

    public void setIdRazonSocial(String idRazonSocial) {
        IdRazonSocial = idRazonSocial;
    }

    public String getNombreRazonSocial() {
        return NombreRazonSocial;
    }

    public void setNombreRazonSocial(String nombreRazonSocial) {
        NombreRazonSocial = nombreRazonSocial;
    }
}
