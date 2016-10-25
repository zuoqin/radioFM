package com.nxcast.stations.il.fm100.inner.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nxcast.stations.il.fm100.RemoteControlReceiver;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.widget.WheelView;

import cz.msebera.android.httpclient.Header;
import com.nxcast.stations.il.fm100.adapters.MyWheelAdapter;
import com.nxcast.stations.il.fm100.busEvents.IntBusEvent;
import com.nxcast.stations.il.fm100.busEvents.StationBusEvent;
import com.nxcast.stations.il.fm100.fragments.MyHome;
import com.nxcast.stations.il.fm100.helpers.DownloadImageTask;
import com.nxcast.stations.il.fm100.helpers.Downloader;
import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.busEvents.NewSongBusEvent;
import com.nxcast.stations.il.fm100.busEvents.NotificationBusEvent;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.helpers.ScheduleXMLParser;
import com.nxcast.stations.il.fm100.helpers.SongXMLParser;
import com.nxcast.stations.il.fm100.adapters.ChannelListAdapter;
import com.nxcast.stations.il.fm100.models.Channel;
import com.nxcast.stations.il.fm100.models.MyObject;
import com.nxcast.stations.il.fm100.models.ScheduleItem;
import com.nxcast.stations.il.fm100.models.Song;
import com.nxcast.stations.il.fm100.models.Station;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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

	private ImageView imgRound;
	private ImageView imgCover;
	private ImageView imgDarken;

	String lastCoverImage = "";

	//default channel url
	Channel currentChannel = new Channel();//("100fm" , "http://audio.100fm.co.il/100fmAudio" , "http://digital.100fm.co.il/label/Ch0-100fm.xml" , "http://demo.goufo.co.il/100fm/images/100fmlive.png" );
	Channel lastChannel = new Channel();//("100fm" , "http://audio.100fm.co.il/100fmAudio" , "http://digital.100fm.co.il/label/Ch0-100fm.xml" , "http://demo.goufo.co.il/100fm/images/100fmlive.png");

	//default songs
	Song lastSong = new Song(" " , " ");
	Song currentSong = new Song(" " , " ");
	int firstSong = 0; // checking if it is the 1st song since starting the app in order to set lastSong in motion

	int firstChannel = 0;
	private Boolean intialStage = true;

	public int isPlaying = 0;

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

	private int lastSelectionLoaded = -1;

	private Handler customHandler = new Handler();
	Runnable runnable;

	private int isLoading = 0;

	//private Player myPlayer;

	LinearLayout rLayout;
	ImageButton btnMenu = null;

	private List<ScheduleItem> scheduleItemList = new ArrayList<>();

	RelativeLayout flying;
	public ArrayList<View> monkeys = new ArrayList<>();
	int monkeysIndex = 0;
	int flyingColor = 0xFFF8F301;
	Timer timer_flying = null;

	private int wheelLastPos;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.music_fragment, container, false);
		mView = v;
		// flipping Layout the RTL to LTR incase needed
		RelativeLayout songBarLL = (RelativeLayout) v.findViewById(R.id.songBarLL);
		//task.execute("JsonCo.json");
		EventBus.getDefault().register(this);

		initWheel();

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

		imgCover = (ImageView) v.findViewById(R.id.imgCover);
		imgDarken = (ImageView) v.findViewById(R.id.imgDarken);
		imgRound = (ImageView) v.findViewById(R.id.imgRound);

		rLayout = (LinearLayout) v.findViewById(R.id.playRotate);

		flying = (RelativeLayout) v.findViewById(R.id.flyingMonkeys);

		if (progressView != null) {
			progressView.setIndeterminate(true);
			progressView.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
		}

		playPauseBtn = (ImageButton) v.findViewById(R.id.play_pause_btn);
		if(mediaPlayer == null){
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer mp){
					mediaPlayer.start();
					progressView.setVisibility(View.INVISIBLE);
					playPauseBtn.setVisibility(View.VISIBLE);
					imgRound.setVisibility(View.VISIBLE);
				}
			});
		}
		playPauseBtn.setOnClickListener(playPauseListener);
		if (isPlaying == 1) {
			playPauseBtn.setImageResource(R.drawable.stop);
		} else {
			playPauseBtn.setImageResource(R.drawable.play);
		}

		if (listCreated == 0 && channelsArray!=null){
			myCustomAdapter = new ChannelListAdapter(getActivity(), MainActivity.channelsArray);
		}

		btnMenu = (ImageButton) v.findViewById(R.id.btnMenu);
		btnMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyHome activity = (MyHome) getParentFragment();
				activity.openSubmenu();
			}
		});

		ImageButton btnLike = (ImageButton) v.findViewById(R.id.btnLike);
		btnLike.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = "http://digital.100fm.co.il/";
				//sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));
			}
		});


		ImageButton btn100 = (ImageButton) v.findViewById(R.id.img100fm);
		btn100.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playStation(0);
				myWheelView.smoothScrollToPosition(0);
			}
		});

		for( int i = 0; i < 6; i++ ) {
			Random r = new Random();
			View myButton = new View(getContext());
			int s = r.nextInt(60) + 40;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(s, s);
			params.rightMargin = 0;
			//params.topMargin = 1500;
			myButton.setLayoutParams(params);
			myButton.setBackgroundColor(flyingColor);
			myButton.setAlpha(.8f);
			myButton.setVisibility(View.INVISIBLE);
			flying.addView(myButton);
			monkeys.add(myButton);
		}

		startSongThread();



		return v;
	}

	public void setStationData(JSONObject obj) throws JSONException {
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
		listCreated = 1;
		lvProgressView.setVisibility(View.INVISIBLE);
		List<MyObject> newMyObjList = new ArrayList<>();
		for (int i = 0; i < channelsArray.size(); i++) {
			newMyObjList.add(new MyObject(channelsArray.get(i).getChannelLogo()));
		}
		myWheelView.setWheelData(newMyObjList);
		myWheelView.setSelection(0);
		myWheelView.setVisibility(View.VISIBLE);
	}

	private void initWheel() {
		myWheelView = (WheelView) mView.findViewById(R.id.wheelview);
		myWheelView.setWheelAdapter(new MyWheelAdapter(getActivity()));
		myWheelView.setWheelData(createArrays());
		myWheelView.setWheelSize(7);
		myWheelView.setSkin(WheelView.Skin.None);
		myWheelView.setWheelClickable(true);
		myWheelView.setSelection(0);
		WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();


		style.backgroundColor = Color.TRANSPARENT;
		style.textColor = Color.TRANSPARENT;
		style.selectedTextColor = Color.YELLOW;
		style.selectedTextZoom = 62f;
		style.selectedTextSize = 20;
		myWheelView.setLoop(true);
		myWheelView.setStyle(style);

		myWheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
			@Override
			public void onItemClick(int position, Object o) {
				Log.i("100fm", "onItemClick " + position + " last " + lastSelectionLoaded);
				myWheelView.smoothScrollToPosition(position);
			}
		});
		myWheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
			@Override
			public void onItemSelected(int position, Object o) {
				//Log.i("100fm", "onItemSelected " + position);
				playStation(position);
			}
		});
	}

	private void playStation(int position) {
		if( channelsArray.size() == 0 ) {
			return;
		}

		lastSelectionLoaded = position;

		NotificationBusEvent event = null;

		currentChannel = channelsArray.get(position);

		playPauseBtn.setImageResource(R.drawable.stop);
		lastChannel.setChannelName(currentChannel.getChannelName());
		lastChannel.setChannelUrl(currentChannel.getChannelUrl());
		event = new NotificationBusEvent("play");
		EventBus.getDefault().post(event);
	}
	private void stopStation() {
		NotificationBusEvent event = new NotificationBusEvent("pause");
		EventBus.getDefault().post(event);
		rLayout.clearAnimation();
		if( timer_flying != null ) {
			timer_flying.cancel();
			timer_flying = null;

			for( int i = 0; i < monkeys.size(); i++ ) {
				monkeys.get(i).clearAnimation();
				monkeys.get(i).setVisibility(View.INVISIBLE);
			}
		}
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

	View.OnClickListener playPauseListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i("100fm", "playPauseListener " + isPlaying + " " + lastSelectionLoaded);
			if (isPlaying == 0) {
				playStation(lastSelectionLoaded);
			} else {
				stopStation();
			}
		}
	};

	public boolean isPlaying() {
		return isPlaying == 1;
	}

	private void pauseOnPhoneCalls() {
		telephonyManager = (TelephonyManager) MainActivity.getMyApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state){
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						if (mediaPlayer != null){
							if(mediaPlayer.isPlaying()){
								stopStation();
								isPausedInCall = true;
							}
					}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						if(isPausedInCall){
							isPausedInCall = false;
							playStation(lastSelectionLoaded);
						}
						break;
				}
			}
		};

		telephonyManager.listen(phoneStateListener ,PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//EventBus.getDefault().post(new NotificationBusEvent("delete"));
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

	private void reload100fmName() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
		int hours = date.getHours() * 100;

		ScheduleItem itemCurrent = null;
		for (int i=0 ; i<scheduleItemList.size() ; i++) {
			ScheduleItem item = scheduleItemList.get(i);
			if( day.equals(item.getProgramDay()) ) {
				int h = Integer.parseInt( item.getProgramStartHoure().replace(":","") );
				if( hours > h ) {
					itemCurrent = item;
					//Log.i("100fm", "reload100fmName | " + item.getProgramName() + " | " + item.getProgramAutor() );
				}
			}
		}
		if( itemCurrent != null ) {
			currentSong = new Song( itemCurrent.getProgramName(), itemCurrent.getProgramAutor() );
			songNameTv.setText(currentSong.getSongName());
			artistNameTv.setText(currentSong.getArtist());

			if( !itemCurrent.getProgramImage().isEmpty() ) {
				showCover(itemCurrent.getProgramImage());
			} else {
				hideCover();
			}

			lastSong.setSongName(itemCurrent.getProgramName());
			lastSong.setArtist(itemCurrent.getProgramAutor());
			EventBus.getDefault().post(new NewSongBusEvent(itemCurrent.getProgramName(), itemCurrent.getProgramAutor()));
		}
	}

	private void showCover(String img) {
		if( lastCoverImage.equals(img) ) {
			return;
		}
		Log.i("100fm", "showCover : " + img );
		lastCoverImage = img;

		if( imgDarken.getVisibility() != View.VISIBLE ) {
			Animation a = new AlphaAnimation(0.00f, 1.00f);

			a.setDuration(500);
			a.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					imgDarken.setVisibility(View.VISIBLE);
				}
			});

			imgDarken.startAnimation(a);

			Animation b = new AlphaAnimation(1.00f, 0.00f);

			b.setDuration(200);
			b.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					imgCover.setVisibility(View.INVISIBLE);
				}
			});

			imgCover.startAnimation(b);
		}
		new DownloadImageTask(imgCover, getContext()).execute(img);
	}

	private void hideCover() {
		flyingColor = 0xFFF8F301;
		if( imgDarken.getVisibility() != View.INVISIBLE ) {
			Animation a = new AlphaAnimation(1.00f, 0.00f);

			a.setDuration(500);
			a.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					imgCover.setVisibility(View.INVISIBLE);
					imgCover.setImageResource(android.R.color.transparent);
					imgDarken.setVisibility(View.INVISIBLE);
				}
			});

			imgCover.startAnimation(a);
			imgDarken.startAnimation(a);
		}
	}
	// getting the xml for the song data and checking for it every second if changed
	private class ScheduleDownloadTask extends AsyncTask<Void, Void, List<ScheduleItem>> {


		@Override
		protected List<ScheduleItem> doInBackground(Void... arg0) {
			//Download the file
			try {
				Downloader.DownloadFromUrl("http://www.100fm.co.il/smartphoneXML/programs.aspx", MainActivity.getMyApplicationContext().openFileOutput("Schedule.xml", Context.MODE_PRIVATE));
				String path = MainActivity.getMyApplicationContext().getFilesDir().getAbsolutePath() + "/Schedule.xml";
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
			reload100fmName();
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
			//itunesIb.setVisibility(View.VISIBLE);
			try {
					if( !currentSong.getArtist().isEmpty() && !currentSong.getArtist().equals(" ") && !lastSong.getSongName().equals( currentSong.getSongName() ) ) {
						songNameTv.setText(currentSong.getSongName());
						artistNameTv.setText(currentSong.getArtist());
						lastSong.setSongName(currentSong.getSongName());
						lastSong.setArtist(currentSong.getArtist());
						EventBus.getDefault().post(new NewSongBusEvent(currentSong.getSongName(), currentSong.getArtist()));

						String key = currentSong.getSongName() + " " + currentSong.getArtist();

						AsyncHttpClient client = new AsyncHttpClient();
						client.get("https://itunes.apple.com/search?limit=1&country=IL&term=" + URLEncoder.encode(key, "utf-8"), new AsyncHttpResponseHandler() {


							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								try {
									JSONObject obj = new JSONObject(new String(responseBody));

									if( obj.getInt("resultCount") > 0 ) {
										JSONObject song = (JSONObject) obj.getJSONArray("results").get(0);

										Log.d("fm100", "artworkUrl100 : " + song.getString("artworkUrl100").replace("100x100bb", "400x400bb") );

										showCover(song.getString("artworkUrl100").replace("100x100bb", "400x400bb"));
									} else {
										hideCover();
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

							}
						});
					}
			}
			catch (Exception e) {
				Log.i("MyLog", "exception postExecute: " + e.getMessage());
			}
		}
	}

	// checking for new song every 1 second
	public void startSongThread(){
		/*Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				reloadSongName();
			}
		}, 0, 15000);*/
		customHandler.postDelayed(updateSongName, 0);
	}

	private void reloadSongName() {
		if( lastSelectionLoaded == 0 ) {
			if( scheduleItemList.size() == 0 ) {
				ScheduleDownloadTask download = new ScheduleDownloadTask();
				download.execute();
			} else {
				reload100fmName();
			}
		} else {
			SongDownloadTask download = new SongDownloadTask();
			download.execute();
		}
	}

	private Runnable updateSongName = new Runnable() {
		public void run() {
			Log.i("100fm", "updateSongName");
			reloadSongName();
			customHandler.postDelayed(updateSongName, 15000);
		}
	};

	public void startFlyingMonkeys() {
		if (timer_flying != null) {
			timer_flying.cancel();
		}
		timer_flying = new Timer();
		timer_flying.schedule(new TimerTask() {
			@Override
			public void run() {
				if( getActivity() != null ) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Random r = new Random();

							if( imgCover.getDrawable() != null && imgCover.getVisibility() == View.VISIBLE ) {
								Bitmap bitmap = ((BitmapDrawable)imgCover.getDrawable()).getBitmap();
								flyingColor = bitmap.getPixel(0,0);
							}

							if( getContext() != null ) {
								/*View myButton = new View(getContext());
								int s = r.nextInt(60) + 40;
								RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(s, s);
								params.rightMargin = r.nextInt(flying.getWidth());
								params.topMargin = 0;
								myButton.setLayoutParams(params);
								myButton.setBackgroundColor(flyingColor);
								myButton.setAlpha(.5f);
								flying.addView(myButton);
								monkeys.add(myButton);*/
								View myButton = monkeys.get(monkeysIndex);
								myButton.setBackgroundColor(flyingColor);

								monkeysIndex = (monkeysIndex + 1) % monkeys.size();
								//Log.i("100fm", "monkeysIndex " + monkeysIndex + " size " + monkeys.size());

								AnimationSet snowMov1 = new AnimationSet(true);

								RotateAnimation rotate1 = new RotateAnimation(-90, r.nextInt(180), myButton.getWidth(), myButton.getHeight());
								rotate1.setDuration(3000);
								//rotate1.setRepeatCount(1);
								//rotate1.setInterpolator(new AccelerateInterpolator());
								snowMov1.addAnimation(rotate1);

								int x = r.nextInt(flying.getWidth());
								int y = flying.getHeight();
								TranslateAnimation trans1 = new TranslateAnimation(x, x, y + 150, -150);
								//TranslateAnimation trans1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF, x, Animation.ABSOLUTE, y, Animation.ABSOLUTE, 0);

								trans1.setDuration(3000);
								snowMov1.addAnimation(trans1);

								myButton.setVisibility(View.VISIBLE);
								myButton.startAnimation(snowMov1);
								//myButton.startAnimation(trans1);
								//myButton.setVisibility(View.VISIBLE);
							}
						}
					});
				}
			}
		}, 0, 500);
	}

	@Subscribe
	public void onEvent(NotificationBusEvent event) throws IOException {
		Log.i("100fm", "event.getNotificationBusMsg() " + event.getNotificationBusMsg() + " isPlaying " + isPlaying);

		if (event.getNotificationBusMsg().equals("pause") ) {
			playPauseBtn.setImageResource(R.drawable.play);
			if (mediaPlayer.isPlaying())
				mediaPlayer.stop();

			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;

			isPlaying = 0;
			rLayout.clearAnimation();
			if( timer_flying != null ) {
				timer_flying.cancel();
				timer_flying = null;

				for( int i = 0; i < monkeys.size(); i++ ) {
					monkeys.get(i).clearAnimation();
					monkeys.get(i).setVisibility(View.INVISIBLE);
				}
			}
		} else if (event.getNotificationBusMsg().equals("play")) {
			playPauseBtn.setImageResource(R.drawable.stop);
			if( mediaPlayer != null ) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.i("100fm", "onPrepared MediaPlayer");
					mp.start();
					progressView.setVisibility(View.INVISIBLE);
					playPauseBtn.setVisibility(View.VISIBLE);
					imgRound.setVisibility(View.VISIBLE);
				}
			});
			mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					Log.i("100fm", "Buffering " + percent);
				}
			});
			mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.i("100fm", "onError MediaPlayer");
					mp.release();
					progressView.setVisibility(View.INVISIBLE);
					playPauseBtn.setVisibility(View.VISIBLE);
					imgRound.setVisibility(View.VISIBLE);
					return false;
				}
			});
			mediaPlayer.setDataSource(currentChannel.getChannelUrl());
			mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
			progressView.setVisibility(View.VISIBLE);
			playPauseBtn.setVisibility(View.INVISIBLE);
			imgRound.setVisibility(View.INVISIBLE);

			isPlaying = 1;
			reloadSongName();

			Animation an = new RotateAnimation(0.0f, 360.0f, rLayout.getWidth()/2, rLayout.getHeight()/2);

			an.setDuration(2000);
			an.setRepeatCount(-1);
			an.setFillAfter(false);              // DO NOT keep rotation after animation
			an.setFillEnabled(true);             // Make smooth ending of Animation
			an.setInterpolator(new LinearInterpolator());
			an.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}

				@Override
				public void onAnimationRepeat(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					rLayout.setRotation(0.0f);      // Make instant rotation when Animation is finished
				}
			});

			// Aply animation to image view
			//rLayout.setAnimation(an);
			rLayout.startAnimation(an);

			startFlyingMonkeys();
		} else if (event.getNotificationBusMsg().equals("next")) {
			playStation( (lastSelectionLoaded + 1) % stationList.size() );
		} else if (event.getNotificationBusMsg().equals("prev")) {
			int pos = lastSelectionLoaded - 1;
			if( pos < 0 ) pos = stationList.size() - 1;
			playStation( pos );
		}
	}

	private boolean isRTL() {
		Locale defLocale = Locale.getDefault();
		return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
	}

	// catching item clicks on drawer pane from main activity by position
	@Subscribe
	public void onIntEvent(IntBusEvent intEvent) {
		int pos = intEvent.getintBusEvent();
		Log.i("100fm", "playPauseListener " + isPlaying + " " + lastSelectionLoaded + " " + pos);
		playStation(pos);
		myWheelView.smoothScrollToPosition(pos);

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
}