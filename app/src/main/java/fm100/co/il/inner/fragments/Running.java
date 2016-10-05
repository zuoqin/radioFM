package fm100.co.il.inner.fragments;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.helpers.GPSTracker;
import fm100.co.il.models.RunShape;
import lecho.lib.hellocharts.util.ChartUtils;

/************************************************
 * The Running Fragment Class , allow the user to see the distance and speed in which he ran and display it in line graph
 ************************************************/


public class Running extends Fragment {

	//private final static Intent myIntent = new Intent(MainActivity.getMyApplicationContext(), DistanceService.class);
	private TextView distanceTv;
	private TextView speedTv;
	//private TextView lastDistanceTv;
	private TextView timerTv;
	//private TextView lastTimeTv;

	private String currentDistance = "0";
	private String currentSpeed = "0";

	//private int firstTime = 0;

	private long startTime = 0L;
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;

	private Handler customHandler = new Handler();


	int startStop = 0;

	int drawAreaWidth = 0;
	int drawAreaHeight = 0;
	private int firstWidth;
	private int firstCanvas = 0;

	//private LinearLayout ll;

	//private int curSpeed=34;
	//private int lastSpeed =23;

	// the distance of the start of the graph from screen
	private int graphStartPoint = 100;
	private int lastSpeed = 1 / 2;

	private int drawHeightUnit;
	private float unitWidth;

	private Canvas canvas = null;
	private Bitmap bg = null;

	private int height;
	private float width;

	private Path path = null;
	private Path speedMarkpath = null;

	private List<RunShape> runSegments = new ArrayList<>();

	private View v;

	//private TextView timePastTv;

	String timeStamp = "   ";
	String timerText = "";
	String tentime = "00:00:00";

	//private Handler timeStampHandler = new Handler();

	GPSTracker gps;

	private Location lastLocation;

	private int firstLocation = 0;

	//private HorizontalScrollView hsView;

	private int minWidh = 500;

	private float speed =0;

	private List<String> timetenList = new ArrayList<>();

	private LocationManager manager;

	private Button startStopBtn;

	private LineChart lineGraph;

	private float distance = 0;

	private float lastDistance =0;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		//EventBus.getDefault().register(this);

		v = inflater.inflate(R.layout.running_fragment, container, false);
		// flipping Layout the RTL to LTR incase needed
		flipLayouts();

		manager = (LocationManager) getActivity().getSystemService(MainActivity.getMyApplicationContext().LOCATION_SERVICE);

		gps = new GPSTracker(MainActivity.getMyApplicationContext());

		startStopBtn = (Button) v.findViewById(R.id.startStopBtn);
		distanceTv = (TextView) v.findViewById(R.id.distanceTv);
		speedTv = (TextView) v.findViewById(R.id.speedTv);
		//lastDistanceTv = (TextView) v.findViewById(R.id.lastDistanceTv);
		timerTv = (TextView) v.findViewById(R.id.timerTv);
		//lastTimeTv = (TextView) v.findViewById(R.id.lastTimeTv);
		//timePastTv = (TextView) v.findViewById(R.id.timePastTv);

		//ll = (LinearLayout) v.findViewById(R.id.drawingArea);

		//hsView = (HorizontalScrollView) v.findViewById(R.id.hScrollView);
		lineGraph = (LineChart) v.findViewById(R.id.lineGraph);

		//setting canvas and canvas needed variables
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		height = (displaymetrics.heightPixels) / 5;
		width = displaymetrics.widthPixels;
		//int intWidth = (int) llWidth;
// -----------------------------------------



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

		// setting 1st entry
		final List<Entry> entries = new ArrayList<Entry>();
		entries.add(new Entry(0, 0));


