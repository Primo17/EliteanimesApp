package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

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
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.FriendAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Friend;
import de.btcdev.eliteanimesapp.data.Configuration;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.NewsThread;

/**
 * Activity-Klasse zur Anzeige einer Freundesliste
 */
public class FriendActivity extends ParentActivity implements
		OnItemClickListener {

	private String aktuellerUser = null;
	private int userID = 0;
	private ListView freundesliste;
	private FriendAdapter adapter;
	private FreundeTask freundeTask;
	private ArrayList<Friend> freundeliste;
	private int chosenPosition;

	/**
	 * Die Activity wird erzeugt, falls sie schon vorher erzeugt wurde, werden
	 * die Daten aus dem Bundle geladen, ansonsten wird ein neuer FreundeTask
	 * aufgerufen
	 * 
	 * @param savedInstanceState
	 *            Bundle für schon vorhandene Daten
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_freunde);
		bar = getSupportActionBar();
		networkService = NetworkService.instance(this);
		eaParser = new EAParser(this);

		if (savedInstanceState != null) {
			freundeliste = savedInstanceState
					.getParcelableArrayList("Freundeliste");
			aktuellerUser = savedInstanceState.getString("User");
			userID = savedInstanceState.getInt("UserID");
			chosenPosition = savedInstanceState.getInt("chosenPosition");
			bar.setSubtitle(aktuellerUser);
			if (freundeliste != null) {
				viewZuweisung(freundeliste);
			} else {
				freundeTask = new FreundeTask();
				freundeTask.execute("");
			}
		} else {
			Intent intent = getIntent();
			Bundle intentdata = intent.getExtras();
			aktuellerUser = intentdata.getString("User");
			userID = intentdata.getInt("UserID");
			bar.setSubtitle(aktuellerUser);
			freundeTask = new FreundeTask();
			freundeTask.execute("");
		}
		handleNavigationDrawer(R.id.nav_freunde, R.id.nav_freunde_list,
				"Freunde", aktuellerUser);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * FreundeTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (freundeTask != null) {
			freundeTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	/**
	 * Speichert die Daten der Freundesliste und des aktuellen Benutzers
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("Freundeliste", freundeliste);
		savedInstanceState.putString("User", aktuellerUser);
		savedInstanceState.putInt("UserID", userID);
		savedInstanceState.putInt("chosenPosition", chosenPosition);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.freunde_aktualisieren:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Erzeugt und füllt die benötigten Views zur Anzeige der Freundesliste.
	 * 
	 * @param result
	 *            Ein Array, bestehend aus den Daten der Freundesliste und den
	 *            Links der Freundesliste, jeweils in einer ArrayList
	 */
	public void viewZuweisung(ArrayList<Friend> result) {
		freundeliste = result;
		if (freundeliste == null || freundeliste.isEmpty()) {
			LinearLayout lin = (LinearLayout) findViewById(R.id.freunde_layout);
			TextView text = new TextView(this);
			text.setTextSize(16);
			text.setTypeface(text.getTypeface(), Typeface.BOLD);
			text.setText("Du hast keine Freunde. :(");
			text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			lin.addView(text, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		} else {
			freundesliste = (ListView) findViewById(R.id.freundesliste);
			adapter = new FriendAdapter(this, freundeliste);
			freundesliste.setAdapter(adapter);
			freundesliste.setOnItemClickListener(this);
			registerForContextMenu(freundesliste);
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		freundeTask = new FreundeTask();
		freundeTask.execute();
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.freunde, menu);
		return true;
	}

	/**
	 * Behandelt einen Klick auf die Freundesliste. Dabei wird die
	 * ProfileActivity des angeklickten Users per Intent mit den erforderlichen
	 * Informationen gestartet.
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
		if (arg0.getId() == R.id.freundesliste) {
			Friend friend = freundeliste.get(arg2);
			int id = friend.getId();
			Intent intent;
			if (friend.getName().equals(
					Configuration.getUserName(getApplicationContext())))
				intent = new Intent(this,
						ProfileActivity.class);
			else {
				intent = new Intent(
						this,
						UserProfileActivity.class);
				intent.putExtra("User", friend.getName());
				intent.putExtra("UserID", id);
			}
			startActivity(intent);
		} else if (arg0.getId() == R.id.nav_freunde_list) {
			if (arg2 == navigation_freunde) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String temp = item.getTitle().toString();
		if (temp.equals(getResources().getString(R.string.delete_friend))) {
			// löschen
			Friend friend = freundeliste.get(chosenPosition);
			final String id = "" + friend.getId();
			new Thread(new Runnable() {
				public void run() {
					try {
						networkService = NetworkService.instance(getApplicationContext());
						networkService.deleteFriend(id);
					} catch (Exception e) {

					}
				}
			}).start();
			Toast.makeText(this, "Friend wurde gelöscht.", Toast.LENGTH_SHORT)
					.show();
			freundeliste.remove(chosenPosition);
			chosenPosition = -1;
			adapter.notifyDataSetChanged();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.freundesliste) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			chosenPosition = info.position;
			if (chosenPosition < freundeliste.size()) {
				menu.add(getResources().getString(R.string.delete_friend));
				menu.setHeaderTitle(freundeliste.get(chosenPosition).getName());
			}
		}

	}

	/**
	 * Klasse für das Herunterladen der Freundesliste. Erbt von AsyncTask.
	 */
	public class FreundeTask extends
			AsyncTask<String, String, ArrayList<Friend>> {

		/**
		 * Lädt im Hintergrund den HTML-Code der Freundesliste und parst diesen
		 * nach gewünschten Informationen.
		 * 
		 * @param arg0
		 *            irrelevant
		 * @return geparste Freundesliste in einer ArrayList
		 */
		@Override
		protected ArrayList<Friend> doInBackground(String... arg0) {
			eaParser = new EAParser(null);
			networkService = NetworkService.instance(getApplicationContext());
			try {
				if (isCancelled())
					return null;
				final String input = networkService.getFriendList(aktuellerUser,
						userID);
				if (isCancelled())
					return null;
				new NewsThread(getApplicationContext()).start();
				ArrayList<Friend> ergebnis = eaParser.getFriendList(input);
				return ergebnis;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Der Task wird abgebrochen
		 * 
		 * @param result
		 *            irrelevant
		 */
		@Override
		protected void onCancelled(ArrayList<Friend> result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Die viewZuweisung wird mit den erhaltenen Daten aufgerufen, der
		 * LoadDialog wird geschlossen.
		 * 
		 * @param result
		 *            die ArrayList mit den erhaltenen Daten
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<Friend> result) {
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

	}

}
