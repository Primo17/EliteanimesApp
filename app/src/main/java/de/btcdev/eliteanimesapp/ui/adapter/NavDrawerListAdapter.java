package de.btcdev.eliteanimesapp.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.models.NavDrawerItem;
import de.btcdev.eliteanimesapp.data.services.ConfigurationService;
import de.btcdev.eliteanimesapp.ui.activities.ParentActivity;

public class NavDrawerListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private ConfigurationService configurationService;

	public static NavDrawerListAdapter instance(Context context,
												ArrayList<NavDrawerItem> items,
												ConfigurationService configurationService) {
		return new NavDrawerListAdapter(context, items, configurationService);
	}

	private NavDrawerListAdapter(Context context,
								 ArrayList<NavDrawerItem> navDrawerItems,
								 ConfigurationService configurationService) {
		this.context = context;
		this.navDrawerItems = navDrawerItems;
		this.configurationService = configurationService;
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
				textCount.setText(""+ configurationService.getNewCommentCount());
			else if(position == ParentActivity.NAVIGATION_PRIVATE_MESSAGES)
				textCount.setText(""+ configurationService.getNewMessageCount());
//			textCount.setMessage(navDrawerItems.get(position).getCount());
		} else {
			textCount.setVisibility(View.GONE);
		}
		return convertView;
	}

}
