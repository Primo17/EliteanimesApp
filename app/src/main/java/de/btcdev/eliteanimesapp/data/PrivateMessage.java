package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PrivateMessage implements Parcelable {

	private int id;
	private String userName;
	private String subject;
	private int userId;
	private String date;
	private String message;
	private boolean read;

	public PrivateMessage(int id) {
		setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean equals(Object o) {
		if (o instanceof PrivateMessage) {
			PrivateMessage k = (PrivateMessage) o;
			return k.getId() == this.id;
		}
		return false;
	}

	public String toString() {
		return id + ", " + userName + ", " + subject;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putString("userName", userName);
		bundle.putString("subject", subject);
		bundle.putString("date", date);
		bundle.putString("message", message);
		bundle.putInt("userId", userId);
		bundle.putBoolean("read", read);
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
		userName = bundle.getString("userName");
		subject = bundle.getString("subject");
		date = bundle.getString("date");
		message = bundle.getString("message");
		userId = bundle.getInt("userId");
		read = bundle.getBoolean("read");
	}

}
