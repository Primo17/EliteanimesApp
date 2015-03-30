package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.btcdev.eliteanimesapp.data.Kommentar;

public class KommentarSerializer implements JsonSerializer<Kommentar> {
	public JsonElement serialize(Kommentar src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("cid", src.getId());
		object.addProperty("name", src.getBenutzername());
		object.addProperty("date", src.getDate());
		object.addProperty("uid", src.getUserId());
		object.addProperty("image", src.getBildUrl());
		object.addProperty("text", src.getText());
		return object;
	}
}