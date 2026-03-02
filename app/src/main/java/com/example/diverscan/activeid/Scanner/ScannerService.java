package com.example.diverscan.activeid.Scanner;

public interface ScannerService {
    void connect();
    void disconnect();
    void setListener(ScannerListener listener);
    void triggerScan(); // Manually trigger a scan (useful for testing or soft-trigger)
    
    interface ScannerListener {
        void onScanResult(String data, String type); // type: "BARCODE" or "RFID"
        void onStatusMessage(String message);
    }
}
