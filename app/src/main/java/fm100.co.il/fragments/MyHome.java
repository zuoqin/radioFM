package fm100.co.il.fragments;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

import fm100.co.il.adapters.MyFragmentPagerAdapter;
import fm100.co.il.inner.fragments.Music;
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
		viewPager.setOffscreenPageLimit(4);
		//setting default tab (opening tab)
		tabHost.setCurrentTab(1);

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

			fragmentsList.add(new Video());
			fragmentsList.add(new Music());
			fragmentsList.add(new Running());

		this.myViewPagerAdapter = new MyFragmentPagerAdapter(
				getChildFragmentManager(), fragmentsList);
		this.viewPager = (ViewPager) v.findViewById(R.id.viewPager);
		this.viewPager.setAdapter(this.myViewPagerAdapter);
		this.viewPager.setOnPageChangeListener(this);
	}

	private void initializeTabHost(Bundle args) {
		tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		tabHost.setup();

		//set default tabs according to titles
		Drawable video = getResources().getDrawable(R.drawable.video_off);
		Drawable music = getResources().getDrawable(R.drawable.radio_off);
		Drawable running = getResources().getDrawable(R.drawable.run_off);

		Drawable[] drawables = {video, music , running };

		setTabsIcons(drawables);
		tabHost.setOnTabChangedListener(this);
	}

	private void setTabsIcons(Drawable[] drawables) {
		String[] tabsNames = {"video" , "music" , "running"};

		for (int i=drawables.length-1 ; i>=0 ; i--){
			TabHost.TabSpec myTabSpec = tabHost.newTabSpec(tabsNames[i]);
			myTabSpec.setIndicator(getTabIndicator(tabHost.getContext(), drawables[i] )); //getTabIndicator: new function to inject own tab layout
			myTabSpec.setContent(new FakeContent(getActivity()));
			tabHost.addTab(myTabSpec);
		}
	}
	// Set own tab layout
	private View getTabIndicator(Context context, Drawable drawable) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_widget_layout, null);
		ImageView iv = (ImageView) view.findViewById(R.id.tabImage);
		iv.setImageDrawable(drawable);
		return view;
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.e("MyLog", "does it fall here onTabChange???");
		if(null != getActivity() ) {
			int pos = this.tabHost.getCurrentTab();
			if(isRTL()==true){
				Log.e("RunLog", "CAN THIS WORK " + "TRUE BABY " + isRTL());
				this.viewPager.setCurrentItem(fragmentsList.size() - 1 - pos);
			}else {
				Log.e("RunLog", "CAN THIS WORK " + "FALSE BABY" + isRTL());
				this.viewPager.setCurrentItem(pos);
			}
			//method makes tab selected icon change to selected icon(with circle)
			highlightCurrentTab(pos);

			HorizontalScrollView hScrollView = (HorizontalScrollView) v
					.findViewById(R.id.hScrollView);
			View tabView = tabHost.getCurrentTabView();
			int scrollPos = tabView.getLeft()
					- (hScrollView.getWidth() - tabView.getWidth()) / 2;
			hScrollView.smoothScrollTo(scrollPos, 0);
		}
	}

	private void highlightCurrentTab(int selectedItem) {
		ImageView videoIv = (ImageView) tabHost.getTabWidget().getChildAt(2).findViewById(R.id.tabImage);
		videoIv.setImageResource(R.drawable.video_off);
		ImageView musicIv = (ImageView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tabImage);
		musicIv.setImageResource(R.drawable.radio_off);
		ImageView runningIv = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tabImage);
		runningIv.setImageResource(R.drawable.run_off);

		switch (selectedItem){
			case 2:
				videoIv = (ImageView) tabHost.getTabWidget().getChildAt(2).findViewById(R.id.tabImage);
				videoIv.setImageResource(R.drawable.video_on);
				break;
			case 1:
				musicIv = (ImageView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tabImage);
				musicIv.setImageResource(R.drawable.radio_on);
				break;
			case 0:
				runningIv = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tabImage);
				runningIv.setImageResource(R.drawable.run_on);
				break;
		}

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
			Log.e("RunLog", "CAN THIS WORK " + isRTL());
			if(isRTL()==true){
				this.tabHost.setCurrentTab(fragmentsList.size() - 1 - position);
				Log.e("RunLog", "CAN THIS WORK " + "TRUE BABY " + isRTL());
			}else {
				this.tabHost.setCurrentTab(position);
				Log.e("RunLog", "CAN THIS WORK " + "FALSE BABY" + isRTL());
			}
		}
	}

	private boolean isRTL() {
		Locale defLocale = Locale.getDefault();
		return  Character.getDirectionality(defLocale.getDisplayName(defLocale).charAt(0)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;
	}

}
