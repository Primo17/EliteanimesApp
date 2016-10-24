package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.PrivateMessage;

public class PrivateMessageDeserializer implements JsonDeserializer<PrivateMessage> {
	public PrivateMessage deserialize(JsonElement json, Type typeOfT,
									  JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		// ist der String ein gültiger JSON-String?
		if (jsonObject.has("id") && jsonObject.has("subject") && jsonObject.has("date")
				&& jsonObject.has("readed") && jsonObject.has("f_uid")
				&& jsonObject.has("f_uname") && jsonObject.has("pm")) {
			PrivateMessage privateMessage = new PrivateMessage(jsonObject.get("id").getAsInt());
			privateMessage.setUserName(jsonObject.get("f_uname").getAsString());
			privateMessage.setSubject(jsonObject.get("subject").getAsString());
			privateMessage.setDate(jsonObject.get("date").getAsString());
			int read = jsonObject.get("readed").getAsInt();
			if (read == 1)
				privateMessage.setRead(true);
			else
				privateMessage.setRead(false);
			privateMessage.setMessage(jsonObject.get("pm").getAsString());
			privateMessage.setUserId(jsonObject.get("f_uid").getAsInt());
			return privateMessage;
		} else {
			throw new JsonParseException("invalid");
		}
	}
}
