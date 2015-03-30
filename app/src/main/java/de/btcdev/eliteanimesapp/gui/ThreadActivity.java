package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

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
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.ForumThreadAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Forum;
import de.btcdev.eliteanimesapp.data.ForumThread;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.Subforum;

public class ThreadActivity extends ParentActivity implements
		OnItemSelectedListener {

	private ListView listView;
	private ForumThreadAdapter threadAdapter;
	private Forum forum;
	private ThreadTask threadTask;
	private int pageCount;
	private int seite = 1;
	private ArrayList<ForumThread> threadList;
	ArrayAdapter<String> pageAdapter;
	String[] pages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thread);
		listView = (ListView) findViewById(R.id.threads_list);
		if (savedInstanceState != null) {
			forum = savedInstanceState.getParcelable("forum");
			threadList = savedInstanceState
					.getParcelableArrayList("threadList");
			seite = savedInstanceState.getInt("seite");
			pageCount = savedInstanceState.getInt("pageCount");
			pages = savedInstanceState.getStringArray("pages");
			viewZuweisung();
		} else {
			Bundle bundle = getIntent().getExtras();
			forum = bundle.getParcelable("forum");
			threadTask = new ThreadTask();
			threadTask.execute("");
		}
		bar = getSupportActionBar();
		bar.setTitle(forum.getName());
		handleNavigationDrawer(R.id.nav_threads, R.id.nav_threads_list,
				forum.getName(), null);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender ForenTask
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
		outState.putParcelable("forum", forum);
		outState.putParcelableArrayList("threadList", threadList);
		outState.putInt("seite", seite);
		outState.putInt("pageCount", pageCount);
		outState.putStringArray("pages", pages);
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
			Object temp = threadAdapter.getItem(arg2);
			if (temp == null || forum == null)
				return;
			if (temp instanceof Subforum) {
				Subforum sub = (Subforum) temp;
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.ThreadActivity.class);
				Forum f = new Forum();
				f.setId(sub.getId());
				f.setName(sub.getName());
				f.setOberforumId(forum.getId());
				f.setOberforumName(forum.getName());
				intent.putExtra("forum", f);
				sub = null;
				startActivity(intent);
			} else if (temp instanceof ForumThread) {
				ForumThread ft = (ForumThread) temp;
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.PostActivity.class);
				intent.putExtra("thread", ft);
				intent.putExtra("seite", ft.getPages());
				startActivity(intent);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.threads_spinner) {
			if (seite != position + 1) {
				seite = position + 1;
				threadTask = new ThreadTask();
				threadTask.execute("");
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}

	public void viewZuweisung() {
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

			pageSpinner.setSelection(seite - 1);
			pageSpinner.setAdapter(pageAdapter);
			pageAdapter
					.setDropDownViewResource(R.layout.dialog_animelist_rate_dropdown);
			pageSpinner.setOnItemSelectedListener(this);
			root.addView(pagesView, 0);
		}
		listView = (ListView) findViewById(R.id.threads_list);
		if (seite == 1)
			threadAdapter = new ForumThreadAdapter(this, threadList,
					forum.getSubforen());
		else
			threadAdapter = new ForumThreadAdapter(this, threadList, null);
		listView.setAdapter(threadAdapter);
		listView.setOnItemClickListener(this);
	}

	public void refresh() {
		LinearLayout root = (LinearLayout) findViewById(R.id.threads_root);
		View v = root.getChildAt(0);
		if (v != null && v.getId() == R.id.forum_pages_root)
			root.removeViewAt(0);
		threadAdapter = null;
		pages = null;
		pageAdapter = null;
		threadTask = new ThreadTask();
		threadTask.execute("");
	}

	public class ThreadTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String input;
			netzwerk = Netzwerk.instance(getApplicationContext());
			eaParser = new EAParser(getApplicationContext());
			try {
				if (isCancelled())
					return null;
				input = netzwerk.getThreads(forum.getId(), seite);
				if (isCancelled())
					return null;
				pageCount = eaParser.getForumThreadPageCount(input);
				if (isCancelled())
					return null;
				threadList = eaParser.getForumThreads(input);
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
		 * Der Lade-Dialog wird geschlossen und die viewZuweisung aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (!isNullOrEmpty(result))
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
