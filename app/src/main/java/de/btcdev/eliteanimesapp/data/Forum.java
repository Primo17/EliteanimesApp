package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Forum implements Parcelable, Comparable<Forum> {

	private int oberforumId;
	private String oberforumName;
	private int id;
	private String name;
	private String beschreibung;
	private int anzahlThreads;
	private int anzahlPosts;
	private int anzahlUnread;
	private ArrayList<Subforum> subforen;

	public Forum() {

	}

	public int getOberforumId() {
		return oberforumId;
	}

	public void setOberforumId(int oberforumId) {
		this.oberforumId = oberforumId;
	}

	public String getOberforumName() {
		return oberforumName;
	}

	public void setOberforumName(String oberforumName) {
		this.oberforumName = oberforumName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
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

	public int getAnzahlUnread() {
		return anzahlUnread;
	}

	public void setAnzahlUnread(int anzahlUnread) {
		this.anzahlUnread = anzahlUnread;
	}

	public ArrayList<Subforum> getSubforen() {
		return subforen;
	}

	public void setSubforen(ArrayList<Subforum> subforen) {
		this.subforen = subforen;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (subforen == null)
			subforen = new ArrayList<Subforum>();
		dest.writeTypedList(subforen);
		Bundle bundle = new Bundle();
		bundle.putInt("oberforumId", oberforumId);
		bundle.putString("oberforumName", oberforumName);
		bundle.putInt("id", id);
		bundle.putString("name", name);
		bundle.putString("beschreibung", beschreibung);
		bundle.putInt("anzahlThreads", anzahlThreads);
		bundle.putInt("anzahlPosts", anzahlPosts);
		bundle.putInt("anzahlUnread", anzahlUnread);
		dest.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Forum> CREATOR = new Parcelable.Creator<Forum>() {
		public Forum createFromParcel(Parcel in) {
			return new Forum(in);
		}

		public Forum[] newArray(int size) {
			return new Forum[size];
		}
	};

	private Forum(Parcel in) {
		subforen = new ArrayList<Subforum>();
		in.readTypedList(subforen, Subforum.CREATOR);
		Bundle bundle = in.readBundle();
		oberforumId = bundle.getInt("oberforumId");
		oberforumName = bundle.getString("oberforumName");
		id = bundle.getInt("id");
		name = bundle.getString("name");
		beschreibung = bundle.getString("beschreibung");
		anzahlThreads = bundle.getInt("anzahlThreads");
		anzahlPosts = bundle.getInt("anzahlPosts");
		anzahlUnread = bundle.getInt("anzahlUnread");
	}

	@Override
	public int compareTo(Forum another) {
		return Double.compare(id, another.getId());
	}

	public String toString() {
		return id + name;
	}

	public boolean equals(Object o) {
		if (o instanceof Forum) {
			Forum f = (Forum) o;
			return id == f.getId();
		} else
			return false;
	}
}
