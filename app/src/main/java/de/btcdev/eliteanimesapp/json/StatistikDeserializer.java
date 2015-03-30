package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Statistik;

public class StatistikDeserializer implements JsonDeserializer<Statistik> {

	@Override
	public Statistik deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
			Statistik stat = new Statistik();
			if (obj.has("count")) {
				JsonObject count = obj.get("count").getAsJsonObject();
				if (count.has("users"))
					stat.setAnzahlUser(count.get("users").getAsInt());
				if (count.has("threads"))
					stat.setAnzahlThreads(count.get("threads").getAsInt());
				if (count.has("posts"))
					stat.setAnzahlPosts(count.get("posts").getAsInt());
				if (count.has("online"))
					stat.setAnzahlOnline(count.get("online").getAsInt());
				if (obj.has("lastuser_id"))
					stat.setLastUserId(obj.get("lastuser_id").getAsInt());
				if (obj.has("lastuser_name"))
					stat.setLastUserName(obj.get("lastuser_name").getAsString());
				ArrayList<Statistik.StatistikUser> list = new ArrayList<Statistik.StatistikUser>();
				if (obj.has("users_online")) {
					JsonArray ar = obj.get("users_online").getAsJsonArray();
					Statistik.StatistikUser user;
					for (JsonElement el : ar) {
						user = new Statistik.StatistikUser();
						count = el.getAsJsonObject();
						if (count.has("id"))
							user.id = count.get("id").getAsInt();
						if (count.has("user_name"))
							user.name = count.get("user_name").getAsString();
						if (count.has("user_level"))
							user.level = count.get("user_level").getAsInt();
						if (count.has("donator"))
							user.setDonator(count.get("donator").getAsInt());
						list.add(user);
					}
					stat.setUsersOnline(list);
				}
				return stat;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

}
