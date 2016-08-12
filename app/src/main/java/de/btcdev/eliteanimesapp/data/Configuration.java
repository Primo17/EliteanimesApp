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
import de.btcdev.eliteanimesapp.gui.CommentActivity;
import de.btcdev.eliteanimesapp.gui.PrivateMessageActivity;
import de.btcdev.eliteanimesapp.gui.ProfileActivity;

/**
 * Abstrakte Klasse zur Verwaltung von Daten, die immer wiederkehren und so
 * nicht immer neu berechnet werden müssen.
 */
public abstract class Configuration {

	private static String userName;
	//TODO remove this ASAP
	private static String password;
	private static int userId;
	private static String boardToken;
	private static int newCommentCount;
	private static int newMessageCount;
	private static Context context;
	private static int notificationId;

	/**
	 * Gibt das aktuelle Passwort zurück
	 * 
	 * @return verwendetes Passwort
	 */
	public static String getPassword() {
		return password;
	}

	/**
	 * Setzt ein neues Passwort
	 * 
	 * @param password
	 *            zu setzendes Passwort
	 */
	public static void setPassword(String password) {
		Configuration.password = password;
	}

	/**
	 * Gibt die aktuelle UserID des eingeloggten Benutzers zurück.
	 * 
	 * @return aktuelle UserID
	 */
	public static int getUserID(Context context) {
		if (context != null)
			Configuration.context = context;
		if (userId == 0) {
			if (Configuration.context != null) {
				Bundle bundle = new UserDataHelper(Configuration.context)
						.getData();
				userId = bundle.getInt("userid");
			}
		}
		if (userId == 0) {
			userId = NetworkService.instance(Configuration.context).getIdByCookie();
		}
		return userId;
	}

	/**
	 * Setzt eine neue UserID.
	 * 
	 * @param userId
	 *            UserID des eingeloggten Benutzers
	 */
	public static void setUserId(int userId) {
		Configuration.userId = userId;
		if (context != null) {
			new UserDataHelper(context).updateData(userName, Configuration.userId,
					boardToken);
		}
	}

	/**
	 * Setzt einen neuen Benutzernamen.
	 * 
	 * @param userName
	 *            Benutzername des eingeloggten Benutzers
	 */
	public static void setUserName(String userName) {
		Configuration.userName = new String(userName);
		new InfoThread(context);
		if (context != null) {
			new UserDataHelper(context).updateData(Configuration.userName, userId,
					boardToken);
		}
	}

	/**
	 * Gibt den Benutzernamen des eingeloggten Benutzers zurück
	 * 
	 * @return aktueller Benutzername
	 */
	public static String getUserName(Context context) {
		if (context != null)
			Configuration.context = context;
		if (userName == null || userName.isEmpty()) {
			if (Configuration.context != null) {
				Bundle bundle = new UserDataHelper(Configuration.context)
						.getData();
				userName = bundle.getString("name");
			}
		}
		if (userName == null) {
			userName = NetworkService.instance(Configuration.context)
					.getUserByCookie();
		}
		return userName;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		Configuration.context = context;
	}

	public static String getBoardToken() {
		if (boardToken == null || boardToken.isEmpty()) {
			if (context != null) {
				Bundle bundle = new UserDataHelper(context).getData();
				boardToken = bundle.getString("token");
			}
		}
		return boardToken;
	}

	public static void setBoardToken(String boardToken) {
		Configuration.boardToken = boardToken;
		if (context != null) {
			new UserDataHelper(context).updateData(userName, userId,
					Configuration.boardToken);
		}
	}

	public static int getNewCommentCount() {
		return newCommentCount;
	}

	public static void setNewCommentCount(int newCommentCount, Context context) {
		if (context != null)
			Configuration.context = context;
		if (newCommentCount > Configuration.newCommentCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(Configuration.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createCommentNotification(newCommentCount,
						Configuration.context);
		}
		Configuration.newCommentCount = newCommentCount;
	}

	public static int getNewMessageCount() {
		return newMessageCount;
	}

	public static void setNewMessageCount(int newMessageCount, Context context) {
		if (context != null)
			Configuration.context = context;
		if (newMessageCount > Configuration.newMessageCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(Configuration.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createPrivateMessageNotification(newMessageCount, Configuration.context);
		}
		Configuration.newMessageCount = newMessageCount;
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
		Intent resultIntent = new Intent(context, CommentActivity.class);
		resultIntent.putExtra("User", getUserName(context));
		resultIntent.putExtra("UserID", getUserID(context));
		Intent backIntent = new Intent(context, ProfileActivity.class);
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

	public static void createPrivateMessageNotification(int count, Context context) {
		String text;
		if (count == 1) {
			text = " neue Private Nachricht vorhanden";
		} else {
			text = " neue Private Nachrichten vorhanden";
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_drawer_pn)
				.setContentTitle("Neue Nachricht").setContentText(count + text);
		Intent resultIntent = new Intent(context, PrivateMessageActivity.class);
		Intent backIntent = new Intent(context, ProfileActivity.class);
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
