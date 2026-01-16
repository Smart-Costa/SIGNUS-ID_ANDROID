package com.example.diverscan.activeid.RFID;

import android.content.Context;

import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;

public class RfidManager {

    private final Context context;
    private final RfidListener listener;

    private Readers readers;
    private RFIDReader rfidReader;

    public RfidManager(Context context, RfidListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void connect() {
        new Thread(() -> {
            try {
                // 1. Try Serial first (Preferred for eConnex)
                if (connectWithTransport(ENUM_TRANSPORT.SERVICE_SERIAL)) {
                    return;
                }

                // 2. Try Bluetooth if Serial failed
                if (connectWithTransport(ENUM_TRANSPORT.BLUETOOTH)) {
                    return;
                }

                if (listener != null) listener.onError("No se encontraron lectores RFID (Serial/BT)");

            } catch (Exception e) {
                if (listener != null) listener.onError("Error al conectar: " + e.getMessage());
            }
        }).start();
    }

    private boolean connectWithTransport(ENUM_TRANSPORT transport) {
        try {
            readers = new Readers(context, transport);
            var list = readers.GetAvailableRFIDReaderList();

            if (list != null && !list.isEmpty()) {
                rfidReader = list.get(0).getRFIDReader();
                if (rfidReader != null) {
                    rfidReader.connect();
                    configureReader(); // Configure Antennas/Region
                    
                    rfidReader.Events.addEventsListener(eventHandler);
                    rfidReader.Events.setTagReadEvent(true);
                    rfidReader.Events.setReaderDisconnectEvent(true);
                    
                    if (listener != null) listener.onConnected();
                    return true;
                }
            }
        } catch (Exception e) {
            // Log or ignore to try next transport
        }
        return false;
    }

    private void configureReader() {
        try {
            if (rfidReader.isConnected()) {
                // Set default power and region
                // Note: Real apps should manage Region configuration properly.
                // Here we assume defaults or try to set a safe config.
                try {
                   com.zebra.rfid.api3.Antennas.AntennaRfConfig config = rfidReader.Config.Antennas.getAntennaRfConfig(1);
                   config.setTransmitPowerIndex(270); // Default safe power
                   rfidReader.Config.Antennas.setAntennaRfConfig(1, config);
                } catch (Exception e) {
                    // Region might not be set, but we continue
                }
                
                // Ensure Trigger Mode is RFID
                rfidReader.Config.setTriggerMode(com.zebra.rfid.api3.ENUM_TRIGGER_MODE.RFID_MODE, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReading() {
        try {
            if (rfidReader != null)
                rfidReader.Actions.Inventory.perform();
        } catch (Exception e) {
            listener.onError("Error al iniciar lectura");
        }
    }

    public void stopReading() {
        try {
            if (rfidReader != null)
                rfidReader.Actions.Inventory.stop();
        } catch (Exception e) {
            listener.onError("Error al detener lectura");
        }
    }

    private final RfidEventsListener eventHandler = new RfidEventsListener() {

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            try {
                TagData[] tags = rfidReader.Actions.getReadTags(1);

                if (tags != null && tags.length > 0) {
                    listener.onTagRead(tags[0].getTagID());
                }

            } catch (Exception ignored) {}
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents e) {
            listener.onReaderDisconnected();
        }
    };
}