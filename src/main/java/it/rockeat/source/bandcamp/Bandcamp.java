package it.rockeat.source.bandcamp;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Playlist;
import it.rockeat.model.Track;
import it.rockeat.source.SourceSupport;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class Bandcamp extends SourceSupport {
	
	private static final String DEVELOPER_KEY = "snaefellsjokull";
	private static final String BAND_SEARCH_URL_PATTERN = "http://api.bandcamp.com/api/band/3/search?key={0}&name={1}";
    private static final String BAND_DISCOGRAPHY_URL_PATTERN = "http://api.bandcamp.com/api/band/3/discography?key={0}&band_id={1}";
    private static final String BAND_INFO_URL_PATTERN = "http://api.bandcamp.com/api/band/3/info?key={0}&band_id={1}";
    private static final String ALBUM_INFO_URL_PATTERN = "http://api.bandcamp.com/api/album/2/info?key={0}&album_id={1}";
    private static final String TRACK_INFO_URL_PATTERN = "http://api.bandcamp.com/api/track/1/info?key={0}&track_id={1}";
    private static final String URL_INFO_URL_PATTERN = "http://api.bandcamp.com/api/url/1/info?key={0}&url={1}";
	
	@Override
	public Playlist findTracks() throws ParsingException {
		throw new ParsingException("Non ancora pronto");
	}

	@Override
	public void download(Track track, OutputStream outputStream)
			throws ConnectionException {
		throw new ConnectionException("Non ancora pronto");
	}

	@Override
	public boolean runTest() {
		return false;
	}
	
	public static String buildUrl(String baseUrl, String apiKey, Object[] arguments) {
        String nameParams = toUrlEncodedCommaSeparatedList(arguments);
        return MessageFormat.format(baseUrl, apiKey, nameParams);
    }
	
	 public static String toUrlEncodedCommaSeparatedList(Object[] objects) {
	        StringBuilder params = new StringBuilder();
	        for (Object object : objects) {
	            try {
	                params.append(URLEncoder.encode(object.toString(), "UTF-8"));
	                params.append(',');
	            } catch (UnsupportedEncodingException e) {
	                // this shouldn't happen
	            }
	        }
	        return (params.length() > 0 ? params.substring(0, params.length() - 1) : params.toString());
	    }

}
