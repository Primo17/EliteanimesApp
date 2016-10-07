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
		JsonObject jsonObject;
		try {
			jsonObject = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("invalid");
		}
		if (jsonObject.has("uid") && jsonObject.has("cid") && jsonObject.has("name")
				&& jsonObject.has("date") && jsonObject.has("text")
				&& jsonObject.has("image")) {
			Comment comment = new Comment(jsonObject.get("cid").getAsInt());
			comment.setUserName(jsonObject.get("name").getAsString());
			comment.setDate(jsonObject.get("date").getAsString());
			comment.setUserId(jsonObject.get("uid").getAsInt());
			String image = jsonObject.get("image").getAsString();
			if (image.equals(""))
				comment.setAvatarURL("noava.png");
			else
				comment.setAvatarURL(image);
			comment.setText(jsonObject.get("text").getAsString());
			return comment;
		} else {
			throw new JsonParseException("invaid");
		}
	}
}