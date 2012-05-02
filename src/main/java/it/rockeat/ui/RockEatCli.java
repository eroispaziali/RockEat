package it.rockeat.ui;

import java.net.MalformedURLException;

import it.rockeat.Controller;
import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.ParsingException;
import it.rockeat.http.HttpClientFactory;
import it.rockeat.http.RockCrawler;
import it.rockeat.util.FormatUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class RockEatCli {
		 
		private static final String DISABLE_TAGGING = "t";
		private static final String URL = "u";
		private static final String CRAWLING = "e";
		private static final String DOWNLOAD = "d";
		
		private static void printHelp(Options options) {
			HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "java -jar rockeat.jar" , options );
		}
		
		public static void crawl(String url) throws Exception {
			   String crawlStorageFolder = "RockEat-tmp";
		       int numberOfCrawlers = 1;
		       CrawlConfig config = new CrawlConfig();
		       config.setCrawlStorageFolder(crawlStorageFolder);
		       config.setUserAgentString(HttpClientFactory.USER_AGENT_STRING);
		       config.setPolitenessDelay(10);
		       config.setMaxDepthOfCrawling(7);
		       config.setMaxPagesToFetch(10000);
		       config.setResumableCrawling(false);
		       PageFetcher pageFetcher = new PageFetcher(config);
		       RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		       RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		       CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		       controller.addSeed(url);
		       System.out.println();
		       controller.start(RockCrawler.class, numberOfCrawlers);
			}
		
		public static void main(String[] args) {
			Options options = new Options();
			Option optionUrl = new Option(URL, "url", true, "specifica l'indirizzo della pagina da analizzare");
			Option optionTagging = new Option(DISABLE_TAGGING, "disable-tagging", false, "disabilita la scrittura automatica dei tag ID3");
			Option optionDownload = new Option(DOWNLOAD, "download", false, "scarica tutte le tracce disponibili");
			Option optionCrawling = new Option(CRAWLING, "explore", false, "esplora le pagine vicine (funzionalità sperimentale)");
			options.addOption(optionCrawling);
			options.addOption(optionDownload);
			options.addOption(optionTagging);
			options.addOption(optionUrl);
			
		    try {
		    	CommandLineParser parser = new GnuParser();
		        CommandLine line = parser.parse(options,args);
				Controller controller = new Controller();
				controller.setId3TaggingEnabled(!line.hasOption(DISABLE_TAGGING));
				if (line.hasOption(URL)) {
					if (line.hasOption(URL)) {
						String url = line.getOptionValue(URL);
						
						if (false || line.hasOption(CRAWLING)) {
							System.out.println("RockEat sta analizzando la pagina, un attimo di pazienza...");
							try {
								crawl(url);
							} catch (Exception e) {
								System.out.println("RockEat non riesce ad ispezionare nulla.");
							}
						} else {
							System.out.println("RockEat sta analizzando la pagina, un attimo di pazienza...");
							Album album;
							try {
								album = controller.parse(url);
								System.out.println(FormatUtils.formatAlbum(album));
								
								if (line.hasOption(DOWNLOAD)) {
									System.out.print("Download delle tracce: [");
									for (Track track : album.getTracks()) {
										System.out.print("=");
										try {
											controller.download(album, track);
										} catch (DownloadException e) {
											/* Silently ignore */
										} catch (FileSaveException e) {
											/* Silently ignore */
										}
									}
									String label = StringUtils.EMPTY;
									if (controller.getDownloadedTracks()>0) {
										label = "RockEat ha scaricato " + Long.toString(controller.getDownloadedTracks()) + " tracce (" + FileUtils.byteCountToDisplaySize(controller.getBytesDownloaded()) + ") e spera che ti piacciano.";
									} else {
										label = "RockEat non è riuscito a scaricare niente e se ne dispiace.";
									}
									System.out.println("]\n" + label + "\n");
								}

							} catch (MalformedURLException e) {
								System.out.println("RockEat ha bisogno di un indirizzo valido");
							} catch (ConnectionException e) {
								System.out.println("RockEat non è riuscito a connettersi");
							} catch (ParsingException e) {
								System.out.println("RockEat non ha trovato nulla");
							}
						}
			        	
			        }
					
				} else {
					printHelp(options);
				}
		    } catch(ParseException exp) {
		    	printHelp(options);
			} 
		}
}
