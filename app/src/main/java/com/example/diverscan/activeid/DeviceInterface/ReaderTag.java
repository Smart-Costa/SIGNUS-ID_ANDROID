package com.example.diverscan.activeid.DeviceInterface;

public class ReaderTag {
    private String epc;
    private String tid;
    private short rssi;
    private int memoryBank;
    private int offset;

    public ReaderTag(String epc, short rssi) {
        this.epc = epc;
        this.rssi = rssi;
    }

    public ReaderTag(String epc, String tid, short rssi) {
        this.epc = epc;
        this.tid = tid;
        this.rssi = rssi;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public short getRssi() {
        return rssi;
    }

    public void setRssi(short rssi) {
        this.rssi = rssi;
    }
    
    // Compatibility methods for Zebra TagData replacement
    public String getTagID() {
        return epc;
    }
    
    public short getPeakRSSI() {
        return rssi;
    }

    private boolean containsLocationInfo = false;
    private short relativeDistance = 0;

    public boolean isContainsLocationInfo() {
        return containsLocationInfo;
    }

    public void setContainsLocationInfo(boolean containsLocationInfo) {
        this.containsLocationInfo = containsLocationInfo;
    }

    public short getRelativeDistance() {
        return relativeDistance;
    }

    public void setRelativeDistance(short relativeDistance) {
        this.relativeDistance = relativeDistance;
    }
}
