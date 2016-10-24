package de.btcdev.eliteanimesapp.data.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class FriendRequest implements Parcelable {

	private String name;
	private boolean status;
	private String age;
	private String sex;
	private int id;

	public FriendRequest(String name) {
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

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
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
		return name + ", " + status + ", " + age + ", " + id;
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
		bundle.putString("sex", sex);
		bundle.putInt("id", id);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<FriendRequest> CREATOR = new Parcelable.Creator<FriendRequest>() {
		public FriendRequest createFromParcel(Parcel in) {
			return new FriendRequest(in);
		}

		public FriendRequest[] newArray(int size) {
			return new FriendRequest[size];
		}
	};

	private FriendRequest(Parcel in) {
		Bundle bundle = in.readBundle();
		name = bundle.getString("name");
		status = bundle.getBoolean("status");
		age = bundle.getString("age");
		id = bundle.getInt("id");
		sex = bundle.getString("sex");
	}

}
