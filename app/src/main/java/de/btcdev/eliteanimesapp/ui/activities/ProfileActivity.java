package de.btcdev.eliteanimesapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.exceptions.EAException;
import de.btcdev.eliteanimesapp.data.exceptions.JsonErrorException;
import de.btcdev.eliteanimesapp.data.models.Profile;
import de.btcdev.eliteanimesapp.data.services.ProfileCache;
import de.btcdev.eliteanimesapp.data.services.ProfileService;

/**
 * Activity für die Anzeige des eigenen Profils.
 */
public class ProfileActivity extends ParentActivity implements
		OnItemClickListener {

	@Inject
	ProfileService profileService;

	private ProfileCache profileCache;
	private ImageView avatarView;
	private ProfileTask profileTask;

	/**
	 * Das UI wird erzeugt, NetworkService, Cache und Parser werden aus der
	 * configurationService geladen. Anschließend wird ein neuer ProfileTask gestartet,
	 * falls das Profile noch nicht vollständig ist. Falls doch, wird gleich
	 * fillViews aufgerufen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_profil);
		avatarView = (ImageView) findViewById(R.id.profilbild_eigenes);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Profile");
		actionBar.setSubtitle(configurationService.getUserName(getApplicationContext()));

		profileCache = ProfileCache.instance();
		Profile ownProfile = profileCache.getOwnProfile();
		if (ownProfile != null && ownProfile.isComplete())
			fillViews(ownProfile);
		else {
			profileTask = new ProfileTask();
			profileTask.execute("");
		}

		handleNavigationDrawer(R.id.nav_profil, R.id.nav_profil_list, "Profile",
				configurationService.getUserName(this));
	}

    @Override
    protected void injectDependencies() {
        ((EaApp) getApplication()).getEaComponent().inject(this);
    }

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * ProfileTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (profileTask != null) {
			profileTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.profil, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.profil_aktualisieren:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Die Views werden sichtbar gemacht und mit den übergebenen Informationen
	 * gefüllt.
	 * 
	 * @param newProfile
	 *            Profile, das angezeigt werden soll
	 */
	public void fillViews(Profile newProfile) {
		Profile profile = newProfile;
		if (profile != null) {
			TextView frag1 = (TextView) findViewById(R.id.profil_benutzername);
			TextView frag2 = (TextView) findViewById(R.id.profil_status);
			TextView frag3 = (TextView) findViewById(R.id.profil_gruppe);
			TextView frag4 = (TextView) findViewById(R.id.profil_geschlecht);
			TextView frag5 = (TextView) findViewById(R.id.profil_alter);
			TextView frag6 = (TextView) findViewById(R.id.profil_single);
			TextView frag7 = (TextView) findViewById(R.id.profil_wohnort);
			TextView frag8 = (TextView) findViewById(R.id.profil_dabei);
			TextView antw1 = (TextView) findViewById(R.id.profil_antw1);
			TextView antw2 = (TextView) findViewById(R.id.profil_antw2);
			TextView antw3 = (TextView) findViewById(R.id.profil_antw3);
			TextView antw4 = (TextView) findViewById(R.id.profil_antw4);
			TextView antw5 = (TextView) findViewById(R.id.profil_antw5);
			TextView antw6 = (TextView) findViewById(R.id.profil_antw6);
			TextView antw7 = (TextView) findViewById(R.id.profil_antw7);
			TextView antw8 = (TextView) findViewById(R.id.profil_antw8);
			frag1.setVisibility(View.VISIBLE);
			frag2.setVisibility(View.VISIBLE);
			frag3.setVisibility(View.VISIBLE);
			frag4.setVisibility(View.VISIBLE);
			frag5.setVisibility(View.VISIBLE);
			frag6.setVisibility(View.VISIBLE);
			frag7.setVisibility(View.VISIBLE);
			frag8.setVisibility(View.VISIBLE);
			antw1.setText(profile.getUserName());
			if (profile.isOnline())
				antw2.setText("Online");
			else
				antw2.setText("Offline");
			antw3.setText(profile.getGroup());
			antw4.setText(profile.getSex());
			antw5.setText(profile.getAge());
			antw6.setText(profile.getSingle());
			antw7.setText(profile.getResidence());
			antw8.setText(profile.getRegisteredSince());
			avatarView.setImageBitmap(profile.getAvatar());
			avatarView.setAdjustViewBounds(true);
			avatarView.setMaxHeight(200);
			avatarView.setMaxWidth(200);

			LinearLayout linkContent = (LinearLayout) findViewById(R.id.profil_link_content);
			linkContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Aktualisiert die Activity, indem alle Daten neu geladen werden.
	 */
	public void refresh() {
		profileTask = new ProfileTask();
		profileTask.execute("");
	}

	/**
	 * Behandelt einen Klick auf die Liste unter dem Profile und im Navigation
	 * Drawer. Dabei wird die entsprechende Activity per Intent mit den
	 * erforderlichen Informationen gestartet.
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
		if (arg0.getId() == R.id.nav_profil_list) {
			if (arg2 == NAVIGATION_PROFILE) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} else {
				super.onItemClick(arg0, arg1, arg2, arg3);
			}
		}
	}

	public void onNavigationClick(View v) {
		if (v.getId() == R.id.profil_beschreibung) {
			Intent intent = new Intent(
					this,
					ProfileDescriptionActivity.class);
			intent.putExtra("User",
					configurationService.getUserName(getApplicationContext()));
			intent.putExtra("UserID",
					configurationService.getUserID(getApplicationContext()));
			startActivity(intent);
		} else if (v.getId() == R.id.profil_kommentare) {
			Intent intent = new Intent(this,
					CommentActivity.class);
			intent.putExtra("User",
					configurationService.getUserName(getApplicationContext()));
			intent.putExtra("UserID",
					configurationService.getUserID(getApplicationContext()));
			startActivity(intent);
		} else if (v.getId() == R.id.profil_freunde) {
			Intent intent = new Intent(this,
					FriendActivity.class);
			intent.putExtra("User",
					configurationService.getUserName(getApplicationContext()));
			intent.putExtra("UserID",
					configurationService.getUserID(getApplicationContext()));
			startActivity(intent);

		} else if (v.getId() == R.id.profil_animeliste) {
			Intent intent = new Intent(this,
					AnimeListActivity.class);
			intent.putExtra("User",
					configurationService.getUserName(getApplicationContext()));
			intent.putExtra("UserID",
					configurationService.getUserID(getApplicationContext()));
			startActivity(intent);
		} else if (v.getId() == R.id.profil_pns) {
			Intent intent = new Intent(this,
					PrivateMessageActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class ProfileTask extends AsyncTask<String, String, Profile> {

		boolean loginError = false;

		/**
		 * Lädt im Hintergrund übers NetworkService die Profilinformationen und parst
		 * sie noch den gewünschten Daten. Gibt ein Profile mit den erhaltenen
		 * Daten zurück.
		 * 
		 * @param params
		 *            String-Array mit Informationen - hier irrelevant
		 * @return Profile, mit den geladenen Daten
		 * @throws EAException
		 *             bei Verbindungs- und Streamfehlern jeglicher Art
		 */
		@Override
		protected Profile doInBackground(String... params) {
			final String input;
			Profile profile;
			try {
				if (this.isCancelled())
					return null;
				if (configurationService.getBoardToken() == null) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								loginService.getToken();
							} catch (EAException e) {

							}
						}
					});
					t.start();
				}
                getNotifications();
				try {
                    profile = profileService.getProfile();
				} catch (JsonErrorException ex) {
					if (ex != null && ex.getMessage() != null) {
						if (ex.getMessage().equals("You need to Login"))
							loginError = true;
					} else {
						loginError = true;
					}
					return null;
				}
				if (this.isCancelled())
					return null;
				return profile;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;

		}

		/**
		 * Die fillViews wird mit den erhaltenen Daten aufgerufen, der
		 * LoadDialog wird geschlossen.
		 * 
		 * @param profile
		 *            das Profile mit den erhaltenen Daten
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Profile profile) {
			if (loginError) {
				networkService.deleteCookies();
				Toast.makeText(getApplicationContext(),
						"Bitte erneut einloggen.", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getApplicationContext(),
						LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			fillViews(profile);
			navDrawerListAdapter.notifyDataSetChanged();
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
		 * @param profile
		 *            irrelevant
		 */
		@Override
		protected void onCancelled(Profile profile) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
