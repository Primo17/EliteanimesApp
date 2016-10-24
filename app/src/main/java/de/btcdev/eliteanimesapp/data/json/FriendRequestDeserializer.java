package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.FriendRequest;

public class FriendRequestDeserializer implements
		JsonDeserializer<FriendRequest> {

	@Override
	public FriendRequest deserialize(JsonElement json, Type typeOfT,
									 JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("id") && jsonObject.has("name") && jsonObject.has("status")
				&& jsonObject.has("gender") && jsonObject.has("age")) {
			FriendRequest friendRequest = new FriendRequest(jsonObject.get(
					"name").getAsString());
			friendRequest.setId(jsonObject.get("id").getAsInt());
			friendRequest.setAge(jsonObject.get("age").getAsString());
			friendRequest.setSex(jsonObject.get("gender").getAsString());
			int status = jsonObject.get("status").getAsInt();
			if (status == 1)
				friendRequest.setStatus(true);
			else
				friendRequest.setStatus(false);
			return friendRequest;
		}
		return null;
	}

}
