package it.rockeat.bean;

import org.apache.commons.lang3.StringUtils;

public class Track {
	
	private Integer order;
	private String id;
	private String title;
	private String url;
	private String album;
	private String artist;
	
	public Track() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	
	public String toString() {
		String st = StringUtils.EMPTY;
		st += "id: " + id + "\n";
		st += "titolo: " + title + "\n";
		st += "url: " + url;
		return st;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
}
