package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Friend;

public class FriendAdapter extends ArrayAdapter<Friend> {

    private final Context context;
    private final ArrayList<Friend> friendList;

    public FriendAdapter(Context context, ArrayList<Friend> list) {
        super(context, R.layout.freundesliste, list);
        this.context = context;
        this.friendList = list;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.freunde_layout)
            rowView = inflater.inflate(R.layout.freundesliste, parent, false);
        else
            rowView = convertView;
        ImageView status = (ImageView) rowView.findViewById(R.id.icon);
        TextView name = (TextView) rowView.findViewById(R.id.freund_name);
        TextView date = (TextView) rowView
                .findViewById(R.id.freund_zuletzt_online);
        TextView age = (TextView) rowView.findViewById(R.id.freund_alter);
        Friend friend = friendList.get(position);
        if (friend.getStatus())
            status.setImageResource(R.drawable.online);
        name.setText(friend.getName());
        age.setText(friend.getAge());
        date.setText(friend.getDate());
        return rowView;
    }
}
