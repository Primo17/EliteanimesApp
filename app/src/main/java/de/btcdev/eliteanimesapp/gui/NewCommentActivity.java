package de.btcdev.eliteanimesapp.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.Comment;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;

public class NewCommentActivity extends ParentActivity implements
		OnItemClickListener {

	@Inject
	NetworkService networkService;

	private EditText commentInputView;
	private String currentUser;
	private int userId;
	private boolean response;
	private Comment responseComment = null;
	private NewCommentTask newCommentTask;
	private Comment editedComment;
	private String status;
	private String commentInput;

	/**
	 * Erzeugt die Activity, liest User und UserID aus.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((EaApp) getApplication()).getEaComponent().inject(this);
		setContentView(R.layout.activity_neuer_kommentar);
		commentInputView = (EditText) findViewById(R.id.new_comment_text);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Neuer Comment");
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		currentUser = bundle.getString("User");
		userId = bundle.getInt("UserID");
		status = bundle.getString("Status");
		response = bundle.getBoolean("Response", false);
		if (response) {
			responseComment = bundle.getParcelable("Comment");
			LayoutInflater inflater = getLayoutInflater();
			LinearLayout root = (LinearLayout) findViewById(R.id.new_comment_root);
			View commentLayoutView = inflater.inflate(R.layout.kommentar_layout, root,
					false);
			TextView name = (TextView) commentLayoutView.findViewById(R.id.comment_name);
			TextView date = (TextView) commentLayoutView.findViewById(R.id.comment_date);
			ImageView avatar = (ImageView) commentLayoutView
					.findViewById(R.id.comment_img);
			TextView text = (TextView) commentLayoutView.findViewById(R.id.comment_text);
			name.setText(responseComment.getUserName());
			date.setText(responseComment.getDate());
			text.setText(Html.fromHtml(responseComment.getText()));
			avatar.setImageBitmap(responseComment.getAvatar());
			commentLayoutView.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.borderback));
			root.addView(commentLayoutView, 0);
		}
		actionBar.setSubtitle(currentUser);
		if (status.equals("Editieren")) {
			actionBar.setTitle("Comment editieren");
			editedComment = bundle.getParcelable("Comment");
			commentInputView.setText(Html.fromHtml(editedComment.getText()));
		}
		handleNavigationDrawer(R.id.nav_neuer_kommentar,
				R.id.nav_neuer_kommentar_list, "Neuer Comment", currentUser);
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
		getMenuInflater().inflate(R.menu.neuer_kommentar, menu);
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
		case R.id.new_comment_send:
			commentInput = commentInputView.getText().toString();
			newCommentTask = new NewCommentTask();
			newCommentTask.execute("");
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
		if (arg0.getId() == R.id.nav_neuer_kommentar_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class NewCommentTask extends AsyncTask<String, String, String> {

		/**
		 * Schickt die Eingabe aus dem Textfeld als neuen bzw editierten
		 * Comment ab.
		 * 
		 * @param params
		 *            String-Array mit Informationen - hier irrelevant
		 * @return String - irrelevant
		 */
		@Override
		protected String doInBackground(String... params) {
			boolean send = false;
			try {
				if (status.equals("Editieren")) {
					editedComment.setText(commentInput);
					networkService.editComment(editedComment, currentUser, userId);
					send = true;
				} else {
					if (isCancelled())
						return null;
					send = networkService.postComment(commentInput, currentUser, userId);
				}
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			if (send)
				return "Erfolg";
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
			if (result != null && result.equals("Erfolg")) {
				Intent intent = new Intent(getApplicationContext(),
						CommentActivity.class);
				intent.putExtra("User", currentUser);
				intent.putExtra("UserID", userId);
				intent.putExtra("Send", true);
				startActivity(intent);
			} else {
				Toast.makeText(getBaseContext(),
						"Comment konnte nicht gesendet werden.",
						Toast.LENGTH_LONG).show();
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
