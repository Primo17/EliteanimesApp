package de.btcdev.eliteanimesapp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.ApiPath;
import de.btcdev.eliteanimesapp.data.Comment;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.json.CommentDeserializer;

public class CommentService {

    private NetworkService networkService;
    private ImageService imageService;
    private ConfigurationService configurationService;

    @Inject
    public CommentService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService) {
        this.networkService = networkService;
        this.imageService = imageService;
        this.configurationService = configurationService;
    }

    /**
     * Lädt die gewünschte Kommentarseite und gibt die JSON-Antwort der API
     * zurück.
     *
     * @param page   die Seitenzahl der gewünschten Kommentarseite
     * @param userId       die UserID des Profils, zu dem die Kommentarseite gehört
     * @return JSON-Antwort der API
     * @throws EAException bei Verbindungs- und Streamfehlern jeglicher Art
     */
    public String getCommentPage(int page, int userId)
            throws EAException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("id", Integer.toString(userId)));
        nvps.add(new BasicNameValuePair("page", Integer
                .toString(page)));
        return networkService.doPOST(ApiPath.COMMENTS, nvps);
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
            for (Comment comment: comments) {
                comment.setAvatar(imageService.getBitmapFromUrl(comment.getAvatarURL(), ImageService.commentSize));
            }
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
            Collection<Comment> newComments = gson.fromJson(input, collectionType);
            for (Comment comment: newComments) {
                comment.setAvatar(imageService.getBitmapFromUrl(comment.getAvatarURL(), ImageService.commentSize));
            }
            comments.addAll(newComments);
        } catch (Exception e) {
            return comments;
        }
        return comments;
    }

    /**
     * Schickt den übergebenen Comment per POST an den übergebenen User
     * und überprüft, ob der Comment erfolgreich übermittelt wurde.
     *
     * @param comment Kommentartext
     * @param userName    User, der den Comment erhalten soll
     * @param userId  UserID des Users, der den Comment erhalten soll
     * @return Wahrheitswert ob Senden erfolgreich
     */
    public boolean postComment(String comment, String userName, int userId)
            throws EAException {
        if (configurationService.getBoardToken() != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("comment", comment));
            nvps.add(new BasicNameValuePair("forumtoken", configurationService
                    .getBoardToken()));
            nvps.add(new BasicNameValuePair("id", "" + userId));
            nvps.add(new BasicNameValuePair("name", userName));
            nvps.add(new BasicNameValuePair("submit", "Eintragen"));
            //TODO: check if sending the apikey breaks this
            String input = networkService.doPOST(ApiPath.COMMENT_ADD + userId
                    + "/" + userName, nvps);
            return checkComment(input);
        }
        return false;
    }

    /**
     * Schickt den editierten Comment per POST an den übergebenen User.
     *
     * @param comment Kommentartext
     * @param userName    User, der den Comment erhalten soll
     * @param userId  UserID des Users, der den Comment erhalten soll
     */
    public void editComment(Comment comment, String userName, int userId)
            throws EAException {
        if (configurationService.getBoardToken() != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("commentedit", comment
                    .getText()));
            nvps.add(new BasicNameValuePair("commentid", Integer
                    .toString(comment.getId())));
            nvps.add(new BasicNameValuePair("editcomment", "Editieren"));
            nvps.add(new BasicNameValuePair("forumtoken", configurationService
                    .getBoardToken()));
            nvps.add(new BasicNameValuePair("fromuser", ""
                    + configurationService.getUserID(null)));
            nvps.add(new BasicNameValuePair("touser", "" + userId));
            //TODO: check if sending the apikey breaks this
            networkService.doPOST(ApiPath.COMMENT_EDIT + userId
                    + "/" + userName, nvps);
        }
    }

    /**
     * Löscht den Comment mit der übergebenen Comment-ID, falls die
     * Berechtigung dazu vorhanden ist
     *
     * @param commentId Comment-ID des zu löschenden Kommentars
     */
    public void deleteComment(String commentId) throws EAException {
        if (configurationService.getBoardToken() != null) {
            networkService.doGET(ApiPath.COMMENT_DELETE + "?id="
                    + commentId + "&ft=" + configurationService.getBoardToken());
        }
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
}
