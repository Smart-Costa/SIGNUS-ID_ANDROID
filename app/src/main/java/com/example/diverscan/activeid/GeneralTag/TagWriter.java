package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderFactory;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.example.diverscan.activeid.DeviceInterface.ReaderType;

import java.util.List;

public class TagWriter implements IReaderListener {
    final static String TAG = "RFID_TAG_WRITER";
    Context context;
    
    // Decoupled Device Interface
    private IReaderDevice device;

    private int MAX_POWER = 0;
    private String Power;
    private static final String _PASSWORD = "00";

    ResponseHandlerInterface responseHandlerInterface;

    private static TagWriter instance = null;

    public static synchronized TagWriter getInstance() {
        if (instance == null) {
            instance = new TagWriter();
        }
        return instance;
    }

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setResponseHandler(ResponseHandlerInterface handler) {
        this.responseHandlerInterface = handler;
        // If device is already connected, we might want to notify the new handler?
        // But for now, we just update the reference.
    }

    public void updateContext(ResponseHandlerInterface activity) {
        this.responseHandlerInterface = activity;
        this.context = activity.GetContext();
        Log.d(TAG, "Context updated for TagWriter");
    }

    public void onCreate(ResponseHandlerInterface activity) {
        responseHandlerInterface = activity;
        context = activity.GetContext();
        
        Power = SharedPreferencesGetSet.leer_local("potenciaAntena", context);
        try {
            if (Power != null && !Power.isEmpty()) {
                MAX_POWER = Integer.parseInt(Power);
            } else {
                MAX_POWER = 270;
                Power = "270";
            }
        } catch (NumberFormatException e) {
            MAX_POWER = 270;
            Power = "270";
            Log.e(TAG, "Error parsing power preference", e);
        }
        
        // Only InitSDK if not initialized or if we want to force a refresh.
        // But traditionally onCreate implies setup. 
        // We'll keep InitSDK here for backward compatibility, but updateContext should be used for simple context switches.
        InitSDK();
        initialized = true;
    }

    private boolean autoDetect = true;

    public void setAutoDetect(boolean enable) {
        this.autoDetect = enable;
    }

    public boolean isConnected() {
        return device != null && device.isConnected();
    }

    public String getReaderName() {
        return device != null ? device.getDeviceName() : "No Device";
    }

    public String getReaderModel() {
        return device != null ? device.getDeviceName() : "Unknown";
    }

    public List<String> getFoundDevices() {
        return new java.util.ArrayList<>();
    }

    public void setTransport(Object transport) {
        // Generic transport setter, implementation depends on specific device
        Log.d(TAG, "setTransport called with: " + transport);
    }

    public boolean setTriggerMode(String mode) {
        // Todo: Implement
        return true;
    }

    public void LocateTag(String epc) {
        if (device != null) {
            device.startLocation(epc);
        }
    }

    public void StopLocateTag() {
        if (device != null) {
            device.stopLocation();
        }
    }

    public String getDiagnosticInfo() {
        if (device instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) {
            return ((com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) device).getDiagnosticInfo();
        } else if (device instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminScannerImpl) {
             // Basic scanner diagnostic
             return "Diagnóstico Scanner: Activo (Broadcast Mode)";
        }
        return "Diagnóstico no disponible para este dispositivo.";
    }

