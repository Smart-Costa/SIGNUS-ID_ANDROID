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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IminReaderImpl — Integración RFID UHF para dispositivos iMin (modelo I24P01 / Lark 1).
 *
 * SDK: IminRfidSdk v1.0.3
 * Requiere: com.imin.peripherservice (app de sistema en dispositivos iMin)
 *
 * Flujo de inicialización:
 *   RFIDManager.getInstance()
 *     → connect(context)       [async bind al servicio]
 *     → getHelper()            [disponible tras bind exitoso]
 *     → registerReaderCall()   [callback de tags/errores]
 *
 * ═══ Modos de Lectura ═══
 *   SENCILLA  (isSingleReadMode = true):
 *     rfidHelper.tagInventoryAsyncFastStartReading()
 *     → callback onTag() → auto-stop al primer tag recibido
 *
 *   MÚLTIPLE  (isSingleReadMode = false):
 *     rfidHelper.tagInventoryRawStartReading()
 *     → callback onTag() continuo → stopInventory() para detener
 *
 * ═══ Comandos CMD (extendOperation) ═══
 *   CMD.SET_READ_WRITE_POWER      → potencia antena (5–33 dBm)
 *   CMD.SCANNER_START_DECODE (0x27) → iniciar decode barcode
 *   CMD.SCANNER_STOP_DECODE  (0x28) → detener decode barcode
 *   CMD.REAL_TIME_INVENTORY       → inventario tiempo real
 *   CMD.SET_TRIGGER_FUNCTION      → configurar gatillo físico
 *
 * ═══ Campos Tag via ParamCts ═══
 *   ParamCts.TAG_EPC        → tag_epc
 *   ParamCts.TAG_RSSI       → tag_rssi
 *   ParamCts.TAG_READ_COUNT → tag_read_count
 *   ParamCts.TAG_TIME       → tag_time
 *   ParamCts.TAG_FREQ       → tag_freq
 *   ParamCts.TAG_PC         → tag_pc
 *   ParamCts.TAG_CRC        → tag_crc
 *   ParamCts.ANT_ID         → ant_id
 *
 * ═══ Notas ═══
 *   - rfidManager.connect() es asíncrono; getHelper() puede ser null si el bind no completó.
 *   - Se usa un postDelayed(500ms) tras connect() antes de llamar getHelper() / registerReaderCall().
 *   - setPrintLog(true) activa los logs internos del SDK de iMin en Logcat.
 */
public class IminReaderImpl implements IReaderDevice {

    private static final String TAG = "IminReaderImpl";

    // ── Dependencias ──────────────────────────────────────────────────────────
    private Context context;
    private IReaderListener listener;
    private ConnectionType connectionType = ConnectionType.AUTO;

    // ── Estado ────────────────────────────────────────────────────────────────
    private volatile boolean isConnected = false;
    private volatile boolean isConnecting = false;
    private int currentPower = 30; // dBm por defecto
    /** true → lectura sencilla (auto-stop al primer tag)
     *  false → lectura múltiple continua */
    private volatile boolean isSingleReadMode = false;

    // ── SDK iMin ──────────────────────────────────────────────────────────────
    private RFIDManager rfidManager;
    private RFIDHelper rfidHelper;

    // ── Threading ─────────────────────────────────────────────────────────────
    private Handler uiHandler;
    private ExecutorService executor;

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Inicialización
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
        Log.d(TAG, "[CONFIG] ConnectionType establecido: " + type);
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Log.i(TAG, "╔══════════════════════════════════════════╗");
            Log.i(TAG, "║  iMin RFID SDK v1.0.3 — Inicializando   ║");
            Log.i(TAG, "╚══════════════════════════════════════════╝");
            Log.i(TAG, "[DEVICE] Modelo: " + android.os.Build.MODEL);
            Log.i(TAG, "[DEVICE] Android SDK: " + android.os.Build.VERSION.SDK_INT);
            Log.i(TAG, "[CONFIG] ConnectionType: " + connectionType);

            // 1. Verificar servicio iMin instalado
            if (!isServiceInstalled("com.imin.peripherservice")) {
                String msg = "CRÍTICO: com.imin.peripherservice NO encontrado. " +
                             "Este dispositivo no soporta iMin RFID.";
                Log.e(TAG, "[INIT] " + msg);
                notifyError(msg);
                return;
            }
            Log.i(TAG, "[INIT] Servicio com.imin.peripherservice: FOUND ✓");

