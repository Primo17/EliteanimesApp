package de.btcdev.eliteanimesapp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.services.ConfigurationService;

public class Comment implements Parcelable {

	private int id;
	private String userName;
	private int userId;
	private String date;
	private transient Bitmap avatar;
	private String text;
	private String avatarURL;

	//TODO remove this
	@Inject
	ConfigurationService configurationService;

	public Comment(int id) {
		setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean equals(Object o) {
		if (o instanceof Comment) {
			Comment k = (Comment) o;
			return k.getId() == this.id;
		}
		return false;
	}

	public String toString() {
		return id + ", " + userName + ", " + text;
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
		bundle.putInt("userId", userId);
		bundle.putString("date", date);
		bundle.putString("text", text);
		bundle.putString("avatarURL", avatarURL);
		arg0.writeBundle(bundle);
		avatar.writeToParcel(arg0, 0);
	}

	public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
		public Comment createFromParcel(Parcel in) {
			return new Comment(in);
		}

		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};

	private Comment(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		userName = bundle.getString("userName");
		userId = bundle.getInt("userId");
		date = bundle.getString("date");
		text = bundle.getString("text");
		avatarURL = bundle.getString("avatarURL");
		try {
			avatar = Bitmap.CREATOR.createFromParcel(in);
		} catch (Exception e) {
			System.out.println("Failure Parcel Comment caused by: "+ e);
		}
		if (avatar == null) {
			avatar = BitmapFactory.decodeResource(configurationService.getContext()
					.getResources(), R.drawable.noava);
		}
	}
}
