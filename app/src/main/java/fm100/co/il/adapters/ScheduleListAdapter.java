package fm100.co.il.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.models.ScheduleItem;
import fm100.co.il.models.VideoObj;

public class ScheduleListAdapter extends BaseAdapter {

    Context context;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private String[] daysList = MainActivity.getMyApplicationContext().getResources().getStringArray(R.array.days_list);
    private String[] daysListEng = MainActivity.getMyApplicationContext().getResources().getStringArray(R.array.days_list_eng);
        LayoutInflater inflater = null;
    List<ScheduleItem> tempScheduleList = new ArrayList<>();
    private ScheduleDayAdapter myScheduleDayAdapter;

    public ScheduleListAdapter(Context context , List<ScheduleItem> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return daysList.length;
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
        public TextView dayTv;
        public ListView scheduleListView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.schedule_list_item , parent , false);

            holder = new ViewHolder();
            holder.dayTv = (TextView) rowView.findViewById(R.id.dayTv);
            holder.scheduleListView = (ListView) rowView.findViewById(R.id.scheduleItemLv);
            Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

            holder.dayTv.setTypeface(custom_font_heb_regular);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        tempScheduleList.clear();
        int h = 0;
        for (int i=0 ; i<scheduleList.size() ; i++){
            if(scheduleList.get(i).getProgramDay().toLowerCase().equals(daysListEng[position].toLowerCase())){
                tempScheduleList.add(scheduleList.get(i));

                h += scheduleList.get(i).getProgramAutor().isEmpty() ? 88 : 182;
            }
        }
        holder.dayTv.setText(daysList[position]);

        Log.i("100fm", daysList[position] + " - " + tempScheduleList.size());

        myScheduleDayAdapter = new ScheduleDayAdapter(MainActivity.getMyApplicationContext() , tempScheduleList);
        holder.scheduleListView.setAdapter(myScheduleDayAdapter);

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) holder.scheduleListView.getLayoutParams();
        lp.height = h;
        holder.scheduleListView.setLayoutParams(lp);
        //holder.scheduleListView.setAdapter();

        return rowView;
    }
}
