package de.btcdev.eliteanimesapp.gui;

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

import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.PrivateMessageAdapter;
import de.btcdev.eliteanimesapp.cache.PrivateMessageCacheThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.data.PrivateMessage;
import de.btcdev.eliteanimesapp.json.PrivateMessageDeserializer;
import de.btcdev.eliteanimesapp.services.PrivateMessageService;

public class PrivateMessageActivity extends ParentActivity implements OnItemClickListener {

	@Inject
	PrivateMessageService privateMessageService;

	private PrivateMessageTask privateMessageTask;
	private int pageCount = 1;
	private ArrayList<PrivateMessage> privateMessages;
	private int chosenPosition;
	private PrivateMessageAdapter privateMessageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pn);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Meine Nachrichten");
		if (savedInstanceState != null) {
			privateMessages = savedInstanceState.getParcelableArrayList("PNs");
			pageCount = savedInstanceState.getInt("Seitenzahl");
			if (privateMessages == null) {
				privateMessageTask = new PrivateMessageTask();
				privateMessageTask.execute("");
			} else
				fillViews(privateMessages);
		} else {
			// Neue Nachrichten vorhanden
			if (configurationService.getNewMessageCount() != 0) {
				pageCount = 1;
				privateMessageTask = new PrivateMessageTask();
				privateMessageTask.execute("no_cache");
			} else {
				// Versuche Daten aus Cache zu laden
				pageCount = 1;
				privateMessageTask = new PrivateMessageTask();
				privateMessageTask.execute("");
			}

		}
		handleNavigationDrawer(R.id.nav_pn, R.id.nav_pn_list,
				"Meine Nachrichten", null);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.pn, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender PrivateMessageTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (privateMessageTask != null) {
			privateMessageTask.cancel(true);
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
	 * Speichert die aktuelle Seitenzahl und die PrivateMessage-Liste
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("PNs", privateMessages);
		savedInstanceState.putInt("Seitenzahl", pageCount);
	}

	/**
	 * Erzeugt eine ListView-Komponente, über die mit einem Adapter die PNs
	 * zugewiesen werden
	 * 
	 * @param result
	 *            die PNs in einer ArrayList
	 */
	public void fillViews(ArrayList<PrivateMessage> result) {
		privateMessages = result;
		if (privateMessages == null || privateMessages.isEmpty()) {
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
			privateMessageAdapter = new PrivateMessageAdapter(this, result);
			list.setAdapter(privateMessageAdapter);
			registerForContextMenu(list);
			list.setSelection(30 * (pageCount - 1) - 1);
			if (privateMessages.size() % 30 == 0)
				pageCount = privateMessages.size() / 30;
			else
				pageCount = privateMessages.size() / 30 + 1;
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		pageCount = 1;
		privateMessageTask = new PrivateMessageTask();
		privateMessageTask.execute("no_cache");
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
			if (arg2 == NAVIGATION_PRIVATE_MESSAGES)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.pnliste) {
			if (arg2 < privateMessages.size()) {
				PrivateMessage privateMessage = privateMessages.get(arg2);
				boolean read = privateMessage.isRead();
				privateMessage.setRead(true);
				privateMessageAdapter.notifyDataSetChanged();
				Intent intent = new Intent(getApplicationContext(),
						NewPrivateMessageActivity.class);
				intent.putExtra("PrivateMessage", privateMessage);
				intent.putExtra("read", read);
				new PrivateMessageCacheThread(PrivateMessageCacheThread.MODE_SAVE_CACHE, privateMessages);
				startActivity(intent);
			} else {
				privateMessageTask = new PrivateMessageTask();
				privateMessageTask.execute("more");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String temp = item.getTitle().toString();
		if (temp.equals(getResources().getString(R.string.pn_delete))) {
			privateMessageTask = new PrivateMessageTask();
			privateMessageTask.execute("delete");
		} else if (temp.equals(getResources().getString(
				R.string.pn_profil_besuchen))) {
			PrivateMessage k = privateMessages.get(chosenPosition);
			chosenPosition = -1;
			String name = k.getUserName();
			int userid = k.getUserId();
			Intent intent = new Intent(this,
					UserProfileActivity.class);
			intent.putExtra("User", name);
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
			if (chosenPosition < privateMessages.size()) {
				menu.add(getResources().getString(R.string.pn_delete));
				menu.add(getResources().getString(R.string.pn_profil_besuchen));
				menu.setHeaderTitle("PrivateMessage von "
						+ privateMessages.get(chosenPosition).getUserName());
			}
		}

	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class PrivateMessageTask extends AsyncTask<String, String, ArrayList<PrivateMessage>> {

		private boolean delete, more, cache;

		/**
		 * Lädt im Hintergrund übers NetworkService die PNs und parst sie noch den
		 * gewünschten Daten. Gibt eine ArrayList mit den PNdaten zurück
		 */
		@Override
		protected ArrayList<PrivateMessage> doInBackground(String... params) {
			String input;
            NewsThread.getNews(networkService);
			try {
				if (params[0].equals("no_cache")) {
					if (isCancelled())
						return null;
					input = privateMessageService.getPrivateMessagePage(1);
					if (isCancelled())
						return null;
					privateMessages = privateMessageService.getPrivateMessages(input);
					return privateMessages;
				} else if (params[0].equals("delete")) {
					PrivateMessage privateMessage = privateMessages.get(chosenPosition);
					chosenPosition = -1;
					privateMessageService.deletePrivateMessage(Integer.toString(privateMessage.getId()));
					privateMessages.remove(privateMessage);
					delete = true;
					return privateMessages;
				} else if (params[0].equals("more")) {
					more = true;
					if (isCancelled())
						return null;
					input = privateMessageService.getPrivateMessagePage(pageCount + 1);
					if (isCancelled())
						return null;
					privateMessages = privateMessageService.getMorePrivateMessages(input, privateMessages);
					pageCount++;
					return privateMessages;
				} else {
					if (loadCache()) {
						cache = true;
						return privateMessages;
					} else {
						if (isCancelled())
							return null;
						input = privateMessageService.getPrivateMessagePage(1);
						if (isCancelled())
							return null;
						privateMessages = privateMessageService.getPrivateMessages(input);
						return privateMessages;
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
		protected void onPostExecute(ArrayList<PrivateMessage> result) {
			if (result != null && !result.isEmpty()) {
				if (!more && !cache && pageCount == 1) {
					new PrivateMessageCacheThread(PrivateMessageCacheThread.MODE_SAVE_CACHE, result);
				}
			}
			if (more || delete)
				privateMessageAdapter.notifyDataSetChanged();
			else
				fillViews(result);
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
		protected void onCancelled(ArrayList<PrivateMessage> result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		/**
		 * Versucht den PrivateMessage-Cache zu laden und gibt den Erfolg der Operation
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
					// ist der Cache vom aktuellen User?
					if (prefs.getString("lastUser", "").equals(
							configurationService
									.getUserName(getApplicationContext()))) {
						// lese Cache aus und speicher in ConfigurationService
						String jsonCache = prefs.getString("PNCache", "");
						if (!jsonCache.equals("")) {
							// Konvertiere JSON zurück zu ArrayList aus PNs
							// und setze Cache in ConfigurationService
							try {
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(PrivateMessage.class,
												new PrivateMessageDeserializer()).create();

								Type collectionType = new TypeToken<ArrayList<PrivateMessage>>() {
								}.getType();
								ArrayList<PrivateMessage> jsonList = gson.fromJson(
										jsonCache, collectionType);
								privateMessages = jsonList;
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
