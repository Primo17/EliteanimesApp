package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.User;

public class SearchUserDeserializer implements JsonDeserializer<User> {

	@Override
	public User deserialize(JsonElement json, Type typeOfT,
							JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("name") && jsonObject.has("id")) {
			User user = new User(jsonObject.get("name").getAsString());
			user.setId(jsonObject.get("id").getAsString());
			return user;
		} else {
			throw new JsonParseException("invalid");
		}
	}

}
