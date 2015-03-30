package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

public class Statistik {

    private int anzahlUser;
    private int anzahlThreads;
    private int anzahlPosts;
    private int anzahlOnline;
    private int lastUserId;
    private String lastUserName;
    private ArrayList<StatistikUser> usersOnline;

    public Statistik() {

    }

    public int getAnzahlUser() {
        return anzahlUser;
    }

    public void setAnzahlUser(int anzahlUser) {
        this.anzahlUser = anzahlUser;
    }

    public int getAnzahlThreads() {
        return anzahlThreads;
    }

    public void setAnzahlThreads(int anzahlThreads) {
        this.anzahlThreads = anzahlThreads;
    }

    public int getAnzahlPosts() {
        return anzahlPosts;
    }

    public void setAnzahlPosts(int anzahlPosts) {
        this.anzahlPosts = anzahlPosts;
    }

    public int getAnzahlOnline() {
        return anzahlOnline;
    }

    public void setAnzahlOnline(int anzahlOnline) {
        this.anzahlOnline = anzahlOnline;
    }

    public int getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(int lastUserId) {
        this.lastUserId = lastUserId;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public ArrayList<StatistikUser> getUsersOnline() {
        return usersOnline;
    }

    public void setUsersOnline(ArrayList<StatistikUser> usersOnline) {
        this.usersOnline = usersOnline;
    }

    public static class StatistikUser {
        public int id;
        public String name;
        public int level;
        public boolean donator;

        public StatistikUser() {

        }

        public boolean isDonator() {
            return donator;
        }

        public void setDonator(int don) {
            donator = (don == 1);
        }

        public String getStyledName() {
            StringBuilder b = new StringBuilder();
            if (level == 2 || level == 3) {
                b.append("<font color=\"red\">");
                b.append(name);
                b.append("</font>");
            } else if (donator) {
                b.append("<font color=\"blue\">");
                b.append(name);
                b.append("</font>");
            } else {
                b.append(name);
            }
            return b.toString();
        }

        public String toString() {
            return name;
        }
    }
}
