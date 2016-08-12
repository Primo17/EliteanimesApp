package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Friend;

public class FriendDeserializer implements JsonDeserializer<Friend> {

	@Override
	public Friend deserialize(JsonElement json, Type typeOfT,
							  JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("name")) {
			Friend friend = new Friend(jsonObject.get("name").getAsString());
			if (jsonObject.has("id"))
				friend.setId(jsonObject.get("id").getAsInt());
			if (jsonObject.has("status")) {
				int status = jsonObject.get("status").getAsInt();
				if (status == 0)
					friend.setStatus(false);
				else
					friend.setStatus(true);
			}
			if (jsonObject.has("gender")) {
				String gender = jsonObject.get("gender").getAsString();
				if (gender.equals("m"))
					friend.setSex("Männlich");
				if (gender.equals("w"))
					friend.setSex("Weiblich");
				else
					friend.setSex("Nicht angegeben");
			}
			if (jsonObject.has("age"))
				friend.setAge(jsonObject.get("age").getAsString());
			if (jsonObject.has("date"))
				friend.setDate(jsonObject.get("date").getAsString());
			return friend;
		}
		return null;
	}

}
