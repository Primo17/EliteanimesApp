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
public class Profil {

	private int userID;
	private String benutzername;
	private String gruppe;
	private boolean online;
	private String geschlecht;
	private String alter;
	private String single;
	private String wohnort;
	private String dabei;
	private int friend;
	private transient Bitmap profilbild;
	private String bildlink;

	/**
	 * Erzeugt ein neues Profil.
	 * 
	 * @param benutzername
	 *            Benutzername, mit dem das Profil erzeugt werden soll
	 */
	public Profil(String benutzername) {
		setBenutzername(benutzername);
	}

	/**
	 * Gibt den Benutzernamen des Profils zurück.
	 * 
	 * @return Benutzername als String
	 */
	public String getBenutzername() {
		return benutzername;
	}

	/**
	 * Gibt die UserID des Profils zurück.
	 * 
	 * @return UserID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * Setzt eine neue UserID.
	 * 
	 * @param userID
	 *            UserID, die gesetzt werden soll
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * Gibt das Profilbild des Profils zurück.
	 * 
	 * @return Profilbild als Bitmap
	 */
	public Bitmap getProfilbild() {
		return profilbild;
	}

	/**
	 * Setzt ein neues Profilbild.
	 * 
	 * @return profilbild
	 *            Bitmap, die als Profilbild gesetzt werden soll
	 */
	public void setProfilbild(String bildlink) {
		this.bildlink = bildlink;
		final int reqHeight = 120;
		final int reqWidth = 120;
		try {
			if (bildlink.contains("noava.png")) {
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
				profilbild = BitmapFactory
						.decodeResource(Konfiguration.getContext()
								.getResources(), R.drawable.noava, options);
			} else {
				// First decode with inJustDecodeBounds=true to check dimensions
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				HttpGet httpRequest = new HttpGet(bildlink);
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
				profilbild = BitmapFactory.decodeStream(is, null, options);
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

	public String getBildURL() {
		return bildlink;
	}

	/**
	 * Setzt einen neuen Benutzernamen.
	 * 
	 * @param benutzername
	 *            Benutzername, der gesetzt werden soll
	 */
	public void setBenutzername(String benutzername) {
		this.benutzername = benutzername;
	}

	/**
	 * Gibt den Status des Benutzers zurück.
	 * 
	 * @return Status des Benutzers als boolean
	 */
	public boolean getOnline() {
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
	public String getGruppe() {
		return gruppe;
	}

	/**
	 * Setzt eine neue Gruppe.
	 * 
	 * @param gruppe
	 *            Gruppe, die gesetzt werden soll
	 */
	public void setGruppe(String gruppe) {
		this.gruppe = gruppe;
	}

	/**
	 * Gibt das Geschlecht des Benutzers zurück.
	 * 
	 * @return Geschlecht als String
	 */
	public String getGeschlecht() {
		return geschlecht;
	}

	/**
	 * Setzt ein neues Geschlecht.
	 * 
	 * @param geschlecht
	 *            Geschlecht, das gesetzt werden soll
	 */
	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}

	/**
	 * Gibt das Alter des Benutzers zurück.
	 * 
	 * @return Alter als String
	 */
	public String getAlter() {
		return alter;
	}

	/**
	 * Setzt ein neues Alter.
	 * 
	 * @param alter
	 *            Alter, das gesetzt werden soll
	 */
	public void setAlter(String alter) {
		this.alter = alter;
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
	public String getWohnort() {
		return wohnort;
	}

	/**
	 * Setzt einen neuen Wohnort.
	 * 
	 * @param wohnort
	 *            Wohnort, der gesetzt werden soll
	 */
	public void setWohnort(String wohnort) {
		this.wohnort = wohnort;
	}

	/**
	 * Gibt das Datum, seitdem der Benutzer registriert ist, zurück.
	 * 
	 * @return Registrierungsdatum als String
	 */
	public String getDabei() {
		return dabei;
	}

	/**
	 * Setzt ein neues Registrierungsdatum.
	 * 
	 * @param dabei
	 *            Registrierungsdatum, das gesetzt werden soll
	 */
	public void setDabei(String dabei) {
		this.dabei = dabei;
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
		return benutzername;
	}

	/**
	 * Gibt zurück, ob das übergebene Objekt mit dem Profil übereinstimmt. Ein
	 * Profil stimmt dann überein, wenn der Benutzername gleich ist.
	 * 
	 * @param o
	 *            zu vergleichendes Objekt
	 * @return Wahrheitswert für die Gleichheit
	 */
	public boolean equals(Object o) {
		if (o instanceof Profil) {
			Profil p = (Profil) o;
			return p.getBenutzername().equals(benutzername);
		}
		return false;
	}

	/**
	 * Gibt zurück, ob das Profil vollständig ist, also ob alle Daten vorhanden
	 * sind.
	 * 
	 * @return Wahrheitswert über Vollständigkeit
	 */
	public boolean isComplete() {
		return (benutzername != null && gruppe != null && geschlecht != null
				&& alter != null && single != null && wohnort != null
				&& dabei != null && profilbild != null);

	}
}
