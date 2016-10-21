package de.btcdev.eliteanimesapp.gui;

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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.BoardAdapter;
import de.btcdev.eliteanimesapp.data.Board;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.Statistics;
import de.btcdev.eliteanimesapp.services.BoardService;

public class BoardActivity extends ParentActivity implements
		OnItemClickListener, OnChildClickListener {

	@Inject
	BoardService boardService;

	private ExpandableListView listView;
	private BoardAdapter boardAdapter;
	private TreeMap<Integer, ArrayList<Board>> boardMap;
	private Statistics statistics;
	private BoardTask boardTask;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_foren);
		listView = (ExpandableListView) findViewById(R.id.foren_list);
		if (savedInstanceState != null) {
			Serializable temp = (TreeMap<Integer, ArrayList<Board>>) savedInstanceState
					.getSerializable("boardMap");
			if (temp instanceof TreeMap) {
				boardMap = (TreeMap<Integer, ArrayList<Board>>) temp;
				fillViews();
			}
		} else {
			boardTask = new BoardTask();
			boardTask.execute("");
		}
		handleNavigationDrawer(R.id.nav_foren, R.id.nav_foren_list, "Board",
				null);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender BoardTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (boardTask != null) {
			boardTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
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
		savedInstanceState.putSerializable("boardMap", boardMap);
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
			if (arg2 == NAVIGATION_BOARD)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	public void fillViews() {
		boardTask = null;
		if (boardMap != null) {
			listView = (ExpandableListView) findViewById(R.id.foren_list);
			boardAdapter = new BoardAdapter(this, boardMap, statistics);
			listView.setAdapter(boardAdapter);
			listView.expandGroup(0);
			listView.setOnChildClickListener(this);
		}
	}

	public void refresh() {
		boardTask = new BoardTask();
		boardTask.execute("");
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if (groupPosition < boardAdapter.getGroupCount()
				&& childPosition < boardAdapter.getChildrenCount(groupPosition)) {
			Board board = (Board) boardAdapter.getChild(groupPosition,
					childPosition);
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.ThreadActivity.class);
			intent.putExtra("board", board);
			startActivity(intent);
			return true;
		} else
			return false;
	}

	public class BoardTask extends AsyncTask<String, String, String> {

		private boolean error;

		@Override
		protected String doInBackground(String... params) {
			String input;
			try {
				if (isCancelled())
					return "";
				boardMap = boardService.getBoards();
				if (isCancelled())
					return "";
				statistics = boardService.getBoardStatistics();
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
		 * Der Lade-Dialog wird geschlossen und die fillViews aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (!error && StringUtils.isNotEmpty(result))
				fillViews();
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
