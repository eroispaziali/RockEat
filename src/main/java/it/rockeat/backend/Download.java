package it.rockeat.backend;

public class Download {

	public final static String REMOTE_CLASSNAME = "Download";

	private String title;
	private String artist;
	private String url;
	private Long tracks;
	private Long size;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getTracks() {
		return tracks;
	}

	public void setTracks(Long tracks) {
		this.tracks = tracks;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

}
