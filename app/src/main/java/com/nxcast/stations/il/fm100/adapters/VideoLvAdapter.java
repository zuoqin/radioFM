package com.nxcast.stations.il.fm100.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.models.VideoObj;

public class VideoLvAdapter  extends BaseAdapter {
    Context context;
    private List<VideoObj> videoList = new ArrayList<>();
    LayoutInflater inflater = null;
    VideoObj tempVideo;

    public VideoLvAdapter(Context context , List<VideoObj> videoList) {
        this.context = context;
        this.videoList = videoList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return videoList.size();
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
        public TextView videoTitleText;
        public TextView videoPublishedText;
        public ImageView videoImageView;

        //public String channelUrl;
        // public TextView channelName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null){
            rowView = inflater.inflate(R.layout.video_list_item , parent , false);

            holder = new ViewHolder();
            holder.videoTitleText = (TextView) rowView.findViewById(R.id.vidTitleTv);
            holder.videoPublishedText = (TextView) rowView.findViewById(R.id.vidSubTv);
            holder.videoImageView = (ImageView) rowView.findViewById(R.id.videoItemIv);

            Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

            holder.videoTitleText.setTypeface(custom_font_heb_regular);
            holder.videoPublishedText.setTypeface(custom_font_heb_regular);

            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }
        if (videoList.size() <= 0){
            holder.videoTitleText.setText("There is no data to display");
            holder.videoPublishedText.setText("There is no data to display");
            holder.videoImageView.setImageResource(R.drawable.noimage);
        }
        else{
            tempVideo = null;
            tempVideo = videoList.get(position);

            holder.videoTitleText.setText(tempVideo.getTitle());

            if( !tempVideo.getPublished().isEmpty() ) {
                String reDate = tempVideo.getPublished().substring(0 , 10);
                holder.videoPublishedText.setText(reDate);
            } else {
                holder.videoPublishedText.setText("");
            }



           // Log.e("mynewlog", tempVideo.getThumbnail().toString());

            Picasso.with(context).load(tempVideo.getThumbnail()).into(holder.videoImageView);
                    //.setImageResource(R.drawable.noimage);
                    //.setImageDrawable(LoadImageFromWebOperations(tempVideo.getThumbnail()));
        }

        return rowView;
    }

}


