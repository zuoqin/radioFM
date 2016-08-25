package fm100.co.il.inner.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fm100.co.il.adapters.DataAdapter;
import fm100.co.il.busEvents.DistanceBusEvent;
import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.helpers.DistanceService;
import fm100.co.il.helpers.GPSTracker;
import fm100.co.il.models.RunShape;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/************************************************
 * The Running Fragment Class , allow the user to see the distance and speed in which he ran and display it in line graph
 ************************************************/

public class Running extends Fragment {

	//private final static Intent myIntent = new Intent(MainActivity.getMyApplicationContext(), DistanceService.class);
	private TextView distanceTv;
	private TextView speedTv;
	private TextView lastDistanceTv;
	private TextView timerTv;
	private TextView lastTimeTv;

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

	private LinearLayout ll;

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

	private TextView timePastTv;

	String timeStamp = "   ";
	String timerText = "";
	String tentime = "00:00:00";

	//private Handler timeStampHandler = new Handler();

	GPSTracker gps;

	private Location lastLocation;

	private int firstLocation = 0;

	private HorizontalScrollView hsView;

	private int minWidh = 500;

	private float speed =0;

	private List<String> timetenList = new ArrayList<>();

	private LocationManager manager;

	private Button startStopBtn;

	private ArrayList<String> countries = new ArrayList<>();
	private RecyclerView recyclerView;

	private int simpleCount = 0;

	private ArrayList<Integer> backgroundColors = new ArrayList<>();
	private ArrayList<Drawable> backgrounds = new ArrayList<>();

	private int newLastSpeed = 0;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		//EventBus.getDefault().register(this);

		v = inflater.inflate(R.layout.running_fragment, container, false);
		// flipping Layout the RTL to LTR incase needed
		flipLayouts();
		recyclerView = (RecyclerView)v.findViewById(R.id.card_recycler_view);

		initViews();

		manager = (LocationManager) getActivity().getSystemService(MainActivity.getMyApplicationContext().LOCATION_SERVICE);

		gps = new GPSTracker(MainActivity.getMyApplicationContext());

		startStopBtn = (Button) v.findViewById(R.id.startStopBtn);
		distanceTv = (TextView) v.findViewById(R.id.distanceTv);
		speedTv = (TextView) v.findViewById(R.id.speedTv);
		lastDistanceTv = (TextView) v.findViewById(R.id.lastDistanceTv);
		timerTv = (TextView) v.findViewById(R.id.timerTv);
		lastTimeTv = (TextView) v.findViewById(R.id.lastTimeTv);
		timePastTv = (TextView) v.findViewById(R.id.timePastTv);

		ll = (LinearLayout) v.findViewById(R.id.drawingArea);


		hsView = (HorizontalScrollView) v.findViewById(R.id.hScrollView);

		//setting canvas and canvas needed variables
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		height = (displaymetrics.heightPixels) / 5;
		width = displaymetrics.widthPixels;
		//int intWidth = (int) llWidth;



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
		lastDistanceTv.setText(currentDistance);
		currentDistance = "0";
		currentSpeed = "0";
		distanceTv.setText(currentDistance);
		speedTv.setText(currentSpeed);
		lastTimeTv.setText(timerTv.getText());
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

