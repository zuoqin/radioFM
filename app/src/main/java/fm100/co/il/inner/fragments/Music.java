package fm100.co.il.inner.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.util.WheelUtils;
import com.wx.wheelview.widget.WheelView;

import cz.msebera.android.httpclient.Header;
import fm100.co.il.adapters.MyWheelAdapter;
import fm100.co.il.adapters.StationListAdapter;
import fm100.co.il.busEvents.IntBusEvent;
import fm100.co.il.busEvents.StationBusEvent;
import fm100.co.il.busEvents.VideoListBusEvent;
import fm100.co.il.helpers.Downloader;
import fm100.co.il.MainActivity;
import fm100.co.il.busEvents.NewSongBusEvent;
import fm100.co.il.busEvents.NotificationBusEvent;
import fm100.co.il.R;
import fm100.co.il.helpers.SongXMLParser;
import fm100.co.il.adapters.ChannelListAdapter;
import fm100.co.il.models.Channel;
import fm100.co.il.models.MyObject;
import fm100.co.il.models.Song;
import fm100.co.il.models.Station;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;
import com.loopj.android.http.*;

/************************************************
 * the activity of the music fragment
 ************************************************/

public class Music extends Fragment {

	private ImageButton playPauseBtn;
	private boolean playPause;
	private static MediaPlayer mediaPlayer = new MediaPlayer();
	private TextView channelNameTv;
	private Button itunesIb;

	private TextView songNameTv;
	private TextView artistNameTv;

	private ProgressBar progressView;

	private ProgressBar lvProgressView;

	//default channel url
	Channel currentChannel = new Channel();//("100fm" , "http://audio.100fm.co.il/100fmAudio" , "http://digital.100fm.co.il/label/Ch0-100fm.xml" , "http://demo.goufo.co.il/100fm/images/100fmlive.png" );
	Channel lastChannel = new Channel();//("100fm" , "http://audio.100fm.co.il/100fmAudio" , "http://digital.100fm.co.il/label/Ch0-100fm.xml" , "http://demo.goufo.co.il/100fm/images/100fmlive.png");

	//default songs
	Song lastSong = new Song(" " , " ");
	Song currentSong = new Song(" " , " ");
	int firstSong = 0; // checking if it is the 1st song since starting the app in order to set lastSong in motion

	int firstChannel = 0;
	private Boolean intialStage = true;

	private int isPlaying = 0;

	public ArrayList<Channel> channelsArray = new ArrayList<>();
	//ListView channelsLv;
	ChannelListAdapter myCustomAdapter;

	public List<Station> stationList = new ArrayList<>();

	private int destroyed = 0;
	//-------------------------------------------------------- 1 more
	private int appRunning = 0;

	private int lastStationLoading = 0;

	private int listCreated = 0;

	//ReadFileTask task = new ReadFileTask();

	private Boolean isPausedInCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;

	private WheelView myWheelView;
	private View mView;

	private int lastSelectionLoaded = 0;

	private Handler customHandler = new Handler();
	Runnable runnable;

	private int isLoading = 0;

	private Player myPlayer;

