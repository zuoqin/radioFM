package fm100.co.il.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import fm100.co.il.R;
import fm100.co.il.models.Channel;

import java.util.ArrayList;

/************************************************
 * Adapter For the Channel(Station) List from Music class
 ************************************************/

public class ChannelListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Channel> nameObjArray;
    LayoutInflater inflater = null;
    Channel tempValues;

    public ChannelListAdapter(Context context , ArrayList<Channel> namesList) {
        this.context = context;
        this.nameObjArray = namesList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return nameObjArray.size();
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
        public TextView channelListItem;
        //public String channelUrl;
       // public TextView channelName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.channel_row_layout , parent , false);

            holder = new ViewHolder();
            holder.channelListItem = (TextView) rowView.findViewById(R.id.nameText);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (nameObjArray.size() <= 0){
            holder.channelListItem.setText("There is no Names to display");
        }
        else{
            tempValues = null;
            tempValues = nameObjArray.get(position);

            holder.channelListItem.setText(tempValues.getChannelName());
        }

        return rowView;
    }
}
