package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Kommentar;

public class KommentarDeserializer implements JsonDeserializer<Kommentar> {
	public Kommentar deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("uid") && object.has("cid") && object.has("name")
				&& object.has("date") && object.has("text")
				&& object.has("image")) {
			Kommentar k = new Kommentar(object.get("cid").getAsInt());
			k.setBenutzername(object.get("name").getAsString());
			k.setDate(object.get("date").getAsString());
			k.setUserId(object.get("uid").getAsInt());
			String image = object.get("image").getAsString();
			if (image.equals(""))
				k.setBild("noava.png");
			else
				k.setBild(image);
			k.setText(object.get("text").getAsString());
			return k;
		} else {
			throw new JsonParseException("invaid");
		}
	}
}