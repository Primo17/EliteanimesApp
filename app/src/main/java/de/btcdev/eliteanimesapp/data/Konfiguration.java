package de.btcdev.eliteanimesapp.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.database.UserDataHelper;
import de.btcdev.eliteanimesapp.gui.KommentarActivity;
import de.btcdev.eliteanimesapp.gui.PNActivity;
import de.btcdev.eliteanimesapp.gui.ProfilActivity;

/**
 * Abstrakte Klasse zur Verwaltung von Daten, die immer wiederkehren und so
 * nicht immer neu berechnet werden müssen.
 */
public abstract class Konfiguration {

	private static String benutzername;
	private static String passwort;
	private static int userID;
	private static String forumtoken;
	private static int newCommentCount;
	private static int newMessageCount;
	private static Context context;
	private static int notificationId;

	/**
	 * Gibt das aktuelle Passwort zurück
	 * 
	 * @return verwendetes Passwort
	 */
	public static String getPasswort() {
		return passwort;
	}

	/**
	 * Setzt ein neues Passwort
	 * 
	 * @param passwortNeu
	 *            zu setzendes Passwort
	 */
	public static void setPasswort(String passwortNeu) {
		passwort = passwortNeu;
	}

	/**
	 * Gibt die aktuelle UserID des eingeloggten Benutzers zurück.
	 * 
	 * @return aktuelle UserID
	 */
	public static int getUserID(Context context) {
		if (context != null)
			Konfiguration.context = context;
		if (userID == 0) {
			if (Konfiguration.context != null) {
				Bundle bundle = new UserDataHelper(Konfiguration.context)
						.getData();
				userID = bundle.getInt("userid");
			}
		}
		if (userID == 0) {
			userID = Netzwerk.instance(Konfiguration.context).getIdByCookie();
		}
		return userID;
	}

	/**
	 * Setzt eine neue UserID.
	 * 
	 * @param userIDNeu
	 *            UserID des eingeloggten Benutzers
	 */
	public static void setUserID(int userIDNeu) {
		userID = userIDNeu;
		if (context != null) {
			new UserDataHelper(context).updateData(benutzername, userID,
					forumtoken);
		}
	}

	/**
	 * Setzt einen neuen Benutzernamen.
	 * 
	 * @param benutzernameNeu
	 *            Benutzername des eingeloggten Benutzers
	 */
	public static void setBenutzername(String benutzernameNeu) {
		benutzername = new String(benutzernameNeu);
		new InfoThread(context);
		if (context != null) {
			new UserDataHelper(context).updateData(benutzername, userID,
					forumtoken);
		}
	}

	/**
	 * Gibt den Benutzernamen des eingeloggten Benutzers zurück
	 * 
	 * @return aktueller Benutzername
	 */
	public static String getBenutzername(Context context) {
		if (context != null)
			Konfiguration.context = context;
		if (benutzername == null || benutzername.isEmpty()) {
			if (Konfiguration.context != null) {
				Bundle bundle = new UserDataHelper(Konfiguration.context)
						.getData();
				benutzername = bundle.getString("name");
			}
		}
		if (benutzername == null) {
			benutzername = Netzwerk.instance(Konfiguration.context)
					.getUserByCookie();
		}
		return benutzername;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		Konfiguration.context = context;
	}

	public static String getForumtoken() {
		if (forumtoken == null || forumtoken.isEmpty()) {
			if (context != null) {
				Bundle bundle = new UserDataHelper(context).getData();
				forumtoken = bundle.getString("token");
			}
		}
		return forumtoken;
	}

	public static void setForumtoken(String forumtokenNeu) {
		forumtoken = forumtokenNeu;
		if (context != null) {
			new UserDataHelper(context).updateData(benutzername, userID,
					forumtoken);
		}
	}

	public static int getNewCommentCount() {
		return newCommentCount;
	}

	public static void setNewCommentCount(int newCommentCount, Context context) {
		if (context != null)
			Konfiguration.context = context;
		if (newCommentCount > Konfiguration.newCommentCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(Konfiguration.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createCommentNotification(newCommentCount,
						Konfiguration.context);
		}
		Konfiguration.newCommentCount = newCommentCount;
	}

	public static int getNewMessageCount() {
		return newMessageCount;
	}

	public static void setNewMessageCount(int newMessageCount, Context context) {
		if (context != null)
			Konfiguration.context = context;
		if (newMessageCount > Konfiguration.newMessageCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(Konfiguration.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createPnNotification(newMessageCount, Konfiguration.context);
		}
		Konfiguration.newMessageCount = newMessageCount;
	}

	public static void createCommentNotification(int count, Context context) {
		String text;
		if (count == 1) {
			text = " neuer Kommentar vorhanden";
		} else {
			text = " neue Kommentare vorhanden";
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_drawer_pn)
				.setContentTitle("Neue Kommentare")
				.setContentText(count + text);
		Intent resultIntent = new Intent(context, KommentarActivity.class);
		resultIntent.putExtra("Benutzer", getBenutzername(context));
		resultIntent.putExtra("UserID", getUserID(context));
		Intent backIntent = new Intent(context, ProfilActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntent(backIntent);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
	}

	public static void createPnNotification(int count, Context context) {
		String text;
		if (count == 1) {
			text = " neue Private Nachricht vorhanden";
		} else {
			text = " neue Private Nachrichten vorhanden";
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_drawer_pn)
				.setContentTitle("Neue Nachricht").setContentText(count + text);
		Intent resultIntent = new Intent(context, PNActivity.class);
		Intent backIntent = new Intent(context, ProfilActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntent(backIntent);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
	}
}
