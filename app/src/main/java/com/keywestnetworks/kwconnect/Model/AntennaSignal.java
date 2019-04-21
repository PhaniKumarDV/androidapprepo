package com.keywestnetworks.kwconnect.Model;

public class AntennaSignal {

    private String name;

    private String deviceRadioMode;

    private int best = Integer.MIN_VALUE;

    private int bg1Resource;

    private int bg2Resource;

    private int current = 0;

    public AntennaSignal(String deviceRadioMode, String name, int bg1Resource, int bg2Resource) {
        this.deviceRadioMode = deviceRadioMode;
        this.name = name;
        this.bg1Resource = bg1Resource;
        this.bg2Resource = bg2Resource;
    }

    public void setCurrent(int current) {
        this.current = current;
        this.best = current > this.best ? current: this.best;
    }

    public int getCurrent() {
        return this.current;
    }

    public int getBest() {
        return this.best;
    }

    public String getName() {
        return this.name;
    }

    public String getDeviceRadioMode() {
        return this.deviceRadioMode;
    }

    public int getDiff() {
        return this.best - this.current;
    }

    public int getBg1Resource() {
        return bg1Resource;
    }

    public int getBg2Resource() {
        return bg2Resource;
    }
}
