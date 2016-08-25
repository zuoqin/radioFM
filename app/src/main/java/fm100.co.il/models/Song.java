package fm100.co.il.models;

/************************************************
 * an Object retracted from the channelSongInfo xml , from the station object
 ************************************************/

public class Song {

    private String songDataUrl = "http://www.fmplayer.co.il/NowPlaying/Ch10-Hiphop.xml";

    private String songName = "" ;
    private String artist ="";

    public Song() {

    }

    public Song(String songName, String artist) {
        this.artist = artist;
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

}
