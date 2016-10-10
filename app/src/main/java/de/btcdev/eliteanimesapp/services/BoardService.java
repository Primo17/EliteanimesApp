package de.btcdev.eliteanimesapp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.Board;
import de.btcdev.eliteanimesapp.data.BoardPost;
import de.btcdev.eliteanimesapp.data.BoardThread;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.Statistics;
import de.btcdev.eliteanimesapp.json.BoardDeserializer;
import de.btcdev.eliteanimesapp.json.BoardPostDeserializer;
import de.btcdev.eliteanimesapp.json.BoardThreadDeserializer;
import de.btcdev.eliteanimesapp.json.StatisticsDeserializer;

public class BoardService {
    
    private NetworkService networkService;
    private ImageService imageService;
    private ConfigurationService configurationService;

    @Inject
    public BoardService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.imageService = imageService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt die Forenübersicht
     *
     * @return Json-String der Forenübersicht
     * @throws EAException bei allen Netzwerkfehlern
     */
    public TreeMap<Integer, ArrayList<Board>> getBoards() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String result = networkService.doPOST(NetworkService.eaURL + "/api/getForums", nvps);
        return getBoardsFromJson(result);
    }

    /**
     * Parst den übergebenen String nach Daten der Forenübersicht und gibt diese
     * entsprechend des ForenAdapters zurück.
     *
     * @param input Json-String der API-Funktion getForums
     * @return Eine Map der Forenübersicht
     */
    private TreeMap<Integer, ArrayList<Board>> getBoardsFromJson(String input) {
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
     * Lädt die Forenstatistik
     *
     * @return Json-String der Forenstatistik
     * @throws EAException bei allen Netzwerkfehlern
     */
    public Statistics getBoardStatistics() throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String result = networkService.doPOST(NetworkService.eaURL + "/api/getStatistics", nvps);
        return getBoardStatisticsFromJson(result);
    }

    /**
     * Parst den String nach Daten der Forenstatistik.
     *
     * @param input Json-String der API-Funktion getStatistics
     * @return Die Forenstatistik als Statistics-Objekt
     */
    public Statistics getBoardStatisticsFromJson(String input) {
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
     * Lädt die Threadübersicht eines Forums
     *
     * @param id   Die Id des Forums
     * @param page Die Seite des Forums
     * @return Json-String der Thread�bersicht
     * @throws EAException bei allen Netzwerkfehlern
     */
    public String getThreads(int id, int page) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("page", Integer.toString(page)));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(NetworkService.eaURL + "/api/getForum", nvps);
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
     * Lädt die Posts eines Threads
     *
     * @param id   Die Id des Threads
     * @param page Die Seite des Threads
     * @return Json-String der Threadübersicht
     * @throws EAException bei allen Netzwerkfehlern
     */
    public String getPosts(int id, int page) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("page", Integer.toString(page)));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        return networkService.doPOST(NetworkService.eaURL + "/api/getForumThread", nvps);
    }

    /**
     * Schickt den übergebenen Post-Text an die API und überprüft, ob der Post
     * erfolgreich übermittelt wurde.
     *
     * @param text     Text des Posts
     * @param threadId Id des Threads
     * @return Wahrheitswert ob Senden erfolgreich
     */
    public boolean addPost(String text, int threadId) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(threadId)));
        nvps.add(new BasicNameValuePair("text", text));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String input = networkService.doPOST(NetworkService.eaURL + "/api/addForumPost", nvps);
        return input.contains("status");
    }

    /**
     * Schickt den übergebenen Post-Text an die API und überprüft, ob der Post
     * erfolgreich übermittelt wurde.
     *
     * @param text   Text des Posts
     * @param postId Id des Posts
     * @return Wahrheitswert ob Senden erfolgreich
     */
    public boolean editPost(String text, int postId) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(postId)));
        nvps.add(new BasicNameValuePair("text", text));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String input = networkService.doPOST(NetworkService.eaURL + "/api/editForumPost", nvps);
        return input.contains("status");
    }

    /**
     * Löscht einen Forumpost.
     *
     * @param postId Id des Posts
     * @return Wahrheitswert ob Senden erfolgreich
     */
    public boolean deletePost(int postId) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(postId)));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String input = networkService.doPOST(NetworkService.eaURL + "/api/deleteForumPost", nvps);
        return input.contains("status");
    }

    /**
     * Lädt den gewünschten Board-Post.
     *
     * @param id     Id des Posts
     * @param bbcode Ob der Post als bbcode geladen werden soll
     * @return Text des Posts als String
     * @throws EAException Bei allen Fehlern
     */
    public String getPost(int id, boolean bbcode) throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(id)));
        nvps.add(new BasicNameValuePair("bbcode", Boolean.toString(bbcode)));
        nvps.add(new BasicNameValuePair("apikey", networkService.getApikey()));
        String result = networkService.doPOST(NetworkService.eaURL + "/api/getForumPost", nvps);
        return getPostFromJson(result);
    }

    /**
     * Parst den String nach dem Post-Text.
     *
     * @param input Antwort der Api-Funktion getForumPost
     * @return Text des Posts
     */
    private String getPostFromJson(String input) {
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
            if (obj.has("user_image")) {
                first.setAvatarURL(obj.get("user_image").getAsString());
                //TODO call image service the right way
                first.setAvatar(imageService.getBitmapFromUrl(first.getAvatarURL(), ImageService.boardPostSize));
            }
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
            for(BoardPost boardPost: boardPosts) {
                boardPost.setAvatar(imageService.getBitmapFromUrl(boardPost.getAvatarURL(), ImageService.boardPostSize));
            }
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

}
