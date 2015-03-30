package de.btcdev.eliteanimesapp.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.data.Profil;
import de.btcdev.eliteanimesapp.data.ProfilCache;
import de.btcdev.eliteanimesapp.json.JsonErrorException;

/**
 * Activity-Klasse zur Darstellung eines fremden Profils
 */
public class FremdesProfilActivity extends ParentActivity implements
		OnItemClickListener {

	private ProfilCache profilcache;
	private ImageView bild;
	private ProfilTask profilTask;
	private String aktuellerUser;
	private int userID;
	private Menu menu;

	/**
	 * Das UI wird erzeugt, Netzwerk, Cache und Parser werden aus der
	 * Konfiguration geladen. Anschließend wird ein neuer ProfilTask gestartet,
	 * falls das Profil noch nicht vollständig ist. Falls doch, wird gleich
	 * viewZuweisung aufgerufen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			aktuellerUser = savedInstanceState.getString("Benutzer");
			userID = savedInstanceState.getInt("UserID");
		} else {
			Intent intent = getIntent();
			Bundle intentdata = intent.getExtras();
			aktuellerUser = intentdata.getString("Benutzer");
			userID = intentdata.getInt("UserID");
		}
		setContentView(R.layout.activity_fremdes_profil);
		bild = (ImageView) findViewById(R.id.profilbild_eigenes);
		bar = getSupportActionBar();
		bar.setTitle("Profil");
		bar.setSubtitle(aktuellerUser);

		netzwerk = Netzwerk.instance(this);
		eaParser = new EAParser(this);
		profilcache = ProfilCache.instance();
		Profil temp = profilcache.contains(aktuellerUser);

		handleNavigationDrawer(R.id.nav_fremdes_profil,
				R.id.nav_fremdes_profil_list, "Profil", aktuellerUser);

		if (temp != null && temp.isComplete())
			viewZuweisung(temp);
		else {
			profilTask = new ProfilTask();
			profilTask.execute("");
		}
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * ProfilTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (profilTask != null) {
			profilTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	/**
	 * Speichert den aktuellen Benutzernamen und die UserID
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("Benutzer", aktuellerUser);
		savedInstanceState.putInt("UserID", userID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fremdes_profil, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.fremdes_profil_aktualisieren:
			refresh();
			return true;
		case R.id.fremdes_profil_add:
			Profil p = profilcache.contains(aktuellerUser);
			if (p.getFriend() == 2) {
				Toast.makeText(this,
						"Eine vorige Anfrage wurde noch nicht bestätigt.",
						Toast.LENGTH_SHORT).show();
			} else {
				new Thread(new Runnable() {
					public void run() {
						try {
							netzwerk = Netzwerk
									.instance(getApplicationContext());
							netzwerk.addFriend("" + userID);
						} catch (Exception e) {

						}
					}
				}).start();
				Toast.makeText(this, "Anfrage wurde gesendet.",
						Toast.LENGTH_SHORT).show();
				p.setFriend(2);
				supportInvalidateOptionsMenu();
			}
			return true;
		case R.id.fremdes_profil_delete:
			Profil profil = profilcache.contains(aktuellerUser);
			new Thread(new Runnable() {
				public void run() {
					try {
						netzwerk = Netzwerk.instance(getApplicationContext());
						netzwerk.deleteFriend("" + userID);
					} catch (Exception e) {

					}
				}
			}).start();
			Toast.makeText(this, "Freund wurde gelöscht.", Toast.LENGTH_SHORT)
					.show();
			profil.setFriend(0);
			supportInvalidateOptionsMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Die Views werden sichtbar gemacht und mit den übergebenen Informationen
	 * gefüllt.
	 * 
	 * @param profilNeu
	 *            Profil, das angezeigt werden soll
	 */
	public void viewZuweisung(Profil profilNeu) {
		supportInvalidateOptionsMenu();
		Profil profil = profilNeu;
		if (profilNeu != null) {
			TextView frag1 = (TextView) findViewById(R.id.profil_benutzername);
			TextView frag2 = (TextView) findViewById(R.id.profil_status);
			TextView frag3 = (TextView) findViewById(R.id.profil_gruppe);
			TextView frag4 = (TextView) findViewById(R.id.profil_geschlecht);
			TextView frag5 = (TextView) findViewById(R.id.profil_alter);
			TextView frag6 = (TextView) findViewById(R.id.profil_single);
			TextView frag7 = (TextView) findViewById(R.id.profil_wohnort);
			TextView frag8 = (TextView) findViewById(R.id.profil_dabei);
			TextView antw1 = (TextView) findViewById(R.id.profil_antw1);
			TextView antw2 = (TextView) findViewById(R.id.profil_antw2);
			TextView antw3 = (TextView) findViewById(R.id.profil_antw3);
			TextView antw4 = (TextView) findViewById(R.id.profil_antw4);
			TextView antw5 = (TextView) findViewById(R.id.profil_antw5);
			TextView antw6 = (TextView) findViewById(R.id.profil_antw6);
			TextView antw7 = (TextView) findViewById(R.id.profil_antw7);
			TextView antw8 = (TextView) findViewById(R.id.profil_antw8);
			frag1.setVisibility(View.VISIBLE);
			frag2.setVisibility(View.VISIBLE);
			frag3.setVisibility(View.VISIBLE);
			frag4.setVisibility(View.VISIBLE);
			frag5.setVisibility(View.VISIBLE);
			frag6.setVisibility(View.VISIBLE);
			frag7.setVisibility(View.VISIBLE);
			frag8.setVisibility(View.VISIBLE);
			antw1.setText(profil.getBenutzername());
			if (profil.getOnline())
				antw2.setText("Online");
			else
				antw2.setText("Offline");
			antw3.setText(profil.getGruppe());
			antw4.setText(profil.getGeschlecht());
			antw5.setText(profil.getAlter());
			antw6.setText(profil.getSingle());
			antw7.setText(profil.getWohnort());
			antw8.setText(profil.getDabei());
			if (menu != null) {
				// waiting
				if (profil.getFriend() == 2) {
					menu.findItem(R.id.fremdes_profil_add).setVisible(true);
					menu.findItem(R.id.fremdes_profil_delete).setVisible(false);
				}
				// friend
				else if (profil.getFriend() == 1) {
					menu.findItem(R.id.fremdes_profil_add).setVisible(false);
					menu.findItem(R.id.fremdes_profil_delete).setVisible(true);
				}
				// no friend
				else if (profil.getFriend() == 0) {
					menu.findItem(R.id.fremdes_profil_add).setVisible(true);
					menu.findItem(R.id.fremdes_profil_delete).setVisible(false);
				}
			}
			bild.setImageBitmap(profil.getProfilbild());
			bild.setAdjustViewBounds(true);
			bild.setMaxHeight(200);
			bild.setMaxWidth(200);

			LinearLayout linkContent = (LinearLayout) findViewById(R.id.profil_link_content);
			linkContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		profilTask = new ProfilTask();
		profilTask.execute("");
	}

	/**
	 * Behandelt einen Klick auf die Liste unter dem Profil und im Navigation
	 * Drawer. Dabei wird die entsprechende Activity per Intent mit den
	 * erforderlichen Informationen gestartet.
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
		if (arg0.getId() == R.id.nav_fremdes_profil_list)
			super.onItemClick(arg0, arg1, arg2, arg3);
	}

	public void onNavigationClick(View v) {
		if (v.getId() == R.id.profil_beschreibung) {
			Intent intent = new Intent(
					this,
					de.btcdev.eliteanimesapp.gui.ProfilBeschreibungActivity.class);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			startActivity(intent);
		} else if (v.getId() == R.id.profil_kommentare) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.KommentarActivity.class);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			startActivity(intent);
		} else if (v.getId() == R.id.profil_freunde) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.FreundeActivity.class);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			startActivity(intent);

		} else if (v.getId() == R.id.profil_animeliste) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.AnimeListActivity.class);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			startActivity(intent);
		}
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class ProfilTask extends AsyncTask<String, String, Profil> {

		/**
		 * Lädt im Hintergrund übers Netzwerk die Profilinformationen und parst
		 * sie noch den gewünschten Daten. Gibt ein Profil mit den erhaltenen
		 * Daten zurück.
		 * 
		 * @param params
		 *            String-Array mit Informationen - hier irrelevant
		 * @return Profil, mit den geladenen Daten
		 * @throws EAException
		 *             bei Verbindungs- und Streamfehlern jeglicher Art
		 */
		@Override
		protected Profil doInBackground(String... params) {
			final String input;
			final Profil profil;
			netzwerk = Netzwerk.instance(getApplicationContext());
			eaParser = new EAParser(null);
			try {
				if (this.isCancelled())
					return null;
				input = netzwerk.getProfil(aktuellerUser, userID);
				if (this.isCancelled())
					return null;
				new NewsThread(getApplicationContext()).start();
				profil = eaParser.getProfilDaten(input);
				if (this.isCancelled())
					return null;
				return profil;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			} catch (JsonErrorException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;

		}

		/**
		 * Die viewZuweisung wird mit den erhaltenen Daten aufgerufen, der
		 * LoadDialog wird geschlossen.
		 * 
		 * @param profil
		 *            das Profil mit den erhaltenen Daten
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Profil profil) {
			viewZuweisung(profil);
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Wird vor Ausführung aufgerufen, öffnet einen LoadDialog.
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
		 * Wird zum Anzeigen von Fehlern missbraucht, da Fortschrittsanzeige
		 * nicht benötigt
		 * 
		 * @param values
		 *            String-Array, an 0. Stelle steht "Exception", an 1. der
		 *            jeweilige Fehlertext
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
		 * Der Task wird abgebrochen
		 * 
		 * @param profil
		 *            irrelevant
		 */
		@Override
		protected void onCancelled(Profil profil) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