	private void addPath(int newSpeed) {
		float llWidth = ll.getWidth();
		Log.e("mynewlog" , "llWidthh: " +llWidth);
		bg = Bitmap.createBitmap((int) llWidth, height, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bg);


		int bitHeight = height-20;
		//minWidh = 500 + minWidh;
		//ll.setMinimumWidth(300000);

		//int widthRatio = (500/minWidh);
		// until here change
		//float llWidth = ll.getWidth();
		//float horiWidth = width;
		float widthRatio = (width/llWidth);
		drawHeightUnit = bitHeight / 45;
		//Log.e("mynewlog", "width: " + width + " llwidth: " + llWidth + " ratio: " + (width/llWidth));
		//if (llWidth < width) {
		unitWidth = 110;
		//width / 10;
		//}
		//else {
		//unitWidth = (width/20)/(llWidth/width);
		//	}
		int startingPoint = 30;
		// setting the colors
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		Paint circlePaint = new Paint();
		circlePaint.setColor(Color.parseColor("#fdff37"));
		Paint polygonPaint = new Paint();
		polygonPaint.setColor(Color.parseColor("#88ecfb"));
		polygonPaint.setStrokeWidth(1);
		polygonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		Paint newSpeedLinePaint = new Paint();
		newSpeedLinePaint.setColor(Color.parseColor("#ffffd4"));
		newSpeedLinePaint.setStrokeWidth(3);
		newSpeedLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		newSpeedLinePaint.setAntiAlias(true);
		newSpeedLinePaint.setShader(new LinearGradient(0, 0, 0, bitHeight - 180, Color.WHITE, Color.rgb(142, 192, 202), Shader.TileMode.CLAMP));
		polygonPaint.setAntiAlias(true);
		polygonPaint.setShader(new LinearGradient(0, 0, 0, bitHeight - 100, Color.WHITE, Color.WHITE, Shader.TileMode.CLAMP));
		Paint textPaint = new Paint();
		textPaint.setColor(Color.parseColor("#ffffff"));
		textPaint.setTextSize(28.0f);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		Paint speedMarkPaint = new Paint();
		speedMarkPaint.setColor(Color.parseColor("#204060"));

		// drawing the polygon from the last speed to the current speed

		for (int i = 0; i < runSegments.size(); i++) {
			// currently not used because i wanted to try with floats
			Point a = new Point(Math.round(startingPoint + (unitWidth * (runSegments.size() - (i + 1)))), bitHeight - 20 - 10);
			Point b = new Point(Math.round(startingPoint + (unitWidth * (runSegments.size() - (i + 1)))), (bitHeight - 10 - (drawHeightUnit * runSegments.get(i).getCurrentSpeed())) - 20);
			Point c = new Point(Math.round(startingPoint + (unitWidth * (runSegments.size() - i))), (bitHeight - 10 - (drawHeightUnit * runSegments.get(i).getPreviousSpeed())) - 20);
			Point d = new Point(Math.round(startingPoint + (unitWidth * (runSegments.size() - i))), bitHeight - 20 - 10);

			path = new Path();
			path.setFillType(Path.FillType.EVEN_ODD);
			path.moveTo((startingPoint + (unitWidth * (runSegments.size() - (i + 1)))), bitHeight - 20 - 10);
			path.lineTo(startingPoint + (unitWidth * (runSegments.size() - (i + 1))), (bitHeight - 10 - (drawHeightUnit * runSegments.get(i).getCurrentSpeed())) - 20);
			path.lineTo(startingPoint + (unitWidth * (runSegments.size() - i)), (bitHeight - 10 - (drawHeightUnit * runSegments.get(i).getPreviousSpeed())) - 20);
			path.lineTo(startingPoint + (unitWidth * (runSegments.size() - i)), bitHeight - 20 - 10);
			path.lineTo((startingPoint + (unitWidth * (runSegments.size() - (i + 1)))), bitHeight - 20 - 10);
			path.close();

			canvas.drawPath(path, polygonPaint);

		}

			for (int i = 0; i < timetenList.size(); i++) {
				//Log.e("mynewlog", "tentime: " + timetenList.get(i));
				if (timetenList != null) {
					canvas.drawText(timetenList.get(i), (startingPoint + (unitWidth * ((runSegments.size()) - ((i*3) + 1)))) + 5, height, textPaint);
				}
			}
		// creating the speed mark path
		speedMarkpath = new Path();
		speedMarkpath.setFillType(Path.FillType.EVEN_ODD);
		speedMarkpath.moveTo(startingPoint + unitWidth / 4, bitHeight - (drawHeightUnit * newSpeed) - 28 - 10);
		speedMarkpath.lineTo(startingPoint + unitWidth / 2, bitHeight - (drawHeightUnit * newSpeed) - 48 - 10);
		speedMarkpath.lineTo(startingPoint + unitWidth * 1.7f, bitHeight - (drawHeightUnit * newSpeed) - 48 - 10);
		speedMarkpath.lineTo(startingPoint + unitWidth * 1.7f, bitHeight - (drawHeightUnit * newSpeed) - 8 - 10);
		speedMarkpath.lineTo(startingPoint + unitWidth / 2, bitHeight - (drawHeightUnit * newSpeed) - 8 - 10);
		speedMarkpath.lineTo(startingPoint + unitWidth / 4, bitHeight - (drawHeightUnit * newSpeed) - 28 - 10);
		speedMarkpath.close();

		//drawing the path circle and speed mark
		canvas.drawPath(speedMarkpath, speedMarkPaint);
		canvas.drawText(newSpeed + " קמ''ש ", 10 + startingPoint + unitWidth / 2, bitHeight - (drawHeightUnit * newSpeed) - 20 - 10, textPaint);
		canvas.drawLine(startingPoint, bitHeight - 20 - 10, startingPoint, bitHeight - (drawHeightUnit * newSpeed) - 20 - 10, newSpeedLinePaint);
		canvas.drawCircle(startingPoint, bitHeight - (drawHeightUnit * newSpeed) - 28 - 10, 8, circlePaint);


		/*int circleWidth = 300;
		float resizedWidth = 300*widthRatio;
		int resizedWidthInt = (int) resizedWidth;
		Bitmap bg2 = Bitmap.createBitmap(resizedWidthInt, height, Bitmap.Config.ARGB_8888);
		//Log.e("mynewlog" , "resized width: " + resizedWidth + " widthRatio: " + widthRatio);
		//Bitmap resizedBitmap = Bitmap.createScaledBitmap(bg2, resizedWidthInt, height, false);
		Canvas canvas2 = new Canvas(bg2);
		for (int i = 0; i < runSegments.size(); i++) {
			canvas2.drawCircle((circleWidth/3) * (i + 1), 50, 50, textPaint);
		}

		Paint bitPaint = new Paint();
		bitPaint.setAntiAlias(true);
		bitPaint.setFilterBitmap(true);
		bitPaint.setDither(true);
		canvas.drawBitmap(bg2, 0, 0, bitPaint);
		*/

		ll.setBackgroundDrawable(new BitmapDrawable(bg));

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
			if(secs %10 ==0){
				tentime = timerText;
				timetenList.add(tentime);

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
		LinearLayout runningFragLL4 = (LinearLayout) v.findViewById(R.id.runningFragLL4);
		LinearLayout runningFragLL5 = (LinearLayout) v.findViewById(R.id.runningFragLL5);

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
			float distance;
			distance = lastLocation.distanceTo(newLocation);

			// just now //Toast.makeText(MainActivity.getMyApplicationContext(), "distance: " + distance, Toast.LENGTH_SHORT).show();
			speed = (distance / 10) * 18 / 5;
			// just now //Toast.makeText(MainActivity.getMyApplicationContext(), "speed: " + speed, Toast.LENGTH_SHORT).show();
			lastLocation = gps.getLocation();

			customHandler.postDelayed(this, 10000);
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
				float distance;
				distance = lastLocation.distanceTo(newLocation);

				//Toast.makeText(MainActivity.getMyApplicationContext(), "distance: " + distance, Toast.LENGTH_SHORT).show();
				speed = (distance / 10) * 18 / 5;
				//Toast.makeText(MainActivity.getMyApplicationContext(), "speed: " + speed, Toast.LENGTH_SHORT).show();
				lastLocation = gps.getLocation();

				currentDistance = String.format("%.0f", (speed));
				currentSpeed = String.format("%.0f", (speed));
				distanceTv.setText(currentDistance);
				speedTv.setText(currentSpeed);

				RunShape newRunShape = new RunShape((int) speed, lastSpeed);
				runSegments.add(newRunShape);
				lastSpeed = (int) speed;
				addPath((int) speed);
				timeStamp = "|                 |" + timeStamp;
				timePastTv.setText(timeStamp);

				Random rnd = new Random();
				int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

				/////////

				DisplayMetrics displaymetrics = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				int newHeight = (displaymetrics.heightPixels)/5;
				int newWidth = (displaymetrics.widthPixels);
				Bitmap bg2 = Bitmap.createBitmap(newWidth , height , Bitmap.Config.ARGB_8888);

				Canvas canvas2 = new Canvas(bg2);
				canvas2.drawColor(0, PorterDuff.Mode.CLEAR);
				//ll.setBackgroundDrawable(new BitmapDrawable(bg));

				float llWidth = ll.getWidth();
				drawHeightUnit = height/70;
				unitWidth = width / 20;

				// setting the colors
				//canvas.drawColor(0, PorterDuff.Mode.CLEAR);
				Paint circlePaint = new Paint();
				circlePaint.setColor(Color.parseColor("#fdff37"));
				Paint polygonPaint = new Paint();
				polygonPaint.setColor(Color.parseColor("#88ecfb"));
				polygonPaint.setStrokeWidth(1);
				polygonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				Paint newSpeedLinePaint = new Paint();
				newSpeedLinePaint.setColor(Color.parseColor("#ffffd4"));
				newSpeedLinePaint.setStrokeWidth(5);
				newSpeedLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
				newSpeedLinePaint.setAntiAlias(true);
				newSpeedLinePaint.setShader(new LinearGradient(0, 0, 0, height - 180, Color.WHITE, Color.rgb(242, 292, 202), Shader.TileMode.CLAMP));
				polygonPaint.setAntiAlias(true);
				polygonPaint.setShader(new LinearGradient(0, 0, 0, height - 100, Color.WHITE, Color.WHITE, Shader.TileMode.CLAMP));
				Paint textPaint = new Paint();
				textPaint.setColor(Color.parseColor("#ffffff"));
				textPaint.setTextSize(24.0f);
				textPaint.setTypeface(Typeface.DEFAULT_BOLD);
				Paint speedMarkPaint = new Paint();
				speedMarkPaint.setColor(Color.parseColor("#204060"));

				// drawing the polygon from the last speed to the current speed
				Random rnd2 = new Random();
				int nowSpeed = rnd2.nextInt(50);
				path = new Path();
				path.setFillType(Path.FillType.EVEN_ODD);
				path.moveTo(0, height - 60);
				path.lineTo(0, height - 60 - (nowSpeed * drawHeightUnit));// the random number = curSpeed
				path.lineTo(width, height - 60 - (newLastSpeed * drawHeightUnit));// the random number = lastSpeed
				path.lineTo(width, height - 60);
				path.lineTo(0, height - 60);
				path.close();

				newLastSpeed = nowSpeed;
				canvas2.drawPath(path, polygonPaint);

				backgrounds.add(0 , new BitmapDrawable(bg2));
				////////
				simpleCount++;
				backgroundColors.add( color);
				countries.add(" " + simpleCount);
				RecyclerView.Adapter adapter = new DataAdapter(backgrounds);
				recyclerView.setAdapter(adapter);

				customHandler.postDelayed(this, 10000/3);
			}
			else {
				stop();
				Toast.makeText(MainActivity.getMyApplicationContext(), "Run stopped , GPS has been disabled" , Toast.LENGTH_LONG).show();
			}
			//customHandler.postDelayed(this, 10000/3);
		}
	};
	public class initViews extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {


			return null;
		}
	}

	private void initViews(){
		recyclerView.setHasFixedSize(true);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.getMyApplicationContext() , LinearLayoutManager.HORIZONTAL , false);
		recyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
		recyclerView.setLayoutManager(layoutManager);
		//countries.add("1");

		recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
			GestureDetector gestureDetector = new GestureDetector(MainActivity.getMyApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

				@Override public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

			});
			@Override
			public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

				View child = rv.findChildViewUnder(e.getX(), e.getY());
				if(child != null && gestureDetector.onTouchEvent(e)) {
					int position = rv.getChildAdapterPosition(child);
					Toast.makeText(MainActivity.getMyApplicationContext(), countries.get(position), Toast.LENGTH_SHORT).show();
				}

				return false;
			}

			@Override
			public void onTouchEvent(RecyclerView rv, MotionEvent e) {

			}

			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

			}
		});
	}

}
