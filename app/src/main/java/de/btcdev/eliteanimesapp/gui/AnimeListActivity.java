package de.btcdev.eliteanimesapp.gui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.ListAnimeAdapter;
import de.btcdev.eliteanimesapp.adapter.OnAnimeRatedListener;
import de.btcdev.eliteanimesapp.cache.AnimelistCacheThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Configuration;
import de.btcdev.eliteanimesapp.data.ListAnime;
import de.btcdev.eliteanimesapp.data.ListAnimeAlphabetComparator;
import de.btcdev.eliteanimesapp.data.ListAnimeRatingComparator;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.NewsThread;
import de.btcdev.eliteanimesapp.json.ListAnimeDeserializer;

public class AnimeListActivity extends ParentActivity implements
		OnItemClickListener, OnItemSelectedListener, OnAnimeRatedListener {

	private String aktuellerUser;
	private int userID;
	private ArrayList<ListAnime> komplett;
	private ArrayList<ListAnime> amSchauen;
	private ArrayList<ListAnime> kurzAufgehoert;
	private ArrayList<ListAnime> abgebrochen;
	private ArrayList<ListAnime> geplant;
	private AnimelistTask animelistTask;
	private ListAnimeAdapter animeAdapter;

	private enum AnimelistSelection {
		KOMPLETT, AMSCHAUEN, KURZAUFGEHOERT, ABGEBROCHEN, GEPLANT
	};

	private AnimelistSelection animelistSelection;

	private enum AnimelistSort {
		BEWERTUNG, ALPHABET
	};

	private AnimelistSort animelistSort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anime_list);
		ActionBar bar = getSupportActionBar();
		networkService = NetworkService.instance(this);
		eaParser = new EAParser(this);
		if (savedInstanceState != null) {
			aktuellerUser = savedInstanceState.getString("User");
			userID = savedInstanceState.getInt("UserID");
			komplett = savedInstanceState.getParcelableArrayList("Komplett");
			amSchauen = savedInstanceState.getParcelableArrayList("AmSchauen");
			kurzAufgehoert = savedInstanceState
					.getParcelableArrayList("KurzAufgehoert");
			abgebrochen = savedInstanceState
					.getParcelableArrayList("Abgebrochen");
			geplant = savedInstanceState.getParcelableArrayList("Geplant");
			animelistSelection = (AnimelistSelection) savedInstanceState
					.getSerializable("Selection");
			animelistSort = (AnimelistSort) savedInstanceState
					.getSerializable("Sort");
			viewZuweisung();
		} else {
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				aktuellerUser = bundle.getString("User");
				userID = bundle.getInt("UserID");
				bar.setSubtitle(aktuellerUser);
			}
			animelistTask = new AnimelistTask();
			if (aktuellerUser.equals(Configuration
					.getUserName(getApplicationContext())))
				animelistTask.ownList = true;
			else
				animelistTask.ownList = false;
			animelistTask.execute("");
		}
		handleNavigationDrawer(R.id.nav_animelist, R.id.nav_animelist_list,
				"Animeliste", aktuellerUser);
	}

	/**
	 * Speichert den aktuellen User, seine UserID und seine Animelisten
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("User", aktuellerUser);
		savedInstanceState.putInt("UserID", userID);
		savedInstanceState.putParcelableArrayList("Komplett", komplett);
		savedInstanceState.putParcelableArrayList("AmSchauen", amSchauen);
		savedInstanceState.putParcelableArrayList("KurzAufgehoert",
				kurzAufgehoert);
		savedInstanceState.putParcelableArrayList("Abgebrochen", abgebrochen);
		savedInstanceState.putParcelableArrayList("Geplant", geplant);
		savedInstanceState.putSerializable("Selection", animelistSelection);
		savedInstanceState.putSerializable("Sort", animelistSort);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.anime_list, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * AnimelistTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (animelistTask != null) {
			animelistTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.animelist_aktualisieren:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.animelist_listSelectionSpinner:
			switch (position) {
			case 0:
				animelistSelection = AnimelistSelection.KOMPLETT;
				break;
			case 1:
				animelistSelection = AnimelistSelection.AMSCHAUEN;
				break;
			case 2:
				animelistSelection = AnimelistSelection.KURZAUFGEHOERT;
				break;
			case 3:
				animelistSelection = AnimelistSelection.ABGEBROCHEN;
				break;
			case 4:
				animelistSelection = AnimelistSelection.GEPLANT;
				break;
			}
			viewZuweisung();
			break;
		case R.id.animelist_listSortSpinner:
			switch (position) {
			case 0:
				animelistSort = AnimelistSort.BEWERTUNG;
				ListAnimeRatingComparator comp = new ListAnimeRatingComparator();
				Collections.sort(komplett, comp);
				Collections.sort(amSchauen, comp);
				Collections.sort(kurzAufgehoert, comp);
				Collections.sort(abgebrochen, comp);
				Collections.sort(geplant, comp);
				break;
			case 1:
				animelistSort = AnimelistSort.ALPHABET;
				ListAnimeAlphabetComparator comparator = new ListAnimeAlphabetComparator();
				Collections.sort(komplett, comparator);
				Collections.sort(amSchauen, comparator);
				Collections.sort(kurzAufgehoert, comparator);
				Collections.sort(abgebrochen, comparator);
				Collections.sort(geplant, comparator);
				break;
			}
			viewZuweisung();
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}

	/**
	 * Sortiert alle Animelisten nach der ausgewählten Methode.
	 */
	public void sortLists() {
		if (animelistSort == AnimelistSort.BEWERTUNG) {
			ListAnimeRatingComparator comp = new ListAnimeRatingComparator();
			Collections.sort(komplett, comp);
			Collections.sort(amSchauen, comp);
			Collections.sort(kurzAufgehoert, comp);
			Collections.sort(abgebrochen, comp);
			Collections.sort(geplant, comp);
		}
		if (animelistSort == AnimelistSort.ALPHABET) {
			ListAnimeAlphabetComparator comparator = new ListAnimeAlphabetComparator();
			Collections.sort(komplett, comparator);
			Collections.sort(amSchauen, comparator);
			Collections.sort(kurzAufgehoert, comparator);
			Collections.sort(abgebrochen, comparator);
			Collections.sort(geplant, comparator);
		}
	}

	/**
	 * Stellt die ausgewählte Animeliste in der ListView dar und setzt evtl
	 * Default-Werte für die Spinner.
	 */
	public void viewZuweisung() {
		Spinner animelistSelectionSpinner = (Spinner) findViewById(R.id.animelist_listSelectionSpinner);
		Spinner animelistSortSpinner = (Spinner) findViewById(R.id.animelist_listSortSpinner);
		animelistSelectionSpinner.setOnItemSelectedListener(this);
		animelistSortSpinner.setOnItemSelectedListener(this);
		// setze default-wert aus Einstellungen
		SharedPreferences defaultprefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (animelistSelection == null) {
			String category = defaultprefs.getString("pref_animelist_category",
					"Komplett");
			if (category.equals("Komplett")) {
				animelistSelection = AnimelistSelection.KOMPLETT;
				animelistSelectionSpinner.setSelection(0);
			} else if (category.equals("Am Schauen")) {
				animelistSelection = AnimelistSelection.AMSCHAUEN;
				animelistSelectionSpinner.setSelection(1);
			} else if (category.equals("Kurz Aufgehört")) {
				animelistSelection = AnimelistSelection.KURZAUFGEHOERT;
				animelistSelectionSpinner.setSelection(2);
			} else if (category.equals("Abgebrochen")) {
				animelistSelection = AnimelistSelection.ABGEBROCHEN;
				animelistSelectionSpinner.setSelection(3);
			} else if (category.equals("Geplant")) {
				animelistSelection = AnimelistSelection.GEPLANT;
				animelistSelectionSpinner.setSelection(4);
			}
		}
		if (animelistSort == null) {
			String sort = defaultprefs.getString("pref_animelist_sort",
					"Bewertung");
			if (sort.equals("Bewertung")) {
				animelistSort = AnimelistSort.BEWERTUNG;
				animelistSortSpinner.setSelection(0);
			} else if (sort.equals("Alphabet")) {
				animelistSort = AnimelistSort.ALPHABET;
				animelistSortSpinner.setSelection(1);
			}
		}
		// zeige die gewünschte liste an
		ListView animelistView = (ListView) findViewById(R.id.animelist_list);
		animelistView.setOnItemClickListener(this);
		switch (animelistSelection) {
		case KOMPLETT:
			animeAdapter = new ListAnimeAdapter(this, komplett);
			break;
		case AMSCHAUEN:
			animeAdapter = new ListAnimeAdapter(this, amSchauen);
			break;
		case KURZAUFGEHOERT:
			animeAdapter = new ListAnimeAdapter(this, kurzAufgehoert);
			break;
		case ABGEBROCHEN:
			animeAdapter = new ListAnimeAdapter(this, abgebrochen);
			break;
		case GEPLANT:
			animeAdapter = new ListAnimeAdapter(this, geplant);
			break;
		default:
			// unmöglicher Fall, den der Compiler trotzdem haben will
			animeAdapter = new ListAnimeAdapter(this, null);
			break;
		}
		animelistView.setAdapter(animeAdapter);
	}

	/**
	 * Lädt die Animeliste neu aus dem Internet über den AnimelistTask.
	 */
	public void refresh() {
		animelistTask = new AnimelistTask();
		if (aktuellerUser.equals(Configuration
				.getUserName(getApplicationContext())))
			animelistTask.ownList = true;
		else
			animelistTask.ownList = false;
		animelistTask.execute("no_cache");
	}

	/**
	 * Ereignisbehandlung des Navigation Drawers
	 * 
	 * @param arg0
	 *            betroffener Adapter
	 * @param arg1
	 *            betroffenes View-Element
	 * @param arg2
	 *            Position des Elements im Adapter
	 * @param arg3
	 *            irgendwas hier unwichtiges
	 */
	@SuppressLint("RtlHardcoded")
    @Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.getId() == R.id.nav_animelist_list) {
			if (arg2 == navigation_animeliste) {
				if (aktuellerUser.equals(Configuration
						.getUserName(getApplicationContext())))
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				else {
					Intent intent = new Intent(
							this,
							de.btcdev.eliteanimesapp.gui.AnimeListActivity.class);
					intent.putExtra("User", Configuration
							.getUserName(getApplicationContext()));
					intent.putExtra("UserID",
							Configuration.getUserID(getApplicationContext()));
					mDrawerLayout.closeDrawer(Gravity.LEFT);
					startActivity(intent);
				}
			} else
				super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.animelist_list) {
			if (aktuellerUser.equals(Configuration
					.getUserName(getApplicationContext()))) {
				ListAnime selectedAnime;
				int status;
				switch (animelistSelection) {
				case KOMPLETT:
					selectedAnime = komplett.get(arg2);
					status = 2;
					break;
				case AMSCHAUEN:
					selectedAnime = amSchauen.get(arg2);
					status = 1;
					break;
				case KURZAUFGEHOERT:
					selectedAnime = kurzAufgehoert.get(arg2);
					status = 3;
					break;
				case ABGEBROCHEN:
					selectedAnime = abgebrochen.get(arg2);
					status = 4;
					break;
				case GEPLANT:
					selectedAnime = geplant.get(arg2);
					status = 5;
					break;
				default:
					selectedAnime = new ListAnime();
					status = 0;
					break;
				}
				// übergebe Infos an Dialog
				DialogFragment dialog = new AnimeRatingDialogFragment();
				Bundle bundle = new Bundle();
				bundle.putParcelable("anime", selectedAnime);
				bundle.putInt("status", status);
				dialog.setArguments(bundle);
				dialog.show(getSupportFragmentManager(),
						"AnimeRatingDialogFragment");
			}
		}
	}

	public class AnimelistTask extends AsyncTask<String, String, String> {

		public boolean ownList = false;

		@Override
		protected String doInBackground(String... params) {
			String input = null;
			eaParser = new EAParser(null);
			try {
				// Cache explizit nicht gewünscht!
				if (params[0].equals("no_cache") || !ownList) {
					if (this.isCancelled())
						return null;
					new NewsThread(getApplicationContext()).start();
					if (this.isCancelled())
						return null;
					input = networkService.getAnimeList(aktuellerUser, userID);
					if (this.isCancelled())
						return null;
					komplett = new ArrayList<ListAnime>();
					amSchauen = new ArrayList<ListAnime>();
					kurzAufgehoert = new ArrayList<ListAnime>();
					abgebrochen = new ArrayList<ListAnime>();
					geplant = new ArrayList<ListAnime>();
					if (this.isCancelled())
						return null;
					eaParser.getListAnime(input, komplett, amSchauen,
							kurzAufgehoert, abgebrochen, geplant, ownList);
					if (ownList)
						new AnimelistCacheThread(
								AnimelistCacheThread.MODE_SAVE_CACHE, komplett,
								amSchauen, kurzAufgehoert, abgebrochen, geplant);
				}
				// eigene Liste -> Cache kommt in Betracht
				else {
					// teste ob Cache
					if (!loadCache()) {
						input = networkService.getAnimeList(aktuellerUser, userID);
						komplett = new ArrayList<ListAnime>();
						amSchauen = new ArrayList<ListAnime>();
						kurzAufgehoert = new ArrayList<ListAnime>();
						abgebrochen = new ArrayList<ListAnime>();
						geplant = new ArrayList<ListAnime>();
						eaParser.getListAnime(input, komplett, amSchauen,
								kurzAufgehoert, abgebrochen, geplant, ownList);
						new AnimelistCacheThread(
								AnimelistCacheThread.MODE_SAVE_CACHE, komplett,
								amSchauen, kurzAufgehoert, abgebrochen, geplant);
					}
				}
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return "";
		}

		/**
		 * öffnet einen Lade-Dialog.
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
		 * Der Lade-Dialog wird geschlossen und die viewZuweisung aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			viewZuweisung();
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
		 * Der Task wird abgebrochen.
		 */
		@Override
		protected void onCancelled() {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onCancelled();
		}

		/**
		 * Lädt die gecachte Animeliste aus dem Speicher.
		 */
		public boolean loadCache() {
			// Gibt es überhaupt Speicherstände eines Benutzers?
			SharedPreferences prefs = getSharedPreferences("cache",
					Context.MODE_PRIVATE);
			if (prefs.contains("lastUser")) {
				// Gibt es einen AnimelistCache?
				if (prefs.contains("AnimelistCache")) {
					// ist der Cache vom aktuellen User?
					if (prefs.getString("lastUser", "").equals(
							Configuration
									.getUserName(getApplicationContext()))) {
						komplett = new ArrayList<ListAnime>();
						amSchauen = new ArrayList<ListAnime>();
						kurzAufgehoert = new ArrayList<ListAnime>();
						abgebrochen = new ArrayList<ListAnime>();
						geplant = new ArrayList<ListAnime>();
						// lese Cache aus und speicher in Configuration
						String jsonKomplett = prefs.getString(
								"AnimelistCacheKomplett", "");
						String jsonAmSchauen = prefs.getString(
								"AnimelistCacheAmSchauen", "");
						String jsonKurzAufgehoert = prefs.getString(
								"AnimelistCacheKurzAufgehoert", "");
						String jsonAbgebrochen = prefs.getString(
								"AnimelistCacheAbgebrochen", "");
						String jsonGeplant = prefs.getString(
								"AnimelistCacheGeplant", "");
						if (!jsonKomplett.equals("")
								&& !jsonAmSchauen.equals("")
								&& !jsonKurzAufgehoert.equals("")
								&& !jsonAbgebrochen.equals("")
								&& !jsonGeplant.equals("")) {
							// Konvertiere JSON-Strings zurück zu ArrayLists aus
							// ListAnime-Objekten
							// und setze Cache in Configuration
							try {
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(ListAnime.class,
												new ListAnimeDeserializer())
										.create();
								Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
								}.getType();
								komplett = gson.fromJson(jsonKomplett,
										collectionType);
								amSchauen = gson.fromJson(jsonAmSchauen,
										collectionType);
								kurzAufgehoert = gson.fromJson(
										jsonKurzAufgehoert, collectionType);
								abgebrochen = gson.fromJson(jsonAbgebrochen,
										collectionType);
								geplant = gson.fromJson(jsonGeplant,
										collectionType);
								return true;
							} catch (JsonParseException e) {
								// lösche vorhanden Cache
								SharedPreferences.Editor editor = prefs.edit();
								editor.remove("AnimelistCache");
								editor.apply();
								return false;
							}
						}
					} else {
						// lösche vorhanden Cache
						SharedPreferences.Editor editor = prefs.edit();
						editor.remove("AnimelistCache");
						editor.apply();
						return false;
					}
				}
			}
			return false;
		}
	}

	public static class AnimeRatingDialogFragment extends DialogFragment {

		ListAnime anime;
		int status;
		Spinner scoreSpinner;
		Spinner categorySpinner;
		EditText fortschritt;
		EditText folgenanzahl;
		OnAnimeRatedListener ratedListener;

		public AnimeRatingDialogFragment() {

		}

		@Override
		public void onAttach(Activity activity) {
			try {
				ratedListener = (OnAnimeRatedListener) activity;
			} catch (ClassCastException e) {

			} finally {
				super.onAttach(activity);
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle bundle = getArguments();
			anime = bundle.getParcelable("anime");
			status = bundle.getInt("status");
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(anime.getTitle());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			View view = inflater.inflate(R.layout.dialog_animelist_rate, null);
			scoreSpinner = (Spinner) view
					.findViewById(R.id.dialog_animelist_rate_score);
			ArrayList<String> scores = new ArrayList<String>();
			scores.add("Keine Bewertung");
			double i = 10.0;
			while (i >= 1) {
				scores.add(String.format(Locale.US, "%.1f", i));
				i = i - 0.1;
			}
			ArrayAdapter<String> scoreAdapter = new ArrayAdapter<String>(
					view.getContext(), android.R.layout.simple_spinner_item,
					scores);
			scoreAdapter
					.setDropDownViewResource(R.layout.dialog_animelist_rate_dropdown);
			scoreSpinner.setAdapter(scoreAdapter);
			if (anime.getRating() == 0.0)
				scoreSpinner.setSelection(0);
			else {
				scoreSpinner
						.setSelection((int) Math.abs((anime.getRating() * 10) - 101));
			}
			categorySpinner = (Spinner) view
					.findViewById(R.id.dialog_animelist_rate_category);
			categorySpinner.setSelection(status - 1);
			fortschritt = (EditText) view
					.findViewById(R.id.dialog_animelist_rate_fortschritt);
			fortschritt.setText("" + anime.getProgress());
			folgenanzahl = (EditText) view
					.findViewById(R.id.dialog_animelist_rate_folgenzahl);
			folgenanzahl.setText("" + anime.getEpisodeCount());
			builder.setView(view)
					// Add action buttons
					.setPositiveButton("Bewerten",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// rate anime
									final double score;
									if (scoreSpinner.getSelectedItemPosition() == 0) {
										score = 0.0;
									} else {
										score = Double
												.parseDouble((String) scoreSpinner
														.getSelectedItem());
									}
									final int seen = Integer
											.parseInt(fortschritt.getText()
													.toString());
									final int statusNeu = categorySpinner
											.getSelectedItemPosition() + 1;
									new Thread(new Runnable() {
										public void run() {
											try {
												NetworkService.instance(getActivity())
														.rateAnime(
																anime.getId(),
																score,
																seen,
																anime.getEpisodeCount(),
																statusNeu);
											} catch (EAException e) {

											}
										}
									}).start();
									anime.setRating(score);
									anime.setProgress(seen);
									ratedListener.onAnimeRatingComplete(anime,
											status, statusNeu);
								}
							})
					.setNegativeButton("Abbrechen",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									AnimeRatingDialogFragment.this.getDialog()
											.cancel();
								}
							});
			return builder.create();
		}
	}

	/**
	 * Wird aufgerufen, wenn der AnimeRatingDialog mit dem Bewerten-Button
	 * geschlossen wurde. Setzt den gewählten Anime wenn nötig in seine neue
	 * Liste und aktualisiert die Darstellung.
	 */
	@Override
	public void onAnimeRatingComplete(ListAnime anime, int oldStatus, int newStatus) {
		// aktualisiere Liste
		if (oldStatus != newStatus) {
			switch (oldStatus) {
			case 1:
				amSchauen.remove(anime);
				break;
			case 2:
				komplett.remove(anime);
				break;
			case 3:
				kurzAufgehoert.remove(anime);
				break;
			case 4:
				abgebrochen.remove(anime);
				break;
			case 5:
				abgebrochen.remove(anime);
				break;
			}
			switch (newStatus) {
			case 1:
				amSchauen.add(anime);
				break;
			case 2:
				komplett.add(anime);
				break;
			case 3:
				kurzAufgehoert.add(anime);
				break;
			case 4:
				abgebrochen.add(anime);
				break;
			case 5:
				abgebrochen.add(anime);
				break;
			}
		}
		sortLists();
		animeAdapter.notifyDataSetChanged();
		new AnimelistCacheThread(AnimelistCacheThread.MODE_SAVE_CACHE,
				komplett, amSchauen, kurzAufgehoert, abgebrochen, geplant);
	}
}
