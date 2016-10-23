package com.nxcast.stations.il.fm100.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.models.ScheduleItem;
import com.nxcast.stations.il.fm100.models.VideoObj;

public class ScheduleDayAdapter extends BaseAdapter {
    Context context;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private String[] daysList = MainActivity.getMyApplicationContext().getResources().getStringArray(R.array.days_list_eng);
    LayoutInflater inflater = null;
    ScheduleItem tempItem;

    public ScheduleDayAdapter(Context context , List<ScheduleItem> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        public TextView scheduleTv;
        public TextView scheduleAuthor;
        public TextView scheduleTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.schedule_item , parent , false);

            holder = new ViewHolder();
            holder.scheduleTv = (TextView) rowView.findViewById(R.id.programName);
            holder.scheduleAuthor = (TextView) rowView.findViewById(R.id.programAuthor);
            holder.scheduleTime = (TextView) rowView.findViewById(R.id.programTime);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        tempItem = null;
        tempItem = scheduleList.get(position);

        Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

        holder.scheduleTv.setText(tempItem.getProgramName());
        holder.scheduleTime.setText(tempItem.getProgramStartHoure());

        holder.scheduleTv.setTypeface(custom_font_heb_regular);
        holder.scheduleTime.setTypeface(custom_font_heb_regular);

        if( !tempItem.getProgramAutor().equals("") ) {
            holder.scheduleAuthor.setText(tempItem.getProgramAutor());
            holder.scheduleAuthor.setTypeface(custom_font_heb_regular);
        } else {
            holder.scheduleAuthor.setVisibility(View.GONE);
        }
        return rowView;
    }
}