    public void InitSDK() {
        Log.d(TAG, "InitSDK - Initializing via Factory");
        if (responseHandlerInterface != null)
            responseHandlerInterface.SetMessage("Iniciando servicio de lectura...");

        // Load reader type from preferences
        String typeStr = SharedPreferencesGetSet.leer_local("reader_type", context);
        ReaderType type = ReaderType.ZEBRA; // Default
        
        // Load connection type from preferences
        String connStr = SharedPreferencesGetSet.leer_local("connection_type", context);
        ConnectionType connType = ConnectionType.AUTO;

        if (connStr != null && !connStr.isEmpty()) {
            try {
                connType = ConnectionType.valueOf(connStr);
            } catch (Exception e) {
                connType = ConnectionType.AUTO;
                Log.w(TAG, "Tipo de conexión inválido en preferencias, usando AUTO");
            }
        }

        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = ReaderType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid reader type: " + typeStr + ", defaulting to ZEBRA");
                type = ReaderType.ZEBRA;
            }
        } else if (autoDetect) {
            // Auto-detect device model if no preference is set and autoDetect is true
            String model = android.os.Build.MODEL;
            Log.d(TAG, "Auto-detecting reader. Device Model: " + model);
            if (model != null) {
                if (model.contains("Lark 1")) {
                     type = ReaderType.IMIN_SCANNER;
                     Log.i(TAG, "Auto-detected iMin Lark 1 device (Scanner): " + model);
                } else if (model.contains("I24P01")) {
                    type = ReaderType.IMIN;
                    Log.i(TAG, "Auto-detected iMin device (RFID): " + model);
                }
            }
        }

        Log.i(TAG, "Inicializando ReaderType: " + type + " ConnectionType: " + connType);
        setReaderType(type, connType);
    }

    public void setReaderType(ReaderType type) {
        setReaderType(type, ConnectionType.AUTO);
    }

    public void setReaderType(ReaderType type, ConnectionType connType) {
        String msg = "Configurando Lector: " + type + " (" + connType + ")";
        Log.i(TAG, msg);
        if (responseHandlerInterface != null) responseHandlerInterface.SetMessage(msg);

        // Dispose existing device if any
        if (device != null) {
            try {
                Log.d(TAG, "Disposing previous device...");
                device.dispose();
            } catch (Exception e) {
                Log.e(TAG, "Error disposing previous device", e);
            }
        }

        // Use Factory to create new reader
        Log.d(TAG, "Creating new reader instance...");
        device = ReaderFactory.createReader(type, connType, context, this);
        
        // Persist preference
        SharedPreferencesGetSet.guardar_local("reader_type", type.name(), context);
        SharedPreferencesGetSet.guardar_local("connection_type", connType.name(), context);
    }
    
    public ReaderType getCurrentReaderType() {
        if (device != null) {
             if (device instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminScannerImpl) return ReaderType.IMIN_SCANNER;
             if (device instanceof com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl) return ReaderType.IMIN;
             if (device instanceof com.example.diverscan.activeid.DeviceInterface.Impl.ZebraReaderImpl) return ReaderType.ZEBRA;
        }
        // Fallback to preference or detection
        String typeStr = SharedPreferencesGetSet.leer_local("reader_type", context);
        if (typeStr != null) {
             try { return ReaderType.valueOf(typeStr); } catch (Exception e) {}
        }
        return ReaderType.ZEBRA;
    }
    
    // IReaderListener Implementation
    @Override
    public void onConnected(String readerName) {
        Log.d(TAG, "Connected to " + readerName);
        if (responseHandlerInterface != null)
            responseHandlerInterface.SetMessage("Conectado a " + readerName);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected");
        if (responseHandlerInterface != null)
            responseHandlerInterface.SetMessage("Desconectado");
    }

    @Override
    public void onConnectionError(String message) {
        Log.e(TAG, "Connection Error: " + message);
        if (responseHandlerInterface != null)
            responseHandlerInterface.SetMessage("Error: " + message);
    }

    @Override
    public void onTagRead(List<ReaderTag> tags) {
        if (responseHandlerInterface != null && tags != null && !tags.isEmpty()) {
            ReaderTag[] legacyTags = tags.toArray(new ReaderTag[0]);
            responseHandlerInterface.handleTagdata(legacyTags);
        }
    }

    @Override
    public void onTrigger(boolean pressed) {
        if (responseHandlerInterface != null)
            responseHandlerInterface.handleTriggerPress(pressed);
    }

    @Override
    public void onStatusMessage(String message) {
        if (responseHandlerInterface != null)
            responseHandlerInterface.SetMessage(message);
    }

    // Legacy Methods Mapped to New Interface

    public String onResume() {
        return connect();
    }

    public void onPause() {
        // Typically we don't disconnect on pause to keep reader active, 
        // but if required: disconnect();
    }

    public void onDestroy() {
        if (device != null) device.dispose();
    }

    private synchronized String connect() {
        if (device != null) {
            if (device.isConnected()) return "Conectado";
            if (device.connect()) {
                return "Conectado";
            } else {
                return "Error: Fallo al conectar";
            }
        }
        return "Error: No device";
    }

    private synchronized String disconnect() {
        if (device != null) {
            device.disconnect();
            return "Desconectando...";
        }
        return "Error: No device";
    }

    public synchronized void startRead() {
        performInventory();
    }

    public synchronized void stopRead() {
        stopInventory();
    }

    public synchronized void performInventory() {
        Log.d(TAG, "performInventory called");
        if (device != null) {
            Log.d(TAG, "Delegating startInventory to device: " + device.getDeviceName());
            device.startInventory();
        } else {
            Log.e(TAG, "performInventory failed: Device is null");
        }
    }

    public synchronized void stopInventory() {
        Log.d(TAG, "stopInventory called");
        if (device != null) {
            Log.d(TAG, "Delegating stopInventory to device: " + device.getDeviceName());
            device.stopInventory();
        } else {
             Log.e(TAG, "stopInventory failed: Device is null");
        }
    }

    public void setAntennaPower(int power) {
        if (device != null) device.setPower(power);
    }
    
    public boolean WriteTag(String SourceEPC, String EPCToWrite) {
        if (device != null) {
            return device.writeTag(SourceEPC, EPCToWrite, _PASSWORD);
        }
        return false;
    }

    // Stub methods for compatibility
    public void setDPO(boolean bEnable) {
        // TODO: Implement DPO in IReaderDevice if needed
    }

    public void setAccessOperationConfiguration() {
        if (device != null) device.setPower(MAX_POWER);
    }
    
    // public void setAutoDetect(boolean enable) {} // Removed duplicate
    public void setTransport(String transport) {} 
    public void setValidationMode(boolean enabled) {}    public boolean isReaderConnected() {
        return device != null && device.isConnected();
    }
    
    // Testing methods from original file
    public void Test1() {}
    public void Test2() {}
    public void Defaults() {}
    public void EncenderRFID() { performInventory(); }
    public void ApagarRFID() { stopInventory(); }
}
