package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Freundschaftsanfrage;

public class AnfragenAdapter extends ArrayAdapter<Object> {

    private final Context context;
    private final ArrayList<Freundschaftsanfrage> list;

    public AnfragenAdapter(Context context, ArrayList<Freundschaftsanfrage> list) {
        super(context, R.layout.freundesanfragen_layout, list.toArray());
        this.context = context;
        this.list = list;
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
        Freundschaftsanfrage f = list.get(position);
        name.setText(f.getName());
        alter.setText(f.getAlter());
        return rowView;
    }
}
