package de.btcdev.eliteanimesapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.models.ListAnime;

public class ListAnimeAdapter extends ArrayAdapter<ListAnime> {
    private final Context context;
    private final ArrayList<ListAnime> animeList;

    public ListAnimeAdapter(Context context, ArrayList<ListAnime> animeList) {
        super(context, R.layout.listanime_layout, animeList);
        this.context = context;
        this.animeList = animeList;
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
        TextView title = (TextView) rowView.findViewById(R.id.listanime_titel);
        TextView episodes = (TextView) rowView
                .findViewById(R.id.listanime_folgen);
        TextView rating = (TextView) rowView
                .findViewById(R.id.listanime_bewertung);
        ListAnime anime = animeList.get(position);
        pos.setText("" + (position + 1));
        title.setText(anime.getTitle());
        episodes.setText(anime.getProgress() + "/" + anime.getEpisodeCount());
        rating.setText("" + anime.getRating());
        return rowView;
    }
}
