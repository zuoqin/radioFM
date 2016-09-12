package fm100.co.il.inner.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.adapters.ScheduleListAdapter;
import fm100.co.il.adapters.StationListAdapter;
import fm100.co.il.busEvents.NewSongBusEvent;
import fm100.co.il.busEvents.NotificationBusEvent;
import fm100.co.il.helpers.Downloader;
import fm100.co.il.helpers.ScheduleXMLParser;
import fm100.co.il.helpers.SongXMLParser;
import fm100.co.il.models.ScheduleItem;
import fm100.co.il.models.Song;
import fm100.co.il.models.Station;

public class Schedule extends Fragment {

    String path;
    private ListView scheduleList;
    private ScheduleListAdapter myScheduleListAdapter;
    private List<ScheduleItem> scheduleItemList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_fragment, container, false);

        scheduleList = (ListView) v.findViewById(R.id.scheduleList);

        ScheduleDownloadTask download = new ScheduleDownloadTask();
        download.execute();

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
                    scheduleItemList = ScheduleXMLParser.getScheduleListFromFile(MainActivity.getMyApplicationContext());
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
            //Log.i("100fm", "happening" + scheduleItemList);
            //Log.i("100fm", "OR HERE: " + scheduleItems);
        }
    }
}




