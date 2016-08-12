package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Configuration;
import de.btcdev.eliteanimesapp.data.NavDrawerItem;
import de.btcdev.eliteanimesapp.gui.ParentActivity;

public class NavDrawerListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;

	public NavDrawerListAdapter(Context context,
			ArrayList<NavDrawerItem> navDrawerItems) {
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getId() != R.id.navigation_drawer_item) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.navigation_drawer_item,
					null);
		}

		ImageView imgIcon = (ImageView) convertView
				.findViewById(R.id.nav_item_icon);
		TextView textTitle = (TextView) convertView
				.findViewById(R.id.nav_item_title);
		TextView textCount = (TextView) convertView
				.findViewById(R.id.nav_item_counter);
		imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
		textTitle.setText(navDrawerItems.get(position).getTitle());

		if (navDrawerItems.get(position).getCounterVisibility()) {
			if(position == ParentActivity.NAVIGATION_COMMENTS)
				textCount.setText(""+ Configuration.getNewCommentCount());
			else if(position == ParentActivity.NAVIGATION_PRIVATE_MESSAGES)
				textCount.setText(""+ Configuration.getNewMessageCount());
//			textCount.setMessage(navDrawerItems.get(position).getCount());
		} else {
			textCount.setVisibility(View.GONE);
		}
		return convertView;
	}

}
