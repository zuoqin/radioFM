package fm100.co.il.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.models.ScheduleItem;
import fm100.co.il.models.VideoObj;

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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.schedule_list_item , parent , false);

            holder = new ViewHolder();
            holder.scheduleTv = (TextView) rowView.findViewById(R.id.dayTv);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        tempItem = null;
        tempItem = scheduleList.get(position);

        holder.scheduleTv.setText(daysList[position]);

        return rowView;
    }
}
