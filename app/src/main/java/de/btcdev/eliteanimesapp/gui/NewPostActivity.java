package de.btcdev.eliteanimesapp.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.ForumPost;
import de.btcdev.eliteanimesapp.data.ForumThread;
import de.btcdev.eliteanimesapp.data.Netzwerk;

public class NewPostActivity extends ParentActivity {

	private ForumThread thread;
	private boolean editieren;
	private boolean zitieren;
	private EditText postEingabe;
	private ForumPost editPost;
	private String barName;
	private NewPostTask newPostTask;

	/**
	 * Erzeugt die Activity, liest Benutzer und UserID aus.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post);
		postEingabe = (EditText) findViewById(R.id.new_post_text);
		bar = getSupportActionBar();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		editieren = bundle.getBoolean("editieren", false);
		zitieren = bundle.getBoolean("zitieren", false);
		thread = bundle.getParcelable("thread");
		barName = "Neuer Beitrag";
		if (editieren) {
			editPost = (ForumPost) bundle.getParcelable("editPost");
			System.out.println(editPost);
			barName = "Beitrag editieren";
			newPostTask = new NewPostTask(false, true);
			newPostTask.execute("");
		} else if (zitieren) {
			editPost = (ForumPost) bundle.getParcelable("editPost");
			newPostTask = new NewPostTask(false, true);
			newPostTask.execute("");
		}
		bar.setTitle(barName);
		bar.setSubtitle(thread.getName());
		handleNavigationDrawer(R.id.nav_new_post, R.id.nav_new_post_list,
				barName, thread.getName());
	}

	/**
	 * Erzeugt das Menü.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_post, menu);
		return true;
	}

	/**
	 * Behandelt Klick-Ereignisse des Menüs
	 * 
	 * @param item
	 *            angeklicktes Menü-Element
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.new_post_send:
			if (!isNullOrEmpty(postEingabe.getText().toString())) {
				newPostTask = new NewPostTask(editieren, false);
				newPostTask.execute("");
			}
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Behandelt einen Klick auf den Navigation Drawer. Dabei wird evtl eine
	 * neue Activity gestartet.
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
		if (arg0.getId() == R.id.nav_new_post_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	public class NewPostTask extends AsyncTask<String, String, String> {

		private boolean edit;
		private boolean editGet;

		public NewPostTask(boolean edit, boolean editGet) {
			this.edit = edit;
			this.editGet = editGet;
		}

		/**
		 * Schickt die Eingabe aus dem Textfeld als neuen bzw editierten Post
		 * ab.
		 * 
		 * @param params
		 *            String-Array mit Informationen - hier irrelevant
		 * @return String - irrelevant
		 * @throws EAException
		 *             bei Verbindungs- und Streamfehlern jeglicher Art
		 */
		@Override
		protected String doInBackground(String... params) {
			boolean send = false;
			netzwerk = Netzwerk.instance(getApplicationContext());
			try {
				if (edit) {
					String text = postEingabe.getText().toString();
					send = netzwerk.editPost(text, editPost.getId());
				} else if (editGet) {
					String result = netzwerk.getPost(editPost.getId(), true);
					result = new EAParser(getApplicationContext())
							.getPost(result);
					if (!isNullOrEmpty(result))
						editPost.setText(result);
					send = true;
				} else {
					if (isCancelled())
						return null;
					String text = postEingabe.getText().toString();
					if (isCancelled())
						return null;
					send = netzwerk.addPost(text, thread.getId());
				}
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			if (send)
				return "Erfolg";
			return null;
		}

		/**
		 * Schließt den Ladedialog und öffnet die PostActivity
		 * 
		 * @param result
		 *            - irrelevant
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (editGet) {
				if (zitieren) {
					StringBuilder b = new StringBuilder();
					b.append("[quote]");
					b.append(editPost.getText());
					b.append("\n");
					b.append("Zitat von [url=http://www.eliteanimes.com/profil/");
					b.append(editPost.getUserId());
					b.append("/");
					b.append(editPost.getUserName());
					b.append("]");
					b.append(editPost.getUserName());
					b.append("[/url][/quote]\n");
					postEingabe.setText(b.toString());
				} else {
					postEingabe.setText(editPost.getText());
				}
			} else {
				if (result != null && result.equals("Erfolg")) {
					Intent intent = new Intent(getApplicationContext(),
							de.btcdev.eliteanimesapp.gui.PostActivity.class);
					intent.putExtra("thread", thread);
					intent.putExtra("seite", thread.getPages());
					startActivity(intent);
				} else {
					Toast.makeText(getBaseContext(),
							"Kommentar konnte nicht gesendet werden.",
							Toast.LENGTH_LONG).show();
				}
			}
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
		protected void onCancelled(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
