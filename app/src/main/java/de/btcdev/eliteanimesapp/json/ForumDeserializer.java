package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Forum;
import de.btcdev.eliteanimesapp.data.Subforum;

public class ForumDeserializer implements JsonDeserializer<Forum> {

	@Override
	public Forum deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (obj.has("f_id")) {
			Forum f = new Forum();
			f.setId(obj.get("f_id").getAsInt());
			if (obj.has("of_id"))
				f.setOberforumId(obj.get("of_id").getAsInt());
			if (obj.has("of_name"))
				f.setOberforumName(obj.get("of_name").getAsString());
			if (obj.has("f_name"))
				f.setName(obj.get("f_name").getAsString());
			if (obj.has("f_text"))
				f.setBeschreibung(obj.get("f_text").getAsString());
			if (obj.has("count_t"))
				f.setAnzahlThreads(obj.get("count_t").getAsInt());
			if (obj.has("count_b"))
				f.setAnzahlPosts(obj.get("count_b").getAsInt());
			if (obj.has("unread_t"))
				f.setAnzahlUnread(obj.get("unread_t").getAsInt());
			if (obj.has("uf")) {
				JsonArray array = obj.get("uf").getAsJsonArray();
				ArrayList<Subforum> uf = new ArrayList<Subforum>();
				Subforum s;
				for (JsonElement el : array) {
					if(el.isJsonObject()){
						JsonObject object = el.getAsJsonObject();
						if(object.has("id") && object.has("name")){
							s = new Subforum();
							s.setId(object.get("id").getAsInt());
							s.setName(object.get("name").getAsString());
							uf.add(s);
						}
					}
				}
				if(!uf.isEmpty())
					f.setSubforen(uf);
			}
			return f;
		}
		return null;
	}

}
