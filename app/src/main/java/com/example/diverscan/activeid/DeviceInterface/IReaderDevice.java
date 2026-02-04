package com.example.diverscan.activeid.DeviceInterface;

import android.content.Context;

public interface IReaderDevice {
    void initialize(Context context);
    boolean connect();
    boolean disconnect();
    boolean isConnected();
    boolean startInventory();
    boolean stopInventory();
    boolean startLocation(String epc);
    boolean stopLocation();
    void setPower(int power);
    void setListener(IReaderListener listener);
    void setConnectionType(ConnectionType type);
    String getDeviceName();
    
    // Optional methods
    boolean writeTag(String sourceEpc, String newEpc, String password);
    void dispose();
}
