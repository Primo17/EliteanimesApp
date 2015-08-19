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
import de.btcdev.eliteanimesapp.adapter.KommentarAdapter;
import de.btcdev.eliteanimesapp.cache.KommentarCacheThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Kommentar;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.json.KommentarDeserializer;

public class KommentarActivity extends ParentActivity implements
		OnItemClickListener {

	private String aktuellerUser;
	private int userID;
	private int seitenzahl;
	private CommentTask commentTask;
	private KommentarAdapter kommentarAdapter;
	private ArrayList<Kommentar> commentlist;
	private ArrayList<Boolean> spoilerArray;
	private int chosenPosition;

	/**
	 * Erzeugt die Activity und ruft einen CommentTask auf, um die
	 * Kommentardaten zu laden
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kommentar);
		bar = getSupportActionBar();
		bar.setTitle("Kommentare");

		netzwerk = Netzwerk.instance(this);
		eaParser = new EAParser(null);

		if (savedInstanceState != null) {
			aktuellerUser = savedInstanceState.getString("Benutzer");
			userID = savedInstanceState.getInt("UserID");
			commentlist = savedInstanceState
					.getParcelableArrayList("Kommentare");
			seitenzahl = savedInstanceState.getInt("Seitenzahl");
			bar.setSubtitle(aktuellerUser);
			chosenPosition = savedInstanceState.getInt("chosenPosition");
			if (commentlist != null)
				viewZuweisung(commentlist);
			else {
				commentTask = new CommentTask();
				commentTask.execute("");
			}
		} else {
			Intent intent = getIntent();
			Bundle intentdata = intent.getExtras();
			aktuellerUser = intentdata.getString("Benutzer");
			userID = intentdata.getInt("UserID");
			bar.setSubtitle(aktuellerUser);
			// Neue Kommentare vorhanden oder anderer
			// Benutzer oder selbst neuer Kommentar gesendet
			if (Konfiguration.getNewCommentCount() != 0
					|| !Konfiguration.getBenutzername(getApplicationContext())
							.equals(aktuellerUser)
					|| intentdata.getBoolean("Send")) {
				seitenzahl = 1;
				commentTask = new CommentTask();
				commentTask.execute("no_cache");
			} else {
				seitenzahl = 1;
				commentTask = new CommentTask();
				commentTask.execute("");
			}
		}
		handleNavigationDrawer(R.id.nav_kommentare, R.id.nav_kommentare_list,
				"Kommentare", aktuellerUser);
		Konfiguration.setNewCommentCount(0, this);
	}

	/**
	 * Erzeugt das Menü
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kommentar, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * ProfilTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (commentTask != null) {
			commentTask.cancel(true);
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
		savedInstanceState.putParcelableArrayList("Kommentare", commentlist);
		savedInstanceState.putInt("Seitenzahl", seitenzahl);
		savedInstanceState.putInt("chosenPosition", chosenPosition);
	}

	/**
	 * Erzeugt eine ListView-Komponente, über die mit einem Adapter die
	 * Kommentare zugewiesen werden
	 * 
	 * @param result
	 *            die Kommentare in einer ArrayList
	 */
	public void viewZuweisung(ArrayList<Kommentar> result) {
		Konfiguration.setNewCommentCount(0, this);
		commentlist = result;
		if (commentlist == null || commentlist.isEmpty()) {
			LinearLayout lin = (LinearLayout) findViewById(R.id.comments_layout);
			TextView text = new TextView(this);
			text.setTextSize(16);
			text.setTypeface(text.getTypeface(), Typeface.BOLD);
			text.setText("Keine Kommentare vorhanden.");
			text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			lin.addView(text, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		} else {
			spoilerArray = new ArrayList<Boolean>(commentlist.size());
			for (int i = 0; i < commentlist.size(); i++) {
				spoilerArray.add(false);
			}
			ListView list = (ListView) findViewById(R.id.kommentarliste);
			kommentarAdapter = new KommentarAdapter(this, result, spoilerArray);
			list.setAdapter(kommentarAdapter);
			list.setOnItemClickListener(this);
			registerForContextMenu(list);
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		seitenzahl = 1;
		commentTask = new CommentTask();
		commentTask.execute("no_cache");
	}

	/**
	 * Behandelt Klick-Ereignisse des Menüs
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.new_comment_icon:
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NeuerKommentarActivity.class);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			intent.putExtra("Status", "Neu");
			startActivity(intent);
			break;
		case R.id.kommentar_aktualisieren:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Behandelt Klick-Ereignisse auf der Kommentarliste
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.getId() == R.id.kommentarliste) {
			if (arg2 >= commentlist.size()) {
				commentTask = new CommentTask();
				commentTask.execute("more");
			}
		} else if (arg0.getId() == R.id.nav_kommentare_list) {
			if (arg2 == navigation_kommentare) {
				if (aktuellerUser.equals(Konfiguration
						.getBenutzername(getApplicationContext()))) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					Intent intent = new Intent(
							this,
							de.btcdev.eliteanimesapp.gui.KommentarActivity.class);
					intent.putExtra("Benutzer", Konfiguration
							.getBenutzername(getApplicationContext()));
					intent.putExtra("UserID",
							Konfiguration.getUserID(getApplicationContext()));
					mDrawerLayout.closeDrawer(Gravity.LEFT);
					startActivity(intent);
				}
			} else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String temp = item.getTitle().toString();
		if (temp.equals(getResources().getString(R.string.comment_delete))) {
			commentTask = new CommentTask();
			commentTask.execute("delete");
		} else if (temp.equals(getResources().getString(R.string.comment_edit))) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NeuerKommentarActivity.class);
			Kommentar k = commentlist.get(chosenPosition);
			chosenPosition = -1;
			intent.putExtra("Kommentar", k);
			intent.putExtra("Benutzer", aktuellerUser);
			intent.putExtra("UserID", userID);
			intent.putExtra("Status", "Editieren");
			startActivity(intent);
		} else if (temp.equals(getResources().getString(
				R.string.comment_profil_besuchen))) {
			Kommentar k = commentlist.get(chosenPosition);
			chosenPosition = -1;
			String name = k.getBenutzername();
			if (name.equals(Konfiguration
					.getBenutzername(getApplicationContext()))) {
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.ProfilActivity.class);
				startActivity(intent);
			} else {
				int userid = k.getUserId();
				Intent intent = new Intent(
						this,
						de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
				intent.putExtra("Benutzer", name);
				intent.putExtra("UserID", userid);
				startActivity(intent);
			}
		} else if (temp.contains("antworten")) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NeuerKommentarActivity.class);
			Kommentar k = commentlist.get(chosenPosition);
			int userid = k.getUserId();
			intent.putExtra("Benutzer", k.getBenutzername());
			intent.putExtra("UserID", userid);
			intent.putExtra("Status", "Neu");
			intent.putExtra("Response", true);
			intent.putExtra("Kommentar", k);
			startActivity(intent);
		} else if (temp.equals(getResources().getString(R.string.comment_copy))) {
			Kommentar k = commentlist.get(chosenPosition);
			String text = k.getText();
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
			Toast.makeText(this,
					"Kommentar wurde in die Zwischenablage kopiert",
					Toast.LENGTH_SHORT).show();
		} else if (temp.equals(getResources().getString(
				R.string.comment_spoiler))) {
			if (spoilerArray.get(chosenPosition))
				spoilerArray.set(chosenPosition, false);
			else
				spoilerArray.set(chosenPosition, true);
			kommentarAdapter.notifyDataSetChanged();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.kommentarliste) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			chosenPosition = info.position;
			if (chosenPosition < commentlist.size()) {
				Kommentar k = commentlist.get(chosenPosition);
				String name = Konfiguration
						.getBenutzername(getApplicationContext());
				ArrayList<String> items = new ArrayList<String>();
				if (k.getBenutzername().equals(name)
						&& aktuellerUser.equals(name)) {
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_delete));
					items.add(getResources().getString(R.string.comment_edit));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else if (k.getBenutzername().equals(name)) {
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_edit));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else if (aktuellerUser.equals(name)) {
					items.add(k.getBenutzername() + " antworten");
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_delete));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else {
					items.add(k.getBenutzername() + " antworten");
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				}
				for (int i = 0; i < items.size(); i++)
					menu.add(items.get(i));
				menu.setHeaderTitle("Kommentar von " + k.getBenutzername());
			}
		}
	}

	/**
	 * Klasse f�r das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class CommentTask extends
			AsyncTask<String, String, ArrayList<Kommentar>> {

		private boolean more, delete;

		/**
		 * Lädt im Hintergrund übers Netzwerk die Kommentare und parst sie noch
		 * den gewünschten Daten. Gibt eine ArrayList mit den Kommentardaten
		 * zurück
		 * 
		 * @param params
		 *            String-Array, nachdem ausgewählt wird: "more" - es werden
		 *            mehr Kommentare geladen "delete" - der gewählte Kommentar
		 *            wird gelöscht sonst - es werden Kommentare geladen
		 * @return ArrayList, mit den geladenen Kommentaren
		 * @throws EAException
		 *             bei Verbindungs- und Streamfehlern jeglicher Art
		 */
		@Override
		protected ArrayList<Kommentar> doInBackground(String... params) {
			String input;
			eaParser = new EAParser(null);
			netzwerk = Netzwerk.instance(getApplicationContext());
			new NewsThread(getApplicationContext()).start();
			if (params[0].equals("more")) {
				try {
					if (isCancelled())
						return null;
					input = netzwerk.getCommentSite(seitenzahl + 1,
							aktuellerUser, userID);
					if (isCancelled())
						return null;
					commentlist = eaParser.getMoreComments(input, commentlist);
					seitenzahl++;
					more = true;
					return commentlist;
				} catch (EAException e) {
					publishProgress("Exception", e.getMessage());
				}
			} else if (params[0].equals("delete")) {
				Kommentar k = commentlist.get(chosenPosition);
				chosenPosition = -1;
				try {
					if (isCancelled())
						return null;
					netzwerk.deleteComment(Integer.toString(k.getId()));
					commentlist.remove(k);
					delete = true;
					return commentlist;
				} catch (EAException e) {
					publishProgress("Exception", e.getMessage());
				}
			} else {
				try {
					// cache nicht gewünscht
					if (params[0].equals("no_cache")) {
						if (isCancelled())
							return null;
						input = netzwerk.getCommentSite(1, aktuellerUser,
								userID);
						if (isCancelled())
							return null;
						commentlist = eaParser.getComments(input);
						return commentlist;
					}
					// teste ob Cache vorhanden und wähle dann daraus Quelle
					else {
						if (loadCache()) {
							return commentlist;
						} else {
							if (isCancelled())
								return null;
							input = netzwerk.getCommentSite(1, aktuellerUser,
									userID);
							if (isCancelled())
								return null;
							commentlist = eaParser.getComments(input);
							return commentlist;
						}
					}
				} catch (EAException e) {
					publishProgress("Exception", e.getMessage());
				}
			}
			return null;

		}

		/**
		 * Die viewZuweisung wird mit den erhaltenen Daten aufgerufen, der
		 * LoadDialog wird geschlossen.
		 * 
		 * @param result
		 *            ArrayList mit den erhaltenen Kommentaren
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<Kommentar> result) {

			if (result != null && !result.isEmpty()) {
				if (aktuellerUser.equals(Konfiguration
						.getBenutzername(getApplicationContext()))
						&& seitenzahl == 1) {
					new KommentarCacheThread(
							KommentarCacheThread.MODE_SAVE_CACHE, result);
				}
			}
			if (more || delete) {
				spoilerArray = new ArrayList<Boolean>(result.size());
				for (int i = 0; i < result.size(); i++) {
					spoilerArray.add(false);
				}
				kommentarAdapter.updateSpoilerArray(spoilerArray);
				kommentarAdapter.notifyDataSetChanged();
			} else
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
		protected void onCancelled(ArrayList<Kommentar> result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	/**
	 * Versucht den Kommentar-Cache zu laden und gibt den Erfolg der Operation
	 * zurück
	 * 
	 * @return Ob Cache vorhanden war
	 */
	public boolean loadCache() {
		SharedPreferences prefs = getSharedPreferences("cache",
				Context.MODE_PRIVATE);
		// Gibt es �berhaupt Speicherstände eines Benutzers?
		if (prefs.contains("lastUser")) {
			// Gibt es einen CommentCache?
			if (prefs.contains("CommentCache")) {
				// ist der Cache vom aktuellen Benutzer?
				if (prefs.getString("lastUser", "").equals(
						Konfiguration.getBenutzername(getApplicationContext()))) {
					// lese Cache aus und speicher in Konfiguration
					String jsonCache = prefs.getString("CommentCache", "");
					if (!jsonCache.equals("")) {
						// Konvertiere JSON zurück zu ArrayList aus Kommentaren
						// und setze Cache in Konfiguration
						try {
							Gson gson = new GsonBuilder().registerTypeAdapter(
									Kommentar.class,
									new KommentarDeserializer()).create();

							Type collectionType = new TypeToken<ArrayList<Kommentar>>() {
							}.getType();
							ArrayList<Kommentar> jsonList = gson.fromJson(
									jsonCache, collectionType);
							commentlist = jsonList;
							return true;
						} catch (JsonParseException e) {
							// lösche vorhanden Cache
							SharedPreferences.Editor editor = prefs.edit();
							editor.remove("CommentCache");
							editor.apply();
							return false;
						}
					}
				} else {
					// lösche vorhanden Cache
					SharedPreferences.Editor editor = prefs.edit();
					editor.remove("CommentCache");
					editor.apply();
					return false;
				}
			}
		}
		return false;
	}

}
