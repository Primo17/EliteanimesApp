package de.btcdev.eliteanimesapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.PN;

public class PNAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<PN> list;

    public PNAdapter(Context context, ArrayList<PN> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= list.size()) {
            TextView more = new TextView(context);
            more.setText("\n"
                    + context.getResources().getString(R.string.pns_more));
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 60, context.getResources().getDisplayMetrics());
            more.setHeight(px);
            more.setTypeface(more.getTypeface(), Typeface.BOLD);
            more.setGravity(Gravity.CENTER_HORIZONTAL);
            return more;
        }
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.pn_layout)
            rowView = inflater.inflate(R.layout.pn_layout, parent, false);
        else
            rowView = convertView;

        TextView betreff = (TextView) rowView.findViewById(R.id.pn_betreff);
        TextView absender = (TextView) rowView.findViewById(R.id.pn_absender);
        TextView datum = (TextView) rowView.findViewById(R.id.pn_datum);
        PN pn = list.get(position);
        betreff.setText(Html.fromHtml(pn.getBetreff()));
        if (!pn.getGelesen())
            betreff.setTypeface(Typeface.DEFAULT_BOLD);
        absender.setText(Html.fromHtml(pn.getBenutzername()));
        datum.setText(pn.getDate());
        return rowView;
    }

    @Override
    public int getCount() {
        return list.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position >= list.size())
            return null;
        else
            return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
