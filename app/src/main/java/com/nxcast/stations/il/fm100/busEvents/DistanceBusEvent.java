package com.nxcast.stations.il.fm100.busEvents;

/************************************************
 * an class which transfer the data from DistanceService to Running Class
 ************************************************/

public class DistanceBusEvent {
    private double distanceBusMsg;
    private double speedBusMsg;

    public DistanceBusEvent(double distanceBusMsg, double speedBusMsg) {
        this.distanceBusMsg = distanceBusMsg;
        this.speedBusMsg = speedBusMsg;
    }

    public double getDistanceBusMsg() {
        return distanceBusMsg;
    }

    public double getSpeedBusMsg() {
        return speedBusMsg;
    }
}
