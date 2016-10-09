package de.btcdev.eliteanimesapp.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.data.PrivateMessage;
import de.btcdev.eliteanimesapp.services.PrivateMessageService;

public class NewPrivateMessageActivity extends ParentActivity implements
		OnItemClickListener {

	@Inject
	PrivateMessageService privateMessageService;

	private EditText privateMessageInputView;
	private PrivateMessage privateMessage;
	private boolean showSpoiler;
	private PrivateMessageTask privateMessageTask;
	private boolean sendMode;
	private String privateMessageInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_neue_pn);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Neue Nachricht");
		if (savedInstanceState != null) {
			privateMessage = savedInstanceState.getParcelable("PrivateMessage");
		} else {
			Intent intent = getIntent();
			privateMessage = intent.getParcelableExtra("PrivateMessage");
			boolean read = intent.getBooleanExtra("read", false);
			if (!read) {
				// markiere PrivateMessage auch servertseitig als gelesen
				new Thread(new Runnable() {
					public void run() {
						try {
							privateMessageService.getPrivateMessage(
									privateMessage.getId());
						} catch (Exception e) {

						}
					}
				}).start();
			}
		}
		actionBar.setSubtitle(privateMessage.getUserName());
		fillViews(privateMessage);
		handleNavigationDrawer(R.id.nav_neue_pn, R.id.nav_neue_pn_list,
				"Neue Nachricht", privateMessage.getUserName());
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.neue_pn, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.new_pn_send:
			sendMode = true;
			privateMessageInput = privateMessageInputView.getText().toString();
			privateMessageTask = new PrivateMessageTask();
			privateMessageTask.execute("");
			return true;
		case R.id.new_pn_delete:
			privateMessageTask = new PrivateMessageTask();
			privateMessageTask.execute("delete");
			return true;
		case R.id.new_pn_profil:
			String name = privateMessage.getUserName();
			int userid = privateMessage.getUserId();
			Intent intent = new Intent(this,
					UserProfileActivity.class);
			intent.putExtra("User", name);
			intent.putExtra("UserID", userid);
			startActivity(intent);
			return true;
		case R.id.new_pn_spoiler:
			showSpoiler = !showSpoiler;
			fillViews(privateMessage);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Speichert die aktuellen PrivateMessage-Daten
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("PrivateMessage", privateMessage);
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
		if (arg0.getId() == R.id.nav_neue_pn_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	public void fillViews(PrivateMessage privateMessage) {
		TextView subjectView = (TextView) findViewById(R.id.neue_pn_betreff);
		TextView dateView = (TextView) findViewById(R.id.neue_pn_datum);
		TextView messageContentView = (TextView) findViewById(R.id.neue_pn_text);
		privateMessageInputView = (EditText) findViewById(R.id.neue_pn_textfeld);
		subjectView.setText(privateMessage.getSubject());
		dateView.setText(privateMessage.getDate());
		String messageContent = privateMessage.getMessage();
		messageContent = new EAParser(this).showSpoiler(showSpoiler, messageContent);
		messageContent = messageContent.replace("\r", "");
		messageContent = messageContent.replace("\t", "");
		messageContent = "<html><head></head><body> ----- " + privateMessage.getUserName()
				+ " schrieb: ----- <br> " + messageContent + " </body></html>";
		messageContentView.setText(Html.fromHtml(messageContent));
		privateMessage.setRead(true);
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class PrivateMessageTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String input;
			if (params[0].equals("")) {
				// Alte Nachricht soll abgerufen werden
				if (!sendMode) {
					try {
                        NewsThread.getNews(networkService);
						input = privateMessageService.getPrivateMessage(privateMessage.getId());
						privateMessage = privateMessageService.getPrivateMessage(privateMessage, input);
						return null;
					} catch (EAException e) {
						publishProgress("Exception", e.getMessage());
					}
				}
				// Neue Nachricht soll abgeschickt werden
				else {
					try {
						String result = privateMessageService.answerPrivateMessage(privateMessage.getId(), privateMessageInput);
						return privateMessageService.checkPrivateMessage(result);
					} catch (EAException e) {
						publishProgress("Exception", e.getMessage());
					}
				}
			} else if (params[0].equals("delete")) {
				try {
					privateMessageService.deletePrivateMessage(Integer.toString(privateMessage.getId()));
					return "delete";
				} catch (EAException e) {
					publishProgress("Exception", e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Schließt den Ladedialog und öffnet die CommentActivity
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
			if (result == null) {
				if (!sendMode) {
					fillViews(privateMessage);
				} else {
					Toast.makeText(getBaseContext(),
							"PrivateMessage konnte nicht gesendet werden.",
							Toast.LENGTH_LONG).show();
				}
			} else if (result.equals("delete")) {
				Intent intent = new Intent(getApplicationContext(),
						PrivateMessageActivity.class);
				startActivity(intent);
			} else if (result.equals("Erfolg")) {
				Intent intent = new Intent(getApplicationContext(),
						PrivateMessageActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), result,
						Toast.LENGTH_SHORT).show();
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
