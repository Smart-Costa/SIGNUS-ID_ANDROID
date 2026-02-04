package com.example.diverscan.activeid.DeviceInterface;

import android.content.Context;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.Impl.ZebraReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminScannerImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.DatalogicReaderImpl;

public class ReaderFactory {
    public static IReaderDevice createReader(ReaderType type, ConnectionType connectionType, Context context, IReaderListener listener) {
        android.util.Log.d("ReaderFactory", "createReader called with Type: " + type + ", ConnectionType: " + connectionType);
        IReaderDevice device = null;
        try {
            switch (type) {
                case ZEBRA:
                    android.util.Log.d("ReaderFactory", "Instantiating ZebraReaderImpl");
                    device = new ZebraReaderImpl();
                    break;
                case IMIN:
                    android.util.Log.d("ReaderFactory", "Instantiating IminReaderImpl");
                    device = new IminReaderImpl();
                    break;
                case IMIN_SCANNER:
                    android.util.Log.d("ReaderFactory", "Instantiating IminScannerImpl");
                    device = new IminScannerImpl();
                    break;
                case DATALOGIC:
                    android.util.Log.d("ReaderFactory", "Instantiating DatalogicReaderImpl");
                    device = new DatalogicReaderImpl();
                    break;
                default:
                    android.util.Log.e("ReaderFactory", "Unknown reader type: " + type);
                    throw new IllegalArgumentException("Unknown reader type: " + type);
            }
            
            if (device != null) {
                android.util.Log.d("ReaderFactory", "Initializing device: " + device.getClass().getSimpleName());
                device.setListener(listener);
                device.setConnectionType(connectionType);
                device.initialize(context);
                android.util.Log.d("ReaderFactory", "Device initialized successfully");
            }
        } catch (Exception e) {
            android.util.Log.e("ReaderFactory", "Error creating reader: " + e.getMessage(), e);
        }
        
        return device;
    }

    // Overload for backward compatibility (defaults to AUTO)
    public static IReaderDevice createReader(ReaderType type, Context context, IReaderListener listener) {
        return createReader(type, ConnectionType.AUTO, context, listener);
    }
}
