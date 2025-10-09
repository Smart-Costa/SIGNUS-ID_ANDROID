package com.example.diverscan.activeid.GeneralTag;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
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

public class RFIDHandler implements Readers.RFIDReaderEventHandler  {
    final static String TAG = "RFID_SAMPLE";
    // RFID Reader
    private static Readers readers;
    private static ReaderDevice readerDevice;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static RFIDReader reader;
    private EventHandler eventHandler;
    // UI and context
    //TextView textView;
    private Context context;
    ResponseHandlerInterface responseHandlerInterface;
    // general
    private int MAX_POWER;
    private String Power;
    // In case of RFD8500 change reader name with intended device below from list of paired RFD8500
    String readername = "RFD8500123";
    private static final String _PASSWORD = "00";

    void onCreate(ResponseHandlerInterface activity) {

        responseHandlerInterface = activity;


        // application context
        context = activity.GetContext();

        Power = SharedPreferencesGetSet.leer_local("potenciaAntena",context);
        MAX_POWER = Integer.parseInt(Power);

        // SDK
        InitSDK();
    }

    // TEST BUTTON functionality
    // following two tests are to try out different configurations features

    public String Test1() {
        return "Antenna power Set to 220";
    }

    public String Test2() {

        return "Session set to S2";
    }

    public String Defaults() {
        // check reader connection
        if (!isReaderConnected())
            return "Not connected";
        try {
            // Power to 270
            Antennas.AntennaRfConfig config = null;
            config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(MAX_POWER);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            // singulation to S0
            Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
            s1_singulationControl.setSession(SESSION.SESSION_S0);
            s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
            reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return e.getResults().toString() + " " + e.getVendorMessage();
        }
        return "Default settings applied";
    }

    private void WriteTag(String sourceEPC, String Password, MEMORY_BANK memory_bank, String targetData, int offset) {
        Log.d(TAG, "WriteTag " + targetData);
        try {
            TagData tagData = null;
            String tagId = sourceEPC;
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
            String writeData = targetData;
            writeAccessParams.setAccessPassword(Long.parseLong(Password,16));
            writeAccessParams.setMemoryBank(memory_bank);
            writeAccessParams.setOffset(offset); // start writing from word offset 0
            writeAccessParams.setWriteData(writeData);
            // set retries in case of partial write happens
            writeAccessParams.setWriteRetries(3);
            // data length in words
            writeAccessParams.setWriteDataLength(writeData.length() / 4);
            // 5th parameter bPrefilter flag is true which means API will apply pre filter internally
            // 6th parameter should be true in case of changing EPC ID it self i.e. source and target both is EPC
            boolean useTIDfilter = memory_bank == MEMORY_BANK.MEMORY_BANK_EPC;

            reader.Actions.TagAccess.writeWait(tagId, writeAccessParams, null, tagData, true, useTIDfilter);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage() + " " + e.getStackTrace());

        }catch (OperationFailureException ex){
            ex.printStackTrace();
        }

    }



    public void setAntennaPower(int power) {
        Log.d(TAG, "setAntennaPower " + power);
        try {
            // set antenna configurations
            Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(power);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public void setDPO(boolean bEnable) {
        Log.d(TAG, "setDPO " + bEnable);
        try {
            // control the DPO
            reader.Config.setDPOState(bEnable ? DYNAMIC_POWER_OPTIMIZATION.ENABLE : DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public void setAccessOperationConfiguration() {
        // set required power and profile
        setAntennaPower(MAX_POWER);
        // in case of RFD8500 disable DPO
        if (reader.getHostName().contains("RFD8500"))
            setDPO(false);
        //
        try {
            // set access operation time out value to 1 second, so reader will tries for a second
            // to perform operation before timing out
            reader.Config.setAccessOperationWaitTimeout(7000);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    private boolean isReaderConnected() {
        if (reader != null && reader.isConnected())
            return true;
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    //
    //  Activity life cycle behavior
    //

    public String onResume() {
        return connect();
    }

    public void onPause() {
        disconnect();
    }

    public void onDestroy() {
        dispose();
    }

    //
    // RFID SDK
    //

    private void InitSDK() {
        Log.d(TAG, "InitSDK");
        if (readers == null) {
            new CreateInstanceTask().execute();
        } else
            new ConnectionTask().execute();
    }

    // Enumerates SDK based on host device
    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "CreateInstanceTask");
            // Based on support available on host device choose the reader type
            InvalidUsageException invalidUsageException = null;
            try {
                readers = new Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL);
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                invalidUsageException = e;
            }
            if (invalidUsageException != null) {
                readers.Dispose();
                readers = null;
                if (readers == null) {
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new ConnectionTask().execute();
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (reader != null)
                return connect();
            return "Failed to find or connect reader";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //textView.setText(result);
        }
    }

    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        try {
            if (readers != null) {
                readers.attach(this);
                if (readers.GetAvailableRFIDReaderList() != null) {
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (availableRFIDReaderList.size() != 0) {
                        // if single reader is available then connect it
                        if (availableRFIDReaderList.size() == 1) {
                            readerDevice = availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                        } else {
                            // search reader specified by name
                            for (ReaderDevice device : availableRFIDReaderList) {
                                if (device.getName().equals(readername)) {
                                    readerDevice = device;
                                    reader = readerDevice.getRFIDReader();
                                }
                            }
                        }
                    }
                }
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        }
    }

    // handler for receiving reader appearance events
    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        new ConnectionTask().execute();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (readerDevice.getName().equals(reader.getHostName()))
            disconnect();
    }

    private synchronized String connect() {
        if (reader != null) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    // Establish connection to the RFID Reader
                    reader.connect();
                    ConfigureReader();
                    return "Connected";
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();
                return "Connection failed" + e.getVendorMessage() + " " + des;
            }
        }
        return "";
    }

    private void ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + reader.getHostName());
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
                // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(MAX_POWER);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                reader.Config.Antennas.setAntennaRfConfig(1, config);
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                // delete any prefilters
                reader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void disconnect() {
        Log.d(TAG, "disconnect " + reader);
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                responseHandlerInterface.SetMessage("Disconnected");
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        try {
            if (readers != null) {
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void performInventory() {
        // check reader connection
        if (!isReaderConnected())
            return;
        try {
            reader.Actions.Inventory.perform();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopInventory() {
        // check reader connection
        if (!isReaderConnected())
            return;
        try {
            reader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                    if (myTags[index].isContainsLocationInfo()) {
                        short dist = myTags[index].LocationInfo.getRelativeDistance();
                        Log.d(TAG, "Tag relative distance " + dist);
                    }
                }
                // possibly if operation was invoked from async task and still busy
                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
                new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, myTags);
            }
        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification 2: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            responseHandlerInterface.handleTriggerPress(true);
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            responseHandlerInterface.handleTriggerPress(false);
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            responseHandlerInterface.handleTagdata(params[0]);
            return null;
        }
    }




}
