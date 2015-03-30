package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Klasse zum einfachen Speichern von Nutzerdaten, wenn nicht viele
 * Informationen verlangt sind bzw benätigt werden.
 * 
 */
public class Benutzer implements Parcelable {
	String id;
	String name;

	public Benutzer(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return getName();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putString("name", name);
		bundle.putString("id", id);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Benutzer> CREATOR = new Parcelable.Creator<Benutzer>() {
		public Benutzer createFromParcel(Parcel in) {
			return new Benutzer(in);
		}

		public Benutzer[] newArray(int size) {
			return new Benutzer[size];
		}
	};

	private Benutzer(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		id = bundle.getString("id");
	}
}
