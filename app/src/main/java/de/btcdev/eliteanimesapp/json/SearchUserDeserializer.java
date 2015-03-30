package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Benutzer;

public class SearchUserDeserializer implements JsonDeserializer<Benutzer> {

	@Override
	public Benutzer deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (obj.has("name") && obj.has("id")) {
			Benutzer benutzer = new Benutzer(obj.get("name").getAsString());
			benutzer.setId(obj.get("id").getAsString());
			return benutzer;
		} else {
			throw new JsonParseException("invalid");
		}
	}

}
