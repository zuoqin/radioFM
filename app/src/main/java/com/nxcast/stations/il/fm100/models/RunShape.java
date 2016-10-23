package com.nxcast.stations.il.fm100.models;

public class RunShape {

    private int currentSpeed;
    private int previousSpeed;

    public RunShape(int currentSpeed, int previousSpeed) {
        this.currentSpeed = currentSpeed;
        this.previousSpeed = previousSpeed;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getPreviousSpeed() {
        return previousSpeed;
    }

    public void setPreviousSpeed(int previousSpeed) {
        this.previousSpeed = previousSpeed;
    }
}
