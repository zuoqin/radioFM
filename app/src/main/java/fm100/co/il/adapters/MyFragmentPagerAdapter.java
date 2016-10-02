package fm100.co.il.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/************************************************
 * The pager adapter of the 3 fragments Video/Music/Running for the ViewPager from MyHome class
 ************************************************/

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	List<Fragment> fragments;


	public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {

		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		
		return fragments.size();
	}



}
