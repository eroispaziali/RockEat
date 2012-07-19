package it.rockeat.source.rockit;

import it.rockeat.SettingsManager;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.LookupException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpUtils;
import it.rockeat.model.RockitAlbum;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final static Logger logger = Logger.getLogger(RockitSource.class .getName());
    public static final String PARSING_TRACK_SELECTION_EXPRESSION = "ul.items li.play a";
    public static final String PLAYER_SELECTION_EXPRESSION = "div.player embed[type=application/x-shockwave-flash]";
    public static final String PLAYER_SOURCE_FILE = "rockitPlayer.as";
    public static final String REFERER_VALUE = "http://www.rockit.it/web/js/player3.swf";
    public static final String TEMP_FILENAME = ".rockeat.tmp";
    public static final String TEMP_FOLDER = "rockeat-tmp/";
    public static final String TOKEN_PARAM = "rockitID";
    
    private Document document;
    private HttpClient httpClient;
    private File player;
    private String playerMd5; 
    private String secret;
    private SettingsManager settingsManager;
    private String url;
    
    public static final String TRACK_LOOKUP_URL = "http://www.rockit.it/web/include/ajax.play.php";
    private static RockitTrack cleanup(RockitTrack track) {
        String cleanedTitle = track.getTitle();
        track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? StringUtils.trim(cleanedTitle.replaceAll(" +", " ")) : cleanedTitle);
        return track;
    }
    
    private boolean isPlayerKnown(String playerMd5) {
    	Map<String, String> keyPairs = settingsManager.getSettings().getKeypairs();
    	return BooleanUtils.isTrue(keyPairs.containsKey(playerMd5));
    }
    
    private String findSecretKey(String md5) throws ParsingException {
    	try {
    		return settingsManager.findKey(md5);
    	} catch (Exception e) {
    		/* decompile */
			try {
		    	ActionScriptUtils.decompileSwf(player.getAbsolutePath(), TEMP_FOLDER);
				String source = FileManagementUtils.findFile(TEMP_FOLDER, PLAYER_SOURCE_FILE);
				String line = FileManagementUtils.searchLine(source, TOKEN_PARAM);
				String secret = StringUtils.substringBetween(line, "\"");
				settingsManager.addNewKnownPlayer(md5, secret);
				return secret;
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

    public RockitSource(HttpClient httpClient, SettingsManager settingsManager) throws BackendException, ConnectionException, ParsingException, MalformedURLException {
    	this.settingsManager = settingsManager;
        this.httpClient = httpClient;
    }

    public void prepare(String url) throws ConnectionException, MalformedURLException, ParsingException {
    	this.url = url;
        this.document = fetchDocument(url);
        this.player = fetchPlayer();
        this.playerMd5 = HashUtils.md5(player);
		this.secret = findSecretKey(playerMd5);
    }
    
    public void finalize() {
    	FileUtils.deleteQuietly(player);
    }

    @Override
    public void download(RockitTrack track, OutputStream out) throws ConnectionException {
        try {
        	Map<String, String> keyPairs = settingsManager.getSettings().getKeypairs();
        	String secretInUse = keyPairs.get(playerMd5);
        	logger.log(Level.INFO, "Download di {0} in corso...", track.toString());
            HttpPost request = new HttpPost(track.getUrl());
            request.setHeader("Referer", REFERER_VALUE);
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair(TOKEN_PARAM, HashUtils.md5(track.getUrl() + secretInUse)));
            request.setEntity(new UrlEncodedFormEntity(qparams));
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseEntity.writeTo(out);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    private Document fetchDocument(String url) throws ConnectionException {
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
    	logger.log(Level.INFO, "Sto scaricando il player");
        URL parsedUrl = new URL(url);
        Element playerEl = document.select(PLAYER_SELECTION_EXPRESSION).first();
        String playerUrl;
        if (playerEl != null && playerEl.hasAttr("src")) {
            playerUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + playerEl.attr("src");
            InputStream playerData = HttpUtils.httpGet(playerUrl);
			try {
				OutputStream tmpFile = new FileOutputStream(TEMP_FILENAME);
				IOUtils.copy(playerData,tmpFile); 
				tmpFile.close();
				return new File(TEMP_FILENAME);
			} catch (IOException e) {
				/* Non riesco a leggere il secret dal sorgente */
				throw new ParsingException(e);
			}
        } else {
        	logger.log(Level.WARNING, "Player non trovato nella pagina");
            throw new ParsingException("Player non trovato nella pagina");
        }
    }



    public String getSecret() {
        return secret;
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

    public void noticeDownloadSuccess() {
        if (!isPlayerKnown(playerMd5)) {
        	settingsManager.addNewKnownPlayer(playerMd5, secret);
        }
    }
    
    @Override
    public RockitAlbum parse() throws ParsingException {
        RockitAlbum album = new RockitAlbum();
        String albumTitle;
        String albumArtist;
        List<RockitTrack> tracks = new ArrayList<RockitTrack>();
        Elements playlistEl = document.select(PARSING_TRACK_SELECTION_EXPRESSION);
        Integer trackNumber = 0;
        if (CollectionUtils.isNotEmpty(playlistEl)) {
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
                        /*
                         * Connection error on lookup
                         */
                        throw new ParsingException(e);
                    } catch (LookupException e) {
                        /*
                         * Track lookup failure
                         */
                        throw new ParsingException(e);
                    }
                }
            }
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
            logger.log(Level.INFO, "Trovato: {0}", album.toString());
            return album;
        } else {
            /* Nothing found */
           logger.log(Level.INFO, "Nessun elemento trovato");
           throw new ParsingException();
        }
    }
    
    @Override
    public boolean runTest() {
    	Map<String, String> keyPairs = settingsManager.getSettings().getKeypairs();
        return (keyPairs.containsKey(playerMd5)) ? true : false;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

	
}
