package onn.android.hpuser.rad100fm.busEvents;

/************************************************
 * a bus event that Transfer Song data between classes
 ************************************************/

public class NewSongBusEvent {
    private String songName;
    private String artistName;

    public NewSongBusEvent(String songName , String artistName) {
        this.songName = songName;
        this.artistName = artistName;

    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongName() {
        return songName;
    }
}
