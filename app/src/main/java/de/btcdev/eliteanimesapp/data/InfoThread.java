package de.btcdev.eliteanimesapp.data;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InfoThread extends Thread {

	private Context context;
	private String userName;

	public static void runInfoThread(Context context, String userName) {
		new InfoThread(context, userName).start();
	}

	public InfoThread(Context context, String userName) {
		this.context = context;
		this.userName = userName;
	}

	public void run() {
		sendLoginAnalytics();
	}

	public void start() {
		super.start();
	}

	/**
	 * Konvertiert einen InputStream mithilfe eines Scanners zu einem String und
	 * gibt diesen zurück
	 * 
	 * @param is
	 *            InputStream, der konvertiert werden soll
	 * @return eine String-Repräsentation des InputStreams
	 * @throws IllegalStateException
	 *             wenn der zum konvertieren verwendete Scanner schon
	 *             geschlossen wurde
	 * @throws NoSuchElementException
	 *             wenn auf ein nicht vorhandenes Element des Scanners
	 *             zugegriffen wurde
	 */
	@SuppressWarnings("resource")
	public String convertStreamToString(InputStream is)
			throws IllegalStateException, NoSuchElementException {
		if (is == null) {
			return "";
		}
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public void sendLoginAnalytics() {
		PackageInfo packageInfo = null;
		String appVersion = null;
		String androidVersion = null;
		String phoneModel = null;
		while (packageInfo == null) {
			try {
				packageInfo = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				appVersion = packageInfo.versionName;
				androidVersion = android.os.Build.VERSION.RELEASE;
				phoneModel = android.os.Build.MANUFACTURER + ", "
						+ android.os.Build.PRODUCT;
			} catch (Exception e) {
			}
		}
		EasyTracker tracker = EasyTracker.getInstance(context);
		tracker.send(MapBuilder.createEvent(
				"login_action",
				"login",
				userName + ", " + appVersion + ", "
						+ androidVersion + ", " + phoneModel, null).build());
	}
}
