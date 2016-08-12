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
		JsonObject object = new JsonObject();
		object.addProperty("mid", src.getTokenId());
		object.addProperty("aid", src.getId());
		object.addProperty("aname", src.getTitle());
		object.addProperty("epi", src.getEpisodeCount());
		object.addProperty("seen", src.getProgress());
		object.addProperty("score", src.getRating());
		return object;
	}

}
