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
import de.btcdev.eliteanimesapp.R;

/**
 * Datenstruktur zur effizienten Verwaltung eines Profils
 * 
 */
public class Profile {

	private int userId;
	private String userName;
	private String group;
	private boolean online;
	private String sex;
	private String age;
	private String single;
	private String residence;
	private String registeredSince;
	private int friend;
	private transient Bitmap avatar;
	private String avatarURL;

	/**
	 * Erzeugt ein neues Profile.
	 * 
	 * @param userName
	 *            Benutzername, mit dem das Profile erzeugt werden soll
	 */
	public Profile(String userName) {
		setUserName(userName);
	}

	/**
	 * Gibt den Benutzernamen des Profils zurück.
	 * 
	 * @return Benutzername als String
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Gibt die UserID des Profils zurück.
	 * 
	 * @return UserID
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Setzt eine neue UserID.
	 * 
	 * @param userId
	 *            UserID, die gesetzt werden soll
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Gibt das Profilbild des Profils zurück.
	 * 
	 * @return Profilbild als Bitmap
	 */
	public Bitmap getAvatar() {
		return avatar;
	}

	/**
	 * Setzt ein neues Profilbild.
	 * 
	 * @return avatar
	 *            Bitmap, die als Profilbild gesetzt werden soll
	 */
	public void setAvatar(String avatarURL) {
		this.avatarURL = avatarURL;
		final int reqHeight = 120;
		final int reqWidth = 120;
		try {
			if (avatarURL.contains("noava.png")) {
				// First decode with inJustDecodeBounds=true to check dimensions
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(Configuration.getContext()
						.getResources(), R.drawable.noava, options);
				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth,
						reqHeight);
				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				avatar = BitmapFactory
						.decodeResource(Configuration.getContext()
								.getResources(), R.drawable.noava, options);
			} else {
				// First decode with inJustDecodeBounds=true to check dimensions
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				HttpGet httpRequest = new HttpGet(avatarURL);
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 3000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 5000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);
				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpResponse response = (HttpResponse) httpclient
						.execute(httpRequest);
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
						entity);
				InputStream is = bufferedHttpEntity.getContent();
				BitmapFactory.decodeStream(is, null, options);
				is.close();
				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth,
						reqHeight);
				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				is = bufferedHttpEntity.getContent();
				avatar = BitmapFactory.decodeStream(is, null, options);
				is.close();
			}
		} catch (Exception e) {
			System.out.println(e.getClass().toString());
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

	/**
	 * Setzt einen neuen Benutzernamen.
	 * 
	 * @param userName
	 *            Benutzername, der gesetzt werden soll
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gibt den Status des Benutzers zurück.
	 * 
	 * @return Status des Benutzers als boolean
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * Setzt einen neuen Status.
	 * 
	 * @param online
	 *            Status, der gesetzt werden soll
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * Gibt die Gruppe des Benutzers zurück.
	 * 
	 * @return Gruppe des Benutzers als String
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Setzt eine neue Gruppe.
	 * 
	 * @param group
	 *            Gruppe, die gesetzt werden soll
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gibt das Geschlecht des Benutzers zurück.
	 * 
	 * @return Geschlecht als String
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * Setzt ein neues Geschlecht.
	 * 
	 * @param sex
	 *            Geschlecht, das gesetzt werden soll
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}

	/**
	 * Gibt das Alter des Benutzers zurück.
	 * 
	 * @return Alter als String
	 */
	public String getAge() {
		return age;
	}

	/**
	 * Setzt ein neues Alter.
	 * 
	 * @param age
	 *            Alter, das gesetzt werden soll
	 */
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * Gibt den Beziehungsstatus des Benutzers zurück.
	 * 
	 * @return Beziehungsstatus als String
	 */
	public String getSingle() {
		return single;
	}

	/**
	 * Setzt einen neuen Beziehungsstatus.
	 * 
	 * @param single
	 *            Beziehungsstatus, der gesetzt werden soll
	 */
	public void setSingle(String single) {
		this.single = single;
	}

	/**
	 * Gibt den Wohnort des Benutzers zurück.
	 * 
	 * @return Wohnort des Benutzers
	 */
	public String getResidence() {
		return residence;
	}

	/**
	 * Setzt einen neuen Wohnort.
	 * 
	 * @param residence
	 *            Wohnort, der gesetzt werden soll
	 */
	public void setResidence(String residence) {
		this.residence = residence;
	}

	/**
	 * Gibt das Datum, seitdem der User registriert ist, zurück.
	 * 
	 * @return Registrierungsdatum als String
	 */
	public String getRegisteredSince() {
		return registeredSince;
	}

	/**
	 * Setzt ein neues Registrierungsdatum.
	 * 
	 * @param registeredSince
	 *            Registrierungsdatum, das gesetzt werden soll
	 */
	public void setRegisteredSince(String registeredSince) {
		this.registeredSince = registeredSince;
	}

	/**
	 * Gibt den Freundestatus zurück.
	 * 
	 * @return Integer-Repräsentation des Freundestatus
	 */
	public int getFriend() {
		return friend;
	}

	/**
	 * Setzt den Freundestatus.
	 * 
	 * @param friend
	 *            Integer-Repräsentation des Freundestatus
	 */
	public void setFriend(int friend) {
		this.friend = friend;
	}

	/**
	 * String-Repräsentation des Profils
	 * 
	 * @return String-Repräsentation des Profils
	 */
	public String toString() {
		return userName;
	}

	/**
	 * Gibt zurück, ob das übergebene Objekt mit dem Profile übereinstimmt. Ein
	 * Profile stimmt dann überein, wenn der Benutzername gleich ist.
	 * 
	 * @param o
	 *            zu vergleichendes Objekt
	 * @return Wahrheitswert für die Gleichheit
	 */
	public boolean equals(Object o) {
		if (o instanceof Profile) {
			Profile p = (Profile) o;
			return p.getUserName().equals(userName);
		}
		return false;
	}

	/**
	 * Gibt zurück, ob das Profile vollständig ist, also ob alle Daten vorhanden
	 * sind.
	 * 
	 * @return Wahrheitswert über Vollständigkeit
	 */
	public boolean isComplete() {
		return (userName != null && group != null && sex != null
				&& age != null && single != null && residence != null
				&& registeredSince != null && avatar != null);

	}
}
