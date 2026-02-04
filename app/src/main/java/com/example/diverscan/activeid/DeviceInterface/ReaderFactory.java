package com.example.diverscan.activeid.DeviceInterface;

import android.content.Context;
import com.example.diverscan.activeid.DeviceInterface.ConnectionType;
import com.example.diverscan.activeid.DeviceInterface.Impl.ZebraReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminReaderImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.IminScannerImpl;
import com.example.diverscan.activeid.DeviceInterface.Impl.DatalogicReaderImpl;

public class ReaderFactory {
    public static IReaderDevice createReader(ReaderType type, ConnectionType connectionType, Context context, IReaderListener listener) {
        IReaderDevice device = null;
        switch (type) {
            case ZEBRA:
                device = new ZebraReaderImpl();
                break;
            case IMIN:
                device = new IminReaderImpl();
                break;
            case IMIN_SCANNER:
                device = new IminScannerImpl();
                break;
            case DATALOGIC:
                device = new DatalogicReaderImpl();
                break;
            // Future implementations
            // case CHAINWAY:
            //     device = new ChainwayReaderImpl();
            //     break;
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

    // Overload for backward compatibility (defaults to AUTO)
    public static IReaderDevice createReader(ReaderType type, Context context, IReaderListener listener) {
        return createReader(type, ConnectionType.AUTO, context, listener);
    }
}
