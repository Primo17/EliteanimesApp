package de.btcdev.eliteanimesapp.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.ApiPath;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;

public class NotificationService {

    private NetworkService networkService;
    private ConfigurationService configurationService;

    @Inject
    public NotificationService(NetworkService networkService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt dir Anzahl neuer Kommentare und PNs und setzt die erhaltenen Werte im ConfigurationService.
     */
    public void getNotifications() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        String result = networkService.doPOST(ApiPath.NEWS, nvps);
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(result).getAsJsonObject();
            int commentcount = 0;
            int pncount = 0;
            if (object.has("pm")) {
                pncount = object.get("pm").getAsInt();
            }
            if (object.has("comment")) {
                commentcount = object.get("comment").getAsInt();
            }
            configurationService.setNewCommentCount(commentcount, null);
            configurationService.setNewMessageCount(pncount, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
