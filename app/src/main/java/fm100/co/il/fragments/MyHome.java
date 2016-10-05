package fm100.co.il.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fm100.co.il.MainActivity;
import fm100.co.il.adapters.MyFragmentPagerAdapter;
import fm100.co.il.inner.fragments.Music;
import fm100.co.il.inner.fragments.Schedule;
import fm100.co.il.inner.fragments.Video;
import fm100.co.il.inner.fragments.Running;
import fm100.co.il.R;

public class MyHome extends Fragment implements OnTabChangeListener,
		OnPageChangeListener {

	private TabHost tabHost;
	private ViewPager viewPager;
	private static MyFragmentPagerAdapter myViewPagerAdapter;
	int i = 0;
	List<Fragment> fragmentsList;
	View v;
	Bundle state;

	Music music = null;
	Video video = null;
	Running running = null;

	ActionBar bar = null;
	DrawerLayout drawer = null;
	RelativeLayout pane = null;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		state = savedInstanceState;

		v = inflater.inflate(R.layout.tabs_viewpager_layout, container, false);
		i++;
		// init tabhost
		this.initializeTabHost(savedInstanceState);
		// init ViewPager
		this.initializeViewPager();
		// setting limit so the fragments wont get paused
		viewPager.setOffscreenPageLimit(5);
		//setting default tab (opening tab)

		return v;
	}
	// fake(temp) content for tabhost
	class FakeContent implements TabContentFactory {
		private final Context mContext;

		public FakeContent(Context context) {
			mContext = context;
		}
		@Override
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumHeight(0);
			v.setMinimumWidth(0);
			return v;
		}
	}

	private void initializeViewPager() {
		fragmentsList = new Vector<>();

		//if(isRTL()==true){
		//	fragmentsList.add(new Schedule());
		//	fragmentsList.add(running = new Running());
		//	fragmentsList.add(video = new Video());
		//	fragmentsList.add(music = new Music());
		//}else {
		fragmentsList.add(music = new Music());
		fragmentsList.add(video = new Video());
		//fragmentsList.add(running = new Running());
		fragmentsList.add(new Schedule());
		//}

		this.myViewPagerAdapter = new MyFragmentPagerAdapter(
				getChildFragmentManager(), fragmentsList);
		this.viewPager = (ViewPager) v.findViewById(R.id.viewPager);
		this.viewPager.setAdapter(this.myViewPagerAdapter);
		this.viewPager.setOnPageChangeListener(this);

		if(isRTL()==true){
			//tabHost.setCurrentTab(1);
			//tabHost.getCurrentTabTag();
			tabHost.setCurrentTabByTag("music");
			viewPager.setCurrentItem(0);
		}else {
			tabHost.setCurrentTab(0);

		}
	}

	private void initializeTabHost(Bundle args) {
		tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		tabHost.setup();

		//set default tabs according to titles
		Drawable video = getResources().getDrawable(R.drawable.video_off);
		Drawable music = getResources().getDrawable(R.drawable.radio_on);
		Drawable running = getResources().getDrawable(R.drawable.run_off);
		Drawable schedule = getResources().getDrawable(R.drawable.menu_off);

		Drawable[] drawables = new Drawable[]{music, video, /*running ,*/schedule};

		//Drawable[] drawables = new Drawable[]{schedule, running, video ,music};

		setTabsIcons(drawables);
		tabHost.setOnTabChangedListener(this);
	}

	public void setStationData(JSONObject obj) throws JSONException {
		if( music != null ) {
			music.setStationData(obj);
		}
		if( video != null ) {
			video.setStationData(obj);
		}
	}

	public void setHeaderBar(ActionBar bar, DrawerLayout drawer, RelativeLayout drawerPane) {
		this.bar = bar;
		this.bar.hide();

		this.drawer = drawer;
		this.pane = drawerPane;
	}

	public void openSubmenu() {
		this.drawer.openDrawer(this.pane);
	}

	private void setTabsIcons(Drawable[] drawables) {
		String[] tabsNames = new String[]{"music", "video", /*"running" ,*/ "schedule"};
		String[] tabsLabel = new String[]{"ערוצים דיגיטליים", "שידור לייב", /*"running" ,*/ "לוח שידורים"};
		/*if(isRTL()==true){
			tabsNames = new String[]{"schedule", "running", "video" , "music"};
		}else {
			tabsNames = new String[]{"music", "video", "running" , "schedule"};
		}*/

		for (int i=0 ; i < drawables.length ; i++){
			TabHost.TabSpec myTabSpec = tabHost.newTabSpec(tabsNames[i]);
			myTabSpec.setIndicator(getTabIndicator(tabHost.getContext(), drawables[i], tabsLabel[i])); //getTabIndicator: new function to inject own tab layout
			myTabSpec.setContent(new FakeContent(getActivity()));
			tabHost.addTab(myTabSpec);
		}
	}
	// Set own tab layout
	private View getTabIndicator(Context context, Drawable drawable, String label) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_widget_layout, null);
		ImageView iv = (ImageView) view.findViewById(R.id.tabImage);
		iv.setImageDrawable(drawable);
		TextView txt = (TextView) view.findViewById(R.id.txtLabel);
		txt.setText(label);
		return view;
	}

	@Override
	public void onTabChanged(String tabId) {
		if(null != getActivity() ) {
			int pos = this.tabHost.getCurrentTab();

			if(isRTL()==true){
				this.viewPager.setCurrentItem(fragmentsList.size() - 1 - pos);
			}else {
				this.viewPager.setCurrentItem(pos);
			}

			//this.viewPager.setCurrentItem(pos);
			//method makes tab selected icon change to selected icon(with circle)
			highlightCurrentTab(pos);

			/*HorizontalScrollView hScrollView = (HorizontalScrollView) v
					.findViewById(R.id.hScrollView);
			View tabView = tabHost.getCurrentTabView();
			int scrollPos = tabView.getLeft()
					- (hScrollView.getWidth() - tabView.getWidth()) / 2;
			hScrollView.smoothScrollTo(scrollPos, 0);*/


			//getSupportActionBar().hide();
		}
	}

	private void highlightCurrentTab(int selectedItem) {
		int [] resurce_off = new int[] {R.drawable.radio_off, R.drawable.video_off/*, R.drawable.run_off*/, R.drawable.menu_off};
		int [] resurce_on = new int[] {R.drawable.radio_on, R.drawable.video_on/*, R.drawable.run_on*/, R.drawable.menu_on};

		//int [] resurce_off = new int[] {R.drawable.menu_off, /*R.drawable.run_off, */R.drawable.video_off, R.drawable.radio_off};
		//int [] resurce_on = new int[] {R.drawable.menu_on, /*R.drawable.run_on, */R.drawable.video_on, R.drawable.radio_on};


		for( int i = 0; i < resurce_off.length; i++ ) {
			ImageView iv = (ImageView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.tabImage);
			iv.setImageResource(resurce_off[i]);
		}
		ImageView iv = (ImageView) tabHost.getTabWidget().getChildAt(selectedItem).findViewById(R.id.tabImage);
		iv.setImageResource(resurce_on[selectedItem]);

		/*if( selectedItem == 0 ) {
			bar.hide();
		} else {
			bar.show();
		}*/
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		if(null != getActivity() ) {
			// set current tab set for flipped order to make the swipe go the right way
			if(isRTL()==true){
				this.tabHost.setCurrentTab(fragmentsList.size() - 1 - position);
			}else {
				this.tabHost.setCurrentTab(position);
			}
			//this.tabHost.setCurrentTab(position);
		}
	}

	private boolean isRTL() {
		Locale defLocale = Locale.getDefault();
		return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
	}

}
