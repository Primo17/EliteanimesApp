package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.btcdev.eliteanimesapp.data.models.Board;
import de.btcdev.eliteanimesapp.data.models.Subboard;

public class BoardDeserializer implements JsonDeserializer<Board> {

	@Override
	public Board deserialize(JsonElement json, Type typeOfT,
							 JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("f_id")) {
			Board board = new Board();
			board.setId(jsonObject.get("f_id").getAsInt());
			if (jsonObject.has("of_id"))
				board.setBoardCategoryId(jsonObject.get("of_id").getAsInt());
			if (jsonObject.has("of_name"))
				board.setBoardCategoryName(jsonObject.get("of_name").getAsString());
			if (jsonObject.has("f_name"))
				board.setName(jsonObject.get("f_name").getAsString());
			if (jsonObject.has("f_text"))
				board.setDescription(jsonObject.get("f_text").getAsString());
			if (jsonObject.has("count_t"))
				board.setThreadCount(jsonObject.get("count_t").getAsInt());
			if (jsonObject.has("count_b"))
				board.setPostCount(jsonObject.get("count_b").getAsInt());
			if (jsonObject.has("unread_t"))
				board.setUnreadCount(jsonObject.get("unread_t").getAsInt());
			if (jsonObject.has("uf")) {
				JsonArray array = jsonObject.get("uf").getAsJsonArray();
				ArrayList<Subboard> uf = new ArrayList<Subboard>();
				Subboard subboard;
				for (JsonElement jsonElement : array) {
					if(jsonElement.isJsonObject()){
						JsonObject object = jsonElement.getAsJsonObject();
						if(object.has("id") && object.has("name")){
							subboard = new Subboard();
							subboard.setId(object.get("id").getAsInt());
							subboard.setName(object.get("name").getAsString());
							uf.add(subboard);
						}
					}
				}
				if(!uf.isEmpty())
					board.setSubboards(uf);
			}
			return board;
		}
		return null;
	}

}
