package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Statistics;

public class StatisticsDeserializer implements JsonDeserializer<Statistics> {

	@Override
	public Statistics deserialize(JsonElement json, Type typeOfT,
								  JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
			Statistics statistics = new Statistics();
			if (jsonObject.has("count")) {
				JsonObject count = jsonObject.get("count").getAsJsonObject();
				if (count.has("users"))
					statistics.setUserCount(count.get("users").getAsInt());
				if (count.has("threads"))
					statistics.setThreadCount(count.get("threads").getAsInt());
				if (count.has("posts"))
					statistics.setPostCount(count.get("posts").getAsInt());
				if (count.has("online"))
					statistics.setOnlineCount(count.get("online").getAsInt());
				if (jsonObject.has("lastuser_id"))
					statistics.setLastUserId(jsonObject.get("lastuser_id").getAsInt());
				if (jsonObject.has("lastuser_name"))
					statistics.setLastUserName(jsonObject.get("lastuser_name").getAsString());
				ArrayList<Statistics.StatisticsUser> statisticsUsers = new ArrayList<>();
				if (jsonObject.has("users_online")) {
					JsonArray ar = jsonObject.get("users_online").getAsJsonArray();
					Statistics.StatisticsUser user;
					for (JsonElement el : ar) {
						user = new Statistics.StatisticsUser();
						count = el.getAsJsonObject();
						if (count.has("id"))
							user.id = count.get("id").getAsInt();
						if (count.has("user_name"))
							user.name = count.get("user_name").getAsString();
						if (count.has("user_level"))
							user.level = count.get("user_level").getAsInt();
						if (count.has("donator"))
							user.setDonator(count.get("donator").getAsInt());
						statisticsUsers.add(user);
					}
					statistics.setUsersOnline(statisticsUsers);
				}
				return statistics;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

}
