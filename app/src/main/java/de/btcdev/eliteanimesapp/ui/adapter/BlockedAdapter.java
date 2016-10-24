package de.btcdev.eliteanimesapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.models.FriendRequest;

public class BlockedAdapter extends ArrayAdapter<Object> {

    private final Context context;
    private final ArrayList<FriendRequest> list;

    public BlockedAdapter(Context context, ArrayList<FriendRequest> list) {
        super(context, R.layout.blockierte_benutzer_layout, list.toArray());
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.blockierte_benutzer)
            rowView = inflater.inflate(R.layout.blockierte_benutzer_layout,
                    parent, false);
        else
            rowView = convertView;
        TextView name = (TextView) rowView.findViewById(R.id.blocked_name);
        TextView alter = (TextView) rowView.findViewById(R.id.blocked_alter);
        TextView status = (TextView) rowView.findViewById(R.id.blocked_status);
        FriendRequest f = list.get(position);
        name.setText(f.getName());
        alter.setText(f.getAge());
        if (f.getStatus())
            status.setText("Online");
        else
            status.setText("Offline");
        return rowView;
    }
}
