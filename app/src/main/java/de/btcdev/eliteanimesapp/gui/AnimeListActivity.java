package de.btcdev.eliteanimesapp.gui;

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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.ListAnimeAdapter;
import de.btcdev.eliteanimesapp.adapter.OnAnimeRatedListener;
import de.btcdev.eliteanimesapp.cache.AnimelistCacheThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.ListAnime;
import de.btcdev.eliteanimesapp.data.ListAnimeAlphabetComparator;
import de.btcdev.eliteanimesapp.data.ListAnimeRatingComparator;
import de.btcdev.eliteanimesapp.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.services.AnimeService;

public class AnimeListActivity extends ParentActivity implements
		OnItemClickListener, OnItemSelectedListener, OnAnimeRatedListener {

	@Inject
	AnimeService animeService;

	private String currentUser;
	private int userId;
	private ArrayList<ListAnime> completeAnime;
	private ArrayList<ListAnime> watchingAnime;
	private ArrayList<ListAnime> stalledAnime;
	private ArrayList<ListAnime> droppedAnime;
	private ArrayList<ListAnime> plannedAnime;
	private AnimelistTask animelistTask;
	private ListAnimeAdapter listAnimeAdapter;

	private enum AnimelistSelection {
		COMPLETE, WATCHING, STALLED, DROPPED, PLANNED
	};

	private AnimelistSelection animelistSelection;

	private enum AnimelistSort {
		BY_RATING, ALPHABETICAL
	};

	private AnimelistSort AnimelistSort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_anime_list);
		ActionBar bar = getSupportActionBar();
		if (savedInstanceState != null) {
			currentUser = savedInstanceState.getString("User");
			userId = savedInstanceState.getInt("UserID");
			completeAnime = savedInstanceState.getParcelableArrayList("Komplett");
			watchingAnime = savedInstanceState.getParcelableArrayList("AmSchauen");
			stalledAnime = savedInstanceState
					.getParcelableArrayList("KurzAufgehoert");
			droppedAnime = savedInstanceState
					.getParcelableArrayList("Abgebrochen");
			plannedAnime = savedInstanceState.getParcelableArrayList("Geplant");
			animelistSelection = (AnimelistSelection) savedInstanceState
					.getSerializable("Selection");
			AnimelistSort = (AnimelistSort) savedInstanceState
					.getSerializable("Sort");
			fillViews();
		} else {
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				currentUser = bundle.getString("User");
				userId = bundle.getInt("UserID");
				bar.setSubtitle(currentUser);
			}
			animelistTask = new AnimelistTask();
			if (currentUser.equals(configurationService
					.getUserName(getApplicationContext())))
				animelistTask.ownList = true;
			else
				animelistTask.ownList = false;
			animelistTask.execute("");
		}
		handleNavigationDrawer(R.id.nav_animelist, R.id.nav_animelist_list,
				"Animeliste", currentUser);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	/**
	 * Speichert den aktuellen User, seine UserID und seine Animelisten
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("User", currentUser);
		savedInstanceState.putInt("UserID", userId);
		savedInstanceState.putParcelableArrayList("Komplett", completeAnime);
		savedInstanceState.putParcelableArrayList("AmSchauen", watchingAnime);
		savedInstanceState.putParcelableArrayList("KurzAufgehoert",
				stalledAnime);
		savedInstanceState.putParcelableArrayList("Abgebrochen", droppedAnime);
		savedInstanceState.putParcelableArrayList("Geplant", plannedAnime);
		savedInstanceState.putSerializable("Selection", animelistSelection);
		savedInstanceState.putSerializable("Sort", AnimelistSort);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action actionBar if it is present.
		getMenuInflater().inflate(R.menu.anime_list, menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * AnimelistTask wird dabei droppedAnime.
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
				animelistSelection = AnimelistSelection.COMPLETE;
				break;
			case 1:
				animelistSelection = AnimelistSelection.WATCHING;
				break;
			case 2:
				animelistSelection = AnimelistSelection.STALLED;
				break;
			case 3:
				animelistSelection = AnimelistSelection.DROPPED;
				break;
			case 4:
				animelistSelection = AnimelistSelection.PLANNED;
				break;
			}
			fillViews();
			break;
		case R.id.animelist_listSortSpinner:
			switch (position) {
			case 0:
				AnimelistSort = AnimelistSort.BY_RATING;
				ListAnimeRatingComparator comp = new ListAnimeRatingComparator();
				Collections.sort(completeAnime, comp);
				Collections.sort(watchingAnime, comp);
				Collections.sort(stalledAnime, comp);
				Collections.sort(droppedAnime, comp);
				Collections.sort(plannedAnime, comp);
				break;
			case 1:
				AnimelistSort = AnimelistSort.ALPHABETICAL;
				ListAnimeAlphabetComparator comparator = new ListAnimeAlphabetComparator();
				Collections.sort(completeAnime, comparator);
				Collections.sort(watchingAnime, comparator);
				Collections.sort(stalledAnime, comparator);
				Collections.sort(droppedAnime, comparator);
				Collections.sort(plannedAnime, comparator);
				break;
			}
			fillViews();
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
		if (AnimelistSort == AnimelistSort.BY_RATING) {
			ListAnimeRatingComparator comp = new ListAnimeRatingComparator();
			Collections.sort(completeAnime, comp);
			Collections.sort(watchingAnime, comp);
			Collections.sort(stalledAnime, comp);
			Collections.sort(droppedAnime, comp);
			Collections.sort(plannedAnime, comp);
		}
		if (AnimelistSort == AnimelistSort.ALPHABETICAL) {
			ListAnimeAlphabetComparator comparator = new ListAnimeAlphabetComparator();
			Collections.sort(completeAnime, comparator);
			Collections.sort(watchingAnime, comparator);
			Collections.sort(stalledAnime, comparator);
			Collections.sort(droppedAnime, comparator);
			Collections.sort(plannedAnime, comparator);
		}
	}

	/**
	 * Stellt die ausgewählte Animeliste in der ListView dar und setzt evtl
	 * Default-Werte für die Spinner.
	 */
	public void fillViews() {
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
				animelistSelection = AnimelistSelection.COMPLETE;
				animelistSelectionSpinner.setSelection(0);
			} else if (category.equals("Am Schauen")) {
				animelistSelection = AnimelistSelection.WATCHING;
				animelistSelectionSpinner.setSelection(1);
			} else if (category.equals("Kurz Aufgehört")) {
				animelistSelection = AnimelistSelection.STALLED;
				animelistSelectionSpinner.setSelection(2);
			} else if (category.equals("Abgebrochen")) {
				animelistSelection = AnimelistSelection.DROPPED;
				animelistSelectionSpinner.setSelection(3);
			} else if (category.equals("Geplant")) {
				animelistSelection = AnimelistSelection.PLANNED;
				animelistSelectionSpinner.setSelection(4);
			}
		}
		if (AnimelistSort == null) {
			String sort = defaultprefs.getString("pref_animelist_sort",
					"Bewertung");
			if (sort.equals("Bewertung")) {
				AnimelistSort = AnimelistSort.BY_RATING;
				animelistSortSpinner.setSelection(0);
			} else if (sort.equals("Alphabet")) {
				AnimelistSort = AnimelistSort.ALPHABETICAL;
				animelistSortSpinner.setSelection(1);
			}
		}
		// zeige die gewünschte liste an
		ListView animelistView = (ListView) findViewById(R.id.animelist_list);
		animelistView.setOnItemClickListener(this);
		switch (animelistSelection) {
		case COMPLETE:
			listAnimeAdapter = new ListAnimeAdapter(this, completeAnime);
			break;
		case WATCHING:
			listAnimeAdapter = new ListAnimeAdapter(this, watchingAnime);
			break;
		case STALLED:
			listAnimeAdapter = new ListAnimeAdapter(this, stalledAnime);
			break;
		case DROPPED:
			listAnimeAdapter = new ListAnimeAdapter(this, droppedAnime);
			break;
		case PLANNED:
			listAnimeAdapter = new ListAnimeAdapter(this, plannedAnime);
			break;
		default:
			// unmöglicher Fall, den der Compiler trotzdem haben will
			listAnimeAdapter = new ListAnimeAdapter(this, null);
			break;
		}
		animelistView.setAdapter(listAnimeAdapter);
	}

	/**
	 * Lädt die Animeliste neu aus dem Internet über den AnimelistTask.
	 */
	public void refresh() {
		animelistTask = new AnimelistTask();
		if (currentUser.equals(configurationService
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
			if (arg2 == NAVIGATION_ANIMELIST) {
				if (currentUser.equals(configurationService
						.getUserName(getApplicationContext())))
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				else {
					Intent intent = new Intent(
							this,
							de.btcdev.eliteanimesapp.gui.AnimeListActivity.class);
					intent.putExtra("User", configurationService
							.getUserName(getApplicationContext()));
					intent.putExtra("UserID",
							configurationService.getUserID(getApplicationContext()));
					mDrawerLayout.closeDrawer(Gravity.LEFT);
					startActivity(intent);
				}
			} else
				super.onItemClick(arg0, arg1, arg2, arg3);
		} else if (arg0.getId() == R.id.animelist_list) {
			if (currentUser.equals(configurationService
					.getUserName(getApplicationContext()))) {
				ListAnime selectedAnime;
				int status;
				switch (animelistSelection) {
				case COMPLETE:
					selectedAnime = completeAnime.get(arg2);
					status = 2;
					break;
				case WATCHING:
					selectedAnime = watchingAnime.get(arg2);
					status = 1;
					break;
				case STALLED:
					selectedAnime = stalledAnime.get(arg2);
					status = 3;
					break;
				case DROPPED:
					selectedAnime = droppedAnime.get(arg2);
					status = 4;
					break;
				case PLANNED:
					selectedAnime = plannedAnime.get(arg2);
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
			String input;
			try {
				// Cache explizit nicht gewünscht!
				if (params[0].equals("no_cache") || !ownList) {
					if (this.isCancelled())
						return null;
					getNotifications();
					if (this.isCancelled())
						return null;
					input = animeService.getAnimeList(userId);
					if (this.isCancelled())
						return null;
					completeAnime = new ArrayList<ListAnime>();
					watchingAnime = new ArrayList<ListAnime>();
					stalledAnime = new ArrayList<ListAnime>();
					droppedAnime = new ArrayList<ListAnime>();
					plannedAnime = new ArrayList<ListAnime>();
					if (this.isCancelled())
						return null;
					animeService.getListAnime(input, completeAnime, watchingAnime,
							stalledAnime, droppedAnime, plannedAnime);
					if (ownList)
						new AnimelistCacheThread(
								currentUser, getApplicationContext(), completeAnime,
								watchingAnime, stalledAnime, droppedAnime, plannedAnime);
				}
				// eigene Liste -> Cache kommt in Betracht
				else {
					// teste ob Cache
					if (!loadCache()) {
						input = animeService.getAnimeList(userId);
						completeAnime = new ArrayList<ListAnime>();
						watchingAnime = new ArrayList<ListAnime>();
						stalledAnime = new ArrayList<ListAnime>();
						droppedAnime = new ArrayList<ListAnime>();
						plannedAnime = new ArrayList<ListAnime>();
						animeService.getListAnime(input, completeAnime, watchingAnime,
								stalledAnime, droppedAnime, plannedAnime);
						new AnimelistCacheThread(
								currentUser, getApplicationContext(), completeAnime,
								watchingAnime, stalledAnime, droppedAnime, plannedAnime);
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
		 * Der Lade-Dialog wird geschlossen und die fillViews aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			fillViews();
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
		 * Der Task wird droppedAnime.
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
							configurationService
									.getUserName(getApplicationContext()))) {
						completeAnime = new ArrayList<ListAnime>();
						watchingAnime = new ArrayList<ListAnime>();
						stalledAnime = new ArrayList<ListAnime>();
						droppedAnime = new ArrayList<ListAnime>();
						plannedAnime = new ArrayList<ListAnime>();
						// lese Cache aus und speicher in configurationService
						String jsonComplete = prefs.getString(
								"AnimelistCacheKomplett", "");
						String jsonWatching = prefs.getString(
								"AnimelistCacheAmSchauen", "");
						String jsonStalled = prefs.getString(
								"AnimelistCacheKurzAufgehoert", "");
						String jsonDropped = prefs.getString(
								"AnimelistCacheAbgebrochen", "");
						String jsonPlanned = prefs.getString(
								"AnimelistCacheGeplant", "");
						if (!jsonComplete.equals("")
								&& !jsonWatching.equals("")
								&& !jsonStalled.equals("")
								&& !jsonDropped.equals("")
								&& !jsonPlanned.equals("")) {
							// Konvertiere JSON-Strings zurück zu ArrayLists aus
							// ListAnime-Objekten
							// und setze Cache in configurationService
							try {
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(ListAnime.class,
												new ListAnimeDeserializer())
										.create();
								Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
								}.getType();
								completeAnime = gson.fromJson(jsonComplete,
										collectionType);
								watchingAnime = gson.fromJson(jsonWatching,
										collectionType);
								stalledAnime = gson.fromJson(
										jsonStalled, collectionType);
								droppedAnime = gson.fromJson(jsonDropped,
										collectionType);
								plannedAnime = gson.fromJson(jsonPlanned,
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
		EditText progressView;
		EditText episodeCountView;
		OnAnimeRatedListener onAnimeRatedListener;

		@Inject
		AnimeService animeService;

		public AnimeRatingDialogFragment() {
            ((EaApp) getActivity().getApplication()).getEaComponent().inject(this);
		}

		@Override
		public void onAttach(Activity activity) {
			try {
				onAnimeRatedListener = (OnAnimeRatedListener) activity;
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
			progressView = (EditText) view
					.findViewById(R.id.dialog_animelist_rate_fortschritt);
			progressView.setText("" + anime.getProgress());
			episodeCountView = (EditText) view
					.findViewById(R.id.dialog_animelist_rate_folgenzahl);
			episodeCountView.setText("" + anime.getEpisodeCount());
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
											.parseInt(progressView.getText()
													.toString());
									final int statusNew = categorySpinner
											.getSelectedItemPosition() + 1;
									new Thread(new Runnable() {
										public void run() {
											try {
												animeService
														.rateAnime(
																anime.getId(),
																score,
																seen,
																anime.getEpisodeCount(),
																statusNew);
											} catch (EAException e) {

											}
										}
									}).start();
									anime.setRating(score);
									anime.setProgress(seen);
									onAnimeRatedListener.onAnimeRatingComplete(anime,
											status, statusNew);
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
				watchingAnime.remove(anime);
				break;
			case 2:
				completeAnime.remove(anime);
				break;
			case 3:
				stalledAnime.remove(anime);
				break;
			case 4:
				droppedAnime.remove(anime);
				break;
			case 5:
				droppedAnime.remove(anime);
				break;
			}
			switch (newStatus) {
			case 1:
				watchingAnime.add(anime);
				break;
			case 2:
				completeAnime.add(anime);
				break;
			case 3:
				stalledAnime.add(anime);
				break;
			case 4:
				droppedAnime.add(anime);
				break;
			case 5:
				droppedAnime.add(anime);
				break;
			}
		}
		sortLists();
		listAnimeAdapter.notifyDataSetChanged();
		new AnimelistCacheThread(currentUser, getApplicationContext(),
				completeAnime, watchingAnime, stalledAnime, droppedAnime, plannedAnime);
	}
}
