package it.rockeat.source.bandcamp;

import java.util.List;

public class BcAlbum {
	private long album_id;
	private long band_id;
	private String artist;
	private String title;
	private String url;
	private String about;
	private String credits;
	private String small_art_url;
	private String large_art_url;
	private long release_date;
	private int downloadable;
	private List<BcTrack> tracks;
	public long getAlbumId() {
		return album_id;
	}
	public void setAlbumId(long albumId) {
		this.album_id = albumId;
	}
	public long getBandId() {
		return band_id;
	}
	public void setBandId(long bandId) {
		this.band_id = bandId;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public String getCredits() {
		return credits;
	}
	public void setCredits(String credits) {
		this.credits = credits;
	}
	public String getSmallArtUrl() {
		return small_art_url;
	}
	public void setSmallArtUrl(String smallArtUrl) {
		this.small_art_url = smallArtUrl;
	}
	public String getLargeArtUrl() {
		return large_art_url;
	}
	public void setLargeArtUrl(String largeArtUrl) {
		this.large_art_url = largeArtUrl;
	}
	public long getReleaseDate() {
		return release_date;
	}
	public void setReleaseDate(long releaseDate) {
		this.release_date = releaseDate;
	}
	public int getDownloadable() {
		return downloadable;
	}
	public void setDownloadable(int downloadable) {
		this.downloadable = downloadable;
	}
	public List<BcTrack> getTracks() {
		return tracks;
	}
	public void setTracks(List<BcTrack> tracks) {
		this.tracks = tracks;
	}
	
}
