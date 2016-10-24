package de.btcdev.eliteanimesapp.data.services;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
