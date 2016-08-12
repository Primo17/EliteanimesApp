package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.FriendRequest;

public class FriendRequestDeserializer implements
		JsonDeserializer<FriendRequest> {

	@Override
	public FriendRequest deserialize(JsonElement json, Type typeOfT,
									 JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (obj.has("id") && obj.has("name") && obj.has("status")
				&& obj.has("gender") && obj.has("age")) {
			FriendRequest anfrage = new FriendRequest(obj.get(
					"name").getAsString());
			anfrage.setId(obj.get("id").getAsInt());
			anfrage.setAge(obj.get("age").getAsString());
			anfrage.setSex(obj.get("gender").getAsString());
			int status = obj.get("status").getAsInt();
			if (status == 1)
				anfrage.setStatus(true);
			else
				anfrage.setStatus(false);
			return anfrage;
		}
		return null;
	}

}
