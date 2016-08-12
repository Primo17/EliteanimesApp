package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class BoardThread implements Parcelable {
	private int id;
	private String name;
	private int createId;
	private String createName;
	private String createDate;
	private int lastPostId;
	private String lastPostName;
	private String lastPostDate;
	private int hits;
	private int posts;
	private int pages;
	private boolean closed;
	private boolean sticky;
	private boolean unread;

	public BoardThread() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCreateId() {
		return createId;
	}

	public void setCreateId(int createId) {
		this.createId = createId;
	}

	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public int getLastPostId() {
		return lastPostId;
	}

	public void setLastPostId(int lastPostId) {
		this.lastPostId = lastPostId;
	}

	public String getLastPostName() {
		return lastPostName;
	}

	public void setLastPostName(String lastPostName) {
		this.lastPostName = lastPostName;
	}

	public String getLastPostDate() {
		return lastPostDate;
	}

	public void setLastPostDate(String lastPostDate) {
		this.lastPostDate = lastPostDate;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public boolean isUnread() {
		return unread;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}

	public boolean equals(Object o) {
		if (o instanceof BoardThread) {
			BoardThread f = (BoardThread) o;
			return this.getId() == f.getId();
		} else
			return false;
	}

	public String toString() {
		return name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putString("name", name);
		bundle.putInt("createId", createId);
		bundle.putString("createName", createName);
		bundle.putString("createDate", createDate);
		bundle.putInt("lastPostId", lastPostId);
		bundle.putString("lastPostName", lastPostName);
		bundle.putString("lastPostDate", lastPostDate);
		bundle.putInt("hits", hits);
		bundle.putInt("posts", posts);
		bundle.putInt("pages", pages);
		bundle.putBoolean("closed", closed);
		bundle.putBoolean("sticky", sticky);
		bundle.putBoolean("unread", unread);
		dest.writeBundle(bundle);
	}

	public static final Parcelable.Creator<BoardThread> CREATOR = new Parcelable.Creator<BoardThread>() {
		public BoardThread createFromParcel(Parcel in) {
			return new BoardThread(in);
		}

		public BoardThread[] newArray(int size) {
			return new BoardThread[size];
		}
	};

	private BoardThread(Parcel in) {
		Bundle bundle = in.readBundle();
		id = bundle.getInt("id");
		name = bundle.getString("name");
		createId = bundle.getInt("createId");
		createName = bundle.getString("createName");
		createDate = bundle.getString("createDate");
		lastPostId = bundle.getInt("lastPostId");
		lastPostName = bundle.getString("lastPostName");
		lastPostDate = bundle.getString("lastPostDate");
		hits = bundle.getInt("hits");
		posts = bundle.getInt("posts");
		pages = bundle.getInt("pages");
		closed = bundle.getBoolean("closed");
		sticky = bundle.getBoolean("sticky");
		unread = bundle.getBoolean("unread");
	}
}
