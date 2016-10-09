package de.btcdev.eliteanimesapp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.PrivateMessage;
import de.btcdev.eliteanimesapp.json.JsonError;
import de.btcdev.eliteanimesapp.json.PrivateMessageDeserializer;


public class PrivateMessageService {

    private NetworkService networkService;

    @Inject
    public PrivateMessageService(NetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * Lädt die Postfachseite der angegebenen Seitenzahl des aktuellen Benutzers
     * und gibt die PNs als JSON-String zurück.
     *
     * @param page Seitenzahl des Postfaches
     * @return JSON der PNs
     * @throws EAException bei allen Fehlern
     */
    public String getPrivateMessagePage(int page) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("page", Integer
                .toString(page)));
        nvps.add(new BasicNameValuePair("bbcode", "false"));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(NetworkService.eaURL + "/api/getInboxPMs", nvps);
    }

    /**
     * Lädt den Inhalt einer ausgewählten PrivateMessage und gibt den HTML-Code dieser
     * zurück.
     *
     * @param id ID der PrivateMessage
     * @return JSON-Antwort der API
     * @throws EAException bei allen Fehlern
     */
    public String getPrivateMessage(int id) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(false)));
        return networkService.doPOST(NetworkService.eaURL + "/api/getPM", nvps);
    }

    /**
     * Parst den übergebenen String und aktualisiert die PrivateMessage.
     *
     * @param privateMessage    PrivateMessage, die aktualisiert werden soll.
     * @param input JSON-Code von getPrivateMessage
     * @return aktualisierte PrivateMessage
     */
    public PrivateMessage getPrivateMessage(PrivateMessage privateMessage, String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(input).getAsJsonObject();
            if (obj.has("id"))
                privateMessage.setId(obj.get("id").getAsInt());
            if (obj.has("f_uid"))
                privateMessage.setUserId(obj.get("f_uid").getAsInt());
            if (obj.has("f_uname"))
                privateMessage.setUserName(obj.get("f_uname").getAsString());
            if (obj.has("subject"))
                privateMessage.setSubject(obj.get("subject").getAsString());
            if (obj.has("date"))
                privateMessage.setDate(obj.get("date").getAsString());
            if (obj.has("text"))
                privateMessage.setMessage(obj.get("text").getAsString());
            privateMessage.setRead(true);
            return privateMessage;
        } catch (Exception e) {
            return privateMessage;
        }
    }

    /**
     * Lädt den Input (BBCode) einer ausgewählten PrivateMessage.
     *
     * @param id ID der PrivateMessage
     * @return JSON-Antwort der API
     * @throws EAException bei allen Fehlern
     */
    //TODO: is this method needed?
    public String getPrivateMessageInput(int id) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(true)));
        return networkService.doPOST(NetworkService.eaURL + "/api/getPM", nvps);
    }

    /**
     * Schickt eine PrivateMessage mit den übergebenen Werten ab und gibt den Rückgabestring
     * der API zurück
     *
     * @param userId  ID des Empfängers
     * @param message    Text der PrivateMessage
     * @param subject Betreff der PrivateMessage
     * @return Rückgabewert der API
     * @throws EAException bei allen Fehlern
     */
    //TODO: is this method needed?
    public String sendPrivateMessage(int userId, String message, String subject)
            throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("subject", subject));
        nvps.add(new BasicNameValuePair("uid", Integer.toString(userId)));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        nvps.add(new BasicNameValuePair("text", message));
        return networkService.doPOST(NetworkService.eaURL + "/api/sendPM", nvps);
    }

    /**
     * Löscht die PrivateMessage mit der übergebenen ID.
     *
     * @param id ID der PrivateMessage
     * @return Json-Antwort der API
     * @throws EAException bei allen Fehlern
     */
    public String deletePrivateMessage(String id) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", id));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(NetworkService.eaURL + "/api/deletePM/", nvps);
    }

    /**
     * Sendet eine Antwort der PrivateMessage mit der übergebenen Id.
     *
     * @param id   Id der PrivateMessage die beantwortet werden soll
     * @param message Text der PrivateMessage
     * @return JSON-Antwort der API
     */
    public String answerPrivateMessage(int id, String message) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("text", message));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(NetworkService.eaURL + "/api/answerPM", nvps);
    }

    /**
     * überprüft im übergebenen Json-Code, ob die PrivateMessage erfolgreich abgeschickt
     * wurde.
     *
     * @param input Json-Code der POST-Antwort
     * @return "Erfolg" oder Fehlermeldung
     */
    public String checkPrivateMessage(String input) {
        try {
            Gson gson = new Gson();
            JsonError error = gson.fromJson(input, JsonError.class);
            if (error.getError() == null || error.getError().isEmpty())
                return "Erfolg";
            else
                return error.getError();
        } catch (JsonSyntaxException e) {
            return "Erfolg";
        }
    }

    /**
     * Parst den übergebenen String nach den Daten für PNs und speichert diese
     * jeweils in einer ArrayList.
     *
     * @param input HTML-Code mit den PrivateMessage-Daten
     * @return ArrayList aus PNs
     */
    public ArrayList<PrivateMessage> getPrivateMessages(String input) {
        ArrayList<PrivateMessage> privateMessages = new ArrayList<PrivateMessage>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(PrivateMessage.class,
                    new PrivateMessageDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<PrivateMessage>>() {
            }.getType();
            privateMessages = gson.fromJson(input, collectionType);
        } catch (Exception e) {
            return privateMessages;
        }
        return privateMessages;
    }

    /**
     * Parst den übergebenen String nach PNs, die auf der nächsten Seite zu
     * finden sind, und fügt diese in die übergebene Liste ein.
     *
     * @param input  HTML-Code des Postfachs
     * @param privateMessages Liste mit PNs
     * @return Liste mit PNs
     */
    @SuppressWarnings("unchecked")
    public ArrayList<PrivateMessage> getMorePrivateMessages(String input, ArrayList<PrivateMessage> privateMessages) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(PrivateMessage.class,
                    new PrivateMessageDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<PrivateMessage>>() {
            }.getType();
            privateMessages.addAll((Collection<PrivateMessage>) gson.fromJson(input, collectionType));
            return privateMessages;
        } catch (Exception e) {
            return privateMessages;
        }
    }
}
