package com.nxcast.stations.il.fm100.inner.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.adapters.RunningListAdapter;
import com.nxcast.stations.il.fm100.helpers.GPSTracker;
import com.nxcast.stations.il.fm100.models.RunShape;
import com.nxcast.stations.il.fm100.models.RunningObject;
import lecho.lib.hellocharts.util.ChartUtils;

import static android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth;

/************************************************
 * The Running Fragment Class , allow the user to see the distance and speed in which he ran and display it in line graph
 ************************************************/

public class Running extends Fragment implements SensorEventListener {

	private TextView distanceTv;
	private TextView speedTv;

	private TextView timerTv;

	private String currentDistance = "0";
	private String currentSpeed = "0";

	private long startTime = 0L;
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;

	private Handler customHandler = new Handler();

	int startStop = 0;

	private float totalDistance = 0;

	private int height;
	private float width;


	private List<RunShape> runSegments = new ArrayList<>();

	private View v;


	String timeStamp = "   ";
	String timerText = "";
	String tentime = "00:00:00";

	private float speed =0;

	private List<String> timetenList = new ArrayList<>();

	private LocationManager manager;

	private Button startStopBtn;

	private float distance = 0;

	private float lastDistance =0;

	private int entryCounter = 0;

	private static List<Integer> xValues = new ArrayList<>();

	private static List<Integer> yValues = new ArrayList<>();

	private static ArrayList<RunningObject> jsonEntries = new ArrayList<>();

	private int speedSum = 0;

	private ListView runningLv;

	private RunningListAdapter myRunningListAdapter;

	private List<RunningObject> runObjList = new ArrayList<>();

	//private TextView runningHistoryText;
	private Button clearHistoryBtn;


	private static List<Entry> specificItemEntries = new ArrayList<>();

	////////////
	private SensorManager mSensorManager;
	private Sensor mStepCounterSensor;

	private int firstValue = 0;
	private int firstStepValue = 0;
	//private Handler customHandler = new Handler();

	private float currentStepDistance = 0;
	private float lastStepDistance = 0;

	private String m_Text = "";
	private int mHeight = 175;
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

		Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");
		Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

		((TextView) v.findViewById(R.id.distanceTitleTv)).setTypeface(custom_font_heb_regular);
		((TextView) v.findViewById(R.id.timerTitleTv)).setTypeface(custom_font_heb_regular);
		((TextView) v.findViewById(R.id.speedTitleTv)).setTypeface(custom_font_heb_regular);

		startStopBtn.setTypeface(custom_font_heb_regular);

		distanceTv.setTypeface(custom_font_eng_light);
		speedTv.setTypeface(custom_font_eng_light);
		timerTv.setTypeface(custom_font_eng_light);

		Gson gson = new Gson();

		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJ", "defaultStringIfNothingFound");
			if (listFromShared == null) {
				Type type = new TypeToken<ArrayList<RunningObject>>() {
				}.getType();
				if (listFromShared != null) {

					jsonEntries = gson.fromJson(listFromShared, type);
					runObjList = jsonEntries;
				}
			}else{
				runObjList = new ArrayList<>();
			}


		myRunningListAdapter = new RunningListAdapter(getActivity(),runObjList);
		runningLv.setAdapter(myRunningListAdapter);
		//runningLv.setOnItemClickListener(runningListOnItemClick);

		startStopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		return v;
	}


	private void stop() {
		//firstTime = 1;
		mSensorManager.unregisterListener(Running.this, mStepCounterSensor);
		firstValue = 0;
		lastStepDistance = 0;

		startStopBtn.setText("התחל ריצה");

		//////
		long date = System.currentTimeMillis();

		//SimpleDateFormat sdf = new SimpleDateFormat("MMM dd/MM/yyyy , h:mm a"); // different time display
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy , h:mm a");
		String dateString = sdf.format(date);
		/////

		runObjList.get(runObjList.size()-1).setDateAdded(" ");
		runObjList.get(runObjList.size()-1).setTimeAdded(dateString);
		runObjList.get(runObjList.size()-1).setRunDistance(totalDistance + " מטר ");
		runObjList.get(runObjList.size()-1).setRunTime(timerText);
		//getActivity().stopService(myIntent);
		//lastDistanceTv.setText(currentDistance);
		currentDistance = "0";
		currentSpeed = "0";
		distanceTv.setText(currentDistance);
		speedTv.setText(currentSpeed);
		//lastTimeTv.setText(timerTv.getText());
		timerTv.setText("00:00:00");
		//firstTime = 0;
		timeSwapBuff += timeInMilliseconds;
		customHandler.removeCallbacks(updateTimerThread);
		//customHandler.removeCallbacks(timeStampRunnable);
		//customHandler.removeCallbacks(timeSegmentsRunnable);
		startStop = 0;
		// reseting time variables
		startTime = 0L;
		timeInMilliseconds = 0L;
		timeSwapBuff = 0L;
		updatedTime = 0L;
		runSegments = null;
		timeStamp = "             ";
		tentime = "00:00:00";
		timetenList = new ArrayList<>();

		entryCounter = 0;

		//RunningObject firstRunObj = new RunningObject(entries);

		/*firstRunObj.setDateAdded("29/7");
		firstRunObj.setTimeAdded("21:50");
		firstRunObj.setRunDistance("38m");
		firstRunObj.setRunTime("23:00");

		runObjList.add(firstRunObj);*/

		totalDistance = 0;
		speedSum = 0;

		Gson gson = new Gson();
		String jsonAsString = gson.toJson(runObjList);

		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("runningJ", jsonAsString).commit();

		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJ", "defaultStringIfNothingFound");

		Type type = new TypeToken<ArrayList<RunningObject>>(){}.getType();
		jsonEntries = gson.fromJson(listFromShared, type);

		myRunningListAdapter.notifyDataSetChanged();

		runningLv.setVisibility(View.VISIBLE);
		//runningHistoryText.setVisibility(View.VISIBLE);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		customHandler.removeCallbacks(updateTimerThread);
		customHandler.removeCallbacks(speedRunnable);
		mSensorManager.unregisterListener(this, mStepCounterSensor);

	}

	private Runnable updateTimerThread = new Runnable() {
		public void run() {
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
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

			customHandler.postDelayed(this, 1000);
		}

	};

	private void addEntry(float currentSpeed) {

		entryCounter++;
		runObjList.get(runObjList.size()-1).getObjectEntries().add(new Entry(entryCounter, currentSpeed));

		//LineData data = lineGraph.getData();


	}


	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Sensor sensor = sensorEvent.sensor;
		float[] values = sensorEvent.values;
		int value = -1;

		if (values.length > 0) {
			value = (int) values[0];
		}
		if(firstValue == 0){
			firstStepValue = value;
			firstValue = 1;
		}
		value = value - firstStepValue;
		double meterValue = 0.45*(mHeight/100)*value;
		currentStepDistance = (float) meterValue;
		if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
			distanceTv.setText(String.format("%.2f", meterValue) );
		}
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
	private Runnable speedRunnable = new Runnable() {
		public void run() {
			float stepSpeed = (((currentStepDistance - lastStepDistance)/3)*18)/5;
			if (stepSpeed != 0) {
				speedTv.setText(String.format("%.2f", stepSpeed));
				speed = stepSpeed;
			}
			lastStepDistance = currentStepDistance;
			customHandler.postDelayed(this, 5000);
		}

	};

}
