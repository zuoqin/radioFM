package com.nxcast.stations.il.fm100.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<Drawable> colors;

    public DataAdapter(ArrayList<Drawable> colors) {
        this.colors = colors;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {

       // viewHolder.tv_country.setText(countries.get(i));
        viewHolder.tv_country.setBackground(colors.get(i));
        viewHolder.card.setPreventCornerOverlap(false);
        //viewHolder.card.setBackgroundColor(Color.RED);

    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_country;
        private CardView card;

        public ViewHolder(View view) {
            super(view);
            card = (CardView) view.findViewById(R.id.card);
            tv_country = (TextView)view.findViewById(R.id.tv_country);

            Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");

            tv_country.setTypeface(custom_font_eng_light);
        }
    }

}