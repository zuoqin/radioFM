package com.nxcast.stations.il.fm100.helpers;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.models.ScheduleItem;
import com.nxcast.stations.il.fm100.models.Song;

/************************************************
 * Parse the schedule Data xml recieveing from the Schedule Class
 ************************************************/

public class ScheduleXMLParser {

        static final String KEY_PROGRAM = "Program";
        static final String KEY_NAME = "ProgramName";
        static final String KEY_DESC = "ProgramDes";
        static final String KEY_DAY = "ProgramDay";
        static final String KEY_AUTHOR = "ProgramAutor";
    static final String KEY_HOUR = "ProgramStartHoure";
    static final String KEY_IMAGE = "ProgramImg";



        public static  List<ScheduleItem> getScheduleListFromFile(Context ctx) {


            // List of StackSites that we will return
            List<ScheduleItem> scheduleItemList = new ArrayList<>();

            // temp holder for current StackSite while parsing
            ScheduleItem scheduleItem = null;

            // temp holder for current text value while parsing
            String curText = "";

            try {
                // Get our factory and PullParser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                // Open up InputStream and Reader of our file.
                FileInputStream fis = ctx.openFileInput("Schedule.xml");
                //BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                // point the parser to our file.
                //xpp.setInput(reader);
                xpp.setInput(fis, "UTF-8");

                // get initial eventType
                int eventType = xpp.getEventType();

                // Loop through pull events until we reach END_DOCUMENT
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    // Get the current tag
                    String tagname = xpp.getName();
                    // React to different event types appropriately
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagname.equalsIgnoreCase(KEY_PROGRAM)) {
                                scheduleItem = new ScheduleItem();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            curText = xpp.getText();
                            break;

                        case XmlPullParser.END_TAG:
                             if (tagname.equalsIgnoreCase(KEY_NAME)) {
                                scheduleItem.setProgramName(curText);
                            }  else if (tagname.equalsIgnoreCase(KEY_DESC)) {
                                scheduleItem.setProgramDesc(curText);
                            }  else if (tagname.equalsIgnoreCase(KEY_DAY)) {
                                scheduleItem.setProgramDay(curText);
                            }   else if (tagname.equalsIgnoreCase(KEY_HOUR)) {
                                 scheduleItem.setProgramStartHoure(curText);
                             }   else if (tagname.equalsIgnoreCase(KEY_AUTHOR)) {
                                 scheduleItem.setProgramAutor(curText.trim());
                             }   else if (tagname.equalsIgnoreCase(KEY_IMAGE)) {
                                 scheduleItem.setProgramImage(curText);
                             }   else if (tagname.equalsIgnoreCase(KEY_PROGRAM)) {
                                 scheduleItemList.add(scheduleItem);
                             }   else break;

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
            return scheduleItemList;
        }

    }