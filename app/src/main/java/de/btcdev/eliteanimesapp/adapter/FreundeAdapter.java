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
import de.btcdev.eliteanimesapp.data.Freund;

public class FreundeAdapter extends ArrayAdapter<Freund> {

    private final Context context;
    private final ArrayList<Freund> list;

    public FreundeAdapter(Context context, ArrayList<Freund> list) {
        super(context, R.layout.freundesliste, list);
        this.context = context;
        this.list = list;

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
        TextView datum = (TextView) rowView
                .findViewById(R.id.freund_zuletzt_online);
        TextView alter = (TextView) rowView.findViewById(R.id.freund_alter);
        Freund f = list.get(position);
        if (f.getStatus())
            status.setImageResource(R.drawable.online);
        name.setText(f.getName());
        alter.setText(f.getAlter());
        datum.setText(f.getDatum());
        return rowView;
    }
}
