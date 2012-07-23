package it.rockeat.source.rockit;

import it.rockeat.SettingsManager;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.LookupException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.ConnectionManager;
import it.rockeat.model.Album;
import it.rockeat.model.RockitTrack;
import it.rockeat.source.MusicSource;
import it.rockeat.util.ActionScriptUtils;
import it.rockeat.util.FileManagementUtils;
import it.rockeat.util.HashUtils;
import it.rockeat.util.ParsingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RockitSource implements MusicSource {

	public static final String ARTWORK_SELECTION_EXPRESSION = ".datialbum a";
    public static final String PARSING_TRACK_SELECTION_EXPRESSION = "ul.items li.play a";
    public static final String PLAYER_SELECTION_EXPRESSION = "div.player embed[type=application/x-shockwave-flash]";
    public static final String PLAYER_SOURCE_FILE = "rockitPlayer.as";
    public static final String REFERER_VALUE = "http://www.rockit.it/web/js/player3.swf";
    public static final String TEMP_FILENAME = ".rockeat.tmp";
    public static final String TEMP_FOLDER = ".rockeat/";
    public static final String TOKEN_PARAM = "rockitID";
    
    private Document document;
    private HttpClient httpClient;
    private File player;
    private String hash; 
    private String secret;
    private SettingsManager settingsManager;
    private String url;
    
    public static final String TRACK_LOOKUP_URL = "http://www.rockit.it/web/include/ajax.play.php";

    public RockitSource(HttpClient httpClient, SettingsManager settingsManager) throws BackendException, ConnectionException, ParsingException, MalformedURLException {
    	this.settingsManager = settingsManager;
        this.httpClient = httpClient;
    }
    
    private static RockitTrack cleanup(RockitTrack track) {
        String cleanedTitle = track.getTitle();
        track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? StringUtils.trim(cleanedTitle.replaceAll(" +", " ")) : cleanedTitle);
        return track;
    }
    
    private boolean isPlayerKnown(String md5) {
    	Map<String, String> keyPairs = settingsManager.getSettings().getKeypairs();
    	return BooleanUtils.isTrue(keyPairs.containsKey(md5));
    }
    
    private String findSecretKey(String md5) throws ParsingException {
    	try {
    		String key = settingsManager.findKey(md5);
    		FileUtils.deleteQuietly(player);
    		return key;
    	} catch (Exception e) {
    		/* decompile */
			try {
		    	ActionScriptUtils.decompileSwf(player.getAbsolutePath(), TEMP_FOLDER);
				String source = FileManagementUtils.findFile(TEMP_FOLDER, PLAYER_SOURCE_FILE);
				String line = FileManagementUtils.searchLine(source, TOKEN_PARAM);
				String key = StringUtils.substringBetween(line, "\"");
				settingsManager.addNewKey(md5, key);
				FileUtils.deleteQuietly(player);
				FileUtils.deleteQuietly(new File(TEMP_FOLDER));
				return key;
	    	} catch (FileNotFoundException ex) {
	    		/* SWF not found */
	    		throw new ParsingException(ex);
	    	} catch (IOException ex) {
	    		/* Error while reading SWF */
	    		throw new ParsingException(ex);
	    	}
    	}
    }

    private static RockitTrack getTrackFromJson(InputStream jsonStream) {
        String json = ParsingUtils.streamToString(jsonStream);
        Gson gson = new Gson();
        RockitTrack track = gson.fromJson(json, RockitTrack.class);
        return track;
    }

    public void tuneIn(String url) throws ConnectionException, MalformedURLException, ParsingException {
    	if (!StringUtils.equals(url, this.url)) {
	    	this.url = url;
	        document = fetchDocument();
	        player = fetchPlayer();
	        hash = HashUtils.md5(player);
			secret = findSecretKey(hash);
    	}
    }
    
    @Override
    public void tearDown() {
    	url = null;
    	document = null;
    	hash = null;
    	secret = null;
    	FileUtils.deleteQuietly(player);
    }

    @Override
    public void download(RockitTrack track, OutputStream out) throws ConnectionException {
        try {
            HttpPost request = new HttpPost(track.getUrl());
            request.setHeader("Referer", REFERER_VALUE);
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair(TOKEN_PARAM, HashUtils.md5(track.getUrl() + secret)));
            request.setEntity(new UrlEncodedFormEntity(qparams));
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseEntity.writeTo(out);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    private Document fetchDocument() throws ConnectionException {
        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            InputStream pageStream = responseEntity.getContent();
            return Jsoup.parse(ParsingUtils.streamToString(pageStream));
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }
    
    private File fetchPlayer() throws ConnectionException, ParsingException, MalformedURLException {
        URL parsedUrl = new URL(url);
        Element playerEl = document.select(PLAYER_SELECTION_EXPRESSION).first();
        String playerUrl;
        if (playerEl != null && playerEl.hasAttr("src")) {
            playerUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + playerEl.attr("src");
            InputStream playerData = ConnectionManager.httpGet(playerUrl);
			try {
				String filename = StringUtils.substringAfterLast(playerUrl, "/");
				OutputStream tmpFile = new FileOutputStream(filename);
				IOUtils.copy(playerData,tmpFile); 
				tmpFile.close();
				return new File(filename);
			} catch (IOException e) {
				throw new ParsingException(e);
			}
        } else {
            throw new ParsingException("Player non trovato nella pagina");
        }
    }

    private RockitTrack lookupTrack(String id, String lookupUrl) throws ConnectionException, LookupException {
        try {
            HttpPost request = new HttpPost(lookupUrl);
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("id", id));
            qparams.add(new BasicNameValuePair("0k", "ok"));
            request.setEntity(new UrlEncodedFormEntity(qparams));
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            InputStream trackInformation = responseEntity.getContent();
            RockitTrack track = cleanup(getTrackFromJson(trackInformation));
            return track;
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (JsonSyntaxException e) {
            throw new LookupException(e);
        }
    }

    @Override
    public void noticeDownloadSuccess() {
        if (!isPlayerKnown(hash)) {
        	settingsManager.addNewKey(hash, secret);
        }
    }
    
    @Override
    public Album findAlbum() throws ParsingException {
        Album album = new Album();
        String albumTitle;
        String albumArtist;
        List<RockitTrack> tracks = new ArrayList<RockitTrack>();
        Elements playlistEl = document.select(PARSING_TRACK_SELECTION_EXPRESSION);
        Integer trackNumber = 0;
        if (CollectionUtils.isNotEmpty(playlistEl)) {
        	
        	 /* Tracks */
            for (Element trackEl : playlistEl) {
                String trackId = trackEl.attr("rel");
                if (StringUtils.isNotBlank(trackId)) {
                    try {
                        trackNumber++;
                        RockitTrack track = lookupTrack(trackId, TRACK_LOOKUP_URL);
                        track.setId(trackId);
                        track.setOrder(trackNumber);
                        tracks.add(track);
                    } catch (ConnectionException e) {
                        throw new ParsingException(e);
                    } catch (LookupException e) {
                        throw new ParsingException(e);
                    }
                }
            }
            
            /* Title */
            Elements title = document.select("title");
            if (CollectionUtils.isNotEmpty(title)) {
                String meta = title.get(0).text();
                final String separator = " - ";
                if (StringUtils.contains(meta,separator)) {
                    albumTitle = StringUtils.trim(StringUtils.substringBefore(meta, separator));
                    albumArtist = StringUtils.trim(StringUtils.substringAfter(meta, separator));
                } else {
                    albumArtist = meta;
                    albumTitle = "Qualche canzone";
                }
                album.setArtist(albumArtist);
                album.setTitle(albumTitle);
            }
            album.setTracks(tracks);
            album.setUrl(url);
            
            /* Artwork */
            Element artworkEl = document.select(ARTWORK_SELECTION_EXPRESSION).first();
            if (artworkEl!=null && artworkEl.hasAttr("href")) {
            	try {
	            	URL parsedUrl = new URL(url);
	            	String artworkUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + artworkEl.attr("href");
	            	InputStream artworkData = ConnectionManager.httpGet(artworkUrl);
	            	String filename = StringUtils.substringAfterLast(artworkUrl, "/");
    				OutputStream tmpFile = new FileOutputStream(filename);
    				IOUtils.copy(artworkData,tmpFile); 
    				tmpFile.close();
    				album.setArtwork(new File(filename));
            	} catch (MalformedURLException e) {
            		/* silently ignore */
            	} catch (ConnectionException e) {
            		/* silently ignore */
            	} catch (IOException e) {
            		/* silently ignore */
				}
            }
            
            return album;
        } else {
            /* Nothing found */
           throw new ParsingException();
        }
    }
    
    @Override
    public boolean runTest() {
    	return isPlayerKnown(hash);
    }

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}


}
