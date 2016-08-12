package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.User;

public class SearchUserDeserializer implements JsonDeserializer<User> {

	@Override
	public User deserialize(JsonElement json, Type typeOfT,
							JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (obj.has("name") && obj.has("id")) {
			User user = new User(obj.get("name").getAsString());
			user.setId(obj.get("id").getAsString());
			return user;
		} else {
			throw new JsonParseException("invalid");
		}
	}

}
