package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.btcdev.eliteanimesapp.data.Comment;

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