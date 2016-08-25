package onn.android.hpuser.rad100fm.models;

/************************************************
 * an Object same as station but only get some of the data , can be removed
 ************************************************/

public class Channel {

    private String channelName ="";
    private String channelUrl ="";
    private String songDataUrl ="";
    private String channelLogo = "";

    public Channel(){

    }

    public Channel(String channelName, String channelUrl , String songDataUrl) {
        this.channelName = channelName;
        this.channelUrl = channelUrl;
        this.songDataUrl = songDataUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String name) {
        this.channelName = name;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        channelUrl = channelUrl;
    }

    public String getSongDataUrl() {
        return songDataUrl;
    }

    public void setSongDataUrl(String songDataUrl) {
        this.songDataUrl = songDataUrl;
    }
}