            // 2. Obtener instancia RFIDManager
            try {
                rfidManager = RFIDManager.getInstance();
                if (rfidManager == null) {
                    Log.e(TAG, "[INIT] RFIDManager.getInstance() retornó null — SDK no disponible.");
                    notifyError("RFIDManager null — SDK no cargado correctamente.");
                    return;
                }
                Log.d(TAG, "[INIT] RFIDManager.getInstance() → OK");

                // 3. Activar logs internos del SDK
                try {
                    rfidManager.setPrintLog(true);
                    Log.d(TAG, "[INIT] rfidManager.setPrintLog(true) → Logs SDK activados");
                } catch (Exception e) {
                    Log.w(TAG, "[INIT] setPrintLog no disponible en esta versión de SDK: " + e.getMessage());
                }

                // 4. Bind al servicio iMin (async)
                Log.d(TAG, "[INIT] Llamando rfidManager.connect(context)...");
                rfidManager.connect(context);
                Log.d(TAG, "[INIT] rfidManager.connect() invocado — bind asíncrono en curso");

            } catch (Throwable e) {
                Log.e(TAG, "[INIT] Excepción crítica inicializando SDK iMin", e);
                notifyError("Excepción SDK iMin: " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Conexión
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean connect() {
        Log.d(TAG, "[CONNECT] Intentando conectar u obtener RFIDHelper...");

        if (!isServiceInstalled("com.imin.peripherservice")) {
            String msg = "com.imin.peripherservice no instalado — RFID no disponible.";
            Log.e(TAG, "[CONNECT] " + msg);
            notifyError(msg);
            return false;
        }

        if (isConnected || isConnecting) {
            Log.d(TAG, "[CONNECT] Ya conectado o en proceso de conexión. Ignorando.");
            return true;
        }
        isConnecting = true;

        executor.execute(() -> {
            try {
                if (rfidManager == null) {
                    Log.w(TAG, "[CONNECT] rfidManager era null, inicializando...");
                    rfidManager = RFIDManager.getInstance();
                    if (rfidManager == null) {
                        Log.e(TAG, "[CONNECT] RFIDManager.getInstance() retornó null.");
                        notifyError("RFIDManager null en connect().");
                        isConnecting = false;
                        return;
                    }
                    rfidManager.connect(context);
                }

                // Polling: esperar hasta que el bind de iMin complete de forma asíncrona.
                int retries = 10; // Hasta 5 segundos (10 * 500ms)
                while (retries > 0) {
                    rfidHelper = rfidManager.getHelper();
                    if (rfidHelper != null) {
                        try {
                            // Validar que el proxy interno no sea nulo invocando un método
                            rfidHelper.getScanModel();
                            break; // ¡Está listo!
                        } catch (Exception e) {
                            Log.w(TAG, "[CONNECT] Proxy aún no listo (esperando bind)... Intentos: " + (retries-1));
                        }
                    } else {
                        Log.w(TAG, "[CONNECT] rfidHelper es null... Intentos: " + (retries-1));
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    retries--;
                }

                if (rfidHelper == null) {
                    Log.e(TAG, "[CONNECT] No se pudo obtener rfidHelper tras varios intentos.");
                    notifyError("Timeout al conectar con servicio RFID iMin.");
                    isConnecting = false;
                    return;
                }

                // Última validación
                try {
                    int scanModel = rfidHelper.getScanModel();
                    Log.i(TAG, "[CONNECT] getScanModel() → " + scanModel + " (0=normal, ver docs SDK)");
                } catch (Exception e) {
                    Log.w(TAG, "[CONNECT] getScanModel() final falló: " + e.getMessage());
                }

                // Registrar callbacks
                registerReaderCall();

                isConnected = true;
                isConnecting = false;
                Log.i(TAG, "[CONNECT] Estado: CONECTADO ✓ (RFIDHelper listo)");
                Log.i(TAG, "[CONNECT] Dispositivo: " + getDeviceName());

                if (listener != null) {
                    uiHandler.post(() -> listener.onConnected(getDeviceName()));
                }

            } catch (Exception e) {
                Log.e(TAG, "[CONNECT] Excepción durante conexión", e);
                notifyError("Error al conectar: " + e.getMessage());
                isConnecting = false;
            }
        });
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Lectura (Inventario)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean startInventory() {
        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "[INVENTORY] No conectado o RFIDHelper null — no se puede iniciar lectura.");
            return false;
        }

        isSingleReadMode = false;
        
        executor.execute(() -> {
            try {
                Log.i(TAG, "[INVENTORY] ══ Modo MÚLTIPLE iniciando...");
                Log.d(TAG, "[INVENTORY] Llamando rfidHelper.tagInventoryRawStartReading()");
                rfidHelper.tagInventoryRawStartReading();
                Log.i(TAG, "[INVENTORY] tagInventoryRawStartReading() → OK ✓");
            } catch (Exception e) {
                Log.e(TAG, "[INVENTORY] Error en tagInventoryRawStartReading(): " + e.getMessage(), e);
                handleBinderError(e);
            }
        });
        return true;
    }

    /**
     * Inicia lectura en modo SENCILLA (auto-stop al primer tag).
     */
    public boolean startSingleRead() {
        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "[SINGLE] No conectado o RFIDHelper null — no se puede iniciar lectura.");
            return false;
        }

        isSingleReadMode = true;
        
        executor.execute(() -> {
            try {
                Log.i(TAG, "[SINGLE] ══ Modo SENCILLA iniciando...");
                Log.d(TAG, "[SINGLE] Llamando rfidHelper.tagInventoryRawStartReading()");
                rfidHelper.tagInventoryRawStartReading();
                Log.i(TAG, "[SINGLE] tagInventoryRawStartReading() → OK ✓");
                
                // Timeout de 3 segundos para detener la lectura si no se encuentra ningún tag
                uiHandler.postDelayed(() -> {
                    if (isSingleReadMode) {
                        Log.i(TAG, "[SINGLE] Timeout de lectura sencilla alcanzado (3s). Deteniendo...");
                        stopInventory();
                    }
                }, 3000);
            } catch (Exception e) {
                Log.e(TAG, "[SINGLE] Error en tagInventoryRawStartReading(): " + e.getMessage(), e);
                handleBinderError(e);
            }
        });
        return true;
    }

