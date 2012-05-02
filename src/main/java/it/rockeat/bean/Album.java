package it.rockeat.bean;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class Album {
	
	private String title;
	private String artist;
	private String label;
	private String year;
	private String genre;
	private List<Track> tracks;
	private String url;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public List<Track> getTracks() {
		return tracks;
	}
	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	@Override
	public String toString() {
		String st = "";
		if (StringUtils.isNotBlank(title)) {
			st += "\"" + title + "\"";
		} else {
			st += "qualche canzone";
		}
		st += " di " + artist + " ("+ CollectionUtils.size(tracks) + " canzoni)";
		return st;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getTracksCount() {
		return CollectionUtils.size(tracks);
	}
}
