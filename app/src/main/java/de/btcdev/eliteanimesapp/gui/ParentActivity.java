package de.btcdev.eliteanimesapp.gui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.NavDrawerListAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.NavDrawerItem;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.services.ConfigurationService;
import de.btcdev.eliteanimesapp.services.LoginService;

public abstract class ParentActivity extends ActionBarActivity implements
		OnItemClickListener {


	@Inject
	ConfigurationService configurationService;
	@Inject
	NetworkService networkService;
	@Inject
	LoginService loginService;

	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected ArrayList<NavDrawerItem> navDrawerItems;
	protected NavDrawerListAdapter navDrawerListAdapter;
	protected EAParser eaParser;
	protected final int load_dialog = 0;
	protected ProgressDialog loadDialog;
	protected ActionBar actionBar;
	public static final int NAVIGATION_PROFILE = 0;
	public static final int NAVIGATION_COMMENTS = 1;
	public static final int NAVIGATION_PRIVATE_MESSAGES = 2;
	public static final int NAVIGATION_FRIENDS = 3;
	public static final int NAVIGATION_ANIMELIST = 4;
	public static final int NAVIGATION_BOARD = 5;
	public static final int NAVIGATION_SEARCH = 6;
	public static final int NAVIGATION_ACCOUNT = 7;
	public static final int NAVIGATION_SETTINGS = 8;
	public static final int NAVIGATION_INFO = 9;
	public static final int NAVIGATION_LOGOUT = 10;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		injectDependencies();
	}

	protected abstract void injectDependencies();

	public void handleNavigationDrawer(int layoutId, int listId,
									   final String name, final String sub) {
		actionBar = getSupportActionBar();
		mDrawerLayout = (DrawerLayout) findViewById(layoutId);
		mDrawerList = (ListView) findViewById(listId);
		navDrawerItems = new ArrayList<>();
		navDrawerItems = setNavigationDrawer(navDrawerItems,
				getApplicationContext());
		navDrawerListAdapter = NavDrawerListAdapter.instance(getApplicationContext(), navDrawerItems, configurationService);
		mDrawerList.setAdapter(navDrawerListAdapter);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
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
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_PROFILE],
				R.drawable.ic_drawer_profil));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_COMMENTS],
				R.drawable.ic_drawer_comments, true, ""
						+ configurationService.getNewCommentCount()));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_PRIVATE_MESSAGES],
				R.drawable.ic_drawer_pn, true, ""
						+ configurationService.getNewMessageCount()));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_FRIENDS],
				R.drawable.ic_drawer_friends));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_ANIMELIST],
				R.drawable.ic_drawer_animelist));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_BOARD],
				R.drawable.ic_drawer_animelist));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_SEARCH],
				R.drawable.ic_drawer_search));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_ACCOUNT],
				R.drawable.ic_drawer_settings));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_SETTINGS],
				R.drawable.ic_drawer_settings));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_INFO],
				R.drawable.ic_drawer_info));
		list.add(new NavDrawerItem(navMenuTitles[NAVIGATION_LOGOUT],
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
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
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
		if (!loginService.isSomeoneLoggedIn()) {
			Toast.makeText(this, "Erst nach Login möglich!", Toast.LENGTH_SHORT)
					.show();
		} else {
			switch (arg2) {
			case NAVIGATION_PROFILE:
				Intent intent = new Intent(getApplicationContext(),
						ProfileActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_COMMENTS:
				intent = new Intent(this,
						CommentActivity.class);
				intent.putExtra("User",
						configurationService.getUserName(getApplicationContext()));
				intent.putExtra("UserID",
						configurationService.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_PRIVATE_MESSAGES:
				intent = new Intent(this,
						PrivateMessageActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_FRIENDS:
				intent = new Intent(this,
						FriendActivity.class);
				intent.putExtra("User",
						configurationService.getUserName(getApplicationContext()));
				intent.putExtra("UserID",
						configurationService.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_ANIMELIST:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.AnimeListActivity.class);
				intent.putExtra("User",
						configurationService.getUserName(getApplicationContext()));
				intent.putExtra("UserID",
						configurationService.getUserID(getApplicationContext()));
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_BOARD:
				intent = new Intent(this,
						BoardActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_SEARCH:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.SearchActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_INFO:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.InfoActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_ACCOUNT:
				intent = new Intent(
						this,
						AccountSettingsActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_SETTINGS:
				intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.SettingsActivity.class);
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				startActivity(intent);
				break;
			case NAVIGATION_LOGOUT:
				try {
					loginService.logout();
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
