package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Board;
import de.btcdev.eliteanimesapp.data.Subboard;

public class BoardDeserializer implements JsonDeserializer<Board> {

	@Override
	public Board deserialize(JsonElement json, Type typeOfT,
							 JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj;
		try {
			obj = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (obj.has("f_id")) {
			Board f = new Board();
			f.setId(obj.get("f_id").getAsInt());
			if (obj.has("of_id"))
				f.setBoardCategoryId(obj.get("of_id").getAsInt());
			if (obj.has("of_name"))
				f.setBoardCategoryName(obj.get("of_name").getAsString());
			if (obj.has("f_name"))
				f.setName(obj.get("f_name").getAsString());
			if (obj.has("f_text"))
				f.setDescription(obj.get("f_text").getAsString());
			if (obj.has("count_t"))
				f.setThreadCount(obj.get("count_t").getAsInt());
			if (obj.has("count_b"))
				f.setPostCount(obj.get("count_b").getAsInt());
			if (obj.has("unread_t"))
				f.setUnreadCount(obj.get("unread_t").getAsInt());
			if (obj.has("uf")) {
				JsonArray array = obj.get("uf").getAsJsonArray();
				ArrayList<Subboard> uf = new ArrayList<Subboard>();
				Subboard s;
				for (JsonElement el : array) {
					if(el.isJsonObject()){
						JsonObject object = el.getAsJsonObject();
						if(object.has("id") && object.has("name")){
							s = new Subboard();
							s.setId(object.get("id").getAsInt());
							s.setName(object.get("name").getAsString());
							uf.add(s);
						}
					}
				}
				if(!uf.isEmpty())
					f.setSubboards(uf);
			}
			return f;
		}
		return null;
	}

}
