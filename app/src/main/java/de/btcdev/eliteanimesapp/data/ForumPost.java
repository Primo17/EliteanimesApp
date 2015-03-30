package de.btcdev.eliteanimesapp.data;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.cache.CommentLruCache;

public class ForumPost implements Parcelable {

	private int id;
	private String date;
	private int edited;
	private String editedTime;
	private String userName;
	private int userId;
	private String userDate;
	private int userLevel;
	private boolean online;
	private Bitmap bild;
	private String bildlink;
	private String geschlecht;
	private String text;
	private String signatur;

	public ForumPost() {

	}

	public ForumPost(int id) {
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

	public int getEdited() {
		return edited;
	}

	public void setEdited(int edited) {
		this.edited = edited;
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

	public Bitmap getBild() {
		return bild;
	}

	public void setBild(Bitmap bild) {
		this.bild = bild;
	}

	public void setBild(String bildurl) {
		this.bildlink = bildurl;
		final int reqHeight = 60;
		final int reqWidth = 60;

		if (bildurl.contains("noava.png")) {
			try {
				// First decode with inJustDecodeBounds=true to check dimensions
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(Konfiguration.getContext()
						.getResources(), R.drawable.noava, options);
				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth,
						reqHeight);
				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				bild = BitmapFactory.decodeResource(Konfiguration.getContext()
						.getResources(), R.drawable.noava, options);
			} catch (Exception exc) {
				System.out.println("Exception!" + exc.getClass().toString());
			}
		} else {
			try {
				CommentLruCache cache = CommentLruCache.instance();
				bild = cache.getBitmapFromMemCache(bildurl);
				if (bild == null) {
					// First decode with inJustDecodeBounds=true to check
					// dimensions
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					HttpGet httpRequest = new HttpGet(bildurl);
					HttpParams httpParameters = new BasicHttpParams();
					int timeoutConnection = 3000;
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							timeoutConnection);
					int timeoutSocket = 5000;
					HttpConnectionParams.setSoTimeout(httpParameters,
							timeoutSocket);
					HttpClient httpclient = new DefaultHttpClient(
							httpParameters);
					HttpResponse response = (HttpResponse) httpclient
							.execute(httpRequest);
					HttpEntity entity = response.getEntity();
					BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
							entity);
					InputStream is = bufferedHttpEntity.getContent();
					BitmapFactory.decodeStream(is, null, options);
					is.close();
					// Calculate inSampleSize
					options.inSampleSize = calculateInSampleSize(options,
							reqWidth, reqHeight);
					// Decode bitmap with inSampleSize set
					options.inJustDecodeBounds = false;
					is = bufferedHttpEntity.getContent();
					bild = BitmapFactory.decodeStream(is, null, options);
					cache.addBitmapToMemoryCache(bildurl, bild);
					is.close();
				}
			} catch (Exception exc) {
				System.out.println("Exception!" + exc.getClass().toString());
				setBild("noava.png");
			}
		}
	}

	public int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public String getBildlink() {
		return bildlink;
	}

	public void setBildlink(String bildlink) {
		this.bildlink = bildlink;
	}

	public String getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSignatur() {
		return signatur;
	}

	public void setSignatur(String signatur) {
		this.signatur = signatur;
	}

	public boolean equals(Object o) {
		if (o instanceof ForumPost) {
			return ((ForumPost) o).getId() == this.getId()
					&& ((ForumPost) o).getDate().equals(this.getDate());
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
		bundle.putInt("edited", edited);
		bundle.putString("editedTime", editedTime);
		bundle.putString("userName", userName);
		bundle.putInt("userId", userId);
		bundle.putString("userDate", userDate);
		bundle.putInt("userLevel", userLevel);
		bundle.putBoolean("online", online);
		bundle.putString("bildlink", bildlink);
		bundle.putString("geschlecht", geschlecht);
		bundle.putString("text", text);
		bundle.putString("signatur", signatur);
		arg0.writeBundle(bundle);
		bild.writeToParcel(arg0, 0);
	}

	public static final Parcelable.Creator<ForumPost> CREATOR = new Parcelable.Creator<ForumPost>() {
		public ForumPost createFromParcel(Parcel in) {
			return new ForumPost(in);
		}

		public ForumPost[] newArray(int size) {
			return new ForumPost[size];
		}
	};

	private ForumPost(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		date = bundle.getString("date");
		edited = bundle.getInt("edited");
		editedTime = bundle.getString("editedTime");
		userName = bundle.getString("userName");
		userId = bundle.getInt("userId");
		userDate = bundle.getString("userDate");
		userLevel = bundle.getInt("userLevel");
		online = bundle.getBoolean("online");
		bildlink = bundle.getString("bildlink");
		geschlecht = bundle.getString("geschlecht");
		text = bundle.getString("text");
		signatur = bundle.getString("signatur");
		try {
			bild = Bitmap.CREATOR.createFromParcel(in);
		} catch (Exception e) {

		}
		if (bild == null) {
			bild = BitmapFactory.decodeResource(Konfiguration.getContext()
					.getResources(), R.drawable.noava);
		}
	}
}
