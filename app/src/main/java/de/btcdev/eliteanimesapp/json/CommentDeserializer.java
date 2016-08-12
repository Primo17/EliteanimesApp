package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.Comment;

public class CommentDeserializer implements JsonDeserializer<Comment> {
	public Comment deserialize(JsonElement json, Type typeOfT,
							   JsonDeserializationContext context) throws JsonParseException {
		JsonObject object;
		try {
			object = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (object.has("uid") && object.has("cid") && object.has("name")
				&& object.has("date") && object.has("text")
				&& object.has("image")) {
			Comment k = new Comment(object.get("cid").getAsInt());
			k.setUserName(object.get("name").getAsString());
			k.setDate(object.get("date").getAsString());
			k.setUserId(object.get("uid").getAsInt());
			String image = object.get("image").getAsString();
			if (image.equals(""))
				k.setAvatar("noava.png");
			else
				k.setAvatar(image);
			k.setText(object.get("text").getAsString());
			return k;
		} else {
			throw new JsonParseException("invaid");
		}
	}
}