package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.eater.Eater;
import it.rockeat.eater.RockitEater;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpUtils;
import it.rockeat.util.FileManagementUtils;
import it.rockeat.util.Id3TaggingUtils;
import it.rockeat.util.ParsingUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.client.HttpClient;

public class Controller {
	
	private Boolean id3TaggingEnabled = Boolean.TRUE;
	private Long downloadedTracks = 0L;
	private Long bytesDownloaded = 0L;
	private Eater eater = new RockitEater();

	private Eater findEater(String url) {
		return eater;
	}
	
	@SuppressWarnings("unused")
	public Album parse(String url) throws MalformedURLException, ConnectionException, ParsingException {
		url = ParsingUtils.addProtocolPrefixIfMissing(url);
		URL parsedUrl = new URL(url);
		Eater eater = findEater(url);
		InputStream pageStream = HttpUtils.httpGet(url);
		String htmlCode = ParsingUtils.streamToString(pageStream);
		HttpClient httpClient = HttpUtils.createClient();
		Album album = eater.parse(httpClient, htmlCode);
		album.setUrl(url);
		return album;
	}
	
	public void download(Album album, Track track) throws ConnectionException, DownloadException, FileSaveException {
		Eater eater = findEater(album.getUrl());
		String folderName = FileManagementUtils.createFolder(album);
		String filePath = folderName + FileManagementUtils.createFilename(album, track); 
		try {
			HttpClient httpClient = HttpUtils.createClient();
			OutputStream outputStream = new FileOutputStream(filePath);
			eater.download(httpClient,track,outputStream);
			outputStream.close();
			File fileOnDisk = new File(filePath);
			if (FileUtils.sizeOf(fileOnDisk) == 0) {
				FileUtils.deleteQuietly(fileOnDisk);
				throw new DownloadException();
			} else {
				bytesDownloaded += FileUtils.sizeOf(fileOnDisk);
				downloadedTracks++;
			}
			if (BooleanUtils.isTrue(id3TaggingEnabled)) {
				try {
					Id3TaggingUtils.id3Tag(album, track, fileOnDisk);
				} catch (Id3TaggingException e) {
					/* tagging exception, silently ignored */
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

}
