package com.example.diverscan.activeid.Scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.Impl.ZebraReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.DatalogicReaderImpl;

import java.util.List;

public class PhysicalScannerImpl implements ScannerService, IReaderListener {

    private static final String TAG = "PhysicalScannerImpl";
    private Context context;
    private ScannerListener listener;
    
    // Hardware abstractions
    private IReaderDevice rfidReader; // Primary RFID reader
    private IReaderDevice barcodeReader; // Secondary/Barcode reader (if implemented as IReaderDevice)
    
    private boolean isConnected = false;
    private String deviceManufacturer;

    // --- Zebra Specific Constants ---
    private static final String ACTION_RESULT_DATAWEDGE = "com.symbol.datawedge.api.RESULT_ACTION";
    private static final String EXTRA_DATA_STRING = "com.symbol.datawedge.data_string";
    private static final String EXTRA_LABEL_TYPE = "com.symbol.datawedge.label_type";

    // --- iMin Specific Constants ---
    private static final String IMIN_RESULT_ACTION = "com.imin.scanner.api.RESULT_ACTION";
    private static final String IMIN_EXTRA_DECODE_DATA = "decode_data";

    // BroadcastReceiver for Intent-based Barcode Scanners (Zebra & iMin)
    private final BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String data = null;
            String type = "BARCODE";

            if (ACTION_RESULT_DATAWEDGE.equals(action)) {
                // Zebra DataWedge
                if (intent.hasExtra(EXTRA_DATA_STRING)) {
                    data = intent.getStringExtra(EXTRA_DATA_STRING);
                    String labelType = intent.getStringExtra(EXTRA_LABEL_TYPE);
                    type = "BARCODE (" + labelType + ")";
                }
            } else if (IMIN_RESULT_ACTION.equals(action)) {
                // iMin Scanner
                if (intent.hasExtra(IMIN_EXTRA_DECODE_DATA)) {
                    data = intent.getStringExtra(IMIN_EXTRA_DECODE_DATA);
                    type = "BARCODE (iMin)";
                }
            }

