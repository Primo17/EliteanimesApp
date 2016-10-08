package de.btcdev.eliteanimesapp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.R;

public class BoardPost implements Parcelable {

	private int id;
	private String date;
	private int editedCount;
	private String editedTime;
	private String userName;
	private int userId;
	private String userDate;
	private int userLevel;
	private boolean online;
	private Bitmap avatar;
	private String avatarURL;
	private String sex;
	private String text;
	private String signature;

	@Inject
	ConfigurationService configurationService;

	public BoardPost() {

	}

	public BoardPost(int id) {
		setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getEditedCount() {
		return editedCount;
	}

	public void setEditedCount(int editedCount) {
		this.editedCount = editedCount;
	}

	public String getEditedTime() {
		return editedTime;
	}

	public void setEditedTime(String editedTime) {
		this.editedTime = editedTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserDate() {
		return userDate;
	}

	public void setUserDate(String userDate) {
		this.userDate = userDate;
	}

	public int getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public boolean equals(Object o) {
		if (o instanceof BoardPost) {
			return ((BoardPost) o).getId() == this.getId()
					&& ((BoardPost) o).getDate().equals(this.getDate());
		} else
			return false;
	}

	public String toString() {
		return id + " " + userName + " " + text;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putString("date", date);
		bundle.putInt("editedCount", editedCount);
		bundle.putString("editedTime", editedTime);
		bundle.putString("userName", userName);
		bundle.putInt("userId", userId);
		bundle.putString("userDate", userDate);
		bundle.putInt("userLevel", userLevel);
		bundle.putBoolean("online", online);
		bundle.putString("avatarURL", avatarURL);
		bundle.putString("sex", sex);
		bundle.putString("text", text);
		bundle.putString("signature", signature);
		arg0.writeBundle(bundle);
		avatar.writeToParcel(arg0, 0);
	}

	public static final Parcelable.Creator<BoardPost> CREATOR = new Parcelable.Creator<BoardPost>() {
		public BoardPost createFromParcel(Parcel in) {
			return new BoardPost(in);
		}

		public BoardPost[] newArray(int size) {
			return new BoardPost[size];
		}
	};

	private BoardPost(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		date = bundle.getString("date");
		editedCount = bundle.getInt("editedCount");
		editedTime = bundle.getString("editedTime");
		userName = bundle.getString("userName");
		userId = bundle.getInt("userId");
		userDate = bundle.getString("userDate");
		userLevel = bundle.getInt("userLevel");
		online = bundle.getBoolean("online");
		avatarURL = bundle.getString("avatarURL");
		sex = bundle.getString("sex");
		text = bundle.getString("text");
		signature = bundle.getString("signature");
		try {
			avatar = Bitmap.CREATOR.createFromParcel(in);
		} catch (Exception e) {

		}
		if (avatar == null) {
			avatar = BitmapFactory.decodeResource(configurationService.getContext()
					.getResources(), R.drawable.noava);
		}
	}
}
