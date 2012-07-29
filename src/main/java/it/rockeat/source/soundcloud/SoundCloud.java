package it.rockeat.source.soundcloud;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Album;
import it.rockeat.model.Track;
import it.rockeat.source.SourceSupport;
import it.rockeat.util.ParsingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;


public class SoundCloud extends SourceSupport {
	
	@SuppressWarnings("unused")
	private static final String CLIENT_SECRET="ce3f6bdd4e2e3347615fd806bb4ab115";
	private static final String CLIENT_ID="6612a64d278d53f70eed78644d6d90f9";
	private static final String PARSING_TRACK_SELECTION_EXPRESSION = "div[data-sc-track]";
	
	private static Track getTrackFromJson(InputStream jsonStream) {
		String json = ParsingUtils.streamToString(jsonStream);
		Gson gson = new Gson();
		ScTrack soundCloudTrack = gson.fromJson(json, ScTrack.class);
		return soundCloudTrack.toTrack();
	}
	
	public Track lookup(String trackId) throws ClientProtocolException, IOException  {
		HttpClient httpClient = getConnectionManager().createClient();
		HttpGet request = new HttpGet("http://api.soundcloud.com/tracks/"+ trackId +".json?client_id="+CLIENT_ID);
		request.setHeader("Content-Type", "application/json");
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		InputStream trackInformation = responseEntity.getContent();
		try {
			Track track = getTrackFromJson(trackInformation);
			return track;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public Album findAlbum() throws ParsingException {
		Album album = new Album();
		String albumTitle = "Qualche traccia";
		String albumArtist = "Artisti vari";
		List<Track> tracks = new ArrayList<Track>();
		Elements playlistEl = getDocument()
				.select(PARSING_TRACK_SELECTION_EXPRESSION);
		
		if (CollectionUtils.isNotEmpty(playlistEl)) {
			Integer trackNumber = 0;
			for (Element trackEl : playlistEl) {
				String trackId = trackEl.attr("data-sc-track");
				if (StringUtils.isNotBlank(trackId)) {
					try {
						trackNumber++;
						Track track = lookup(trackId);					
						track.setId(trackId);
						track.setOrder(trackNumber);
						tracks.add(track);
					} catch (IOException e) {
						throw new ParsingException(e);
					}
				}
			}
		}
		
		/* TODO: download artwork */
		album.setTitle(albumTitle);
		album.setArtist(albumArtist);
		album.setTracks(tracks);
		return album;
	}

	@Override
	public void download(Track track, OutputStream outputStream)
			throws ConnectionException {
		try {
			HttpClient httpClient = getConnectionManager().createClient();
			HttpGet request = new HttpGet(track.getUrl() +"?client_id="+CLIENT_ID);
			request.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			responseEntity.writeTo(outputStream);
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public boolean runTest() {
		return true;
	}

}
