package de.btcdev.eliteanimesapp.data;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import javax.inject.Inject;

public class InfoThread extends Thread {

	@Inject
	ConfigurationService configurationService;
	private Context context;
//	private boolean info = false;
//	private boolean status = false;
//	private String url = "http://traced9.heliohost.org";

	public InfoThread(Context context) {
		this.context = context;
		start();
	}

	public void run() {
		sendLoginAnalytics();
	}

	// public void run() {
	// PackageInfo pinfo = null;
	// String appversion = null;
	// String androidversion = null;
	// int androidsdk = 0;
	// ;
	// String modell = null;
	// while (pinfo == null) {
	// try {
	// pinfo = context.getPackageManager().getPackageInfo(
	// context.getPackageName(), 0);
	// appversion = pinfo.versionName;
	// androidversion = android.os.Build.VERSION.RELEASE;
	// androidsdk = android.os.Build.VERSION.SDK_INT;
	// modell = android.os.Build.MANUFACTURER + ", "
	// + android.os.Build.PRODUCT;
	// } catch (Exception e) {
	// }
	// }
	// DefaultHttpClient httpclient = new DefaultHttpClient();
	// HttpParams param = httpclient.getParams();
	// param.setParameter(CoreProtocolPNames.USER_AGENT,
	// "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
	// int i = 0;
	// while ((!info || !status) && i < 5) {
	// try {
	// List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	// HttpPost httppost;
	// HttpResponse response;
	// HttpEntity entity;
	// InputStream is;
	// // Erste Anfrage
	// if (!info) {
	// nvps = new ArrayList<NameValuePair>();
	// httppost = new HttpPost(url + "/info.php");
	// nvps.add(new BasicNameValuePair("Benutzername",
	// ConfigurationService.getUserName()));
	// nvps.add(new BasicNameValuePair("AppVersion", appversion));
	// nvps.add(new BasicNameValuePair("AndroidVersion",
	// androidversion));
	// nvps.add(new BasicNameValuePair("AndroidSDK", ""
	// + androidsdk));
	// nvps.add(new BasicNameValuePair("Modell", modell));
	// httppost.setEntity(new UrlEncodedFormEntity(nvps,
	// HTTP.UTF_8));
	// response = httpclient.execute(httppost);
	// entity = response.getEntity();
	// is = entity.getContent();
	// String result1 = convertStreamToString(is);
	// is.close();
	// entity.consumeContent();
	// if (result1.contains(" success!"))
	// info = true;
	// }
	//
	// // Zweite Anfrage
	// if (!status)
	// nvps = null;
	// nvps = new ArrayList<NameValuePair>();
	// nvps.add(new BasicNameValuePair("Benutzername", ConfigurationService
	// .getUserName()));
	// httppost = new HttpPost(url + "/status.php");
	// httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	// response = httpclient.execute(httppost);
	// entity = response.getEntity();
	// is = entity.getContent();
	// String result2 = convertStreamToString(is);
	// is.close();
	// entity.consumeContent();
	// if (result2.contains(" success! "))
	// status = true;
	// } catch (Exception e) {
	// }
	// try {
	// i++;
	// Thread.sleep(20000);
	// } catch (Exception e) {
	//
	// }
	// }
	// }

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
				configurationService.getUserName(context) + ", " + appVersion + ", "
						+ androidVersion + ", " + phoneModel, null).build());
	}
}
