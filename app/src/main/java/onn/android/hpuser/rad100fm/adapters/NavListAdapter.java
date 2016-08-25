package onn.android.hpuser.rad100fm.adapters;

import java.util.List;

import onn.android.hpuser.rad100fm.models.NavItem;
import onn.android.hpuser.rad100fm.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/************************************************
 * The adapter for the nav items in the drawer
 ************************************************/

public class NavListAdapter extends ArrayAdapter<NavItem>{
	
	Context context;
	int resLayout;
	List<NavItem> listNavItems;

	public NavListAdapter(Context context, int resLayout, List<NavItem> listNavItems) {
		super(context, resLayout, listNavItems);
		
		this.context = context;
		this.resLayout = resLayout;
		this.listNavItems = listNavItems;
	}
	
	@SuppressLint("ViewHolder") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = View.inflate(context, resLayout, null);
		
		TextView tvTitle  = (TextView) v.findViewById(R.id.title);
		TextView tvSubTitle = (TextView) v.findViewById(R.id.subtitle);
		ImageView navIcon =  (ImageView) v.findViewById(R.id.nav_icon);
		
		NavItem navItem = listNavItems.get(position);
		
		tvTitle.setText(navItem.getTitle());
		tvSubTitle.setText(navItem.getSubTitle());
		navIcon.setImageResource(navItem.getResIcon());
		
		return v;
	}

}
