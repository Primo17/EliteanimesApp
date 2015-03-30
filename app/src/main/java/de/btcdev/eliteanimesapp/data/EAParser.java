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

import de.btcdev.eliteanimesapp.json.ForumDeserializer;
import de.btcdev.eliteanimesapp.json.ForumPostDeserializer;
import de.btcdev.eliteanimesapp.json.ForumThreadDeserializer;
import de.btcdev.eliteanimesapp.json.FreundDeserializer;
import de.btcdev.eliteanimesapp.json.FreundschaftsanfrageDeserializer;
import de.btcdev.eliteanimesapp.json.JsonError;
import de.btcdev.eliteanimesapp.json.JsonErrorException;
import de.btcdev.eliteanimesapp.json.KommentarDeserializer;
import de.btcdev.eliteanimesapp.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.json.PNDeserializer;
import de.btcdev.eliteanimesapp.json.ProfilDeserializer;
import de.btcdev.eliteanimesapp.json.SearchUserDeserializer;
import de.btcdev.eliteanimesapp.json.StatistikDeserializer;

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
                Konfiguration.setForumtoken(token.getAsString());
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
     * @return neues Profil, das die geparsten Daten enthält
     * @throws EAException
     */
    public Profil getProfilDaten(String input) throws EAException,
            JsonErrorException {
        Profil profil = null;
        if (input != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Profil.class,
                    new ProfilDeserializer()).create();
            try {
                profil = gson.fromJson(input, Profil.class);
            } catch (JsonParseException ex) {
                JsonError error = gson.fromJson(input, JsonError.class);
                throw new JsonErrorException(error.getError());
            }
            return profil;
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
    public String getProfilBeschreibung(String input) {
        String ausgabe = null;
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(input).getAsJsonObject();
        if (object.has("text")) {
            ausgabe = "<html><head><meta name=\"viewport\" content=\"width=device-width\"/></head><body>"
                    + object.get("text").getAsString() + "</body></html>";
        }
        return ausgabe;
    }

    /**
     * Parst den übergebenen JSON-String nach Freunden und gibt die
     * Informationen in einer ArrayList aus Freunden zurück.
     *
     * @param input JSON mit Freunden
     * @return ArrayList mit den erhaltenen Informationen
     */
    public ArrayList<Freund> getFreundesliste(String input) {
        ArrayList<Freund> freundeliste = new ArrayList<Freund>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Freund.class,
                    new FreundDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Freund>>() {
            }.getType();
            freundeliste = gson.fromJson(input, collectionType);
        } catch (Exception e) {
            return freundeliste;
        }
        return freundeliste;
    }

    /**
     * Parst den übergeben JSON-String und gibt eine daraus resultierende
     * ArrayList aus Kommentaren zurück.
     *
     * @param input JSON-Antwort der API
     * @return eine ArrayList aus erhaltenen Kommentaren
     */
    public ArrayList<Kommentar> getComments(String input) {
        ArrayList<Kommentar> commentlist = new ArrayList<Kommentar>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Kommentar.class,
                    new KommentarDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Kommentar>>() {
            }.getType();
            commentlist = gson.fromJson(input, collectionType);
            return commentlist;
        } catch (Exception e) {
            return commentlist;
        }
    }

    /**
     * Parst den übergebenen JSON-String nach den nächsten 5 Kommentaren und
     * fügt sie in die übergebene ArrayList ein
     *
     * @param input       JSON-Code, der geparst werden soll
     * @param commentlist ArrayList mit den schon vorhandenen Kommentaren
     * @return ArrayList aus alten und neuen Kommentaren
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Kommentar> getMoreComments(String input,
                                                ArrayList<Kommentar> commentlist) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Kommentar.class,
                    new KommentarDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Kommentar>>() {
            }.getType();
            commentlist.addAll((Collection<Kommentar>) gson.fromJson(input,
                    collectionType));
        } catch (Exception e) {
            return commentlist;
        }
        return commentlist;
    }

    /**
     * überprüft im übergebenen HTML-Code, ob der Kommentar erfolgreich
     * abgeschickt wurde.
     *
     * @param input HTML-Code der POST-Antwort
     * @return Wahrheitswert ob Kommentar erfolgreich abgeschickt
     */
    public boolean checkComment(String input) {
        Document doc = Jsoup.parse(input);
        Elements antwort = doc.select("div.toolcol8");
        if (antwort.isEmpty())
            return false;
        return (antwort.text().equals("Ihr Kommentar wurde hinzugefügt."));
    }

    /**
     * überprüft im übergebenen Json-Code, ob die PN erfolgreich abgeschickt
     * wurde.
     *
     * @param input Json-Code der POST-Antwort
     * @return "Erfolg" oder Fehlermeldung
     */
    public String checkPN(String input) {
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
     * @param input HTML-Code mit den PN-Daten
     * @return ArrayList aus PNs
     */
    public ArrayList<PN> getPNs(String input) {
        ArrayList<PN> pnlist = new ArrayList<PN>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(PN.class,
                    new PNDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<PN>>() {
            }.getType();
            pnlist = gson.fromJson(input, collectionType);
        } catch (Exception e) {
            return pnlist;
        }
        return pnlist;
    }

    /**
     * Parst den übergebenen String nach PNs, die auf der nächsten Seite zu
     * finden sind, und fügt diese in die übergebene Liste ein.
     *
     * @param input  HTML-Code des Postfachs
     * @param pnlist Liste mit PNs
     * @return Liste mit PNs
     */
    @SuppressWarnings("unchecked")
    public ArrayList<PN> getMorePNs(String input, ArrayList<PN> pnlist) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(PN.class,
                    new PNDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<PN>>() {
            }.getType();
            pnlist.addAll((Collection<PN>) gson.fromJson(input, collectionType));
            return pnlist;
        } catch (Exception e) {
            return pnlist;
        }
    }

    /**
     * Parst den übergebenen String und aktualisiert die PN.
     *
     * @param pn    PN, die aktualisiert werden soll.
     * @param input JSON-Code von getPN
     * @return aktualisierte PN
     */
    public PN getPN(PN pn, String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(input).getAsJsonObject();
            if (obj.has("id"))
                pn.setId(obj.get("id").getAsInt());
            if (obj.has("f_uid"))
                pn.setUserid(obj.get("f_uid").getAsInt());
            if (obj.has("f_uname"))
                pn.setBenutzername(obj.get("f_uname").getAsString());
            if (obj.has("subject"))
                pn.setBetreff(obj.get("subject").getAsString());
            if (obj.has("date"))
                pn.setDate(obj.get("date").getAsString());
            if (obj.has("text"))
                pn.setText(obj.get("text").getAsString());
            pn.setGelesen(true);
            return pn;
        } catch (Exception e) {
            return pn;
        }
    }

    /**
     * Parst den übergebenen String nach dem Input der empfangenen PN, der bei
     * einer neuen Nachricht mitgeschickt werden muss.
     *
     * @param input HTML-Code der PN
     * @return Input der PN
     */
    public String getPNInput(String input) {
        Document doc = Jsoup.parse(input);
        Elements pninput = doc.select("textarea");
        return pninput.text();
    }

    /**
     * Parst den übergebenen String nach den Informationen von
     * Freundschaftsanfragen und gibt diese als ArrayList der entsprechenden
     * Klasse zurück.
     *
     * @param input Json-Code der Seite mit den Freundschaftsanfragen
     * @return ArrayList mit Freundschaftsanfragen
     */
    public ArrayList<Freundschaftsanfrage> getFreundschaftsanfragen(String input) {
        ArrayList<Freundschaftsanfrage> list = new ArrayList<Freundschaftsanfrage>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    Freundschaftsanfrage.class,
                    new FreundschaftsanfrageDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Freundschaftsanfrage>>() {
            }.getType();
            list = gson.fromJson(input, collectionType);
            return list;
        } catch (Exception e) {
            return list;
        }
    }

    /**
     * Parst den übergebenen String nach den Informationen von Blockierten
     * Benutzern und gibt diese als ArrayList der Klasse Freundschaftsanfrage
     * zurück.
     *
     * @param input Json-Code der Seite mit den blockierten Usern
     * @return ArrayList mit Blockierten Usern
     */
    public ArrayList<Freundschaftsanfrage> getBlockierteUser(String input) {
        ArrayList<Freundschaftsanfrage> list = new ArrayList<Freundschaftsanfrage>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    Freundschaftsanfrage.class,
                    new FreundschaftsanfrageDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Freundschaftsanfrage>>() {
            }.getType();
            list = gson.fromJson(input, collectionType);
            return list;
        } catch (Exception e) {
            return list;
        }
    }

    /**
     * Parst den übergebenen String nach den Nutzerinformationen der Suche und
     * gibt eine ArrayList mit den gewünschten Informationen zurück.
     *
     * @param input HTJSON-Code der Suche
     * @return ArrayList mit gefundenen Benutzern
     */
    public ArrayList<Benutzer> getSearchedUsers(String input) {
        ArrayList<Benutzer> result = new ArrayList<Benutzer>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Benutzer.class,
                    new SearchUserDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Benutzer>>() {
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
     * @param komplett       ArrayList für komplett gesehene Animes
     * @param amSchauen      ArrayList für aktuell aktive Animes
     * @param kurzAufgehoert ArrayList für pausierte Animes
     * @param abgebrochen    ArrayList für abgebrochene Animes
     * @param geplant        ArrayList für abgebrochene Animes
     * @param ownList        Flag ob es die eigene Liste ist (tokenId sonst nicht
     *                       vorhanden)
     */
    @SuppressWarnings("unchecked")
    public void getListAnimes(String input, ArrayList<ListAnime> komplett,
                              ArrayList<ListAnime> amSchauen,
                              ArrayList<ListAnime> kurzAufgehoert,
                              ArrayList<ListAnime> abgebrochen, ArrayList<ListAnime> geplant,
                              boolean ownList) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            JsonArray jsonKomplett, jsonAmSchauen, jsonKurzAufgehoert, jsonAbgebrochen, jsonGeplant;
            Gson gson;
            if (object.has("1")) {
                jsonAmSchauen = object.get("1").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                amSchauen.clear();
                amSchauen.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonAmSchauen, collectionType));
            }
            if (object.has("2")) {
                jsonKomplett = object.get("2").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                komplett.clear();
                komplett.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonKomplett, collectionType));
            }
            if (object.has("3")) {
                jsonKurzAufgehoert = object.get("3").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                kurzAufgehoert.clear();
                kurzAufgehoert.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonKurzAufgehoert, collectionType));
            }
            if (object.has("4")) {
                jsonAbgebrochen = object.get("4").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                abgebrochen.clear();
                abgebrochen.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonAbgebrochen, collectionType));
            }
            if (object.has("5")) {
                jsonGeplant = object.get("5").getAsJsonArray();
                gson = new GsonBuilder().registerTypeAdapter(ListAnime.class,
                        new ListAnimeDeserializer()).create();
                Type collectionType = new TypeToken<ArrayList<ListAnime>>() {
                }.getType();
                geplant.clear();
                geplant.addAll((Collection<ListAnime>) gson.fromJson(
                        jsonGeplant, collectionType));
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
    public TreeMap<Integer, ArrayList<Forum>> getForen(String input) {
        ArrayList<Forum> gesamt = new ArrayList<Forum>();
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Forum.class,
                    new ForumDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<Forum>>() {
            }.getType();
            gesamt = gson.fromJson(input, collectionType);
            TreeMap<Integer, ArrayList<Forum>> map = new TreeMap<Integer, ArrayList<Forum>>();
            map.put(1, new ArrayList<Forum>());
            map.put(2, new ArrayList<Forum>());
            map.put(3, new ArrayList<Forum>());
            map.put(4, new ArrayList<Forum>());
            map.put(5, new ArrayList<Forum>());
            for (Forum f : gesamt) {
                map.get(f.getOberforumId()).add(f);
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
     * @return Die Forenstatistik als Statistik-Objekt
     */
    public Statistik getForenStatistik(String input) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Statistik.class,
                    new StatistikDeserializer()).create();
            Type collectionType = new TypeToken<Statistik>() {
            }.getType();
            Statistik stat = gson.fromJson(input, collectionType);
            return stat;
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
    public int getForumThreadPageCount(String input) {
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
    public ArrayList<ForumThread> getForumThreads(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(input);
            JsonObject obj = el.getAsJsonObject();
            el = obj.get("threads");
            if (el == null)
                return null;
            Gson gson = new GsonBuilder().registerTypeAdapter(
                    ForumThread.class, new ForumThreadDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<ForumThread>>() {
            }.getType();
            ArrayList<ForumThread> list = gson.fromJson(el, collectionType);
            if (list == null)
                list = new ArrayList<ForumThread>();
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
    public ArrayList<ForumPost> getForumPosts(String input) {
        try {
            ArrayList<ForumPost> list = new ArrayList<ForumPost>();
            JsonParser parser = new JsonParser();
            JsonElement el = parser.parse(input);
            JsonObject obj = el.getAsJsonObject();
            ForumPost first = new ForumPost();
            if (obj.has("thread_id"))
                first.setId(0);
            if (obj.has("thread_date"))
                first.setDate(obj.get("thread_date").getAsString());
            if (obj.has("text"))
                first.setText(obj.get("text").getAsString());
            if (obj.has("edited"))
                first.setEdited(obj.get("edited").getAsInt());
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
                first.setSignatur(obj.get("signatur").getAsString());
            if (obj.has("geschlecht")) {
                String gender = obj.get("geschlecht").getAsString();
                if (gender.equals("m"))
                    first.setGeschlecht("Männlich");
                else if (gender.equals("w"))
                    first.setGeschlecht("Weiblich");
                else
                    first.setGeschlecht("Nicht angegeben");
            }
            if (obj.has("user_image"))
                first.setBild(obj.get("user_image").getAsString());
            list.add(first);
            el = obj.get("posts");
            if (el == null)
                return null;
            Gson gson = new GsonBuilder().registerTypeAdapter(ForumPost.class,
                    new ForumPostDeserializer()).create();
            Type collectionType = new TypeToken<ArrayList<ForumPost>>() {
            }.getType();
            list.addAll((Collection<ForumPost>) gson.fromJson(el,
                    collectionType));
            return list;
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
     * Kommentare) und setzt die erhaltenen Werte in der Konfiguration.
     *
     * @param input JSON der die News enthält
     */
    public void getNews(String input) {
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
            Konfiguration.setNewCommentCount(commentcount, context);
            Konfiguration.setNewMessageCount(pncount, context);
        } catch (Exception e) {

        }
    }

    /**
     * Parst den übergebenen JSON-String der API-Funktion getToken nach dem
     * Token und setzt diesen in der Konfiguration.
     *
     * @param input JSON-String der den Token enthält
     */
    public void getToken(String input) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            if (object.has("token")) {
                Konfiguration.setForumtoken(object.get("token").getAsString());
            }
        } catch (Exception e) {

        }
    }

    /**
     * Parst den übergebenen String nach der Kopfzeile und überprüft, ob der
     * Benutzer eingeloggt ist.
     *
     * @param input HTML-Code einer Seite auf EA
     * @return Wahrheitswert, ob der Benutzer eingeloggt ist
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
