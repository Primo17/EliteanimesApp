package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.BoardPost;

public class BoardPostDeserializer implements JsonDeserializer<BoardPost> {

	@Override
	public BoardPost deserialize(JsonElement json, Type typeOfT,
								 JsonDeserializationContext context) throws JsonParseException {
		try {
			JsonObject obj = json.getAsJsonObject();
			BoardPost fp = new BoardPost();
			if (obj.has("post_id")) {
				fp.setId(obj.get("post_id").getAsInt());
				if (obj.has("post_date"))
					fp.setDate(obj.get("post_date").getAsString());
				if (obj.has("text"))
					fp.setText(obj.get("text").getAsString());
				if (obj.has("edited"))
					fp.setEditedCount(obj.get("edited").getAsInt());
				if (obj.has("edited_time"))
					fp.setEditedTime(obj.get("edited_time").getAsString());
				if (obj.has("uname"))
					fp.setUserName(obj.get("uname").getAsString());
				if (obj.has("uid"))
					fp.setUserId(obj.get("uid").getAsInt());
				if (obj.has("user_level"))
					fp.setUserLevel(obj.get("user_level").getAsInt());
				if (obj.has("regdate"))
					fp.setUserDate(obj.get("regdate").getAsString());
				if (obj.has("online"))
					fp.setOnline(obj.get("online").getAsInt() == 1);
				if (obj.has("signatur"))
					fp.setSignature(obj.get("signatur").getAsString());
				if (obj.has("geschlecht")) {
					String gender = obj.get("geschlecht").getAsString();
					if (gender.equals("m"))
						fp.setSex("Männlich");
					else if (gender.equals("w"))
						fp.setSex("Weiblich");
					else
						fp.setSex("Nicht angegeben");
				}
				if (obj.has("user_image"))
					fp.setAvatar(obj.get("user_image").getAsString());
				return fp;
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
