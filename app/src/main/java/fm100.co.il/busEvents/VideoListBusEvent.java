package fm100.co.il.busEvents;


import java.util.ArrayList;
import java.util.List;

import fm100.co.il.models.Station;
import fm100.co.il.models.VideoObj;

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

