package fm100.co.il.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import fm100.co.il.models.Song;

/************************************************
 * Parse the song Data xml recieveing from the Music Class
 ************************************************/

public class SongXMLParser {

    static final String KEY_TRACK = "track";
    static final String KEY_NAME = "name";
    static final String KEY_ARTIST = "artist";

    public static Song getSongFromFile(Context ctx) {

        // List of StackSites that we will return
        // ------------------------------------Song stackSites;
        // ----------------------------------------Song = new Song();

        // temp holder for current StackSite while parsing
        Song currentSong = null;
        // temp holder for current text value while parsing
        String curText = "";

        try {
            // Get our factory and PullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            // Open up InputStream and Reader of our file.
            FileInputStream fis = ctx.openFileInput("Song.xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            // point the parser to our file.
            xpp.setInput(reader);

            // get initial eventType
            int eventType = xpp.getEventType();

            // Loop through pull events until we reach END_DOCUMENT
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Get the current tag
                String tagname = xpp.getName();
                // React to different event types appropriately
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase(KEY_TRACK)) {
                            // If we are starting a new <site> block we need
                            //a new StackSite object to represent it
                            currentSong = new Song();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        //grab the current text so we can use it in END_TAG event
                        curText = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase(KEY_TRACK)) {
                            // if </site> then we are done with current Site
                            // add it to the list.
                            // ----------------------stackSites.add(curStackSite);
                        } else if (tagname.equalsIgnoreCase(KEY_NAME)) {
                            // if </name> use setName() on curSite
                            currentSong.setSongName(curText);
                        } else if (tagname.equalsIgnoreCase(KEY_ARTIST)) {
                            // if </link> use setLink() on curSite
                            currentSong.setArtist(curText);
                        } else break;

                        break;

                    default:
                        break;
                }
                //move on to next iteration
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the populated list.
        return currentSong;
    }

}