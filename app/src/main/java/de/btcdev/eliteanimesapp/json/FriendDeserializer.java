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
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("name")) {
			Friend f = new Friend(object.get("name").getAsString());
			if (object.has("id"))
				f.setId(object.get("id").getAsInt());
			if (object.has("status")) {
				int status = object.get("status").getAsInt();
				if (status == 0)
					f.setStatus(false);
				else
					f.setStatus(true);
			}
			if (object.has("gender")) {
				String gender = object.get("gender").getAsString();
				if (gender.equals("m"))
					f.setSex("Männlich");
				if (gender.equals("w"))
					f.setSex("Weiblich");
				else
					f.setSex("Nicht angegeben");
			}
			if (object.has("age"))
				f.setAge(object.get("age").getAsString());
			if (object.has("date"))
				f.setDate(object.get("date").getAsString());
			return f;
		}
		return null;
	}

}
