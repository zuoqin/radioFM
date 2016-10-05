package fm100.co.il.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.models.RunningObject;
import fm100.co.il.models.VideoObj;

public class RunningListAdapter extends BaseAdapter {
    Context context;
    private List<RunningObject> runningObjectList = new ArrayList<>();
    LayoutInflater inflater = null;
    RunningObject tempRunObj;

    public RunningListAdapter(Context context , List<RunningObject> runningObjectList) {
        this.context = context;
        this.runningObjectList = runningObjectList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return runningObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder{

        public TextView runDistance;
        public TextView runTime;
        public TextView dateAdded;
        public TextView timeAdded;

        public List<Entry> entriesList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.running_list_item , parent , false);

            holder = new ViewHolder();
            holder.runDistance = (TextView) rowView.findViewById(R.id.runDistance);
            holder.runTime = (TextView) rowView.findViewById(R.id.runTime);
            holder.dateAdded = (TextView) rowView.findViewById(R.id.dateAdded);
            holder.timeAdded = (TextView) rowView.findViewById(R.id.timeAdded);

            Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

            holder.runDistance.setTypeface(custom_font_heb_regular);
            holder.runTime.setTypeface(custom_font_heb_regular);
            holder.dateAdded.setTypeface(custom_font_heb_regular);
            holder.timeAdded.setTypeface(custom_font_heb_regular);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (runningObjectList.size() <= 0){

        }
        else{
            tempRunObj = null;
            tempRunObj = runningObjectList.get(position);

            holder.runDistance.setText(tempRunObj.getRunDistance());
            holder.runTime.setText(tempRunObj.getRunTime());
            holder.dateAdded.setText(tempRunObj.getDateAdded());
            holder.timeAdded.setText(tempRunObj.getTimeAdded());



        }

        return rowView;
    }

}


