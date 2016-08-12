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
import de.btcdev.eliteanimesapp.data.PrivateMessage;

public class PrivateMessageAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<PrivateMessage> privateMessages;

    public PrivateMessageAdapter(Context context, ArrayList<PrivateMessage> privateMessages) {
        this.context = context;
        this.privateMessages = privateMessages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= privateMessages.size()) {
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

        TextView subject = (TextView) rowView.findViewById(R.id.pn_betreff);
        TextView sender = (TextView) rowView.findViewById(R.id.pn_absender);
        TextView date = (TextView) rowView.findViewById(R.id.pn_datum);
        PrivateMessage privateMessage = privateMessages.get(position);
        subject.setText(Html.fromHtml(privateMessage.getBetreff()));
        if (!privateMessage.getGelesen())
            subject.setTypeface(Typeface.DEFAULT_BOLD);
        sender.setText(Html.fromHtml(privateMessage.getBenutzername()));
        date.setText(privateMessage.getDate());
        return rowView;
    }

    @Override
    public int getCount() {
        return privateMessages.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position >= privateMessages.size())
            return null;
        else
            return privateMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
