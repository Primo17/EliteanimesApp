package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Kommentar;

public class KommentarAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Kommentar> list;
    private ArrayList<Boolean> spoilerArray;

    public KommentarAdapter(Context context, ArrayList<Kommentar> list, ArrayList<Boolean> spoilerArray) {
        this.context = context;
        this.list = list;
        this.spoilerArray = spoilerArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= list.size()) {
            TextView more = new TextView(context);
            more.setText("\n"
                    + context.getResources().getString(R.string.comments_more));
            more.setHeight(70);
            more.setTypeface(more.getTypeface(), Typeface.BOLD);
            more.setGravity(Gravity.CENTER_HORIZONTAL);
            return more;
        }
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.kommentar_layout)
            rowView = inflater.inflate(R.layout.kommentar_layout, parent,
                    false);
        else
            rowView = convertView;

        TextView name = (TextView) rowView.findViewById(R.id.comment_name);
        TextView date = (TextView) rowView.findViewById(R.id.comment_date);
        ImageView profilbild = (ImageView) rowView
                .findViewById(R.id.comment_img);
        TextView text = (TextView) rowView.findViewById(R.id.comment_text);
        Kommentar kommi = list.get(position);
        name.setText(kommi.getBenutzername());
        date.setText(kommi.getDate());
        profilbild.setImageBitmap(kommi.getBild());
        String content = kommi.getText();
        if (content.contains("spoiler")) {
            content = new EAParser(null).showSpoiler(spoilerArray.get(position) == true, content);
        }
        text.setText(Html.fromHtml(content));
        return rowView;
    }

    @Override
    public int getCount() {
        return list.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < list.size())
            return list.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateSpoilerArray(ArrayList<Boolean> spoilerArray) {
        this.spoilerArray = spoilerArray;
    }
}
