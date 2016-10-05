package fm100.co.il;

import java.io.BufferedReader;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Locale;

        import android.annotation.SuppressLint;
        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.ActionBarActivity;
        import android.support.v7.app.ActionBarDrawerToggle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.AdapterView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;
        import android.widget.RemoteViews;
        import android.widget.TextView;
        import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import fm100.co.il.adapters.ChannelListAdapter;
import fm100.co.il.adapters.StationListAdapter;
        import fm100.co.il.busEvents.IntBusEvent;
        import fm100.co.il.busEvents.NewSongBusEvent;
        import fm100.co.il.busEvents.NotificationBusEvent;
        import fm100.co.il.adapters.NavListAdapter;
        import fm100.co.il.busEvents.StationBusEvent;
        import fm100.co.il.busEvents.VideoListBusEvent;
        import fm100.co.il.fragments.MyAbout;
        import fm100.co.il.fragments.MyHome;
        import fm100.co.il.fragments.MySettings;
        import fm100.co.il.models.Channel;
import fm100.co.il.models.MyObject;
import fm100.co.il.models.NavItem;
        import fm100.co.il.models.Station;
        import fm100.co.il.R;
        import fm100.co.il.models.VideoObj;

        import org.greenrobot.eventbus.EventBus;
        import org.greenrobot.eventbus.Subscribe;
        import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
