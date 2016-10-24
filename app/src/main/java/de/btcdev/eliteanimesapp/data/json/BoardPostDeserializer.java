package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.BoardPost;

public class BoardPostDeserializer implements JsonDeserializer<BoardPost> {

	@Override
	public BoardPost deserialize(JsonElement json, Type typeOfT,
								 JsonDeserializationContext context) throws JsonParseException {
		try {
			JsonObject jsonObject = json.getAsJsonObject();
			BoardPost boardPost = new BoardPost();
			if (jsonObject.has("post_id")) {
				boardPost.setId(jsonObject.get("post_id").getAsInt());
				if (jsonObject.has("post_date"))
					boardPost.setDate(jsonObject.get("post_date").getAsString());
				if (jsonObject.has("text"))
					boardPost.setText(jsonObject.get("text").getAsString());
				if (jsonObject.has("edited"))
					boardPost.setEditedCount(jsonObject.get("edited").getAsInt());
				if (jsonObject.has("edited_time"))
					boardPost.setEditedTime(jsonObject.get("edited_time").getAsString());
				if (jsonObject.has("uname"))
					boardPost.setUserName(jsonObject.get("uname").getAsString());
				if (jsonObject.has("uid"))
					boardPost.setUserId(jsonObject.get("uid").getAsInt());
				if (jsonObject.has("user_level"))
					boardPost.setUserLevel(jsonObject.get("user_level").getAsInt());
				if (jsonObject.has("regdate"))
					boardPost.setUserDate(jsonObject.get("regdate").getAsString());
				if (jsonObject.has("online"))
					boardPost.setOnline(jsonObject.get("online").getAsInt() == 1);
				if (jsonObject.has("signatur"))
					boardPost.setSignature(jsonObject.get("signatur").getAsString());
				if (jsonObject.has("geschlecht")) {
					String gender = jsonObject.get("geschlecht").getAsString();
					if (gender.equals("m"))
						boardPost.setSex("Männlich");
					else if (gender.equals("w"))
						boardPost.setSex("Weiblich");
					else
						boardPost.setSex("Nicht angegeben");
				}
				if (jsonObject.has("user_image"))
					boardPost.setAvatarURL(jsonObject.get("user_image").getAsString());
				return boardPost;
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
