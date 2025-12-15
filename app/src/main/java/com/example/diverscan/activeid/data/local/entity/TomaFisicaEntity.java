package com.example.diverscan.activeid.data.local.entity;

import androidx.annotation.NonNull;

public class TomaFisicaEntity {
    private String tomaFisicaId;
    private String nombre;
    private String fechaInicial;
    private String fechaFinal;
    private String categoria;
    private String usuarioAsignado;
    private String unidadOrganizativa;
    private String estadoActivo;
    private String ubicacionA;
    private String ubicacionB;
    private String ubicacionC;
    private String ubicacionD;

    // --- Getters y Setters ---

    @NonNull
    public String getTomaFisicaId() {
        return tomaFisicaId;
    }

    public void setTomaFisicaId(@NonNull String tomaFisicaId) {
        this.tomaFisicaId = tomaFisicaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(String fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public String getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(String fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(String usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }

    public String getUnidadOrganizativa() {
        return unidadOrganizativa;
    }

    public void setUnidadOrganizativa(String unidadOrganizativa) {
        this.unidadOrganizativa = unidadOrganizativa;
    }

    public String getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(String estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    public String getUbicacionA() {
        return ubicacionA;
    }

    public void setUbicacionA(String ubicacionA) {
        this.ubicacionA = ubicacionA;
    }

    public String getUbicacionB() {
        return ubicacionB;
    }

    public void setUbicacionB(String ubicacionB) {
        this.ubicacionB = ubicacionB;
    }

    public String getUbicacionC() {
        return ubicacionC;
    }

    public void setUbicacionC(String ubicacionC) {
        this.ubicacionC = ubicacionC;
    }

    public String getUbicacionD() {
        return ubicacionD;
    }

    public void setUbicacionD(String ubicacionD) {
        this.ubicacionD = ubicacionD;
    }
}
