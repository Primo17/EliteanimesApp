package de.btcdev.eliteanimesapp.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import de.btcdev.eliteanimesapp.json.BoardDeserializer;
import de.btcdev.eliteanimesapp.json.BoardPostDeserializer;
import de.btcdev.eliteanimesapp.json.BoardThreadDeserializer;
import de.btcdev.eliteanimesapp.json.FriendDeserializer;
import de.btcdev.eliteanimesapp.json.FriendRequestDeserializer;
import de.btcdev.eliteanimesapp.json.JsonError;
import de.btcdev.eliteanimesapp.json.JsonErrorException;
import de.btcdev.eliteanimesapp.json.CommentDeserializer;
import de.btcdev.eliteanimesapp.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.json.PrivateMessageDeserializer;
import de.btcdev.eliteanimesapp.json.ProfileDeserializer;
import de.btcdev.eliteanimesapp.json.SearchUserDeserializer;
import de.btcdev.eliteanimesapp.json.StatisticsDeserializer;

/**
 * Klasse für alle verwendeten Parsing-Aufgaben
 */
public class EAParser {

    private Context context;

    /**
     * Erzeugt einen neuen EAParser
     *
     * @param context Context des Aufrufers, benötigt für String-Ressourcen
     */
    public EAParser(Context context) {
        this.context = context;
    }