	private ImageButton itemTopIb;
	private ImageButton itemBotIb;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.music_fragment, container, false);
		mView = v;
		// flipping Layout the RTL to LTR incase needed
		LinearLayout songBarLL = (LinearLayout) v.findViewById(R.id.songBarLL);
		//task.execute("JsonCo.json");
		EventBus.getDefault().register(this);

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

					setListData(stationList);
					myCustomAdapter = new ChannelListAdapter(getActivity(), MainActivity.channelsArray);
					//channelsLv.setAdapter(myCustomAdapter);
					//channelsLv.setOnItemClickListener(itemClickListener);
					listCreated = 1;
					//progressView.setVisibility(View.INVISIBLE);
					//channelsLv.setBackgroundColor(Color.RED);
					lvProgressView.setVisibility(View.INVISIBLE);
					//initWheel();
					//myWheelView.setWheelAdapter(new MyWheelAdapter(getActivity()));
					//myWheelView.setWheelData(createArrays());
					List<MyObject> newMyObjList = new ArrayList<>();
					for (int i = 0; i < channelsArray.size(); i++) {
						newMyObjList.add(new MyObject(channelsArray.get(i).getChannelLogo()));
					}
					myWheelView.setWheelData(newMyObjList);
					myWheelView.setSelection(0);
					myWheelView.setVisibility(View.VISIBLE);
					itemTopIb.setVisibility(View.VISIBLE);
					itemBotIb.setVisibility(View.VISIBLE);
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


		initWheel();

		/*if(isRTL()==false) {
			ArrayList<View> views = new ArrayList<View>();
			for (int x = 0; x < songBarLL.getChildCount(); x++) {
				views.add(songBarLL.getChildAt(x));
			}
			songBarLL.removeAllViews();
			for (int x = views.size() - 1; x >= 0; x--) {
				songBarLL.addView(views.get(x));
			}
		}
		*/


		pauseOnPhoneCalls();

		Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");
		Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

		//channelsLv = (ListView) v.findViewById(R.id.channelLv);

		channelNameTv = (TextView) v.findViewById(R.id.channelNameTv);
		channelNameTv.setTypeface(custom_font_heb_regular);
		//channelNameTv.setText("No Channel Yet Selected");

		songNameTv = (TextView) v.findViewById(R.id.songNameTv);
		songNameTv.setSelected(true);

		songNameTv.setTypeface(custom_font_eng_light);
		artistNameTv = (TextView) v.findViewById(R.id.artistNameTv);
		artistNameTv.setTypeface(custom_font_eng_light);
		progressView = (ProgressBar) v.findViewById(R.id.progress);

		lvProgressView = (ProgressBar) v.findViewById(R.id.lvProgress);

		itunesIb = (Button) v.findViewById(R.id.itunesIb);
		itunesIb.setVisibility(View.INVISIBLE);
		itunesIb.setOnClickListener(itunesListener);

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
			myCustomAdapter = new ChannelListAdapter(getActivity(), MainActivity.channelsArray);
			//channelsLv.setAdapter(myCustomAdapter);
			//channelsLv.setOnItemClickListener(itemClickListener);

		}

		itemTopIb = (ImageButton) v.findViewById(R.id.itemTopIb);
		itemTopIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myWheelView.smoothScrollBy(-315,1000);
			}
		});
		itemBotIb = (ImageButton) v.findViewById(R.id.itemBotIb);
		itemBotIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myWheelView.smoothScrollBy(315, 1000);
			}
		});

		startSongThread();

		return v;
	}

	private void initWheel() {

		myWheelView = (WheelView) mView.findViewById(R.id.wheelview);
		myWheelView.setVisibility(View.GONE);
		myWheelView.setWheelAdapter(new MyWheelAdapter(getActivity()));
		myWheelView.setWheelData(createArrays());
		//myWheelView.setWheelData(channelsArray);
		myWheelView.setWheelSize(3);
		myWheelView.setSkin(WheelView.Skin.None);
		myWheelView.setWheelClickable(true);
		myWheelView.setSelection(2);
		WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();


		style.backgroundColor = Color.TRANSPARENT;
		style.textColor = Color.TRANSPARENT;
		style.selectedTextColor = Color.BLUE;
		style.selectedTextZoom = 62f;
		style.selectedTextSize = 20;
		myWheelView.setLoop(true);
		myWheelView.setStyle(style);
		myWheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
			@Override
			public void onItemClick(int position, Object o) {
				Toast.makeText(MainActivity.getMyApplicationContext(), "click" + myWheelView.getCurrentPosition() , Toast.LENGTH_SHORT).show();
				//myWheelView.smoothScrollToPosition(position+1 ,0);
				//myWheelView.smoothScrollByOffset(5);
				//myWheelView.smoothScrollBy(300,1000);
			}
		});
		myWheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
			@Override
			public void onItemSelected(final int position, Object o) {
				if (listCreated ==1) {
				runnable = new Runnable() {
					@Override
					public void run() {

							if (myWheelView.getCurrentPosition() == position) {
								//Toast.makeText(MainActivity.getMyApplicationContext(), "scroll" + myWheelView.getCurrentPosition() +" " + position, Toast.LENGTH_SHORT).show();
								if (lastStationLoading == 0) {
									lastSelectionLoaded = position;
									//event = new NotificationBusEvent("play");
									firstSong = 0;
									NotificationBusEvent event = null;
									firstChannel = 1;
									currentChannel = channelsArray.get(position);
									if (!Objects.equals(currentChannel.getChannelName(), lastChannel.getChannelName())) {
										event = new NotificationBusEvent("play");
										EventBus.getDefault().post(event);
										//channelNameTv.setText("Current Channel Selected : " + channelsArray.get(position).getChannelName());
										playPauseBtn.setImageResource(R.drawable.button_pause);
										lastChannel.setChannelName(currentChannel.getChannelName());
										lastChannel.setChannelUrl(currentChannel.getChannelUrl());
										mediaPlayer.reset();
										myPlayer = new Player();
										myPlayer.execute(currentChannel.getChannelUrl());
										isPlaying = 1;
									}/* ----- if re-chosing the station it pause\play based on last state -----
									 else {
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
									}*/


								} else {
									//if (lastSelectionLoaded == position){
									//	isLoading = 0;
									//}
									if (isLoading == 0) {
										Toast.makeText(MainActivity.getMyApplicationContext(), "please waite while loading last station selected", Toast.LENGTH_LONG).show();
										myWheelView.setSelection(lastSelectionLoaded);
										//isLoading = 1;
									}
									//myPlayer.cancel(true);
								}

							}
						}
					};
					customHandler.postDelayed(runnable, 1500);
				}
			}
		});

	}


	private List<MyObject> createArrays() {
		List<MyObject> list = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			list.add(new MyObject(null));
		}
		return list;
	}

	private ArrayList<WheelData> createDatas() {
		ArrayList<WheelData> list = new ArrayList<WheelData>();
		WheelData item;
		for (int i = 0; i < 20; i++) {
			item = new WheelData();
			item.setId(R.mipmap.ic_launcher);
			item.setName((i < 10) ? ("0" + i) : ("" + i));
			list.add(item);
		}
		return list;
	}

	private void setListData(List<Station> stations) {
		for (int i = 0 ; i<stations.size() ; i++){
			final Channel newChannel = new Channel(stations.get(i).getStationSlug()
					, stations.get(i).getStationAudio()
					, stations.get(i).getSongInfo(), stations.get(i).getStationLogo());
			channelsArray.add(newChannel);
		}
	}

	//Setting Click Listeners
	View.OnClickListener itunesListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(lastSong != null) {
				Toast.makeText(MainActivity.getMyApplicationContext(), "artist: " + lastSong.getArtist()
						+ " song: " + lastSong.getSongName(), Toast.LENGTH_LONG).show();
				Uri uri = Uri.parse("https://itunes.apple.com/search?limit=1&country=IL&term=" + "in the end" + "linkin park"); // missing 'http://' will cause crashed
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			else {
				Toast.makeText(MainActivity.getMyApplicationContext() , "null" , Toast.LENGTH_SHORT).show();
			}

		}
	};

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
							myPlayer = new Player();
							myPlayer.execute(currentChannel.getChannelUrl());

						} else {
							myPlayer = new Player();
							myPlayer.execute(currentChannel.getChannelUrl());
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
								Log.e("mynewlog", "call ended");
								Log.e("mynewlog", "is playing " + isPlaying);
								if(isPlaying == 1)
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
		//	channelsLv.smoothScrollToPosition(position);
			if (lastStationLoading == 0) {
				//event = new NotificationBusEvent("play");
				firstSong = 0;
				NotificationBusEvent event = null;
				firstChannel = 1;
				currentChannel = channelsArray.get(position);
				if (!Objects.equals(currentChannel.getChannelName(), lastChannel.getChannelName())) {
					event = new NotificationBusEvent("play");
					EventBus.getDefault().post(event);
					//channelNameTv.setText("Current Channel Selected : " + channelsArray.get(position).getChannelName());
					playPauseBtn.setImageResource(R.drawable.button_pause);
					lastChannel.setChannelName(currentChannel.getChannelName());
					lastChannel.setChannelUrl(currentChannel.getChannelUrl());
					mediaPlayer.reset();
					myPlayer = new Player();
					myPlayer.execute(currentChannel.getChannelUrl());
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
			Boolean prepared= false;
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
		customHandler.removeCallbacks(runnable);
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
					myWheelView.setSelection(intEvent.getintBusEvent());
				//	channelNameTv.setText("Current Channel Selected : " + channelsArray.get(intEvent.getintBusEvent()).getChannelName());
					playPauseBtn.setImageResource(R.drawable.button_pause);
					lastChannel.setChannelName(currentChannel.getChannelName());
					lastChannel.setChannelUrl(currentChannel.getChannelUrl());
					mediaPlayer.reset();
					myPlayer = new Player();
					myPlayer.execute(currentChannel.getChannelUrl());
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
		myCustomAdapter = new ChannelListAdapter(getActivity(), MainActivity.channelsArray);
		//channelsLv.setAdapter(myCustomAdapter);

		//channelsLv.setOnItemClickListener(itemClickListener);
		listCreated = 1;
	}

	// JSON RELATED ASYNCTASKS
	/*public class ReadFileTask extends AsyncTask<String, Void, String> {

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
			myCustomAdapter = new ChannelListAdapter(getActivity(), MainActivity.channelsArray);
			//channelsLv.setAdapter(myCustomAdapter);
			//channelsLv.setOnItemClickListener(itemClickListener);
			listCreated = 1;
			//progressView.setVisibility(View.INVISIBLE);
			//channelsLv.setBackgroundColor(Color.RED);
			lvProgressView.setVisibility(View.INVISIBLE);
			//initWheel();
			//myWheelView.setWheelAdapter(new MyWheelAdapter(getActivity()));
			//myWheelView.setWheelData(createArrays());
			List<MyObject> newMyObjList = new ArrayList<>();
			for (int i = 0; i < channelsArray.size(); i++) {
				newMyObjList.add(new MyObject(channelsArray.get(i).getChannelLogo()));
			}
			myWheelView.setWheelData(newMyObjList);
			myWheelView.setSelection(0);
			myWheelView.setVisibility(View.VISIBLE);
			itemTopIb.setVisibility(View.VISIBLE);
			itemBotIb.setVisibility(View.VISIBLE);

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

	}*/

	/*class itunesTask extends AsyncTask<String ,Void,String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(lastSong != null) {
				Toast.makeText(MainActivity.getMyApplicationContext(), "artist: " + lastSong.getArtist()
						+ " song: " + lastSong.getSongName(), Toast.LENGTH_LONG).show();
				Uri uri = Uri.parse("https://itunes.apple.com/search?limit=1&country=IL&term=" + "in the end" + "linkin park"); // missing 'http://' will cause crashed
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			else {
				Toast.makeText(MainActivity.getMyApplicationContext() , "null" , Toast.LENGTH_SHORT).show();
			}
			String path=MainActivity.getMyApplicationContext().getFilesDir().getAbsolutePath()+"/JsonCo.json";
			File file = new File ( path );
			if ( file.exists() ){

			}else{
				Toast.makeText(MainActivity.getMyApplicationContext() ,
						"please download the file to countinue" , Toast.LENGTH_SHORT).show();
			}

		}

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
			super.onPostExecute(aBoolean);
		}
	}*/
}