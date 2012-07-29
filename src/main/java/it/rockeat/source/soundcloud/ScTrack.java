package it.rockeat.source.soundcloud;

import it.rockeat.model.Track;

public class ScTrack {

	private String id;
	private String kind;
	private String user_id;
	private ScUser user;
	private String title;
	private String stream_url;
	private String permalink;
	private String permalink_url;
	private String uri;
	private String sharing;
	private String embeddable_by;
	private String purchase_url;
	private String artwork_url;
	private String description;
	private String label;
	private Long duration;
	private String genre;
	private Integer shared_to_count;
	private String tag_list;
	private String label_id;
	private String label_name;
	private String license;
	private String release;
	private Integer release_day;
	private Integer release_month;
	private Integer release_year;
	private Boolean streamable;
	private Boolean downloadable;
	private String state;
	private String track_type;
	private String waveform_url;
	private String download_url;
	private String video_url;
	private String bpm;
	private Boolean commentable;
	private String isrc;
	private String key_signature;
	private String comment_count;
	private String download_count;
	private String playback_count;
	private String favoritings_count;
	private String original_format;
	private String original_content_size;
	private String created_with;
	private String asset_data;
	private String artwork_data;
	private String user_favorite;

	public Track toTrack() {
		Track track = new Track();
		track.setTitle(title);
		track.setAuthor(user.getUsername());
		track.setId(id);
		track.setUrl(stream_url);
		return track;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public ScUser getUser() {
		return user;
	}

	public void setUser(ScUser user) {
		this.user = user;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStream_url() {
		return stream_url;
	}

	public void setStream_url(String stream_url) {
		this.stream_url = stream_url;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public String getPermalink_url() {
		return permalink_url;
	}

	public void setPermalink_url(String permalink_url) {
		this.permalink_url = permalink_url;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSharing() {
		return sharing;
	}

	public void setSharing(String sharing) {
		this.sharing = sharing;
	}

	public String getEmbeddable_by() {
		return embeddable_by;
	}

	public void setEmbeddable_by(String embeddable_by) {
		this.embeddable_by = embeddable_by;
	}

	public String getPurchase_url() {
		return purchase_url;
	}

	public void setPurchase_url(String purchase_url) {
		this.purchase_url = purchase_url;
	}

	public String getArtwork_url() {
		return artwork_url;
	}

	public void setArtwork_url(String artwork_url) {
		this.artwork_url = artwork_url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Integer getShared_to_count() {
		return shared_to_count;
	}

	public void setShared_to_count(Integer shared_to_count) {
		this.shared_to_count = shared_to_count;
	}

	public String getTag_list() {
		return tag_list;
	}

	public void setTag_list(String tag_list) {
		this.tag_list = tag_list;
	}

	public String getLabel_id() {
		return label_id;
	}

	public void setLabel_id(String label_id) {
		this.label_id = label_id;
	}

	public String getLabel_name() {
		return label_name;
	}

	public void setLabel_name(String label_name) {
		this.label_name = label_name;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public Integer getRelease_day() {
		return release_day;
	}

	public void setRelease_day(Integer release_day) {
		this.release_day = release_day;
	}

	public Integer getRelease_month() {
		return release_month;
	}

	public void setRelease_month(Integer release_month) {
		this.release_month = release_month;
	}

	public Integer getRelease_year() {
		return release_year;
	}

	public void setRelease_year(Integer release_year) {
		this.release_year = release_year;
	}

	public Boolean getStreamable() {
		return streamable;
	}

	public void setStreamable(Boolean streamable) {
		this.streamable = streamable;
	}

	public Boolean getDownloadable() {
		return downloadable;
	}

	public void setDownloadable(Boolean downloadable) {
		this.downloadable = downloadable;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTrack_type() {
		return track_type;
	}

	public void setTrack_type(String track_type) {
		this.track_type = track_type;
	}

	public String getWaveform_url() {
		return waveform_url;
	}

	public void setWaveform_url(String waveform_url) {
		this.waveform_url = waveform_url;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public String getVideo_url() {
		return video_url;
	}

	public void setVideo_url(String video_url) {
		this.video_url = video_url;
	}

	public String getBpm() {
		return bpm;
	}

	public void setBpm(String bpm) {
		this.bpm = bpm;
	}

	public Boolean getCommentable() {
		return commentable;
	}

	public void setCommentable(Boolean commentable) {
		this.commentable = commentable;
	}

	public String getIsrc() {
		return isrc;
	}

	public void setIsrc(String isrc) {
		this.isrc = isrc;
	}

	public String getKey_signature() {
		return key_signature;
	}

	public void setKey_signature(String key_signature) {
		this.key_signature = key_signature;
	}

	public String getComment_count() {
		return comment_count;
	}

	public void setComment_count(String comment_count) {
		this.comment_count = comment_count;
	}

	public String getDownload_count() {
		return download_count;
	}

	public void setDownload_count(String download_count) {
		this.download_count = download_count;
	}

	public String getPlayback_count() {
		return playback_count;
	}

	public void setPlayback_count(String playback_count) {
		this.playback_count = playback_count;
	}

	public String getFavoritings_count() {
		return favoritings_count;
	}

	public void setFavoritings_count(String favoritings_count) {
		this.favoritings_count = favoritings_count;
	}

	public String getOriginal_format() {
		return original_format;
	}

	public void setOriginal_format(String original_format) {
		this.original_format = original_format;
	}

	public String getOriginal_content_size() {
		return original_content_size;
	}

	public void setOriginal_content_size(String original_content_size) {
		this.original_content_size = original_content_size;
	}

	public String getCreated_with() {
		return created_with;
	}

	public void setCreated_with(String created_with) {
		this.created_with = created_with;
	}

	public String getAsset_data() {
		return asset_data;
	}

	public void setAsset_data(String asset_data) {
		this.asset_data = asset_data;
	}

	public String getArtwork_data() {
		return artwork_data;
	}

	public void setArtwork_data(String artwork_data) {
		this.artwork_data = artwork_data;
	}

	public String getUser_favorite() {
		return user_favorite;
	}

	public void setUser_favorite(String user_favorite) {
		this.user_favorite = user_favorite;
	}

}
