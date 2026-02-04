package com.example.diverscan.activeid.DeviceInterface.Impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

import java.util.Collections;

public class IminScannerImpl implements IReaderDevice {
    private static final String TAG = "IminScannerImpl";
    private Context context;
    private IReaderListener listener;
    private boolean isConnected = false;
    private ConnectionType connectionType = ConnectionType.AUTO;

    // Broadcast Actions
    private static final String RESULT_ACTION = "com.imin.scanner.api.RESULT_ACTION";
    private static final String DEVICE_CONNECTION = "com.imin.scanner.api.DEVICE_CONNECTION";
    private static final String DEVICE_DISCONNECTION = "com.imin.scanner.api.DEVICE_DISCONNECTION";
    private static final String CONNECTION_STATUS_ACTION = "com.imin.scanner.api.DEVICE_IS_CONNECTION";
    private static final String CONNECTION_BACK_ACTION = "com.imin.scanner.api.CONNECTION_RESULT";
    private static final String GET_STATUS_PROP = "persist.sys.imin.scanner.status";
    
    private static final String EXTRA_DECODE_DATA = "decode_data";
    private static final String EXTRA_DECODE_DATA_STR = "decode_data_str";
    
    private ScannerReceiver scannerReceiver;
    
    @Override
    public void initialize(Context context) {
        this.context = context;
        Log.d(TAG, "Initializing iMin Scanner (Broadcast Receiver mode)");
        checkSystemPropertyStatus();
    }

    private void checkSystemPropertyStatus() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            java.lang.reflect.Method get = c.getMethod("get", String.class, String.class);
            String connectStatus = (String) get.invoke(c, GET_STATUS_PROP, "0");
            Log.d(TAG, "System Property Scanner Status: " + connectStatus + " (1=Connected, 0=Disconnected)");
            if ("1".equals(connectStatus)) {
                 isConnected = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking system property", e);
        }
    }

    @Override
    public boolean connect() {
        if (isConnected) return true;
        
        Log.d(TAG, "Connecting iMin Scanner (Registering Receiver for actions)...");
        try {
            scannerReceiver = new ScannerReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RESULT_ACTION);
            intentFilter.addAction(DEVICE_CONNECTION);
            intentFilter.addAction(DEVICE_DISCONNECTION);
            intentFilter.addAction(CONNECTION_BACK_ACTION);
            context.registerReceiver(scannerReceiver, intentFilter);
            
            // Request status update via broadcast too
            try {
                Intent statusIntent = new Intent(CONNECTION_STATUS_ACTION);
                context.sendBroadcast(statusIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error sending status request broadcast", e);
            }
            
            isConnected = true; // Assume connected until proven otherwise by callbacks
            if (listener != null) {
                listener.onConnected("iMin Scanner (Internal)");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error registering scanner receiver", e);
            if (listener != null) {
                listener.onConnectionError("Error registering scanner: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        if (!isConnected || scannerReceiver == null) return true;

        Log.d(TAG, "Disconnecting iMin Scanner...");
        try {
            context.unregisterReceiver(scannerReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
        
        isConnected = false;
        scannerReceiver = null;
        if (listener != null) {
            listener.onDisconnected();
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean startInventory() {
        // Scanner is always "listening" via Broadcast, but we can simulate start
        // or trigger soft scan if SDK allows. For now, we assume hardware button trigger.
        Log.d(TAG, "startInventory: Listening for broadcasts...");
        return true;
    }

    @Override
    public boolean stopInventory() {
        Log.d(TAG, "stopInventory: Still listening until disconnect...");
        return true;
    }

    @Override
    public boolean startLocation(String epc) {
        return false;
    }

    @Override
    public boolean stopLocation() {
        return false;
    }

    @Override
    public void setPower(int power) {
        // No power setting for Scanner
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
    public String getDeviceName() {
        return "iMin Scanner (Lark 1)";
    }

    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
        return false; // Not supported
    }

    @Override
    public void dispose() {
        disconnect();
    }

    private class ScannerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Broadcast Received Action: " + action);
            
            if (DEVICE_CONNECTION.equals(action)) {
                 Log.i(TAG, "Scanner Device Connected");
                 isConnected = true;
            } else if (DEVICE_DISCONNECTION.equals(action)) {
                 Log.i(TAG, "Scanner Device Disconnected");
                 isConnected = false;
            } else if (CONNECTION_BACK_ACTION.equals(action)) {
                 int type = intent.getIntExtra("com.imin.scanner.api.status", 0);
                 Log.i(TAG, "Scanner Status Callback: " + type + " (1=Connected)");
                 isConnected = (type == 1);
            } else if (RESULT_ACTION.equals(action)) {
                // Log all extras for debugging
                if (intent.getExtras() != null) {
                    for (String key : intent.getExtras().keySet()) {
                         Object value = intent.getExtras().get(key);
                         Log.d(TAG, "Extra: " + key + " = " + value);
                    }
                }

                String data = intent.getStringExtra(EXTRA_DECODE_DATA);
                String dataStr = intent.getStringExtra(EXTRA_DECODE_DATA_STR);
                
                Log.d(TAG, "Scanner Data Decoded. Data: " + data + ", DataStr: " + dataStr);

                String finalData = data;
                if (finalData == null || finalData.isEmpty()) {
                    finalData = dataStr;
                }

                if (finalData != null && !finalData.isEmpty()) {
                    // Wrap barcode as ReaderTag
                    ReaderTag tag = new ReaderTag(finalData, (short) 0); // RSSI 0
                    if (listener != null) {
                        listener.onTagRead(Collections.singletonList(tag));
                    }
                }
            }
        }
    }
}
