package de.btcdev.eliteanimesapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.exceptions.EAException;
import de.btcdev.eliteanimesapp.data.models.Board;
import de.btcdev.eliteanimesapp.data.models.BoardThread;
import de.btcdev.eliteanimesapp.data.models.Subboard;
import de.btcdev.eliteanimesapp.data.services.BoardService;
import de.btcdev.eliteanimesapp.ui.adapter.BoardThreadAdapter;

public class ThreadActivity extends ParentActivity implements
		OnItemSelectedListener {

	@Inject
	BoardService boardService;

	private ListView listView;
	private BoardThreadAdapter boardThreadAdapter;
	private Board board;
	private ThreadTask threadTask;
	private int pageCount;
	private int page = 1;
	private ArrayList<BoardThread> boardThreads;
	ArrayAdapter<String> pageAdapter;
	String[] pages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_thread);
		listView = (ListView) findViewById(R.id.threads_list);
		if (savedInstanceState != null) {
			board = savedInstanceState.getParcelable("board");
			boardThreads = savedInstanceState
					.getParcelableArrayList("boardThreads");
			page = savedInstanceState.getInt("page");
			pageCount = savedInstanceState.getInt("pageCount");
			pages = savedInstanceState.getStringArray("pages");
			fillViews();
		} else {
			Bundle bundle = getIntent().getExtras();
			board = bundle.getParcelable("board");
			threadTask = new ThreadTask();
			threadTask.execute("");
		}
		actionBar = getSupportActionBar();
		actionBar.setTitle(board.getName());
		handleNavigationDrawer(R.id.nav_threads, R.id.nav_threads_list,
				board.getName(), null);
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
		if (threadTask != null) {
			threadTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("board", board);
		outState.putParcelableArrayList("boardThreads", boardThreads);
		outState.putInt("page", page);
		outState.putInt("pageCount", pageCount);
		outState.putStringArray("pages", pages);
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.threads, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.threads_aktualisieren:
			refresh();
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
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
		if (arg0.getId() == R.id.nav_threads_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.threads_list) {
			Object temp = boardThreadAdapter.getItem(arg2);
			if (temp == null || board == null)
				return;
			if (temp instanceof Subboard) {
				Subboard sub = (Subboard) temp;
				Intent intent = new Intent(this,
						ThreadActivity.class);
				Board f = new Board();
				f.setId(sub.getId());
				f.setName(sub.getName());
				f.setBoardCategoryId(board.getId());
				f.setBoardCategoryName(board.getName());
				intent.putExtra("board", f);
				sub = null;
				startActivity(intent);
			} else if (temp instanceof BoardThread) {
				BoardThread ft = (BoardThread) temp;
				Intent intent = new Intent(this,
						PostActivity.class);
				intent.putExtra("thread", ft);
				intent.putExtra("page", ft.getPages());
				startActivity(intent);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.threads_spinner) {
			if (page != position + 1) {
				page = position + 1;
				threadTask = new ThreadTask();
				threadTask.execute("");
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}

	public void fillViews() {
		if (pages == null)
			pages = new String[pageCount];
		if (pageAdapter == null) {
			LinearLayout root = (LinearLayout) findViewById(R.id.threads_root);
			LayoutInflater inflater = getLayoutInflater();
			View pagesView = inflater
					.inflate(R.layout.forum_pages, root, false);
			pageAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, pages);
			Spinner pageSpinner = (Spinner) pagesView
					.findViewById(R.id.threads_spinner);
			for (int i = 0; i < pageCount; i++)
				pages[i] = Integer.toString(i + 1);

			pageSpinner.setSelection(page - 1);
			pageSpinner.setAdapter(pageAdapter);
			pageAdapter
					.setDropDownViewResource(R.layout.dialog_animelist_rate_dropdown);
			pageSpinner.setOnItemSelectedListener(this);
			root.addView(pagesView, 0);
		}
		listView = (ListView) findViewById(R.id.threads_list);
		if (page == 1)
			boardThreadAdapter = new BoardThreadAdapter(this, boardThreads,
					board.getSubboards());
		else
			boardThreadAdapter = new BoardThreadAdapter(this, boardThreads, null);
		listView.setAdapter(boardThreadAdapter);
		listView.setOnItemClickListener(this);
	}

	public void refresh() {
		LinearLayout root = (LinearLayout) findViewById(R.id.threads_root);
		View v = root.getChildAt(0);
		if (v != null && v.getId() == R.id.forum_pages_root)
			root.removeViewAt(0);
		boardThreadAdapter = null;
		pages = null;
		pageAdapter = null;
		threadTask = new ThreadTask();
		threadTask.execute("");
	}

	public class ThreadTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String input;
			try {
				if (isCancelled())
					return null;
				input = boardService.getThreads(board.getId(), page);
				if (isCancelled())
					return null;
				pageCount = boardService.getBoardThreadPageCount(input);
				if (isCancelled())
					return null;
				boardThreads = boardService.getBoardThreads(input);
				return "success";
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Öffnet einen Ladedialog.
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
			if (StringUtils.isNotEmpty(result))
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
