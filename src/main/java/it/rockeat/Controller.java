package it.rockeat;

import it.rockeat.backend.Backend;
import it.rockeat.backend.DownloadActivity;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Album;
import it.rockeat.model.Track;
import it.rockeat.source.MusicSource;
import it.rockeat.util.FileManagementUtils;
import it.rockeat.util.Id3TaggingUtils;
import it.rockeat.util.ParsingUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.google.inject.Inject;

public class Controller {

	private Boolean id3TaggingEnabled = Boolean.TRUE;
	private Long downloadedTracks = 0L;
	private Long bytesDownloaded = 0L;

	@Inject private MusicSource musicSource;
	@Inject	private SettingsManager settingsManager;
	@Inject	private Backend backend;

	public MusicSource tuneInSource(String url) throws BackendException,
			ConnectionException, ParsingException, MalformedURLException {
		musicSource.tuneIn(ParsingUtils.addProtocolPrefixIfMissing(url));
		return musicSource;
	}

	public void clean() {
		musicSource.release();
	}

	public void downloadFinished(Album album) {
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
	public Album findAlbum(String url) throws BackendException,
			MalformedURLException, ConnectionException, ParsingException {
		/* reset counters */
		downloadedTracks = 0L;
		bytesDownloaded = 0L;

		/* parse */
		url = ParsingUtils.addProtocolPrefixIfMissing(url);
		URL parsedUrl = new URL(url);
		MusicSource musicSource = tuneInSource(url);
		Album album = musicSource.findAlbum();
		album.setUrl(url);
		return album;
	}

	public void download(Album album, Track track)
			throws BackendException, ConnectionException, DownloadException,
			FileSaveException, MalformedURLException, ParsingException {
		MusicSource musicSource = tuneInSource(album.getUrl());
		String folderName = FileManagementUtils.createFolder(album);
		String filePath = folderName
				+ FileManagementUtils.createFilename(album, track);
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
					Id3TaggingUtils.id3Tag(album, track, fileOnDisk);
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
