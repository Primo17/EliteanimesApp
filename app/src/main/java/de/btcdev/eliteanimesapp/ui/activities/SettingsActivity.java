package de.btcdev.eliteanimesapp.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.btcdev.eliteanimesapp.R;

public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
