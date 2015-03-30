package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Freundschaftsanfrage;

public class BlockedAdapter extends ArrayAdapter<Object> {

    private final Context context;
    private final ArrayList<Freundschaftsanfrage> list;

    public BlockedAdapter(Context context, ArrayList<Freundschaftsanfrage> list) {
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
        Freundschaftsanfrage f = list.get(position);
        name.setText(f.getName());
        alter.setText(f.getAlter());
        if (f.getStatus())
            status.setText("Online");
        else
            status.setText("Offline");
        return rowView;
    }
}
