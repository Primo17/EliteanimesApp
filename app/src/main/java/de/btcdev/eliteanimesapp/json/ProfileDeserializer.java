package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Profile;
import de.btcdev.eliteanimesapp.data.ProfileCache;

public class ProfileDeserializer implements JsonDeserializer<Profile> {

	@Override
	public Profile deserialize(JsonElement json, Type typeOfT,
							   JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("error") || jsonObject.has("Error")) {
			throw new JsonParseException("JsonError");
		}
		if (jsonObject.has("name")) {
			String userName = jsonObject.get("name").getAsString();
			Profile profile = ProfileCache.instance().contains(userName);
			if (jsonObject.has("id"))
				profile.setUserId(jsonObject.get("id").getAsInt());
			if (jsonObject.has("location"))
				profile.setResidence(jsonObject.get("location").getAsString());
			if (jsonObject.has("group")) {
				int group = jsonObject.get("group").getAsInt();
				switch (group) {
				case 1:
					profile.setGroup("User");
					break;
				case 2:
					profile.setGroup("Moderator");
					break;
				case 3:
					profile.setGroup("Admin");
					break;
				}
			}
			if (jsonObject.has("status")) {
				int status = jsonObject.get("status").getAsInt();
				if (status == 0)
					profile.setOnline(false);
				else
					profile.setOnline(true);
			}
			if (jsonObject.has("gender")) {
				String gender = jsonObject.get("gender").getAsString();
				if (gender.equals("m"))
					profile.setSex("Männlich");
				else if (gender.equals("w"))
					profile.setSex("Weiblich");
				else
					profile.setSex("Nicht angegeben");
			}
			if (jsonObject.has("age"))
				profile.setAge(jsonObject.get("age").getAsString());
			if (jsonObject.has("single")) {
				String single = jsonObject.get("single").getAsString();
				if (single.equals(""))
					profile.setSingle("k.A.");
				else
					profile.setSingle(single);
			}
			if (jsonObject.has("since"))
				profile.setRegisteredSince(jsonObject.get("since").getAsString());
			if (jsonObject.has("friend"))
				profile.setFriend(jsonObject.get("friend").getAsInt());
			if (jsonObject.has("image")) {
				String image = jsonObject.get("image").getAsString();
				if (image.equals("")) {
					profile.setAvatarURL("noava.png");
				} else
					profile.setAvatarURL(image);
			}
			return profile;
		}
		return null;
	}

}
