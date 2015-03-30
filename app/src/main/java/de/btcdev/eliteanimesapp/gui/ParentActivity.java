package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.NavDrawerListAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.NavDrawerItem;
import de.btcdev.eliteanimesapp.data.Netzwerk;

public abstract class ParentActivity extends ActionBarActivity implements
		OnItemClickListener {

	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected ArrayList<NavDrawerItem> navDrawerItems;
	protected NavDrawerListAdapter adapter;
	protected Netzwerk netzwerk;
	protected EAParser eaParser;
	protected final int load_dialog = 0;
	protected ProgressDialog loadDialog;
	protected ActionBar bar;
	public static final int navigation_profil = 0;
	public static final int navigation_kommentare = 1;
	public static final int navigation_pns = 2;
	public static final int navigation_freunde = 3;
	public static final int navigation_animeliste = 4;
	public static final int navigation_forum = 5;
	public static final int navigation_suche = 6;
	public static final int navigation_konto = 7;
	public static final int navigation_settings = 8;
	public static final int navigation_info = 9;
	public static final int navigation_logout = 10;

	public void handleNavigationDrawer(int layoutId, int listId,
			final String name, final String sub) {
		bar = getSupportActionBar();
		mDrawerLayout = (DrawerLayout) findViewById(layoutId);
		mDrawerList = (ListView) findViewById(listId);
		navDrawerItems = new ArrayList<NavDrawerItem>();
		navDrawerItems = setNavigationDrawer(navDrawerItems,
				getApplicationContext());
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(name);
				getSupportActionBar().setSubtitle(sub);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View view) {
				getSupportActionBar().setTitle("Eliteanimes");
				getSupportActionBar().setSubtitle(null);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerList.setOnItemClickListener(this);
	}

	public ArrayList<NavDrawerItem> setNavigationDrawer(
			ArrayList<NavDrawerItem> list, Context context) {
		String[] navMenuTitles = context.getResources().getStringArray(
				R.array.nav_drawer_items);
		list.add(new NavDrawerItem(navMenuTitles[navigation_profil],
				R.drawable.ic_drawer_profil));
		list.add(new NavDrawerItem(navMenuTitles[navigation_kommentare],
				R.drawable.ic_drawer_comments, true, ""
						+ Konfiguration.getNewCommentCount()));
		list.add(new NavDrawerItem(navMenuTitles[navigation_pns],
				R.drawable.ic_drawer_pn, true, ""
						+ Konfiguration.getNewMessageCount()));
		list.add(new NavDrawerItem(navMenuTitles[navigation_freunde],
				R.drawable.ic_drawer_friends));
		list.add(new NavDrawerItem(navMenuTitles[navigation_animeliste],
				R.drawable.ic_drawer_animelist));
		list.add(new NavDrawerItem(navMenuTitles[navigation_forum],
				R.drawable.ic_drawer_animelist));
		list.add(new NavDrawerItem(navMenuTitles[navigation_suche],
				R.drawable.ic_drawer_search));
		list.add(new NavDrawerItem(navMenuTitles[navigation_konto],
				R.drawable.ic_drawer_settings));
		list.add(new NavDrawerItem(navMenuTitles[navigation_settings],
				R.drawable.ic_drawer_settings));
		list.add(new NavDrawerItem(navMenuTitles[navigation_info],
				R.drawable.ic_drawer_info));
		list.add(new NavDrawerItem(navMenuTitles[navigation_logout],
				R.drawable.ic_drawer_logout));
		return list;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Überschreibt die Funktion der Menü-Taste und öffnet damit den Navigation
	 * Drawer.
	 * 
	 * @param keycode
	 *            Code der gedrückten Taste
	 * @param e
	 *            ausgelöstes Event
	 */
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (mDrawerLayout.isDrawerVisible(Gravity.LEFT))
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				mDrawerLayout.openDrawer(Gravity.LEFT);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	/**
	 * Wird aufgerufen, wenn ein Dialog erzeugt wird. Wenn ein Lade-Dialog
	 * verlangt ist (load_dialog), wird ein ProgressDialog aufgerufen.
	 * 
	 * @param id
	 *            id des gewünschten Dialogs
	 * @return gibt den erzeugten Dialog zurück
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == load_dialog) {
			loadDialog = new ProgressDialog(this);
			loadDialog.setIndeterminate(true);
			loadDialog.setMessage("Loading...");
			loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loadDialog.show();
			return loadDialog;
		} else
			return null;
	}

	/**
	 * Behandelt einen Klick auf die Liste unter dem Profil und im Navigation
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
		Netzwerk netzwerk = Netzwerk.instance(this);
		if (!netzwerk.isLoggedIn()) {
			Toast.makeText(this, "Erst nach Login möglich!", Toast.LENGTH_SHORT)
					.show();
		} else {
			switch (arg2) {
			case navigation_profil:
				Intent intent = new Intent(getApplicationContext(),
						de.btcdev.eliteanimesapp.gui.ProfilActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_kommentare:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.KommentarActivity.class);
				intent.putExtra("Benutzer",
						Konfiguration.getBenutzername(getApplicationContext()));
				intent.putExtra("UserID",
						Konfiguration.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_pns:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.PNActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_freunde:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.FreundeActivity.class);
				intent.putExtra("Benutzer",
						Konfiguration.getBenutzername(getApplicationContext()));
				intent.putExtra("UserID",
						Konfiguration.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_animeliste:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.AnimeListActivity.class);
				intent.putExtra("Benutzer",
						Konfiguration.getBenutzername(getApplicationContext()));
				intent.putExtra("UserID",
						Konfiguration.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_forum:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.ForenActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_suche:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.SearchActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_info:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.InfoActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_konto:
				intent = new Intent(
						this,
						de.btcdev.eliteanimesapp.gui.KontoeinstellungenActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_settings:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.SettingsActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case navigation_logout:
				try {
					netzwerk = Netzwerk.instance(this);
					netzwerk.logout();
					intent = new Intent(this,
							de.btcdev.eliteanimesapp.gui.LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("Logout", true);
					mDrawerLayout.closeDrawer(Gravity.LEFT);
					startActivity(intent);
				} catch (EAException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
		}
	}

	public boolean isNullOrEmpty(String input) {
		return input == null || input.isEmpty();
	}
}
