package com.example.diverscan.activeid.RFID;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderFactory;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;

import java.util.List;

public class RfidManager implements IReaderListener {

    private static final String TAG = "RfidManager";
    private final Context context;
    private final RfidListener listener;
    private IReaderDevice device;

    public RfidManager(Context context, RfidListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void connect() {
        // Use Factory to create reader (Decoupled from specific implementation)
        // Detect the best reader type dynamically
        ReaderType bestType = ReaderFactory.getBestReaderType(context);
        com.example.diverscan.activeid.DeviceInterface.ConnectionType bestConn = ReaderFactory.getBestConnectionType(context, bestType);
        
        device = ReaderFactory.createReader(bestType, bestConn, context, this);
        
        new Thread(() -> {
            if (device != null) {
                // ZebraReaderImpl handles connection internally (check/connect)
                // We just trigger it. It's asynchronous usually if it needs to scan,
                // but connect() in interface returns boolean.
                // In ZebraReaderImpl, connect() calls notifyConnected.
                if (!device.connect()) {
                   // If it returns false immediately (e.g. null reader), notify error
                   // But typically ZebraReaderImpl.connect() handles the complex part.
                   // If it fails to find reader, it might notifyError.
                }
            } else {
                if (listener != null) listener.onError("Error: No se pudo crear el lector");
            }
        }).start();
    }

    public void startReading() {
        if (device != null) device.startInventory();
    }

    public void stopReading() {
        if (device != null) device.stopInventory();
    }

    // IReaderListener Implementation

    @Override
    public void onConnected(String readerName) {
        if (listener != null) listener.onConnected();
    }

    @Override
    public void onDisconnected() {
        if (listener != null) listener.onReaderDisconnected();
    }

    @Override
    public void onConnectionError(String message) {
        if (listener != null) listener.onError(message);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (listener != null && tags != null) {
            for (ReaderTag tag : tags) {
                // Forward each tag to the legacy listener which expects single string
                listener.onTagRead(tag.getEpc());
            }
        }
    }

    @Override
    public void onTrigger(boolean pressed) {
        // RfidManager/LocalizarActivo doesn't seem to handle trigger events explicitly
        // but we could expose it if needed.
    }

    @Override
    public void onStatusMessage(String message) {
        // Optional logging or feedback
        Log.d(TAG, "Status: " + message);
    }
}
