package com.example.diverscan.activeid.Scanner;

import android.os.Handler;
import android.os.Looper;
import java.util.Random;
import java.util.UUID;

public class MockScannerImpl implements ScannerService {

    private ScannerListener listener;
    private boolean isConnected = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    @Override
    public void connect() {
        isConnected = true;
        if (listener != null) {
            listener.onStatusMessage("Mock Scanner Connected");
        }
    }

    @Override
    public void disconnect() {
        isConnected = false;
        if (listener != null) {
            listener.onStatusMessage("Mock Scanner Disconnected");
        }
    }

    @Override
    public void setListener(ScannerListener listener) {
        this.listener = listener;
    }

    @Override
    public void triggerScan() {
        if (!isConnected) {
            if (listener != null) {
                listener.onStatusMessage("Error: Scanner not connected");
            }
            return;
        }

        // Simulate a delay like a real scanner
        handler.postDelayed(() -> {
            if (listener != null) {
                boolean isRfid = random.nextBoolean();
                String type = isRfid ? "RFID" : "BARCODE";
                String data;
                
                if (isRfid) {
                    // Generate a fake EPC (Electronic Product Code)
                    data = "E200" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
                } else {
                    // Generate a fake Barcode (EAN-13 style)
                    data = String.format("%013d", Math.abs(random.nextLong() % 10000000000000L));
                }
                
                listener.onScanResult(data, type);
                listener.onStatusMessage("Scanned: " + data + " (" + type + ")");
            }
        }, 500); // 500ms delay
    }
}
