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
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("mid") && jsonObject.has("aid") && jsonObject.has("aname")
				&& jsonObject.has("epi") && jsonObject.has("seen")
				&& jsonObject.has("score")) {
			ListAnime anime = new ListAnime();
			anime.setTokenId(jsonObject.get("mid").getAsString());
			anime.setId(jsonObject.get("aid").getAsInt());
			anime.setTitle(jsonObject.get("aname").getAsString());
			anime.setEpisodeCount(jsonObject.get("epi").getAsInt());
			anime.setProgress(jsonObject.get("seen").getAsInt());
			anime.setRating(jsonObject.get("score").getAsDouble());
			return anime;
		} else
			throw new JsonParseException("invalid");
	}

}
