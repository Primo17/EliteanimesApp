package de.btcdev.eliteanimesapp.data;

import android.graphics.Bitmap;

/**
 * UserProfile data from eliteanimes profile page
 */
public class Profile {

	private int userId;
	private String userName;
	private String group;
	private boolean online;
	private String sex;
	private String age;
	private String single;
	private String residence;
	private String registeredSince;
	private int friend;
	private transient Bitmap avatar;
	private String avatarURL;

	public Profile(String userName) {
		this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSingle() {
        return single;
    }

    public void setSingle(String single) {
        this.single = single;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getRegisteredSince() {
        return registeredSince;
    }

    public void setRegisteredSince(String registeredSince) {
        this.registeredSince = registeredSince;
    }

    public int getFriend() {
        return friend;
    }

    public void setFriend(int friend) {
        this.friend = friend;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

	public String toString() {
		return userName;
	}

	/**
	 * Gibt zurück, ob das übergebene Objekt mit dem Profile übereinstimmt. Ein
	 * Profile stimmt dann überein, wenn der Benutzername gleich ist.
	 * 
	 * @param o
	 *            zu vergleichendes Objekt
	 * @return Wahrheitswert für die Gleichheit
	 */
	public boolean equals(Object o) {
		if (o instanceof Profile) {
			Profile p = (Profile) o;
			return p.getUserName().equals(userName);
		}
		return false;
	}

	/**
	 * Gibt zurück, ob das Profile vollständig ist, also ob alle Daten vorhanden
	 * sind.
	 * 
	 * @return Wahrheitswert über Vollständigkeit
	 */
	public boolean isComplete() {
		return (userName != null && group != null && sex != null
				&& age != null && single != null && residence != null
				&& registeredSince != null && avatar != null);

	}
}
