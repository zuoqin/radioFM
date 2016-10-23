package com.nxcast.stations.il.fm100.busEvents;


import com.nxcast.stations.il.fm100.models.Station;

import java.util.ArrayList;
import java.util.List;

/************************************************
 * the bus evenet which transfer the Station object list from main activity to music
 ************************************************/


public class StationBusEvent {
    private List<Station> stationBusMsg = new ArrayList<>();

    public StationBusEvent(List<Station> stationBusMsg) {
        this.stationBusMsg = stationBusMsg;
    }
    public List<Station> getStationBusEvent() {
        return stationBusMsg;
    }
}
