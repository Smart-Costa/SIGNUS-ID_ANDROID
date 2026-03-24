package com.example.diverscan.activeid.DeviceInterface.Impl;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;
import java.util.List;

public class ZebraReaderImpl implements IReaderDevice, Readers.RFIDReaderEventHandler {
    private static final String TAG = "ZebraReaderImpl";
    private Context context;
    private IReaderListener listener;
    
    private Readers readers;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice readerDevice;
    private RFIDReader reader;
    private EventHandler eventHandler;
    
    private int maxPower = 270;
    private String readerNamePreference = null; // Can be set if we want to target a specific reader
    private ConnectionType connectionType = ConnectionType.AUTO;

    @Override
    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        String powerStr = SharedPreferencesGetSet.leer_local("potenciaAntena", context);
        try {
            if (powerStr != null && !powerStr.isEmpty()) {
                maxPower = Integer.parseInt(powerStr);
            }
        } catch (NumberFormatException e) {
            maxPower = 270;
        }
        
        initSDK();
    }

    private boolean isDisposing = false;

    private void initSDK() {
        new Thread(() -> {
            if (readers != null) {
                try {
                    Log.d(TAG, "Disposing previous readers instance...");
                    readers.Dispose();
                } catch (Exception e) {
                    Log.w(TAG, "Dispose previo falló, se continúa con reinicialización: " + e.getMessage());
                } finally {
                    // BUG #2 FIX: siempre liberar la referencia incluso si Dispose() lanza NPE
                    readers = null;
                }
            }

            try {
                availableRFIDReaderList = null;
                boolean notifiedNoReaderSpecific = false;

                // Priority based on ConnectionType
                if (connectionType == ConnectionType.SERIAL || connectionType == ConnectionType.USB || connectionType == ConnectionType.AUTO) {
                        // BUG #1 FIX: SERVICE_USB para conexion USB directa (RFD40, RFD8500, etc.)
                    // SERVICE_SERIAL es para cable COM/RS-232 (eConnex), NO para USB fisico
                    ENUM_TRANSPORT transport;
                    if (connectionType == ConnectionType.USB) {
                        transport = ENUM_TRANSPORT.SERVICE_USB;
                        Log.d(TAG, "Searching for USB readers (SERVICE_USB)...");
                    } else {
                        transport = ENUM_TRANSPORT.SERVICE_SERIAL;
                        Log.d(TAG, "Searching for SERIAL readers (SERVICE_SERIAL)...");
                    }
                    try {
                        readers = new Readers(context, transport);
                        readers.attach(this);
                        availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                        Log.d(TAG, "Readers found [" + transport + "]: " + (availableRFIDReaderList != null ? availableRFIDReaderList.size() : 0));
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking readers [" + transport + "]: " + e.getMessage());
                    }
                }
                
                if ((availableRFIDReaderList == null || availableRFIDReaderList.isEmpty()) &&
                    (connectionType == ConnectionType.BLUETOOTH
                        || connectionType == ConnectionType.AUTO)) {
                    
                    if (readers != null) {
                        try { readers.Dispose(); } catch (Exception e) {}
                    }
                    Log.d(TAG, "Searching for BLUETOOTH readers...");
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    readers.attach(this); // Attach for events
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    Log.d(TAG, "Bluetooth readers found: " + (availableRFIDReaderList != null ? availableRFIDReaderList.size() : 0));
                }

                if ((availableRFIDReaderList == null || availableRFIDReaderList.isEmpty()) &&
                    (connectionType == ConnectionType.SERIAL || connectionType == ConnectionType.USB)) {
                    notifyError("No se encontraron lectores Zebra por " + connectionType + ".");
                    notifiedNoReaderSpecific = true;
                }

                if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
                    readerDevice = availableRFIDReaderList.get(0);
                    reader = readerDevice.getRFIDReader();
                    Log.d(TAG, "Reader selected: " + readerDevice.getName() + " Address: " + readerDevice.getAddress());
                    connect();
                } else {
                    String msg = "No se encontraron lectores Zebra (" + connectionType + ").";
                    Log.w(TAG, msg);
                    if (!notifiedNoReaderSpecific) {
                        notifyError(msg);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando SDK: " + e.getMessage(), e);
                notifyError("Error inicializando SDK: " + e.getMessage());
            }
        }).start();
    }

    private boolean isConnecting = false;
    private boolean autoReconnect = true;
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDisposing || isConnected()) return;
            Log.d(TAG, "Attempting auto-reconnect...");
            new Thread(() -> {
                 if (!connect()) {
                     // If failed, schedule next attempt
                     reconnectHandler.postDelayed(this, 5000);
                 }
            }).start();
        }
    };

    @Override
    public synchronized boolean connect() {
        if (isConnecting) return false;
        isConnecting = true;
        try {
            if (reader != null) {
                try {
                    if (!reader.isConnected()) {
                        // Retry logic for connection
                        int retries = 3;
                        while (retries > 0) {
                            try {
                                reader.connect();
                                configureReader();
                                notifyConnected(reader.getHostName());
                                isConnecting = false;
                                return true;
                            } catch (OperationFailureException e) {
                                retries--;
                                Log.w(TAG, "Connection attempt failed, retries left: " + retries + " Error: " + e.getVendorMessage());
                                if (retries == 0) throw e;
                                try { Thread.sleep(500); } catch (InterruptedException ie) {}
                            }
                        }
                    } else {
                        notifyConnected(reader.getHostName()); // Already connected
                        isConnecting = false;
                        return true;
                    }
                } catch (InvalidUsageException | OperationFailureException e) {
                    notifyError("Error conectando: " + e.getMessage() + (e instanceof OperationFailureException ? " [Info: " + ((OperationFailureException)e).getVendorMessage() + "]" : ""));
                }
            }
        } finally {
            isConnecting = false;
        }
        return false;
    }

    private void configureReader() {
        if (reader != null && reader.isConnected()) {
            try {
                if (eventHandler == null) {
                    eventHandler = new EventHandler();
                }
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                try {
                    reader.Events.setReaderDisconnectEvent(true);
                } catch (Exception e) {
                    Log.w(TAG, "setReaderDisconnectEvent not supported or failed: " + e.getMessage());
                }
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                
                // BUGFIX: We MUST use IMMEDIATE trigger type so that startInventory() starts immediately
                // regardless of whether it's triggered by the UI button or the physical trigger (handled via eventStatusNotify).
                configureTrigger(false); 
                
                setPower(maxPower);
                
                // Singulation
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                
                reader.Actions.PreFilters.deleteAll();
                
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void configureTrigger(boolean isHandheld) {
        try {
            TriggerInfo triggerInfo = new TriggerInfo();
            if (isHandheld) {
                triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD);
                triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT);
            } else {
                triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
                triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            }
            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            reader.Config.setStopTrigger(triggerInfo.StopTrigger);
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean disconnect() {
        // Cancel any pending reconnect attempts
        reconnectHandler.removeCallbacks(reconnectRunnable);
        
        try {
            if (reader != null) {
                try {
                    reader.Events.removeEventsListener(eventHandler);
                } catch (Exception e) { /* Ignore if not added */ }
                
                try {
                    reader.disconnect();
                } catch (Exception e) {
                    Log.w(TAG, "Error during reader.disconnect(): " + e.getMessage());
                }
                
                notifyDisconnected();
                return true;
            }
        } catch (Exception e) {
            notifyError("Error desconectando: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        return reader != null && reader.isConnected();
    }

    @Override
    public boolean startInventory() {
        if (reader == null) {
            notifyError("Error: Lector no inicializado.");
            return false;
        }
        try {
            if (!reader.isConnected()) {
                notifyError("Error: Lector desconectado.");
                return false;
            }
            reader.Actions.Inventory.perform();
            return true;
        } catch (InvalidUsageException | OperationFailureException e) {
            String msg = (e.getMessage() != null) ? e.getMessage() : "Error de operación";
            String detail = (e instanceof OperationFailureException) ? ((OperationFailureException)e).getVendorMessage() : "";
            
            // Ignore "Operation In Progress" or "Command in progress" as they are redundant
            if (detail.contains("Operation In Progress") || detail.contains("Command in progress")) {
                Log.d(TAG, "Inventory already in progress, ignoring redundant start command.");
                return true; 
            }
            
            notifyError("Error iniciando inventario: " + msg + " [Info: " + detail + "]");
            return false;
        }
    }

    @Override
    public boolean stopInventory() {
        if (!isConnected() || reader == null) return false;
        try {
            reader.Actions.Inventory.stop();
            return true;
        } catch (InvalidUsageException e) {
            // Already stopped or not running, ignore
            return true;
        } catch (OperationFailureException e) {
            // If it's already stopped, it might throw here too. Check results if possible.
            // For now, let's just log it instead of showing a scary error if it happens during stop.
            Log.w(TAG, "Fallo al detener inventario: " + e.getVendorMessage());
            return false;
        }
    }

    @Override
    public void setPower(int power) {
        this.maxPower = power;
        if (isConnected()) {
            try {
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(power);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                reader.Config.Antennas.setAntennaRfConfig(1, config);
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setListener(IReaderListener listener) {
        this.listener = listener;
    }

    @Override
    public String getDeviceName() {
        return reader != null ? reader.getHostName() : "Unknown";
    }

    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
         try {
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
            writeAccessParams.setAccessPassword(Long.parseLong(password != null ? password : "0", 16));
            writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
            writeAccessParams.setOffset(2); // Standard EPC offset
            writeAccessParams.setWriteData(newEpc);
            writeAccessParams.setWriteRetries(3);
            writeAccessParams.setWriteDataLength(newEpc.length() / 4);

            reader.Actions.TagAccess.writeWait(sourceEpc, writeAccessParams, null, new TagData(), true, true);
            return true;
        } catch (Exception e) {
            String msg = (e.getMessage() != null) ? e.getMessage() : "Error de operación (Sin mensaje)";
            String detail = (e instanceof OperationFailureException) ? " [Info: " + ((OperationFailureException)e).getVendorMessage() + "]" : "";
            notifyError("Error escribiendo tag: " + msg + detail);
            return false;
        }
    }

    @Override
    public void dispose() {
        isDisposing = true;
        reconnectHandler.removeCallbacks(reconnectRunnable);
        try {
            disconnect();
        } catch (Exception e) {
            Log.w(TAG, "Error durante disconnect en dispose: " + e.getMessage());
        }
        if (readers != null) {
            try {
                readers.Dispose();
            } catch (Exception e) {
                Log.w(TAG, "Error durante readers.Dispose(): " + e.getMessage());
            } finally {
                // BUG #2 FIX: always null-out to avoid stale reference causing NPE
                readers = null;
            }
        }
        reader = null;
        readerDevice = null;
        availableRFIDReaderList = null;
        isDisposing = false;
    }

    // Event Handling
    
    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        // Handle new reader appearing
        // For simplicity, we might auto-connect if not connected
        if (!isConnected()) {
             new Thread(() -> {
                 this.readerDevice = readerDevice;
                 this.reader = readerDevice.getRFIDReader();
                 connect();
             }).start();
        }
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        if (reader != null && reader.getHostName().equals(readerDevice.getName())) {
            Log.w(TAG, "Reader Disappeared: " + readerDevice.getName());
            notifyDisconnected();
             if (!isDisposing && autoReconnect) {
                 reconnectHandler.postDelayed(reconnectRunnable, 1000);
             }
        }
    }

    // Inner Event Handler
    public class EventHandler implements RfidEventsListener {
        @Override
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                List<ReaderTag> convertedTags = new ArrayList<>();
                for (TagData tag : myTags) {
                    convertedTags.add(new ReaderTag(tag.getTagID(), tag.getPeakRSSI()));
                }
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onTagRead(convertedTags));
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents e) {
            if (e.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                boolean pressed = e.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;
                if (listener != null) {
                     new Handler(Looper.getMainLooper()).post(() -> listener.onTrigger(pressed));
                }
                
                /* REMOVED redundant calls - they are handled by the Activity via onTrigger listener
                if (pressed) {
                    startInventory();
                } else {
                    stopInventory();
                }
                */
            } else if (e.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                Log.w(TAG, "Received DISCONNECTION_EVENT from reader");
                disconnect();
            }
        }
    }

    // Helpers to notify listener on main thread
    private void notifyConnected(String name) {
        if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onConnected(name));
    }

    private void notifyDisconnected() {
        if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onDisconnected());
    }

    private void notifyError(String msg) {
        if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onConnectionError(msg));
    }
}
