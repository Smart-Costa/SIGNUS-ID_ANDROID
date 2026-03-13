package com.example.diverscan.activeid.DeviceInterface.Impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.IReaderDevice;
import com.example.diverscan.activeid.DeviceInterface.IReaderListener;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

// ── Importaciones del SDK TSL ASCII 2 (Rfid.AsciiProtocol-4.0.1-release.aar) ────────────────
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.AbortCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.device.ObservableReaderList;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.device.TransportType;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.Observable;
// ─────────────────────────────────────────────────────────────────────────────────────────────

import java.util.ArrayList;
import java.util.List;

/**
 * DatalogicReaderImpl — Implementación del patrón Factory para sleds Datalogic
 * RFID.
 *
 * Los sleds Datalogic (2128P, 3138 UHF) utilizan el protocolo TSL ASCII 2 de
 * Technology Solutions (UK) Ltd.
 *
 * SDK: Rfid.AsciiProtocol-4.0.1-release.aar
 * Para agregar al proyecto:
 * 1. Copiar el .aar en SIGNUS-ID_ANDROID/app/libs/
 * 2. En build.gradle (app): implementation fileTree(dir: 'libs', include:
 * ['*.jar','*.aar'])
 *
 * Ciclo de vida de la conexión:
 * initialize() → setEnabled(true) → connect vía ReaderManager →
 * startInventory() → stopInventory() → disconnect() → dispose()
 */
public class DatalogicReaderImpl implements IReaderDevice {

    private static final String TAG = "DatalogicReaderImpl";

    private Context context;
    private IReaderListener listener;
    private Handler uiHandler;
    private ConnectionType connectionType = ConnectionType.AUTO;

    private boolean isConnected = false;
    private String connectedDeviceName = "Datalogic Sled";

    // ── TSL SDK core objects ──────────────────────────────────────────────────
    private Reader mReader = null;

    /** The responder that listens for incoming RFID transponder data */
    private InventoryCommand mInventoryResponder;

    /** The command used to start/stop/configure inventory */
    private InventoryCommand mInventoryCommand;

    /** Barcode capture responder */
    private BarcodeCommand mBarcodeResponder;

    private boolean mContinuousScanEnabled = false;

    // ── Observer for connection state events ─────────────────────────────────
    private final Observable.Observer<String> mConnectionStateObserver = (observable, reason) -> {
        Log.d(TAG, "TSL state changed: " + reason +
                " | connected=" + getCommander().isConnected());

        ConnectionState state = getCommander().getConnectionState();

        if (state == ConnectionState.CONNECTED) {
            isConnected = true;
            connectedDeviceName = getCommander().getConnectedDeviceName();
            Log.i(TAG, "TSL Sled connected: " + connectedDeviceName);
            notifyConnected(connectedDeviceName);
        } else if (state == ConnectionState.DISCONNECTED) {
            isConnected = false;
            notifyDisconnected();
        } else if (state == ConnectionState.LOST) {
            isConnected = false;
            mReader = null;
            notifyError("Conexión perdida con el sled Datalogic.");
        }
    };

    // ── ReaderManager observers ───────────────────────────────────────────────
    private final Observable.Observer<Reader> mAddedObserver = (observable, reader) -> {
        Log.d(TAG, "Reader added: " + reader.getDisplayName());
        autoSelectReader(true);
    };

    private final Observable.Observer<Reader> mUpdatedObserver = (observable, reader) -> {
        if (reader == mReader && !reader.isConnected()) {
            mReader = null;
            getCommander().setReader(null);
        } else {
            autoSelectReader(true);
        }
    };

