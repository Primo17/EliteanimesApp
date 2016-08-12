package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PrivateMessage implements Parcelable {

	private int id;
	private String benutzername;
	private String betreff;
	private int userid;
	private String date;
	private String text;
	private boolean gelesen;

	public PrivateMessage(int id) {
		setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(String betreff) {
		this.betreff = betreff;
	}

	public String getBenutzername() {
		return benutzername;
	}

	public void setBenutzername(String benutzername) {
		this.benutzername = benutzername;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean getGelesen() {
		return gelesen;
	}

	public void setGelesen(boolean gelesen) {
		this.gelesen = gelesen;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public boolean equals(Object o) {
		if (o instanceof PrivateMessage) {
			PrivateMessage k = (PrivateMessage) o;
			return k.getId() == this.id;
		}
		return false;
	}

	public String toString() {
		return id + ", " + benutzername + ", " + betreff;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putString("benutzername", benutzername);
		bundle.putString("betreff", betreff);
		bundle.putString("date", date);
		bundle.putString("text", text);
		bundle.putInt("userid", userid);
		bundle.putBoolean("gelesen", gelesen);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<PrivateMessage> CREATOR = new Parcelable.Creator<PrivateMessage>() {
		public PrivateMessage createFromParcel(Parcel in) {
			return new PrivateMessage(in);
		}

		public PrivateMessage[] newArray(int size) {
			return new PrivateMessage[size];
		}
	};

	private PrivateMessage(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		benutzername = bundle.getString("benutzername");
		betreff = bundle.getString("betreff");
		date = bundle.getString("date");
		text = bundle.getString("text");
		userid = bundle.getInt("userid");
		gelesen = bundle.getBoolean("gelesen");
	}

}
