package de.btcdev.eliteanimesapp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.ApiPath;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.Profile;
import de.btcdev.eliteanimesapp.json.JsonError;
import de.btcdev.eliteanimesapp.json.JsonErrorException;
import de.btcdev.eliteanimesapp.json.ProfileDeserializer;

public class ProfileService {

    private NetworkService networkService;
    private ImageService imageService;
    private ConfigurationService configurationService;

    @Inject
    public ProfileService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.imageService = imageService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt den HTML-Code des eigenen Profils und gibt diesen als String zurück.
     *
     * @return HTML-Code des eigenen Profils als String
     * @throws EAException bei Verbindungs- und Streamfehlern jeglicher Art
     */
    public Profile getProfile() throws EAException, JsonErrorException {
        return getProfile(configurationService.getUserID(null));
    }

    /**
     * Lädt den HTML-Code des Profils mit den übergebenen Daten und gibt diesen
     * als String zurück.
     *
     * @param userId       die UserID des gewünschten Profils
     * @return HTML-Code des Profils als String
     * @throws EAException bei Verbindungs- und Streamfehlern jeglicher Art
     */
    public Profile getProfile(int userId) throws EAException, JsonErrorException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(userId)));
        String result = networkService.doPOST(ApiPath.PROFILE, nvps);
        return getProfile(result);
    }

    /**
     * Parst den übergebenen String nach den tabellarischen Profildaten.
     *
     * @param input HTML-Code der Profilseite
     * @return neues Profile, das die geparsten Daten enthält
     * @throws EAException
     */
    private Profile getProfile(String input) throws EAException,
            JsonErrorException {
        Profile profile = null;
        if (input != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Profile.class,
                    new ProfileDeserializer()).create();
            try {
                profile = gson.fromJson(input, Profile.class);
                profile.setAvatar(imageService.getBitmapFromUrl(profile.getAvatarURL(), ImageService.profileSize));
            } catch (JsonParseException ex) {
                JsonError error = gson.fromJson(input, JsonError.class);
                throw new JsonErrorException(error.getError());
            }
            return profile;
        } else {
            return null;
        }
    }

    /**
     * Lädt die Profilbeschreibung mit der API-Funktion getProfilDescription und
     * gibt das JSON-Ergebnis zurück.
     *
     * @param userId       Id des Benutzers
     * @return JSON-Ergebnis
     * @throws EAException bei allen Fehlern
     */
    public String getProfileDescription(int userId) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(userId)));
        String result = networkService.doPOST(ApiPath.PROFILE_DESCRIPTION, nvps);
        return getProfileDescription(result);
    }

    /**
     * Parst den übergebenen JSON-String nach der Userbeschreibung und gibt
     * diese zurück.
     *
     * @param input JSON der Profilbeschreibung
     * @return die Userbeschreibung als String
     */
    private String getProfileDescription(String input) {
        String description = null;
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(input).getAsJsonObject();
        if (object.has("text")) {
            description = "<html><head><meta name=\"viewport\" content=\"width=device-width\"/></head><body>"
                    + object.get("text").getAsString() + "</body></html>";
        }
        return description;
    }
}
