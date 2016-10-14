package fm100.co.il.inner.fragments;

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

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.adapters.RunningListAdapter;
import fm100.co.il.helpers.GPSTracker;
import fm100.co.il.helpers.Pop;
import fm100.co.il.models.RunShape;
import fm100.co.il.models.RunningObject;
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

	private LineChart lineGraph;

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
	private FrameLayout runningHistoryFrame;
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
		flipLayouts();

		manager = (LocationManager) getActivity().getSystemService(MainActivity.getMyApplicationContext().LOCATION_SERVICE);

		mSensorManager = (SensorManager) MainActivity.getMyApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

		startStopBtn = (Button) v.findViewById(R.id.startStopBtn);
		distanceTv = (TextView) v.findViewById(R.id.distanceTv);
		speedTv = (TextView) v.findViewById(R.id.speedTv);
		timerTv = (TextView) v.findViewById(R.id.timerTv);
		runningLv = (ListView) v.findViewById(R.id.runningLv);
		//runningHistoryText = (TextView) v.findViewById(R.id.runningHistoryText);
		runningHistoryFrame = (FrameLayout) v.findViewById(R.id.runningHistoryFrame);
		clearHistoryBtn = (Button) v.findViewById(R.id.clearHistoryBtn);
		Gson gson = new Gson();
		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJson", "defaultStringIfNothingFound");

		Type type = new TypeToken<ArrayList<RunningObject>>(){}.getType();
		jsonEntries = gson.fromJson(listFromShared, type);

		runObjList = jsonEntries;

		myRunningListAdapter = new RunningListAdapter(getActivity(),runObjList);
		runningLv.setAdapter(myRunningListAdapter);
		runningLv.setOnItemClickListener(runningListOnItemClick);

		lineGraph = (LineChart) v.findViewById(R.id.lineGraph);

		//setting canvas and canvas needed variables
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		height = (displaymetrics.heightPixels) / 5;
		width = displaymetrics.widthPixels;

		clearHistoryBtn.setOnClickListener(clearHistory);

		//setting the xAxis values (72 hours by 5 secs)
		int hours = 0;
		int mins = 0;
		int secs = 0;
		String[] values = new String[51840];
		int timeValuesCounter = 0;

		for (int i = 0; i<72 ; i++){
			hours = i;
			for(int j = 0 ; j<60 ; j++){
				mins = j;
				for (int k=0 ; k<12; k++){
					secs = 5*k;
					String timeValues = "" + String.format("%02d", hours) + ":"
							+ String.format("%02d", mins) + ":"
							+ String.format("%02d", secs);
					values[timeValuesCounter] = timeValues;
					timeValuesCounter++;

				}
			}
		}

		List<Entry> newEntriesSet = new ArrayList<Entry>();
		newEntriesSet.add(new Entry(0, 0));

		LineDataSet dataSet = new LineDataSet(newEntriesSet, null);

		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setLineWidth(2f);
		dataSet.setDrawCircles(false);
		dataSet.setFillAlpha(65);
		dataSet.setFillColor(ColorTemplate.getHoloBlue());
		dataSet.setHighLightColor(Color.rgb(244, 117, 117));
		dataSet.setValueTextColor(Color.WHITE);
		dataSet.setValueTextSize(8f);
		dataSet.setDrawValues(false);
		dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

		final LineData lineData = new LineData(dataSet);
		lineGraph.setData(lineData);

		lineGraph.setDrawGridBackground(false);
		lineGraph.setDrawBorders(false);

		lineGraph.getAxisLeft().setEnabled(false);
		lineGraph.getAxisRight().setEnabled(true);
		lineGraph.setDescription("");

		lineGraph.getXAxis().setEnabled(true);
		lineGraph.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		lineGraph.getXAxis().setDrawAxisLine(false);
		lineGraph.getXAxis().setDrawGridLines(false);

		lineGraph.getXAxis().setAxisLineWidth(1.0f);

		// enable touch gestures
		lineGraph.setTouchEnabled(true);

		// enable scaling and dragging
		lineGraph.setDragEnabled(true);
		lineGraph.setScaleEnabled(false);

		// if disabled, scaling can be done on x- and y-axis separately
		lineGraph.setPinchZoom(false);
		lineGraph.setVisibleXRangeMaximum(6f);

		XAxis xAxis = lineGraph.getXAxis();
		xAxis.setTextColor(Color.WHITE);
		Legend lx = lineGraph.getLegend();
		lx.setEnabled(false);
		xAxis.setAxisLineWidth(1f);
		xAxis.setValueFormatter(new MyXAxisValueFormatter(values));

		YAxis yAxis = lineGraph.getAxisRight();
		yAxis.setTextColor(Color.WHITE);
		yAxis.setDrawGridLines(false);
		yAxis.setAxisMinValue(0.0f);
		yAxis.setAxisMaxValue(50);
		yAxis.setAxisLineColor(Color.YELLOW);
		yAxis.setAxisLineWidth(1f);

		lineGraph.invalidate();

		startStopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (startStop == 0) {
					// building height input dialog before starting the run
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity() ,  Theme_Holo_Dialog_NoActionBar_MinWidth);
					builder.setTitle("נא הכנס/י גובה בס''מ");

					// Set up the input
					/*
					final EditText input = new EditText(getActivity());
					// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setHintTextColor(Color.LTGRAY);
					String mHeightString = Integer.toString(mHeight);
					input.setText(mHeightString);
					input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
					input.setTextColor(Color.WHITE);

					builder.setView(input);
					*/

					/////
					final NumberPicker numberPicker = new NumberPicker(getActivity());
					numberPicker.setMinValue(100);
					numberPicker.setMaxValue(250);
					numberPicker.setValue(mHeight);
					numberPicker.setWrapSelectorWheel(true);
					setNumberPickerTextColor(numberPicker,Color.WHITE);
					builder.setView(numberPicker);
					/////


					// Set up the buttons
					builder.setNeutralButton("אישור", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						//	m_Text = input.getText().toString();
							mHeight = numberPicker.getValue();
							//mHeight = Integer.parseInt(m_Text);
							mSensorManager.registerListener(Running.this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
							customHandler.postDelayed(speedRunnable, 0);
							runningLv.setVisibility(View.INVISIBLE);
							//runningHistoryText.setVisibility(View.INVISIBLE);
							runningHistoryFrame.setVisibility(View.INVISIBLE);

							lineGraph.setVisibility(View.VISIBLE);

							runSegments = new ArrayList<>();
							startStopBtn.setText("עצור");

							startTime = SystemClock.uptimeMillis();
							customHandler.postDelayed(updateTimerThread, 0);

							while(lineData.getEntryCount()!=0)
								lineData.removeEntry(0,0);

							lineData.removeDataSet(0);

							LineDataSet dataSet = new LineDataSet(null, null); // add entries to dataset
							dataSet.setHighlightEnabled(false);
							dataSet.setColor(Color.RED);
							dataSet.setValueTextColor(Color.BLUE);
							dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
							dataSet.setColor(ColorTemplate.getHoloBlue());
							//dataSet.setCircleColor(Color.WHITE);
							dataSet.setLineWidth(2f);
							//dataSet.setCircleRadius(1f);
							dataSet.setDrawCircles(false);
							dataSet.setFillAlpha(65);
							dataSet.setFillColor(ColorTemplate.getHoloBlue());
							dataSet.setHighLightColor(Color.rgb(244, 117, 117));
							dataSet.setValueTextColor(Color.WHITE);
							dataSet.setValueTextSize(9f);
							dataSet.setDrawValues(false);
							dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);


							final LineData lineData = new LineData(dataSet);
							lineGraph.setData(lineData);

							List<Entry> newEntriesSet = new ArrayList<Entry>();

							runObjList.add(new RunningObject(newEntriesSet));


							createSet(runObjList.get(runObjList.size()-1).getObjectEntries());

							startStop = 1;
						}
					});
					builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

					builder.show();
				} else {
					stop();


				}
			}
		});
		return v;
	}

	View.OnClickListener clearHistory = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity() ,  Theme_Holo_Dialog_NoActionBar_MinWidth);
			builder.setTitle("האם את/ה בטוח כי ברצונך לנקות את היסטורית הריצה?");

			// Set up the buttons
			builder.setNeutralButton("כן", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					runObjList.clear();
					Gson gson = new Gson();
					String jsonAsString = gson.toJson(runObjList);

					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("runningJson", jsonAsString).commit();

					String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJson", "defaultStringIfNothingFound");

					Type type = new TypeToken<ArrayList<RunningObject>>(){}.getType();
					jsonEntries = gson.fromJson(listFromShared, type);

					myRunningListAdapter.notifyDataSetChanged();
				}
			});
			builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			builder.show();
		}
	};


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

		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("runningJson", jsonAsString).commit();

		String listFromShared = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("runningJson", "defaultStringIfNothingFound");

		Type type = new TypeToken<ArrayList<RunningObject>>(){}.getType();
		jsonEntries = gson.fromJson(listFromShared, type);

		myRunningListAdapter.notifyDataSetChanged();

		runningLv.setVisibility(View.VISIBLE);
		lineGraph.setVisibility(View.INVISIBLE);
		//runningHistoryText.setVisibility(View.VISIBLE);
		runningHistoryFrame.setVisibility(View.VISIBLE);

	}

	public void getGraph(List<Entry> getEntries){
		LineDataSet dataSet = new LineDataSet(getEntries, null);
		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setLineWidth(2f);
		dataSet.setDrawCircles(false);
		dataSet.setFillAlpha(65);
		dataSet.setFillColor(ColorTemplate.getHoloBlue());
		dataSet.setHighLightColor(Color.rgb(244, 117, 117));
		dataSet.setValueTextColor(Color.WHITE);
		dataSet.setValueTextSize(8f);
		dataSet.setDrawValues(false);
		dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
		LineData data = new LineData(dataSet);
		lineGraph.setData(data);
		lineGraph.setDrawGridBackground(false);
		lineGraph.setDrawBorders(false);

		lineGraph.getAxisLeft().setEnabled(false);
		lineGraph.getAxisRight().setEnabled(true);
		lineGraph.setDescription("");
		lineGraph.getXAxis().setEnabled(true);
		lineGraph.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		lineGraph.getXAxis().setDrawAxisLine(false);
		lineGraph.getXAxis().setDrawGridLines(false);

		lineGraph.getXAxis().setAxisLineWidth(1.0f);

		// enable touch gestures
		lineGraph.setTouchEnabled(true);

		// enable scaling and dragging
		lineGraph.setDragEnabled(true);
		lineGraph.setScaleEnabled(false);

		// if disabled, scaling can be done on x- and y-axis separately
		lineGraph.setPinchZoom(false);

		lineGraph.setVisibleXRangeMaximum(6f);

		lineGraph.invalidate();


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

	private boolean isRTL() {
		Locale defLocale = Locale.getDefault();
		return Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
	}

	private void flipLayouts() {
		LinearLayout runningFragLL1 = (LinearLayout) v.findViewById(R.id.runningFragLL1);
		LinearLayout runningFragLL2 = (LinearLayout) v.findViewById(R.id.runningFragLL2);
		LinearLayout runningFragLL3 = (LinearLayout) v.findViewById(R.id.runningFragLL3);

		List<LinearLayout> llList = new ArrayList<>();
		llList.add(runningFragLL1);
		llList.add(runningFragLL2);
		llList.add(runningFragLL3);

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


	private void addEntry(float currentSpeed) {

		entryCounter++;
		runObjList.get(runObjList.size()-1).getObjectEntries().add(new Entry(entryCounter, currentSpeed));

		LineData data = lineGraph.getData();

		if (data != null) {

			ILineDataSet set = data.getDataSetByIndex(0);
			// set.addEntry(...); // can be called as well

			if (set == null) {
				set = createSet();
				data.addDataSet(set);
			}

			data.addEntry(new Entry(set.getEntryCount(),currentSpeed), 0);

			data.notifyDataChanged();

			// let the chart know it's data has changed
			lineGraph.notifyDataSetChanged();

			// limit the number of visible entries
			lineGraph.setVisibleXRangeMaximum(6f);
			// mChart.setVisibleYRange(30, AxisDependency.LEFT);

			// move to the latest entry
			lineGraph.moveViewToX(data.getEntryCount());
		}

	}

	private LineDataSet createSet(List<Entry> newestEntries){
		LineDataSet dataSet = new LineDataSet(newestEntries, null); // add entries to dataset
		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setLineWidth(2f);
		dataSet.setDrawCircles(false);
		dataSet.setFillAlpha(65);
		dataSet.setFillColor(ColorTemplate.getHoloBlue());
		dataSet.setHighLightColor(Color.rgb(244, 117, 117));
		dataSet.setValueTextColor(Color.WHITE);
		dataSet.setValueTextSize(9f);
		dataSet.setDrawValues(false);


		return dataSet;
	}

	private LineDataSet createSet(){
		LineDataSet dataSet = new LineDataSet(null, null); // add entries to dataset
		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setLineWidth(2f);
		dataSet.setDrawCircles(false);
		dataSet.setFillAlpha(65);
		dataSet.setFillColor(ColorTemplate.getHoloBlue());
		dataSet.setHighLightColor(Color.rgb(244, 117, 117));
		dataSet.setValueTextColor(Color.WHITE);
		dataSet.setValueTextSize(9f);
		dataSet.setDrawValues(false);

		return dataSet;
	}

	public class MyXAxisValueFormatter implements AxisValueFormatter {

		private String[] mValues;
		String lastValue ="";

		public MyXAxisValueFormatter(String[] values) {
			this.mValues = values;
		}

		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			// "value" represents the position of the label on the axis (x or y)
			if ( mValues[(int) value].equals(lastValue)){
				return "";
			}
			lastValue =  mValues[(int) value];
			return mValues[(int) value];
		}

		/** this is only needed if numbers are returned, else return 0 */
		@Override
		public int getDecimalDigits() { return 0; }
	}

	public static List<Entry> getNewEntries(){
		List<Entry> newEntries = new ArrayList<>();
		newEntries = specificItemEntries;
		//Toast.makeText(MainActivity.getMyApplicationContext() , obj.toString() , Toast.LENGTH_LONG ).show();
		//newEntries = entries;
		return  newEntries;
	}

	AdapterView.OnItemClickListener runningListOnItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			getGraph(jsonEntries.get(position).getObjectEntries());//jsonEntries.get(0).getObjectEntries());

			specificItemEntries  = jsonEntries.get(position).getObjectEntries();

			startActivity(new Intent(MainActivity.getMyApplicationContext() , Pop.class));
		}
	};


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

	public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
	{
		final int count = numberPicker.getChildCount();
		for(int i = 0; i < count; i++){
			View child = numberPicker.getChildAt(i);
			if(child instanceof EditText){
				try{
					Field selectorWheelPaintField = numberPicker.getClass()
							.getDeclaredField("mSelectorWheelPaint");
					selectorWheelPaintField.setAccessible(true);
					((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
					((EditText)child).setTextColor(color);
					numberPicker.invalidate();
					return true;
				}
				catch(Exception e){
					Log.e("loglog", e.getMessage());
				}
			}
		}
		return false;
	}
}
