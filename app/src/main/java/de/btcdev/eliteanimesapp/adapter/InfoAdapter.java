package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;

public class InfoAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> ueberschrift;
    private final ArrayList<String> normaltext;

    public InfoAdapter(Context context, ArrayList<String> ueberschrift,
                       ArrayList<String> normaltext) {
        super(context, R.layout.infolayout, ueberschrift);
        this.context = context;
        this.ueberschrift = ueberschrift;
        this.normaltext = normaltext;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.infolayout)
            rowView = inflater.inflate(R.layout.infolayout, parent, false);
        else
            rowView = convertView;
        TextView fett = (TextView) rowView
                .findViewById(R.id.infolayout_ueberschrift);
        TextView normal = (TextView) rowView
                .findViewById(R.id.infolayout_normaltext);

        fett.setText(ueberschrift.get(position));
        normal.setText(normaltext.get(position));
        return rowView;
    }

}
