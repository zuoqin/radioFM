package onn.android.hpuser.rad100fm.inner.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import onn.android.hpuser.rad100fm.adapters.StationListAdapter;
import onn.android.hpuser.rad100fm.busEvents.IntBusEvent;
import onn.android.hpuser.rad100fm.busEvents.StationBusEvent;
import onn.android.hpuser.rad100fm.busEvents.VideoListBusEvent;
import onn.android.hpuser.rad100fm.helpers.Downloader;
import onn.android.hpuser.rad100fm.MainActivity;
import onn.android.hpuser.rad100fm.busEvents.NewSongBusEvent;
import onn.android.hpuser.rad100fm.busEvents.NotificationBusEvent;
import onn.android.hpuser.rad100fm.R;
import onn.android.hpuser.rad100fm.helpers.SongXMLParser;
import onn.android.hpuser.rad100fm.adapters.ChannelListAdapter;
import onn.android.hpuser.rad100fm.models.Channel;
import onn.android.hpuser.rad100fm.models.Song;
import onn.android.hpuser.rad100fm.models.Station;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/************************************************
 * the activity of the music fragment
 ************************************************/

public class Music extends Fragment {

	private ImageButton playPauseBtn;
	private boolean playPause;
	private static MediaPlayer mediaPlayer = new MediaPlayer();
	private TextView channelNameTv;
	private ImageButton itunesIb;

	private TextView songNameTv;
	private TextView artistNameTv;

	private ProgressBar progressView;

	//default channel url
	Channel currentChannel = new Channel("Special" , "http://213.8.143.168:80/100Special" , "http://www.fmplayer.co.il/NowPlaying/Ch14-Special.xml");
	Channel lastChannel = new Channel("100fm" , "http://audio.100fm.co.il/100fmAudio" , "http://digital.100fm.co.il/label/Ch0-100fm.xml");

	//default songs
	Song lastSong = new Song(" " , " ");
	Song currentSong = new Song(" " , " ");
	int firstSong = 0; // checking if it is the 1st song since starting the app in order to set lastSong in motion

	int firstChannel = 0;
	private Boolean intialStage = true;

	private int isPlaying = 0;

	public ArrayList<Channel> channelsArray = new ArrayList<>();
	ListView channelsLv;
	ChannelListAdapter myCustomAdapter;

	public List<Station> stationList = new ArrayList<>();

	private int destroyed = 0;
	//-------------------------------------------------------- 1 more
	private int appRunning = 0;

	private int lastStationLoading = 0;

	private int listCreated = 0;

	ReadFileTask task = new ReadFileTask();

