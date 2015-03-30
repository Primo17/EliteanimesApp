package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.PN;

public class PNDeserializer implements JsonDeserializer<PN> {
	public PN deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		// ist der String ein gültiger JSON-String?
		if (object.has("id") && object.has("subject") && object.has("date")
				&& object.has("readed") && object.has("f_uid")
				&& object.has("f_uname") && object.has("pm")) {
			PN n = new PN(object.get("id").getAsInt());
			n.setBenutzername(object.get("f_uname").getAsString());
			n.setBetreff(object.get("subject").getAsString());
			n.setDate(object.get("date").getAsString());
			int gelesen = object.get("readed").getAsInt();
			if (gelesen == 1)
				n.setGelesen(true);
			else
				n.setGelesen(false);
			n.setText(object.get("pm").getAsString());
			n.setUserid(object.get("f_uid").getAsInt());
			return n;
		} else {
			throw new JsonParseException("invalid");
		}
	}
}
