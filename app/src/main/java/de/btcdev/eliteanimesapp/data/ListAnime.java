package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ListAnime implements Parcelable {

	private String title;
	private int episodeCount;
	private int progress;
	private double rating;
	private String url;
	private int id;
	private String tokenId;

	public ListAnime() {

	}

	public ListAnime(String title) {
		setTitle(title);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getEpisodeCount() {
		return episodeCount;
	}

	public void setEpisodeCount(int episodeCount) {
		this.episodeCount = episodeCount;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public boolean equals(Object o) {
		if (o instanceof ListAnime) {
			ListAnime anime = (ListAnime) o;
			return id == anime.getId();
		} else
			return false;
	}

	public String toString() {
		return title + ", " + id + ", " + progress + "/" + episodeCount
				+ ", " + rating;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putString("Titel", title);
		bundle.putInt("Folgenanzahl", episodeCount);
		bundle.putInt("Fortschritt", progress);
		bundle.putDouble("Bewertung", rating);
		bundle.putString("Link", url);
		bundle.putInt("Id", id);
		bundle.putString("TokenId", tokenId);
		arg0.writeBundle(bundle);
	}

	public static final Parcelable.Creator<ListAnime> CREATOR = new Parcelable.Creator<ListAnime>() {
		public ListAnime createFromParcel(Parcel in) {
			return new ListAnime(in);
		}

		public ListAnime[] newArray(int size) {
			return new ListAnime[size];
		}
	};

	private ListAnime(Parcel in) {
		Bundle bundle = in.readBundle();
		title = bundle.getString("Titel");
		episodeCount = bundle.getInt("Folgenanzahl");
		progress = bundle.getInt("Fortschritt");
		rating = bundle.getDouble("Bewertung");
		url = bundle.getString("Link");
		id = bundle.getInt("Id");
		tokenId = bundle.getString("TokenId");
	}
}
