package de.btcdev.eliteanimesapp.data.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.database.UserDataHelper;
import de.btcdev.eliteanimesapp.data.threads.InfoThread;
import de.btcdev.eliteanimesapp.ui.activities.CommentActivity;
import de.btcdev.eliteanimesapp.ui.activities.PrivateMessageActivity;
import de.btcdev.eliteanimesapp.ui.activities.ProfileActivity;

/**
 * Service for all configurations like global values
 */
@Singleton
public class ConfigurationService {

	private EaApp eaApp;
	private String userName;
	//TODO remove this ASAP
	private String password;
	private int userId;
	private String boardToken;
	private int newCommentCount;
	private int newMessageCount;
	private Context context;
	private int notificationId;

	//TODO: save all values to sharedprefs before setting, load if not available
	/* TODO: Remove ConfigurationService access from:
	 	InfoThread? AnimelistCacheThread? PrivateMessageCacheThread?
	*/
	//TODO: remove context parameters from various methods

	@Inject
	public ConfigurationService(EaApp app) {
		this.eaApp = app;
		this.context = app.getApplicationContext();
	}

	/**
	 * Gibt das aktuelle Passwort zurück
	 * 
	 * @return verwendetes Passwort
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setzt ein neues Passwort
	 * 
	 * @param password
	 *            zu setzendes Passwort
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gibt die aktuelle UserID des eingeloggten Benutzers zurück.
	 * 
	 * @return aktuelle UserID
	 */
	public int getUserID(Context context) {
		if (context != null)
			this.context = context;
		if (userId == 0) {
			if (this.context != null) {
				Bundle bundle = new UserDataHelper(this.context)
						.getData();
				userId = bundle.getInt("userid");
			}
		}
		//TODO: Fix this abomination
		/*if (userId == 0) {
			userId = NetworkService.instance(this.context).getIdByCookie();
		}*/
		return userId;
	}

	/**
	 * Setzt eine neue UserID.
	 * 
	 * @param userId
	 *            UserID des eingeloggten Benutzers
	 */
	public void setUserId(int userId) {
		this.userId = userId;
		if (context != null) {
			new UserDataHelper(context).updateData(userName, this.userId,
					boardToken);
		}
	}

	/**
	 * Setzt einen neuen Benutzernamen.
	 * 
	 * @param userName
	 *            Benutzername des eingeloggten Benutzers
	 */
	public void setUserName(String userName) {
		this.userName = new String(userName);
		InfoThread.runInfoThread(context, userName);
		if (context != null) {
			new UserDataHelper(context).updateData(this.userName, userId,
					boardToken);
		}
	}

	/**
	 * Gibt den Benutzernamen des eingeloggten Benutzers zurück
	 * 
	 * @return aktueller Benutzername
	 */
	public String getUserName(Context context) {
		if (context != null)
			this.context = context;
		if (userName == null || userName.isEmpty()) {
			if (this.context != null) {
				Bundle bundle = new UserDataHelper(this.context)
						.getData();
				userName = bundle.getString("name");
			}
		}
		//TODO: fix this abomination
		/*if (userName == null) {
			userName = NetworkService.instance(this.context)
					.getUserByCookie();
		}*/
		return userName;
	}

	public Context getContext() {
		return eaApp.getApplicationContext();
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getBoardToken() {
		if (boardToken == null || boardToken.isEmpty()) {
			if (context != null) {
				Bundle bundle = new UserDataHelper(context).getData();
				boardToken = bundle.getString("token");
			}
		}
		return boardToken;
	}

	public void setBoardToken(String boardToken) {
		this.boardToken = boardToken;
		if (context != null) {
			new UserDataHelper(context).updateData(userName, userId,
					this.boardToken);
		}
	}

	public int getNewCommentCount() {
		return newCommentCount;
	}

	public void setNewCommentCount(int newCommentCount, Context context) {
		if (context != null)
			this.context = context;
		if (newCommentCount > this.newCommentCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(this.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createCommentNotification(newCommentCount,
						this.context);
		}
		this.newCommentCount = newCommentCount;
	}

	public int getNewMessageCount() {
		return newMessageCount;
	}

	public void setNewMessageCount(int newMessageCount, Context context) {
		if (context != null)
			this.context = context;
		if (newMessageCount > this.newMessageCount) {
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(this.context);
			if (defaultprefs.getBoolean("pref_notifications", true))
				createPrivateMessageNotification(newMessageCount, this.context);
		}
		this.newMessageCount = newMessageCount;
	}

	public void createCommentNotification(int count, Context context) {
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

	public void createPrivateMessageNotification(int count, Context context) {
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
