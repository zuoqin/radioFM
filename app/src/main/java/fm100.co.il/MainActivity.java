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
        import android.view.animation.TranslateAnimation;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.AdapterView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.RelativeLayout;
        import android.widget.RemoteViews;
        import android.widget.Toast;

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
        import fm100.co.il.models.NavItem;
        import fm100.co.il.models.Station;
        import fm100.co.il.R;
        import fm100.co.il.models.VideoObj;

        import org.greenrobot.eventbus.EventBus;
        import org.greenrobot.eventbus.Subscribe;
        import org.json.JSONArray;
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
    public ArrayList<Channel> channelsArray = new ArrayList<>();
    StationListAdapter myCustomAdapter;
    public List<Station> stationList = new ArrayList<>();
    public List<VideoObj> videoList = new ArrayList<>();
    ReadFileTask task = new ReadFileTask();
    StationsFromJsonTask stationTask = new StationsFromJsonTask();


    // setting irational number so first time never be even
    private int lastItemClicked = 100;


    private LinearLayout inDrawLayout ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myBundle = savedInstanceState;
        if(isRTL()==false) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
             setContentView(R.layout.activity_main);
        myApplicationContext = this;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
       // flipLayouts();
        if(isRTL()==false) {
            ArrayList<View> views = new ArrayList<View>();
            for (int x = 0; x < drawerLayout.getChildCount(); x++) {
                views.add(drawerLayout.getChildAt(x));
                Log.e("mynewlog" , "FALSE WAS HERE");
            }
            drawerLayout.removeAllViews();
            for (int x = views.size() - 1; x >= 0; x--) {
                drawerLayout.addView(views.get(x));
            }
        }


        drawerPane = (RelativeLayout) findViewById(R.id.drawer_pane);
        // flipLayouts();

        lvNav = (ListView) findViewById(R.id.nav_list);
        lvStations = (ListView) findViewById(R.id.stationsLv);

        EventBus.getDefault().register(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // checking if file already exists if it does it doesnt download it again
        String path=this.getFilesDir().getAbsolutePath()+"/JsonCo.json";
        File file = new File ( path );
        if ( file.exists() )
        {
            task.execute("JsonCo.json");
        } else {
            new JSONTask().execute("http://demo.goufo.co.il/100fm/");
        }
        // setting the StationsList

        //setting title bar background
        Drawable titleDrawable = getResources().getDrawable(R.drawable.title100fm);
        getSupportActionBar().setBackgroundDrawable(titleDrawable);

        //setting drawer list items , Fragments and setting an adapter to the view
        listNavItems = new ArrayList<NavItem>();
        listNavItems.add(new NavItem("Home", "MyHome page",
                R.drawable.homeyellow));
        listNavItems.add(new NavItem("Settings", "Change something",
                R.drawable.settingsyellow2));
        listNavItems.add(new NavItem("About", "Author's information",
                R.drawable.informationyellow));

        NavListAdapter navListAdapter = new NavListAdapter(
                getApplicationContext(), R.layout.item_nav_list, listNavItems);

        lvNav.setAdapter(navListAdapter);

        listFragments = new ArrayList<Fragment>();
        listFragments.add(new MyHome());
        listFragments.add(new MySettings());
        listFragments.add(new MyAbout());

        // load MyHome fragment as default:
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, listFragments.get(0)).commit();

        setTitle("");

        lvNav.setItemChecked(0, true);
        drawerLayout.closeDrawer(drawerPane);

        // set listener for the drawer list items:
        lvNav.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // replace the fragment with the selected option fragment:
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content, listFragments.get(position))
                        .commit();


                lvNav.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerPane);

            }
        });

        // set listener for drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_opened, R.string.drawer_closed) {


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
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
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

        Notification notification = new Notification(R.drawable.purpleheart, null, System.currentTimeMillis());

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

    //Async Tasks to Write and read new Json File on internal storage
    public class JSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            List<String> stringList = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String finalJson = buffer.toString();

                return finalJson;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            FileOutputStream outputStream = null;
            int created = 0;
            try{
                outputStream = openFileOutput("JsonCo.json" , Context.MODE_PRIVATE);
                outputStream.write(result.getBytes());
                outputStream.close();
                created = 1;
            }catch (Exception e){
                Log.e("MyLog" , e.getMessage());
            }
            if ( created == 1){
                task.execute("JsonCo.json");
            }
        }
    }
    private boolean isRTL() {
        Locale defLocale = Locale.getDefault();
        return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
    }

    // ------------------------ -----------------------           ----------------------------------------------
    // JSON RELATED ASYNCTASKS
    public class ReadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext() , "please wait few seconds for channels to load" , Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String temp="";
            try {
                FileInputStream fin = myApplicationContext.openFileInput(params[0]);
                int c;
                while( (c = fin.read()) != -1){
                    temp = temp + Character.toString((char)c);
                }
                fin.close();
            } catch (Exception e){
                Log.e("MyLog" , "read task exception: " + e.getMessage());
            }
            return temp;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // getting a list of Station objects
           // stationList = StationsFromJson(result);
            stationTask.execute(result);
            //videoList = videosFromJson(result);
           // Log.e("mynewlog" , "its this station: " + stationList.get(0).toString());
            //Log.e("mynewlog" , "its this video: " + videoList.get(0).toString());
            //EventBus.getDefault().post(new StationBusEvent(stationList));
            //EventBus.getDefault().post(new VideoListBusEvent(videoList));
           // setListData(stationList);
            //myCustomAdapter = new StationListAdapter(myApplicationContext, channelsArray);
            //lvStations.setAdapter(myCustomAdapter);
            //lvStations.setOnItemClickListener(itemClickListener);
        }
    }

    public class StationsFromJsonTask extends AsyncTask<String, Void, List<Station>> {

        @Override
        protected List<Station> doInBackground(String... params) {
            try {
                JSONObject parentObject = new JSONObject(params[0]);

                JSONArray parentArray = parentObject.getJSONArray("stations");

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
            } catch (Exception e) {
                Log.e("HereLog", "Json Exception" + e.getMessage());
            }

            return stationList;
        }

        @Override
        protected void onPostExecute(List<Station> stations) {
            super.onPostExecute(stations);
            stationList = stations;
            setListData(stationList);
            myCustomAdapter = new StationListAdapter(myApplicationContext, channelsArray);
            lvStations.setAdapter(myCustomAdapter);
            lvStations.setOnItemClickListener(itemClickListener);
        }
    }
    private void setListData(List<Station> stations) {
        for (int i = 0 ; i<stations.size() ; i++){
            final Channel newChannel = new Channel(stations.get(i).getStationSlug()
                    , stations.get(i).getStationAudio()
                    , stations.get(i).getSongInfo());
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
              //  Toast.makeText(myApplicationContext.getApplicationContext() , "channel already selected" , Toast.LENGTH_SHORT ).show();
           // }
            lastItemClicked = position;
        }
    };

    public List<VideoObj> videosFromJson(String jsonString){

        try {
            JSONObject parentObject = new JSONObject(jsonString);

            JSONObject parentObject2 = parentObject.getJSONObject("video");

            JSONArray parentArray = parentObject2.getJSONArray("archive");

            VideoObj tempVideo = null;

            for (int i = 0; i < parentArray.length(); i++) {

                tempVideo = new VideoObj();
                JSONObject finalObject = parentArray.getJSONObject(i);
                tempVideo.setId((finalObject.getString("id")));
                tempVideo.setPublished(finalObject.getString("published"));
                tempVideo.setThumbnail(finalObject.getString("thumbnail"));
                tempVideo.setTitle(finalObject.getString("title"));

                videoList.add(tempVideo);
            }
        } catch (Exception e) {
            Log.e("HereLog", "Json Exception" + e.getMessage());
        }

        return videoList;

    }
    private void flipLayouts() {
        LinearLayout runningFragLL1 = (LinearLayout) findViewById(R.id.runningFragLL1);
        LinearLayout runningFragLL2 = (LinearLayout) findViewById(R.id.runningFragLL2);
        LinearLayout runningFragLL3 = (LinearLayout) findViewById(R.id.runningFragLL3);
        LinearLayout runningFragLL4 = (LinearLayout) findViewById(R.id.runningFragLL4);
        LinearLayout runningFragLL5 = (LinearLayout) findViewById(R.id.runningFragLL5);

        List<LinearLayout> llList = new ArrayList<>();
        llList.add(runningFragLL1);
        llList.add(runningFragLL2);
        llList.add(runningFragLL3);
        llList.add(runningFragLL4);
        llList.add(runningFragLL5);

        for (int i = 0; i < llList.size(); i++) {
            if (isRTL() == false) {
                ArrayList<View> views = new ArrayList<>();
                for (int x = 0; x < llList.get(i).getChildCount(); x++) {
                    views.add(llList.get(i).getChildAt(x));
                }
                llList.get(i).removeAllViews();
                for (int x = views.size() - 1; x >= 0; x--) {
                    llList.get(i).addView(views.get(x));
                }
            }
        }
    }
}
