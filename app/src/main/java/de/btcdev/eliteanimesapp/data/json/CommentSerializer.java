package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.Comment;

public class CommentSerializer implements JsonSerializer<Comment> {
	public JsonElement serialize(Comment src, Type typeOfSrc,
								 JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("cid", src.getId());
		jsonObject.addProperty("name", src.getUserName());
		jsonObject.addProperty("date", src.getDate());
		jsonObject.addProperty("uid", src.getUserId());
		jsonObject.addProperty("image", src.getAvatarURL());
		jsonObject.addProperty("text", src.getText());
		return jsonObject;
	}
}