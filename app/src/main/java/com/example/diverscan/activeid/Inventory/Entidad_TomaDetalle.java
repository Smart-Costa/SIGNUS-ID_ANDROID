package com.example.diverscan.activeid.Inventory;

import cz.msebera.android.httpclient.entity.StringEntity;

public class Entidad_TomaDetalle {
    String IdTakeDetail;
    String Fk_TomaFisica;
    String epc;
    String dateRead;

    public Entidad_TomaDetalle(String idTakeDetail, String fk_TomaFisica, String EPC, String DateRead){
        this.IdTakeDetail = idTakeDetail;
        this.Fk_TomaFisica = fk_TomaFisica;
        this.epc = EPC;
        this.dateRead = DateRead;
    }

    public String getIdTakeDetail() {
        return IdTakeDetail;
    }

    public String getFk_TomaFisica() {
        return Fk_TomaFisica;
    }

    public String getepc() {
        return epc;
    }

    public String getDateRead() {
        return dateRead;
    }

    public void setIdTakeDetail(String idTakeDetail) {
        IdTakeDetail = idTakeDetail;
    }

    public void setFk_TomaFisica(String fk_TomaFisica) {
        Fk_TomaFisica = fk_TomaFisica;
    }

    public void setepc(String epc) {
        this.epc = epc;
    }

    public void setDateRead(String dateRead) {
        this.dateRead = dateRead;
    }
}
