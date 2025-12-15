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
        try {
            readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);

            var list = readers.GetAvailableRFIDReaderList();

            if (list == null || list.isEmpty()) {
                listener.onError("No se encontraron lectores RFID");
                return;
            }

            rfidReader = list.get(0).getRFIDReader();

            if (rfidReader == null) {
                listener.onError("RFIDReader es null");
                return;
            }

            rfidReader.connect();

            rfidReader.Events.addEventsListener(eventHandler);
            rfidReader.Events.setTagReadEvent(true);
            rfidReader.Events.setReaderDisconnectEvent(true);

            listener.onConnected();

        } catch (Exception e) {
            listener.onError("Error al conectar: " + e.getMessage());
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