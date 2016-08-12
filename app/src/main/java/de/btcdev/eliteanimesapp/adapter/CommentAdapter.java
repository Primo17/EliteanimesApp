package de.btcdev.eliteanimesapp.adapter;

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
import android.util.TypedValue;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Comment;

public class CommentAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Comment> commentList;
    private ArrayList<Boolean> spoilerArray;

    public CommentAdapter(Context context, ArrayList<Comment> commentList, ArrayList<Boolean> spoilerArray) {
        this.context = context;
        this.commentList = commentList;
        this.spoilerArray = spoilerArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= commentList.size()) {
            TextView more = new TextView(context);
            more.setText("\n"
                    + context.getResources().getString(R.string.comments_more));
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 60, context.getResources().getDisplayMetrics());
            more.setHeight(px);
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
        ImageView profilePicture = (ImageView) rowView
                .findViewById(R.id.comment_img);
        TextView text = (TextView) rowView.findViewById(R.id.comment_text);
        Comment comment = commentList.get(position);
        name.setText(comment.getUserName());
        date.setText(comment.getDate());
        profilePicture.setImageBitmap(comment.getAvatar());
        String content = comment.getText();
        if (content.contains("spoiler")) {
            content = new EAParser(null).showSpoiler(spoilerArray.get(position) == true, content);
        }
        text.setText(Html.fromHtml(content));
        return rowView;
    }

    @Override
    public int getCount() {
        return commentList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < commentList.size())
            return commentList.get(position);
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
