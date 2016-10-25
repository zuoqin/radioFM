package com.nxcast.stations.il.fm100.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ButtonBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.nxcast.stations.il.fm100.MainActivity;
import com.nxcast.stations.il.fm100.R;
import com.nxcast.stations.il.fm100.adapters.MyFragmentPagerAdapter;
import com.nxcast.stations.il.fm100.inner.fragments.Music;
import com.nxcast.stations.il.fm100.inner.fragments.Running;
import com.nxcast.stations.il.fm100.inner.fragments.Schedule;
import com.nxcast.stations.il.fm100.inner.fragments.Video;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MyRun extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.running_activity);

		Typeface custom_font_eng_light = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");
		Typeface custom_font_heb_regular = Typeface.createFromAsset(MainActivity.getMyApplicationContext().getAssets(), "fonts/FbSpoilerRegular.ttf");

		((TextView) findViewById(R.id.distanceTitleTv)).setTypeface(custom_font_eng_light);
		((TextView) findViewById(R.id.timerTitleTv)).setTypeface(custom_font_eng_light);
		((TextView) findViewById(R.id.speedTitleTv)).setTypeface(custom_font_eng_light);
		((TextView) findViewById(R.id.txtCalTitle)).setTypeface(custom_font_eng_light);
		((TextView) findViewById(R.id.txtSpeedToKmTitle)).setTypeface(custom_font_eng_light);

		((TextView) findViewById(R.id.distanceTv)).setTypeface(custom_font_heb_regular);
		((TextView) findViewById(R.id.speedTv)).setTypeface(custom_font_heb_regular);
		((TextView) findViewById(R.id.timerTv)).setTypeface(custom_font_heb_regular);
		((TextView) findViewById(R.id.txtCal)).setTypeface(custom_font_heb_regular);
		((TextView) findViewById(R.id.txtSpeedToKm)).setTypeface(custom_font_heb_regular);


		Button close = (Button) findViewById(R.id.btnClose);
		close.setTypeface(custom_font_heb_regular);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