		LineDataSet dataSet = new LineDataSet(entries, null); // add entries to dataset
		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setCircleColor(Color.WHITE);
		dataSet.setLineWidth(2f);
		dataSet.setCircleRadius(1f);
		dataSet.setFillAlpha(65);
		dataSet.setFillColor(ColorTemplate.getHoloBlue());
		dataSet.setHighLightColor(Color.rgb(244, 117, 117));
		dataSet.setValueTextColor(Color.WHITE);
		dataSet.setValueTextSize(8f);
		dataSet.setDrawValues(false);
		dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);


		final LineData lineData = new LineData(dataSet);
		lineGraph.setData(lineData);


		/*
		lineGraph.canScrollHorizontally(1);
		// enable touch gestures
		lineGraph.setTouchEnabled(true);
		// enable scaling and dragging
		lineGraph.setDragEnabled(true);
		//lineGraph.setScaleEnabled(true);
		lineGraph.setDrawGridBackground(false);

		XAxis xl = lineGraph.getXAxis();
		xl.setTextColor(Color.WHITE);
		xl.setDrawGridLines(false);
		xl.setAvoidFirstLastClipping(true);
		xl.setEnabled(true);

		YAxis leftAxis = lineGraph.getAxisLeft();
		leftAxis.setTextColor(Color.WHITE);
		leftAxis.setAxisMaxValue(10f);
		leftAxis.setAxisMinValue(0f);
		leftAxis.setDrawGridLines(true);
		YAxis rightAxis = lineGraph.getAxisRight();
		rightAxis.setEnabled(false);
		*/

		lineGraph.setDrawGridBackground(false);
		lineGraph.setDrawBorders(false);

		lineGraph.getAxisLeft().setEnabled(false);
		lineGraph.getAxisRight().setEnabled(true);
		lineGraph.setDescription("");
		/*.setDrawAxisLine(false);
		lineGraph.getAxisRight().setDrawGridLines(false);
		*/
		lineGraph.getXAxis().setEnabled(true);
		lineGraph.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		lineGraph.getXAxis().setDrawAxisLine(false);
		lineGraph.getXAxis().setDrawGridLines(false);
		//lineGraph.getXAxis().setAxisMaxValue(51840);
		//lineGraph.getXAxis().setAxisMinValue(0);
		//lineGraph.getXAxis().setGranularity(1f);
		//lineGraph.getXAxis().setAxisMinValue(0f);
		//lineGraph.getXAxis().setAxisMaxValue(10f);
		//lineGraph.getXAxis().setDrawLabels(false);
				/*.setDrawAxisLine(false);
		lineGraph.getXAxis().setDrawGridLines(false);

*/

		lineGraph.getXAxis().setAxisLineWidth(1.0f);

		// enable touch gestures
		lineGraph.setTouchEnabled(true);

		// enable scaling and dragging
		lineGraph.setDragEnabled(true);
		lineGraph.setScaleEnabled(false);

		// if disabled, scaling can be done on x- and y-axis separately
		lineGraph.setPinchZoom(false);

		lineGraph.setVisibleXRangeMaximum(6f);
		//lineGraph.setVisibleXRangeMinimum(6f);

		///////////////////////////////////

		XAxis xAxis = lineGraph.getXAxis();
		xAxis.setTextColor(Color.WHITE);
		//xAxis.setTextSize(8f);
		Legend lx = lineGraph.getLegend();
		lx.setEnabled(false);
		//xAxis.setAvoidFirstLastClipping(true);
		xAxis.setAxisLineWidth(1f);
		xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
		//xAxis.setLabelCount(7, true);
		//xAxis.setGranularity(1f);

		YAxis yAxis = lineGraph.getAxisRight();
		yAxis.setTextColor(Color.WHITE);
		//yAxis.setDrawAxisLine(false);
		yAxis.setDrawGridLines(false);
		yAxis.setAxisMinValue(0.0f);
		yAxis.setAxisMaxValue(50);
		yAxis.setAxisLineColor(Color.YELLOW);
		yAxis.setAxisLineWidth(1f);

		///////////////////////////////////


		lineGraph.invalidate();

		// ------------------------------------------------


		startStopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (startStop == 0) {
					if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						/* buildAlertMessageNoGps();
					} else {*/
						//if(gps.canGetLocation()) {

						//Toast.makeText(MainActivity.getMyApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

						//canvas.drawColor(0, PorterDuff.Mode.CLEAR);
						//ll.setBackgroundDrawable(new BitmapDrawable(bg));
						// new change above
						runSegments = new ArrayList<>();
						startStopBtn.setText("עצור");
						//getActivity().startService(myIntent);
						startTime = SystemClock.uptimeMillis();
						customHandler.postDelayed(updateTimerThread, 0);
						customHandler.postDelayed(timeStampRunnable, 0);
						customHandler.post(timeSegmentsRunnable);

						while(lineData.getEntryCount()!=0)
							lineData.removeEntry(0,0);

						lineData.removeDataSet(0);
						/*
						final List<Entry> entries2 = new ArrayList<Entry>();
						entries.add(new Entry(0, 0));
						*/
						LineDataSet dataSet = new LineDataSet(null, null); // add entries to dataset
						dataSet.setHighlightEnabled(false);
						dataSet.setColor(Color.RED);
						dataSet.setValueTextColor(Color.BLUE);
						dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
						dataSet.setColor(ColorTemplate.getHoloBlue());
						dataSet.setCircleColor(Color.WHITE);
						dataSet.setLineWidth(2f);
						dataSet.setCircleRadius(1f);
						dataSet.setFillAlpha(65);
						dataSet.setFillColor(ColorTemplate.getHoloBlue());
						dataSet.setHighLightColor(Color.rgb(244, 117, 117));
						dataSet.setValueTextColor(Color.WHITE);
						dataSet.setValueTextSize(9f);
						dataSet.setDrawValues(false);
						dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);


						final LineData lineData = new LineData(dataSet);
						lineGraph.setData(lineData);


						startStop = 1;

						//Toast.makeText(MainActivity.getMyApplicationContext() , " GPS STOPPED!" , Toast.LENGTH_LONG).show();
						//}
					} else {
						// Can't get location.
						// GPS or network is not enabled.
						// Ask user to enable GPS/network in settings.
						gps.showSettingsAlert();
					}


				} else {
					stop();


				}
			}
		});
		return v;
	}

	private void stop() {
		//firstTime = 1;
		startStopBtn.setText("התחל ריצה");
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
		customHandler.removeCallbacks(timeStampRunnable);
		customHandler.removeCallbacks(timeSegmentsRunnable);
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
		//timetenList.add(tentime);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//getActivity().stopService(myIntent);
		customHandler.removeCallbacks(updateTimerThread);
		customHandler.removeCallbacks(timeStampRunnable);
		customHandler.removeCallbacks(timeSegmentsRunnable);
		//timeStampHandler.removeCallbacks(timeStampRunnable);
		//EventBus.getDefault().unregister(this);
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
				addEntry();

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
		//LinearLayout runningFragLL4 = (LinearLayout) v.findViewById(R.id.runningFragLL4);
		//LinearLayout runningFragLL5 = (LinearLayout) v.findViewById(R.id.runningFragLL5);

		List<LinearLayout> llList = new ArrayList<>();
		llList.add(runningFragLL1);
		llList.add(runningFragLL2);
		llList.add(runningFragLL3);
		//llList.add(runningFragLL4);
		//llList.add(runningFragLL5);

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

	private Runnable timeStampRunnable = new Runnable() {
		public void run() {



			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			// \n is for new line
			//Toast.makeText(MainActivity.getMyApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

			if (firstLocation == 0) {
				//	Toast.makeText(MainActivity.getMyApplicationContext(), "got first location " , Toast.LENGTH_SHORT).show();
				lastLocation = gps.getLocation();
				firstLocation = 1;
			}
			Location newLocation = gps.getLocation();
			// just now //Toast.makeText(MainActivity.getMyApplicationContext(), "Your new Location is " + newLocation + " Your last Location is " + lastLocation, Toast.LENGTH_LONG).show();
			float thisDistance;
			if( lastLocation != null ) {
				thisDistance = lastLocation.distanceTo(newLocation);
			}
			float thisSpeed;
			// just now //Toast.makeText(MainActivity.getMyApplicationContext(), "distance: " + distance, Toast.LENGTH_SHORT).show();
			thisSpeed = (distance / 10) * 18 / 5;
			if (thisSpeed > 50){
				thisSpeed =0;
			}
			Location thisLastLocation;
			// just now //Toast.makeText(MainActivity.getMyApplicationContext(), "speed: " + speed, Toast.LENGTH_SHORT).show();
			thisLastLocation = gps.getLocation();

			customHandler.postDelayed(this, 5000);
		}
	};

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private Runnable timeSegmentsRunnable = new Runnable() {
		public void run() {

			if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Location newLocation = gps.getLocation();
				//Toast.makeText(MainActivity.getMyApplicationContext(), "Your new Location is " + newLocation + " Your last Location is " + lastLocation, Toast.LENGTH_LONG).show();

				if( lastLocation != null ) {
					lastDistance = lastLocation.distanceTo(newLocation);
				}
				if (lastDistance>60){
					lastDistance =0;
				}
				distance += lastDistance;
				//Toast.makeText(MainActivity.getMyApplicationContext(), "distance: " + distance, Toast.LENGTH_SHORT).show();
				speed = (lastDistance / 10) * 18 / 5;
				// making sure if speed is too big reach the max of the chart
				if (speed>50){
					speed=0;
				}
				//Toast.makeText(MainActivity.getMyApplicationContext(), "speed: " + speed, Toast.LENGTH_SHORT).show();
				lastLocation = gps.getLocation();

				currentDistance = String.format("%.0f", (distance));
				currentSpeed = String.format("%.0f", (speed));
				distanceTv.setText(currentDistance);
				speedTv.setText(currentSpeed);
				lastSpeed = (int) speed;

				//RunShape newRunShape = new RunShape((int) speed, lastSpeed);
				//runSegments.add(newRunShape);

				//timeStamp = "|              |" + timeStamp;
				//timePastTv.setText(timeStamp);

				customHandler.postDelayed(this, 5000);
			}
			else {
				stop();
				Toast.makeText(MainActivity.getMyApplicationContext(), "Run stopped , GPS has been disabled" , Toast.LENGTH_LONG).show();
			}
			//customHandler.postDelayed(this, 10000/3);
		}
	};

	private void addEntry() {

		LineData data = lineGraph.getData();

		if (data != null) {

			ILineDataSet set = data.getDataSetByIndex(0);
			// set.addEntry(...); // can be called as well

			if (set == null) {
				set = createSet();
				data.addDataSet(set);
			}

			/*
			Legend l = lineGraph.getLegend();
			l.setFormSize(10f); // set the size of the legend forms/shapes
			l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
			l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
			l.setTextSize(12f);
			l.setTextColor(Color.BLACK);
			l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
			l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis

			// set custom labels and colors
			l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "Set1", "Set2", "Set3", "Set4", "Set5" });

			*/

			data.addEntry(new Entry(set.getEntryCount(),speed), 0);//speed instead random
			//List list = new LinkedList(lineGraph.getLineData().getDataSets());
			//Collections.sort(list, new EntryXComparator());
			data.notifyDataChanged();

			// let the chart know it's data has changed
			lineGraph.notifyDataSetChanged();

			// limit the number of visible entries
			lineGraph.setVisibleXRangeMaximum(6f);
			// mChart.setVisibleYRange(30, AxisDependency.LEFT);

			// move to the latest entry
			lineGraph.moveViewToX(data.getEntryCount());

			// this automatically refreshes the chart (calls invalidate())
			// mChart.moveViewTo(data.getXValCount()-7, 55f,
			// AxisDependency.LEFT);
		}
	}

	private LineDataSet createSet(){
		LineDataSet dataSet = new LineDataSet(null, null); // add entries to dataset
		dataSet.setHighlightEnabled(false);
		dataSet.setColor(Color.RED);
		dataSet.setValueTextColor(Color.BLUE);
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setColor(ColorTemplate.getHoloBlue());
		dataSet.setCircleColor(Color.WHITE);
		dataSet.setLineWidth(2f);
		dataSet.setCircleRadius(1f);
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

}