    public boolean parseLoginResult(String input) {
        try {
            if (input == null || input.isEmpty())
                return false;
            JsonParser jsonParser = new JsonParser();
            JsonObject obj = jsonParser.parse(input).getAsJsonObject();
            JsonElement token = obj.get("token");
            if (token != null) {
                Configuration.setBoardToken(token.getAsString());
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Parst den übergebenen String nach den tabellarischen Profildaten.
     *
     * @param input HTML-Code der Profilseite
     * @return neues Profile, das die geparsten Daten enthält
     * @throws EAException
     */
    public Profile getProfile(String input) throws EAException,
            JsonErrorException {
        Profile profile = null;
        if (input != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Profile.class,
                    new ProfileDeserializer()).create();
            try {
                profile = gson.fromJson(input, Profile.class);
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
     * Parst den übergebenen JSON-String nach der Userbeschreibung und gibt
     * diese zurück.
     *
     * @param input JSON der Profilbeschreibung
     * @return die Userbeschreibung als String
     */
    public String getProfileDescription(String input) {
        String description = null;
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(input).getAsJsonObject();
        if (object.has("text")) {
            description = "<html><head><meta name=\"viewport\" content=\"width=device-width\"/></head><body>"
                    + object.get("text").getAsString() + "</body></html>";
        }
        return description;
    }

    /**
     * Parst den übergebenen JSON-String nach Freunden und gibt die
     * Informationen in einer ArrayList aus Freunden zurück.
     *
     * @param input JSON mit Freunden
     * @return ArrayList mit den erhaltenen Informationen
     */
    public ArrayList<Friend> getFriendList(String input) {
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
     * Parst den übergeben JSON-String und gibt eine daraus resultierende
     * ArrayList aus Kommentaren zurück.
     *
     * @param input JSON-Antwort der API
     * @return eine ArrayList aus erhaltenen Kommentaren
     */
    public ArrayList<Comment> getComments(String input) {
        ArrayList<Comment> comments = new ArrayList<Comment>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Comment.class,
                    new CommentDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Comment>>() {
            }.getType();
            comments = gson.fromJson(input, collectionType);
            return comments;
        } catch (Exception e) {
            return comments;
        }
    }

    /**
     * Parst den übergebenen JSON-String nach den nächsten 5 Kommentaren und
     * fügt sie in die übergebene ArrayList ein
     *
     * @param input       JSON-Code, der geparst werden soll
     * @param comments ArrayList mit den schon vorhandenen Kommentaren
     * @return ArrayList aus alten und neuen Kommentaren
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Comment> getMoreComments(String input,
                                              ArrayList<Comment> comments) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Comment.class,
                    new CommentDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Comment>>() {
            }.getType();
            comments.addAll((Collection<Comment>) gson.fromJson(input,
                    collectionType));
        } catch (Exception e) {
            return comments;
        }
        return comments;
    }

    /**
     * überprüft im übergebenen HTML-Code, ob der Comment erfolgreich
     * abgeschickt wurde.
     *
     * @param input HTML-Code der POST-Antwort
     * @return Wahrheitswert ob Comment erfolgreich abgeschickt
     */
    public boolean checkComment(String input) {
        Document doc = Jsoup.parse(input);
        Elements response = doc.select("div.toolcol8");
        if (response.isEmpty())
            return false;
        return (response.text().equals("Ihr Kommentar wurde hinzugefügt."));
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
     * Parst den übergebenen String nach dem Input der empfangenen PrivateMessage, der bei
     * einer neuen Nachricht mitgeschickt werden muss.
     *
     * @param input HTML-Code der PrivateMessage
     * @return Input der PrivateMessage
     */
    public String getPrivateMessageInput(String input) {
        Document doc = Jsoup.parse(input);
        Elements privateMessageInput = doc.select("textarea");
        return privateMessageInput.text();
    }

    /**
     * Parst den übergebenen String nach den Informationen von
     * Freundschaftsanfragen und gibt diese als ArrayList der entsprechenden
     * Klasse zurück.
     *
     * @param input Json-Code der Seite mit den Freundschaftsanfragen
     * @return ArrayList mit Freundschaftsanfragen
     */
    public ArrayList<FriendRequest> getFriendRequests(String input) {
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
     * Parst den übergebenen String nach den Informationen von Blockierten
     * Benutzern und gibt diese als ArrayList der Klasse FriendRequest
     * zurück.
     *
     * @param input Json-Code der Seite mit den blockierten Usern
     * @return ArrayList mit Blockierten Usern
     */
    public ArrayList<FriendRequest> getBlockedUsers(String input) {
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
     * Parst den übergebenen String nach den Nutzerinformationen der Suche und
     * gibt eine ArrayList mit den gewünschten Informationen zurück.
     *
     * @param input HTJSON-Code der Suche
     * @return ArrayList mit gefundenen Benutzern
     */
    public ArrayList<User> getSearchedUsers(String input) {
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

    /**
     * Parst den übergebenen String nach den Informationen einer Animeliste und
     * speichert die Animes als ListAnime-Objekte in den übergebenen Listen.
     *
     * @param input          HTML-Code der Animeliste als String
     * @param complete       ArrayList für komplett gesehene Animes
     * @param watching      ArrayList für aktuell aktive Animes
     * @param stalled ArrayList für pausierte Animes
     * @param dropped    ArrayList für abgebrochene Animes
     * @param planned        ArrayList für abgebrochene Animes
     * @param ownList        Flag ob es die eigene Liste ist (tokenId sonst nicht
     *                       vorhanden)
     */
    @SuppressWarnings("unchecked")
    public void getListAnime(String input, ArrayList<ListAnime> complete,
                             ArrayList<ListAnime> watching,
                             ArrayList<ListAnime> stalled,
                             ArrayList<ListAnime> dropped, ArrayList<ListAnime> planned,
                             boolean ownList) {
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

        }
    }

    /**
     * Parst den übergebenen String nach Daten der Forenübersicht und gibt diese
     * entsprechend des ForenAdapters zurück.
     *
     * @param input Json-String der API-Funktion getForums
     * @return Eine Map der Forenübersicht
     */
    public TreeMap<Integer, ArrayList<Board>> getBoards(String input) {
        ArrayList<Board> allBoards = new ArrayList<Board>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Board.class,
                    new BoardDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Board>>() {
            }.getType();
            allBoards = gson.fromJson(input, collectionType);
            TreeMap<Integer, ArrayList<Board>> map = new TreeMap<Integer, ArrayList<Board>>();
            map.put(1, new ArrayList<Board>());
            map.put(2, new ArrayList<Board>());
            map.put(3, new ArrayList<Board>());
            map.put(4, new ArrayList<Board>());
            map.put(5, new ArrayList<Board>());
            for (Board f : allBoards) {
                map.get(f.getBoardCategoryId()).add(f);
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parst den String nach Daten der Forenstatistik.
     *
     * @param input Json-String der API-Funktion getStatistics
     * @return Die Forenstatistik als Statistics-Objekt
     */
    public Statistics getBoardStatistics(String input) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Statistics.class,
                    new StatisticsDeserializer()).create();
            Type collectionType = new TypeToken<Statistics>() {
            }.getType();
            Statistics statistics = gson.fromJson(input, collectionType);
            return statistics;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parst den String nach der Seitenzahl der Threadübersicht.
     *
     * @param input Json-String der API-Funktion getForum
     * @return Seitenzahl der Threadübersicht oder 0 bei Fehlern
     */
    public int getBoardThreadPageCount(String input) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(input);
        if (!element.isJsonObject()) {
            return 0;
        }
        JsonObject obj = element.getAsJsonObject();
        try {
            return obj.get("pages").getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parst den String nach Daten der Threadübersicht.
     *
     * @param input Json-String der API-Funktion getForum
     * @return ArrayList mit den gefundenen Thread-Daten oder null bei Fehlern
     */
    public ArrayList<BoardThread> getBoardThreads(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(input);
            JsonObject obj = el.getAsJsonObject();
            el = obj.get("threads");
            if (el == null)
                return null;
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    BoardThread.class, new BoardThreadDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<BoardThread>>() {
            }.getType();
            ArrayList<BoardThread> list = gson.fromJson(el, collectionType);
            if (list == null)
                list = new ArrayList<BoardThread>();
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parst den String nach Daten der Posts.
     *
     * @param input Json-String der API-Funktion getForumThread
     * @return ArrayList mit den gefundenen Post-Daten oder null bei Fehlern
     */
    @SuppressWarnings("unchecked")
    public ArrayList<BoardPost> getBoardPosts(String input) {
        try {
            ArrayList<BoardPost> boardPosts = new ArrayList<BoardPost>();
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(input);
            JsonObject obj = el.getAsJsonObject();
            BoardPost first = new BoardPost();
            if (obj.has("thread_id"))
                first.setId(0);
            if (obj.has("thread_date"))
                first.setDate(obj.get("thread_date").getAsString());
            if (obj.has("text"))
                first.setText(obj.get("text").getAsString());
            if (obj.has("edited"))
                first.setEditedCount(obj.get("edited").getAsInt());
            if (obj.has("edited_time"))
                first.setEditedTime(obj.get("edited_time").getAsString());
            if (obj.has("uname"))
                first.setUserName(obj.get("uname").getAsString());
            if (obj.has("uid"))
                first.setUserId(obj.get("uid").getAsInt());
            if (obj.has("user_level"))
                first.setUserLevel(obj.get("user_level").getAsInt());
            if (obj.has("regdate"))
                first.setUserDate(obj.get("regdate").getAsString());
            if (obj.has("online"))
                first.setOnline(obj.get("online").getAsInt() == 1);
            if (obj.has("signatur"))
                first.setSignature(obj.get("signatur").getAsString());
            if (obj.has("geschlecht")) {
                String gender = obj.get("geschlecht").getAsString();
                if (gender.equals("m"))
                    first.setSex("Männlich");
                else if (gender.equals("w"))
                    first.setSex("Weiblich");
                else
                    first.setSex("Nicht angegeben");
            }
            if (obj.has("user_image"))
                first.setAvatar(obj.get("user_image").getAsString());
            boardPosts.add(first);
            el = obj.get("posts");
            if (el == null)
                return null;
            Gson gson = new GsonBuilder().registerTypeAdapter(BoardPost.class,
                    new BoardPostDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<BoardPost>>() {
            }.getType();
            boardPosts.addAll((Collection<BoardPost>) gson.fromJson(el,
                    collectionType));
            return boardPosts;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parst den String nach der Seitenzahl der Threadseite.
     *
     * @param input Json-String der API-Funktion getForumThread
     * @return Seitenzahl der Threadseite oder 0 bei Fehlern
     */
    public int getForumPostsPageCount(String input) {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(input);
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            try {
                return obj.get("pages").getAsInt();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Parst den String nach dem Post-Text.
     *
     * @param input Antwort der Api-Funktion getForumPost
     * @return Text des Posts
     */
    public String getPost(String input) {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(input);
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            try {
                return obj.get("text").getAsString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Parst den übergebenen String nach den Neuigkeiten (Neue Nachrichten und
     * Kommentare) und setzt die erhaltenen Werte in der Configuration.
     *
     * @param input JSON der die News enthält
     */
    public void getNotifications(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            int commentcount = 0;
            int pncount = 0;
            if (object.has("pm")) {
                pncount = object.get("pm").getAsInt();
            }
            if (object.has("comment")) {
                commentcount = object.get("comment").getAsInt();
            }
            Configuration.setNewCommentCount(commentcount, context);
            Configuration.setNewMessageCount(pncount, context);
        } catch (Exception e) {

        }
    }

    /**
     * Parst den übergebenen JSON-String der API-Funktion getToken nach dem
     * Token und setzt diesen in der Configuration.
     *
     * @param input JSON-String der den Token enthält
     */
    public void getToken(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            if (object.has("token")) {
                Configuration.setBoardToken(object.get("token").getAsString());
            }
        } catch (Exception e) {

        }
    }

    /**
     * Parst den übergebenen String nach der Kopfzeile und überprüft, ob der
     * User eingeloggt ist.
     *
     * @param input HTML-Code einer Seite auf EA
     * @return Wahrheitswert, ob der User eingeloggt ist
     */
    public boolean isLoggedIn(String input) {
        Document doc = Jsoup.parse(input);
        Elements content = doc.select("div.head div");
        if (content != null && content.size() > 0) {
            String status = content.attr("class");
            return status.equals("profilbuttons");
        } else {
            return false;
        }
    }

    /**
     * Durchsucht den Text nach Spoiler-Tags und verändert ihn je nach
     * Anzeigeart.
     *
     * @param show Ob der Spoiler angezeigt werden soll
     * @param text Der Text, in dem Spoiler-Tags vorhanden sind
     * @return Veränderter Text mit Spoiler-Anzeige
     */
    public String showSpoiler(boolean show, String text) {
        Document doc = Jsoup.parse(text);
        Elements elements = doc.select("div.spoiler");
        Element temp = null;
        StringBuilder builder = null;
        for (int i = 0; i < elements.size(); i++) {
            builder = new StringBuilder();
            temp = elements.get(i);
            // Spoiler-Überschrift
            if (i % 2 == 0) {
                builder.append("<b>");
                builder.append(temp.html());
                builder.append("</b>");
                temp.html(builder.toString());
            }
            // Spoiler-Inhalt
            else {
                if (!show) {
                    temp.attr("style",
                            "border-top: medium none; font-weight: normal; height: auto; display: none;");
                    builder.append("<font color=\"#ffffff\">");
                    builder.append(temp.html());
                    builder.append("</font>");
                } else {
                    temp.attr("style",
                            "border-top: medium none; font-weight: normal; height: auto; display: block;");
                    builder.append(temp.html());
                }
                temp.html(builder.toString());
            }
        }
        return doc.toString();
    }

    public String isError(String json) {
        String error = "";
        JsonParser parser = new JsonParser();
        JsonElement element;
        try {
            element = parser.parse(json);
        } catch (Exception e) {
            return "Ung�ltige Antwort vom Server.";
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            try {
                return object.get("error").getAsString();
            } catch (Exception e) {
                return "";
            }
        }
        return error;
    }
}
