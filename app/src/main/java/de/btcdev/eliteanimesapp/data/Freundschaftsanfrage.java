package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Freundschaftsanfrage implements Parcelable {

	private String name;
	private boolean status;
	private String alter;
	private String geschlecht;
	private int id;

	public Freundschaftsanfrage(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getAlter() {
		return alter;
	}

	public void setAlter(String alter) {
		this.alter = alter;
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
		return name + ", " + status + ", " + alter + ", " + id;
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
		bundle.putString("geschlecht", geschlecht);
		bundle.putInt("id", id);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Freundschaftsanfrage> CREATOR = new Parcelable.Creator<Freundschaftsanfrage>() {
		public Freundschaftsanfrage createFromParcel(Parcel in) {
			return new Freundschaftsanfrage(in);
		}

		public Freundschaftsanfrage[] newArray(int size) {
			return new Freundschaftsanfrage[size];
		}
	};

	private Freundschaftsanfrage(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		status = bundle.getBoolean("status");
		alter = bundle.getString("alter");
		id = bundle.getInt("id");
		geschlecht = bundle.getString("geschlecht");
	}

}
