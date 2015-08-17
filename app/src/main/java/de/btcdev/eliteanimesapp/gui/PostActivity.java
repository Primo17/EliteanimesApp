package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.ForumPostAdapter;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.ForumPost;
import de.btcdev.eliteanimesapp.data.ForumThread;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.Netzwerk;

public class PostActivity extends ParentActivity implements
		OnItemSelectedListener {

	private ForumThread thread;
	private ArrayList<ForumPost> postList;
	private ListView listView;
	private PostTask postTask;
	private ForumPostAdapter postAdapter;
	private int pageCount;
	private int seite = 1;
	private String[] pages;
	private ArrayAdapter<String> pageAdapter;
	private ArrayList<Boolean> spoilerArray;
	private int chosenPosition;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		listView = (ListView) findViewById(R.id.threads_list);
		if (savedInstanceState != null) {
			thread = savedInstanceState.getParcelable("thread");
			seite = savedInstanceState.getInt("seite");
			pageCount = savedInstanceState.getInt("pageCount");
			pages = savedInstanceState.getStringArray("pages");
			postList = savedInstanceState.getParcelableArrayList("postList");
			chosenPosition = savedInstanceState.getInt("chosenPosition");
			spoilerArray = (ArrayList<Boolean>) savedInstanceState
					.getSerializable("spoilerArray");
			viewZuweisung();
		} else {
			Bundle bundle = getIntent().getExtras();
			thread = bundle.getParcelable("thread");
			seite = bundle.getInt("seite", 1);

			postTask = new PostTask();
			postTask.execute("");
		}
		bar = getSupportActionBar();
		bar.setTitle(thread.getName());
		handleNavigationDrawer(R.id.nav_posts, R.id.nav_posts_list,
				thread.getName(), null);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender ForenTask
	 * wird dabei abgebrochen.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		if (postTask != null) {
			postTask.cancel(true);
		}
		removeDialog(load_dialog);
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("thread", thread);
		outState.putInt("seite", seite);
		outState.putInt("pageCount", pageCount);
		outState.putStringArray("pages", pages);
		outState.putParcelableArrayList("postList", postList);
		outState.putInt("chosenPosition", chosenPosition);
		outState.putSerializable("spoilerArray", spoilerArray);
	}

	/**
	 * Das Optionsmenü wird erzeugt.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.posts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {
		case R.id.posts_aktualisieren:
			refresh();
			return super.onOptionsItemSelected(item);
		case R.id.posts_new:
			if (thread.isClosed()) {
				Toast.makeText(this, "Thema geschlossen!", Toast.LENGTH_SHORT)
						.show();
			} else {
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.NewPostActivity.class);
				intent.putExtra("thread", thread);
				startActivity(intent);
			}
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.threads_spinner) {
			if (seite != position + 1) {
				seite = position + 1;
				postTask = new PostTask();
				postTask.execute("");
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}

	/**
	 * Behandelt einen Klick im Navigation Drawer. Dabei wird die entsprechende
	 * Activity per Intent mit den erforderlichen Informationen gestartet.
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
		if (arg0.getId() == R.id.nav_posts_list) {
			super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.posts_list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			chosenPosition = info.position;
			if (chosenPosition < postList.size()) {
				ForumPost k = postList.get(chosenPosition);
				String name = Konfiguration
						.getBenutzername(getApplicationContext());
				ArrayList<String> items = new ArrayList<String>();
				if (k.getUserName().equals(name)) {
					if (!thread.isClosed())
						items.add(getResources().getString(
								R.string.post_zitieren));
					items.add(getResources().getString(R.string.post_spoiler));
					if (!thread.isClosed())
						items.add(getResources().getString(R.string.post_edit));
					if (!thread.isClosed() && chosenPosition != 0)
						items.add(getResources()
								.getString(R.string.post_delete));
					items.add(getResources().getString(R.string.post_copy));
					items.add(getResources().getString(R.string.post_profil));
				} else {
					items.add(getResources().getString(R.string.post_zitieren));
					items.add(getResources().getString(R.string.post_spoiler));
					items.add(getResources().getString(R.string.post_copy));
					items.add(getResources().getString(R.string.post_profil));
				}
				for (int i = 0; i < items.size(); i++)
					menu.add(items.get(i));
				menu.setHeaderTitle("Beitrag von " + k.getUserName());
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String temp = item.getTitle().toString();
		if (temp.equals(getResources().getString(R.string.post_delete))) {
			// TODO

		} else if (temp.equals(getResources().getString(R.string.post_edit))) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NewPostActivity.class);
			ForumPost k = postList.get(chosenPosition);
			System.out.println(k);
			chosenPosition = -1;
			intent.putExtra("editieren", true);
			intent.putExtra("editPost", k);
			intent.putExtra("thread", thread);
			startActivity(intent);
		} else if (temp.equals(getResources().getString(R.string.post_profil))) {
			ForumPost k = postList.get(chosenPosition);
			chosenPosition = -1;
			String name = k.getUserName();
			if (name.equals(Konfiguration
					.getBenutzername(getApplicationContext()))) {
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.ProfilActivity.class);
				startActivity(intent);
			} else {
				int userid = k.getUserId();
				Intent intent = new Intent(
						this,
						de.btcdev.eliteanimesapp.gui.FremdesProfilActivity.class);
				intent.putExtra("Benutzer", name);
				intent.putExtra("UserID", userid);
				startActivity(intent);
			}
		} else if (temp.equals(getResources().getString(R.string.post_copy))) {
			ForumPost k = postList.get(chosenPosition);
			String text = k.getText();
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
			Toast.makeText(this, "Beitrag wurde in die Zwischenablage kopiert",
					Toast.LENGTH_SHORT).show();
		} else if (temp.equals(getResources().getString(R.string.post_spoiler))) {
			if (spoilerArray.get(chosenPosition))
				spoilerArray.set(chosenPosition, false);
			else
				spoilerArray.set(chosenPosition, true);
			postAdapter.notifyDataSetChanged();
		} else if (temp
				.equals(getResources().getString(R.string.post_zitieren))) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NewPostActivity.class);
			intent.putExtra("zitieren", true);
			ForumPost k = postList.get(chosenPosition);
			chosenPosition = -1;
			intent.putExtra("editPost", k);
			intent.putExtra("thread", thread);
			startActivity(intent);
		}
		return true;
	}

	public void viewZuweisung() {
		if (pages == null)
			pages = new String[pageCount];
		if (pageAdapter == null) {
			LinearLayout root = (LinearLayout) findViewById(R.id.posts_root);
			LayoutInflater inflater = getLayoutInflater();
			View pagesView = inflater
					.inflate(R.layout.forum_pages, root, false);
			pageAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, pages);
			Spinner pageSpinner = (Spinner) pagesView
					.findViewById(R.id.threads_spinner);
			for (int i = 0; i < pageCount; i++)
				pages[i] = Integer.toString(i + 1);
			pageSpinner.setAdapter(pageAdapter);
			pageSpinner.setSelection(seite - 1);
			pageAdapter.notifyDataSetChanged();
			pageAdapter
					.setDropDownViewResource(R.layout.dialog_animelist_rate_dropdown);
			pageSpinner.setOnItemSelectedListener(this);
			root.addView(pagesView, 0);
		}
		if (postList != null) {
			spoilerArray = new ArrayList<Boolean>(postList.size());
			for (int i = 0; i < postList.size(); i++) {
				spoilerArray.add(false);
			}
			listView = (ListView) findViewById(R.id.posts_list);
			postAdapter = new ForumPostAdapter(this, postList, spoilerArray);
			listView.setAdapter(postAdapter);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}
	}

	public void refresh() {
		LinearLayout root = (LinearLayout) findViewById(R.id.posts_root);
		View v = root.getChildAt(0);
		if (v != null && v.getId() == R.id.forum_pages_root)
			root.removeViewAt(0);
		postAdapter = null;
		pageAdapter = null;
		pages = null;
		postTask = new PostTask();
		postTask.execute("");
	}

	public class PostTask extends AsyncTask<String, String, String> {

		private boolean delete = false;
		private ForumPost p = null;

		@Override
		protected String doInBackground(String... params) {
			String input;
			try {
				netzwerk = Netzwerk.instance(getApplicationContext());
				eaParser = new EAParser(getApplicationContext());
				if (params[0].equals("delete")) {
					delete = true;
					p = postList.get(chosenPosition);
					boolean temp = netzwerk.deletePost(p.getId());
					if (temp)
						return "success";
				} else {
					if (isCancelled())
						return null;
					input = netzwerk.getPosts(thread.getId(), seite);
					if (isCancelled())
						return null;
					pageCount = eaParser.getForumPostsPageCount(input);
					if(pageCount == 0)
						pageCount++;
					if (isCancelled())
						return null;
					postList = eaParser.getForumPosts(input);
					return "success";
				}
			} catch (EAException e) {
				publishProgress("Exception", e.getMessage());
			}
			return null;
		}

		/**
		 * Öffnet einen Ladedialog.
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
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (!isNullOrEmpty(result))
				if (delete) {
					postList.remove(p);
					chosenPosition = -1;
					postAdapter.notifyDataSetChanged();
				} else {
					viewZuweisung();
				}
			try {
				dismissDialog(load_dialog);
			} catch (IllegalArgumentException e) {

			}
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

		@Override
		protected void onCancelled(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onCancelled();
		}

	}
}
