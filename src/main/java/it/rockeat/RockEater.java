package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;

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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class RockEater {
	
	public static final String JSON_BASE_URL = "http://www.rockit.it/web/include/ajax.play.php?id=";
	public static final String TRACK_EL_SELECT_EXPRESSION = "ul.items li.play a";
	public static final String ALBUM_DATA_EL_SELECT_EXPRESSION = ".datialbum";
	public static final String TITLE_ARTIST_SEPARATOR = " - ";
	public static final String SAVE_PATH = ""; //"/home/lorenzo/test/";

	private HttpClient httpClient;
	
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
	
	private void httpDownload(String url, OutputStream out) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);
		request.addHeader("host", "ww2.rockit.it");
		request.addHeader("User-Agent","User-Agent	Mozilla/5.0 (X11; Linux i686; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addHeader("Accept-Language","it-it,it;q=0.8,en-us;q=0.5,en;q=0.3");
		request.addHeader("Accept-Encoding", "gzip, deflate");
		request.addHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		request.addHeader("Connection", "keep-alive");
		request.addHeader("Cookie","__utma=267845901.1025768614.1326885158.1326885158.1326924297.2; __utmz=267845901.1326885158.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmb=267845901.58.9.1326927390837; __utmc=267845901");
		request.addHeader("Referer","http");
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		//request.addHeader("Content-Length", "41");
		request.getParams().setParameter("rockitID", "d6b1f9a1773b5b614722d6aeb55f49cc");
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
						System.out.println("Impossibile ottenere informazioni sulla traccia");
					}
				}
			}
		}
		album.setTracks(tracks);
		return album;
	}
	
	
	public void download(String url) throws IOException, FileNotFoundException {
		String remoteFilename = StringUtils.substringAfterLast(url, "/");
		download(url,remoteFilename);
	}
	
	public void download(String url, String filename) throws IOException, FileNotFoundException {
		String filePath = SAVE_PATH +  filename; // StringUtils.substringAfterLast(url, "/") 
		OutputStream file = new FileOutputStream(filePath);
		httpDownload(url,file);
		file.close();
		File fileOnDisk = new File(filePath);
		System.out.print("["+url+"] --> [" + filename + "] : ");
		Boolean success = false;
		Long sizeOf = 0L;
		try {
			sizeOf = FileUtils.sizeOf(fileOnDisk);
			if (FileUtils.sizeOf(fileOnDisk) == 0) {
				FileUtils.deleteQuietly(fileOnDisk);
				success = false;
			} else {
				success = true;
				System.out.println(FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(fileOnDisk)) + " scaricati");
			}
		} catch (IllegalArgumentException e ) {	}
		System.out.println((success? Long.toString(sizeOf) + " bytes" : "FAIL"));
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
					//System.out.println(HashHelper.hash(track.getUrl() + track.getTitle() + track.getAlbum()));
					download(track.getUrl(), generateFilename(album, track));
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
}
