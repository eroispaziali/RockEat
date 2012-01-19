package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.util.HashHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class RockEater {
	
	public static final String JSON_BASE_URL = "http://www.rockit.it/web/include/ajax.play.php?id=";
	public static final String TRACK_EL_SELECT_EXPRESSION = "ul.items li.play a";
	public static final String ALBUM_DATA_EL_SELECT_EXPRESSION = "div.datialbum";
	public static final String TITLE_ARTIST_SEPARATOR = " - ";
	public static final String SAVE_PATH = "";

	private HttpClient httpClient;
	private Long tracksCounter = 0L;
	private Long bytesDownloaded = 0L;
	
	public RockEater() {
		this.httpClient = new DefaultHttpClient();
	}
	
	public RockEater(HttpClient customHttpClient) {
		this.httpClient = customHttpClient;
	}
	
	private InputStream httpGet(String url) throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity responseEntity = response.getEntity();
		return responseEntity.getContent(); 
	}
	
	private String generateRockitId(Track track) {
		return HashHelper.md5(track.getUrl() + "-daisyduke");
	}
	
	private void httpDownload(Track track, OutputStream out) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(track.getUrl());
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("rockitID", generateRockitId(track)));
		request.setEntity(new UrlEncodedFormEntity(qparams));
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		responseEntity.writeTo(out); 
	}
	
	private Track getTrackFromJson(InputStream jsonStream) {
		String json = streamToString(jsonStream);
		Gson gson = new Gson();
		Track track = gson.fromJson(json, Track.class);  
		return track;
	}
	
	private static String streamToString(InputStream is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		      sb.append(line + "\n");
		    }
		    is.close();
		    return sb.toString();
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}
	
	public Album parse(String url) throws IOException {
		Album album = new Album();
		String albumTitle = StringUtils.EMPTY;
		String albumArtist = StringUtils.EMPTY;
		InputStream pageStream = httpGet(url);
		List<Track> tracks = new ArrayList<Track>();
		Document doc = Jsoup.parse(streamToString(pageStream));
		Elements title = doc.select("title");
		if (CollectionUtils.isNotEmpty(title)) {
			String meta = title.get(0).text();
			albumTitle = StringUtils.trim(StringUtils.substringBefore(meta, TITLE_ARTIST_SEPARATOR));
			albumArtist = StringUtils.trim(StringUtils.substringAfter(meta, TITLE_ARTIST_SEPARATOR));
			album.setArtist(albumArtist);
			album.setTitle(albumTitle);
		}
		Elements albumDataEl = doc.select(ALBUM_DATA_EL_SELECT_EXPRESSION);
		if (CollectionUtils.isNotEmpty(albumDataEl)) {
			for (Element dataEl : albumDataEl) {
				
				if (dataEl.hasClass("anno")) {
					album.setYear(StringUtils.trim(dataEl.text()));
				}
				
				if (dataEl.hasClass("etichette")) {
					album.setLabel(StringUtils.trim(dataEl.text()));
				}
				
				if (dataEl.hasClass("genere")) {
					album.setGenre(StringUtils.trim(dataEl.text()));
				}
			}
		}
		
		Elements playlistEl = doc.select(TRACK_EL_SELECT_EXPRESSION);
		Integer index = 0;
		if (CollectionUtils.isNotEmpty(playlistEl)) {
			for (Element trackEl:playlistEl) {
				String trackId = trackEl.attr("rel");
				if (StringUtils.isNotBlank(trackId)) {
					try {
						index++;
						InputStream jsonStream = httpGet(JSON_BASE_URL + trackId);
						Track track = getTrackFromJson(jsonStream);
						track.setId(trackId);
						track.setOrder(index);
						tracks.add(track);
					} catch (Exception e) {
						System.out.println("RockEat non è riuscito ad ottenere informazioni sulla traccia");
					}
				}
			}
		}
		album.setTracks(tracks);
		return album;
	}
	
	public void download(Track track, String filename) throws IOException, FileNotFoundException {
		String filePath = SAVE_PATH +  filename; 
		OutputStream file = new FileOutputStream(filePath);
		httpDownload(track,file);
		file.close();
		File fileOnDisk = new File(filePath);
		System.out.print("[" + track.getUrl() + "] --> [" + filename + "] : ");
		Boolean success = false;
		try {
			if (FileUtils.sizeOf(fileOnDisk) == 0) {
				FileUtils.deleteQuietly(fileOnDisk);
				success = false;
			} else {
				success = true;
				bytesDownloaded += FileUtils.sizeOf(fileOnDisk);
				tracksCounter++;
			}
		} catch (IllegalArgumentException e ) {	}
		System.out.println((success? FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(fileOnDisk)) + " scaricati" : "FAIL"));
	}
	
	public String generateFilename(Album album, Track track) {
		String remoteFilename = StringUtils.substringAfterLast(track.getUrl(), "/");
		String baseFilename = track.getTitle().replaceAll("[^a-zA-Z0-9 ]+","");
		baseFilename = baseFilename.replace(" ","_");
		String filename =
				StringUtils.right("00" + Integer.toString(track.getOrder()), 2)
				+ "-" + FilenameUtils.normalize(baseFilename) 
				+ "." + FilenameUtils.getExtension(remoteFilename);
		return filename;
	}
	
	public void download(Album album) {
		List<Track> tracks = album.getTracks();
		if (CollectionUtils.isNotEmpty(tracks)) {
			for(Track track : tracks) {
				try {
					download(track, generateFilename(album, track));
				} catch (Exception e) {
					System.out.println("RockEat non riesce a scaricare questa traccia, la salterà");
				} 
			}
			System.out.println("RockEater ha scaricato " + tracksCounter + " e le ha mangiate tutte");
		}
	}
	
}
