package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.btcdev.eliteanimesapp.data.ListAnime;

public class ListAnimeSerializer implements JsonSerializer<ListAnime> {

	@Override
	public JsonElement serialize(ListAnime src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("mid", src.getTokenId());
		jsonObject.addProperty("aid", src.getId());
		jsonObject.addProperty("aname", src.getTitle());
		jsonObject.addProperty("epi", src.getEpisodeCount());
		jsonObject.addProperty("seen", src.getProgress());
		jsonObject.addProperty("score", src.getRating());
		return jsonObject;
	}

}
