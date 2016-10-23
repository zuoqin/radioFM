package com.nxcast.stations.il.fm100.busEvents;

/************************************************
 * the bus evenet which transfer an int from the MainActivity drawer StationList to an action in music class by position
 ************************************************/

public class IntBusEvent {
    private int intBusMsg;

    public IntBusEvent(int intBusMsg) {
        this.intBusMsg = intBusMsg;

    }

    public int getintBusEvent() {
        return intBusMsg;
    }
}

