package com.example.diverscan.activeid.data.local.entity;

import com.google.gson.annotations.SerializedName;

public class TomaFisicaDetallesEntity {
    @SerializedName(value = "IdTakeDetail", alternate = {"idTakeDetail"})
    private String idTakeDetail;

    @SerializedName(value = "IdToma", alternate = {"idToma"})
    private String idToma;

    @SerializedName(value = "NumeroToma", alternate = {"numeroToma"})
    private String numeroToma;

    @SerializedName(value = "FechaToma", alternate = {"fechaToma"})
    private String fechaToma;

    @SerializedName(value = "EPC", alternate = {"epc"})
    private String epc;

    @SerializedName(value = "DateRead", alternate = {"dateRead"})
    private String dateRead;

    @SerializedName(value = "ActivoId", alternate = {"activoId"})
    private String activoId;

    @SerializedName(value = "EstadoInventario", alternate = {"estadoInventario"})
    private String estadoInventario;

    @SerializedName(value = "UbicacionDetalleA", alternate = {"ubicacionDetalleA"})
    private String ubicacionDetalleA;

    @SerializedName(value = "UbicacionDetalleB", alternate = {"ubicacionDetalleB"})
    private String ubicacionDetalleB;

    @SerializedName(value = "UbicacionDetalleC", alternate = {"ubicacionDetalleC"})
    private String ubicacionDetalleC;

    @SerializedName(value = "UbicacionDetalleD", alternate = {"ubicacionDetalleD"})
    private String ubicacionDetalleD;

    @SerializedName(value = "Observaciones", alternate = {"observaciones"})
    private String observaciones;

    public String getIdTakeDetail() { return idTakeDetail; }
    public void setIdTakeDetail(String idTakeDetail) { this.idTakeDetail = idTakeDetail; }

    public String getIdToma() { return idToma; }
    public void setIdToma(String idToma) { this.idToma = idToma; }

    public String getNumeroToma() { return numeroToma; }
    public void setNumeroToma(String numeroToma) { this.numeroToma = numeroToma; }

    public String getFechaToma() { return fechaToma; }
    public void setFechaToma(String fechaToma) { this.fechaToma = fechaToma; }

    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }

    public String getDateRead() { return dateRead; }
    public void setDateRead(String dateRead) { this.dateRead = dateRead; }

    public String getActivoId() { return activoId; }
    public void setActivoId(String activoId) { this.activoId = activoId; }

    public String getEstadoInventario() { return estadoInventario; }
    public void setEstadoInventario(String estadoInventario) { this.estadoInventario = estadoInventario; }

    public String getUbicacionDetalleA() { return ubicacionDetalleA; }
    public void setUbicacionDetalleA(String ubicacionDetalleA) { this.ubicacionDetalleA = ubicacionDetalleA; }

    public String getUbicacionDetalleB() { return ubicacionDetalleB; }
    public void setUbicacionDetalleB(String ubicacionDetalleB) { this.ubicacionDetalleB = ubicacionDetalleB; }

    public String getUbicacionDetalleC() { return ubicacionDetalleC; }
    public void setUbicacionDetalleC(String ubicacionDetalleC) { this.ubicacionDetalleC = ubicacionDetalleC; }

    public String getUbicacionDetalleD() { return ubicacionDetalleD; }
    public void setUbicacionDetalleD(String ubicacionDetalleD) { this.ubicacionDetalleD = ubicacionDetalleD; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
