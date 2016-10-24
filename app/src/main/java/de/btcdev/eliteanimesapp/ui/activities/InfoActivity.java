package de.btcdev.eliteanimesapp.ui.activities;

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

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.ui.adapter.InfoAdapter;

public class InfoActivity extends ParentActivity implements OnItemClickListener {

    private final String privacy = "Mit der Nutzung dieser App wird das Einverständnis gegeben, "
            + "Teile der oben genannten anonymen Daten für Analysezwecke und zur Verbesserung der Software durch Google Analytics zu sammeln. "
            + "Dies betrifft in keinster Weise persönliche und personenbezogene sowie vertrauliche Daten. "
            + "Auf die Verarbeitung eingegebener Daten im Funktionsumfang von Eliteanimes.com hat diese App keinen Einfluss. "
            + "Eine Weitergabe der Daten an Dritte ist mit oben genannter Ausnahme ausgeschlossen. "
            + "Die Nutzung der App ist absolut kostenlos.";
    private final String appContent = "Diese App stellt lediglich Funktionen von Eliteanimes.com für Android-Handys zur Verfügung. "
            + "Auf die Funktionsweisen und Inhalte, die damit verbunden sind, hat diese App keinerlei Einfluss. "
            + "Wir distanzieren uns von jeglichen über Eliteanimes.com gesendeten oder empfangenen Daten.";
    private final String licenses = "Folgende Open-Source-Libraries werden von dieser App genutzt:\n\n"
            + "jsoup\n"
            + "The MIT License, Copyright 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)\n\n"
            + "gson\n"
            + "Apache License 2.0\n\n"
            + "Google Analytics Services SDK\n" + "Creative Commons 3.0\n\n"
            + "Das Launcher-Icon sowie das NoAva-Bild wurden erstellt mit Android Asset Studio\n"
            + "CC BY 3.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Info");

        fillViews();

        handleNavigationDrawer(R.id.nav_info, R.id.nav_info_list, "Info", null);
    }

    @Override
    protected void injectDependencies() {
        ((EaApp) getApplication()).getEaComponent().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action actionBar if it is present.
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
     * @param arg0 betroffener Adapter
     * @param arg1 betroffenes View-Element
     * @param arg2 Position des Elements im Adapter
     * @param arg3 irgendwas hier unwichtiges
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg0.getId() == R.id.nav_info_list) {
            if (arg2 == NAVIGATION_INFO)
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            else
                super.onItemClick(arg0, arg1, arg2, arg3);
        }
    }

    public void fillViews() {
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);

            ArrayList<String> headline = new ArrayList<String>();
            ArrayList<String> content = new ArrayList<String>();

            headline.add("App-Version");
            content.add(pinfo.versionName);
            headline.add("Android-Version");
            content.add(android.os.Build.VERSION.RELEASE);
            headline.add("Android-SDK");
            content.add("" + android.os.Build.VERSION.SDK_INT);
            headline.add("Minimal benötigte SDK-Version");
            content.add("9");
            headline.add("Modell");
            content.add(android.os.Build.MANUFACTURER + ", "
                    + android.os.Build.PRODUCT);
            headline.add("Datenschutz");
            content.add(privacy);
            headline.add("Inhalt von Eliteanimes");
            content.add(this.appContent);
            headline.add("Lizenzen");
            content.add(licenses);

            ListView list = (ListView) findViewById(R.id.info_list);
            InfoAdapter adapter = new InfoAdapter(getApplicationContext(),
                    headline, content);
            list.setAdapter(adapter);

        } catch (Exception e) {
        }
    }
}
