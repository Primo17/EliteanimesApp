package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Freund implements Parcelable {

	private int id;
	private String name;
	private boolean status;
	private String geschlecht;
	private String alter;
	private String datum;

	public Freund(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlter() {
		return alter;
	}

	public void setAlter(String alter) {
		this.alter = alter;
	}

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}

	public boolean equals(Object o) {
		if (o instanceof Freund) {
			Freund f = (Freund) o;
			return name.equals(f.getName());
		} else
			return false;
	}

	public String toString() {
		return id + ", " + name + ", " + status + ", " + alter + ", " + datum
				+ ", ";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		bundle.putBoolean("status", status);
		bundle.putString("alter", alter);
		bundle.putInt("id", id);
		bundle.putString("geschlecht", geschlecht);
		bundle.putString("datum", datum);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Freund> CREATOR = new Parcelable.Creator<Freund>() {
		public Freund createFromParcel(Parcel in) {
			return new Freund(in);
		}

		public Freund[] newArray(int size) {
			return new Freund[size];
		}
	};

	private Freund(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		status = bundle.getBoolean("status");
		id = bundle.getInt("id");
		alter = bundle.getString("alter");
		datum = bundle.getString("datum");
		geschlecht = bundle.getString("geschlecht");
	}
}
