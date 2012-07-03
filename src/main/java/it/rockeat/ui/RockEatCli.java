package it.rockeat.ui;

import it.rockeat.Controller;
import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.eater.Eater;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.ParsingException;
import it.rockeat.exception.UnknownPlayerException;
import it.rockeat.http.HttpUtils;
import it.rockeat.http.RockCrawler;
import it.rockeat.util.FormatUtils;

import java.io.PrintStream;
import java.net.MalformedURLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
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
		private static final String TESTING = "c";
		private static PrintStream out = System.out; //new PrintWriter(new OutputStreamWriter(System.out));
		
		private static void printHelp(Options options) {
			HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "java -jar rockeat.jar" , options );
		}
		
		public static void crawl(String url) throws Exception {
			   String crawlStorageFolder = "RockEat-tmp";
		       int numberOfCrawlers = 1;
		       CrawlConfig config = new CrawlConfig();
		       config.setCrawlStorageFolder(crawlStorageFolder);
		       config.setUserAgentString(HttpUtils.USER_AGENT_STRING);
		       config.setPolitenessDelay(10);
		       config.setMaxDepthOfCrawling(7);
		       config.setMaxPagesToFetch(10000);
		       config.setResumableCrawling(false);
		       PageFetcher pageFetcher = new PageFetcher(config);
		       RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		       RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		       CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		       controller.addSeed(url);
		       out.println();
		       controller.start(RockCrawler.class, numberOfCrawlers);
		}
		
		public static void main(String[] args) {
			Options options = new Options();
			options.addOption(new Option(URL, "url", true, "specifica l'indirizzo della pagina da analizzare"));
			options.addOption(new Option(DISABLE_TAGGING, "disable-tagging", false, "disabilita la scrittura automatica dei tag ID3"));
			options.addOption(new Option(DOWNLOAD, "download", false, "scarica tutte le tracce disponibili"));
			options.addOption(new Option(CRAWLING, "explore", false, "esplora le pagine vicine (funzionalità sperimentale)"));
			options.addOption(new Option(TESTING, "test", false, "controlla il player audio remoto"));
			
		    try {
		    	CommandLineParser parser = new GnuParser();
		        CommandLine commandLine = parser.parse(options,args);
				Controller controller = new Controller();
				controller.setId3TaggingEnabled(!commandLine.hasOption(DISABLE_TAGGING));
				if (commandLine.hasOption(URL)) {
					if (commandLine.hasOption(URL)) {
						String url = commandLine.getOptionValue(URL);
						if (commandLine.hasOption(CRAWLING)) {
							out.println(Messages.PARSING_IN_PROGRESS);
							try {
								crawl(url);
							} catch (Exception e) {
								out.println("RockEat non riesce ad ispezionare nulla.");
							}
						} else {
							try {
								
								if (commandLine.hasOption(TESTING)) {
									out.println(Messages.PARSING_IN_PROGRESS);
									Eater eater = controller.findEater(url);
									Boolean testResult = eater.selfDiagnosticTest(url);
									if (BooleanUtils.isFalse(testResult)) {
										out.println(Messages.TEST_ERROR);
									} else {
										out.println(Messages.TEST_SUCCESS);
									}
								} else {
									out.println(Messages.PARSING_IN_PROGRESS);
									Album album = controller.parse(url);
									out.println(FormatUtils.formatAlbumData(album));
									
									if (commandLine.hasOption(DOWNLOAD)) {
										Integer progress = 0;
										Integer count = album.getTracksCount();
										for (Track track : album.getTracks()) {
											progress++;
											String st = StringUtils.leftPad(">", progress+1, "=") + StringUtils.leftPad("", count-progress, " ");
											out.print("Download: [" + st + "] "+Long.toString(controller.getDownloadedTracks()+1) + "/" + Integer.toString(count));
											out.print("\r");
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
											label = Messages.DOWNLOAD_COMPLETE;
											label = StringUtils.replace(label,"{0}", Long.toString(controller.getDownloadedTracks()));
											label = StringUtils.replace(label,"{1}", FileUtils.byteCountToDisplaySize(controller.getBytesDownloaded()));

										} else {
											label = Messages.ERROR_DOWNLOAD;
										}
										out.println("\n" + label);
										
										/* Self-diagnostic test on failure */
										if (controller.getDownloadedTracks()==0) {
											Eater eater = controller.findEater(url);
											Boolean testResult = eater.selfDiagnosticTest(url);
											if (BooleanUtils.isFalse(testResult)) {
												out.println(Messages.TEST_NEW_PLAYER);
											} else {
												out.println(Messages.TEST_REASON_UNKNOWN);
											}
										}
									}
									
								}

							} catch (MalformedURLException e) {
								out.println(Messages.ERROR_URL);
							} catch (ConnectionException e) {
								out.println(Messages.ERROR_CONNECTION);
							} catch (ParsingException e) {
								out.println(Messages.NOTHING_FOUND);
							} catch (UnknownPlayerException e) {
								out.println(Messages.ERROR_PLAYER);
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
