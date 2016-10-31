package com.nxcast.stations.il.fm100.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nxcast.stations.il.fm100.models.Station;
import com.squareup.picasso.Picasso;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.models.Channel;

import java.util.ArrayList;

import static com.nxcast.stations.il.fm100.R.drawable.fm100;

/************************************************
 * Adapter For the Channel(Station) List from Music class
 ************************************************/

public class ChannelListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Station> nameObjArray;
    LayoutInflater inflater = null;
    Station tempValues;

    public ChannelListAdapter(Context context , ArrayList<Station> namesList) {
        this.context = context;
        this.nameObjArray = namesList;

        if( context != null ) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
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
        public ImageView channelItemImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.channel_row_layout , parent , false);
            holder = new ViewHolder();
            holder.channelItemImage = (ImageView) rowView.findViewById(R.id.channelRowIv);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (nameObjArray.size() <= 0){
            //Toast.makeText(MainActivity.getMyApplicationContext(), "no stations to display" , Toast.LENGTH_LONG).show();
        }
        else{
            tempValues = null;
            tempValues = nameObjArray.get(position);

            Picasso.with(context).load(tempValues.getStationLogo()).into(holder.channelItemImage);
        }

        return rowView;
    }


}
