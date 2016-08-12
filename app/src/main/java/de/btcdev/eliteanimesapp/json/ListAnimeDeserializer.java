package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.ListAnime;

public class ListAnimeDeserializer implements JsonDeserializer<ListAnime> {

	@Override
	public ListAnime deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("mid") && object.has("aid") && object.has("aname")
				&& object.has("epi") && object.has("seen")
				&& object.has("score")) {
			ListAnime anime = new ListAnime();
			anime.setTokenId(object.get("mid").getAsString());
			anime.setId(object.get("aid").getAsInt());
			anime.setTitle(object.get("aname").getAsString());
			anime.setEpisodeCount(object.get("epi").getAsInt());
			anime.setProgress(object.get("seen").getAsInt());
			anime.setRating(object.get("score").getAsDouble());
			return anime;
		} else
			throw new JsonParseException("invalid");
	}

}
