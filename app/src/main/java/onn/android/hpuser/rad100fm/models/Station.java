package onn.android.hpuser.rad100fm.models;

/************************************************
 * an Object retracted from the Station JsonObject from the url
 ************************************************/

public class Station {

    private String StationName;
    private String StationAudio;
    private String SongInfo;
    private String StationSlug;
    private String StationLogo;

    public Station() {
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

}
