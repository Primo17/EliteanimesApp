package de.btcdev.eliteanimesapp.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.Profil;
import de.btcdev.eliteanimesapp.data.ProfilCache;

/**
 * Activity, die für den Login des Benutzers zuständig ist.
 */
public class LoginActivity extends ParentActivity implements OnClickListener,
		OnItemClickListener {

	private Button loginButton;
	private EditText loginBenutzername;
	private EditText loginPasswort;
	private CheckBox loginCheck;
	private SharedPreferences prefs;
	private LoginTask loginTask;
	private ProfilCache profilcache;

	/**
	 * Erzeugt die Activity. Die ActionBar und die restliche grafische
	 * Darstellung wird erzeugt. Benutzername und Passwort werden, wenn
	 * gespeichert, abgerufen. Netzwerk und ProfilCache werden beim Erststart
	 * eingerichtet.
	 * 
	 * @param savedInstanceState
	 *            keine Bedeutung für diese Implementierung
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(de.btcdev.eliteanimesapp.R.layout.activity_login);
		loginBenutzername = (EditText) findViewById(de.btcdev.eliteanimesapp.R.id.login_benutzername);
		loginPasswort = (EditText) findViewById(de.btcdev.eliteanimesapp.R.id.login_passwort);
		loginButton = (Button) findViewById(de.btcdev.eliteanimesapp.R.id.login_button);
		loginCheck = (CheckBox) findViewById(de.btcdev.eliteanimesapp.R.id.login_check);
		loginButton.setOnClickListener(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		Konfiguration.setContext(getApplicationContext());

		ActionBar bar = getSupportActionBar();
		bar.setTitle("Login");

		prefs = getPreferences(Context.MODE_PRIVATE);
		if (prefs.contains("Benutzername"))
			loginBenutzername.setText(prefs.getString("Benutzername", null));
		if (prefs.contains("Passwort"))
			loginPasswort.setText(prefs.getString("Passwort", null));
		if (prefs.contains("Checked"))
			loginCheck.setChecked(prefs.getBoolean("Checked", false));
		netzwerk = Netzwerk.instance(this);
		// überprüfe, ob Cookies vorhanden und geh zum Profil, wenn dies der
		// Fall ist
		if (netzwerk.hasCookies()
				&& Konfiguration.getUserID(getApplicationContext()) != 0
				&& Konfiguration.getBenutzername(getApplicationContext()) != null) {
			profilcache = ProfilCache.instance();
			if (profilcache.getEigenesProfil() == null) {
				Profil temp = new Profil(
						Konfiguration.getBenutzername(getApplicationContext()));
				temp.setUserID(Konfiguration.getUserID(getApplicationContext()));
				profilcache.setEigenesProfil(temp);
			} else {
				Profil p = profilcache.getEigenesProfil();
				Profil temp = new Profil(
						Konfiguration.getBenutzername(getApplicationContext()));
				if (!p.equals(temp)) {
					profilcache.deleteProfil(temp.getBenutzername());
					profilcache.setEigenesProfil(temp);
					profilcache.contains(p.getBenutzername());
				}
			}
			Intent intent = new Intent(getApplicationContext(),
					de.btcdev.eliteanimesapp.gui.ProfilActivity.class);
			startActivity(intent);
		} else {
			// überprüfe in Einstellungen Auto-Login
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			boolean logout;
			if (bundle != null)
				logout = bundle.getBoolean("Logout", false);
			else
				logout = false;
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (defaultprefs.getBoolean("pref_auto_login", false) && !logout) {
				String benutzername = prefs.getString("Benutzername", "");
				String passwort = prefs.getString("Passwort", "");
				if (!benutzername.equals("") && !passwort.equals("")) {
					Konfiguration.setBenutzername(benutzername);
					Konfiguration.setPasswort(passwort);
					loginTask = new LoginTask();
					loginTask.execute("");
				}
			}
		}
		handleNavigationDrawer(R.id.nav_login, R.id.nav_login_list, "Login",
				null);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Dabei wird ein
	 * laufender LoginTask abgebrochen, um Fehler zu vermeiden.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (loginTask != null) {
			loginTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			EasyTracker.getInstance(this).activityStart(this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(de.btcdev.eliteanimesapp.R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Die Eingaben für Benutzername und Passwort werden in der Konfiguration
	 * gespeichert, falls gewünscht auch im Dateisystem. Anschließend wird ein
	 * neuer Login-Task aufgerufen.
	 * 
	 * @param arg0
	 *            View, der angeklickt wurde
	 */
	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == de.btcdev.eliteanimesapp.R.id.login_button) {
			String benutzername = loginBenutzername.getText().toString();
			String passwort = loginPasswort.getText().toString();
			Konfiguration.setBenutzername(benutzername);
			Konfiguration.setPasswort(passwort);
			boolean checked = loginCheck.isChecked();
			SharedPreferences.Editor meinEditor = prefs.edit();
			SharedPreferences defaultprefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean username = defaultprefs.getBoolean("pref_save_password",
					true);
			if (checked) {
				if (username) {
					meinEditor.putString("Benutzername", Konfiguration
							.getBenutzername(getApplicationContext()));
					meinEditor.putString("Passwort",
							Konfiguration.getPasswort());
				} else {
					meinEditor.putString("Benutzername", Konfiguration
							.getBenutzername(getApplicationContext()));
					if (prefs.contains("Passwort"))
						meinEditor.remove("Passwort");
				}
			} else {
				if (prefs.contains("Benutzername"))
					meinEditor.remove("Benutzername");
				if (prefs.contains("Passwort"))
					meinEditor.remove("Passwort");
			}
			meinEditor.putBoolean("Checked", checked);
			meinEditor.apply();
			loginTask = new LoginTask();
			loginTask.execute("");
		}
	}

	/**
	 * Ereignisbehandlung des Navigation Drawers
	 * 
	 * @param arg0
	 *            betroffener Adapter
	 * @param arg1
	 *            betroffenes View-Element
	 * @param arg2
	 *            Position des Elements im Adapter
	 * @param arg3
	 *            irgendwas hier unwichtiges
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case navigation_info:
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.InfoActivity.class);
			mDrawerLayout.closeDrawer(Gravity.LEFT);
			startActivity(intent);
			break;
		case navigation_settings:
			intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.SettingsActivity.class);
			mDrawerLayout.closeDrawer(Gravity.LEFT);
			startActivity(intent);
			break;
		case navigation_logout:
			try {
				netzwerk = Netzwerk.instance(this);
				netzwerk.logout();
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} catch (EAException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			break;
		default:
			Toast.makeText(this, "Login muss zuerst durchgeführt werden!",
					Toast.LENGTH_SHORT).show();
			break;
		}
	}

	/**
	 * Klasse für den Login-Prozess, die von AsyncTask erbt.
	 */
	public class LoginTask extends AsyncTask<String, String, String> {

		/**
		 * Ruft im Hintergrund die Login-Methode des verwendeten Netzwerks auf.
		 * Falls ein Fehler auftritt, wird dieser als Toast ausgegeben.
		 * 
		 * @param arg0
		 *            gibt an, was getan werden soll - für diesen Task nutzlos
		 */
		@Override
		protected String doInBackground(String... arg0) {
			try {
				if (this.isCancelled())
					return null;
				netzwerk = Netzwerk.instance(getApplicationContext());
				String input = netzwerk.login();
				if (input != null)
					new EAParser(getApplicationContext())
							.parseLoginResult(input);
				if (this.isCancelled())
					return null;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Öffnet einen Load-Dialog.
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
		 * Falls der Benutzer schon eingeloggt ist, wird überprüft, ob das
		 * eigene Profil schon im Cache gespeichert ist. Wenn nein, wird das neu
		 * erzeugt und im Cache als eigenes Profil gesetzt. Danach wird
		 * unabhängig davon versucht, die ProfilActivity zu starten und der
		 * Dialog geschlossen.
		 * 
		 * @param loginString
		 *            wird für diese Implementierung nicht benötigt
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String loginString) {
			netzwerk = Netzwerk.instance(getApplicationContext());
			if (netzwerk.isLoggedIn()) {
				profilcache = ProfilCache.instance();
				if (profilcache.getEigenesProfil() == null) {
					Profil temp = new Profil(
							Konfiguration
									.getBenutzername(getApplicationContext()));
					temp.setUserID(Konfiguration
							.getUserID(getApplicationContext()));
					profilcache.setEigenesProfil(temp);
				} else {
					Profil p = profilcache.getEigenesProfil();
					Profil temp = new Profil(
							Konfiguration
									.getBenutzername(getApplicationContext()));
					if (!p.equals(temp)) {
						profilcache.deleteProfil(temp.getBenutzername());
						profilcache.setEigenesProfil(temp);
						profilcache.contains(p.getBenutzername());
					}
				}
				Intent intent = new Intent(getApplicationContext(),
						de.btcdev.eliteanimesapp.gui.ProfilActivity.class);
				startActivity(intent);
				try {
					dismissDialog(load_dialog);
				} catch (IllegalArgumentException e) {

				}
			} else {
				try {
					dismissDialog(load_dialog);
				} catch (IllegalArgumentException e) {

				}
				Toast.makeText(
						getApplicationContext(),
						"Login nicht erfolgreich. Bitte überprüfe deine Eingaben!",
						Toast.LENGTH_LONG).show();
			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Wird für die Fehlerausgabe "missbraucht", da keine
		 * Fortschrittsanzeige benötigt wird.
		 * 
		 * @param values
		 *            String-Array, in dem an 0. Stelle "Exception" und an 1.
		 *            Stelle die Fehlernachricht gespeichert sein soll
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
		 * Der LoginTask wird unterbrochen.
		 * 
		 * @param loginString
		 *            für diese Implementierung unerheblich
		 */
		@Override
		protected void onCancelled(String loginString) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

	}
}
