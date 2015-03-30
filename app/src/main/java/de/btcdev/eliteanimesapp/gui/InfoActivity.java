package de.btcdev.eliteanimesapp.gui;

import java.util.ArrayList;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.adapter.InfoAdapter;

public class InfoActivity extends ParentActivity implements OnItemClickListener {

	private final String datenschutz = "Mit der Nutzung dieser App wird das Einverständnis gegeben, "
			+ "Teile der oben genannten anonymen Daten für Analysezwecke und zur Verbesserung der Software durch Google Analytics zu sammeln. "
			+ "Dies betrifft in keinster Weise persönliche und personenbezogene sowie vertrauliche Daten. "
			+ "Auf die Verarbeitung eingegebener Daten im Funktionsumfang von Eliteanimes.com hat diese App keinen Einfluss. "
			+ "Eine Weitergabe der Daten an Dritte ist mit oben genannter Ausnahme ausgeschlossen. "
			+ "Die Nutzung der App ist absolut kostenlos.";
	private final String inhalt = "Diese App stellt lediglich Funktionen von Eliteanimes.com für Android-Handys zur Verfügung. "
			+ "Auf die Funktionsweisen und Inhalte, die damit verbunden sind, hat diese App keinerlei Einfluss. "
			+ "Wir distanzieren uns von jeglichen über Eliteanimes.com gesendeten oder empfangenen Daten.";
	private final String lizenzen = "Folgende Open-Source-Libraries werden von dieser App genutzt:\n\n"
			+ "jsoup\n"
			+ "The MIT License, Copyright 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)\n\n"
			+ "gson\n"
			+ "Apache License 2.0\n\n"
			+ "Google Analytics Services SDK\n" + "Creative Commons 3.0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		ActionBar bar = getSupportActionBar();
		bar.setTitle("Info");

		viewZuweisung();

		handleNavigationDrawer(R.id.nav_info, R.id.nav_info_list, "Info", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.info, menu);
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
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.getId() == R.id.nav_info_list) {
			if (arg2 == navigation_info)
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			else
				super.onItemClick(arg0, arg1, arg2, arg3);
		}
	}

	public void viewZuweisung() {
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);

			ArrayList<String> ueberschrift = new ArrayList<String>();
			ArrayList<String> normaltext = new ArrayList<String>();

			ueberschrift.add("App-Version");
			normaltext.add(pinfo.versionName);
			ueberschrift.add("Android-Version");
			normaltext.add(android.os.Build.VERSION.RELEASE);
			ueberschrift.add("Android-SDK");
			normaltext.add("" + android.os.Build.VERSION.SDK_INT);
			ueberschrift.add("Minimal benötigte SDK-Version");
			normaltext.add("9");
			ueberschrift.add("Modell");
			normaltext.add(android.os.Build.MANUFACTURER + ", "
					+ android.os.Build.PRODUCT);
			ueberschrift.add("Datenschutz");
			normaltext.add(datenschutz);
			ueberschrift.add("Inhalt von Eliteanimes");
			normaltext.add(inhalt);
			ueberschrift.add("Lizenzen");
			normaltext.add(lizenzen);

			ListView list = (ListView) findViewById(R.id.info_list);
			InfoAdapter adapter = new InfoAdapter(getApplicationContext(),
					ueberschrift, normaltext);
			list.setAdapter(adapter);

		} catch (Exception e) {
		}
	}
}
