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

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.BoardPost;
import de.btcdev.eliteanimesapp.data.BoardThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;

public class NewPostActivity extends ParentActivity {

	private BoardThread boardThread;
	private boolean editMode;
	private boolean citeMode;
	private EditText postInputView;
	private BoardPost editPost;
	private String actionBarTitle;
	private NewPostTask newPostTask;
	private String postInput;

	/**
	 * Erzeugt die Activity, liest User und UserID aus.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_new_post);
		postInputView = (EditText) findViewById(R.id.new_post_text);
		actionBar = getSupportActionBar();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		editMode = bundle.getBoolean("editMode", false);
		citeMode = bundle.getBoolean("citeMode", false);
		boardThread = bundle.getParcelable("boardThread");
		actionBarTitle = "Neuer Beitrag";
		if (editMode) {
			editPost = bundle.getParcelable("editPost");
			actionBarTitle = "Beitrag editMode";
			postInput = postInputView.getText().toString();
			newPostTask = new NewPostTask(false, true);
			newPostTask.execute("");
		} else if (citeMode) {
			editPost = bundle.getParcelable("editPost");
			postInput = postInputView.getText().toString();
			newPostTask = new NewPostTask(false, true);
			newPostTask.execute("");
		}
		actionBar.setTitle(actionBarTitle);
		actionBar.setSubtitle(boardThread.getName());
		handleNavigationDrawer(R.id.nav_new_post, R.id.nav_new_post_list,
				actionBarTitle, boardThread.getName());
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	/**
	 * Erzeugt das Menü.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
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
			if (!isNullOrEmpty(postInputView.getText().toString())) {
				postInput = postInputView.getText().toString();
				newPostTask = new NewPostTask(editMode, false);
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
		 */
		@Override
		protected String doInBackground(String... params) {
			boolean send = false;
			try {
				if (edit) {
					send = networkService.editPost(postInput, editPost.getId());
				} else if (editGet) {
					String result = networkService.getPost(editPost.getId(), true);
					result = new EAParser(getApplicationContext())
							.getPost(result);
					if (!isNullOrEmpty(result))
						editPost.setText(result);
					send = true;
				} else {
					if (isCancelled())
						return null;
					send = networkService.addPost(postInput, boardThread.getId());
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
				if (citeMode) {
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
					postInputView.setText(b.toString());
				} else {
					postInputView.setText(editPost.getText());
				}
			} else {
				if (result != null && result.equals("Erfolg")) {
					Intent intent = new Intent(getApplicationContext(),
							de.btcdev.eliteanimesapp.gui.PostActivity.class);
					intent.putExtra("boardThread", boardThread);
					intent.putExtra("seite", boardThread.getPages());
					startActivity(intent);
				} else {
					Toast.makeText(getBaseContext(),
							"Comment konnte nicht gesendet werden.",
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
