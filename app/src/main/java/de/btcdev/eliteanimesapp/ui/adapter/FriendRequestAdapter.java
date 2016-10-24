package de.btcdev.eliteanimesapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.models.FriendRequest;

public class FriendRequestAdapter extends ArrayAdapter<Object> {

    private final Context context;
    private final ArrayList<FriendRequest> friendRequests;

    public FriendRequestAdapter(Context context, ArrayList<FriendRequest> friendRequests) {
        super(context, R.layout.freundesanfragen_layout, friendRequests.toArray());
        this.context = context;
        this.friendRequests = friendRequests;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        rowView = inflater.inflate(R.layout.freundschaftsanfrage_layout, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.anfrage_name);
        TextView alter = (TextView) rowView.findViewById(R.id.anfrage_alter);
        RadioButton button1 = (RadioButton) rowView.findViewById(R.id.anfrage_annehmen);
        RadioButton button2 = (RadioButton) rowView.findViewById(R.id.anfrage_ablehnen);
        button1.setId(1000 + position);
        button2.setId(2000 + position);
        FriendRequest f = friendRequests.get(position);
        name.setText(f.getName());
        alter.setText(f.getAge());
        return rowView;
    }
}
