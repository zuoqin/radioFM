package com.nxcast.stations.il.fm100;

import java.io.IOException;
import java.util.ArrayList;
        import java.util.List;
        import java.util.Locale;

import android.Manifest;
import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
        import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.app.ActivityManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.ActionBarActivity;
        import android.support.v7.app.ActionBarDrawerToggle;
        import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;
        import android.widget.RemoteViews;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

import com.nxcast.stations.il.fm100.adapters.StationListAdapter;
        import com.nxcast.stations.il.fm100.busEvents.IntBusEvent;
        import com.nxcast.stations.il.fm100.busEvents.NewSongBusEvent;
        import com.nxcast.stations.il.fm100.busEvents.NotificationBusEvent;
import com.nxcast.stations.il.fm100.fragments.MyHome;
import com.nxcast.stations.il.fm100.helpers.DownloadImageTask;
import com.nxcast.stations.il.fm100.models.NavItem;
        import com.nxcast.stations.il.fm100.models.Station;
import com.nxcast.stations.il.fm100.models.VideoObj;

        import org.greenrobot.eventbus.EventBus;
        import org.greenrobot.eventbus.Subscribe;
        import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

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
    private int lastBtnImage = R.drawable.play2;
    private String lastSongName = " ";
    private String lastArtistName = " ";

    ActionBarDrawerToggle actionBarDrawerToggle;
    private static Context myApplicationContext = null;

    // ---------------------------------------
    public static ArrayList<Station> channelsArray = new ArrayList<>();
    StationListAdapter myCustomAdapter;
    public List<Station> stationList = new ArrayList<>();
    public List<VideoObj> videoList = new ArrayList<>();


    // setting irational number so first time never be even
    private int lastItemClicked = 100;


    private LinearLayout inDrawLayout ;

    private ProgressBar drawerListProgressBar;

    MediaSession audioSession;
    RemoteControlReceiver mButtonReceiver;

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    String deepLinkingSlug = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        myBundle = savedInstanceState;

        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myApplicationContext = this;

        Fabric.with(this, new Crashlytics());

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListProgressBar = (ProgressBar) findViewById(R.id.progressBarView);
        inDrawLayout = (LinearLayout) findViewById(R.id.inDrawLayout);

        Intent in = getIntent();
        Uri data = in.getData();
        if( data != null ) {
            if( data.toString().indexOf("fm100://play?") != -1 ) {
                deepLinkingSlug = data.toString().substring(data.toString().indexOf("?") + 1);
                Log.v("100fm", "data: " + data + " deepLinkingSlug: " + deepLinkingSlug);
            }
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioSession = new MediaSession(getApplicationContext(), "100fm");

            audioSession.setCallback(new MediaSession.Callback() {

                @Override
                public boolean onMediaButtonEvent(final Intent mediaButtonIntent) {
                    String intentAction = mediaButtonIntent.getAction();
                    if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                        KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                        if (event != null) {
                            int code = event.getKeyCode();
                            NotificationBusEvent e = null;

                            if (KeyEvent.KEYCODE_HEADSETHOOK == code) {
                                e = new NotificationBusEvent("headsethook");
                            } else if (KeyEvent.KEYCODE_MEDIA_PLAY == code) {
                                e = new NotificationBusEvent("play");
                            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == code) {
                                e = new NotificationBusEvent("pause");
                            } else if (KeyEvent.KEYCODE_MEDIA_NEXT == code) {
                                e = new NotificationBusEvent("next");
                            } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == code) {
                                e = new NotificationBusEvent("prev");
                            }

                            if( e != null ) {
                                EventBus.getDefault().post(e);
                            }
                        }
                    }
                    return true;
                }


            });

            PlaybackState state = new PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                    .setState(PlaybackState.STATE_PLAYING, 0, 0, 0)
                    .build();
            audioSession.setPlaybackState(state);

            audioSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            audioSession.setActive(true);
        } else {
            mButtonReceiver = new RemoteControlReceiver();

            IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);//"android.intent.action.MEDIA_BUTTON"
            filter.setPriority(1000);

            registerReceiver(mButtonReceiver, filter);

            /*AudioManager mAudioManager =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            ComponentName mReceiverComponent = new ComponentName(this,RemoteControlReceiver.class);
            mAudioManager.registerMediaButtonEventReceiver(mReceiverComponent);*/
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }

        drawerPane = (RelativeLayout) findViewById(R.id.drawer_pane);
        // flipLayouts();

        lvNav = (ListView) findViewById(R.id.nav_list);
        lvStations = (ListView) findViewById(R.id.stationsLv);

        EventBus.getDefault().register(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://digital.100fm.co.il/app/", new AsyncHttpResponseHandler() {

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

                    stationList.clear();
                    for (int i = 0; i < parentArray.length(); i++) {
                        tempStation = new Station();
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        tempStation.setStationName(finalObject.getString("name"));
                        tempStation.setStationAudio(finalObject.getString("audioA"));
                        tempStation.setSongInfo(finalObject.getString("info"));
                        tempStation.setStationSlug(finalObject.getString("slug"));
                        tempStation.setStationLogo(finalObject.getString("logo"));
                        tempStation.setStationDescription(finalObject.getString("description"));
                        stationList.add(tempStation);

                        //new DownloadImageTask(null, getApplicationContext()).execute(tempStation.getStationLogo());
                        Picasso.with(getMyApplicationContext()).load(tempStation.getStationLogo()).fetch();//.into(viewHolder.imageView);
                        //Log.i("100fm", tempStation.getStationDescription());
                    }

                    if( drawerListProgressBar != null ) {
                        drawerListProgressBar.setVisibility(View.GONE);
                    }
                    setListData(stationList);
                    myCustomAdapter = new StationListAdapter(myApplicationContext, channelsArray);
                    lvStations.setAdapter(myCustomAdapter);
                    lvStations.setOnItemClickListener(itemClickListener);

                    if( deepLinkingSlug != "" ) {
                        for (int i = 0; i < stationList.size(); i++) {
                            Station s = stationList.get(i);
                            if( s.getStationSlug().equals(deepLinkingSlug) ) {
                                IntBusEvent event = new IntBusEvent(i);
                                EventBus.getDefault().post(event);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d("fm100", "100fm loaded : " + stationList.size());
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

        if( drawerLayout != null ) {
            drawerLayout.setDrawerListener(actionBarDrawerToggle);
        }

        Intent intent = new Intent(MainActivity.this, Loading.class);
        startActivity(intent);

        ImageButton btnFb = (ImageButton) findViewById(R.id.btnFacebook);
        btnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/158664457479014"));
                    startActivity(intent);
                } catch(Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/100FMRADIOS/")));
                }
            }
        });
        ImageButton btnIns = (ImageButton) findViewById(R.id.btnInstagram);
        btnIns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/radios_100fm/")));
            }
        });
        ImageButton btnWeb = (ImageButton) findViewById(R.id.btnChrome);
        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.100fm.co.il/")));
            }
        });
    }

    // Broadcast Receiver for Music play.
    private BroadcastReceiver musicPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v("100fm", "Music play started");
        }
    };

    // Broadcast Receiver for Music pause.
    private BroadcastReceiver musicPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v("100fm", "Music paused");
        }
    };

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
                try{
                    // return super.onOptionsItemSelected(item);
                    if( actionBarDrawerToggle != null ) {
                        if (actionBarDrawerToggle.onOptionsItemSelected(item))
                            return true;
                    }
                }catch(Exception e){

                }
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        //NotificationBusEvent event = new NotificationBusEvent("delete");
        //EventBus.getDefault().post(event);

        EventBus.getDefault().unregister(this);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        super.onDestroy();
    }

    public void CheckMemory(){
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;

        //Percentage can be calculated for API 16+
        //double percentAvail = mi.availMem / (double)mi.totalMem;
        if(availableMegs < 200){

            //this.finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        onDestroy();
    }

        // a method that transfer Events happening on the Notification to MainActivity
    @Subscribe
      public void onEvent(NotificationBusEvent event) {
        CheckMemory();
        // set a "pause" notification
        if (event.getNotificationBusMsg().equals("pause")){
            int buttonImage = R.drawable.play2;
            String actionName = "com.example.hpuser.rad100fm.ACTION_PLAY";
            setNotification(buttonImage, actionName , lastSongName , lastArtistName);
            lastAction = actionName;
            lastBtnImage = buttonImage;
        }

        // set a "play" notification
        else if (event.getNotificationBusMsg().equals("play")){
            int buttonImage = R.drawable.stop2;
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
            //this.finish();
            this.finishAndRemoveTask();
            //System.exit(0);
        }
    }


    // set a new notification with data in order to make changes on notification on button pressess and song changes
    public void setNotification(int playPauseImage , String actionName , String songName , String artistName){
        try{
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
        } catch(Exception e){
            Log.e("100fm", "onSongChange exception : " + e.getMessage());
        }
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
                Log.e("100fm", "onSongChange exception : " + e.getMessage());
            }
        }
    }

    private boolean isRTL() {
        Locale defLocale = Locale.getDefault();
        return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
    }

    private void setListData(List<Station> stations) {
        for (int i = 0 ; i<stations.size() ; i++){
            final Station newChannel = new Station(stations.get(i).getSongInfo(), stations.get(i).getStationAudio(), stations.get(i).getStationLogo(), stations.get(i).getStationName(), stations.get(i).getStationSlug(), stations.get(i).getStationDescription());
            channelsArray.add(newChannel);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IntBusEvent event = new IntBusEvent(position);
            EventBus.getDefault().post(event);
            if (lastItemClicked != position) {
                int buttonImage = R.drawable.stop2;
                String actionName = "com.example.hpuser.rad100fm.ACTION_PAUSE";
                setNotification(buttonImage, actionName, lastSongName, lastArtistName);
           }
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
