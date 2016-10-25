package com.nxcast.stations.il.fm100.models;


import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class RunningObject {

    private List<Entry> objectEntries = new ArrayList<>();
    private String runTime;
    private String runDistance;
    private String dateAdded;
    private String timeAdded;

    public RunningObject() {
    }

    public RunningObject(List<Entry> objectEntries) {
        this.objectEntries = objectEntries;
    }

    public List<Entry> getObjectEntries() {
        return objectEntries;
    }

    public void setObjectEntries(List<Entry> objectEntries) {
        this.objectEntries = objectEntries;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getRunDistance() {
        return runDistance;
    }

    public void setRunDistance(String runDistance) {
        this.runDistance = runDistance;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }
}
