package de.btcdev.eliteanimesapp.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.btcdev.eliteanimesapp.data.ConfigurationService;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;

@Singleton
public class LoginService {

    //TODO: remove the other methods from networkService and eaParser!

    private String eaURL = "http://www.eliteanimes.com";

    private NetworkService networkService;
    private ConfigurationService configurationService;

    @Inject
    public LoginService(NetworkService networkService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.configurationService = configurationService;
    }

    /**
     * Es wird versucht, den User mit den in der ConfigurationService gesetzten
     * Benutzernamen und Passwort einzuloggen. Zuvor wird überprüft, ob schon
     * ein passender Cookie existiert, der User also schon eingeloggt ist.
     * Wenn nicht, werden die Cookies von EA geladen und eine Post-Anfrage mit
     * den entsprechenden Daten abgeschickt. Bei erfolgreichem Login werden die
     * jeweiligen Cookies gespeichert und die UserID in der ConfigurationService
     * gesetzt.
     *
     * @throws EAException wenn ein Verbindungsfehler jeglicher Art auftritt
     */
    public String login(String userName, String password) throws EAException {
        if (!isLoggedIn(userName)) {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("name", userName));
            nvps.add(new BasicNameValuePair("password", password));
            nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
            String input = networkService.doPOST(eaURL + "/api/login", nvps);
            int userId = networkService.getIdByCookie();
            networkService.saveCookies();
            if (userId != 0)
                configurationService.setUserId(userId);
            return input;
        }
        return null;
    }

    /**
     * Falls ein User eingeloggt ist, wird dieser ausgeloggt. Dazu werden
     * alle Login-Cookies gelöscht. (Der gesamte Vorgang wird in einem eigenen
     * Thread ausgeführt)
     *
     * @throws EAException
     */
    public void logout() throws EAException {
        if (isSomeoneLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        List<NameValuePair> nvps = new ArrayList<>();
                        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
                        networkService.doPOST(eaURL + "/api/logout", nvps);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }).start();
            networkService.deleteCookies();
        }
    }

    /**
     * Überprüft, ob schon Login-Cookies vorhanden sind und ob diese mit dem
     * Benutzernamen der ConfigurationService übereinstimmen.
     *
     * @return Wahrheitswert, ob der aktuelle User schon eingeloggt ist
     */
    public boolean isLoggedIn(String expectedUserName) {
        String userName = networkService.getUserByCookie();
        return userName != null && expectedUserName != null && expectedUserName.equals(userName);
    }

    public boolean isSomeoneLoggedIn() {
        return !networkService.getUserByCookie().isEmpty();
    }

    /**
     * L�dt den Token als JSON.
     *
     * @return JSON mit dem Token
     * @throws EAException bei allen Fehlern
     */
    public String getToken() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(eaURL + "/api/getToken", nvps);
    }

    public boolean parseLoginResult(String input) {
        try {
            if (input == null || input.isEmpty())
                return false;
            JsonParser jsonParser = new JsonParser();
            JsonObject obj = jsonParser.parse(input).getAsJsonObject();
            JsonElement token = obj.get("token");
            if (token != null) {
                configurationService.setBoardToken(token.getAsString());
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Parst den übergebenen JSON-String der API-Funktion getToken nach dem
     * Token und setzt diesen in der ConfigurationService.
     *
     * @param input JSON-String der den Token enthält
     */
    public void getToken(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            if (object.has("token")) {
                configurationService.setBoardToken(object.get("token").getAsString());
            }
        } catch (Exception e) {

        }
    }

}