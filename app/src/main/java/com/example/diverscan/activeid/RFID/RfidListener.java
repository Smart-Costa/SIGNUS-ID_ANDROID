package com.example.diverscan.activeid.RFID;

public interface RfidListener {
    void onConnected();

    void onTagRead(String epc);

    void onError(String message);

    void onReaderDisconnected();
}
