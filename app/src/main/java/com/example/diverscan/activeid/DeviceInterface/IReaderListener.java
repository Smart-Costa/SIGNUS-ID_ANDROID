package com.example.diverscan.activeid.DeviceInterface;

import java.util.List;

public interface IReaderListener {
    void onConnected(String readerName);
    void onDisconnected();
    void onConnectionError(String message);
    void onTagRead(List<ReaderTag> tags);
    void onTrigger(boolean pressed);
    void onStatusMessage(String message);
}
