package de.btcdev.eliteanimesapp.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.btcdev.eliteanimesapp.data.ForumThread;

public class ForumThreadDeserializer implements JsonDeserializer<ForumThread> {

	@Override
	public ForumThread deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		try {
			JsonObject obj = json.getAsJsonObject();
			if (obj.has("thread_id")) {
				ForumThread ft = new ForumThread();
				ft.setId(obj.get("thread_id").getAsInt());
				if (obj.has("thread_name"))
					ft.setName(obj.get("thread_name").getAsString());
				if (obj.has("c_uid"))
					ft.setCreateId(obj.get("c_uid").getAsInt());
				if (obj.has("c_uname"))
					ft.setCreateName(obj.get("c_uname").getAsString());
				if (obj.has("c_date"))
					ft.setCreateDate(obj.get("c_date").getAsString());
				if (obj.has("lp_uid"))
					ft.setLastPostId(obj.get("lp_uid").getAsInt());
				if (obj.has("lp_uname"))
					ft.setLastPostName(obj.get("lp_uname").getAsString());
				if (obj.has("lp_date"))
					ft.setLastPostDate(obj.get("lp_date").getAsString());
				if (obj.has("hits"))
					ft.setHits(obj.get("hits").getAsInt());
				if (obj.has("posts"))
					ft.setPosts(obj.get("posts").getAsInt());
				if (obj.has("pages"))
					ft.setPages(obj.get("pages").getAsInt());
				if (obj.has("closed"))
					ft.setClosed(obj.get("closed").getAsInt() == 1);
				if (obj.has("sticky"))
					ft.setSticky(obj.get("sticky").getAsInt() == 1);
				if (obj.has("new"))
					ft.setUnread(obj.get("new").getAsInt() == 1);
				return ft;
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
