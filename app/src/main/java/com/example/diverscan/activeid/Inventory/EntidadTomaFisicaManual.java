package com.example.diverscan.activeid.Inventory;

public class EntidadTomaFisicaManual {
    String epc;

    public EntidadTomaFisicaManual( String EPC){

        this.epc = EPC;
    }

    public String getepc() {
        return epc;
    }

    public void setepc(String EPC) {
        epc = EPC;
    }
}
