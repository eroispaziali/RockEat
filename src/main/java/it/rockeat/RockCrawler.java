package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.eater.Eater;
import it.rockeat.eater.RockitEater;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class RockCrawler extends WebCrawler {
	

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                      + "|png|tiff?|mid|mp2|mp3|mp4"
                                                      + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                      + "|rm|smil|wmv|swf|wma|zip|rar|gz|ico))$");
    
    private final static Pattern URL_AVOID = Pattern.compile(".*/(canzone|user|news|web|compilation)/.*");
    
    private Map<String, Album> albums = new HashMap<String, Album>();
	 
	@Override
    public boolean shouldVisit(WebURL url) {
		if (albums.containsKey(url)) {
			/* già analizzato */
			return false;
		}
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && !URL_AVOID.matcher(href).matches() && href.startsWith("http://www.rockit.it/");
	}
	
    @Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		Eater eater = new RockitEater();
		try {
			HttpClient client = HttpUtils.createClient();
			Album album = eater.parse(client, url);
			if (album.getTracksCount() > 0) {
				albums.put(url, album);
				System.out.println(url + ": " + album);
			} else {
				throw new ParsingException();
			}
		} catch (Exception e) {
			albums.put(url, null);
			//System.out.println(url + ": KO");
		}
	}
}
