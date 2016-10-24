package de.btcdev.eliteanimesapp.data.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.ApiPath;
import de.btcdev.eliteanimesapp.data.exceptions.EAException;
import de.btcdev.eliteanimesapp.data.json.FriendDeserializer;
import de.btcdev.eliteanimesapp.data.json.FriendRequestDeserializer;
import de.btcdev.eliteanimesapp.data.models.Friend;
import de.btcdev.eliteanimesapp.data.models.FriendRequest;

public class FriendService {
    
    private NetworkService networkService;
    private ConfigurationService configurationService;

    @Inject
    public FriendService(NetworkService networkService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt den HTML-Code der Freundesliste mit den übergebenen Daten und gibt
     * diesen als String zurück.
     *
     * @param userId       die UserID des Profils, zu der die Freundesliste gehört
     * @return HTML-Code der Freundesliste als String
     * @throws EAException bei Verbindungs- und Streamfehlern jeglicher Art
     */
    public ArrayList<Friend> getFriendList(int userId)
            throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(userId)));
        String result = networkService.doPOST(ApiPath.FRIENDS, nvps);
        return getFriendListFromJson(result);
    }

    /**
     * Parst den übergebenen JSON-String nach Freunden und gibt die
     * Informationen in einer ArrayList aus Freunden zurück.
     *
     * @param input JSON mit Freunden
     * @return ArrayList mit den erhaltenen Informationen
     */
    private ArrayList<Friend> getFriendListFromJson(String input) {
        ArrayList<Friend> friendList = new ArrayList<Friend>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Friend.class,
                    new FriendDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Friend>>() {
            }.getType();
            friendList = gson.fromJson(input, collectionType);
        } catch (Exception e) {
            return friendList;
        }
        return friendList;
    }

    /**
     * Fügt den User mit der übergebenen User-ID zu den Freunden hinzu.
     *
     * @param id User-ID des Benutzers der hinzugefügt werden soll
     * @throws EAException
     */
    public void addFriend(String id) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(ApiPath.FRIEND_ADD + id + "/"
                    + configurationService.getBoardToken());
        }
    }

    /**
     * Löscht den Friend mit der übergebenen User-ID.
     *
     * @param id User-ID des Freunds, der gelöscht werden soll
     * @throws EAException
     */
    public void deleteFriend(String id) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(ApiPath.FRIEND_DELETE + id
                    + "/" + configurationService.getBoardToken());
        }
    }

    /**
     * Lädt den JSON-Code der Freundschaftsanfragen und gibt diesen zurück.
     *
     * @return JSON-Code der Freundschaftsanfragen
     * @throws EAException Bei Fehlern aller Art
     */
    public ArrayList<FriendRequest> getFriendRequests() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        String result = networkService.doPOST(ApiPath.FRIEND_REQUESTS, nvps);
        return getFriendRequestsFromJson(result);
    }


    /**
     * Parst den übergebenen String nach den Informationen von
     * Freundschaftsanfragen und gibt diese als ArrayList der entsprechenden
     * Klasse zurück.
     *
     * @param input Json-Code der Seite mit den Freundschaftsanfragen
     * @return ArrayList mit Freundschaftsanfragen
     */
    private ArrayList<FriendRequest> getFriendRequestsFromJson(String input) {
        ArrayList<FriendRequest> friendRequests = new ArrayList<FriendRequest>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    FriendRequest.class,
                    new FriendRequestDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<FriendRequest>>() {
            }.getType();
            friendRequests = gson.fromJson(input, collectionType);
            return friendRequests;
        } catch (Exception e) {
            return friendRequests;
        }
    }

    /**
     * Akzeptiert die FriendRequest des Users mit der übergebenen ID.
     *
     * @param id ID der Users
     * @throws EAException Bei allen Fehlern
     */
    public void acceptFriendRequest(String id) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(ApiPath.FRIEND_REQUEST_ACCEPT + id
                    + "/" + configurationService.getBoardToken());
        }
    }

    /**
     * Lehnt die FriendRequest des übergebenen Users ab.
     *
     * @param id ID des Users
     * @throws EAException Bei Fehlern aller Art
     */
    public void declineFriendRequest(String id) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(ApiPath.FRIEND_REQUEST_DECLINE + id
                    + "/" + configurationService.getBoardToken());
        }
    }
}
