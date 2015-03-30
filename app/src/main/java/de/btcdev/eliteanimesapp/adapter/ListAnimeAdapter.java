package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.ListAnime;

public class ListAnimeAdapter extends ArrayAdapter<ListAnime> {
    private final Context context;
    private final ArrayList<ListAnime> list;

    public ListAnimeAdapter(Context context, ArrayList<ListAnime> list) {
        super(context, R.layout.listanime_layout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.listanime_layout)
            rowView = inflater.inflate(R.layout.listanime_layout, parent,
                    false);
        else
            rowView = convertView;
        TextView pos = (TextView) rowView.findViewById(R.id.listanime_position);
        TextView titel = (TextView) rowView.findViewById(R.id.listanime_titel);
        TextView folgen = (TextView) rowView
                .findViewById(R.id.listanime_folgen);
        TextView bewertung = (TextView) rowView
                .findViewById(R.id.listanime_bewertung);
        ListAnime a = list.get(position);
        pos.setText("" + (position + 1));
        titel.setText(a.getTitel());
        folgen.setText(a.getFortschritt() + "/" + a.getFolgenAnzahl());
        bewertung.setText("" + a.getBewertung());
        return rowView;
    }
}
