package com.nxcast.stations.il.fm100.models;

/************************************************
 * an Object retracted from the Station JsonObject from the url
 ************************************************/

public class Station {

    private String StationName;
    private String StationAudio;
    private String SongInfo;
    private String StationSlug;
    private String StationLogo;
    private String StationDescription;

    public Station() {
    }

    public Station(String songInfo, String stationAudio, String stationLogo, String stationName, String stationSlug, String stationDescription) {
        SongInfo = songInfo;
        StationAudio = stationAudio;
        StationLogo = stationLogo;
        StationName = stationName;
        StationSlug = stationSlug;
        StationDescription = stationDescription;
    }

    public String getSongInfo() {
        return SongInfo;
    }

    public void setSongInfo(String songInfo) {
        SongInfo = songInfo;
    }

    public String getStationAudio() {
        return StationAudio;
    }

    public void setStationAudio(String stationAudio) {
        StationAudio = stationAudio;
    }

    public String getStationLogo() {
        return StationLogo;
    }

    public void setStationLogo(String stationLogo) {
        StationLogo = stationLogo;
    }

    public String getStationName() {
        return StationName;
    }

    public void setStationName(String stationName) {
        StationName = stationName;
    }

    public String getStationSlug() {
        return StationSlug;
    }

    public void setStationSlug(String stationSlug) {
        StationSlug = stationSlug;
    }

    public String getStationDescription() {
        return StationDescription;
    }

    public void setStationDescription(String stationSlug) {
        StationDescription = stationSlug;
    }

}
