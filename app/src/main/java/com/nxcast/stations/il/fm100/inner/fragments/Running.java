package com.nxcast.stations.il.fm100.inner.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.adapters.RunningListAdapter;
import com.nxcast.stations.il.fm100.fragments.MyHome;
import com.nxcast.stations.il.fm100.fragments.MyRun;
import com.nxcast.stations.il.fm100.helpers.GPSTracker;
import com.nxcast.stations.il.fm100.models.RunShape;
import com.nxcast.stations.il.fm100.models.RunningObject;

import lecho.lib.hellocharts.util.ChartUtils;

import static android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth;

/************************************************
 * The Running Fragment Class , allow the user to see the distance and speed in which he ran and display it in line graph
 ************************************************/

public class Running extends Fragment {

	private TextView distanceTv;
	private TextView speedTv;

	private TextView timerTv;

	private double distance = 0;
	private double seconds = 0.0;

	private View v;

	private Handler customHandler = new Handler();
	//Timer timer = new Timer();
	Location currentLocation;

	private static List<Location> locations = new ArrayList<>();

	private Button startStopBtn;
	private int entryCounter = 0;

	private static List<Integer> xValues = new ArrayList<>();

	private static List<Integer> yValues = new ArrayList<>();

	private static ArrayList<RunningObject> jsonEntries = new ArrayList<>();

	private ListView runningLv;

	private RunningListAdapter myRunningListAdapter;

	private List<RunningObject> runObjList = new ArrayList<>();

	private LinearLayout runContoller;

	private Boolean isPoused = false;

