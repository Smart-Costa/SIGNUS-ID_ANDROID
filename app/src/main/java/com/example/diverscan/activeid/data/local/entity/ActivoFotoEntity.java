package com.example.diverscan.activeid.data.local.entity;

public class ActivoFotoEntity {
    private int id;
    private String idActivo;
    private int numeroFoto;
    private String rutaLocal;  // puede ser ruta del archivo o base64
    private String urlRemota;  // si se sube al backend

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdActivo() {
        return idActivo;
    }

    public void setIdActivo(String idActivo) {
        this.idActivo = idActivo;
    }

    public int getNumeroFoto() { return numeroFoto; }
    public void setNumeroFoto(int numeroFoto) { this.numeroFoto = numeroFoto; }

    public String getRutaLocal() { return rutaLocal; }
    public void setRutaLocal(String rutaLocal) { this.rutaLocal = rutaLocal; }

    public String getUrlRemota() { return urlRemota; }
    public void setUrlRemota(String urlRemota) { this.urlRemota = urlRemota; }
}