package com.nxcast.stations.il.fm100.inner.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.adapters.ScheduleListAdapter;
import com.nxcast.stations.il.fm100.adapters.StationListAdapter;
import com.nxcast.stations.il.fm100.busEvents.NewSongBusEvent;
import com.nxcast.stations.il.fm100.busEvents.NotificationBusEvent;
import com.nxcast.stations.il.fm100.fragments.MyHome;
import com.nxcast.stations.il.fm100.helpers.Downloader;
import com.nxcast.stations.il.fm100.helpers.ScheduleXMLParser;
import com.nxcast.stations.il.fm100.helpers.SongXMLParser;
import com.nxcast.stations.il.fm100.models.ScheduleItem;
import com.nxcast.stations.il.fm100.models.Song;
import com.nxcast.stations.il.fm100.models.Station;

public class Schedule extends Fragment {

    String path;
    private ListView scheduleList;
    private ScheduleListAdapter myScheduleListAdapter;
    private List<ScheduleItem> scheduleItemList = new ArrayList<>();
    private String[] daysList = MainActivity.getMyApplicationContext().getResources().getStringArray(R.array.days_list);
    int currentRow = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_fragment, container, false);

        scheduleList = (ListView) v.findViewById(R.id.scheduleList);

        ScheduleDownloadTask download = new ScheduleDownloadTask();
        download.execute();

        ImageButton btnMenu = (ImageButton) v.findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //drawerLayout.openDrawer(Gravity.RIGHT);
                MyHome activity = (MyHome) getParentFragment();
                activity.openSubmenu();
            }
        });

        ImageButton btnLike = (ImageButton) v.findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("")
                        .setContentDescription("")
                        .setContentUrl(Uri.parse("http://digital.100fm.co.il/"))
                        .setShareHashtag(new ShareHashtag.Builder()
                                .setHashtag("#100fmDigital")
                                .build())
                        .build();

                ShareDialog shareDialog = new ShareDialog(getActivity());
                shareDialog.show(linkContent, ShareDialog.Mode.AUTOMATIC);
            }
        });

        return v;
    }

    // getting the xml for the song data and checking for it every second if changed
    private class ScheduleDownloadTask extends AsyncTask<Void, Void, List<ScheduleItem>> {


        @Override
        protected List<ScheduleItem> doInBackground(Void... arg0) {
            //Download the file
            try {
                Downloader.DownloadFromUrl("http://www.100fm.co.il/smartphoneXML/programs.aspx", MainActivity.getMyApplicationContext().openFileOutput("Schedule.xml", Context.MODE_PRIVATE));
                path = MainActivity.getMyApplicationContext().getFilesDir().getAbsolutePath() + "/Schedule.xml";
                File file = new File(path);
                if (file.exists()) {
                    List<ScheduleItem> tempScheduleList = ScheduleXMLParser.getScheduleListFromFile(MainActivity.getMyApplicationContext());

                    scheduleItemList.clear();
                    int dd = -1;
                    String slug = "";

                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    String days = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                    int hours = date.getHours() * 100;
                    currentRow = -1;

                    for (int i=0 ; i<tempScheduleList.size() ; i++){
                        ScheduleItem item = new ScheduleItem();
                        if( !slug.equals( tempScheduleList.get(i).getProgramDay() )) {
                            slug = tempScheduleList.get(i).getProgramDay();
                            item.setTitle( daysList[++dd] );
                            scheduleItemList.add(item);
                        }
                        scheduleItemList.add(tempScheduleList.get(i));

                        if( days.equals(tempScheduleList.get(i).getProgramDay()) ) {
                            int h = Integer.parseInt( tempScheduleList.get(i).getProgramStartHoure().replace(":","") );
                                if( hours <= h && currentRow == -1) {
                                    currentRow = i;
                                }
                            }
                        }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return scheduleItemList;
        }

        @Override
        protected void onPostExecute(List<ScheduleItem> scheduleItems) {
            super.onPostExecute(scheduleItems);

            myScheduleListAdapter = new ScheduleListAdapter(MainActivity.getMyApplicationContext(),scheduleItems);
            scheduleList.setAdapter(myScheduleListAdapter);
            scheduleList.setSelection(currentRow);
        }
    }
}




