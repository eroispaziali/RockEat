package it.rockeat;

import it.rockeat.backend.Backend;
import it.rockeat.backend.Activity;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpUtils;
import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;
import it.rockeat.source.MusicSource;
import it.rockeat.source.rockit.RockitSource;
import it.rockeat.util.FileManagementUtils;
import it.rockeat.util.HashUtils;
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

public class SourceManager {

    private Boolean id3TaggingEnabled = Boolean.TRUE;
    private Long downloadedTracks = 0L;
    private Long bytesDownloaded = 0L;
    private MusicSource musicSource;
    private HttpClient httpClient;
    private SettingsManager settingsManager;
    private Backend backend;

    public SourceManager() {
        httpClient = HttpUtils.createClient();
        settingsManager = new SettingsManager(httpClient);
        backend = new Backend(httpClient);
    }

    public MusicSource findSource(String url) throws BackendException, ConnectionException, ParsingException, MalformedURLException {
    	musicSource = new RockitSource(url, httpClient, settingsManager);        
        return musicSource;
    }
    
    private void trackActivity(RockitAlbum album) {
    	Activity activity = new Activity();
    	activity.setAlbum(album.getTitle());
    	activity.setArtist(album.getArtist());
    	activity.setTracksDownloaded(0L);
    	activity.setBytesDownloaded(0L);
    	activity.setUrl(album.getUrl());  
    	activity.setOrigin(HashUtils.getMacAddress());
    	try {
			backend.trackActivity(activity);
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @SuppressWarnings("unused")
    public RockitAlbum parse(String url) throws BackendException, MalformedURLException, ConnectionException, ParsingException {
        url = ParsingUtils.addProtocolPrefixIfMissing(url);
        URL parsedUrl = new URL(url);
        MusicSource musicSource = findSource(url);
        InputStream pageStream = HttpUtils.httpGet(url);
        String htmlCode = ParsingUtils.streamToString(pageStream);
        HttpClient httpClient = HttpUtils.createClient();
        RockitAlbum album = musicSource.parse(htmlCode);
        album.setUrl(url);
        trackActivity(album);
        return album;
    }

    public void download(RockitAlbum album, RockitTrack track) throws BackendException, ConnectionException, DownloadException, FileSaveException, MalformedURLException, ParsingException {
        MusicSource musicSource = findSource(album.getUrl());
        String folderName = FileManagementUtils.createFolder(album);
        String filePath = folderName + FileManagementUtils.createFilename(album, track);
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

	public MusicSource getMusicSource() {
		return musicSource;
	}

	public void setMusicSource(MusicSource musicSource) {
		this.musicSource = musicSource;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}

	public Backend getBackend() {
		return backend;
	}

	public void setBackend(Backend backend) {
		this.backend = backend;
	}

	public void setDownloadedTracks(Long downloadedTracks) {
		this.downloadedTracks = downloadedTracks;
	}

	public void setBytesDownloaded(Long bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}
}
