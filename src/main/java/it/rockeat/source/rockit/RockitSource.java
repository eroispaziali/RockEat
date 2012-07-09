package it.rockeat.source.rockit;

import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.rockeat.backend.Backend;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.LookupException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpUtils;
import it.rockeat.source.MusicSource;
import it.rockeat.util.HashUtils;
import it.rockeat.util.ParsingUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;
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

public class RockitSource implements MusicSource {

    public static final String TRACK_LOOKUP_URL = "http://www.rockit.it/web/include/ajax.play.php";
    public static final String PARSING_TRACK_SELECTION_EXPRESSION = "ul.items li.play a";
    public static final String PARSING_TITLE_ARTIST_SEPARATOR = " - ";
    public static final String TOKEN_PARAM = "rockitID";
    public static final String REFERER_VALUE = "http://www.rockit.it/web/js/player3.swf";
    private Map<String, String> keyPairs = new HashMap<String, String>();
    private String url;
    private Document page;
    private String playerMd5;
    private String secret;
    private HttpClient httpClient;
    private Backend backend;        
    
    private final static Logger logger = Logger.getLogger(RockitSource.class .getName()); 

    public RockitSource(String url, HttpClient httpClient, Backend backend) throws BackendException, ConnectionException, ParsingException, MalformedURLException {
        this.url = url;
        this.httpClient = httpClient;
        this.page = fetchDocument();
        this.playerMd5 = analyzePlayer();
        this.backend = backend;
        this.keyPairs = backend.retrieveKnownKeyPairs();
        this.secret = keyPairs.get(playerMd5);
    }

    public RockitSource(String url, HttpClient httpClient, Backend backend, String secret) throws BackendException, ConnectionException, ParsingException, MalformedURLException {
        this.url = url;
        this.httpClient = httpClient;
        this.playerMd5 = analyzePlayer();
        this.backend = backend;
        this.keyPairs = backend.retrieveKnownKeyPairs();
        this.secret = secret;
    }

    private static RockitTrack cleanup(RockitTrack track) {
        String cleanedTitle = track.getTitle();
        track.setTitle(StringUtils.isNotBlank(cleanedTitle) ? StringUtils.trim(cleanedTitle.replaceAll(" +", " ")) : cleanedTitle);
        return track;
    }

    private static RockitTrack getTrackFromJson(InputStream jsonStream) {
        String json = ParsingUtils.streamToString(jsonStream);
        Gson gson = new Gson();
        RockitTrack track = gson.fromJson(json, RockitTrack.class);
        return track;
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
    public RockitAlbum parse(String htmlCode) throws ParsingException {
        logger.info("Parsing... ");
        RockitAlbum album = new RockitAlbum();
        String albumTitle;
        String albumArtist;
        List<RockitTrack> tracks = new ArrayList<RockitTrack>();
        Document doc = Jsoup.parse(htmlCode);
        Elements playlistEl = doc.select(PARSING_TRACK_SELECTION_EXPRESSION);
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
            logger.log(Level.INFO, "Trovato un elemento: {0}", album.toString());
            return album;
        } else {
            /*
             * Nothing found
             */
           logger.log(Level.INFO, "Nessun elemento trovato");
            throw new ParsingException();
        }
    }

    @Override
    public void download(RockitTrack track, OutputStream out) throws ConnectionException {
        try {
            System.out.println("Downloading " + track.toString());
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

    @Override
    public boolean runTest() {
        return (keyPairs.containsKey(playerMd5)) ? true : false;
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

    private String analyzePlayer() throws ConnectionException, ParsingException, MalformedURLException {
        System.out.println("Analisi player in corso...");
        URL parsedUrl = new URL(url);
        Element playerEl = page.select("div.player embed[type=application/x-shockwave-flash]").first();
        String playerUrl;
        if (playerEl != null && playerEl.hasAttr("src")) {
            playerUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + playerEl.attr("src");
            InputStream playerSwf = HttpUtils.httpGet(playerUrl);
            String hash = HashUtils.md5(playerSwf);
            System.out.println("Hash player: " + hash);
            return hash;
        } else {
            System.out.println("Player non trovato nella pagina");
            throw new ParsingException("Player non trovato nella pagina");
        }
    }

    public void noticeDownloadSuccess() {
        if (!keyPairs.containsKey(playerMd5)) {
            try {                
                backend.storeKeyPair(playerMd5, secret);
                keyPairs.put(playerMd5, secret);
            } catch (BackendException e) {
                /*
                 * non sono riuscito a salvare il keypair, ignoro
                 * silenziosamente
                 */
            }
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
