package it.rockeat.source.bandcamp;

public class BcUrl {
	
	private Long band_id;
 	private Long album_id;
	private Long track_id;
	
	public Long getBandId() {
		return band_id;
	}
	public void setBand_id(Long band_id) {
		this.band_id = band_id;
	}
	public Long getAlbumId() {
		return album_id;
	}
	public void setAlbum_id(Long album_id) {
		this.album_id = album_id;
	}
	public Long getTrackId() {
		return track_id;
	}
	public void setTrack_id(Long track_id) {
		this.track_id = track_id;
	}
	
}