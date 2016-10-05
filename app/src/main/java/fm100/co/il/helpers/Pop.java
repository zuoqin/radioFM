package fm100.co.il.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import fm100.co.il.R;
import fm100.co.il.inner.fragments.Running;


public class Pop extends Activity {

    private LineChart popLineGraph;
    private ProgressBar popProgress;
    private TextView waitText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.graph_pop_dialog);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8),(int)(height*0.5));

        popLineGraph = (LineChart) findViewById(R.id.dialogLineGraph);
        popProgress = (ProgressBar) findViewById(R.id.popProgress);
        waitText = (TextView) findViewById(R.id.waitText);

        popLineGraph.setNoDataText("");

        createGraphTask task = new createGraphTask();
        task.execute();

        /*

        // creating x line values
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

        XAxis xAxis = popLineGraph.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        //xAxis.setTextSize(8f);
        Legend lx = popLineGraph.getLegend();
        lx.setEnabled(false);
        //xAxis.setAvoidFirstLastClipping(true);
        xAxis.setAxisLineWidth(1f);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
        //xAxis.setLabelCount(7, true);
        //xAxis.setGranularity(1f);

        YAxis yAxis = popLineGraph.getAxisRight();
        yAxis.setTextColor(Color.WHITE);
        //yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinValue(0.0f);
        yAxis.setAxisMaxValue(50);
        yAxis.setAxisLineColor(Color.YELLOW);
        yAxis.setAxisLineWidth(1f);

        popLineGraph.invalidate();
        //timetenList.add(tentime);
        */
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

    public class createGraphTask extends AsyncTask<String[], Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            popProgress.setVisibility(View.VISIBLE);
            waitText.setVisibility(View.VISIBLE);
           // popLineGraph.setVisibility(View.INVISIBLE);

        }

        @Override
        protected String[] doInBackground(String[]... params) {
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
            return values;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            popProgress.setVisibility(View.INVISIBLE);
            waitText.setVisibility(View.INVISIBLE);
            //popLineGraph.setVisibility(View.VISIBLE);
            final List<Entry> popEntries = new ArrayList<Entry>();
            popEntries.add(new Entry(0, 0));
            popEntries.add(new Entry(1, 10));
            popEntries.add(new Entry(2, 20));
            popEntries.add(new Entry(3, 0));
            popEntries.add(new Entry(4, 10));
            popEntries.add(new Entry(5, 20));
            popEntries.add(new Entry(6, 0));
            popEntries.add(new Entry(7, 10));
            popEntries.add(new Entry(8, 20));
            popEntries.add(new Entry(9, 0));
            popEntries.add(new Entry(10, 10));
            popEntries.add(new Entry(11, 20));
            popEntries.add(new Entry(12, 0));
            popEntries.add(new Entry(13, 10));
            popEntries.add(new Entry(14, 20));
            popEntries.add(new Entry(15, 0));
            popEntries.add(new Entry(16, 10));
            popEntries.add(new Entry(17, 20));
            //Running.getNewEntries();
            LineDataSet dataSet = new LineDataSet(Running.getNewEntries(), null);
            dataSet.setHighlightEnabled(false);
            dataSet.setColor(Color.RED);
            dataSet.setValueTextColor(Color.BLUE);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setColor(ColorTemplate.getHoloBlue());
            //dataSet.setCircleColor(Color.WHITE);
            dataSet.setLineWidth(2f);
            dataSet.setDrawCircles(false);
            //dataSet.setCircleRadius(1f);
            dataSet.setFillAlpha(65);
            dataSet.setFillColor(ColorTemplate.getHoloBlue());
            dataSet.setHighLightColor(Color.rgb(244, 117, 117));
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(7f);
            dataSet.setDrawValues(false);
            dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            LineData data = new LineData(dataSet);
            popLineGraph.setData(data);
            popLineGraph.setDrawGridBackground(false);
            popLineGraph.setDrawBorders(false);

            popLineGraph.getAxisLeft().setEnabled(false);
            popLineGraph.getAxisRight().setEnabled(true);
            popLineGraph.setDescription("");
		/*.setDrawAxisLine(false);
		lineGraph.getAxisRight().setDrawGridLines(false);
		*/
            popLineGraph.getXAxis().setEnabled(true);
            popLineGraph.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            popLineGraph.getXAxis().setDrawAxisLine(false);
            popLineGraph.getXAxis().setDrawGridLines(false);
            //lineGraph.getXAxis().setAxisMaxValue(51840);
            //lineGraph.getXAxis().setAxisMinValue(0);
            //lineGraph.getXAxis().setGranularity(1f);
            //lineGraph.getXAxis().setAxisMinValue(0f);
            //lineGraph.getXAxis().setAxisMaxValue(10f);
            //lineGraph.getXAxis().setDrawLabels(false);
				/*.setDrawAxisLine(false);
		lineGraph.getXAxis().setDrawGridLines(false);

*/

            popLineGraph.getXAxis().setAxisLineWidth(1.0f);

            // enable touch gestures
            popLineGraph.setTouchEnabled(true);

            // enable scaling and dragging
            popLineGraph.setDragEnabled(true);
            popLineGraph.setScaleEnabled(false);

            // if disabled, scaling can be done on x- and y-axis separately
            popLineGraph.setPinchZoom(false);

            popLineGraph.setVisibleXRangeMaximum(6f);

            XAxis xAxis = popLineGraph.getXAxis();
            xAxis.setTextColor(Color.WHITE);
            //xAxis.setTextSize(8f);
            Legend lx = popLineGraph.getLegend();
            lx.setEnabled(false);
            //xAxis.setAvoidFirstLastClipping(true);
            xAxis.setAxisLineWidth(1f);
            xAxis.setTextSize(8f);
            xAxis.setValueFormatter(new MyXAxisValueFormatter(result));
            //xAxis.setLabelCount(7, true);
            //xAxis.setGranularity(1f);

            YAxis yAxis = popLineGraph.getAxisRight();
            yAxis.setTextColor(Color.WHITE);
            //yAxis.setDrawAxisLine(false);
            yAxis.setDrawGridLines(false);
            yAxis.setAxisMinValue(0.0f);
            yAxis.setAxisMaxValue(50);
            yAxis.setAxisLineColor(Color.YELLOW);
            yAxis.setAxisLineWidth(1f);

            popLineGraph.invalidate();

        }
    }


}