	////////////////

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.running_fragment, container, false);
		// flipping Layout the RTL to LTR incase needed

		/*manager = (LocationManager) getActivity().getSystemService(MainActivity.getMyApplicationContext().LOCATION_SERVICE);

		mSensorManager = (SensorManager) MainActivity.getMyApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);*/

		startStopBtn = (Button) v.findViewById(R.id.startStopBtn);
		distanceTv = (TextView) v.findViewById(R.id.distanceTv);
		speedTv = (TextView) v.findViewById(R.id.speedTv);
		timerTv = (TextView) v.findViewById(R.id.timerTv);
		runningLv = (ListView) v.findViewById(R.id.runningLv);
		runContoller = (LinearLayout) v.findViewById(R.id.runContoller);

		Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");
		Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

		((TextView) v.findViewById(R.id.distanceTitleTv)).setTypeface(custom_font_heb_regular);
		((TextView) v.findViewById(R.id.timerTitleTv)).setTypeface(custom_font_heb_regular);
		((TextView) v.findViewById(R.id.speedTitleTv)).setTypeface(custom_font_heb_regular);

		startStopBtn.setTypeface(custom_font_heb_regular);

		distanceTv.setTypeface(custom_font_heb_regular);
		speedTv.setTypeface(custom_font_heb_regular);
		timerTv.setTypeface(custom_font_heb_regular);

		Gson gson = new Gson();

		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJ", "defaultStringIfNothingFound");
		if (listFromShared == null) {
			Type type = new TypeToken<ArrayList<RunningObject>>() {
			}.getType();
			if (listFromShared != null) {

				jsonEntries = gson.fromJson(listFromShared, type);
				runObjList = jsonEntries;
			}
		} else {
			runObjList = new ArrayList<>();
		}

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
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = "http://digital.100fm.co.il/";
				//sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));
			}
		});

		myRunningListAdapter = new RunningListAdapter(getActivity(), runObjList);
		runningLv.setAdapter(myRunningListAdapter);
		//runningLv.setOnItemClickListener(runningListOnItemClick);

		startStopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				start_run();
			}
		});

		Button btnPause = (Button) v.findViewById(R.id.pouseBtn);
		btnPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( isPoused ) {
					customHandler.postDelayed(updateTimerThread, 0);
				}
				isPoused = !isPoused;
			}
		});

		Button btnStop = (Button) v.findViewById(R.id.stopBtn);
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isPoused = true;
				startStopBtn.setVisibility(View.VISIBLE);
				runContoller.setVisibility(View.GONE);

				stop_run();
			}
		});
		return v;
	}

	private void start_run() {
		Log.i("100fm", "start_run ");
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				currentLocation = location;
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location updates
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
			return;
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

		isPoused = false;
		seconds = 0;
		distance = 0;

		locations.clear();

		customHandler.postDelayed(updateTimerThread, 0);

		startStopBtn.setVisibility(View.GONE);
		runContoller.setVisibility(View.VISIBLE);
	}

	private void stop_run() {

		RunningObject firstRunObj = new RunningObject();

		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm"); //"yyyy-MM-dd HH:mm:ss"

		firstRunObj.setDateAdded( sdf.format(c.getTime()) );
		firstRunObj.setTimeAdded( "" );
		firstRunObj.setRunDistance( String.format("%.02f ק״מ", distance / 1000) );
		firstRunObj.setRunTime( String.format("%02.0f:%02.0f", seconds / 60, seconds % 60) );

		runObjList.add(firstRunObj);

		Gson gson = new Gson();
		String jsonAsString = gson.toJson(runObjList);

		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("runningJ", jsonAsString).commit();

		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJ", "defaultStringIfNothingFound");

		Type type = new TypeToken<ArrayList<RunningObject>>(){}.getType();
		jsonEntries = gson.fromJson(listFromShared, type);

		myRunningListAdapter.notifyDataSetChanged();

		Intent myIntent = new Intent(getContext(), MyRun.class);

		startActivity(myIntent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					start_run();
				}
				return;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//customHandler.removeCallbacks(updateTimerThread);
		//customHandler.removeCallbacks(speedRunnable);
		//mSensorManager.unregisterListener(this, mStepCounterSensor);

	}

	private Runnable updateTimerThread = new Runnable() {
		public void run() {
			seconds += 1.0;
			timerTv.setText(String.format("%02.0f:%02.0f", seconds / 60, seconds % 60));

			if( currentLocation != null ) {
				if( locations.size() > 0 ) {
					Location lastLocation = locations.get( locations.size() - 1);
					distance += lastLocation.distanceTo(currentLocation);
					distanceTv.setText(String.format("%.02f ק״מ", distance / 1000));

					Log.i("100fm", "---------------- " + currentLocation.getAccuracy());

					double sumDistance = 0;
					for( int i = 1; i < 4; i++ ) {
						if( locations.size() - i - 1 >= 0 ) {
							Location Location1 = locations.get( locations.size() - i);
							Location Location2 = locations.get( locations.size() - i - 1);
							sumDistance += Location1.distanceTo(Location2);
							Log.i("100fm", "	" + Location1.distanceTo(Location2));
						}
					}
					Log.i("100fm", "sumDistance " + sumDistance);

					speedTv.setText(String.format("%.02f קמ״ש", sumDistance / 3 * 18 / 5 ));
				}

				locations.add(currentLocation);
			}

			/*timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;
			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			int hours = mins / 60;
			secs = secs % 60;
			int milliseconds = (int) (updatedTime % 1000);

			timerText = "" + String.format("%02d", hours) + ":"
					+ String.format("%02d", mins) + ":"
					+ String.format("%02d", secs);

			timerTv.setText(timerText);
			if(secs %5 ==0){
				tentime = timerText;
				timetenList.add(tentime);
				addEntry(speed);
			}
			//if needed milisecs + String.format("%03d", milliseconds));

			customHandler.postDelayed(this, 1000);*/

			if( !isPoused ) {
				customHandler.postDelayed(this, 1000);
			}
		}
	};

	/*private void addEntry(float currentSpeed) {

		entryCounter++;
		runObjList.get(runObjList.size()-1).getObjectEntries().add(new Entry(entryCounter, currentSpeed));

		//LineData data = lineGraph.getData();


	}*/


	/*private Runnable speedRunnable = new Runnable() {
		public void run() {
			float stepSpeed = (((currentStepDistance - lastStepDistance)/3)*18)/5;
			if (stepSpeed != 0) {
				speedTv.setText(String.format("%.2f", stepSpeed));
				speed = stepSpeed;
			}
			lastStepDistance = currentStepDistance;
			customHandler.postDelayed(this, 5000);
		}

	};*/

}
