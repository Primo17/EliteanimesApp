package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Klasse zum einfachen Speichern von Nutzerdaten, wenn nicht viele
 * Informationen verlangt sind bzw benätigt werden.
 * 
 */
public class User implements Parcelable {
	String id;
	String name;

	public User(String name) {
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

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	private User(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		id = bundle.getString("id");
	}
}
