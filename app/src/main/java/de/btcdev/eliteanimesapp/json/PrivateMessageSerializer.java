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
		JsonObject object = new JsonObject();
		object.addProperty("id", src.getId());
		object.addProperty("subject", src.getBetreff());
		object.addProperty("date", src.getDate());
		if (src.getGelesen())
			object.addProperty("readed", 1);
		else
			object.addProperty("readed", 0);
		object.addProperty("f_uid", src.getUserid());
		object.addProperty("f_uname", src.getBenutzername());
		object.addProperty("pm", src.getText());
		return object;
	}
}
