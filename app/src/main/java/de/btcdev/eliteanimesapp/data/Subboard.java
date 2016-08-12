package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Subboard implements Parcelable, Comparable<Subboard> {

	private String name;
	private int id;

	public Subboard() {

	}

	public Subboard(String name, int id) {
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

	public static final Parcelable.Creator<Subboard> CREATOR = new Parcelable.Creator<Subboard>() {
		public Subboard createFromParcel(Parcel in) {
			return new Subboard(in);
		}

		public Subboard[] newArray(int size) {
			return new Subboard[size];
		}
	};

	private Subboard(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		id = bundle.getInt("id");
	}

	public String toString() {
		return id + name;
	}

	public boolean equals(Object o) {
		if (o instanceof Subboard) {
			Subboard f = (Subboard) o;
			return id == f.getId();
		} else
			return false;
	}

	@Override
	public int compareTo(Subboard another) {
		return Double.compare(id, another.getId());
	}
}
