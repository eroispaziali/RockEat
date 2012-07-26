package it.rockeat.ui;

import it.rockeat.Controller;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Album;
import it.rockeat.model.RockitTrack;
import it.rockeat.source.MusicSource;
import it.rockeat.util.FormatUtils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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

public class RockEatCli {
		 
		private static final String DISABLE_TAGGING = "t";
		private static final String URL = "u";
		private static final String EAT = "e";
		private static final String TESTING = "c";
		private static PrintStream out;
		
		public RockEatCli() {
			try {
				out = new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				out = System.out;
			}
		}
		
		private static void printHelp(Options options) {
			HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "java -jar rockeat.jar" , options );
		}
		
		public static void main(String[] args) {
			
			try {
				out = new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				out = System.out;
			}
			
			Options options = new Options();
			options.addOption(new Option(URL, "url", true, "specifica l'indirizzo della pagina da analizzare"));
			options.addOption(new Option(DISABLE_TAGGING, "disable-tagging", false, "disabilita la scrittura automatica dei tag ID3"));
			options.addOption(new Option(EAT, "eat", false, "mangia tutte le tracce disponibili"));
			options.addOption(new Option(TESTING, "test", false, "verifica di essere in grado di scaricare"));
			
		    try {
		    	CommandLineParser parser = new GnuParser();
		        CommandLine commandLine = parser.parse(options,args);
				Controller controller = new Controller();
				controller.setId3TaggingEnabled(!commandLine.hasOption(DISABLE_TAGGING));
				if (commandLine.hasOption(URL)) {
					if (commandLine.hasOption(URL)) {
						String url = commandLine.getOptionValue(URL);
						MusicSource eater = controller.tuneInSource(url);
						
								
						if (commandLine.hasOption(TESTING)) {
							out.println(Messages.PARSE_IN_PROGRESS);
							
							Boolean testResult = eater.runTest();
							if (BooleanUtils.isFalse(testResult)) {
								out.println(Messages.TEST_ERROR);
							} else {
								out.println(Messages.TEST_SUCCESS);
							}
						} else {
							out.println(Messages.PARSE_IN_PROGRESS);
							Album album = controller.findAlbum(url);
							out.println(FormatUtils.formatAlbumData(album));
							
							if (commandLine.hasOption(EAT)) {
								Integer progress = 0;
								Integer count = album.getTracksCount();
								for (RockitTrack track : album.getTracks()) {
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
									Boolean testResult = eater.runTest();
									if (BooleanUtils.isFalse(testResult)) {
										out.println(Messages.TEST_ERROR);
									} else {
										out.println(Messages.TEST_REASON_UNKNOWN);
									}
								}
							}
								
						
						} 
			        }
					
				} else {
					printHelp(options);
				}
		    } catch (MalformedURLException e) {
				out.println(Messages.ERROR_URL);
			} catch (ParsingException e) {
				out.println(Messages.NOTHING_FOUND);
			} catch (ConnectionException e) {
				out.println(Messages.ERROR_CONNECTION);
			} catch (BackendException e) {
				out.println(Messages.ERROR_PLAYER);
			} catch(ParseException exp) {
		    	printHelp(options);
			} 
		}
}
