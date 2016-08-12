package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable {

	private int id;
	private String name;
	private boolean status;
	private String sex;
	private String age;
	private String date;

	public Friend(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public boolean equals(Object o) {
		if (o instanceof Friend) {
			Friend f = (Friend) o;
			return name.equals(f.getName());
		} else
			return false;
	}

	public String toString() {
		return id + ", " + name + ", " + status + ", " + age + ", " + date
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
		bundle.putString("age", age);
		bundle.putInt("id", id);
		bundle.putString("sex", sex);
		bundle.putString("date", date);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
		public Friend createFromParcel(Parcel in) {
			return new Friend(in);
		}

		public Friend[] newArray(int size) {
			return new Friend[size];
		}
	};

	private Friend(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		status = bundle.getBoolean("status");
		id = bundle.getInt("id");
		age = bundle.getString("age");
		date = bundle.getString("date");
		sex = bundle.getString("sex");
	}
}
