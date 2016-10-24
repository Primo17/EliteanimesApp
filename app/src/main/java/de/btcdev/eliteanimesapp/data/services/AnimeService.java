package de.btcdev.eliteanimesapp.data.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.ApiPath;
import de.btcdev.eliteanimesapp.data.exceptions.EAException;
import de.btcdev.eliteanimesapp.data.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.data.models.ListAnime;

public class AnimeService {
    
    private NetworkService networkService;
    private ConfigurationService configurationService;

    @Inject
    public AnimeService(NetworkService networkService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt die Animeliste des übergebenen Benutzers und gibt den HTML-Code der
     * Seite als String zurück.
     *
     * @param id   Id des Benutzers
     * @return String der HTML-Seite
     * @throws EAException bei allen Netzwerkfehlern
     */
    public String getAnimeList(int id) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        return networkService.doPOST(ApiPath.ANIME_LIST, nvps);
    }

    /**
     * Parst den übergebenen String nach den Informationen einer Animeliste und
     * speichert die Animes als ListAnime-Objekte in den übergebenen Listen.
     *
     * @param input    HTML-Code der Animeliste als String
     * @param complete ArrayList für komplett gesehene Animes
     * @param watching ArrayList für aktuell aktive Animes
     * @param stalled  ArrayList für pausierte Animes
     * @param dropped  ArrayList für abgebrochene Animes
     * @param planned  ArrayList für abgebrochene Animes
     */
    @SuppressWarnings("unchecked")
    public void getListAnime(String input, ArrayList<ListAnime> complete,
                             ArrayList<ListAnime> watching,
                             ArrayList<ListAnime> stalled,
                             ArrayList<ListAnime> dropped, ArrayList<ListAnime> planned) {

        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            JsonArray jsonComplete, jsonWatching, jsonStalled, jsonDropped, jsonPlanned;
            Gson gson;
            if (object.has("1")) {
                jsonWatching = object.get("1").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                watching.clear();
                watching.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonWatching, collectionType));
            }
            if (object.has("2")) {
                jsonComplete = object.get("2").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                complete.clear();
                complete.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonComplete, collectionType));
            }
            if (object.has("3")) {
                jsonStalled = object.get("3").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                stalled.clear();
                stalled.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonStalled, collectionType));
            }
            if (object.has("4")) {
                jsonDropped = object.get("4").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                dropped.clear();
                dropped.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonDropped, collectionType));
            }
            if (object.has("5")) {
                jsonPlanned = object.get("5").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                planned.clear();
                planned.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonPlanned, collectionType));
            }
        } catch (Exception e) {
            System.out.println("Failure while getting anime list: " + e);
        }
    }

    /**
     * Bewertet den übergebenen Anime. Status: 1: Am Schauen 2: Komplett 3: Kurz
     * Aufgehürt 4: Abgebrochen 5: Geplant
     *
     * @param id          ID des Animes
     * @param score       Punktzahl des Animes
     * @param progress Fortschritt des Benutzers
     * @param episodeCount  Gesamte Folgenzahl des Animes
     * @param status      Status des Animes
     * @throws EAException bei allen Fehlern
     */
    public void rateAnime(int id, double score, int progress,
                          int episodeCount, int status) throws EAException {
        if (configurationService.getBoardToken() != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("bewerten", "Bewerten"));
            nvps.add(new BasicNameValuePair("forumtoken", configurationService
                    .getBoardToken()));
            nvps.add(new BasicNameValuePair("id", "" + id));
            nvps.add(new BasicNameValuePair("score", "" + score));
            nvps.add(new BasicNameValuePair("seen", "" + progress));
            nvps.add(new BasicNameValuePair("status", "" + status));
            nvps.add(new BasicNameValuePair("von", "" + episodeCount));
            //TODO: check if the operation fails when the apikey is send too, before it was not
            networkService.doPOST(ApiPath.ANIME_RATE
                    + configurationService.getUserID(null) + "/"
                    + configurationService.getUserName(null), nvps);
        }
    }


}
