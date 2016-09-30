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

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.cache.CommentLruCache;

public class Comment implements Parcelable {

	private int id;
	private String userName;
	private int userId;
	private String date;
	private transient Bitmap avatar;
	private String text;
	private String avatarURL;

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

	public void setAvatar(String avatarURL) {
		this.avatarURL = avatarURL;
		final int reqHeight = 60;
		final int reqWidth = 60;

		if (avatarURL.contains("noava.png")) {
			try {
				// First decode with inJustDecodeBounds=true to check dimensions
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(configurationService.getContext()
						.getResources(), R.drawable.noava, options);
				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth,
						reqHeight);
				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				avatar = BitmapFactory.decodeResource(configurationService.getContext()
						.getResources(), R.drawable.noava, options);
			} catch (Exception exc) {
				System.out.println("Exception!" + exc.getClass().toString());
			}
		} else {
			try {
				CommentLruCache cache = CommentLruCache.instance();
				avatar = cache.getBitmapFromMemCache(avatarURL);
				if (avatar == null) {
					// First decode with inJustDecodeBounds=true to check
					// dimensions
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					HttpGet httpRequest = new HttpGet(avatarURL);
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
					avatar = BitmapFactory.decodeStream(is, null, options);
					cache.addBitmapToMemoryCache(avatarURL, avatar);
					is.close();
				}
			} catch (Exception exc) {
				System.out.println("Exception!" + exc.getClass().toString());
				setAvatar("noava.png");
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

	public String getAvatarURL() {
		return avatarURL;
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

		}
		if (avatar == null) {
			avatar = BitmapFactory.decodeResource(configurationService.getContext()
					.getResources(), R.drawable.noava);
		}
	}
}