//    private static MainActivity ins;

    DrawerLayout drawerLayout;
    RelativeLayout drawerPane;
    ListView lvNav;
    ListView lvStations;
    Bundle myBundle;
    List<NavItem> listNavItems;
    List<Fragment> listFragments;
    MyHome home;
    //private LinearLayout inDrawLayout;

    private EventBus bus = EventBus.getDefault();
    private NotificationManager notificationManager = null;

    // setting last variables to remember the last
    private String lastAction = "com.example.hpuser.rad100fm.ACTION_PLAY";
    private int lastBtnImage = R.drawable.newplayicon1;
    private String lastSongName = " ";
    private String lastArtistName = " ";

    ActionBarDrawerToggle actionBarDrawerToggle;
    private static Context myApplicationContext = null;

    // ---------------------------------------
    public static ArrayList<Channel> channelsArray = new ArrayList<>();
    StationListAdapter myCustomAdapter;
    public List<Station> stationList = new ArrayList<>();
    public List<VideoObj> videoList = new ArrayList<>();


    // setting irational number so first time never be even
    private int lastItemClicked = 100;


    private LinearLayout inDrawLayout ;

    private ProgressBar drawerListProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        myBundle = savedInstanceState;
        if(isRTL()==false) {
            //getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_main);
        myApplicationContext = this;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListProgressBar = (ProgressBar) findViewById(R.id.progressBarView);
        inDrawLayout = (LinearLayout) findViewById(R.id.inDrawLayout);
        //flipLayouts();
       /* if(isRTL()==false) {
            ArrayList<View> views = new ArrayList<View>();
            for (int x = 0; x < inDrawLayout.getChildCount(); x++) {
                views.add(inDrawLayout.getChildAt(x));
                Log.e("mynewlog" , "FALSE WAS HERE");
            }
            inDrawLayout.removeAllViews();
            for (int x = views.size() - 1; x >= 0; x--) {
                inDrawLayout.addView(views.get(x));
            }
        }
        */


        drawerPane = (RelativeLayout) findViewById(R.id.drawer_pane);
        // flipLayouts();

        lvNav = (ListView) findViewById(R.id.nav_list);
        lvStations = (ListView) findViewById(R.id.stationsLv);

        EventBus.getDefault().register(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://demo.goufo.co.il/100fm/", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject obj = new JSONObject(new String(responseBody));

                    home.setStationData(obj);
                    home.setHeaderBar(getSupportActionBar(), drawerLayout, drawerPane);

                    JSONArray parentArray = obj.getJSONArray("stations");

                    Station tempStation = null;

                    for (int i = 0; i < parentArray.length(); i++) {

                        tempStation = new Station();
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        tempStation.setStationName(finalObject.getString("name"));
                        tempStation.setStationAudio(finalObject.getString("audio"));
                        tempStation.setSongInfo(finalObject.getString("info"));
                        tempStation.setStationSlug(finalObject.getString("slug"));
                        tempStation.setStationLogo(finalObject.getString("logo"));
                        stationList.add(tempStation);
                    }

                    drawerListProgressBar.setVisibility(View.GONE);
                    setListData(stationList);
                    myCustomAdapter = new StationListAdapter(myApplicationContext, channelsArray);
                    lvStations.setAdapter(myCustomAdapter);
                    lvStations.setOnItemClickListener(itemClickListener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("fm100", "100fm loaded : " + stationList.size()	 );
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

        home = new MyHome();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, home).commit();

        Drawable titleDrawable = getResources().getDrawable(R.drawable.header);
        getSupportActionBar().setBackgroundDrawable(titleDrawable);
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setTitle("");
        //getActionBar().setIcon(R.drawable.fm100);

        drawerLayout.closeDrawer(drawerPane);
        //drawerLayout.openDrawer(drawerPane);

        // set listener for drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_opened, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // TODO Auto-generated method stub
                invalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }

        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        Intent intent = new Intent(MainActivity.this, Loading.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the MyHome/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
              Toast.makeText(this , "Pressed" , Toast.LENGTH_LONG).show();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "http://digital.100fm.co.il/";
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;

            default:
               // return super.onOptionsItemSelected(item);
                if (actionBarDrawerToggle.onOptionsItemSelected(item))
                    return true;

                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    // method to get main activity context easily
    public static Context getMyApplicationContext() {
        return myApplicationContext;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       EventBus.getDefault().unregister(this);
        if (notificationManager != null)
        notificationManager.cancelAll();
    }

    // a method that transfer Events happening on the Notification to MainActivity
    @Subscribe
      public void onEvent(NotificationBusEvent event) {
        // set a "pause" notification
        if (event.getNotificationBusMsg().equals("pause")){
            int buttonImage = R.drawable.newplayicon1;
            String actionName = "com.example.hpuser.rad100fm.ACTION_PLAY";
            setNotification(buttonImage, actionName , lastSongName , lastArtistName);
            lastAction = actionName;
            lastBtnImage = buttonImage;
        }

        // set a "play" notification
        else if (event.getNotificationBusMsg().equals("play")){
            int buttonImage = R.drawable.newpauseicon1;
            String actionName = "com.example.hpuser.rad100fm.ACTION_PAUSE";
            setNotification(buttonImage, actionName , lastSongName , lastArtistName);
            lastAction = actionName;
            lastBtnImage = buttonImage;
        }

        // remove notification
        else if (event.getNotificationBusMsg().equals("delete")){
                if(notificationManager != null){
                    notificationManager.cancelAll();
                    lastAction = "delete";
                }
        }
    }


    // set a new notification with data in order to make changes on notification on button pressess and song changes
    public void setNotification(int playPauseImage , String actionName , String songName , String artistName){
        String ns = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) getSystemService(ns);

        @SuppressWarnings("deprecation")

        Notification notification = new Notification(R.drawable.fm100, null, System.currentTimeMillis());

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.custom_notifications);
        notificationView.setImageViewResource(R.id.playPauseIb , playPauseImage);

        //the intent that is started when the notification is clicked
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentView = notificationView;
        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        //this is the intent that is supposed to be called when the playPause button is clicked
        Intent switchIntent = new Intent(actionName);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 100, switchIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.playPauseIb, pendingSwitchIntent);

        //this is the intent that is supposed to be called when the delete button is clicked
        Intent deleteIntent = new Intent("com.example.hpuser.rad100fm.ACTION_DELETE");
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this, 100, deleteIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.btnDelete, pendingDeleteIntent);

        // setting the texts in the textviews
        notificationView.setTextViewText(R.id.notiSongNameTv , songName);
        notificationView.setTextViewText(R.id.notiArtistNameTv , artistName);


        notificationManager.notify(1, notification);
    }

    // method activated when song change on Music class
    @Subscribe
    public void onSongChange(NewSongBusEvent songEvent) {
        if (!lastAction.equals("delete")) {
            try {
            setNotification(lastBtnImage, lastAction, songEvent.getSongName(), songEvent.getArtistName());
            lastSongName = songEvent.getSongName();
            lastArtistName = songEvent.getArtistName();
            } catch (Exception e) {
            Log.e("MyLog", "onSongChange exception : " + e.getMessage());
            }
        }
    }

    private boolean isRTL() {
        Locale defLocale = Locale.getDefault();
        return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
    }

    private void setListData(List<Station> stations) {
        for (int i = 0 ; i<stations.size() ; i++){
            final Channel newChannel = new Channel(stations.get(i).getStationName()
                    , stations.get(i).getStationAudio()
                    , stations.get(i).getSongInfo(),stations.get(i).getStationLogo(),stations.get(i).getStationName());
            channelsArray.add(newChannel);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IntBusEvent event = new IntBusEvent(position);
                EventBus.getDefault().post(event);
            //if (lastItemClicked != position) {
                int buttonImage = R.drawable.newpauseicon1;
                String actionName = "com.example.hpuser.rad100fm.ACTION_PAUSE";
                setNotification(buttonImage, actionName, lastSongName, lastArtistName);
           // }
           // else{
                /*
                int buttonImage = R.drawable.newplayicon1;
                String actionName = "com.example.hpuser.rad100fm.ACTION_PLAY";
                setNotification(buttonImage, actionName, lastSongName, lastArtistName);
                */
              //  s.makeText(myApplicationContext.getApplicationContext() , "channel already selected" , Toast.LENGTH_SHORT ).show();
           // }
            lastItemClicked = position;
        }
    };
}
