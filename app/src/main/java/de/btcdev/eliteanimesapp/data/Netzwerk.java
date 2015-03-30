package de.btcdev.eliteanimesapp.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.btcdev.eliteanimesapp.R;

/**
 * Klasse für alle verwendeten Netzwerk-Aufgaben. Verwaltet die Adresse von EA,
 * die gesetzten Cookies und den HttpClient.
 */
public class Netzwerk {

	private String eaURL = "http://www.eliteanimes.com";
	private DefaultHttpClient httpclient;
	private List<Cookie> cookies;
	private static Context context = null;
	private String apikey = "8HB3GcTOiKm973zW9c1ioWwJa4ThDPzV";
	private static Netzwerk unique = null;
	private boolean hasCookies = false;

	public static Netzwerk instance(Context context) {
		if (unique == null)
			unique = new Netzwerk(context);
		if (context == null)
			Netzwerk.context = context;
		return unique;
	}

	/**
	 * Ein neues Netzwerk wird erzeugt. Dafür wird ein neuer HttpClient mit
	 * passendem User-Agent gesetzt.
	 * 
	 * @param context
	 *            Context der aufrufenden Klasse, wird für String-Ressourcen
	 *            benötigt.
	 */
	private Netzwerk(Context context) {
		httpclient = new DefaultHttpClient();
		ClientConnectionManager cmgr = httpclient.getConnectionManager();
		HttpParams param = httpclient.getParams();
		httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				param, cmgr.getSchemeRegistry()), param);
		param.setParameter(CoreProtocolPNames.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		Netzwerk.context = context;
		hasCookies = loadCookies();
	}

	/**
	 * Es wird versucht, den Benutzer mit den in der Konfiguration gesetzten
	 * Benutzernamen und Passwort einzuloggen. Zuvor wird überprüft, ob schon
	 * ein passender Cookie existiert, der Benutzer also schon eingeloggt ist.
	 * Wenn nicht, werden die Cookies von EA geladen und eine Post-Anfrage mit
	 * den entsprechenden Daten abgeschickt. Bei erfolgreichem Login werden die
	 * jeweiligen Cookies gespeichert und die UserID in der Konfiguration
	 * gesetzt.
	 * 
	 * @throws EAException
	 *             wenn ein Verbindungsfehler jeglicher Art auftritt
	 */
	public String login() throws EAException {
		try {
			if (!isLoggedIn()) {
				cookies = httpclient.getCookieStore().getCookies();
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				HttpPost httpost = new HttpPost(eaURL + "/api/login");
				nvps.add(new BasicNameValuePair("name", Konfiguration
						.getBenutzername(context)));
				nvps.add(new BasicNameValuePair("password", Konfiguration
						.getPasswort()));
				nvps.add(new BasicNameValuePair("apikey", apikey));
				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				HttpResponse response = httpclient.execute(httpost);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				String input = convertStreamToString(is);
				if (entity != null) {
					entity.consumeContent();
				}
				int userid = 0;
				for (Cookie c : cookies) {
					if (c.getName().equals("user_id"))
						userid = Integer.parseInt(c.getValue());
				}
				saveCookies();
				if (userid != 0)
					Konfiguration.setUserID(userid);
				return input;
			}
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return null;
	}

	/**
	 * Falls ein Benutzer eingeloggt ist, wird dieser ausgeloggt. Dazu werden
	 * alle Login-Cookies gelöscht. (Der gesamte Vorgang wird in einem eigenen
	 * Thread ausgeführt)
	 * 
	 * @throws EAException
	 */
	public void logout() throws EAException {
		try {
			if (isLoggedIn()) {
				new Thread(new Runnable() {
					public void run() {
						try {
							List<NameValuePair> nvps = new ArrayList<NameValuePair>();
							HttpPost httpost = new HttpPost(eaURL
									+ "/api/logout");
							nvps.add(new BasicNameValuePair("apikey", apikey));
							httpost.setEntity(new UrlEncodedFormEntity(nvps,
									HTTP.UTF_8));
							HttpResponse response = httpclient.execute(httpost);
							HttpEntity entity = response.getEntity();
							if (entity != null) {
								entity.consumeContent();
							}
							cookies = httpclient.getCookieStore().getCookies();
						} catch (Exception e) {

						}
					}
				}).start();
				deleteCookies();
			}
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * L�dt den Token als JSON.
	 * 
	 * @return JSON mit dem Token
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getToken() throws EAException {
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getToken");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Anzahl neuer Kommentare und Privater Nachrichten und gibt diese
	 * als JSON zurück.
	 * 
	 * @return JSON mit den News
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getNews() throws EAException {
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getUserUpdates");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt den HTML-Code des eigenen Profils und gibt diesen als String zurück.
	 * 
	 * @return HTML-Code des eigenen Profils als String
	 * @throws EAException
	 *             bei Verbindungs- und Streamfehlern jeglicher Art
	 */
	public String getProfil() throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getProfil");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("id", Integer
					.toString(Konfiguration.getUserID(context))));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Lädt den HTML-Code des Profils mit den übergebenen Daten und gibt diesen
	 * als String zurück.
	 * 
	 * @param benutzername
	 *            der Benutzername des gewünschten Profils
	 * @param userID
	 *            die UserID des gewünschten Profils
	 * @return HTML-Code des Profils als String
	 * @throws EAException
	 *             bei Verbindungs- und Streamfehlern jeglicher Art
	 */
	public String getProfil(String benutzername, int userID) throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getProfil");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("id", Integer.toString(userID)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Lädt die Profilbeschreibung mit der API-Funktion getProfilDescription und
	 * gibt das JSON-Ergebnis zurück.
	 * 
	 * @param benutzername
	 *            Name des Benutzers
	 * @param userID
	 *            Id des Benutzers
	 * @return JSON-Ergebnis
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getProfilBeschreibung(String benutzername, int userID)
			throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getProfilDescription");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("id", Integer.toString(userID)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Lädt den HTML-Code der Freundesliste mit den übergebenen Daten und gibt
	 * diesen als String zurück.
	 * 
	 * @param benutzername
	 *            der Benutzername des Profils, zu der die Freundesliste gehört
	 * @param userID
	 *            die UserID des Profils, zu der die Freundesliste gehört
	 * @return HTML-Code der Freundesliste als String
	 * @throws EAException
	 *             bei Verbindungs- und Streamfehlern jeglicher Art
	 */
	public String getFreundesliste(String benutzername, int userID)
			throws EAException {
		String input = null;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getFriendList");
			nvps.add(new BasicNameValuePair("id", Integer.toString(userID)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Lädt die gewünschte Kommentarseite und gibt die JSON-Antwort der API
	 * zurück.
	 * 
	 * @param seitenzahl
	 *            die Seitenzahl der gewünschten Kommentarseite
	 * @param benutzername
	 *            der Benutzername des Profils, zu dem die Kommentarseite gehört
	 * @param userID
	 *            die UserID des Profils, zu dem die Kommentarseite gehört
	 * @return JSON-Antwort der API
	 * @throws EAException
	 *             bei Verbindungs- und Streamfehlern jeglicher Art
	 */
	public String getCommentSite(int seitenzahl, String benutzername, int userID)
			throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getComments");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("apikey", apikey));
			nvps.add(new BasicNameValuePair("id", Integer.toString(userID)));
			nvps.add(new BasicNameValuePair("page", Integer
					.toString(seitenzahl)));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream entitystream = entity.getContent();
			input = convertStreamToString(entitystream);
			entitystream.close();
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Schickt den übergebenen Kommentar per POST an den übergebenen Benutzer
	 * und überprüft, ob der Kommentar erfolgreich übermittelt wurde.
	 * 
	 * @param comment
	 *            Kommentartext
	 * @param user
	 *            User, der den Kommentar erhalten soll
	 * @param userID
	 *            UserID des Users, der den Kommentar erhalten soll
	 * @return Wahrheitswert ob Senden erfolgreich
	 */
	public boolean postComment(String comment, String user, int userID)
			throws EAException {
		try {
			if (Konfiguration.getForumtoken() != null) {
				HttpResponse response;
				HttpEntity entity;
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				HttpPost httpost = new HttpPost(eaURL + "/profil/" + userID
						+ "/" + user);
				nvps.add(new BasicNameValuePair("comment", comment));
				nvps.add(new BasicNameValuePair("forumtoken", Konfiguration
						.getForumtoken()));
				nvps.add(new BasicNameValuePair("id", "" + userID));
				nvps.add(new BasicNameValuePair("name", user));
				nvps.add(new BasicNameValuePair("submit", "Eintragen"));
				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				response = httpclient.execute(httpost);
				entity = response.getEntity();
				InputStream is = entity.getContent();
				String input = convertStreamToString(is);
				if (entity != null) {
					entity.consumeContent();
				}
				return new EAParser(null).checkComment(input);
			}
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return true;
	}

	/**
	 * Schickt den editierten Kommentar per POST an den übergebenen Benutzer.
	 * 
	 * @param comment
	 *            Kommentartext
	 * @param user
	 *            User, der den Kommentar erhalten soll
	 * @param userID
	 *            UserID des Users, der den Kommentar erhalten soll
	 */
	public void editComment(Kommentar comment, String user, int userID)
			throws EAException {
		try {
			if (Konfiguration.getForumtoken() != null) {
				HttpResponse response;
				HttpEntity entity;
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				HttpPost httpost = new HttpPost(eaURL + "/profil/" + userID
						+ "/" + user);
				nvps.add(new BasicNameValuePair("commentedit", comment
						.getText()));
				nvps.add(new BasicNameValuePair("commentid", Integer
						.toString(comment.getId())));
				nvps.add(new BasicNameValuePair("editcomment", "Editieren"));
				nvps.add(new BasicNameValuePair("forumtoken", Konfiguration
						.getForumtoken()));
				nvps.add(new BasicNameValuePair("fromuser", ""
						+ Konfiguration.getUserID(context)));
				nvps.add(new BasicNameValuePair("touser", "" + userID));
				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				response = httpclient.execute(httpost);
				entity = response.getEntity();
				if (entity != null) {
					entity.consumeContent();
				}
			}
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Löscht den Kommentar mit der übergebenen Kommentar-ID, falls die
	 * Berechtigung dazu vorhanden ist
	 * 
	 * @param commentid
	 *            Kommentar-ID des zu löschenden Kommentars
	 */
	public void deleteComment(String commentid) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/commentdelete.php?id="
						+ commentid + "&ft=" + Konfiguration.getForumtoken());
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Lädt die Postfachseite der angegebenen Seitenzahl des aktuellen Benutzers
	 * und gibt die PNs als JSON-String zurück.
	 * 
	 * @param seitenzahl
	 *            Seitenzahl des Postfaches
	 * @return JSON der PNs
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getPNSite(int seitenzahl) throws EAException {
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getInboxPMs");
			nvps.add(new BasicNameValuePair("page", Integer
					.toString(seitenzahl)));
			nvps.add(new BasicNameValuePair("bbcode", "false"));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt den Inhalt einer ausgewählten PN und gibt den HTML-Code dieser
	 * zurück.
	 * 
	 * @param id
	 *            ID der PN
	 * @return JSON-Antwort der API
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getPN(int id) throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getPM");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("apikey", apikey));
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(false)));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream entitystream = entity.getContent();
			input = convertStreamToString(entitystream);
			entitystream.close();
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Lädt den Input (BBCode) einer ausgewählten PN.
	 * 
	 * @param id
	 *            ID der PN
	 * @return JSON-Antwort der API
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getPNInput(int id) throws EAException {
		String input = null;
		try {
			HttpPost httpost = new HttpPost(eaURL + "/api/getPM");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("apikey", apikey));
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(true)));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream entitystream = entity.getContent();
			input = convertStreamToString(entitystream);
			entitystream.close();
			if (entity != null)
				entity.consumeContent();
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
		return input;
	}

	/**
	 * Schickt eine PN mit den übergebenen Werten ab und gibt den Rückgabestring
	 * der API zurück
	 * 
	 * @param userId
	 *            ID des Empfängers
	 * @param text
	 *            Text der PN
	 * @param betreff
	 *            Betreff der PN
	 * @return Rückgabewert der API
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String sendPN(int userId, String text, String betreff)
			throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/sendPM");
			nvps.add(new BasicNameValuePair("subject", betreff));
			nvps.add(new BasicNameValuePair("uid", Integer.toString(userId)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			nvps.add(new BasicNameValuePair("text", text));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			is.close();
			return input;
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Löscht die PN mit der übergebenen ID.
	 * 
	 * @param pnid
	 *            ID der PN
	 * @return Json-Antwort der API
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String deletePN(String pnid) throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/deletePM/");
			nvps.add(new BasicNameValuePair("id", pnid));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			is.close();
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Sendet eine Antwort der PN mit der übergebenen Id.
	 * 
	 * @param id
	 *            Id der PN die beantwortet werden soll
	 * @param text
	 *            Text der PN
	 * @return JSON-Antwort der API
	 */
	public String answerPN(int id, String text) throws EAException {
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/answerPM");
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("text", text));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Fügt den User mit der übergebenen User-ID zu den Freunden hinzu.
	 * 
	 * @param id
	 *            User-ID des Benutzers der hinzugefügt werden soll
	 * @throws EAException
	 */
	public void addFriend(String id) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/friend/add/" + id + "/"
						+ Konfiguration.getForumtoken());
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Löscht den Freund mit der übergebenen User-ID.
	 * 
	 * @param id
	 *            User-ID des Freunds, der gelöscht werden soll
	 * @throws EAException
	 */
	public void deleteFriend(String id) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/friend/delete/" + id
						+ "/" + Konfiguration.getForumtoken());
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Lädt den JSON-Code der Freundschaftsanfragen und gibt diesen zurück.
	 * 
	 * @return JSON-Code der Freundschaftsanfragen
	 * @throws EAException
	 *             Bei Fehlern aller Art
	 */
	public String getFriendRequests() throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getFriendRequests");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Akzeptiert die Freundschaftsanfrage des Users mit der übergebenen ID.
	 * 
	 * @param id
	 *            ID der Users
	 * @throws EAException
	 *             Bei allen Fehlern
	 */
	public void acceptFriendRequest(String id) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/friend/accept/" + id
						+ "/" + Konfiguration.getForumtoken());
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Lehnt die Freundschaftsanfrage des übergebenen Users ab.
	 * 
	 * @param id
	 *            ID des Users
	 * @throws EAException
	 *             Bei Fehlern aller Art
	 */
	public void declineFriendRequest(String id) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/friend/decline/" + id
						+ "/" + Konfiguration.getForumtoken());
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Gibt den JSON-Code der Übersicht der blockierten Benutzer zurück
	 * 
	 * @return JSON-Code der Tabelle
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String getBlockedUsers() throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getBlockedUser");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Hebt die Blockierung der übergebenen Benutzers auf.
	 * 
	 * @param id
	 *            ID des blockierten Benutzers
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public void unblockUser(String id) throws EAException {
		if (Konfiguration.getForumtoken() != null) {
			try {
				HttpGet httpget = new HttpGet(eaURL + "/blockuser.php?id=" + id
						+ "&ft=" + Konfiguration.getForumtoken() + "&unblock");
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null)
					entity.consumeContent();
			} catch (IllegalArgumentException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_402));
			} catch (ClientProtocolException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_101));
			} catch (IOException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_302));
			} catch (IllegalStateException e) {
				throw new EAException(context.getResources().getString(
						R.string.error_303));
			} catch (Exception e) {
				throw new EAException(context.getResources().getString(
						R.string.error_unknown)
						+ e.getClass().toString());
			}
		}
	}

	/**
	 * Sucht nach Benutzern mit dem übergebenen Namen.
	 * 
	 * @param name
	 *            Name, nach dem gesucht werden soll
	 * @return JSON-Code der gefundenen User
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public String searchUser(String name) throws EAException {
		String input;
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/searchUser");
			nvps.add(new BasicNameValuePair("name", name));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			is.close();
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Animeliste des übergebenen Benutzers und gibt den HTML-Code der
	 * Seite als String zurück.
	 * 
	 * @param user
	 *            Name des Benutzers
	 * @param id
	 *            Id des Benutzers
	 * @return String der HTML-Seite
	 * @throws EAException
	 *             bei allen Netzwerkfehlern
	 */
	public String getAnimelist(String user, int id) throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getAnimelist");
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Bewertet den übergebenen Anime. Status: 1: Am Schauen 2: Komplett 3: Kurz
	 * Aufgehürt 4: Abgebrochen 5: Geplant
	 * 
	 * @param id
	 *            ID des Animes
	 * @param score
	 *            Punktzahl des Animes
	 * @param fortschritt
	 *            Fortschritt des Benutzers
	 * @param folgenzahl
	 *            Gesamte Folgenzahl des Animes
	 * @param status
	 *            Status des Animes
	 * @throws EAException
	 *             bei allen Fehlern
	 */
	public void rateAnime(int id, double score, int fortschritt,
			int folgenzahl, int status) throws EAException {
		try {
			if (Konfiguration.getForumtoken() != null) {
				HttpResponse response;
				HttpEntity entity;
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				HttpPost httpost = new HttpPost(eaURL + "/animelist/"
						+ Konfiguration.getUserID(context) + "/"
						+ Konfiguration.getBenutzername(context));
				nvps.add(new BasicNameValuePair("bewerten", "Bewerten"));
				nvps.add(new BasicNameValuePair("forumtoken", Konfiguration
						.getForumtoken()));
				nvps.add(new BasicNameValuePair("id", "" + id));
				nvps.add(new BasicNameValuePair("score", "" + score));
				nvps.add(new BasicNameValuePair("seen", "" + fortschritt));
				nvps.add(new BasicNameValuePair("status", "" + status));
				nvps.add(new BasicNameValuePair("von", "" + folgenzahl));
				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				response = httpclient.execute(httpost);
				entity = response.getEntity();
				if (entity != null) {
					entity.consumeContent();
				}
			}
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Forenübersicht
	 * 
	 * @return Json-String der Forenübersicht
	 * @throws EAException
	 *             bei allen Netzwerkfehlern
	 */
	public String getForen() throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getForums");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Forenstatistik
	 * 
	 * @return Json-String der Forenstatistik
	 * @throws EAException
	 *             bei allen Netzwerkfehlern
	 */
	public String getForenStatistik() throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getStatistics");
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Threadübersicht eines Forums
	 * 
	 * @param id
	 *            Die Id des Forums
	 * @param page
	 *            Die Seite des Forums
	 * @return Json-String der Thread�bersicht
	 * @throws EAException
	 *             bei allen Netzwerkfehlern
	 */
	public String getThreads(int id, int page) throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getForum");
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("page", Integer.toString(page)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt die Posts eines Threads
	 * 
	 * @param id
	 *            Die Id des Threads
	 * @param page
	 *            Die Seite des Threads
	 * @return Json-String der Threadübersicht
	 * @throws EAException
	 *             bei allen Netzwerkfehlern
	 */
	public String getPosts(int id, int page) throws EAException {
		String input;
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getForumThread");
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("page", Integer.toString(page)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_302));
		} catch (IllegalStateException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_303));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Schickt den übergebenen Post-Text an die API und überprüft, ob der Post
	 * erfolgreich übermittelt wurde.
	 * 
	 * @param text
	 *            Text des Posts
	 * @param threadId
	 *            Id des Threads
	 * @return Wahrheitswert ob Senden erfolgreich
	 */
	public boolean addPost(String text, int threadId) throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			HttpPost httpost = new HttpPost(eaURL + "/api/addForumPost");
			nvps.add(new BasicNameValuePair("id", Integer.toString(threadId)));
			nvps.add(new BasicNameValuePair("text", text));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input.contains("status");
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Schickt den übergebenen Post-Text an die API und überprüft, ob der Post
	 * erfolgreich übermittelt wurde.
	 * 
	 * @param text
	 *            Text des Posts
	 * @param postId
	 *            Id des Posts
	 * @return Wahrheitswert ob Senden erfolgreich
	 */
	public boolean editPost(String text, int postId) throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			HttpPost httpost = new HttpPost(eaURL + "/api/editForumPost");
			nvps.add(new BasicNameValuePair("id", Integer.toString(postId)));
			nvps.add(new BasicNameValuePair("text", text));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input.contains("status");
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Löscht einen Forumpost.
	 * 
	 * @param postId
	 *            Id des Posts
	 * @return Wahrheitswert ob Senden erfolgreich
	 */
	public boolean deletePost(int postId) throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/deleteForumPost");
			nvps.add(new BasicNameValuePair("id", Integer.toString(postId)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input.contains("status");
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Lädt den gewünschten Forum-Post.
	 * 
	 * @param id
	 *            Id des Posts
	 * @param bbcode
	 *            Ob der Post als bbcode geladen werden soll
	 * @return Text des Posts als String
	 * @throws EAException
	 *             Bei allen Fehlern
	 */
	public String getPost(int id, boolean bbcode) throws EAException {
		try {
			HttpResponse response;
			HttpEntity entity;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			HttpPost httpost = new HttpPost(eaURL + "/api/getForumPost");
			nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
			nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(bbcode)));
			nvps.add(new BasicNameValuePair("apikey", apikey));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = httpclient.execute(httpost);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			String input = convertStreamToString(is);
			if (entity != null) {
				entity.consumeContent();
			}
			return input;
		} catch (NumberFormatException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_401));
		} catch (IllegalArgumentException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_402));
		} catch (ClientProtocolException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_101));
		} catch (IOException e) {
			throw new EAException(context.getResources().getString(
					R.string.error_301));
		} catch (Exception e) {
			throw new EAException(context.getResources().getString(
					R.string.error_unknown)
					+ e.getClass().toString());
		}
	}

	/**
	 * Konvertiert einen InputStream mithilfe eines Scanners zu einem String und
	 * gibt diesen zurück
	 * 
	 * @param is
	 *            InputStream, der konvertiert werden soll
	 * @return eine String-Repr�sentation des InputStreams
	 * @throws IllegalStateException
	 *             wenn der zum konvertieren verwendete Scanner schon
	 *             geschlossen wurde
	 * @throws NoSuchElementException
	 *             wenn auf ein nicht vorhandenes Element des Scanners
	 *             zugegriffen wurde
	 */
	@SuppressWarnings("resource")
	public String convertStreamToString2(InputStream is)
			throws IllegalStateException, NoSuchElementException {
		if (is == null) {
			return "";
		}
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * Konvertiert den InputStream mithilfe eines StringWriters zu einem String
	 * und gibt diesen zurück.
	 * 
	 * @param is
	 *            Zu konvertierender InputStream
	 * @return String-Repräsentation des Streams
	 */
	public String convertStreamToString(InputStream is) {
		char[] buff = new char[1024];
		Writer stringWriter = new StringWriter();
		try {
			Reader bReader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			int n;
			while ((n = bReader.read(buff)) != -1) {
				stringWriter.write(buff, 0, n);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				stringWriter.close();
				is.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return stringWriter.toString();
	}

	/**
	 * Überprüft, ob schon Login-Cookies vorhanden sind und ob diese mit dem
	 * Benutzernamen der Konfiguration übereinstimmen.
	 * 
	 * @return Wahrheitswert, ob der aktuelle Benutzer schon eingeloggt ist
	 */
	public boolean isLoggedIn() {
		cookies = httpclient.getCookieStore().getCookies();
		String username = "";
		for (Cookie c : cookies) {
			if (c.getName().equals("user_name"))
				username = c.getValue();
		}
		if (username != null && Konfiguration.getBenutzername(context) != null) {
			return Konfiguration.getBenutzername(context).equals(username);
		}
		return false;
	}

	/**
	 * Speichert die Cookies als Json-String in den SharedPreferences.
	 */
	public void saveCookies() {
		CookieStore store = httpclient.getCookieStore();
		List<Cookie> list = store.getCookies();
		Gson gson = new Gson();
		String json = gson.toJson(list);
		SharedPreferences prefs = context.getSharedPreferences("cookies",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("cookies", json);
		editor.apply();
	}

	/**
	 * Lädt die Cookies, falls vorhanden, aus einem Json-String in den
	 * CookieStore und setzt die UserID und den Benutzernamen.
	 * 
	 * @return Wahrheitswert, ob Cookies vorhanden waren
	 */
	public boolean loadCookies() {
		SharedPreferences prefs = context.getSharedPreferences("cookies",
				Context.MODE_PRIVATE);
		String json = prefs.getString("cookies", "");
		if (json.equals("")) {
			return false;
		} else {
			Gson gson = new Gson();
			Type collectionType = new TypeToken<Collection<BasicClientCookie>>() {
			}.getType();
			List<Cookie> list = gson.fromJson(json, collectionType);
			CookieStore store = httpclient.getCookieStore();
			for (Cookie c : list) {
				store.addCookie(c);
			}
			list = store.getCookies();
			httpclient.setCookieStore(store);
			for (Cookie c : list) {
				if (c.getName().equals("user_id")) {
					Konfiguration.setUserID(Integer.parseInt(c.getValue()));
				}
				if (c.getName().equals("user_name")) {
					Konfiguration.setBenutzername(c.getValue());
				}
			}
			list = httpclient.getCookieStore().getCookies();
		}
		return true;
	}

	/**
	 * Gibt zurück, ob das Netzwerk die Login-Cookies besitzt.
	 * 
	 * @return Wahrheitswert, ob Login-Cookies vorhanden
	 */
	public boolean hasCookies() {
		cookies = httpclient.getCookieStore().getCookies();
		String username = "";
		for (Cookie c : cookies) {
			if (c.getName().equals("user_name"))
				username = c.getValue();
		}
		hasCookies = !username.equals("");
		return hasCookies;
	}

	/**
	 * Löscht den Json-String der Cookies aus den SharedPreferences.
	 */
	public void deleteCookies() {
		SharedPreferences prefs = context.getSharedPreferences("cookies",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove("cookies");
		editor.apply();
		hasCookies = false;
	}

	public String getUserByCookie() {
		cookies = httpclient.getCookieStore().getCookies();
		String username = "";
		for (Cookie c : cookies) {
			if (c.getName().equals("user_name"))
				username = c.getValue();
		}
		return username;
	}

	public int getIdByCookie() {
		cookies = httpclient.getCookieStore().getCookies();
		int id = 0;
		for (Cookie c : cookies) {
			if (c.getName().equals("user_id"))
				try {
					id = Integer.parseInt(c.getValue());
				} catch (Exception e) {

				}
		}
		return id;
	}
}
