package fm100.co.il.adapters;

import android.content.Context;
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
        public ImageView stationItemImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.station_draweritem_layout, parent , false);
            holder = new ViewHolder();
            holder.stationItemImage = (ImageView) rowView.findViewById(R.id.stationItemIv);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (nameObjArray.size() <= 0){
            Toast.makeText(MainActivity.getMyApplicationContext(), "no stations to display", Toast.LENGTH_LONG).show();
        }
        else{
            tempValues = null;
            tempValues = nameObjArray.get(position);

            Picasso.with(context).load(tempValues.getChannelLogo()).into(holder.stationItemImage);
        }

        return rowView;
    }
}
