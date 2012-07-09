package it.rockeat.backend;

public class Activity {
	public final static String REMOTE_CLASSNAME = "Activity";
	
	private String objectId;
	private String user;
	private String url;
	private String type;
	private String album;
	private String artist;
	private String origin;
	private Long tracksDownloaded;
	private Long bytesDownloaded;
	private String createdAt;
	private String updatedAt;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public Long getTracksDownloaded() {
		return tracksDownloaded;
	}

	public void setTracksDownloaded(Long tracksDownloaded) {
		this.tracksDownloaded = tracksDownloaded;
	}

	public Long getBytesDownloaded() {
		return bytesDownloaded;
	}

	public void setBytesDownloaded(Long bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
