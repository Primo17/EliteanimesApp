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
import de.btcdev.eliteanimesapp.data.Forum;
import de.btcdev.eliteanimesapp.data.Statistik;

public class ForenAdapter extends BaseExpandableListAdapter {

    public static final int OBERFORUM_MAIN = 4;
    public static final int OBERFORUM_ANIME = 2;
    public static final int OBERFORUM_MANGA = 3;
    public static final int OBERFORUM_GFX = 1;
    public static final int OBERFORUM_PLAUDER = 5;
    public static final int STATISTIK = 6;
    private ArrayList<Oberforum> oberforen;
    private LayoutInflater inflater;
    private TreeMap<Integer, ArrayList<Forum>> forenMap;
    private Statistik stat;

    public ForenAdapter(Activity activity,
                        TreeMap<Integer, ArrayList<Forum>> forenMap, Statistik stat) {
        this.forenMap = forenMap;
        this.stat = stat;
        oberforen = new ArrayList<>();
        oberforen.add(new Oberforum("Main", OBERFORUM_MAIN));
        oberforen.add(new Oberforum("Anime", OBERFORUM_ANIME));
        oberforen.add(new Oberforum("Manga", OBERFORUM_MANGA));
        oberforen.add(new Oberforum("GFX Bereich", OBERFORUM_GFX));
        oberforen.add(new Oberforum("Plauderecke", OBERFORUM_PLAUDER));
        oberforen.add(new Oberforum("Statistik", STATISTIK));
        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return oberforen.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition < getGroupCount() - 1) {
            ArrayList<Forum> foren = forenMap
                    .get(oberforen.get(groupPosition).id);
            if (foren == null)
                return 0;
            else
                return foren.size();
        } else
            return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return oberforen.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition < getGroupCount() - 1)
            return forenMap.get(oberforen.get(groupPosition).id).get(
                    childPosition);
        else
            return stat;
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
        Oberforum of = oberforen.get(groupPosition);
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
            Forum forum = (Forum) getChild(groupPosition, childPosition);
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
            name.setText(forum.getName());
            beschreibung.setText(forum.getBeschreibung());
            StringBuilder builder = new StringBuilder();
            builder.append(forum.getAnzahlThreads());
            builder.append(" Themen, ");
            builder.append(forum.getAnzahlPosts());
            builder.append(" Beiträge");
            anzahl.setText(builder.toString());
            if (forum.getAnzahlUnread() == 0)
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
            builder.append(stat.getAnzahlUser());
            builder.append("</b> Mitglieder, die in <b>");
            builder.append(stat.getAnzahlThreads());
            builder.append("</b> Themen <b>");
            builder.append(stat.getAnzahlPosts());
            builder.append("</b> Beiträge geschrieben haben.");
            count.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            builder.append("Unser neuestes Mitglied ist <b>");
            builder.append(stat.getLastUserName());
            builder.append("</b>.");
            member.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            builder.append("Zurzeit sind <b>");
            builder.append(stat.getAnzahlOnline());
            builder.append(" Benutzer</b> online:");
            online.setText(Html.fromHtml(builder.toString()));
            builder = new StringBuilder();
            ArrayList<Statistik.StatistikUser> statusers = stat
                    .getUsersOnline();
            int length = statusers.size();
            Statistik.StatistikUser u;
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

    public class Oberforum implements Comparable<Oberforum> {

        public String name;
        public int id;

        public Oberforum(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public int compareTo(Oberforum another) {
            return Double.compare(this.id, another.id);
        }
    }
}
