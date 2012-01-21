package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.FileWriteException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.util.HashHelper;
import it.rockeat.util.Id3TaggingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
	
	public static final String URL_TRACK_LOOKUP = "/web/include/ajax.play.php";
	public static final String PARSING_TRACK_SELECTECTION_EXPRESSION = "ul.items li.play a";
	public static final String PARSING_TITLE_ARTIST_SEPARATOR = " - ";
	public static final String TOKEN_PARAM = "rockitID";

	private HttpClient httpClient;
	private Long downloadedTracks = 0L;
	private Long bytesDownloaded = 0L;
	
	private Boolean id3TaggingEnabled = Boolean.TRUE;
	
	public RockEater() {
		this.httpClient = new DefaultHttpClient();
	}
	
	public RockEater(HttpClient customHttpClient) {
		this.httpClient = customHttpClient;
	}
	
	private InputStream httpGet(String url) throws ConnectionException {
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity responseEntity = response.getEntity();
			return responseEntity.getContent();
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
	
	private String generateToken(Track track) {
		return HashHelper.md5(track.getUrl() + "-daisyduke");
	}
	
	private void httpDownload(Track track, OutputStream out) throws ConnectionException {
		try {
			HttpPost request = new HttpPost(track.getUrl());
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
	
	private Track lookupTrack(String id, String lookupUrl) throws ConnectionException {
		try {
			HttpPost request = new HttpPost(lookupUrl);
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("id", id));
			request.setEntity(new UrlEncodedFormEntity(qparams));
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			InputStream trackInformation = responseEntity.getContent(); 
			Track track = getTrackFromJson(trackInformation);
			String cleanedTitle = track.getTitle();
			track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? cleanedTitle.replaceAll(" +", " ") : cleanedTitle);
			return track;
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
	
	public Album parse(String url) throws MalformedURLException, ConnectionException {

		URL parsedUrl = new URL(url);
		String baseUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
		
		System.out.println("RockEat sta cercando da mangiare su " + url);
		Album album = new Album();
		String albumTitle = StringUtils.EMPTY;
		String albumArtist = StringUtils.EMPTY;
		InputStream pageStream = httpGet(url);
		List<Track> tracks = new ArrayList<Track>();
		Document doc = Jsoup.parse(streamToString(pageStream));
		Elements title = doc.select("title");
		if (CollectionUtils.isNotEmpty(title)) {
			String meta = title.get(0).text();
			if (StringUtils.contains(meta, PARSING_TITLE_ARTIST_SEPARATOR)) {
				albumTitle = StringUtils.trim(StringUtils.substringBefore(meta, PARSING_TITLE_ARTIST_SEPARATOR));
				albumArtist = StringUtils.trim(StringUtils.substringAfter(meta, PARSING_TITLE_ARTIST_SEPARATOR));
			} else {
				albumArtist = meta;
				albumTitle = "";
			}
			album.setArtist(albumArtist);
			album.setTitle(albumTitle);
		}
		
		Elements playlistEl = doc.select(PARSING_TRACK_SELECTECTION_EXPRESSION);
		Integer index = 0;
		if (CollectionUtils.isNotEmpty(playlistEl)) {
			for (Element trackEl:playlistEl) {
				String trackId = trackEl.attr("rel");
				if (StringUtils.isNotBlank(trackId)) {
					try {
						index++;
						Track track = lookupTrack(trackId, baseUrl + URL_TRACK_LOOKUP);
						track.setId(trackId);
						track.setOrder(index);
						tracks.add(track);
					} catch (ConnectionException e) {
						System.out.println("RockEat non è riuscito ad ottenere informazioni sulla traccia");
					}
				}
			}
		}
		album.setTracks(tracks);
		return album;
	}
	
	private File download(Track track, String filename) throws ConnectionException, FileWriteException {
		System.out.print("RockEat sta scaricando " + track.toString() + "... ");
		String filePath = filename; 
		try {
			OutputStream file = new FileOutputStream(filePath);
			httpDownload(track,file);
			file.close();
			File fileOnDisk = new File(filePath);
			Boolean success = false;
			try {
				if (FileUtils.sizeOf(fileOnDisk) == 0) {
					success = false;
					FileUtils.deleteQuietly(fileOnDisk);
				} else {
					success = true;
					bytesDownloaded += FileUtils.sizeOf(fileOnDisk);
					downloadedTracks++;
				}
			} catch (IllegalArgumentException e ) {	}
			System.out.println((success? "" : "niente da fare"));
			return fileOnDisk;
			
		} catch (IOException e) {
			throw new FileWriteException(e);
		}
	}
	
	private String generateFilename(Album album, Track track) {
		String filenameOnServer = StringUtils.substringAfterLast(track.getUrl(), "/");
		String songTitle = StringUtils.trim(track.getTitle());
		Integer howManyDigits = StringUtils.length(Integer.toString(CollectionUtils.size(album.getTracks())));
		String filename =
				StringUtils.leftPad(Integer.toString(track.getOrder()), howManyDigits, "0")
				+ " - " + FilenameUtils.normalize(songTitle) 
				+ "." + FilenameUtils.getExtension(filenameOnServer);
		return filename;
	}
	
	private String createFolder(Album album) {
		try {
			String folderPath = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(album.getArtist()) && StringUtils.isNotBlank(album.getTitle())) {
				folderPath = album.getArtist() + " - " + album.getTitle();
			} else {
				folderPath = StringUtils.trim(album.getArtist() + " " + album.getTitle());
			}
			FileUtils.forceMkdir(new File(FilenameUtils.normalize(folderPath)));
			return folderPath + "/";
		} catch (IOException e) {
			return "";
		}
	}
	
	public void download(Album album) {
		List<Track> tracks = album.getTracks();
		if (CollectionUtils.isNotEmpty(tracks)) {
			String folderName = createFolder(album);
			for(Track track:tracks) {
				try {
					String filePath = folderName + generateFilename(album, track); 
					File downloadedFile = download(track, filePath);
					if (BooleanUtils.isTrue(id3TaggingEnabled)) {
						try {
							Id3TaggingUtils.id3Tag(album, track, downloadedFile);
						} catch (Id3TaggingException e) {}
					}
				} catch (Exception e) {
					System.out.println("oops");
				} 
			}

			String message = (downloadedTracks>0)
				? "Rockeat ha scaricato " + Long.toString(downloadedTracks) + " tracce (" + FileUtils.byteCountToDisplaySize(bytesDownloaded) + ") e spera che ti piacciano"
				: "RockEat non è riuscito a scaricare nulla e se ne dispiace";
			
			System.out.println(StringUtils.leftPad("", StringUtils.length(message), "="));
			System.out.println(message);
		}
	}

	public Boolean getId3TaggingEnabled() {
		return id3TaggingEnabled;
	}

	public void setId3TaggingEnabled(Boolean id3TaggingEnabled) {
		this.id3TaggingEnabled = id3TaggingEnabled;
	}
	
}
