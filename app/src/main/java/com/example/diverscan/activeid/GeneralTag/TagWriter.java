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

public class TagWriter implements Readers.RFIDReaderEventHandler{
    final static String TAG = "RFID_SAMPLE";
    Context context;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private EventHandler eventHandler;
    //*************************************************************
    // handheld que usan en Fidelitas.
    String readername = "MC3300x";
    //String readername = "RFD8500123";
    //String readername = "MC3300R";
    //*************************************************************
    private int MAX_POWER = 0;
    private String Power;
    private static final String _PASSWORD = "00";

    ResponseHandlerInterface responseHandlerInterface;

    //*******************************************************************************************
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
    }
    public void onCreate(ResponseHandlerInterface activity)
    {
        responseHandlerInterface = activity;
        // Use ApplicationContext to prevent memory leaks and crashes on Activity destruction
        context = activity.GetContext().getApplicationContext();
        
        Power = SharedPreferencesGetSet.leer_local("potenciaAntena", context);
        try {
            if (Power != null && !Power.isEmpty()) {
                MAX_POWER = Integer.parseInt(Power);
            } else {
                MAX_POWER = 270; // Valor por defecto seguro
                Power = "270";
            }
        } catch (NumberFormatException e) {
            MAX_POWER = 270;
            Power = "270";
            Log.e(TAG, "Error parsing power preference", e);
        }
        InitSDK();
        initialized = true;
    }

    //*******************************************************************************************

    public String Test1() {
        return "Antenna power Set to 220";
    }

    //*******************************************************************************************

    public String Test2() {
        return "Session set to S2";
    }

    //*******************************************************************************************

    // Added for Connection Validation
    public boolean isConnected() {
        return reader != null && reader.isConnected();
    }

    public String getReaderName() {
        if (reader != null && reader.isConnected()) {
             try {
                 return reader.getHostName();
             } catch (Exception e) {
                 return "Error";
             }
        }
        return "Desconectado";
    }

    public String getReaderModel() {
        if (reader != null && reader.isConnected()) {
             try {
                 return reader.ReaderCapabilities.getModelName();
             } catch (Exception e) {
                 return "Desconocido";
             }
        }
        return "--";
    }

    public ArrayList<String> getFoundDevices() {
        ArrayList<String> devices = new ArrayList<>();
        if (availableRFIDReaderList != null) {
            for (ReaderDevice device : availableRFIDReaderList) {
                devices.add(device.getName() + " (" + device.getAddress() + ")");
            }
        }
        return devices;
    }

    //*******************************************************************************************

    // cambios realizados por andrey sanchez Zuñiga
    public String Defaults()
    {
        String value ="";
        // check reader connection
        if (!isReaderConnected())
        {
            value="No ha conectado";
            return value;
        }

        try
        {
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
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
            value ="No se ha conectado al lector o no hay lectores disponibles.";
            return  value ;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Defaults error
        }
        return value;
    }

    //*******************************************************************************************

    public boolean setTriggerMode(String Val)
    { //Se recomienda pasar a boolean para controlar la conexión correctamente
        try
        {
            if(Val.equals("RFID"))
            {
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true);
                if(connect() != "conectado"){
                    return false;
                }
            }
            else if(Val.equals("BARCODE"))
            {

                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
                if(disconnect() != "desconectado"){
                    return false;
                }
            }
        }
        catch(InvalidUsageException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //*******************************************************************************************

    public void EncenderRFID()
    {
        try{
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false);
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true); //comentado
            connect();  //comentado
        }catch(InvalidUsageException e){
            e.printStackTrace();
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
        }
    }

    //*******************************************************************************************

    public void ApagarRFID()
    {
        try
        {
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false);   //comentado
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
        }
        catch(InvalidUsageException e)
        {
            e.printStackTrace();
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
        }
    }
    private boolean WriteTag(String sourceEPC, String Password, MEMORY_BANK memory_bank, String EPCToWrite, int offset) {
        Log.d(TAG, "WriteTag " + EPCToWrite);
        try {
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
            writeAccessParams.setAccessPassword(Long.parseLong(Password,16));
            writeAccessParams.setMemoryBank(memory_bank);
            writeAccessParams.setOffset(offset); // start writing from word offset 0
            writeAccessParams.setWriteData(EPCToWrite);
            // set retries in case of partial write happens
            writeAccessParams.setWriteRetries(5);
            // data length in words
            writeAccessParams.setWriteDataLength(EPCToWrite.length() / 4);
            // 5th parameter bPrefilter flag is true which means API will apply pre filter internally
            // 6th parameter should be true in case of changing EPC ID it self i.e. source and target both is EPC
            boolean useTIDfilter = memory_bank == MEMORY_BANK.MEMORY_BANK_EPC;
            reader.Actions.TagAccess.writeWait(sourceEPC, writeAccessParams, null, new TagData(), true, useTIDfilter);
        } catch (OperationFailureException | InvalidUsageException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage() + " TESTING " + e.getStackTrace());
            return false;
        }
        return true;
    }

    //*******************************************************************************************

    public boolean WriteTag(String SourceEPC, String EPCToWrite)
    {
        try
        {
            setAccessOperationConfiguration();
            return WriteTag(SourceEPC, _PASSWORD, MEMORY_BANK.MEMORY_BANK_EPC, EPCToWrite, 2);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    //*******************************************************************************************

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

    //*******************************************************************************************

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

    //*******************************************************************************************

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

    private boolean isReaderConnected()
    {
        if(reader != null && reader.isConnected())
            return true;
        else
        {
            Log.d(TAG, "No se ha conectado al lector");
            //responseHandlerInterface.SetMessage("No se ha conectado al lector o no hay lectores disponibles.");
            return false;
        }
    }

    //*******************************************************************************************

    public String onResume() {
        return connect();
    }

    public void onPause() {
        // disconnect(); // Comentado para mantener conexión entre pantallas
    }

    //*******************************************************************************************

    public void onDestroy() {
        //dispose();
    }

    //*******************************************************************************************

    private synchronized String connect() {
        if (reader != null) {
            Log.d(TAG, "conectar " + reader.getHostName());
            try {
                if (reader.isConnected()) {
                    responseHandlerInterface.SetMessage("Ya conectado a " + reader.getHostName());
                    return "Conectado";
                }
                
                if (!reader.isConnected()) {
                    // Establish connection to the RFID Reader
                    reader.connect();
                    ConfigureReader();
                    responseHandlerInterface.SetMessage("Conectado");
                    return "Conectado";
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                if(responseHandlerInterface != null)
                    responseHandlerInterface.SetMessage("Error InvalidUsage: " + e.getVendorMessage());
            } catch (OperationFailureException e) {
                e.printStackTrace();

                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();
                if(responseHandlerInterface != null)
                    responseHandlerInterface.SetMessage("Fallo Operación: " + e.getVendorMessage() + " " + des);
                return "Connection failed" + e.getVendorMessage() + " " + des;
            } catch (Throwable e) {
                e.printStackTrace();
                if(responseHandlerInterface != null)
                    responseHandlerInterface.SetMessage("Error inesperado al conectar: " + e.toString());
                return "Error: " + e.getMessage();
            }
        }
        return "";
    }

    //*******************************************************************************************

    private synchronized String disconnect(){
        Log.d(TAG, "Desconectado" + reader);
        try{
            if (reader != null){
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                //responseHandlerInterface.SetMessage("Desconectado " + reader);
            }
        }catch (InvalidUsageException ex){
            ex.printStackTrace();
            return ex.getMessage();
        }catch (OperationFailureException ex){
            ex.printStackTrace();
            return ex.getMessage();
        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return "desconectado";
    }

    //*******************************************************************************************

//    private synchronized void dispose(){
//        try{
//            if (readers != null){
//                reader = null;
//                readers.Dispose();
//                readers = null;
//            }
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//    }

    //*******************************************************************************************

    private ENUM_TRANSPORT currentTransport = ENUM_TRANSPORT.BLUETOOTH; // Default
    private boolean autoDetect = true;

    public void setAutoDetect(boolean enable) {
        this.autoDetect = enable;
    }

    public void setTransport(ENUM_TRANSPORT transport) {
        this.currentTransport = transport;
    }

    public void InitSDK()
    {
        Log.d(TAG, "InitSDK");
        if(responseHandlerInterface != null)
            responseHandlerInterface.SetMessage("Iniciando búsqueda de lectores (" + currentTransport.toString() + ")...");

        // Forzar limpieza si cambiamos de transporte o queremos re-escanear
        if (readers != null) {
            try {
                readers.Dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            readers = null;
        }

        new CreateInstanceTask().execute();
    }

    //*******************************************************************************************

    private class CreateInstanceTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids){
            Log.d(TAG, "CreateInstanceTask. AutoDetect: " + autoDetect + ", Transport: " + currentTransport);
            
            if (autoDetect) {
                // 1. Try Serial (eConnex) first
                try {
                    readers = new Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL);
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
                        currentTransport = ENUM_TRANSPORT.SERVICE_SERIAL;
                        Log.d(TAG, "Found reader on SERVICE_SERIAL");
                        return null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Serial check failed or no readers: " + e.getMessage());
                }

                // 2. Try USB
                try {
                    if (readers != null) {
                        try { readers.Dispose(); } catch(Exception e){}
                        readers = null;
                    }
                    readers = new Readers(context, ENUM_TRANSPORT.SERVICE_USB);
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
                        currentTransport = ENUM_TRANSPORT.SERVICE_USB;
                        Log.d(TAG, "Found reader on SERVICE_USB");
                        return null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "USB check failed or no readers: " + e.getMessage());
                }

                // 3. Try Bluetooth (Fallback)
                try {
                    if (readers != null) {
                        try { readers.Dispose(); } catch(Exception e){}
                        readers = null;
                    }
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    currentTransport = ENUM_TRANSPORT.BLUETOOTH;
                    Log.d(TAG, "Fallback to BLUETOOTH");
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Strict mode: Use currentTransport only
                try {
                    readers = new Readers(context, currentTransport);
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (responseHandlerInterface != null) {
                        responseHandlerInterface.SetMessage("Error crítico InitSDK: " + e.toString());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            new ConnectionTask().execute();
        }
    }

    //*******************************************************************************************

    private class ConnectionTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids){
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if(reader != null){
                return connect();
            }
            return "No se pudo encontrar o conectar el lector";
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if (result != null && !result.isEmpty() && !result.equals("Conectado")) {
                if(responseHandlerInterface != null)
                    responseHandlerInterface.SetMessage("Estado Conexión: " + result);
            }
        }
    }

    //*******************************************************************************************

    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        try {
            if (readers != null)
            {
                readers.attach( this);
                if (readers.GetAvailableRFIDReaderList() != null) {
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (availableRFIDReaderList.size() != 0) {
                        // if single reader is available then connect it
                        if (availableRFIDReaderList.size() == 1) {
                            readerDevice = availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                        } else {
                            // search reader specified by name
                            boolean found = false;
                            for (ReaderDevice device : availableRFIDReaderList) {
                                if (device.getName().equals(readername))
                                {
                                    readerDevice = device;
                                    reader = readerDevice.getRFIDReader();
                                    found = true;
                                    break;
                                }
                            }
                            // Fallback: if not found, use the first one
                            if (!found) {
                                readerDevice = availableRFIDReaderList.get(0);
                                reader = readerDevice.getRFIDReader();
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            if (responseHandlerInterface != null) {
                responseHandlerInterface.SetMessage("Error GetAvailableReader: " + e.toString());
            }
        }
    }

    //*******************************************************************************************

    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        new ConnectionTask().execute();
    }

    //*******************************************************************************************

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (reader != null && reader.getHostName() != null && readerDevice.getName().equals(reader.getHostName()))
            disconnect();
    }

    //*******************************************************************************************

    private void ConfigureReader() {
        Log.d(TAG,"ConfigureReader" + reader.getHostName());
        if (reader.isConnected()) {
            try{
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                
                // Configure default trigger (Handheld/Physical)
                configureTrigger(true);

                MAX_POWER =Integer.parseInt(Power);
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

            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public void configureTrigger(boolean isHandheld) {
        if (!isReaderConnected()) return;
        try {
            TriggerInfo triggerInfo = new TriggerInfo();
            if (isHandheld) {
                // Handheld Trigger (Physical Button)
                triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD);
                triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT);
                // triggerInfo.StopTrigger.setHandheldTriggerTimeout(0); // Removing causing error
            } else {
                // Immediate Trigger (Soft Button)
                triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
                triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            }
            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            reader.Config.setStopTrigger(triggerInfo.StopTrigger);
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
            Log.e(TAG, "Error configuring trigger: " + e.getMessage());
        }
    }

    //*******************************************************************************************

    public synchronized void performInventory(){
        if(!isReaderConnected())
            return;
        try{
            reader.Actions.Inventory.perform();
        }catch (InvalidUsageException ex){
            ex.printStackTrace();
        }catch (OperationFailureException ex){
            ex.printStackTrace();
        }
    }

    //*******************************************************************************************

    public synchronized void stopInventory(){
        if (!isReaderConnected())
            return;
        try{
            reader.Actions.Inventory.stop();
        }catch (InvalidUsageException ex){
            ex.printStackTrace();
        }catch (OperationFailureException ex){
            ex.printStackTrace();
        }
    }

    //*******************************************************************************************

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
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification 1: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (responseHandlerInterface != null)
                                responseHandlerInterface.handleTriggerPress(true);
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (responseHandlerInterface != null)
                                responseHandlerInterface.handleTriggerPress(false);
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }

    //*******************************************************************************************

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            if (responseHandlerInterface != null)
                responseHandlerInterface.handleTagdata(params[0]);
            return null;
        }
    }

    //*******************************************************************************************

    public synchronized void startRead(){
        // Switch to Immediate mode for soft-button read
        configureTrigger(false);
        performInventory();
    }

    public synchronized void stopRead(){
        stopInventory();
        // Switch back to Handheld mode for physical trigger
        // We use a small delay or just execute, but better to ensure inventory stopped
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            configureTrigger(true);
        }, 500);
    }

    //*******************************************************************************************

    public void LocateTag(String tagId){
        try {
            if(isReaderConnected()){
                reader.Actions.TagLocationing.Perform(tagId, null, null);
            }else{
                responseHandlerInterface.SetMessage("No hay lectores disponibles.");
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************************************

    public void StopLocateTag(){
        try {
            reader.Actions.TagLocationing.Stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************************************
}