            if (data != null && listener != null) {
                listener.onScanResult(data, type);
            }
        }
    };

    public PhysicalScannerImpl(Context context) {
        this.context = context;
        this.deviceManufacturer = Build.MANUFACTURER.toUpperCase();
        Log.d(TAG, "Detected Manufacturer: " + deviceManufacturer);
        
        initializeDrivers();
    }

    private void initializeDrivers() {
        // Factory logic to instantiate the correct driver based on hardware
        if (deviceManufacturer.contains("ZEBRA") || deviceManufacturer.contains("SYMBOL")) {
            // Zebra: RFID via SDK, Barcode via DataWedge
            rfidReader = new ZebraReaderImpl();
            // Barcode handled by BroadcastReceiver
        } else if (deviceManufacturer.contains("IMIN") || deviceManufacturer.contains("NEOSTRA")) {
            // iMin: RFID via SDK, Barcode via BroadcastReceiver
            rfidReader = new IminReaderImpl();
            // Barcode handled by BroadcastReceiver
        } else if (deviceManufacturer.contains("DATALOGIC")) {
            // Datalogic: Barcode via SDK (DatalogicReaderImpl)
            // Assuming no RFID or it's handled differently. 
            // DatalogicReaderImpl implements IReaderDevice for barcode.
            barcodeReader = new DatalogicReaderImpl();
        } else {
            Log.w(TAG, "Unknown device manufacturer: " + deviceManufacturer + ". Defaulting to generic/none.");
        }
    }

    @Override
    public void connect() {
        if (isConnected) return;

        // 1. Connect RFID Reader (Zebra / iMin)
        if (rfidReader != null) {
            rfidReader.setListener(this);
            rfidReader.setConnectionType(ConnectionType.AUTO);
            rfidReader.initialize(context);
            // Note: Some SDKs connect async, others sync.
            // Zebra: connect() returns bool. iMin: connect() triggers callback.
            // We call connect() and rely on callbacks for status.
            try {
                if (rfidReader.connect()) {
                    if (listener != null) listener.onStatusMessage(deviceManufacturer + " RFID Connecting...");
                } else {
                    if (listener != null) listener.onStatusMessage(deviceManufacturer + " RFID Connection Failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error connecting RFID", e);
            }
        }

        // 2. Connect Barcode Reader (Datalogic SDK)
        if (barcodeReader != null) {
            barcodeReader.setListener(this); // DatalogicReaderImpl calls onTagRead for barcodes
            barcodeReader.initialize(context);
            if (barcodeReader.connect()) {
                if (listener != null) listener.onStatusMessage("Datalogic Scanner Connected");
            }
        }

        // 3. Register Broadcast Receivers (Zebra / iMin Barcode)
        // Only if we are on a supported device for broadcast based scanning
        boolean isZebra = deviceManufacturer.contains("ZEBRA") || deviceManufacturer.contains("SYMBOL");
        boolean isImin = deviceManufacturer.contains("IMIN") || deviceManufacturer.contains("NEOSTRA");
        
        if (isZebra || isImin) {
            IntentFilter filter = new IntentFilter();
            
            if (isZebra) {
                filter.addAction(ACTION_RESULT_DATAWEDGE);
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                // Also listen for our custom intent if configured
                filter.addAction("com.example.diverscan.activeid.SCAN");
            }
            
            if (isImin) {
                filter.addAction(IMIN_RESULT_ACTION);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(barcodeReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(barcodeReceiver, filter);
            }
            Log.d(TAG, "Barcode Receiver Registered for " + deviceManufacturer);
        }

        isConnected = true;
    }

    @Override
    public void disconnect() {
        if (!isConnected) return;

        // Disconnect Drivers
        if (rfidReader != null) rfidReader.disconnect();
        if (barcodeReader != null) barcodeReader.disconnect();

        // Unregister Receivers
        try {
            context.unregisterReceiver(barcodeReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore if not registered
        }

        isConnected = false;
        if (listener != null) listener.onStatusMessage("Scanner Disconnected");
    }

    @Override
    public void triggerScan() {
        // Trigger Logic based on device
        if (rfidReader != null) {
            rfidReader.startInventory(); // Trigger RFID
        }

        // Trigger Soft Scan for Barcode
        if (deviceManufacturer.contains("ZEBRA") || deviceManufacturer.contains("SYMBOL")) {
            Intent i = new Intent();
            i.setAction("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "TOGGLE_SCANNING");
            context.sendBroadcast(i);
        } else if (deviceManufacturer.contains("DATALOGIC")) {
            // Datalogic might have a soft trigger intent or SDK method
            // Not implemented here for brevity, usually hardware button is used.
        }
    }

    @Override
    public void setListener(ScannerListener listener) {
        this.listener = listener;
    }

    // --- IReaderListener Implementation (Bridge from Drivers to Service) ---

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (listener != null) {
            for (ReaderTag tag : tags) {
                // Determine type based on source (this is a simplification)
                // Usually RFID tags are hex strings, barcodes can be anything.
                // We assume RFID unless it came from barcodeReader
                String type = "RFID";
                
                // If the callback comes from DatalogicReaderImpl, it's a barcode
                // But we don't know the source here easily without context.
                // However, DatalogicReaderImpl puts barcode data in ReaderTag.
                if (deviceManufacturer.contains("DATALOGIC")) {
                    type = "BARCODE";
                }

                listener.onScanResult(tag.getEpc(), type);
            }
        }
    }

    @Override
    public void onConnected(String readerName) {
        if (listener != null) listener.onStatusMessage("Connected: " + readerName);
    }

    @Override
    public void onDisconnected() {
        if (listener != null) listener.onStatusMessage("Disconnected");
    }

    @Override
    public void onConnectionError(String message) {
        if (listener != null) listener.onStatusMessage("Error: " + message);
    }

    @Override
    public void onTrigger(boolean pressed) {
        // Handle trigger events if needed
    }

    @Override
    public void onStatusMessage(String message) {
        if (listener != null) listener.onStatusMessage(message);
    }
}