	private Boolean isPausedInCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.music_fragment, container, false);
		// flipping Layout the RTL to LTR incase needed
		LinearLayout songBarLL = (LinearLayout) v.findViewById(R.id.songBarLL);
		EventBus.getDefault().register(this);
		task.execute("JsonCo.json");
		if(isRTL()==false) {
			ArrayList<View> views = new ArrayList<View>();
			for (int x = 0; x < songBarLL.getChildCount(); x++) {
				views.add(songBarLL.getChildAt(x));
			}
			songBarLL.removeAllViews();
			for (int x = views.size() - 1; x >= 0; x--) {
				songBarLL.addView(views.get(x));
			}
		}

		pauseOnPhoneCalls();

		channelsLv = (ListView) v.findViewById(R.id.channelLv);

		channelNameTv = (TextView) v.findViewById(R.id.channelNameTv);
		channelNameTv.setText("No Channel Yet Selected");

		songNameTv = (TextView) v.findViewById(R.id.songNameTv);
		artistNameTv = (TextView) v.findViewById(R.id.artistNameTv);

		progressView = (ProgressBar) v.findViewById(R.id.progress);
		itunesIb = (ImageButton) v.findViewById(R.id.itunesIb);
		itunesIb.setVisibility(View.INVISIBLE);

		playPauseBtn = (ImageButton) v.findViewById(R.id.play_pause_btn);
		if(mediaPlayer == null){
			mediaPlayer = new MediaPlayer();
		}
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		playPauseBtn.setOnClickListener(playPauseListener);
		if (isPlaying == 1) {
			playPauseBtn.setImageResource(R.drawable.button_pause);
		} else {
			playPauseBtn.setImageResource(R.drawable.button_play);
		}

		if (listCreated == 0 && channelsArray!=null){
			myCustomAdapter = new ChannelListAdapter(getActivity(), channelsArray);
			channelsLv.setAdapter(myCustomAdapter);
			channelsLv.setOnItemClickListener(itemClickListener);
		}

		startSongThread();

		return v;
	}

	private void setListData(List<Station> stations) {
		for (int i = 0 ; i<stations.size() ; i++){
			final Channel newChannel = new Channel(stations.get(i).getStationSlug()
					, stations.get(i).getStationAudio()
					, stations.get(i).getSongInfo());
			channelsArray.add(newChannel);
		}
	}

	//Setting Click Listeners

	View.OnClickListener playPauseListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {


			// is it on play or pause and act acordingly
			if (firstChannel == 0) {
				Toast.makeText(getActivity(), "No Channel Selected" , Toast.LENGTH_SHORT).show();
			} else {
				NotificationBusEvent event = null;
				if (isPlaying == 0) {
					event = new NotificationBusEvent("play");
					playPauseBtn.setImageResource(R.drawable.button_pause);
					if (!Objects.equals(currentChannel.getChannelName(), lastChannel.getChannelName())) {
						if (currentChannel != null) {
							lastChannel.setChannelName(currentChannel.getChannelName());
							lastChannel.setChannelUrl(currentChannel.getChannelUrl());
							mediaPlayer.reset();
							new Player().execute(currentChannel.getChannelUrl());

						} else {
							new Player().execute(currentChannel.getChannelUrl());
						}
					} else {
						mediaPlayer.start();
					}
					isPlaying =1;
				} else {
					event = new NotificationBusEvent("pause");
					playPauseBtn.setImageResource(R.drawable.button_play);
					if (mediaPlayer.isPlaying())
						mediaPlayer.pause();
					isPlaying = 0;
				}
				EventBus.getDefault().post(event);
			}
		}
	};

	private void pauseOnPhoneCalls() {
		telephonyManager = (TelephonyManager) MainActivity.getMyApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state){
					case TelephonyManager.CALL_STATE_OFFHOOK:

					case TelephonyManager.CALL_STATE_RINGING:
						if (mediaPlayer != null){
							pauseMedia();
							isPausedInCall = true;
						}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						if (mediaPlayer != null){
							if(isPausedInCall){
								isPausedInCall = false;
								Log.e("mynewlog" , "call ended");
								playMedia();
							}
						}
						break;
				}
			}
		};

		telephonyManager.listen(phoneStateListener ,PhoneStateListener.LISTEN_CALL_STATE);

	}

	private void playMedia() {
			mediaPlayer.start();
	}

	private void pauseMedia() {
		if(mediaPlayer.isPlaying()){
			mediaPlayer.pause();
		}
	}

	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (lastStationLoading == 0) {
				//event = new NotificationBusEvent("play");
				firstSong = 0;
				NotificationBusEvent event = null;
				firstChannel = 1;
				currentChannel = channelsArray.get(position);
				if (!Objects.equals(currentChannel.getChannelName(), lastChannel.getChannelName())) {
					event = new NotificationBusEvent("play");
					EventBus.getDefault().post(event);
					channelNameTv.setText("Current Channel Selected : " + channelsArray.get(position).getChannelName());
					playPauseBtn.setImageResource(R.drawable.button_pause);
					lastChannel.setChannelName(currentChannel.getChannelName());
					lastChannel.setChannelUrl(currentChannel.getChannelUrl());
					mediaPlayer.reset();
					new Player().execute(currentChannel.getChannelUrl());
					isPlaying = 1;
				} else {
					if (isPlaying == 0) {
						mediaPlayer.start();
						playPauseBtn.setImageResource(R.drawable.button_pause);
						event = new NotificationBusEvent("play");
						isPlaying = 1;
					} else {
						event = new NotificationBusEvent("pause");
						playPauseBtn.setImageResource(R.drawable.button_play);
						if (mediaPlayer.isPlaying())
							mediaPlayer.pause();
						isPlaying = 0;
					}
					EventBus.getDefault().post(event);
				}
			} else {
				Toast.makeText(MainActivity.getMyApplicationContext(), "please waite while loading last station selected", Toast.LENGTH_LONG).show();
			}
		}
	};


	// preparing mediaplayer will take sometime to buffer the content so prepare it inside the background thread and starting it on UI thread.
	class Player extends AsyncTask<String ,Void,Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			lastStationLoading = 1;
			Boolean prepared;
			try {
				mediaPlayer.setDataSource(params[0]);
				mediaPlayer.prepare();
				prepared = true;
			} catch (Exception e){
				Log.d("IllegarArgument", e.getMessage());
				prepared = false;
				e.printStackTrace();
			}
			return prepared;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(null != getActivity() ) {
				Log.d("Prepared", "//" + result);
				mediaPlayer.start();

				intialStage = false;
				lastStationLoading = 0;

				progressView.setVisibility(View.INVISIBLE);
				playPauseBtn.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressView.setVisibility(View.VISIBLE);
			playPauseBtn.setVisibility(View.INVISIBLE);

		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer = null;
		}
		destroyed = 1;
		if (phoneStateListener !=null){
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}

	}

	// getting the xml for the song data and checking for it every second if changed
	private class SongDownloadTask extends AsyncTask<Void, Void, Void> {


		@Override
		protected Void doInBackground(Void... arg0) {
			//Download the file
			try {
				Downloader.DownloadFromUrl(currentChannel.getSongDataUrl(), MainActivity.getMyApplicationContext().openFileOutput("Song.xml", Context.MODE_PRIVATE));
				String path=MainActivity.getMyApplicationContext().getFilesDir().getAbsolutePath()+"/Song.xml";
				File file = new File ( path );
				if ( file.exists() ){

					currentSong = SongXMLParser.getSongFromFile(MainActivity.getMyApplicationContext());

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			itunesIb.setVisibility(View.VISIBLE);
			try {
				//making sure till channel selected nothing is showed
					if (firstChannel == 0) {
						currentSong = new Song( getString(R.string.notidirsttext) , " " );
						EventBus.getDefault().post(new NotificationBusEvent("delete"));
					}
				if (firstSong == 0){
					songNameTv.setText(currentSong.getSongName());
					artistNameTv.setText(currentSong.getArtist());
					lastSong.setSongName(currentSong.getSongName());
					lastSong.setArtist(currentSong.getArtist());
					EventBus.getDefault().post(new NewSongBusEvent(currentSong.getSongName(), currentSong.getArtist()));
					//firstSong = 1;
				}
					if (firstChannel == 1) {
						if (!lastSong.getSongName().equals(currentSong.getSongName())) {
							Log.e("mynewlog", "its here now");
							songNameTv.setText(currentSong.getSongName());
							artistNameTv.setText(currentSong.getArtist());
							lastSong.setSongName(currentSong.getSongName());
							lastSong.setArtist(currentSong.getArtist());
							EventBus.getDefault().post(new NewSongBusEvent(currentSong.getSongName(), currentSong.getArtist()));
							firstSong = 1;
						}
					}
			}
			catch (Exception e) {
				Log.i("MyLog", "exception postExecute: " + e.getMessage());
			}
		}
	}
	// checking for new song every 1 second
	public void startSongThread(){
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
					try {
						SongDownloadTask download = new SongDownloadTask();
						download.execute();
					} catch (Exception e) {
						Log.i("MyLog", e.getMessage());
					}
				}

		}, 0, 3000);
	}

	@Subscribe
	public void onEvent(NotificationBusEvent event) {
		// set a "pause" notification
		if (event.getNotificationBusMsg().equals("pause")) {
			playPauseBtn.setImageResource(R.drawable.button_play);
			if (mediaPlayer.isPlaying())
				mediaPlayer.pause();
			isPlaying = 0;
		}
		// set a "play" notification
		else if (event.getNotificationBusMsg().equals("play")) {
			playPauseBtn.setImageResource(R.drawable.button_pause);
				mediaPlayer.start();
				isPlaying = 1;
		}
	}

	private boolean isRTL() {
		Locale defLocale = Locale.getDefault();
		return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
	}

	// catching item clicks on drawer pane from main activity by position
	@Subscribe
	public void onIntEvent(IntBusEvent intEvent) {
		currentChannel = channelsArray.get(intEvent.getintBusEvent());
		firstChannel = 1;
			//NotificationBusEvent notiEvent = null;
			if (lastStationLoading == 0) {
				if (!Objects.equals(currentChannel.getChannelName(), lastChannel.getChannelName())) {
					channelNameTv.setText("Current Channel Selected : " + channelsArray.get(intEvent.getintBusEvent()).getChannelName());
					playPauseBtn.setImageResource(R.drawable.button_pause);
					lastChannel.setChannelName(currentChannel.getChannelName());
					lastChannel.setChannelUrl(currentChannel.getChannelUrl());
					mediaPlayer.reset();
					new Player().execute(currentChannel.getChannelUrl());
					isPlaying = 1;
					/*
					NotificationBusEvent event = new NotificationBusEvent("play");
					EventBus.getDefault().post(event);
					*/
				} else {
					Toast.makeText(MainActivity.getMyApplicationContext() , "channel already selected" , Toast.LENGTH_SHORT ).show();
					/*if (isPlaying == 0) {
						mediaPlayer.start();
						playPauseBtn.setImageResource(R.drawable.button_pause);
						isPlaying = 1;
					} else {
						playPauseBtn.setImageResource(R.drawable.button_play);
						if (mediaPlayer.isPlaying())
							mediaPlayer.pause();
						isPlaying = 0;
					}*/
				}
			} else {
				Toast.makeText(MainActivity.getMyApplicationContext(), "please waite while loading last station selected", Toast.LENGTH_LONG).show();
			}

	}
	// catching list of stations from main activity and attaching to list
	@Subscribe
	public void onStationEvent(StationBusEvent stationEvent) {

		setListData(stationEvent.getStationBusEvent());
		myCustomAdapter = new ChannelListAdapter(getActivity(), channelsArray);
		channelsLv.setAdapter(myCustomAdapter);
		channelsLv.setOnItemClickListener(itemClickListener);
		listCreated = 1;
	}

	// JSON RELATED ASYNCTASKS
	public class ReadFileTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String temp="";
			try {
				FileInputStream fin = MainActivity.getMyApplicationContext().openFileInput(params[0]);
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
			stationList = StationsFromJson(result);
			setListData(stationList);
			myCustomAdapter = new ChannelListAdapter(getActivity(), channelsArray);
			channelsLv.setAdapter(myCustomAdapter);
			channelsLv.setOnItemClickListener(itemClickListener);
			listCreated = 1;
		}
	}

	public List<Station> StationsFromJson(String jsonString){

		try {
			JSONObject parentObject = new JSONObject(jsonString);

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
}