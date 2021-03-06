package de.btcdev.eliteanimesapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import de.btcdev.eliteanimesapp.EaApp;
import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.exceptions.EAException;
import de.btcdev.eliteanimesapp.data.models.Profile;
import de.btcdev.eliteanimesapp.data.services.ProfileCache;

/**
 * Activity, die für den Login des Benutzers zuständig ist.
 */
public class LoginActivity extends ParentActivity implements OnClickListener,
        OnItemClickListener {

    private Button loginButton;
    private EditText loginUserNameView;
    private EditText loginPasswordView;
    private CheckBox loginCheckView;
    private SharedPreferences prefs;
    private LoginTask loginTask;
    private ProfileCache profileCache;

    private String userName;
    private String password;

    /**
     * Erzeugt die Activity. Die ActionBar und die restliche grafische
     * Darstellung wird erzeugt. Benutzername und Passwort werden, wenn
     * gespeichert, abgerufen. NetworkService und ProfileCache werden beim Erststart
     * eingerichtet.
     *
     * @param savedInstanceState keine Bedeutung für diese Implementierung
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = getPreferences(Context.MODE_PRIVATE);
        tryLogin();

        setContentView(de.btcdev.eliteanimesapp.R.layout.activity_login);
        loginUserNameView = (EditText) findViewById(de.btcdev.eliteanimesapp.R.id.login_benutzername);
        loginPasswordView = (EditText) findViewById(de.btcdev.eliteanimesapp.R.id.login_passwort);
        loginButton = (Button) findViewById(de.btcdev.eliteanimesapp.R.id.login_button);
        loginCheckView = (CheckBox) findViewById(de.btcdev.eliteanimesapp.R.id.login_check);
        loginButton.setOnClickListener(this);

        ActionBar bar = getSupportActionBar();
        bar.setTitle("Login");

        if (prefs.contains("Benutzername"))
            loginUserNameView.setText(prefs.getString("Benutzername", null));
        if (prefs.contains("Passwort"))
            loginPasswordView.setText(prefs.getString("Passwort", null));
        if (prefs.contains("Checked"))
            loginCheckView.setChecked(prefs.getBoolean("Checked", false));
        handleNavigationDrawer(R.id.nav_login, R.id.nav_login_list, "Login",
                null);
    }

    @Override
    protected void injectDependencies() {
        ((EaApp) getApplication()).getEaComponent().inject(this);
    }

    /**
     * Wird aufgerufen, wenn die Activity pausiert wird. Dabei wird ein
     * laufender LoginTask abgebrochen, um Fehler zu vermeiden.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        if (loginTask != null) {
            loginTask.cancel(true);
        }
        removeDialog(load_dialog);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    public void tryLogin() {
        // überprüfe, ob Cookies vorhanden und geh zum Profile, wenn dies der
        // Fall ist
        if (networkService.hasCookies()
                && configurationService.getUserID(getApplicationContext()) != 0
                && configurationService.getUserName(getApplicationContext()) != null) {
            profileCache = ProfileCache.instance();
            if (profileCache.getOwnProfile() == null) {
                Profile temp = new Profile(
                        configurationService.getUserName(getApplicationContext()));
                temp.setUserId(configurationService.getUserID(getApplicationContext()));
                profileCache.setOwnProfile(temp);
            } else {
                Profile p = profileCache.getOwnProfile();
                Profile temp = new Profile(
                        configurationService.getUserName(getApplicationContext()));
                if (!p.equals(temp)) {
                    profileCache.deleteProfil(temp.getUserName());
                    profileCache.setOwnProfile(temp);
                    profileCache.contains(p.getUserName());
                }
            }
            Intent intent = new Intent(getApplicationContext(),
                    ProfileActivity.class);
            startActivity(intent);
        } else {
            // überprüfe in Einstellungen Auto-Login
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            boolean logout;
            if (bundle != null)
                logout = bundle.getBoolean("Logout", false);
            else
                logout = false;
            SharedPreferences defaultprefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            if (defaultprefs.getBoolean("pref_auto_login", false) && !logout) {
                userName = prefs.getString("Benutzername", "");
                password = prefs.getString("Passwort", "");
                if (!userName.equals("") && !password.equals("")) {
                    configurationService.setUserName(userName);
                    configurationService.setPassword(password);
                    loginTask = new LoginTask();
                    loginTask.execute("");
                }
            }
        }
    }

    /**
     * Das Optionsmenü wird erzeugt.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action actionBar if it is present.
        getMenuInflater().inflate(de.btcdev.eliteanimesapp.R.menu.login, menu);
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
     * Die Eingaben für Benutzername und Passwort werden in der configurationService
     * gespeichert, falls gewünscht auch im Dateisystem. Anschließend wird ein
     * neuer Login-Task aufgerufen.
     *
     * @param arg0 View, der angeklickt wurde
     */
    @Override
    public void onClick(View arg0) {
        if (arg0.getId() == de.btcdev.eliteanimesapp.R.id.login_button) {
            userName = loginUserNameView.getText().toString();
            password = loginPasswordView.getText().toString();
            configurationService.setUserName(userName);
            configurationService.setPassword(password);
            boolean checked = loginCheckView.isChecked();
            SharedPreferences.Editor meinEditor = prefs.edit();
            SharedPreferences defaultprefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            boolean savePassword = defaultprefs.getBoolean("pref_save_password",
                    true);
            if (checked) {
                if (savePassword) {
                    meinEditor.putString("Benutzername", configurationService
                            .getUserName(getApplicationContext()));
                    meinEditor.putString("Passwort",
                            configurationService.getPassword());
                } else {
                    meinEditor.putString("Benutzername", configurationService
                            .getUserName(getApplicationContext()));
                    if (prefs.contains("Passwort"))
                        meinEditor.remove("Passwort");
                }
            } else {
                if (prefs.contains("Benutzername"))
                    meinEditor.remove("Benutzername");
                if (prefs.contains("Passwort"))
                    meinEditor.remove("Passwort");
            }
            meinEditor.putBoolean("Checked", checked);
            meinEditor.apply();
            loginTask = new LoginTask();
            loginTask.execute("");
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
        switch (arg2) {
            case NAVIGATION_INFO:
                Intent intent = new Intent(this,
                        InfoActivity.class);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(intent);
                break;
            case NAVIGATION_SETTINGS:
                intent = new Intent(this,
                        SettingsActivity.class);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(intent);
                break;
            case NAVIGATION_LOGOUT:
                try {
                    loginService.logout();
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } catch (EAException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                Toast.makeText(this, "Login muss zuerst durchgeführt werden!",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Klasse für den Login-Prozess, die von AsyncTask erbt.
     */
    public class LoginTask extends AsyncTask<String, String, String> {

        /**
         * Ruft im Hintergrund die Login-Methode des verwendeten Netzwerks auf.
         * Falls ein Fehler auftritt, wird dieser als Toast ausgegeben.
         *
         * @param arg0 gibt an, was getan werden soll - für diesen Task nutzlos
         */
        @Override
        protected String doInBackground(String... arg0) {
            try {
                if (this.isCancelled())
                    return null;
                String input = loginService.login(userName, password);
                if (input != null)
                    loginService.parseLoginResult(input);
                if (this.isCancelled())
                    return null;
            } catch (EAException e) {
                publishProgress("Exception", e.getMessage());
            }
            return null;
        }

        /**
         * Öffnet einen Load-Dialog.
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
         * Falls der User schon eingeloggt ist, wird überprüft, ob das
         * eigene Profile schon im Cache gespeichert ist. Wenn nein, wird das neu
         * erzeugt und im Cache als eigenes Profile gesetzt. Danach wird
         * unabhängig davon versucht, die ProfileActivity zu starten und der
         * Dialog geschlossen.
         *
         * @param loginString wird für diese Implementierung nicht benötigt
         */
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String loginString) {
            if (loginService.isSomeoneLoggedIn()) {
                profileCache = ProfileCache.instance();
                if (profileCache.getOwnProfile() == null) {
                    Profile temp = new Profile(
                            configurationService
                                    .getUserName(getApplicationContext()));
                    temp.setUserId(configurationService
                            .getUserID(getApplicationContext()));
                    profileCache.setOwnProfile(temp);
                } else {
                    Profile p = profileCache.getOwnProfile();
                    Profile temp = new Profile(
                            configurationService
                                    .getUserName(getApplicationContext()));
                    if (!p.equals(temp)) {
                        profileCache.deleteProfil(temp.getUserName());
                        profileCache.setOwnProfile(temp);
                        profileCache.contains(p.getUserName());
                    }
                }
                Intent intent = new Intent(getApplicationContext(),
                        ProfileActivity.class);
                startActivity(intent);
                try {
                    dismissDialog(load_dialog);
                } catch (IllegalArgumentException e) {

                }
            } else {
                try {
                    dismissDialog(load_dialog);
                } catch (IllegalArgumentException e) {

                }
                Toast.makeText(
                        getApplicationContext(),
                        "Login nicht erfolgreich. Bitte überprüfe deine Eingaben!",
                        Toast.LENGTH_LONG).show();
            }
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        /**
         * Wird für die Fehlerausgabe "missbraucht", da keine
         * Fortschrittsanzeige benötigt wird.
         *
         * @param values String-Array, in dem an 0. Stelle "Exception" und an 1.
         *               Stelle die Fehlernachricht gespeichert sein soll
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
         * Der LoginTask wird unterbrochen.
         *
         * @param loginString für diese Implementierung unerheblich
         */
        @Override
        protected void onCancelled(String loginString) {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }
}
