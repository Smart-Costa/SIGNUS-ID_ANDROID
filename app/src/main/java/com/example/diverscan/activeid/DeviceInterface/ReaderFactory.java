package com.example.diverscan.activeid.DeviceInterface;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.example.diverscan.activeid.ConfiguracionesGeneral.SharedPreferencesGetSet;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.Impl.DatalogicReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.ZebraReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminScannerImpl;

public class ReaderFactory {
    private static final String TAG = "ReaderFactory";

    public static IReaderDevice createReader(ReaderType type, ConnectionType connectionType, Context context,
            IReaderListener listener) {
        IReaderDevice device = null;
        switch (type) {
            case ZEBRA:
                device = new ZebraReaderImpl();
                break;
            case DATALOGIC:
                device = new DatalogicReaderImpl();
                break;
            case IMIN:
                device = new IminReaderImpl();
                break;
            case IMIN_SCANNER:
                device = new IminScannerImpl();
                break;
            default:
                throw new IllegalArgumentException("Unknown reader type: " + type);
        }

        if (device != null) {
            device.setListener(listener);
            device.setConnectionType(connectionType);
            device.initialize(context);
        }

        return device;
    }

    public static IReaderDevice createReader(ReaderType type, Context context, IReaderListener listener) {
        return createReader(type, ConnectionType.AUTO, context, listener);
    }

    public static ReaderType getBestReaderType(Context context) {
        String typeStr = SharedPreferencesGetSet.leer_local("reader_type", context);
        ReaderType type = ReaderType.ZEBRA; // Default
        
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = ReaderType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid reader type: " + typeStr + ", defaulting to ZEBRA");
                type = ReaderType.ZEBRA;
            }
        } else {
            String model = Build.MODEL;
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
        return type;
    }

    public static ConnectionType getBestConnectionType(Context context, ReaderType type) {
        String connStr = SharedPreferencesGetSet.leer_local("connection_type", context);
        String connLastOk = SharedPreferencesGetSet.leer_local("connection_type_last_ok", context);
        ConnectionType connType = ConnectionType.AUTO;

        if ((connStr == null || connStr.isEmpty()) && connLastOk != null && !connLastOk.isEmpty()) {
            connStr = connLastOk;
        }

        if (connStr != null && !connStr.isEmpty()) {
            try {
                connType = ConnectionType.valueOf(connStr);
            } catch (Exception e) {
                connType = ConnectionType.AUTO;
                Log.w(TAG, "Tipo de conexión inválido en preferencias, usando AUTO");
            }
        }

        if (type == ReaderType.ZEBRA && connType == ConnectionType.AUTO) {
            String zebraConn = SharedPreferencesGetSet.leer_local("zebra_connection_type_last_ok", context);
            if (zebraConn != null && !zebraConn.isEmpty()) {
                try {
                    ConnectionType restored = ConnectionType.valueOf(zebraConn);
                    if (restored != ConnectionType.AUTO) {
                        connType = restored;
                    }
                } catch (Exception ignored) {}
            }
            if (connType == ConnectionType.AUTO) {
                connType = ConnectionType.SERIAL;
            }
        }
        return connType;
    }
}
