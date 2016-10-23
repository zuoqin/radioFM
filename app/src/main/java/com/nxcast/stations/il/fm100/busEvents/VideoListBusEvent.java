package com.nxcast.stations.il.fm100.busEvents;


import java.util.ArrayList;
import java.util.List;

import com.nxcast.stations.il.fm100.models.Station;
import com.nxcast.stations.il.fm100.models.VideoObj;

/************************************************
 * the bus evenet which transfer the Station object list from main activity to music
 ************************************************/

public class VideoListBusEvent {
    private List<VideoObj> videoListBusMsg = new ArrayList<>();

    public VideoListBusEvent(List<VideoObj> stationBusMsg) {
        this.videoListBusMsg = stationBusMsg;
    }
    public List<VideoObj> getVideoListBusMsg() {
        return videoListBusMsg;
    }
}

