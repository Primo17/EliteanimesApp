package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Freund;

public class FreundDeserializer implements JsonDeserializer<Freund> {

	@Override
	public Freund deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("name")) {
			Freund f = new Freund(object.get("name").getAsString());
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
					f.setGeschlecht("Männlich");
				if (gender.equals("w"))
					f.setGeschlecht("Weiblich");
				else
					f.setGeschlecht("Nicht angegeben");
			}
			if (object.has("age"))
				f.setAlter(object.get("age").getAsString());
			if (object.has("date"))
				f.setDatum(object.get("date").getAsString());
			return f;
		}
		return null;
	}

}
