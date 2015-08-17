package de.btcdev.eliteanimesapp.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.ForenAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Forum;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.Statistik;

public class ForenActivity extends ParentActivity implements
		OnItemClickListener, OnChildClickListener {

	private ExpandableListView listView;
	private ForenAdapter forenAdapter;
	private TreeMap<Integer, ArrayList<Forum>> forenMap;
	private Statistik stat;
	private ForenTask forenTask;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foren);
		listView = (ExpandableListView) findViewById(R.id.foren_list);
		if (savedInstanceState != null) {
			Serializable temp = (TreeMap<Integer, ArrayList<Forum>>) savedInstanceState
					.getSerializable("forenMap");
			if (temp instanceof TreeMap) {
				forenMap = (TreeMap<Integer, ArrayList<Forum>>) temp;
				viewZuweisung();
			} else
				temp = null;
		} else {
			forenTask = new ForenTask();
			forenTask.execute("");
		}
		handleNavigationDrawer(R.id.nav_foren, R.id.nav_foren_list, "Forum",
				null);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender ForenTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (forenTask != null) {
			forenTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.foren, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.foren_aktualisieren:
			refresh();
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Speichert die aktuellen Daten
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putSerializable("forenMap", forenMap);
	}

	/**
	 * Behandelt einen Klick im Navigation Drawer. Dabei wird die entsprechende
	 * Activity per Intent mit den erforderlichen Informationen gestartet.
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
		if (arg0.getId() == R.id.nav_foren_list) {
			if (arg2 == navigation_forum)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	public void viewZuweisung() {
		forenTask = null;
		if (forenMap != null) {
			listView = (ExpandableListView) findViewById(R.id.foren_list);
			forenAdapter = new ForenAdapter(this, forenMap, stat);
			listView.setAdapter(forenAdapter);
			listView.expandGroup(0);
			listView.setOnChildClickListener(this);
		}
	}

	public void refresh() {
		forenTask = new ForenTask();
		forenTask.execute("");
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if (groupPosition < forenAdapter.getGroupCount()
				&& childPosition < forenAdapter.getChildrenCount(groupPosition)) {
			Forum forum = (Forum) forenAdapter.getChild(groupPosition,
					childPosition);
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.ThreadActivity.class);
			intent.putExtra("forum", forum);
			startActivity(intent);
			return true;
		} else
			return false;
	}

	public class ForenTask extends AsyncTask<String, String, String> {

		private boolean error;

		@Override
		protected String doInBackground(String... params) {
			String input = null;
			eaParser = new EAParser(getApplicationContext());
			netzwerk = Netzwerk.instance(getApplicationContext());
			try {
				if (isCancelled())
					return "";
				input = netzwerk.getForen();
				if (isCancelled())
					return "";
				forenMap = eaParser.getForen(input);
				if (isCancelled())
					return "";
				input = netzwerk.getForenStatistik();
				if (isCancelled())
					return "";
				stat = eaParser.getForenStatistik(input);
				return "success";
			} catch (EAException e) {
				error = true;
				publishProgress("Exception", e.getMessage());
			}
			error = true;
			return "";
		}

		/**
		 * Öffnet einen Lade-Dialog.
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
		 * Der Lade-Dialog wird geschlossen und die viewZuweisung aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (!error && !isNullOrEmpty(result))
				viewZuweisung();
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
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

		@Override
		protected void onCancelled(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onCancelled();
		}

	}
}
