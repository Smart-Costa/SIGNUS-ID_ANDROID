package com.example.diverscan.activeid.DeviceInterface.Impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

import com.imin.rfid.RFIDManager;
import com.imin.rfid.RFIDHelper;
import com.imin.rfid.ReaderCall;
import com.imin.rfid.entity.DataParameter;
import com.imin.rfid.constant.ParamCts;
import com.imin.rfid.constant.CMD;
import com.google.gson.Gson;

public class IminReaderImpl implements IReaderDevice {
    private static final String TAG = "IminReaderImpl";
    private Context context;
    private IReaderListener listener;
    private boolean isConnected = false;
    private int currentPower = 30; // Default power
    private ConnectionType connectionType = ConnectionType.AUTO;

    @Override
    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
        // iMin SDK mostly handles internal reader, but we might support external if SDK allows.
        // For now, we store it. If connection logic changes based on type, we use it here.
    }

    private RFIDManager rfidManager;
    private RFIDHelper rfidHelper;
    private Handler uiHandler;

    @Override
    public void initialize(Context context) {
        this.context = context;
        this.uiHandler = new Handler(Looper.getMainLooper());
        Log.i(TAG, "============================================");
        Log.i(TAG, "Initializing iMin Lark 1 Reader support...");
        Log.i(TAG, "Device Model: " + android.os.Build.MODEL);
        Log.i(TAG, "============================================");

        try {
            // Check if iMin service package is installed BEFORE trying to get instance or connect
            if (!isServiceInstalled("com.imin.peripherservice")) {
                 String msg = "CRITICAL: iMin Peripheral Service (com.imin.peripherservice) not found.";
                 Log.e(TAG, msg);
                 notifyError(msg);
                 return; // Stop initialization
            }

            rfidManager = RFIDManager.getInstance();
            if (rfidManager != null) {
                Log.d(TAG, "RFIDManager instance obtained successfully.");
                try {
                    rfidManager.connect(context);
                    Log.d(TAG, "rfidManager.connect() called.");
                } catch (Exception e) {
                    Log.e(TAG, "Error calling rfidManager.connect()", e);
                    notifyError("Error connecting to RFID Manager: " + e.getMessage());
                    return;
                }
            } else {
                Log.e(TAG, "CRITICAL: RFIDManager.getInstance() returned null.");
                notifyError("RFIDManager instance is null");
                return;
            }
            
            // Auto-connect attempt
            uiHandler.postDelayed(this::connect, 500);

        } catch (Throwable e) { // Catch Throwable to handle NoClassDefFoundError if SDK is missing classes
            Log.e(TAG, "CRITICAL EXCEPTION initializing iMin SDK", e);
            notifyError("Exception initializing iMin SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        Log.d(TAG, "Attempting to connect to iMin Reader...");
        
        // Check if iMin service package is installed
        if (!isServiceInstalled("com.imin.peripherservice")) {
             String msg = "iMin Peripheral Service (com.imin.peripherservice) not found. This device might not support iMin RFID.";
             Log.e(TAG, msg);
             notifyError(msg);
             return false;
        }

        try {
            if (rfidManager == null) {
                Log.w(TAG, "rfidManager was null during connect(), re-initializing...");
                rfidManager = RFIDManager.getInstance();
                rfidManager.connect(context);
            }
            
            rfidHelper = rfidManager.getHelper();
            if (rfidHelper != null) {
                Log.i(TAG, "RFIDHelper obtained. Service is bound.");
                try {
                    registerReaderCall();
                    isConnected = true;
                    Log.i(TAG, "Connection State: CONNECTED");
                    if (listener != null) {
                        listener.onConnected("iMin Lark 1 (Internal)");
                    }
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error registering reader callback", e);
                    notifyError("Error registering callback: " + e.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "RFIDHelper is null. Service might not be bound yet or RFID module is unavailable.");
                notifyError("RFID Helper null - Service unavailable");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to iMin Reader", e);
            notifyError("Connection failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isServiceAvailable() {
        return isServiceInstalled("com.imin.peripherservice");
    }

    public String getDiagnosticInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DIAGNÓSTICO RFID ===\n");
        
        boolean service = isServiceInstalled("com.imin.peripherservice");
        sb.append("Servicio iMin: ").append(service ? "INSTALADO" : "NO INSTALADO").append("\n");
        
        if (!service) {
            sb.append("CRÍTICO: El servicio 'com.imin.peripherservice' es requerido para el módulo RFID.\n");
            return sb.toString();
        }

        sb.append("RFIDManager: ").append(rfidManager != null ? "OK" : "NULL").append("\n");
        sb.append("RFIDHelper: ").append(rfidHelper != null ? "OK" : "NULL").append("\n");
        sb.append("Conectado: ").append(isConnected).append("\n");
        
        if (isConnected && rfidHelper != null) {
            sb.append("Tipo Conexión: INTERNA (API Service)\n");
            try {
                // Try to get version if method exists, strictly speculative based on common SDKs
                // If it fails, we catch it.
                // String fw = rfidHelper.getFirmwareVersion(); 
                // sb.append("Firmware: ").append(fw).append("\n");
            } catch (Exception e) {
                sb.append("Firmware: N/A\n");
            }
        } else {
            sb.append("Estado: Esperando conexión...\n");
        }
        
        return sb.toString();
    }

    private boolean isServiceInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void registerReaderCall() {
        if (rfidHelper == null) return;
        Log.d(TAG, "Registering ReaderCallback...");
        rfidHelper.registerReaderCall(new ReaderCall() {
            @Override
            public void onSuccess(byte cmd, DataParameter dataParameter) {
                Log.d(TAG, "CMD Success: " + String.format("0x%02X", cmd));
            }

            @Override
            public void onTag(byte cmd, byte state, DataParameter dataParameter) {
                // Verbose log for high frequency tag reads
                // Log.v(TAG, "onTag callback received. cmd: " + cmd + " state: " + state);
                if (dataParameter != null) {
                    String epc = dataParameter.getString(ParamCts.TAG_EPC);
                    String rssiStr = dataParameter.getString(ParamCts.TAG_RSSI);
                    
                    Log.v(TAG, "TAG READ -> EPC: " + epc + " | RSSI: " + rssiStr);

                    if (epc != null) {
                        short rssi = 0;
                        try {
                            if (rssiStr != null) rssi = Short.parseShort(rssiStr);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        
                        ReaderTag tag = new ReaderTag(epc, rssi);
                        notifyTagsRead(new ReaderTag[]{tag});
                    } else {
                         Log.w(TAG, "onTag received null EPC");
                    }
                } else {
                    Log.w(TAG, "onTag received null DataParameter");
                }
            }

            @Override
            public void onFiled(byte cmd, byte errorCode, String msg) {
                Log.w(TAG, "CMD Failed: " + String.format("0x%02X", cmd) + " | ErrorCode: " + errorCode + " | Msg: " + msg);
            }
        });
    }

    @Override
    public boolean disconnect() {
        Log.d(TAG, "Disconnecting iMin Reader...");
        try {
            if (rfidHelper != null) {
                // Check service before trying to unregister to avoid NameNotFoundException
                if (isServiceInstalled("com.imin.peripherservice")) {
                    try {
                        rfidHelper.unregisterReaderCall();
                        rfidHelper.tagInventoryRawStopReading();
                    } catch (Exception e) {
                        Log.w(TAG, "Error unregistering/stopping: " + e.getMessage());
                    }
                }
            }
            if (rfidManager != null) {
                rfidManager.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting", e);
        }
        
        isConnected = false;
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
        if (!isConnected || rfidHelper == null) {
            Log.e(TAG, "startInventory failed: Not connected or Helper null");
            return false;
        }
        Log.d(TAG, "Starting Inventory (iMin)...");
        try {
            rfidHelper.tagInventoryRawStartReading();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting inventory", e);
            return false;
        }
    }

    @Override
    public boolean stopInventory() {
        if (!isConnected || rfidHelper == null) {
            Log.e(TAG, "stopInventory failed: Not connected or Helper null");
            return false;
        }
        Log.d(TAG, "Stopping Inventory (iMin)...");
        try {
            rfidHelper.tagInventoryRawStopReading();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error stopping inventory", e);
            return false;
        }
    }

    @Override
    public boolean startLocation(String epc) {
        Log.w(TAG, "Location not implemented for iMin");
        return false;
    }

    @Override
    public boolean stopLocation() {
        return false;
    }

    @Override
    public void setPower(int power) {
        this.currentPower = power;
        Log.d(TAG, "setPower called with value: " + power);
        
        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "Cannot set power: Reader not connected or helper null");
            return;
        }

        try {
            // Normalize power if it comes in Zebra format (e.g. 270 for 27dBm)
            int p = power;
            if (p > 33) {
                p = p / 10;
            }
            Log.i(TAG, "Normalized Power: " + p + " dBm");
            
            ReadWritePower readWritePower = new ReadWritePower();
            readWritePower.readPower = p;
            readWritePower.writePower = p;
            String config = new Gson().toJson(readWritePower);
            
            Log.d(TAG, "Sending Power Config JSON: " + config);
            rfidHelper.extendOperation(CMD.SET_READ_WRITE_POWER, config);
        } catch (Exception e) {
            Log.e(TAG, "Error setting power", e);
            notifyError("Error setting power: " + e.getMessage());
        }
    }

    private static class ReadWritePower {
        public int readPower;
        public int writePower;
    }

    @Override
    public void setListener(IReaderListener listener) {
        this.listener = listener;
    }

    @Override
    public String getDeviceName() {
        return "iMin Lark 1 I24P01";
    }

    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
        Log.d(TAG, "Writing tag: " + sourceEpc + " -> " + newEpc);
        if (!isConnected || rfidHelper == null) return false;
        
        // TODO: Implement write logic using rfidHelper.writeTag(...)
        // rfidHelper.writeTag(...) requires bank, address, data, etc.
        // This is complex and requires understanding the specific parameters
        return false;
    }

    @Override
    public void dispose() {
        disconnect();
    }
    
    private void notifyTagsRead(ReaderTag[] tags) {
        if (listener != null) {
            // Ensure this runs on UI thread
            uiHandler.post(() -> listener.onTagRead(java.util.Arrays.asList(tags)));
        }
    }

    private void notifyError(String msg) {
        if (listener != null) {
            uiHandler.post(() -> listener.onConnectionError(msg));
        }
    }
}
