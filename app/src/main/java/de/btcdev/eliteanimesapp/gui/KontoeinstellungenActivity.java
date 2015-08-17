package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.AnfragenAdapter;
import de.btcdev.eliteanimesapp.adapter.BlockedAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Freundschaftsanfrage;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.data.NewsThread;

public class KontoeinstellungenActivity extends ParentActivity implements
		OnItemClickListener {

	private KontoPagerAdapter kontoPagerAdapter;
	private ViewPager viewPager;
	private ArrayList<Freundschaftsanfrage> anfrageListe;
	private ArrayList<Freundschaftsanfrage> blockedListe;
	private KontoTask task;
	private int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kontoeinstellungen);
		bar = getSupportActionBar();
		viewPager = (ViewPager) findViewById(R.id.konto_pager);
		// set listener for swiping actions
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						bar.setSelectedNavigationItem(position);
					}
				});
		bar.setTitle("Konto");
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// set listener for tab actions
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				// show the given tab
				viewPager.setCurrentItem(tab.getPosition());
			}

			public void onTabUnselected(ActionBar.Tab tab,
					FragmentTransaction ft) {
				// hide the given tab
			}

			public void onTabReselected(ActionBar.Tab tab,
					FragmentTransaction ft) {
				// probably ignore this event
			}
		};
		bar.addTab(bar.newTab().setText("Anfragen").setTabListener(tabListener));
		bar.addTab(bar.newTab().setText("Blockiert")
				.setTabListener(tabListener));

		netzwerk = Netzwerk.instance(this);

		if (savedInstanceState != null) {
			anfrageListe = savedInstanceState
					.getParcelableArrayList("requests");
			blockedListe = savedInstanceState.getParcelableArrayList("blocked");
			position = savedInstanceState.getInt("tab");
			loadFragments();
		} else {
			task = new KontoTask();
			task.execute("");
		}
		handleNavigationDrawer(R.id.nav_konto, R.id.nav_konto_list, "Konto",
				null);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender
	 * FreundeTask wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (task != null) {
			task.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	public void loadFragments() {
		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		kontoPagerAdapter = new KontoPagerAdapter(getSupportFragmentManager(),
				anfrageListe, blockedListe);
		viewPager.setAdapter(kontoPagerAdapter);
		bar.setSelectedNavigationItem(position);
		kontoPagerAdapter.instantiateItem(viewPager, position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kontoeinstellungen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Speichert die Freundschaftsanfragen und blockierten User
	 * 
	 * @param savedInstanceState
	 *            vom System erzeugtes Bundle zum Speichern der Daten
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelableArrayList("requests", anfrageListe);
		savedInstanceState.putParcelableArrayList("blocked", blockedListe);
		savedInstanceState.putInt("tab", bar.getSelectedNavigationIndex());
	}

	/**
	 * Behandelt einen Klick auf die Liste im Navigation Drawer. Dabei wird die
	 * entsprechende Activity per Intent mit den erforderlichen Informationen
	 * gestartet.
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
		if (arg0.getId() == R.id.nav_konto_list) {
			if(arg2 == navigation_konto)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}


	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		int id = view.getId();
		int position;
		// id >= 2000 -> decline
		// id >= 1000 -> accept
		if (id >= 2000) {
			// decline
			position = id - 2000;
			if (anfrageListe.size() <= position)
				return;
			final Freundschaftsanfrage f = anfrageListe.get(position);
			if (checked) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Netzwerk.instance(getApplicationContext())
									.declineFriendRequest(
											Integer.toString(f.getId()));
						} catch (Exception e) {

						}
					}
				}).start();
				Toast.makeText(this,
						"Anfrage von " + f.getName() + " abgelehnt.",
						Toast.LENGTH_SHORT).show();
				anfrageListe.remove(f);
				reload();
			}
		} else if (id >= 1000) {
			// accept
			position = id - 1000;
			final Freundschaftsanfrage f = anfrageListe.get(position);
			if (checked) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Netzwerk.instance(getApplicationContext())
									.acceptFriendRequest(
											Integer.toString(f.getId()));
						} catch (Exception e) {

						}
					}
				}).start();
				Toast.makeText(this,
						"Anfrage von " + f.getName() + " akzeptiert.",
						Toast.LENGTH_SHORT).show();
				anfrageListe.remove(f);
				kontoPagerAdapter.notifyDataSetChanged();
				reload();
			}
		}

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.anfrage_annehmen:
			if (checked) {

			}
			break;
		case R.id.anfrage_ablehnen:
			if (checked) {

			}
			break;
		}
	}

	public void reload() {
		task = new KontoTask();
		task.execute("");
	}

	public class KontoPagerAdapter extends FragmentPagerAdapter {

		private ArrayList<Freundschaftsanfrage> anfragen;
		private ArrayList<Freundschaftsanfrage> blockiert;

		public KontoPagerAdapter(FragmentManager fm,
				ArrayList<Freundschaftsanfrage> anfragen,
				ArrayList<Freundschaftsanfrage> blockiert) {
			super(fm);
			this.anfragen = anfragen;
			this.blockiert = blockiert;
		}

		@Override
		public Fragment getItem(int arg0) {
			if (arg0 == 0) {
				Fragment fragment = new FreundeAnfragenFragment();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("liste", anfragen);
				fragment.setArguments(bundle);
				return fragment;
			} else {
				Fragment fragment = new BlockierenFragment();
				Bundle args = new Bundle();
				args.putInt(BlockierenFragment.ARG_OBJECT, arg0 + 1);
				args.putParcelableArrayList("liste", blockiert);
				fragment.setArguments(args);
				return fragment;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "OBJECT " + (position + 1);
		}
	}

	public static class FreundeAnfragenFragment extends Fragment {
		public static final String ARG_OBJECT = "object";
		ArrayList<Freundschaftsanfrage> list;
		int chosenPosition;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// The last two arguments ensure LayoutParams are inflated
			// properly.
			View rootView = inflater.inflate(R.layout.freundesanfragen_layout,
					container, false);
			Bundle bundle = getArguments();
			list = bundle.getParcelableArrayList("liste");
			viewZuweisung(rootView);
			return rootView;
		}

		public void viewZuweisung(View view) {
			if (list == null || list.isEmpty()) {
				LinearLayout lin = (LinearLayout) view
						.findViewById(R.id.freundesanfragen_layout);
				TextView text = new TextView(view.getContext());
				text.setTextSize(16);
				text.setTypeface(text.getTypeface(), Typeface.BOLD);
				text.setText("Keine neuen Freundschaftsanfragen vorhanden.");
				text.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL);
				lin.addView(text, 0, new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			} else {
				ListView liste = (ListView) view
						.findViewById(R.id.freundeanfragen_liste);
				AnfragenAdapter adapter = new AnfragenAdapter(
						view.getContext(), list);
				liste.setAdapter(adapter);
				registerForContextMenu(liste);
			}
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			String temp = item.getTitle().toString();
			if (temp.equals(getResources().getString(
					R.string.comment_profil_besuchen))) {
				Freundschaftsanfrage f = list.get(chosenPosition);
				Intent intent = new Intent(
						getActivity(),
						de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
				intent.putExtra("Benutzer", f.getName());
				intent.putExtra("UserID", f.getId());
				startActivity(intent);
			}
			return true;
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (v.getId() == R.id.freundeanfragen_liste) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				chosenPosition = info.position;
				if (chosenPosition < list.size()) {
					menu.add(getResources().getString(
							R.string.comment_profil_besuchen));
					menu.setHeaderTitle("User: "
							+ list.get(chosenPosition).getName());
				}
			}
		}

	}

	public static class BlockierenFragment extends Fragment implements
			OnItemClickListener {
		public static final String ARG_OBJECT = "object";
		ArrayList<Freundschaftsanfrage> list;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// The last two arguments ensure LayoutParams are inflated
			// properly.
			View rootView = inflater.inflate(R.layout.freundesanfragen_layout,
					container, false);
			Bundle bundle = getArguments();
			list = bundle.getParcelableArrayList("liste");
			viewZuweisung(rootView);
			return rootView;
		}

		public void viewZuweisung(View view) {
			if (list == null || list.isEmpty()) {
				LinearLayout lin = (LinearLayout) view
						.findViewById(R.id.freundesanfragen_layout);
				TextView text = new TextView(view.getContext());
				text.setTextSize(16);
				text.setTypeface(text.getTypeface(), Typeface.BOLD);
				text.setText("Keine blockierten Benutzer vorhanden.");
				text.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL);
				lin.addView(text, 0, new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			} else {
				ListView liste = (ListView) view
						.findViewById(R.id.freundeanfragen_liste);
				BlockedAdapter adapter = new BlockedAdapter(view.getContext(),
						list);
				liste.setAdapter(adapter);
				liste.setOnItemClickListener(this);
			}
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			DialogFragment dialog = new BlockierenDialog();
			Bundle bundle = new Bundle();
			bundle.putParcelable("anfrage", list.get(arg2));
			dialog.setArguments(bundle);
			dialog.show(getFragmentManager(), "BlockierenDialog");
		}

	}

	public static class BlockierenDialog extends DialogFragment {

		Freundschaftsanfrage anfrage;

		public BlockierenDialog() {

		}

		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle bundle = getArguments();
			anfrage = bundle.getParcelable("anfrage");
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("User: " + anfrage.getName());
			CharSequence[] choice = { "Profil aufrufen", "Blockierung aufheben" };
			builder.setItems(choice, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						Intent intent = new Intent(
								getActivity(),
								de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
						intent.putExtra("Benutzer", anfrage.getName());
						intent.putExtra("UserID", anfrage.getId());
						startActivity(intent);
						break;
					case 1:
						new Thread(new Runnable() {
							public void run() {
								try {
									Netzwerk.instance(getActivity())
											.unblockUser(
													Integer.toString(anfrage
															.getId()));
								} catch (EAException e) {

								}
							}
						}).start();
						Toast.makeText(getActivity(),
								"Blockierung aufgehoben.", Toast.LENGTH_SHORT)
								.show();
						intent = new Intent(
								getActivity(),
								de.btcdev.eliteanimesapp.gui.KontoeinstellungenActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						break;
					}
				}
			});
			AlertDialog dialog = builder.create();
			return dialog;
		}
	}

	/**
	 * Klasse für das Herunterladen der Informationen. Erbt von AsyncTask.
	 */
	public class KontoTask extends
			AsyncTask<String, String, ArrayList<Freundschaftsanfrage>[]> {

		/**
		 * Freundschaftsanfragen werden heruntergeladen und geparst
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected ArrayList<Freundschaftsanfrage>[] doInBackground(
				String... params) {
			String input;
			ArrayList<Freundschaftsanfrage> list;
			try {
				if (this.isCancelled())
					return null;
				input = Netzwerk.instance(getApplicationContext())
						.getFriendRequests();
				new NewsThread(getApplicationContext()).start();
				if (this.isCancelled())
					return null;
				list = new EAParser(null).getFreundschaftsanfragen(input);
				if (this.isCancelled())
					return null;
				ArrayList<Freundschaftsanfrage> list2;
				if (this.isCancelled())
					return null;
				input = Netzwerk.instance(getApplicationContext())
						.getBlockedUsers();
				if (this.isCancelled())
					return null;
				list2 = new EAParser(null).getBlockierteUser(input);
				if (this.isCancelled())
					return null;
				ArrayList[] gesamt = new ArrayList[2];
				gesamt[0] = list;
				gesamt[1] = list2;
				return gesamt;
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Es wird ein Ladedialog geöffnet und evtl der Bildschirm-Timeout
		 * deaktiviert
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
		 * Task wird abgebrochen
		 */
		@Override
		protected void onCancelled() {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onCancelled();
		}

		/**
		 * Der Ladedialog wird geschlossen und die viewZuweisung des Aufrufers
		 * aufgerufen
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<Freundschaftsanfrage>[] result) {
			anfrageListe = result[0];
			blockedListe = result[1];
			loadFragments();
			try {
				dismissDialog(load_dialog);
			} catch (Exception e) {

			}
		}

		/**
		 * Gibt Exceptions aus
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			if (values[0].equals("Exception")) {
				if (values[1] != null)
					Toast.makeText(getApplicationContext(), values[1],
							Toast.LENGTH_LONG).show();
			}
		}

	}
}
