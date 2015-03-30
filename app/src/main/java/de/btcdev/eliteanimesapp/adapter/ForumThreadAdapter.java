package de.btcdev.eliteanimesapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.ForumThread;
import de.btcdev.eliteanimesapp.data.Subforum;

public class ForumThreadAdapter extends BaseAdapter {

    private ArrayList<ForumThread> threadList;
    private ArrayList<Subforum> subList;
    private Context context;
    private int subCount;
    private int threadCount;

    public ForumThreadAdapter(Context context,
                              ArrayList<ForumThread> threadList, ArrayList<Subforum> subList) {
        this.context = context;
        setLists(threadList, subList);
        setCounts();
    }

    @Override
    public int getCount() {
        return subCount + threadCount;
    }

    @Override
    public Object getItem(int position) {
        if (position < subCount)
            return subList.get(position);
        else
            return threadList.get(position - subCount);
    }

    @Override
    public long getItemId(int position) {
        if (position < subCount)
            return subList.get(position).getId();
        else
            return threadList.get(position - subCount).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (position < subCount) {
            // subforen
            View rowView;
            if (convertView == null || convertView.getId() != R.id.foren_sub)
                rowView = inflater.inflate(R.layout.foren_sub, parent, false);
            else
                rowView = convertView;
            TextView name = (TextView) rowView
                    .findViewById(R.id.foren_sub_name);
            Subforum sf = subList.get(position);
            StringBuilder builder = new StringBuilder();
            builder.append("Subforum: ");
            builder.append(sf.getName());
            name.setText(Html.fromHtml(builder.toString()));
            return rowView;
        } else {
            // threads
            View rowView;
            if (convertView == null || convertView.getId() != R.id.forum_threads)
                rowView = inflater.inflate(R.layout.forum_threads, parent,
                        false);
            else
                rowView = convertView;
            TextView titel = (TextView) rowView
                    .findViewById(R.id.forum_threads_titel);
            TextView erstellt = (TextView) rowView
                    .findViewById(R.id.forum_threads_erstellt);
            TextView lpdate = (TextView) rowView
                    .findViewById(R.id.forum_threads_lpdate);
            TextView lpdate2 = (TextView) rowView
                    .findViewById(R.id.forum_threads_lpdate2);
            ImageView image = (ImageView) rowView
                    .findViewById(R.id.forum_threads_image);
            ForumThread ft = threadList.get(position - subCount);
            titel.setText(Html.fromHtml(ft.getName()));
            StringBuilder builder = new StringBuilder();
            builder.append("Erstellt am ");
            builder.append(ft.getCreateDate());
            builder.append(" von <font color=\"blue\">");
            builder.append(ft.getCreateName());
            builder.append("</font>");
            erstellt.setText(Html.fromHtml(builder.toString()));
            lpdate.setText("Letzter Beitrag:");
            builder = new StringBuilder();
            builder.append(ft.getLastPostDate());
            builder.append(" von <font color=\"blue\">");
            builder.append(ft.getLastPostName());
            builder.append("</font>");
            lpdate2.setText(Html.fromHtml(builder.toString()));
            builder = null;
            if (ft.isClosed())
                image.setImageDrawable(rowView.getResources().getDrawable(
                        R.drawable.thread_closed));
            else if (ft.isUnread())
                image.setImageDrawable(rowView.getResources().getDrawable(
                        R.drawable.thread_new));
            else
                image.setImageDrawable(rowView.getResources().getDrawable(
                        R.drawable.thread_read));
            return rowView;
        }
    }

    public void setCounts() {
        if (subList == null)
            subCount = 0;
        else
            subCount = subList.size();
        if (threadList == null)
            threadCount = 0;
        else
            threadCount = threadList.size();
    }

    public void setLists(ArrayList<ForumThread> threadList,
                         ArrayList<Subforum> subList) {
        this.subList = subList;
        this.threadList = threadList;
    }
}
