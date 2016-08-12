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
import de.btcdev.eliteanimesapp.adapter.CommentAdapter;
import de.btcdev.eliteanimesapp.cache.CommentCacheThread;
import de.btcdev.eliteanimesapp.data.Comment;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Configuration;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.json.CommentDeserializer;

public class CommentActivity extends ParentActivity implements
		OnItemClickListener {

	private String aktuellerUser;
	private int userID;
	private int seitenzahl;
	private CommentTask commentTask;
	private CommentAdapter commentAdapter;
	private ArrayList<Comment> commentlist;
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

		networkService = NetworkService.instance(this);
		eaParser = new EAParser(null);

		if (savedInstanceState != null) {
			aktuellerUser = savedInstanceState.getString("User");
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
			aktuellerUser = intentdata.getString("User");
			userID = intentdata.getInt("UserID");
			bar.setSubtitle(aktuellerUser);
			// Neue Kommentare vorhanden oder anderer
			// User oder selbst neuer Comment gesendet
			if (Configuration.getNewCommentCount() != 0
					|| !Configuration.getUserName(getApplicationContext())
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
		Configuration.setNewCommentCount(0, this);
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
		savedInstanceState.putString("User", aktuellerUser);
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
	public void viewZuweisung(ArrayList<Comment> result) {
		Configuration.setNewCommentCount(0, this);
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
			commentAdapter = new CommentAdapter(this, result, spoilerArray);
			list.setAdapter(commentAdapter);
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
					NewCommentActivity.class);
			intent.putExtra("User", aktuellerUser);
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
				if (aktuellerUser.equals(Configuration
						.getUserName(getApplicationContext()))) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					Intent intent = new Intent(
							this,
							CommentActivity.class);
					intent.putExtra("User", Configuration
							.getUserName(getApplicationContext()));
					intent.putExtra("UserID",
							Configuration.getUserID(getApplicationContext()));
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
					NewCommentActivity.class);
			Comment k = commentlist.get(chosenPosition);
			chosenPosition = -1;
			intent.putExtra("Comment", k);
			intent.putExtra("User", aktuellerUser);
			intent.putExtra("UserID", userID);
			intent.putExtra("Status", "Editieren");
			startActivity(intent);
		} else if (temp.equals(getResources().getString(
				R.string.comment_profil_besuchen))) {
			Comment k = commentlist.get(chosenPosition);
			chosenPosition = -1;
			String name = k.getUserName();
			if (name.equals(Configuration
					.getUserName(getApplicationContext()))) {
				Intent intent = new Intent(this,
						ProfileActivity.class);
				startActivity(intent);
			} else {
				int userid = k.getUserId();
				Intent intent = new Intent(
						this,
						UserProfileActivity.class);
				intent.putExtra("User", name);
				intent.putExtra("UserID", userid);
				startActivity(intent);
			}
		} else if (temp.contains("antworten")) {
			Intent intent = new Intent(this,
					NewCommentActivity.class);
			Comment k = commentlist.get(chosenPosition);
			int userid = k.getUserId();
			intent.putExtra("User", k.getUserName());
			intent.putExtra("UserID", userid);
			intent.putExtra("Status", "Neu");
			intent.putExtra("Response", true);
			intent.putExtra("Comment", k);
			startActivity(intent);
		} else if (temp.equals(getResources().getString(R.string.comment_copy))) {
			Comment k = commentlist.get(chosenPosition);
			String text = k.getText();
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
			Toast.makeText(this,
					"Comment wurde in die Zwischenablage kopiert",
					Toast.LENGTH_SHORT).show();
		} else if (temp.equals(getResources().getString(
				R.string.comment_spoiler))) {
			if (spoilerArray.get(chosenPosition))
				spoilerArray.set(chosenPosition, false);
			else
				spoilerArray.set(chosenPosition, true);
			commentAdapter.notifyDataSetChanged();
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
				Comment k = commentlist.get(chosenPosition);
				String name = Configuration
						.getUserName(getApplicationContext());
				ArrayList<String> items = new ArrayList<String>();
				if (k.getUserName().equals(name)
						&& aktuellerUser.equals(name)) {
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_delete));
					items.add(getResources().getString(R.string.comment_edit));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else if (k.getUserName().equals(name)) {
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_edit));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else if (aktuellerUser.equals(name)) {
					items.add(k.getUserName() + " antworten");
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_delete));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				} else {
					items.add(k.getUserName() + " antworten");
					items.add(getResources()
							.getString(R.string.comment_spoiler));
					items.add(getResources().getString(R.string.comment_copy));
					items.add(getResources().getString(
							R.string.comment_profil_besuchen));
				}
				for (int i = 0; i < items.size(); i++)
					menu.add(items.get(i));
				menu.setHeaderTitle("Comment von " + k.getUserName());
			}
		}
	}

	/**
	 * Klasse f�r das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class CommentTask extends
			AsyncTask<String, String, ArrayList<Comment>> {

		private boolean more, delete;

		/**
		 * Lädt im Hintergrund übers NetworkService die Kommentare und parst sie noch
		 * den gewünschten Daten. Gibt eine ArrayList mit den Kommentardaten
		 * zurück
		 * 
		 * @param params
		 *            String-Array, nachdem ausgewählt wird: "more" - es werden
		 *            mehr Kommentare geladen "delete" - der gewählte Comment
		 *            wird gelöscht sonst - es werden Kommentare geladen
		 * @return ArrayList, mit den geladenen Kommentaren
		 * @throws EAException
		 *             bei Verbindungs- und Streamfehlern jeglicher Art
		 */
		@Override
		protected ArrayList<Comment> doInBackground(String... params) {
			String input;
			eaParser = new EAParser(null);
			networkService = NetworkService.instance(getApplicationContext());
			new NewsThread(getApplicationContext()).start();
			if (params[0].equals("more")) {
				try {
					if (isCancelled())
						return null;
					input = networkService.getCommentPage(seitenzahl + 1,
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
				Comment k = commentlist.get(chosenPosition);
				chosenPosition = -1;
				try {
					if (isCancelled())
						return null;
					networkService.deleteComment(Integer.toString(k.getId()));
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
						input = networkService.getCommentPage(1, aktuellerUser,
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
							input = networkService.getCommentPage(1, aktuellerUser,
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
		protected void onPostExecute(ArrayList<Comment> result) {

			if (result != null && !result.isEmpty()) {
				if (aktuellerUser.equals(Configuration
						.getUserName(getApplicationContext()))
						&& seitenzahl == 1) {
					new CommentCacheThread(
							CommentCacheThread.MODE_SAVE_CACHE, result);
				}
			}
			if (more || delete) {
				spoilerArray = new ArrayList<Boolean>(result.size());
				for (int i = 0; i < result.size(); i++) {
					spoilerArray.add(false);
				}
				commentAdapter.updateSpoilerArray(spoilerArray);
				commentAdapter.notifyDataSetChanged();
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
		protected void onCancelled(ArrayList<Comment> result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	/**
	 * Versucht den Comment-Cache zu laden und gibt den Erfolg der Operation
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
				// ist der Cache vom aktuellen User?
				if (prefs.getString("lastUser", "").equals(
						Configuration.getUserName(getApplicationContext()))) {
					// lese Cache aus und speicher in Configuration
					String jsonCache = prefs.getString("CommentCache", "");
					if (!jsonCache.equals("")) {
						// Konvertiere JSON zurück zu ArrayList aus Kommentaren
						// und setze Cache in Configuration
						try {
							Gson gson = new GsonBuilder().registerTypeAdapter(
									Comment.class,
									new CommentDeserializer()).create();

							Type collectionType = new TypeToken<ArrayList<Comment>>() {
							}.getType();
							ArrayList<Comment> jsonList = gson.fromJson(
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
