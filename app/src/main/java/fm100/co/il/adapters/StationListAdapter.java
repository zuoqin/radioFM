package fm100.co.il.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.models.Channel;

import java.util.ArrayList;

/************************************************
 * Adapter For the Channel(Station) List from Music class
 ************************************************/
public class StationListAdapter extends BaseAdapter{
    Context context;
    ArrayList<Channel> nameObjArray;
    LayoutInflater inflater = null;
    Channel tempValues;

    public StationListAdapter(Context context , ArrayList<Channel> namesList) {
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
       // public ImageView stationItemImage;
        public TextView stationItemText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.station_draweritem_layout, parent , false);
            holder = new ViewHolder();

            holder.stationItemText = (TextView) rowView.findViewById(R.id.stationItemTv);

            Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");

            holder.stationItemText.setTypeface(custom_font_eng_light);


            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (nameObjArray.size() <= 0){
            //Toast.makeText(MainActivity.getMyApplicationContext(), "no stations to display", Toast.LENGTH_LONG).show();
        }
        else{
            tempValues = null;
            tempValues = nameObjArray.get(position);
            holder.stationItemText.setText(tempValues.getChannelName());

            //Log.i("ufo", "slug " + tempValues.getChannelSlug());
            if( tempValues.getChannelSlug().equals("100FM שידור חי") ) {
                holder.stationItemText.setTextColor(0xFFFFEB3D);
            } else {
                holder.stationItemText.setTextColor(0xFFFFFFFF);
            }
        }

        return rowView;
    }
}
