package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

public class Statistics {

    private int userCount;
    private int threadCount;
    private int postCount;
    private int onlineCount;
    private int lastUserId;
    private String lastUserName;
    private ArrayList<StatisticsUser> usersOnline;

    public Statistics() {

    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
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

    public ArrayList<StatisticsUser> getUsersOnline() {
        return usersOnline;
    }

    public void setUsersOnline(ArrayList<StatisticsUser> usersOnline) {
        this.usersOnline = usersOnline;
    }

    public static class StatisticsUser {
        public int id;
        public String name;
        public int level;
        public boolean donator;

        public StatisticsUser() {

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
