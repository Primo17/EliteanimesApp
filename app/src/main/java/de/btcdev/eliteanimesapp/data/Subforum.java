package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Subforum implements Parcelable, Comparable<Subforum> {

	private String name;
	private int id;

	public Subforum() {

	}

	public Subforum(String name, int id) {
		setName(name);
		setId(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		bundle.putInt("id", id);
		dest.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Subforum> CREATOR = new Parcelable.Creator<Subforum>() {
		public Subforum createFromParcel(Parcel in) {
			return new Subforum(in);
		}

		public Subforum[] newArray(int size) {
			return new Subforum[size];
		}
	};

	private Subforum(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		id = bundle.getInt("id");
	}

	public String toString() {
		return id + name;
	}

	public boolean equals(Object o) {
		if (o instanceof Subforum) {
			Subforum f = (Subforum) o;
			return id == f.getId();
		} else
			return false;
	}

	@Override
	public int compareTo(Subforum another) {
		return Double.compare(id, another.getId());
	}
}
