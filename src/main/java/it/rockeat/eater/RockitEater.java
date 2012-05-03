package it.rockeat.eater;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.LookupException;
import it.rockeat.exception.ParsingException;
import it.rockeat.util.HashHelper;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RockitEater implements Eater {
	
	public static final String TRACK_LOOKUP_URL = "http://www.rockit.it/web/include/ajax.play.php";
	public static final String PARSING_TRACK_SELECTION_EXPRESSION = "ul.items li.play a";
	public static final String PARSING_TITLE_ARTIST_SEPARATOR = " - ";
	public static final String TOKEN_PARAM = "rockitID";
	public static final String REFERER_VALUE = "http://www.rockit.it/web/js/player3.swf";

	private static Track cleanup(Track track) {
		String cleanedTitle = track.getTitle();
		track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? StringUtils.trim(cleanedTitle.replaceAll(" +", " ")) : cleanedTitle);
		return track;
	}
	
	private static Track getTrackFromJson(InputStream jsonStream) {
		String json = ParsingUtils.streamToString(jsonStream);
		Gson gson = new Gson();
		Track track = gson.fromJson(json, Track.class);
		return track;
	}
	
	private static String generateToken(Track track) {
		return HashHelper.md5(track.getUrl() + "-rapfuturistico");
	}
	
	private Track lookupTrack(HttpClient httpClient, String id, String lookupUrl) throws ConnectionException, LookupException {
		try {
			HttpPost request = new HttpPost(lookupUrl);
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("id", id));
			qparams.add(new BasicNameValuePair("0k", "ok"));
			request.setEntity(new UrlEncodedFormEntity(qparams));
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			InputStream trackInformation = responseEntity.getContent(); 
			Track track = cleanup(getTrackFromJson(trackInformation));
			return track;
		} catch (IOException e) {
			throw new ConnectionException(e);
		} catch (JsonSyntaxException e) {
			throw new LookupException(e);
		}
	}


	@Override
	public Album parse(HttpClient httpClient, String htmlCode) throws ParsingException {
		Album album = new Album();
		String albumTitle = StringUtils.EMPTY;
		String albumArtist = StringUtils.EMPTY;
		List<Track> tracks = new ArrayList<Track>();
		Document doc = Jsoup.parse(htmlCode);
		
		Elements playlistEl = doc.select(PARSING_TRACK_SELECTION_EXPRESSION);
		Integer trackNumber = 0;
		if (CollectionUtils.isNotEmpty(playlistEl)) {
			for (Element trackEl:playlistEl) {
				String trackId = trackEl.attr("rel");
				if (StringUtils.isNotBlank(trackId)) {
					try {
						trackNumber++;
						Track track = lookupTrack(httpClient, trackId, TRACK_LOOKUP_URL);
						track.setId(trackId);
						track.setOrder(trackNumber);
						tracks.add(track);
					} catch (ConnectionException e) {
						/* Connection error on lookup */
						throw new ParsingException(e);
					} catch (LookupException e) {
						/* Track lookup failure */
						throw new ParsingException(e);
					}
				}
			}
			
			Elements title = doc.select("title");
			if (CollectionUtils.isNotEmpty(title)) {
				String meta = title.get(0).text();
				if (StringUtils.contains(meta, PARSING_TITLE_ARTIST_SEPARATOR)) {
					albumTitle = StringUtils.trim(StringUtils.substringBefore(meta, PARSING_TITLE_ARTIST_SEPARATOR));
					albumArtist = StringUtils.trim(StringUtils.substringAfter(meta, PARSING_TITLE_ARTIST_SEPARATOR));
				} else {
					albumArtist = meta;
					albumTitle = "Qualche canzone";
				}
				album.setArtist(albumArtist);
				album.setTitle(albumTitle);
			}
			album.setTracks(tracks);
			return album;
		} else {
			/* Nothing found */
			throw new ParsingException();
		}
	}

	@Override
	public void download(HttpClient httpClient, Track track, OutputStream out) throws ConnectionException {
		try {
			HttpPost request = new HttpPost(track.getUrl());
			request.setHeader("Referer", REFERER_VALUE);
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair(TOKEN_PARAM, generateToken(track)));
			request.setEntity(new UrlEncodedFormEntity(qparams));
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			responseEntity.writeTo(out);
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}

}
