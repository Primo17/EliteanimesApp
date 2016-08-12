package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Board implements Parcelable, Comparable<Board> {

	private int boardCategoryId;
	private String boardCategoryName;
	private int id;
	private String name;
	private String description;
	private int threadCount;
	private int postCount;
	private int unreadCount;
	private ArrayList<Subboard> subboards;

	public Board() {

	}

	public int getBoardCategoryId() {
		return boardCategoryId;
	}

	public void setBoardCategoryId(int boardCategoryId) {
		this.boardCategoryId = boardCategoryId;
	}

	public String getBoardCategoryName() {
		return boardCategoryName;
	}

	public void setBoardCategoryName(String boardCategoryName) {
		this.boardCategoryName = boardCategoryName;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public ArrayList<Subboard> getSubboards() {
		return subboards;
	}

	public void setSubboards(ArrayList<Subboard> subboards) {
		this.subboards = subboards;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (subboards == null)
			subboards = new ArrayList<Subboard>();
		dest.writeTypedList(subboards);
		Bundle bundle = new Bundle();
		bundle.putInt("boardCategoryId", boardCategoryId);
		bundle.putString("boardCategoryName", boardCategoryName);
		bundle.putInt("id", id);
		bundle.putString("name", name);
		bundle.putString("description", description);
		bundle.putInt("threadCount", threadCount);
		bundle.putInt("postCount", postCount);
		bundle.putInt("unreadCount", unreadCount);
		dest.writeBundle(bundle);
	}

	public static final Parcelable.Creator<Board> CREATOR = new Parcelable.Creator<Board>() {
		public Board createFromParcel(Parcel in) {
			return new Board(in);
		}

		public Board[] newArray(int size) {
			return new Board[size];
		}
	};

	private Board(Parcel in) {
		subboards = new ArrayList<Subboard>();
		in.readTypedList(subboards, Subboard.CREATOR);
		Bundle bundle = in.readBundle();
		boardCategoryId = bundle.getInt("boardCategoryId");
		boardCategoryName = bundle.getString("boardCategoryName");
		id = bundle.getInt("id");
		name = bundle.getString("name");
		description = bundle.getString("description");
		threadCount = bundle.getInt("threadCount");
		postCount = bundle.getInt("postCount");
		unreadCount = bundle.getInt("unreadCount");
	}

	@Override
	public int compareTo(Board another) {
		return Double.compare(id, another.getId());
	}

	public String toString() {
		return id + name;
	}

	public boolean equals(Object o) {
		if (o instanceof Board) {
			Board f = (Board) o;
			return id == f.getId();
		} else
			return false;
	}
}
