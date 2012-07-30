package it.rockeat;

import it.rockeat.backend.Backend;
import it.rockeat.backend.DownloadActivity;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.exception.ParsingException;
import it.rockeat.exception.UnknownSourceException;
import it.rockeat.model.Playlist;
import it.rockeat.model.Track;
import it.rockeat.source.MusicSource;
import it.rockeat.source.bandcamp.Bandcamp;
import it.rockeat.source.rockit.Rockit;
import it.rockeat.source.soundcloud.SoundCloud;
import it.rockeat.util.FileManagementUtils;
import it.rockeat.util.Id3TaggingUtils;
import it.rockeat.util.ParsingUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Controller {

	private Boolean id3TaggingEnabled = Boolean.TRUE;
	private Long downloadedTracks = 0L;
	private Long bytesDownloaded = 0L;
	
	private Map<String,Class<? extends MusicSource>> sources = new HashMap<String,Class<? extends MusicSource>>();

	private MusicSource musicSource;
	@Inject	private SettingsManager settingsManager;
	@Inject	private Backend backend;
	
	
	public Controller() {
		sources.put("soundcloud.com", SoundCloud.class);
		sources.put("rockit.it", Rockit.class);
		sources.put("makemine.bandcamp.com", Bandcamp.class);
	}

	public MusicSource tuneInSource(String url) throws BackendException,
			ConnectionException, ParsingException, MalformedURLException, UnknownSourceException {
		
		Injector injector = Guice.createInjector();
		String domain = ParsingUtils.getDomainName(url);
		if (sources.containsKey(domain)) {
			Class<? extends MusicSource> sourceManager = sources.get(domain);
			musicSource = injector.getInstance(sourceManager);
			musicSource.tuneIn(ParsingUtils.addProtocolPrefixIfMissing(url));
			return musicSource;
		} else {
			throw new UnknownSourceException("Nessuna sorgente registrata per questo dominio");
		}
	}

	public void clean() {
		musicSource.release();
	}

	public void downloadFinished(Playlist album) {
		clean();
		DownloadActivity downloadActivity = new DownloadActivity();
		downloadActivity.setTitle(album.getTitle());
		downloadActivity.setArtist(album.getArtist());
		downloadActivity.setTracks(downloadedTracks);
		downloadActivity.setBytes(bytesDownloaded);
		downloadActivity.setUrl(album.getUrl());
		downloadActivity.setUid(settingsManager.getSettings().getUid());
		try {
			backend.log(downloadActivity);
		} catch (BackendException e) {
			/* silently ignore */
		}
	}

	@SuppressWarnings("unused")
	public Playlist findTracks(String url) throws BackendException,
			MalformedURLException, ConnectionException, ParsingException, UnknownSourceException {
		/* reset counters */
		downloadedTracks = 0L;
		bytesDownloaded = 0L;

		/* parse */
		url = ParsingUtils.addProtocolPrefixIfMissing(url);
		URL parsedUrl = new URL(url);
		MusicSource musicSource = tuneInSource(url);
		Playlist playlist = musicSource.findTracks();
		playlist.setUrl(url);
		return playlist;
	}

	public void download(Track track)
			throws BackendException, ConnectionException, DownloadException,
			FileSaveException, MalformedURLException, ParsingException, UnknownSourceException {
		Playlist playlist = track.getPlaylist();
		MusicSource musicSource = tuneInSource(playlist.getUrl());
		String folderName = FileManagementUtils.createFolder(playlist);
		String filePath = folderName
				+ FileManagementUtils.createFilename(track);
		try {
			OutputStream outputStream = new FileOutputStream(filePath);
			musicSource.download(track, outputStream);
			outputStream.close();
			File fileOnDisk = new File(filePath);
			if (FileUtils.sizeOf(fileOnDisk) == 0) {
				FileUtils.deleteQuietly(fileOnDisk);
				throw new DownloadException();
			} else {
				/* il download sta funzionando */
				bytesDownloaded += FileUtils.sizeOf(fileOnDisk);
				downloadedTracks++;
				musicSource.noticeDownloadSuccess();
				// backend.trackDownload(track, bytesDownloaded);
			}
			if (BooleanUtils.isTrue(id3TaggingEnabled)) {
				try {
					Id3TaggingUtils.id3Tag(playlist, track, fileOnDisk);
				} catch (Id3TaggingException e) {
					/*
					 * tagging exception, silently ignored
					 */
				}
			}
		} catch (IOException e) {
			throw new FileSaveException(e);
		}
	}

	public Boolean getId3TaggingEnabled() {
		return id3TaggingEnabled;
	}

	public void setId3TaggingEnabled(Boolean id3TaggingEnabled) {
		this.id3TaggingEnabled = id3TaggingEnabled;
	}

	public Long getDownloadedTracks() {
		return downloadedTracks;
	}

	public Long getBytesDownloaded() {
		return bytesDownloaded;
	}

	public void setDownloadedTracks(Long downloadedTracks) {
		this.downloadedTracks = downloadedTracks;
	}

	public void setBytesDownloaded(Long bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}
}