    private final Observable.Observer<Reader> mRemovedObserver = (observable, reader) -> {
        if (reader == mReader) {
            mReader = null;
            getCommander().setReader(null);
        }
    };

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
    }

    /**
     * Debe llamarse desde el Activity/Fragment en onCreate().
     * Inicializa el AsciiCommander y el ReaderManager del SDK TSL.
     */
    @Override
    public void initialize(Context context) {
        this.context = context;
        this.uiHandler = new Handler(Looper.getMainLooper());

        Log.i(TAG, "Initializing TSL SDK — ConnType: " + connectionType);

        // Paso 1: Crear la instancia compartida del AsciiCommander
        AsciiCommander.createSharedInstance(context.getApplicationContext());

        AsciiCommander commander = getCommander();

        // Paso 2: Limpiar responders anteriores y añadir log + synchronous
        commander.clearResponders();
        commander.addResponder(new LoggerResponder()); // debe ser el primero
        commander.addSynchronousResponder();

        // Paso 3: Configurar el ReaderManager (gestiona BT, USB y ePop-Loq)
        ReaderManager.create(context.getApplicationContext());
        ReaderManager.sharedInstance().setBleSupportEnabled(false); // BLE opcional

        // Paso 4: Observar cambios en la lista de lectores
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().addObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().addObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().addObserver(mRemovedObserver);

        // Paso 5: Observar cambios de estado del AsciiCommander
        commander.stateChangedEvent().addObserver(mConnectionStateObserver);

        // Paso 6: Configurar comandos de inventario
        setupInventoryCommands(commander);

        Log.i(TAG, "TSL SDK initialized. Enabling responders...");
        enableResponders();

        // Paso 7: Intentar conectar automáticamente
        connect();
    }

    /** Configura los comandos de inventario y barcode del SDK TSL */
    private void setupInventoryCommands(AsciiCommander commander) {
        // Comando que se usa para ejecutar el inventario
        mInventoryCommand = new InventoryCommand();
        mInventoryCommand.setResetParameters(TriState.YES);
        mInventoryCommand.setUseAlert(TriState.NO);
        mInventoryCommand.setIncludeTransponderRssi(TriState.YES);

        // Responder que captura las respuestas RFID entrantes
        mInventoryResponder = new InventoryCommand();
        mInventoryResponder.setCaptureNonLibraryResponses(true);
        mInventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                if (transponder.getEpc() != null) {
                    String epc = transponder.getEpc();
                    int rssi = (transponder.getRssi() != null) ? transponder.getRssi() : 0;
                    Log.d(TAG, "Tag leído: EPC=" + epc + " RSSI=" + rssi);
                    notifyTagRead(epc, (short) rssi);
                }
                // Si hay más inventario activo, iniciar otro ciclo
                if (!moreAvailable && mContinuousScanEnabled) {
                    commander.executeCommand(mInventoryCommand);
                }
            }
        });

        // Responder de barcode (captura lecturas del lector de código de barras del
        // sled)
        mBarcodeResponder = new BarcodeCommand();
        mBarcodeResponder.setCaptureNonLibraryResponses(true);
        mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
        mBarcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate() {
            @Override
            public void barcodeReceived(String barcode) {
                Log.d(TAG, "Barcode leído: " + barcode);
                // Notificar el barcode como un tag especial
                notifyTagRead("BC:" + barcode, (short) 0);
            }
        });
    }

    /** Registra los responders en el AsciiCommander */
    private void enableResponders() {
        AsciiCommander commander = getCommander();
        commander.addResponder(mInventoryResponder);
        commander.addResponder(mBarcodeResponder);
    }

    /** Desregistra los responders del AsciiCommander */
    private void disableResponders() {
        AsciiCommander commander = getCommander();
        if (mInventoryResponder != null)
            commander.removeResponder(mInventoryResponder);
        if (mBarcodeResponder != null)
            commander.removeResponder(mBarcodeResponder);
    }

    /**
     * Selecciona automáticamente el primer lector disponible en el ReaderManager.
     * Prioriza USB/ePop-Loq sobre Bluetooth cuando connectionType == AUTO.
     */
    private void autoSelectReader(boolean attemptReconnect) {
        ObservableReaderList readerList = ReaderManager.sharedInstance().getReaderList();
        Reader usbReader = null;

        // Buscar lector USB primero
        for (Reader reader : readerList.list()) {
            if (reader.hasTransportOfType(TransportType.USB)) {
                usbReader = reader;
                break;
            }
        }

        // Estrategia según connectionType
        if (connectionType == ConnectionType.SERIAL_USB && usbReader != null) {
            mReader = usbReader;
        } else if (mReader == null) {
            // Tomar el primero disponible en la lista (BT o USB)
            if (usbReader != null) {
                mReader = usbReader;
            } else if (!readerList.list().isEmpty()) {
                mReader = readerList.list().get(0);
            }
        }

        if (mReader != null) {
            getCommander().setReader(mReader);

            // Reconectar si está desconectado
            if (attemptReconnect && !mReader.isConnecting()) {
                boolean isDisconnected = mReader.getActiveTransport() == null
                        || mReader.getActiveTransport().connectionStatus().value() == ConnectionState.DISCONNECTED;
                if (isDisconnected) {
                    Log.d(TAG, "Auto-connecting to: " + mReader.getDisplayName());
                    mReader.connect();
                }
            }
        } else {
            Log.d(TAG, "No TSL readers found in ReaderManager. Empareja el sled por Bluetooth.");
        }
    }

    @Override
    public synchronized boolean connect() {
        Log.d(TAG, "connect() called — connectionType=" + connectionType);

        // Actualizar la lista del ReaderManager para detectar nuevos lectores
        ReaderManager.sharedInstance().updateList();
        autoSelectReader(true);

        return mReader != null;
    }

    @Override
    public synchronized boolean disconnect() {
        Log.d(TAG, "disconnect() called");
        mContinuousScanEnabled = false;

        if (mReader != null) {
            mReader.disconnect();
            mReader = null;
        }

        isConnected = false;
        return true;
    }

    @Override
    public boolean isConnected() {
        return isConnected && getCommander().isConnected();
    }

    /**
     * Inicia el inventario RFID continuo.
     * Equivalente a InventoryModel.scanStart() del SDK TSL.
     */
    @Override
    public boolean startInventory() {
        AsciiCommander commander = getCommander();
        if (!commander.isConnected()) {
            Log.w(TAG, "startInventory: Commander no conectado.");
            return false;
        }
        Log.d(TAG, "Iniciando inventario continuo...");
        mContinuousScanEnabled = true;
        mInventoryCommand.setTakeNoAction(TriState.NO);
        commander.executeCommand(mInventoryCommand);
        return true;
    }

    /**
     * Detiene el inventario RFID.
     * Equivalente a InventoryModel.scanStop() del SDK TSL.
     * Envía AbortCommand para cancelar cualquier inventario en curso.
     */
    @Override
    public boolean stopInventory() {
        mContinuousScanEnabled = false;
        mInventoryCommand.setTakeNoAction(TriState.YES);

        AsciiCommander commander = getCommander();
        if (commander.isConnected()) {
            Log.d(TAG, "Deteniendo inventario (AbortCommand)...");
            commander.executeCommand(new AbortCommand());
        }
        return true;
    }

    /**
     * Configura la potencia de la antena.
     * El rango TSL típico es 0–29 dBm para Bluetooth, hasta 33 dBm para USB.
     * El AsciiCommander reporta los límites reales via getDeviceProperties().
     */
    @Override
    public void setPower(int power) {
        // Normalizar: si viene en formato Zebra (e.g. 270 → 27 dBm)
        int normalizedPower = (power > 33) ? power / 10 : power;
        Log.d(TAG, "setPower: " + normalizedPower + " dBm (raw input: " + power + ")");

        if (isConnected()) {
            // Clampear al rango real del dispositivo conectado
            int minPower = getCommander().getDeviceProperties().getMinimumCarrierPower();
            int maxPower = getCommander().getDeviceProperties().getMaximumCarrierPower();
            normalizedPower = Math.max(minPower, Math.min(maxPower, normalizedPower));
        }

        mInventoryCommand.setOutputPower(normalizedPower);

        // Aplicar configuración al lector si está conectado
        if (isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(mInventoryCommand);
        }
    }

    @Override
    public void setListener(IReaderListener listener) {
        this.listener = listener;
    }

    @Override
    public String getDeviceName() {
        return connectedDeviceName;
    }

    /**
     * Escritura de EPC en un tag.
     * Requiere WriteTransponderCommand del SDK TSL (no incluido en sample
     * Inventory,
     * ver Sample Code/ReadWrite).
     */
    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
        if (!isConnected())
            return false;
        Log.d(TAG, "writeTag: " + sourceEpc + " → " + newEpc);

        // TODO: Implementar con ReadWrite sample del SDK TSL:
        // WriteTransponderCommand writeCmd =
        // WriteTransponderCommand.synchronousCommand();
        // writeCmd.setSelectByEPC(sourceEpc);
        // writeCmd.setEPC(newEpc);
        // writeCmd.setAccessPassword(password);
        // getCommander().executeCommand(writeCmd);
        // return writeCmd.isSuccessful();

        Log.w(TAG, "writeTag: No implementado aún. Ver Sample Code/ReadWrite del SDK TSL.");
        return false;
    }

    @Override
    public void dispose() {
        Log.d(TAG, "dispose()");

        // Detener scan si está activo
        stopInventory();

        // Desregistrar responders
        disableResponders();

        // Desconectar lector
        disconnect();

        // Limpiar observers del ReaderManager
        if (ReaderManager.sharedInstance() != null) {
            ReaderManager.sharedInstance().getReaderList().readerAddedEvent().removeObserver(mAddedObserver);
            ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().removeObserver(mUpdatedObserver);
            ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().removeObserver(mRemovedObserver);
        }

        // Desregistrar observer de estado
        getCommander().stateChangedEvent().removeObserver(mConnectionStateObserver);
    }

    // ── Helper accessor ───────────────────────────────────────────────────────
    private AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

    // ── Helpers para notificar al listener en el hilo principal ──────────────

    private void notifyTagRead(String epc, short rssi) {
        if (listener == null)
            return;
        List<ReaderTag> tags = new ArrayList<>();
        tags.add(new ReaderTag(epc, rssi));
        uiHandler.post(() -> listener.onTagRead(tags));
    }

    private void notifyConnected(String name) {
        if (listener != null)
            uiHandler.post(() -> listener.onConnected(name));
    }

    private void notifyDisconnected() {
        if (listener != null)
            uiHandler.post(() -> listener.onDisconnected());
    }

    private void notifyError(String msg) {
        if (listener != null)
            uiHandler.post(() -> listener.onConnectionError(msg));
    }
}
