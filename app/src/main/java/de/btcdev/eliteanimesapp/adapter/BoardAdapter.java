package de.btcdev.eliteanimesapp.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeMap;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Board;
import de.btcdev.eliteanimesapp.data.Statistics;

public class BoardAdapter extends BaseExpandableListAdapter {

    public static final int ROOTBOARD_MAIN = 4;
    public static final int ROOTBOARD_ANIME = 2;
    public static final int ROOTBOARD_MANGA = 3;
    public static final int ROOTBOARD_GFX = 1;
    public static final int ROOTBOARD_CHAT = 5;
    public static final int STATISTICS = 6;
    private ArrayList<BoardCategory> boardCategories;
    private LayoutInflater inflater;
    private TreeMap<Integer, ArrayList<Board>> boardMap;
    private Statistics statistics;

    public BoardAdapter(Activity activity,
                        TreeMap<Integer, ArrayList<Board>> boardMap, Statistics stat) {
        this.boardMap = boardMap;
        this.statistics = stat;
        boardCategories = new ArrayList<>();
        boardCategories.add(new BoardCategory("Main", ROOTBOARD_MAIN));
        boardCategories.add(new BoardCategory("Anime", ROOTBOARD_ANIME));
        boardCategories.add(new BoardCategory("Manga", ROOTBOARD_MANGA));
        boardCategories.add(new BoardCategory("GFX Bereich", ROOTBOARD_GFX));
        boardCategories.add(new BoardCategory("Plauderecke", ROOTBOARD_CHAT));
        boardCategories.add(new BoardCategory("Statistics", STATISTICS));
        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return boardCategories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition < getGroupCount() - 1) {
            ArrayList<Board> foren = boardMap
                    .get(boardCategories.get(groupPosition).id);
            if (foren == null)
                return 0;
            else
                return foren.size();
        } else
            return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return boardCategories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition < getGroupCount() - 1)
            return boardMap.get(boardCategories.get(groupPosition).id).get(
                    childPosition);
        else
            return statistics;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (groupPosition * 100) + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        BoardCategory of = boardCategories.get(groupPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.foren_group, null);
        }
        TextView name = (TextView) convertView
                .findViewById(R.id.foren_group_name);
        name.setText(of.name);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition < getGroupCount() - 1) {
            // foren
            Board board = (Board) getChild(groupPosition, childPosition);
            if (convertView == null
                    || convertView.getId() != R.id.foren_detail) {
                convertView = inflater.inflate(R.layout.foren_detail, null);
            }
            TextView name = (TextView) convertView
                    .findViewById(R.id.foren_detail_name);
            TextView beschreibung = (TextView) convertView
                    .findViewById(R.id.foren_detail_beschreibung);
            TextView anzahl = (TextView) convertView
                    .findViewById(R.id.foren_detail_anzahl);
            ImageView image = (ImageView) convertView
                    .findViewById(R.id.foren_detail_image);
            name.setText(board.getName());
            beschreibung.setText(board.getDescription());
            StringBuilder builder = new StringBuilder();
            builder.append(board.getThreadCount());
            builder.append(" Themen, ");
            builder.append(board.getPostCount());
            builder.append(" Beiträge");
            anzahl.setText(builder.toString());
            if (board.getUnreadCount() == 0)
                image.setImageDrawable(convertView.getResources().getDrawable(
                        R.drawable.forum_readall));
            else
                image.setImageDrawable(convertView.getResources().getDrawable(
                        R.drawable.forum_new));
            return convertView;
        } else {
            // statistik
            if (convertView == null
                    || convertView.getId() != R.id.foren_statistik) {
                convertView = inflater.inflate(R.layout.foren_statistik, null);
            }
            TextView count = (TextView) convertView
                    .findViewById(R.id.statistik_count);
            TextView member = (TextView) convertView
                    .findViewById(R.id.statistik_member);
            TextView online = (TextView) convertView
                    .findViewById(R.id.statistik_online);
            TextView users = (TextView) convertView
                    .findViewById(R.id.statistik_users);
            StringBuilder builder = new StringBuilder();
            builder.append("Wir haben ingesamt <b>");
            builder.append(statistics.getUserCount());
            builder.append("</b> Mitglieder, die in <b>");
            builder.append(statistics.getThreadCount());
            builder.append("</b> Themen <b>");
            builder.append(statistics.getPostCount());
            builder.append("</b> Beiträge geschrieben haben.");
            count.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            builder.append("Unser neuestes Mitglied ist <b>");
            builder.append(statistics.getLastUserName());
            builder.append("</b>.");
            member.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            builder.append("Zurzeit sind <b>");
            builder.append(statistics.getOnlineCount());
            builder.append(" User</b> online:");
            online.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            ArrayList<Statistics.StatisticsUser> statusers = statistics
                    .getUsersOnline();
            int length = statusers.size();
            Statistics.StatisticsUser u;
            for (int i = 0; i < length; i++) {
                u = statusers.get(i);
                builder.append(u.getStyledName());
                if (i + 1 < length)
                    builder.append(", ");
            }
            users.setText(Html.fromHtml(builder.toString()));
            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return groupPosition < getGroupCount() - 1;
    }

    public class BoardCategory implements Comparable<BoardCategory> {

        public String name;
        public int id;

        public BoardCategory(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public int compareTo(BoardCategory another) {
            return Double.compare(this.id, another.id);
        }
    }
}
