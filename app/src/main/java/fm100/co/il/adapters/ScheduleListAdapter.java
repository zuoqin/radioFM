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
    //List<ScheduleItem> tempScheduleList = new ArrayList<>();
    private ScheduleDayAdapter myScheduleDayAdapter;

    public ScheduleListAdapter(Context context , List<ScheduleItem> scheduleList) {
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
        public TextView dayTv;
        public ListView scheduleListView;

        public TextView scheduleTv;
        public TextView scheduleAuthor;
        public TextView scheduleTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        ScheduleItem item = scheduleList.get(position);

            rowView = inflater.inflate( item.getTitle().isEmpty() ? R.layout.schedule_item : R.layout.schedule_list_item , parent , false);

            holder = new ViewHolder();
            Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");
            Typeface custom_font_heb_regular2 = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerBlack.ttf");

            if( item.getTitle().isEmpty() ) {
                holder.scheduleTv = (TextView) rowView.findViewById(R.id.programName);
                holder.scheduleAuthor = (TextView) rowView.findViewById(R.id.programAuthor);
                holder.scheduleTime = (TextView) rowView.findViewById(R.id.programTime);

                holder.scheduleTv.setTypeface(custom_font_heb_regular2);
                holder.scheduleTime.setTypeface(custom_font_heb_regular);
                holder.scheduleAuthor.setTypeface(custom_font_heb_regular);
            } else {
                holder.dayTv = (TextView) rowView.findViewById(R.id.dayTv);
                holder.scheduleListView = (ListView) rowView.findViewById(R.id.scheduleItemLv);

                holder.dayTv.setTypeface(custom_font_heb_regular);
            }

            rowView.setTag(holder);

        if( item.getTitle().isEmpty() ) {
            holder.scheduleTv.setText(item.getProgramName());
            holder.scheduleTime.setText(item.getProgramStartHoure());

            if( !item.getProgramAutor().equals("") ) {
                holder.scheduleAuthor.setText(item.getProgramAutor());
            } else {
                holder.scheduleAuthor.setVisibility(View.GONE);
            }
        } else {
            holder.dayTv.setText(item.getTitle());
        }

        return rowView;
    }
}
