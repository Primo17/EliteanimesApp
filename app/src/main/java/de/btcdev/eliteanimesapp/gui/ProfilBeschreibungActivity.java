package de.btcdev.eliteanimesapp.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;

/**
 * Activity zur Anzeige einer Profilbeschreibung
 */
public class ProfilBeschreibungActivity extends ParentActivity implements
		OnItemClickListener {

	private String aktuellerUser = null;
	private int userID = 0;
	private boolean spoiler = false;
	private ProfilBeschreibungTask task = null;
	private String beschreibung;
	private WebView webview;

	/**
	 * ActionBar wird erzeugt, Netzwerk und Parser werden aus der Konfiguration
	 * geladen. Anschließend wird ein neuer ProfilBeschreibungTask aufgerufen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			aktuellerUser = savedInstanceState.getString(aktuellerUser);
			userID = savedInstanceState.getInt("UserID");
			spoiler = savedInstanceState.getBoolean("Spoiler");
		} else {
			Intent intent = getIntent();
			Bundle intentdata = intent.getExtras();
			aktuellerUser = intentdata.getString("Benutzer");
			userID = intentdata.getInt("UserID");
		}
		setContentView(R.layout.activity_profil_beschreibung);
		ActionBar bar = getSupportActionBar();
		bar.setTitle("Über");
		bar.setSubtitle(aktuellerUser);

		netzwerk = Netzwerk.instance(this);
		eaParser = new EAParser(null);

		task = new ProfilBeschreibungTask();
		task.execute("");
		handleNavigationDrawer(R.id.nav_profil_beschreibung,
				R.id.nav_profil_beschreibung_list, "Über", aktuellerUser);
	}

	/**
	 * Erzeugt das Optionsmenü.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profil_beschreibung, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("Benutzer", aktuellerUser);
		outState.putInt("UserID", userID);
		outState.putBoolean("Spoiler", spoiler);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * ProfilBeschreibungTask wird abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (task != null) {
			task.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.profil_beschreibung_aktualisieren:
			refresh();
			return true;
		case R.id.profil_beschreibung_spoiler:
			spoiler = !spoiler;
			EAParser parser = new EAParser(this);
			webview.loadDataWithBaseURL("http://www.eliteanimes.com",
					parser.showSpoiler(spoiler, beschreibung), "text/html",
					"utf-8", null);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Behandelt einen Klick auf den Navigation Drawer. Dabei wird evtl eine
	 * neue Activity gestartet
	 * 
	 * @param arg0
	 *            Adapter, der das angeklickte Element beinhaltet
	 * @param arg1
	 *            View, das angeklickt wurde
	 * @param arg2
	 *            Position des angeklickten Views in der Liste
	 * @param arg3
	 *            gute Frage!
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.getId() == R.id.nav_profil_beschreibung_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	/**
	 * Die Anzeige wird mit den geladenen Informationen erstellt. Momentan:
	 * Einfaches Workaround, das nicht auf Größe von Bildern achtet.
	 * 
	 * @param profilBeschreibung
	 *            geladene Profilbeschreibung, die angezeigt werden soll
	 */
	@SuppressWarnings("deprecation")
	public void viewZuweisung(String profilBeschreibung) {
		this.beschreibung = profilBeschreibung;
		webview = (WebView) findViewById(R.id.beschreibung_anzeige);
		WebSettings settings = webview.getSettings();
		settings.setDefaultZoom(ZoomDensity.FAR);
		settings.setBuiltInZoomControls(true);
		EAParser parser = new EAParser(this);
		webview.loadDataWithBaseURL("http://www.eliteanimes.com",
				parser.showSpoiler(spoiler, beschreibung), "text/html",
				"utf-8", null);
	}

	public void refresh() {
		task = new ProfilBeschreibungTask();
		task.execute("");
	}

	/**
	 * Klasse für das Herunterladen der Profilbeschreibung. Erbt von AsyncTask.
	 */
	public class ProfilBeschreibungTask extends
			AsyncTask<String, String, String> {

		/**
		 * Lädt im Hintergrund den HTML-Code des Profils und parst diesen nach
		 * der Profilbeschreibung.
		 * 
		 * @param params
		 *            irrelevant
		 * @return geparste Profilbeschreibung als String (enthält HTML)
		 */
		@Override
		protected String doInBackground(String... params) {
			netzwerk = Netzwerk.instance(getApplicationContext());
			eaParser = new EAParser(null);
			try {
				if (isCancelled())
					return null;
				String input = netzwerk.getProfilBeschreibung(aktuellerUser,
						userID);
				if (isCancelled())
					return null;
				new NewsThread(getApplicationContext()).start();
				String output = eaParser.getProfilBeschreibung(input);
				return output;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;

		}

		/**
		 * Ruft die viewZuweisung mit der erhaltenen Profilbeschreibung auf und
		 * schließt den LoadDialog.
		 * 
		 * @param string
		 *            die geparste Profilbeschreibung
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String string) {
			viewZuweisung(string);
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Wird vor der Ausführung aufgerufen. öffnet einen LoadDialog.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			if (defaultprefs.getBoolean("pref_keep_screen_on", true))
				getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			showDialog(load_dialog);
		}

		/**
		 * Wird für das Anzeigen von Fehlermeldungen "missbraucht", da
		 * Fortschritt nicht benötigt wird.
		 * 
		 * @param values
		 *            String-Array, an 0. Stelle steht "Exception", an 1. die
		 *            Fehlermeldung
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			if (values[0].equals("Exception")) {
				if (values[1] != null)
					Toast.makeText(getApplicationContext(), values[1],
							Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * Der Task wird abgebrochen.
		 * 
		 * @param string
		 *            irrelevant
		 */
		@Override
		protected void onCancelled(String string) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

}
