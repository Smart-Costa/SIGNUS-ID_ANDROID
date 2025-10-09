package com.example.diverscan.activeid.FotoActivo;

public class EFotoActivo {

    private String rutaFoto;
    private String observacionFoto;
    private String assetSysId;
    private String nombreArchivo;
    private String idFoto;

    public EFotoActivo(String rutaFoto, String observacionFoto, String assetSysId, String nombreArchivo, String idFoto) {
        this.rutaFoto = rutaFoto;
        this.observacionFoto = observacionFoto;
        this.assetSysId = assetSysId;
        this.nombreArchivo = nombreArchivo;
        this.idFoto = idFoto;
    }

    public EFotoActivo() {
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    public String getObservacionFoto() {
        return observacionFoto;
    }

    public void setObservacionFoto(String observacionFoto) {
        this.observacionFoto = observacionFoto;
    }

    public String getAssetSysId() {
        return assetSysId;
    }

    public void setAssetSysId(String assetSysId) {
        this.assetSysId = assetSysId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getIdFoto() {
        return idFoto;
    }

    public void setIdFoto(String idFoto) {
        this.idFoto = idFoto;
    }
}
