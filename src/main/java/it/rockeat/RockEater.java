package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.Id3TaggingException;
import it.rockeat.util.FileManagementUtils;
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
	private Long parsedTracks = 0L;
	private Long downloadedAlbums = 0L;
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
	
	private static Track cleanup(Track track) {
		String cleanedTitle = track.getTitle();
		track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? StringUtils.trim(cleanedTitle.replaceAll(" +", " ")) : cleanedTitle);
		return track;
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
			Track track = cleanup(getTrackFromJson(trackInformation));
			return track;
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
	
	public Album parse(String url) throws MalformedURLException, ConnectionException {

		URL parsedUrl = new URL(url);
		String baseUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
		
		System.out.println("RockEat sta annusando " + url);
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
				albumTitle = StringUtils.EMPTY;
			}
			album.setArtist(albumArtist);
			album.setTitle(albumTitle);
			album.setUrl(url);
		}
		
		Elements playlistEl = doc.select(PARSING_TRACK_SELECTECTION_EXPRESSION);
		Integer trackNumber = 0;
		if (CollectionUtils.isNotEmpty(playlistEl)) {
			for (Element trackEl:playlistEl) {
				String trackId = trackEl.attr("rel");
				if (StringUtils.isNotBlank(trackId)) {
					try {
						parsedTracks++;
						trackNumber++;
						Track track = lookupTrack(trackId, baseUrl + URL_TRACK_LOOKUP);
						track.setId(trackId);
						track.setOrder(trackNumber);
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
	
	private File download(Track track, String filename) throws ConnectionException, DownloadException, FileSaveException {
		String filePath = filename; 
		try {
			OutputStream file = new FileOutputStream(filePath);
			httpDownload(track,file);
			file.close();
			File fileOnDisk = new File(filePath);
			if (FileUtils.sizeOf(fileOnDisk) == 0) {
				FileUtils.deleteQuietly(fileOnDisk);
				throw new DownloadException();
			} else {
				bytesDownloaded += FileUtils.sizeOf(fileOnDisk);
				downloadedTracks++;
				return fileOnDisk;
			}
			
		} catch (IOException e) {
			throw new FileSaveException(e);
		}
	}
	
	public void download(Album album) {
		List<Track> tracks = album.getTracks();
		if (CollectionUtils.isNotEmpty(tracks)) {
			String folderName = FileManagementUtils.createFolder(album);
			Integer count = 0;
			for ( Track track : tracks ) {
				try {
					System.out.print("RockEat sta scaricando " + track.toString() + "... ");
					String filePath = folderName + FileManagementUtils.createFilename(album, track); 
					File file = download(track, filePath);
					if (BooleanUtils.isTrue(id3TaggingEnabled)) {
						try {
							Id3TaggingUtils.id3Tag(album, track, file);
						} catch (Id3TaggingException e) {}
					}
					count++;
					System.out.println("");
				} catch (FileSaveException e) {
					System.out.println("non riesce a salvare il file. Disco pieno?");
				} catch (ConnectionException e) {
					System.out.println("niente da fare");
				} catch (DownloadException e) {
					System.out.println("niente da fare");
				}
			}
			
			if (count.equals(CollectionUtils.size(album.getTracks()))) {
				downloadedAlbums++;
			}
			
			String message = (downloadedTracks>0)
				? "RockEat ha scaricato " + Long.toString(downloadedAlbums) + " album, per un totale di " + Long.toString(downloadedTracks) + " tracce (" + FileUtils.byteCountToDisplaySize(bytesDownloaded) + ") e spera che ti piacciano"
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

	public Long getParsedTracks() {
		return parsedTracks;
	}

	public Long getDownloadedTracks() {
		return downloadedTracks;
	}

	public Long getBytesDownloaded() {
		return bytesDownloaded;
	}

	public Long getDownloadedAlbums() {
		return downloadedAlbums;
	}
	
}
