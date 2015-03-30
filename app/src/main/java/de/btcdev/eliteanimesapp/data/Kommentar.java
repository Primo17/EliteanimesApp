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

public class Kommentar implements Parcelable {

	private int id;
	private String benutzername;
	private int userId;
	private String date;
	private transient Bitmap bild;
	private String text;
	private String bildurl;

	public Kommentar(int id) {
		setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Bitmap getBild() {
		return bild;
	}

	public void setBild(String bildurl) {
		this.bildurl = bildurl;
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

	public String getBildUrl() {
		return bildurl;
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
		if (o instanceof Kommentar) {
			Kommentar k = (Kommentar) o;
			return k.getId() == this.id;
		}
		return false;
	}

	public String toString() {
		return id + ", " + benutzername + ", " + text;
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
		bundle.putInt("userId", userId);
		bundle.putString("date", date);
		bundle.putString("text", text);
		bundle.putString("bildurl", bildurl);
		arg0.writeBundle(bundle);
		bild.writeToParcel(arg0, 0);
	}

	public static final Parcelable.Creator<Kommentar> CREATOR = new Parcelable.Creator<Kommentar>() {
		public Kommentar createFromParcel(Parcel in) {
			return new Kommentar(in);
		}

		public Kommentar[] newArray(int size) {
			return new Kommentar[size];
		}
	};

	private Kommentar(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		benutzername = bundle.getString("benutzername");
		userId = bundle.getInt("userId");
		date = bundle.getString("date");
		text = bundle.getString("text");
		bildurl = bundle.getString("bildurl");
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
