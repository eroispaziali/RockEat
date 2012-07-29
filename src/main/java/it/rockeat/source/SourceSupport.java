package it.rockeat.source;

import it.rockeat.SettingsManager;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.ConnectionManager;
import it.rockeat.util.ParsingUtils;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.inject.Inject;

public abstract class SourceSupport implements MusicSource {
	
	private String url;
	private Document document;
	private File artwork;

	@Inject private SettingsManager settingsManager;
	@Inject	private ConnectionManager connectionManager;
	
	protected final Document fetchDocument() throws ConnectionException {
		try {
			HttpClient httpClient = connectionManager.createClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			InputStream pageStream = responseEntity.getContent();
			return Jsoup.parse(ParsingUtils.streamToString(pageStream));
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
	}

	public void tuneIn(String url) throws ConnectionException, MalformedURLException, ParsingException {
		if (!StringUtils.equals(url, getUrl())) {
			this.url = url;
			document = fetchDocument();
		}
	}
	
	public void release() {
		url = null;
		document = null;
		FileUtils.deleteQuietly(artwork);
	}

	public void noticeDownloadSuccess() { 
		/* do nothing */
	}

	
	public String getUrl() {
		return url;
	}
	public Document getDocument() {
		return document;
	}
	public File getArtwork() {
		return artwork;
	}
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setArtwork(File artwork) {
		this.artwork = artwork;
	}
	
	
}
