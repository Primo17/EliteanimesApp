package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.btcdev.eliteanimesapp.data.PrivateMessage;

public class PrivateMessageSerializer implements JsonSerializer<PrivateMessage> {
	public JsonElement serialize(PrivateMessage src, Type typeOfSrc,
								 JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", src.getId());
		jsonObject.addProperty("subject", src.getSubject());
		jsonObject.addProperty("date", src.getDate());
		if (src.isRead())
			jsonObject.addProperty("readed", 1);
		else
			jsonObject.addProperty("readed", 0);
		jsonObject.addProperty("f_uid", src.getUserId());
		jsonObject.addProperty("f_uname", src.getUserName());
		jsonObject.addProperty("pm", src.getMessage());
		return jsonObject;
	}
}
