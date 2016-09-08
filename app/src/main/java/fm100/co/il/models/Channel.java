package fm100.co.il.models;

/************************************************
 * an Object same as station but only get some of the data , can be removed
 ************************************************/

public class Channel {

    private String channelName ="";
    private String channelUrl ="";
    private String songDataUrl ="";
    private String channelLogo = "";
    private String channelSlug = "";

    public Channel(){

    }

    public Channel(String channelName, String channelUrl , String songDataUrl ,  String channelLogo) {
        this.channelName = channelName;
        this.channelUrl = channelUrl;
        this.songDataUrl = songDataUrl;
        this.channelLogo = channelLogo;
    }

    public Channel(String channelName, String channelUrl , String songDataUrl ,  String channelLogo , String channelSlug) {
        this.channelName = channelName;
        this.channelUrl = channelUrl;
        this.songDataUrl = songDataUrl;
        this.channelLogo = channelLogo;
        this.channelSlug = channelSlug;
    }

    public String getChannelSlug() {
        return channelSlug;
    }

    public void setChannelSlug(String channelSlug) {
        this.channelSlug = channelSlug;
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

    public String getChannelLogo() {
        return channelLogo;
    }

    public void setChannelLogo(String channelLogo) {
        this.channelLogo = channelLogo;
    }
}
