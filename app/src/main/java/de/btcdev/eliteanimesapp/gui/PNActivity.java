package de.btcdev.eliteanimesapp.gui;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.PNAdapter;
import de.btcdev.eliteanimesapp.cache.PNCacheThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.data.PN;
import de.btcdev.eliteanimesapp.json.PNDeserializer;

public class PNActivity extends ParentActivity implements OnItemClickListener {

	private PNTask pnTask;
	private int seitenzahl = 1;
	private ArrayList<PN> pnlist;
	private int chosenPosition;
	private PNAdapter pnAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pn);
		bar = getSupportActionBar();
		bar.setTitle("Meine Nachrichten");
		netzwerk = Netzwerk.instance(this);
		eaParser = new EAParser(null);
		if (savedInstanceState != null) {
			pnlist = savedInstanceState.getParcelableArrayList("PNs");
			seitenzahl = savedInstanceState.getInt("Seitenzahl");
			if (pnlist == null) {
				pnTask = new PNTask();
				pnTask.execute("");
			} else
				viewZuweisung(pnlist);
		} else {
			// Neue Nachrichten vorhanden
			if (Konfiguration.getNewMessageCount() != 0) {
				seitenzahl = 1;
				pnTask = new PNTask();
				pnTask.execute("no_cache");
			} else {
				// Versuche Daten aus Cache zu laden
				seitenzahl = 1;
				pnTask = new PNTask();
				pnTask.execute("");
			}

		}
		handleNavigationDrawer(R.id.nav_pn, R.id.nav_pn_list,
				"Meine Nachrichten", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pn, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender PNTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (pnTask != null) {
			pnTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.pn_aktualisieren:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Speichert die aktuelle Seitenzahl und die PN-Liste
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("PNs", pnlist);
		savedInstanceState.putInt("Seitenzahl", seitenzahl);
	}

	/**
	 * Erzeugt eine ListView-Komponente, über die mit einem Adapter die PNs
	 * zugewiesen werden
	 * 
	 * @param result
	 *            die PNs in einer ArrayList
	 */
	public void viewZuweisung(ArrayList<PN> result) {
		pnlist = result;
		if (pnlist == null || pnlist.isEmpty()) {
			LinearLayout lin = (LinearLayout) findViewById(R.id.pns_layout);
			TextView text = new TextView(this);
			text.setTextSize(16);
			text.setTypeface(text.getTypeface(), Typeface.BOLD);
			text.setText("Keine Privaten Nachrichten vorhanden.");
			text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			lin.addView(text, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		} else {
			ListView list = (ListView) findViewById(R.id.pnliste);
			list.setOnItemClickListener(this);
			pnAdapter = new PNAdapter(this, result);
			list.setAdapter(pnAdapter);
			registerForContextMenu(list);
			list.setSelection(30 * (seitenzahl - 1) - 1);
			if (pnlist.size() % 30 == 0)
				seitenzahl = pnlist.size() / 30;
			else
				seitenzahl = pnlist.size() / 30 + 1;
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		seitenzahl = 1;
		pnTask = new PNTask();
		pnTask.execute("no_cache");
	}

	/**
	 * Behandelt einen Klick auf den Navigation Drawer. Dabei wird die
	 * entsprechende Activity per Intent mit den erforderlichen Informationen
	 * gestartet.
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
		if (arg0.getId() == R.id.nav_pn_list) {
			if (arg2 == navigation_pns)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.pnliste) {
			if (arg2 < pnlist.size()) {
				PN pn = pnlist.get(arg2);
				boolean read = pn.getGelesen();
				pn.setGelesen(true);
				pnAdapter.notifyDataSetChanged();
				Intent intent = new Intent(getApplicationContext(),
						de.btcdev.eliteanimesapp.gui.NeuePNActivity.class);
				intent.putExtra("PN", pn);
				intent.putExtra("read", read);
				new PNCacheThread(PNCacheThread.MODE_SAVE_CACHE, pnlist);
				startActivity(intent);
			} else {
				pnTask = new PNTask();
				pnTask.execute("more");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String temp = item.getTitle().toString();
		if (temp.equals(getResources().getString(R.string.pn_delete))) {
			pnTask = new PNTask();
			pnTask.execute("delete");
		} else if (temp.equals(getResources().getString(
				R.string.pn_profil_besuchen))) {
			PN k = pnlist.get(chosenPosition);
			chosenPosition = -1;
			String name = k.getBenutzername();
			int userid = k.getUserid();
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
			intent.putExtra("Benutzer", name);
			intent.putExtra("UserID", userid);
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.pnliste) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			chosenPosition = info.position;
			if (chosenPosition < pnlist.size()) {
				menu.add(getResources().getString(R.string.pn_delete));
				menu.add(getResources().getString(R.string.pn_profil_besuchen));
				menu.setHeaderTitle("PN von "
						+ pnlist.get(chosenPosition).getBenutzername());
			}
		}

	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class PNTask extends AsyncTask<String, String, ArrayList<PN>> {

		private boolean delete, more, cache;

		/**
		 * Lädt im Hintergrund übers Netzwerk die PNs und parst sie noch den
		 * gewünschten Daten. Gibt eine ArrayList mit den PNdaten zurück
		 */
		@Override
		protected ArrayList<PN> doInBackground(String... params) {
			String input;
			netzwerk = Netzwerk.instance(getApplicationContext());
			eaParser = new EAParser(null);
			new NewsThread(getApplicationContext()).start();
			try {
				if (params[0].equals("no_cache")) {
					if (isCancelled())
						return null;
					input = netzwerk.getPNSite(1);
					if (isCancelled())
						return null;
					pnlist = eaParser.getPNs(input);
					return pnlist;
				} else if (params[0].equals("delete")) {
					PN k = pnlist.get(chosenPosition);
					chosenPosition = -1;
					netzwerk.deletePN(Integer.toString(k.getId()));
					pnlist.remove(k);
					delete = true;
					return pnlist;
				} else if (params[0].equals("more")) {
					more = true;

					if (isCancelled())
						return null;
					input = netzwerk.getPNSite(seitenzahl + 1);
					if (isCancelled())
						return null;
					pnlist = eaParser.getMorePNs(input, pnlist);
					seitenzahl++;
					return pnlist;
				} else {
					if (loadCache()) {
						cache = true;
						return pnlist;
					} else {
						if (isCancelled())
							return null;
						input = netzwerk.getPNSite(1);
						if (isCancelled())
							return null;
						pnlist = eaParser.getPNs(input);
						return pnlist;
					}
				}
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Die ViewZuweisung wird mit den erhaltenen Ergebnissen aufgerufen, der
		 * LoadDialog wird geschlossen.
		 * 
		 * @param result
		 *            erhaltene PNs in einer ArrayList
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<PN> result) {
			if (result != null && !result.isEmpty()) {
				if (!more && !cache && seitenzahl == 1) {
					new PNCacheThread(PNCacheThread.MODE_SAVE_CACHE, result);
				}
			}
			if (more || delete)
				pnAdapter.notifyDataSetChanged();
			else
				viewZuweisung(result);
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
		 * @param result
		 *            irrelevant
		 */
		@Override
		protected void onCancelled(ArrayList<PN> result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Versucht den PN-Cache zu laden und gibt den Erfolg der Operation
		 * zurück.
		 * 
		 * @return Ob Cache-Operation erfolgreich war
		 */
		public boolean loadCache() {
			// Gibt es überhaupt Speicherstände eines Benutzers?
			SharedPreferences prefs = getSharedPreferences("cache",
					Context.MODE_PRIVATE);
			if (prefs.contains("lastUser")) {
				// Gibt es einen PNCache?
				if (prefs.contains("PNCache")) {
					// ist der Cache vom aktuellen Benutzer?
					if (prefs.getString("lastUser", "").equals(
							Konfiguration
									.getBenutzername(getApplicationContext()))) {
						// lese Cache aus und speicher in Konfiguration
						String jsonCache = prefs.getString("PNCache", "");
						if (!jsonCache.equals("")) {
							// Konvertiere JSON zurück zu ArrayList aus PNs
							// und setze Cache in Konfiguration
							try {
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(PN.class,
												new PNDeserializer()).create();

								Type collectionType = new TypeToken<ArrayList<PN>>() {
								}.getType();
								ArrayList<PN> jsonList = gson.fromJson(
										jsonCache, collectionType);
								pnlist = jsonList;
								return true;
							} catch (JsonParseException e) {
								// lösche vorhanden Cache
								SharedPreferences.Editor editor = prefs.edit();
								editor.remove("PNCache");
								editor.apply();
								return false;
							}
						}
					} else {
						// lösche vorhanden Cache
						SharedPreferences.Editor editor = prefs.edit();
						editor.remove("PNCache");
						editor.apply();
						return false;
					}
				}
			}
			return false;
		}
	}

}
