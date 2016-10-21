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

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.services.ProfileService;

/**
 * Activity zur Anzeige einer Profilbeschreibung
 */
public class ProfileDescriptionActivity extends ParentActivity implements
		OnItemClickListener {

	@Inject
	ProfileService profileService;

	private String currentUser = null;
	private int userId = 0;
	private boolean showSpoiler;
	private ProfileDescriptionTask profileDescriptionTask = null;
	private String description;
	private WebView webView;

	/**
	 * ActionBar wird erzeugt, NetworkService und Parser werden aus der ConfigurationService
	 * geladen. Anschließend wird ein neuer ProfileDescriptionTask aufgerufen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			currentUser = savedInstanceState.getString(currentUser);
			userId = savedInstanceState.getInt("UserID");
			showSpoiler = savedInstanceState.getBoolean("Spoiler");
		} else {
			Intent intent = getIntent();
			Bundle intentdata = intent.getExtras();
			currentUser = intentdata.getString("User");
			userId = intentdata.getInt("UserID");
		}
		setContentView(R.layout.activity_profil_beschreibung);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Über");
		actionBar.setSubtitle(currentUser);

		eaParser = new EAParser(null);

		profileDescriptionTask = new ProfileDescriptionTask();
		profileDescriptionTask.execute("");
		handleNavigationDrawer(R.id.nav_profil_beschreibung,
				R.id.nav_profil_beschreibung_list, "Über", currentUser);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	/**
	 * Erzeugt das Optionsmenü.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.profil_beschreibung, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("User", currentUser);
		outState.putInt("UserID", userId);
		outState.putBoolean("Spoiler", showSpoiler);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * ProfileDescriptionTask wird abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (profileDescriptionTask != null) {
			profileDescriptionTask.cancel(true);
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
			showSpoiler = !showSpoiler;
			webView.loadDataWithBaseURL("http://www.eliteanimes.com",
					eaParser.showSpoiler(showSpoiler, description), "text/html",
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
	 * @param profileDescription
	 *            geladene Profilbeschreibung, die angezeigt werden soll
	 */
	@SuppressWarnings("deprecation")
	public void fillViews(String profileDescription) {
		this.description = profileDescription;
		webView = (WebView) findViewById(R.id.beschreibung_anzeige);
		WebSettings settings = webView.getSettings();
		settings.setDefaultZoom(ZoomDensity.FAR);
		settings.setBuiltInZoomControls(true);
		webView.loadDataWithBaseURL("http://www.eliteanimes.com",
				eaParser.showSpoiler(showSpoiler, description), "text/html",
				"utf-8", null);
	}

	public void refresh() {
		profileDescriptionTask = new ProfileDescriptionTask();
		profileDescriptionTask.execute("");
	}

	/**
	 * Klasse für das Herunterladen der Profilbeschreibung. Erbt von AsyncTask.
	 */
	public class ProfileDescriptionTask extends
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
			try {
				if (isCancelled())
					return null;
                getNotifications();
                return profileService.getProfileDescription(userId);
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;

		}

		/**
		 * Ruft die fillViews mit der erhaltenen Profilbeschreibung auf und
		 * schließt den LoadDialog.
		 * 
		 * @param string
		 *            die geparste Profilbeschreibung
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String string) {
			fillViews(string);
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
