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
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("error") || object.has("Error")) {
			throw new JsonParseException("JsonError");
		}
		if (object.has("name")) {
			String benutzername = object.get("name").getAsString();
			Profile p = ProfileCache.instance().contains(benutzername);
			if (object.has("id"))
				p.setUserID(object.get("id").getAsInt());
			if (object.has("location"))
				p.setWohnort(object.get("location").getAsString());
			if (object.has("group")) {
				int group = object.get("group").getAsInt();
				switch (group) {
				case 1:
					p.setGruppe("User");
					break;
				case 2:
					p.setGruppe("Moderator");
					break;
				case 3:
					p.setGruppe("Admin");
					break;
				}
			}
			if (object.has("status")) {
				int status = object.get("status").getAsInt();
				if (status == 0)
					p.setOnline(false);
				else
					p.setOnline(true);
			}
			if (object.has("gender")) {
				String gender = object.get("gender").getAsString();
				if (gender.equals("m"))
					p.setGeschlecht("Männlich");
				else if (gender.equals("w"))
					p.setGeschlecht("Weiblich");
				else
					p.setGeschlecht("Nicht angegeben");
			}
			if (object.has("age"))
				p.setAlter(object.get("age").getAsString());
			if (object.has("single")) {
				String single = object.get("single").getAsString();
				if (single.equals(""))
					p.setSingle("k.A.");
				else
					p.setSingle(single);
			}
			if (object.has("since"))
				p.setDabei(object.get("since").getAsString());
			if (object.has("friend"))
				p.setFriend(object.get("friend").getAsInt());
			if (object.has("image")) {
				String image = object.get("image").getAsString();
				if (image.equals("")) {
					p.setProfilbild("noava.png");
				} else
					p.setProfilbild(image);
			}
			return p;
		}
		return null;
	}

}
