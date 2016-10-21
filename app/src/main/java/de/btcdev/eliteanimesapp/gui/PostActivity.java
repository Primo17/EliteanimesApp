package de.btcdev.eliteanimesapp.gui;

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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.BoardPostAdapter;
import de.btcdev.eliteanimesapp.data.BoardPost;
import de.btcdev.eliteanimesapp.data.BoardThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.services.BoardService;

public class PostActivity extends ParentActivity implements
		OnItemSelectedListener {

	@Inject
	BoardService boardService;

	private BoardThread boardThread;
	private ArrayList<BoardPost> postList;
	private ListView listView;
	private PostTask postTask;
	private BoardPostAdapter boardPostAdapter;
	private int pageCount;
	private int page = 1;
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
			boardThread = savedInstanceState.getParcelable("boardThread");
			page = savedInstanceState.getInt("page");
			pageCount = savedInstanceState.getInt("pageCount");
			pages = savedInstanceState.getStringArray("pages");
			postList = savedInstanceState.getParcelableArrayList("postList");
			chosenPosition = savedInstanceState.getInt("chosenPosition");
			spoilerArray = (ArrayList<Boolean>) savedInstanceState
					.getSerializable("spoilerArray");
			fillViews();
		} else {
			Bundle bundle = getIntent().getExtras();
			boardThread = bundle.getParcelable("boardThread");
			page = bundle.getInt("page", 1);

			postTask = new PostTask();
			postTask.execute("");
		}
		actionBar = getSupportActionBar();
		actionBar.setTitle(boardThread.getName());
		handleNavigationDrawer(R.id.nav_posts, R.id.nav_posts_list,
				boardThread.getName(), null);
	}

	@Override
	protected void injectDependencies() {
		((EaApp) getApplication()).getEaComponent().inject(this);
	}

	/**
	 * Wird aufgerufen, wenn die Activity pausiert wird. Ein laufender BoardTask
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
		outState.putParcelable("boardThread", boardThread);
		outState.putInt("page", page);
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
		// Inflate the menu; this adds items to the action actionBar if it is present.
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
			if (boardThread.isClosed()) {
				Toast.makeText(this, "Thema geschlossen!", Toast.LENGTH_SHORT)
						.show();
			} else {
				Intent intent = new Intent(this,
						de.btcdev.eliteanimesapp.gui.NewPostActivity.class);
				intent.putExtra("boardThread", boardThread);
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
			if (page != position + 1) {
				page = position + 1;
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
				BoardPost boardPost = postList.get(chosenPosition);
				String userName = configurationService
						.getUserName(getApplicationContext());
				ArrayList<String> items = new ArrayList<String>();
				if (boardPost.getUserName().equals(userName)) {
					if (!boardThread.isClosed())
						items.add(getResources().getString(
								R.string.post_zitieren));
					items.add(getResources().getString(R.string.post_spoiler));
					if (!boardThread.isClosed())
						items.add(getResources().getString(R.string.post_edit));
					if (!boardThread.isClosed() && chosenPosition != 0)
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
				menu.setHeaderTitle("Beitrag von " + boardPost.getUserName());
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
			BoardPost boardPost = postList.get(chosenPosition);
			chosenPosition = -1;
			intent.putExtra("editieren", true);
			intent.putExtra("editPost", boardPost);
			intent.putExtra("boardThread", boardThread);
			startActivity(intent);
		} else if (temp.equals(getResources().getString(R.string.post_profil))) {
			BoardPost boardPost = postList.get(chosenPosition);
			chosenPosition = -1;
			String userName = boardPost.getUserName();
			if (userName.equals(configurationService
					.getUserName(getApplicationContext()))) {
				Intent intent = new Intent(this,
						ProfileActivity.class);
				startActivity(intent);
			} else {
				int userId = boardPost.getUserId();
				Intent intent = new Intent(
						this,
						UserProfileActivity.class);
				intent.putExtra("User", userName);
				intent.putExtra("UserID", userId);
				startActivity(intent);
			}
		} else if (temp.equals(getResources().getString(R.string.post_copy))) {
			BoardPost boardPost = postList.get(chosenPosition);
			String boardPostContent = boardPost.getText();
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(boardPostContent);
			Toast.makeText(this, "Beitrag wurde in die Zwischenablage kopiert",
					Toast.LENGTH_SHORT).show();
		} else if (temp.equals(getResources().getString(R.string.post_spoiler))) {
			if (spoilerArray.get(chosenPosition))
				spoilerArray.set(chosenPosition, false);
			else
				spoilerArray.set(chosenPosition, true);
			boardPostAdapter.notifyDataSetChanged();
		} else if (temp
				.equals(getResources().getString(R.string.post_zitieren))) {
			Intent intent = new Intent(this,
					de.btcdev.eliteanimesapp.gui.NewPostActivity.class);
			intent.putExtra("zitieren", true);
			BoardPost boardPost = postList.get(chosenPosition);
			chosenPosition = -1;
			intent.putExtra("editPost", boardPost);
			intent.putExtra("boardThread", boardThread);
			startActivity(intent);
		}
		return true;
	}

	public void fillViews() {
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
			pageSpinner.setSelection(page - 1);
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
			boardPostAdapter = new BoardPostAdapter(this, postList, spoilerArray);
			listView.setAdapter(boardPostAdapter);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}
	}

	public void refresh() {
		LinearLayout root = (LinearLayout) findViewById(R.id.posts_root);
		View v = root.getChildAt(0);
		if (v != null && v.getId() == R.id.forum_pages_root)
			root.removeViewAt(0);
		boardPostAdapter = null;
		pageAdapter = null;
		pages = null;
		postTask = new PostTask();
		postTask.execute("");
	}

	public class PostTask extends AsyncTask<String, String, String> {

		private boolean delete = false;
		private BoardPost p = null;

		@Override
		protected String doInBackground(String... params) {
			String input;
			try {
				if (params[0].equals("delete")) {
					delete = true;
					p = postList.get(chosenPosition);
					boolean temp = boardService.deletePost(p.getId());
					if (temp)
						return "success";
				} else {
					if (isCancelled())
						return null;
					input = boardService.getPosts(boardThread.getId(), page);
					if (isCancelled())
						return null;
					pageCount = boardService.getForumPostsPageCount(input);
					if(pageCount == 0)
						pageCount++;
					if (isCancelled())
						return null;
					postList = boardService.getBoardPosts(input);
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
		 * Der Lade-Dialog wird geschlossen und die fillViews aufgerufen.
		 */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if (StringUtils.isNotEmpty(result))
				if (delete) {
					postList.remove(p);
					chosenPosition = -1;
					boardPostAdapter.notifyDataSetChanged();
				} else {
					fillViews();
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
