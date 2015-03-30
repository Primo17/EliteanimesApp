package de.btcdev.eliteanimesapp.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ListAnime implements Parcelable {

	private String titel;
	private int folgenAnzahl;
	private int fortschritt;
	private double bewertung;
	private String link;
	private int id;
	private String tokenId;

	public ListAnime() {

	}

	public ListAnime(String titel) {
		setTitel(titel);
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public int getFolgenAnzahl() {
		return folgenAnzahl;
	}

	public void setFolgenAnzahl(int folgenAnzahl) {
		this.folgenAnzahl = folgenAnzahl;
	}

	public int getFortschritt() {
		return fortschritt;
	}

	public void setFortschritt(int fortschritt) {
		this.fortschritt = fortschritt;
	}

	public double getBewertung() {
		return bewertung;
	}

	public void setBewertung(double bewertung) {
		this.bewertung = bewertung;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
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
		return titel + ", " + id + ", " + fortschritt + "/" + folgenAnzahl
				+ ", " + bewertung;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		Bundle bundle = new Bundle();
		bundle.putString("Titel", titel);
		bundle.putInt("Folgenanzahl", folgenAnzahl);
		bundle.putInt("Fortschritt", fortschritt);
		bundle.putDouble("Bewertung", bewertung);
		bundle.putString("Link", link);
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
		titel = bundle.getString("Titel");
		folgenAnzahl = bundle.getInt("Folgenanzahl");
		fortschritt = bundle.getInt("Fortschritt");
		bewertung = bundle.getDouble("Bewertung");
		link = bundle.getString("Link");
		id = bundle.getInt("Id");
		tokenId = bundle.getString("TokenId");
	}
}