    @Override
    public boolean stopInventory() {
        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "[STOP] No conectado — stopInventory() ignorado.");
            return false;
        }
        executor.execute(() -> {
            Log.d(TAG, "[STOP] Llamando rfidHelper.tagInventoryRawStopReading()...");
            try {
                rfidHelper.tagInventoryRawStopReading();
                Log.i(TAG, "[STOP] tagInventoryRawStopReading() → OK ✓");
            } catch (Exception e) {
                Log.e(TAG, "[STOP] Error en stopInventory()", e);
                handleBinderError(e);
            }
            
            // También intentar detener el FastReading por si acaso el SDK lo mantuvo vivo
            try {
                rfidHelper.tagInventoryAsyncFastStopReading();
            } catch (Exception ignored) { }
        });
        
        isSingleReadMode = false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Callback del SDK — ReaderCall
    // ─────────────────────────────────────────────────────────────────────────

    private void registerReaderCall() {
        if (rfidHelper == null) return;
        Log.d(TAG, "[CALLBACK] Registrando ReaderCall (callback de tags/errores/success)...");

        rfidHelper.registerReaderCall(new ReaderCall() {

            /**
             * onSuccess — Invocado cuando un comando CMD se ejecutó exitosamente
             * (no necesariamente cuando se lee un tag).
             *
             * @param cmd          Código del comando ejecutado (ver CMD.*)
             * @param dataParameter Datos de resultado; puede ser null
             */
            @Override
            public void onSuccess(byte cmd, DataParameter dataParameter) {
                Log.d(TAG, "[onSuccess] CMD=0x" + String.format("%02X", cmd) +
                        " (" + getCmdName(cmd) + ")");
                if (dataParameter != null) {
                    String extra = dataParameter.toString();
                    if (extra != null && !extra.isEmpty()) {
                        Log.v(TAG, "[onSuccess] DataParameter: " + extra);
                    }
                }
            }

            /**
             * onTag — Invocado por cada tag RFID detectado durante un inventario.
             *
             * @param cmd           Código del comando que generó el tag (ej: CMD.INVENTORY)
             * @param state         Estado de la operación (0=OK normal)
             * @param dataParameter Campos del tag; usar ParamCts.TAG_* para extraer
             */
            @Override
            public void onTag(byte cmd, byte state, DataParameter dataParameter) {
                Log.v(TAG, "[onTag] CMD=0x" + String.format("%02X", cmd) +
                        " state=" + (state & 0xFF));

                if (dataParameter == null) {
                    Log.w(TAG, "[onTag] DataParameter es null — tag ignorado.");
                    return;
                }

                // ── Extraer campos del tag via ParamCts ──
                String epc       = dataParameter.getString(ParamCts.TAG_EPC);
                String rssiStr   = dataParameter.getString(ParamCts.TAG_RSSI);
                String readCount = dataParameter.getString(ParamCts.TAG_READ_COUNT);
                String tagTime   = dataParameter.getString(ParamCts.TAG_TIME);
                String tagFreq   = dataParameter.getString(ParamCts.TAG_FREQ);
                String tagPc     = dataParameter.getString(ParamCts.TAG_PC);
                String tagCrc    = dataParameter.getString(ParamCts.TAG_CRC);
                String antId     = dataParameter.getString(ParamCts.ANT_ID);

                Log.i(TAG, "[TAG] ══════════════════════════════════");
                Log.i(TAG, "[TAG] EPC        : " + epc);
                Log.i(TAG, "[TAG] RSSI       : " + rssiStr + " dBm");
                Log.i(TAG, "[TAG] ReadCount  : " + readCount);
                Log.i(TAG, "[TAG] Timestamp  : " + tagTime);
                Log.i(TAG, "[TAG] Frecuencia : " + tagFreq);
                Log.i(TAG, "[TAG] PC         : " + tagPc);
                Log.i(TAG, "[TAG] CRC        : " + tagCrc);
                Log.i(TAG, "[TAG] Antena ID  : " + antId);
                Log.i(TAG, "[TAG] Modo       : " + (isSingleReadMode ? "SENCILLA" : "MÚLTIPLE"));
                Log.i(TAG, "[TAG] ══════════════════════════════════");

                if (epc == null || epc.isEmpty()) {
                    Log.w(TAG, "[TAG] EPC vacío/null — tag descartado.");
                    return;
                }

                // Parse RSSI
                short rssi = 0;
                if (rssiStr != null) {
                    try {
                        rssi = Short.parseShort(rssiStr);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "[TAG] No se pudo parsear RSSI: '" + rssiStr + "'");
                    }
                }

                final ReaderTag tag = new ReaderTag(epc, rssi);
                notifyTagsRead(new ReaderTag[]{tag});

                // ── Lectura Sencilla: detener al primer tag ──
                if (isSingleReadMode) {
                    Log.i(TAG, "[TAG] Modo SENCILLA → auto-stop después de primer tag.");
                    isSingleReadMode = false;
                    uiHandler.post(() -> stopInventory());
                }
            }

            /**
             * onFiled — Invocado cuando un comando CMD falló en el lector.
             *
             * @param cmd       Código del comando que falló
             * @param errorCode Código de error del lector
             * @param msg       Descripción del error
             */
            @Override
            public void onFiled(byte cmd, byte errorCode, String msg) {
                Log.w(TAG, "[onFiled] CMD=0x" + String.format("%02X", cmd) +
                        " (" + getCmdName(cmd) + ")" +
                        " | ErrorCode=0x" + String.format("%02X", errorCode) +
                        " | Msg: " + msg);
            }
        });

        Log.i(TAG, "[CALLBACK] ReaderCall registrado exitosamente ✓");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Potencia
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setPower(int power) {
        Log.d(TAG, "[POWER] setPower() llamado con valor: " + power);
        this.currentPower = power;

        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "[POWER] No conectado — setPower() omitido. Valor guardado: " + power);
            return;
        }

        executor.execute(() -> {
            try {
                // Normalizar: si viene en formato Zebra (ej. 270 = 27.0 dBm) → dividir entre 10
                int dBm = power;
                if (dBm > 33) {
                    dBm = dBm / 10;
                    Log.d(TAG, "[POWER] Valor normalizado (Zebra format): " + power + " → " + dBm + " dBm");
                }

                // Rango SDK iMin: 5–33 dBm
                if (dBm < 5)  dBm = 5;
                if (dBm > 33) dBm = 33;
                Log.i(TAG, "[POWER] Potencia final a aplicar: " + dBm + " dBm (rango válido: 5–33 dBm)");

                ReadWritePower rwPower = new ReadWritePower();
                rwPower.readPower  = dBm;
                rwPower.writePower = dBm;
                String configJson = new Gson().toJson(rwPower);

                Log.d(TAG, "[POWER] CMD=0x" + String.format("%02X", CMD.SET_READ_WRITE_POWER) +
                        " (SET_READ_WRITE_POWER) JSON: " + configJson);
                rfidHelper.extendOperation(CMD.SET_READ_WRITE_POWER, configJson);
                Log.i(TAG, "[POWER] extendOperation(SET_READ_WRITE_POWER) → OK ✓");

            } catch (Exception e) {
                Log.e(TAG, "[POWER] Error configurando potencia", e);
                notifyError("Error al configurar potencia: " + e.getMessage());
                handleBinderError(e);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Escritura de tag
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean writeTag(String sourceEpc, String newEpc, String password) {
        Log.d(TAG, "[WRITE] writeTag() sourceEpc=" + sourceEpc + " newEpc=" + newEpc);
        if (!isConnected || rfidHelper == null) {
            Log.w(TAG, "[WRITE] No conectado — writeTag() abortado.");
            return false;
        }
        // TODO: implementar rfidHelper.writeTag(bank, address, length, password, data)
        // CMD.WRITE_TAG = 0x03
        Log.w(TAG, "[WRITE] writeTag() no implementado. Requiere params de banco/dirección según SDK.");
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Desconexión / Ciclo de vida
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean disconnect() {
        Log.d(TAG, "[DISCONNECT] Desconectando iMin Reader...");
        if (executor != null && !executor.isShutdown()) {
            executor.execute(() -> {
                try {
                    if (rfidHelper != null && isServiceInstalled("com.imin.peripherservice")) {
                        Log.d(TAG, "[DISCONNECT] Llamando rfidHelper.unregisterReaderCall()...");
                        try {
                            rfidHelper.unregisterReaderCall();
                            Log.i(TAG, "[DISCONNECT] unregisterReaderCall() → OK ✓");
                        } catch (Exception e) {
                            Log.w(TAG, "[DISCONNECT] Error en unregisterReaderCall(): " + e.getMessage());
                        }

                        Log.d(TAG, "[DISCONNECT] Llamando rfidHelper.tagInventoryRawStopReading()...");
                        try {
                            rfidHelper.tagInventoryRawStopReading();
                            Log.i(TAG, "[DISCONNECT] tagInventoryRawStopReading() → OK ✓");
                        } catch (Exception e) {
                            Log.w(TAG, "[DISCONNECT] Error en tagInventoryRawStopReading(): " + e.getMessage());
                        }
                    }

                    if (rfidManager != null) {
                        Log.d(TAG, "[DISCONNECT] Llamando rfidManager.disconnect()...");
                        rfidManager.disconnect();
                        Log.i(TAG, "[DISCONNECT] rfidManager.disconnect() → OK ✓");
                    }

                } catch (Exception e) {
                    Log.e(TAG, "[DISCONNECT] Excepción durante desconexión", e);
                }

                rfidHelper = null;
                isConnected = false;
                isSingleReadMode = false;

                Log.i(TAG, "[DISCONNECT] Estado: DESCONECTADO");
                if (listener != null) {
                    uiHandler.post(() -> listener.onDisconnected());
                }
            });
        }
        return true;
    }

    @Override
    public void dispose() {
        Log.d(TAG, "[DISPOSE] Liberando recursos iMin...");
        disconnect();
        if (executor != null) {
            executor.shutdown();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IReaderDevice — Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean isConnected() { return isConnected; }

    @Override
    public void setListener(IReaderListener listener) {
        this.listener = listener;
    }

    @Override
    public String getDeviceName() { return "iMin Lark 1 I24P01 (RFID UHF)"; }

    // ─────────────────────────────────────────────────────────────────────────
    // Métodos de Diagnóstico Públicos
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isServiceAvailable() {
        return isServiceInstalled("com.imin.peripherservice");
    }

    public boolean isServiceInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            Log.v(TAG, "[PKG] " + packageName + " → INSTALADO ✓");
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.v(TAG, "[PKG] " + packageName + " → NO INSTALADO");
            return false;
        }
    }

    public String getDiagnosticInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DIAGNÓSTICO RFID iMin ===\n");
        sb.append("Modelo Android   : ").append(android.os.Build.MODEL).append("\n");
        sb.append("SDK Android      : ").append(android.os.Build.VERSION.SDK_INT).append("\n");
        sb.append("SDK iMin RFID    : v1.0.3\n");
        sb.append("com.imin.peripherservice: ")
          .append(isServiceInstalled("com.imin.peripherservice") ? "✅ INSTALADO" : "❌ AUSENTE").append("\n");
        sb.append("RFIDManager      : ").append(rfidManager != null ? "✅ OK" : "❌ NULL").append("\n");
        sb.append("RFIDHelper       : ").append(rfidHelper != null ? "✅ OK" : "❌ NULL").append("\n");
        sb.append("Conectado        : ").append(isConnected ? "✅ SÍ" : "❌ NO").append("\n");
        sb.append("Modo lectura     : ").append(isSingleReadMode ? "SENCILLA" : "MÚLTIPLE/IDLE").append("\n");
        sb.append("Potencia actual  : ").append(currentPower).append(" dBm\n");

        if (isConnected && rfidHelper != null) {
            try {
                int scanModel = rfidHelper.getScanModel();
                sb.append("Scan Model       : ").append(scanModel)
                  .append(" (ver docs SDK)\n");
            } catch (Exception e) {
                sb.append("Scan Model       : N/A (").append(e.getMessage()).append(")\n");
            }
        }
        return sb.toString();
    }

    private void handleBinderError(Exception e) {
        if (e.toString().contains("DeadObjectException") || e.toString().contains("RemoteException") || e.toString().contains("IllegalStateException")) {
            Log.e(TAG, "[RECOVERY] Binder died o estado ilegal. Intentando reconectar en 1 segundo...");
            isConnected = false;
            rfidHelper = null;
            uiHandler.postDelayed(() -> {
                Log.i(TAG, "[RECOVERY] Re-ejecutando rfidManager.connect()...");
                try {
                    if (rfidManager != null) rfidManager.connect(context);
                    uiHandler.postDelayed(this::connect, 500);
                } catch (Exception ex) {
                    Log.e(TAG, "[RECOVERY] Fallo crítico intentando reconectar: " + ex.getMessage());
                }
            }, 1000);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private void notifyTagsRead(ReaderTag[] tags) {
        if (listener != null) {
            uiHandler.post(() -> listener.onTagRead(java.util.Arrays.asList(tags)));
        }
    }

    private void notifyError(String msg) {
        Log.e(TAG, "[ERROR] " + msg);
        if (listener != null) {
            uiHandler.post(() -> listener.onConnectionError(msg));
        }
    }

    /**
     * Devuelve nombre legible de un comando CMD para logs.
     */
    private String getCmdName(byte cmd) {
        switch (cmd & 0xFF) {
            case 0x00: return "INVENTORY";
            case 0x01: return "REAL_TIME_INVENTORY";
            case 0x02: return "FAST_SWITCH_ANT_INVENTORY";
            case 0x03: return "WRITE_TAG";
            case 0x04: return "READ_TAG";
            case 0x05: return "LOCK_TAG";
            case 0x06: return "KILL_TAG";
            case 0x07: return "SET_ACCESS_EPC_MATCH";
            case 0x08: return "GET_ACCESS_EPC_MATCH";
            case 0x09: return "CUSTOMIZED_SESSION_TARGET_INVENTORY";
            case 0x0A: return "SET_IMPINJ_FAST_TID";
            case 0x0B: return "SET_AND_SAVE_IMPINJ_FAST_TID";
            case 0x0C: return "GET_IMPINJ_FAST_TID";
            case 0x19: return "SET_READ_WRITE_POWER";
            case 0x26: return "SET_TRIGGER_FUNCTION";
            case 0x27: return "SCANNER_START_DECODE";
            case 0x28: return "SCANNER_STOP_DECODE";
            case 0x2C: return "GET_INVENTORY_BUFFER";
            case 0x2D: return "GET_AND_RESET_INVENTORY_BUFFER";
            case 0x2E: return "GET_INVENTORY_BUFFER_TAG_COUNT";
            case 0x2F: return "RESET_INVENTORY_BUFFER";
            case 0x30: return "CLEAR_TAG";
            case 0x31: return "CLEAR_TAG_FILTER";
            default:   return "UNKNOWN_CMD";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Clase interna: Power Config (para Gson)
    // ─────────────────────────────────────────────────────────────────────────

    private static class ReadWritePower {
        public int readPower;
        public int writePower;
    }
}
