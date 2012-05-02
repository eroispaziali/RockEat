package it.rockeat.bean;

import org.apache.commons.lang3.StringUtils;

public class Track {
	
	private Integer order;
	private String id;
	private String title;
	private String url;
	private String album;
	private String author;
	
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
	public String getRemoteFilename() {
		return StringUtils.substringAfterLast(url, "/");
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
	
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Override
	public String toString() {
		String string = "\"" + title + "\" di " + author; 
		return string;
	}
	
}
