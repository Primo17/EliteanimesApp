package de.btcdev.eliteanimesapp.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import de.btcdev.eliteanimesapp.data.models.BoardThread;

public class BoardThreadDeserializer implements JsonDeserializer<BoardThread> {

	@Override
	public BoardThread deserialize(JsonElement json, Type typeOfT,
								   JsonDeserializationContext context) throws JsonParseException {
		try {
			JsonObject jsonObject = json.getAsJsonObject();
			if (jsonObject.has("thread_id")) {
				BoardThread boardThread = new BoardThread();
				boardThread.setId(jsonObject.get("thread_id").getAsInt());
				if (jsonObject.has("thread_name"))
					boardThread.setName(jsonObject.get("thread_name").getAsString());
				if (jsonObject.has("c_uid"))
					boardThread.setCreateId(jsonObject.get("c_uid").getAsInt());
				if (jsonObject.has("c_uname"))
					boardThread.setCreateName(jsonObject.get("c_uname").getAsString());
				if (jsonObject.has("c_date"))
					boardThread.setCreateDate(jsonObject.get("c_date").getAsString());
				if (jsonObject.has("lp_uid"))
					boardThread.setLastPostId(jsonObject.get("lp_uid").getAsInt());
				if (jsonObject.has("lp_uname"))
					boardThread.setLastPostName(jsonObject.get("lp_uname").getAsString());
				if (jsonObject.has("lp_date"))
					boardThread.setLastPostDate(jsonObject.get("lp_date").getAsString());
				if (jsonObject.has("hits"))
					boardThread.setHits(jsonObject.get("hits").getAsInt());
				if (jsonObject.has("posts"))
					boardThread.setPosts(jsonObject.get("posts").getAsInt());
				if (jsonObject.has("pages"))
					boardThread.setPages(jsonObject.get("pages").getAsInt());
				if (jsonObject.has("closed"))
					boardThread.setClosed(jsonObject.get("closed").getAsInt() == 1);
				if (jsonObject.has("sticky"))
					boardThread.setSticky(jsonObject.get("sticky").getAsInt() == 1);
				if (jsonObject.has("new"))
					boardThread.setUnread(jsonObject.get("new").getAsInt() == 1);
				return boardThread;
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
