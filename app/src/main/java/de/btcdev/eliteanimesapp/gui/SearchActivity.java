package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.ConfigurationService;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.User;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.NewsThread;

public class SearchActivity extends ParentActivity implements
		OnItemClickListener, OnClickListener {

	@Inject
	ConfigurationService configurationService;
	@Inject
	NetworkService networkService;

	private EditText eingabe;
	private Button searchButton;
	private SearchTask task;
	private ArrayList<User> liste;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((EaApp) getApplication()).getEaComponent().inject(this);
		setContentView(R.layout.activity_search);
		actionBar = getSupportActionBar();
		eaParser = new EAParser(null);
		eingabe = (EditText) findViewById(R.id.search_eingabe);
		searchButton = (Button) findViewById(R.id.search_button);
		searchButton.setOnClickListener(this);

		if (savedInstanceState != null) {
			liste = savedInstanceState.getParcelableArrayList("Suche");
			viewZuweisung(liste);
		}
		handleNavigationDrawer(R.id.nav_search, R.id.nav_search_list,
				"User-Suche", null);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * SearchTask wird dabei abgebrochen.
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

	/**
	 * Speichert die Daten der Suche.
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("Suche", liste);
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.getId() == R.id.nav_search_list) {
			if (arg2 == NAVIGATION_SEARCH)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.search_liste) {
			if (arg2 <= liste.size()) {
				User user = liste.get(arg2);
				if (user.getName().equals(
						configurationService.getUserName(getApplicationContext()))) {
					Intent intent = new Intent(this,
							ProfileActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(
							this,
							UserProfileActivity.class);
					intent.putExtra("User", user.getName());
					intent.putExtra("UserID",
							Integer.parseInt(user.getId()));
					startActivity(intent);
				}
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.search_button) {
			String text = eingabe.getText().toString();
			if (text != null && !text.isEmpty()) {
				// suchen
				task = new SearchTask(text);
				task.execute("");
			}
		}
	}

	public void viewZuweisung(ArrayList<User> result) {
		liste = result;
		LinearLayout lin = (LinearLayout) findViewById(R.id.search_layout);
		if (liste == null || liste.isEmpty()) {
			TextView text = new TextView(this);
			text.setTextSize(16);
			text.setTypeface(text.getTypeface(), Typeface.BOLD);
			text.setText("Die Suche ergab keine Treffer.");
			text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			lin.addView(text, 1, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		} else {
			if (lin.getChildCount() != 2) {
				lin.removeViewAt(1);
			}
			ListView searchList = (ListView) findViewById(R.id.search_liste);
			ArrayAdapter<User> adapter = new ArrayAdapter<User>(this,
					android.R.layout.simple_list_item_1, liste);
			searchList.setAdapter(adapter);
			searchList.setOnItemClickListener(this);
		}
	}

	public class SearchTask extends
			AsyncTask<String, String, ArrayList<User>> {

		String name;

		public SearchTask(String name) {
			this.name = name;
		}

		@Override
		protected ArrayList<User> doInBackground(String... params) {
			String input;
			eaParser = new EAParser(null);
            NewsThread.getNews(networkService);
			ArrayList<User> result;
			try {
				if (isCancelled())
					return null;
				input = networkService.searchUser(name);
				if (isCancelled())
					return null;
				result = eaParser.getSearchedUsers(input);
				if (isCancelled())
					return null;
				return result;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onCancelled();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<User> result) {
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			viewZuweisung(result);
		}

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
