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
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.data.PN;

public class NeuePNActivity extends ParentActivity implements
		OnItemClickListener {

	private EditText pnEingabe;
	private PN pn;
	private boolean spoiler;
	private NewPNTask pnTask;
	private boolean send = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_neue_pn);
		bar = getSupportActionBar();
		bar.setTitle("Neue Nachricht");
		netzwerk = Netzwerk.instance(this);
		eaParser = new EAParser(null);
		if (savedInstanceState != null) {
			pn = savedInstanceState.getParcelable("PN");
		} else {
			Intent intent = getIntent();
			pn = intent.getParcelableExtra("PN");
			boolean read = intent.getBooleanExtra("read", false);
			if (!read) {
				// markiere PN auch servertseitig als gelesen
				new Thread(new Runnable() {
					public void run() {
						try {
							Netzwerk.instance(getApplicationContext()).getPN(
									pn.getId());
						} catch (Exception e) {

						}
					}
				}).start();
			}
		}
		bar.setSubtitle(pn.getBenutzername());
		viewZuweisung(pn);
		handleNavigationDrawer(R.id.nav_neue_pn, R.id.nav_neue_pn_list,
				"Neue Nachricht", pn.getBenutzername());
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender NewPNTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (pnTask != null) {
			pnTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.neue_pn, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.new_pn_send:
			send = true;
			pnTask = new NewPNTask();
			pnTask.execute("");
			return true;
		case R.id.new_pn_delete:
			pnTask = new NewPNTask();
			pnTask.execute("delete");
			return true;
		case R.id.new_pn_profil:
			String name = pn.getBenutzername();
			int userid = pn.getUserid();
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
			intent.putExtra("Benutzer", name);
			intent.putExtra("UserID", userid);
			startActivity(intent);
			return true;
		case R.id.new_pn_spoiler:
			spoiler = !spoiler;
			viewZuweisung(pn);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Speichert die aktuellen PN-Daten
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("PN", pn);
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

	public void viewZuweisung(PN pn) {
		TextView betreff = (TextView) findViewById(R.id.neue_pn_betreff);
		TextView datum = (TextView) findViewById(R.id.neue_pn_datum);
		TextView text = (TextView) findViewById(R.id.neue_pn_text);
		pnEingabe = (EditText) findViewById(R.id.neue_pn_textfeld);
		betreff.setText(pn.getBetreff());
		datum.setText(pn.getDate());
		String temp = pn.getText();
		temp = new EAParser(this).showSpoiler(spoiler, temp);
		temp = temp.replace("\r", "");
		temp = temp.replace("\t", "");
		temp = "<html><head></head><body> ----- " + pn.getBenutzername()
				+ " schrieb: ----- <br> " + temp + " </body></html>";
		text.setText(Html.fromHtml(temp));
		pn.setGelesen(true);
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class NewPNTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String input;
			netzwerk = Netzwerk.instance(getApplicationContext());
			eaParser = new EAParser(null);
			if (params[0].equals("")) {
				// Alte Nachricht soll abgerufen werden
				if (!send) {
					try {
						input = netzwerk.getPN(pn.getId());
						new NewsThread(getApplicationContext()).start();
						eaParser = new EAParser(null);
						pn = eaParser.getPN(pn, input);
						return null;
					} catch (EAException e) {
						publishProgress("Exception", e.getMessage());
					}
				}
				// Neue Nachricht soll abgeschickt werden
				else {
					try {
						String text = pnEingabe.getText().toString();
						String erfolg = netzwerk.answerPN(pn.getId(), text);
						eaParser = new EAParser(null);
						return eaParser.checkPN(erfolg);
					} catch (EAException e) {
						publishProgress("Exception", e.getMessage());
					}
				}
			} else if (params[0].equals("delete")) {
				try {
					netzwerk.deletePN(Integer.toString(pn.getId()));
					return "delete";
				} catch (EAException e) {
					publishProgress("Exception", e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Schließt den Ladedialog und öffnet die KommentarActivity
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
				if (!send) {
					viewZuweisung(pn);
				} else {
					Toast.makeText(getBaseContext(),
							"PN konnte nicht gesendet werden.",
							Toast.LENGTH_LONG).show();
				}
			} else if (result.equals("delete")) {
				Intent intent = new Intent(getApplicationContext(),
						de.btcdev.eliteanimesapp.gui.PNActivity.class);
				startActivity(intent);
			} else if (result.equals("Erfolg")) {
				Intent intent = new Intent(getApplicationContext(),
						de.btcdev.eliteanimesapp.gui.PNActivity.class);
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
