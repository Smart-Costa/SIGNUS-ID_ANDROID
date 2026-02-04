package com.example.diverscan.activeid.DeviceInterface.Impl;

import android.content.Context;
import android.util.Log;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

import java.util.Collections;

public class DatalogicReaderImpl implements IReaderDevice {
    private static final String TAG = "DatalogicReaderImpl";
    private Context context;
    private IReaderListener listener;
    private boolean isConnected = false;
    private ConnectionType connectionType = ConnectionType.AUTO;

    private BarcodeManager barcodeManager;
    private ReadListener readListener;

    @Override
    public void initialize(Context context) {
        this.context = context;
        Log.d(TAG, "Initializing Datalogic Reader...");
    }

    @Override
    public boolean connect() {
        Log.d(TAG, "Connecting to Datalogic Reader...");
        try {
            if (barcodeManager == null) {
                barcodeManager = new BarcodeManager();
            }

            if (readListener == null) {
                readListener = new ReadListener() {
                    @Override
                    public void onRead(DecodeResult decodeResult) {
                        handleDecodeResult(decodeResult);
                    }
                };
            }

            barcodeManager.addReadListener(readListener);
            isConnected = true;
            if (listener != null) {
                listener.onConnected("Datalogic Scanner");
            }
            return true;

        } catch (DecodeException e) {
            Log.e(TAG, "Error initializing BarcodeManager", e);
            if (listener != null) {
                listener.onStatusMessage("Error Datalogic: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Generic Error connecting Datalogic", e);
            // Likely not a Datalogic device
            if (listener != null) {
                listener.onStatusMessage("Error: No es un dispositivo Datalogic compatible");
            }
            return false;
        }
    }

    private void handleDecodeResult(DecodeResult result) {
        if (result == null || result.getText() == null) {
            Log.w(TAG, "handleDecodeResult: Result is null or empty");
            return;
        }

        String barcode = result.getText();
        Log.d(TAG, "Datalogic Scanned: " + barcode + " | Type: " + result.getBarcodeID());

        // RSSI is not applicable for Barcode, using 100 as dummy
        ReaderTag tag = new ReaderTag(barcode, (short) 100);
        
        if (listener != null) {
            listener.onTagRead(Collections.singletonList(tag));
        }
    }

    @Override
    public boolean disconnect() {
        Log.d(TAG, "Disconnecting Datalogic Reader...");
        try {
            if (barcodeManager != null) {
                if (readListener != null) {
                    barcodeManager.removeReadListener(readListener);
                }
                // barcodeManager = null; // Optional: Keep instance or nullify
            }
            isConnected = false;
            if (listener != null) {
                listener.onDisconnected();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting", e);
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean startInventory() {
        Log.d(TAG, "startInventory called (Datalogic)");
        // Datalogic usually relies on hardware trigger, but we can enable software trigger if needed
        // For now, we just log.
        return true;
    }

    @Override
    public boolean stopInventory() {
        Log.d(TAG, "stopInventory called (Datalogic)");
        return true;
    }

    @Override
    public boolean startLocation(String epc) {
        Log.w(TAG, "Location not implemented for Datalogic");
        return false;
    }

    @Override
    public boolean stopLocation() {
        return false;
    }

    @Override
    public void setPower(int power) {
        Log.d(TAG, "setPower not applicable for Datalogic Scanner");
    }

    @Override
    public void setListener(IReaderListener listener) {
        this.listener = listener;
    }

    @Override
    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
    }

    @Override
    public void dispose() {
        disconnect();
        barcodeManager = null;
        readListener = null;
    }

    @Override
    public String getDeviceName() {
        return "Datalogic Scanner";
    }
    
    // Optional methods from IReaderDevice if any others are missing...
    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
        // Not applicable for Barcode
        return false;
    }
}
