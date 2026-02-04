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

    private void initSDK() {
        new Thread(() -> {
            if (readers != null) {
                try {
                    Log.d(TAG, "Disposing previous readers instance...");
                    readers.Dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                readers = null;
            }

            try {
                availableRFIDReaderList = null;

                // Priority based on ConnectionType
                if (connectionType == ConnectionType.SERIAL_USB || connectionType == ConnectionType.AUTO) {
                     // Try Serial (eConnex)
                    try {
                        Log.d(TAG, "Searching for SERIAL/USB readers...");
                        readers = new Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL);
                        availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                        Log.d(TAG, "Serial readers found: " + (availableRFIDReaderList != null ? availableRFIDReaderList.size() : 0));
                    } catch (Exception e) {
                         Log.e(TAG, "Error checking Serial readers: " + e.getMessage());
                    }
                }
                
                if ((availableRFIDReaderList == null || availableRFIDReaderList.isEmpty()) && 
                    (connectionType == ConnectionType.BLUETOOTH || connectionType == ConnectionType.AUTO)) {
                    
                    if (readers != null) {
                        try { readers.Dispose(); } catch (Exception e) {}
                    }
                    // Try Bluetooth
                    Log.d(TAG, "Searching for BLUETOOTH readers...");
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    Log.d(TAG, "Bluetooth readers found: " + (availableRFIDReaderList != null ? availableRFIDReaderList.size() : 0));
                }

                if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
                    readerDevice = availableRFIDReaderList.get(0);
                    reader = readerDevice.getRFIDReader();
                    Log.d(TAG, "Reader selected: " + readerDevice.getName() + " Address: " + readerDevice.getAddress());
                    connect();
                } else {
                    String msg = "No se encontraron lectores Zebra (" + connectionType + ").";
                    Log.w(TAG, msg);
                    notifyError(msg);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando SDK: " + e.getMessage(), e);
                notifyError("Error inicializando SDK: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public boolean connect() {
        if (reader != null) {
            try {
                if (!reader.isConnected()) {
                    reader.connect();
                    configureReader();
                    notifyConnected(reader.getHostName());
                    return true;
                } else {
                    notifyConnected(reader.getHostName()); // Already connected
                    return true;
                }
            } catch (InvalidUsageException | OperationFailureException e) {
                notifyError("Error conectando: " + e.getMessage());
            }
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
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                
                configureTrigger(true); // Default to handheld trigger
                
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
    public boolean disconnect() {
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
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
        if (!isConnected()) return false;
        try {
            reader.Actions.Inventory.perform();
            return true;
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyError("Error iniciando inventario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stopInventory() {
        if (!isConnected()) return false;
        try {
            reader.Actions.Inventory.stop();
            return true;
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyError("Error deteniendo inventario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean startLocation(String epc) {
        if (!isConnected()) return false;
        try {
            reader.Actions.TagLocationing.Perform(epc, null, null);
            return true;
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyError("Error iniciando localización: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stopLocation() {
        if (!isConnected()) return false;
        try {
            reader.Actions.TagLocationing.Stop();
            return true;
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyError("Error deteniendo localización: " + e.getMessage());
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
            notifyError("Error escribiendo tag: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void dispose() {
        disconnect();
        if (readers != null) {
            try {
                readers.Dispose();
            } catch (Exception e) {
                // Ignore NPE inside Zebra SDK during disposal
                Log.w(TAG, "Error disposing Zebra readers (safe to ignore): " + e.getMessage());
            }
            readers = null;
        }
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
            disconnect();
        }
    }

    // Inner Event Handler
    public class EventHandler implements RfidEventsListener {
        @Override
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                Log.d(TAG, "Zebra EventReadNotify: " + myTags.length + " tags read.");
                List<ReaderTag> convertedTags = new ArrayList<>();
                for (TagData tag : myTags) {
                    Log.d(TAG, "Tag ID: " + tag.getTagID() + " RSSI: " + tag.getPeakRSSI());
                    convertedTags.add(new ReaderTag(tag.getTagID(), tag.getPeakRSSI()));
                }
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onTagRead(convertedTags));
                }
            } else {
                Log.d(TAG, "Zebra EventReadNotify: No tags in buffer.");
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents e) {
            Log.d(TAG, "Zebra Status Event: " + e.StatusEventData.getStatusEventType());
            if (e.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                boolean pressed = e.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;
                if (listener != null) {
                     new Handler(Looper.getMainLooper()).post(() -> listener.onTrigger(pressed));
                }
                
                // Mimic original behavior: start/stop inventory on trigger
                // if (pressed) {
                //    startInventory();
                // } else {
                //    stopInventory();
                // }
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
