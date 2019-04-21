package com.keywestnetworks.kwconnect.Model;

public class wifiDetailsdata {

    String SSID;
    String BSSID;
    int frequency;
    int rssi;
    int channelWidth;
    String capabilities;
    boolean passpoint;


    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getChannelWidth() {
        return channelWidth;
    }

    public void setChannelWidth(int channelWidth) {
        this.channelWidth = channelWidth;
    }

    public boolean isPasspoint() {
        return passpoint;
    }

    public void setPasspoint(boolean passpoint) {
        this.passpoint = passpoint;
    }
}
