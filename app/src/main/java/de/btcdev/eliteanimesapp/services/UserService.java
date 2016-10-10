package de.btcdev.eliteanimesapp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.FriendRequest;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.User;
import de.btcdev.eliteanimesapp.json.FriendRequestDeserializer;
import de.btcdev.eliteanimesapp.json.SearchUserDeserializer;

public class UserService {
    
    private NetworkService networkService;
    private ConfigurationService configurationService;

    @Inject
    public UserService(NetworkService networkService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.configurationService = configurationService;
    }

    /**
     * Gibt den JSON-Code der Übersicht der blockierten User zurück
     *
     * @return JSON-Code der Tabelle
     * @throws EAException bei allen Fehlern
     */
    public ArrayList<FriendRequest> getBlockedUsers() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String result = networkService.doPOST(NetworkService.eaURL + "/api/getBlockedUser", nvps);
        return getBlockedUsersFromJson(result);
    }

    /**
     * Parst den übergebenen String nach den Informationen von Blockierten
     * Benutzern und gibt diese als ArrayList der Klasse FriendRequest
     * zurück.
     *
     * @param input Json-Code der Seite mit den blockierten Usern
     * @return ArrayList mit Blockierten Usern
     */
    private ArrayList<FriendRequest> getBlockedUsersFromJson(String input) {
        ArrayList<FriendRequest> blockedUsers = new ArrayList<FriendRequest>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    FriendRequest.class,
                    new FriendRequestDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<FriendRequest>>() {
            }.getType();
            blockedUsers = gson.fromJson(input, collectionType);
            return blockedUsers;
        } catch (Exception e) {
            return blockedUsers;
        }
    }

    /**
     * Hebt die Blockierung der übergebenen Benutzers auf.
     *
     * @param id ID des blockierten Benutzers
     * @throws EAException bei allen Fehlern
     */
    public void unblockUser(String id) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(NetworkService.eaURL + "/blockuser.php?id=" + id
                    + "&ft=" + configurationService.getBoardToken() + "&unblock");
        }
    }

    /**
     * Sucht nach Benutzern mit dem übergebenen Namen.
     *
     * @param userName Name, nach dem gesucht werden soll
     * @return JSON-Code der gefundenen User
     * @throws EAException bei allen Fehlern
     */
    public ArrayList<User> searchUser(String userName) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("name", userName));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String result = networkService.doPOST(NetworkService.eaURL + "/api/searchUser", nvps);
        return getSearchedUsersFromJson(result);
    }

    /**
     * Parst den übergebenen String nach den Nutzerinformationen der Suche und
     * gibt eine ArrayList mit den gewünschten Informationen zurück.
     *
     * @param input HTJSON-Code der Suche
     * @return ArrayList mit gefundenen Benutzern
     */
    private ArrayList<User> getSearchedUsersFromJson(String input) {
        ArrayList<User> result = new ArrayList<User>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(User.class,
                    new SearchUserDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<User>>() {
            }.getType();
            result = gson.fromJson(input, collectionType);
            return result;
        } catch (Exception e) {
            return result;
        }
    }
}
