package it.rockeat.source.bandcamp;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Playlist;
import it.rockeat.model.Track;
import it.rockeat.source.SourceSupport;
import it.rockeat.util.ParsingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class Bandcamp extends SourceSupport {
	
	private static final String API_KEY = "bandcamp";
	private static final String BAND_SEARCH_URL_PATTERN = "http://api.bandcamp.com/api/band/3/search?key={0}&name={1}";
    private static final String BAND_DISCOGRAPHY_URL_PATTERN = "http://api.bandcamp.com/api/band/3/discography?key={0}&band_id={1}";
    private static final String BAND_INFO_URL_PATTERN = "http://api.bandcamp.com/api/band/3/info?key={0}&band_id={1}";
    private static final String ALBUM_INFO_URL_PATTERN = "http://api.bandcamp.com/api/album/2/info?key={0}&album_id={1}";
    private static final String TRACK_INFO_URL_PATTERN = "http://api.bandcamp.com/api/track/1/info?key={0}&track_id={1}";
    private static final String URL_INFO_URL_PATTERN = "http://api.bandcamp.com/api/url/1/info?key={0}&url={1}";
    
    @Inject private Gson gson;
    
	@Override
	public Playlist findTracks() throws ParsingException {
		try {
	        String finalUrl = buildUrl(URL_INFO_URL_PATTERN, API_KEY, new Object[]{getUrl()});
			HttpClient httpClient = getConnectionManager().createClient();
			HttpGet request = new HttpGet(finalUrl);
			request.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			InputStream jsonStream = responseEntity.getContent();
			String json = ParsingUtils.streamToString(jsonStream);
			BcUrl urlInfo = gson.fromJson(json, BcUrl.class);

			if (urlInfo.getAlbumId()!=null) {
				/* TODO: estrarre tracce album */
				return null;
			} 
			
			if (urlInfo.getTrackId()!=null) {
				/* TODO: estrarre info traccia */
				return null;
			} 
			throw new ParsingException("Non ho trovato nulla");	
			
		} catch (Exception e) {
			throw new ParsingException(e);
		}
		        
	}

	@Override
	public void download(Track track, OutputStream outputStream)
			throws ConnectionException {
		try {
			HttpClient httpClient = getConnectionManager().createClient();
			HttpGet request = new HttpGet(track.getUrl() +"?key="+API_KEY);
			request.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			if (response.getStatusLine().getStatusCode()!=200) {
				throw new ConnectionException("Errore durante il download");
			} else {
				responseEntity.writeTo(outputStream);	
			}
			
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public boolean runTest() {
		return true;
	}
	
	private static String buildUrl(String baseUrl, String apiKey, Object[] arguments) {
        String nameParams = ParsingUtils.toUrlEncodedCommaSeparatedList(arguments);
        return MessageFormat.format(baseUrl, apiKey, nameParams);
    }
	

}
