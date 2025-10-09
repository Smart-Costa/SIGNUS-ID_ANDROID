package com.example.diverscan.activeid.FotoActivo;

public enum EnumFotos {
    PATRIMONIO_ANTERIOR("Patrimonio Actual",1), PATRIMONIO_NUEVO("Patrimonio Nuevo",2),
    FOTO_GENERAL("Foto General",3), FOTO_TECNICA("Foto Técina",4);

    private String nombreFoto;
    private int numeroFoto;

    private EnumFotos(String nombreFoto, int numeroFoto){
        this.nombreFoto = nombreFoto;
        this.numeroFoto = numeroFoto;
    }

    public String getNombreFoto() {
        return nombreFoto;
    }

    public void setNombreFoto(String nombreFoto) {
        this.nombreFoto = nombreFoto;
    }

    public int getNumeroFoto() {
        return numeroFoto;
    }

    public void setNumeroFoto(int numeroFoto) {
        this.numeroFoto = numeroFoto;
    }
}
