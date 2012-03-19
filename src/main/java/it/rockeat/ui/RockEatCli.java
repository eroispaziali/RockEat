package it.rockeat.ui;

import it.rockeat.RockEater;
import it.rockeat.exception.ProxySettingsException;
import it.rockeat.util.ProxySettings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RockEatCli {
		
		private static final String PROXY = "p";
		private static final String PROXY_IGNORE_HOSTS = "n";
		private static final String DISABLE_TAGGING = "d";
		private static final String URL = "u";
		
		private static void printHelp(Options options) {
			HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "java -jar rockeat.jar" , options );
		}
		
		public static void main(String[] args) {
			Options options = new Options();
			Option optionUrl = new Option(URL, "url", true, "specifica l'indirizzo della pagina da analizzare");
			Option optionTagging = new Option(DISABLE_TAGGING, "disable-tagging", false, "disabilita la scrittura automatica dei tag ID3");
			// Option optionProxy = new Option(PROXY, "proxy", true, "imposta un proxy HTTP da usare per la connessione");
			// Option optionProxyIgnore = new Option(PROXY_IGNORE_HOSTS, "no-proxy-for", true, "specifica una lista di indirizzi che non devono passare dal proxy (valori separati da virgola)");

			options.addOption(optionTagging);
			// options.addOption(optionProxy);
			// options.addOption(optionProxyIgnore);
			options.addOption(optionUrl);

		    try {
		    	CommandLineParser parser = new GnuParser();
		        CommandLine line = parser.parse(options,args);

				RockEater rockEater = new RockEater();
		        rockEater.setDownloadEnabled(Boolean.TRUE);
		        rockEater.setId3TaggingEnabled(!line.hasOption(DISABLE_TAGGING));

				List<String> urls = new ArrayList<String>();

				if (line.hasOption(URL)) {
					if (line.hasOption(URL)) {
			        	urls.add(line.getOptionValue(URL));
			        }
			      
			        if (line.hasOption(PROXY)) {
			        	ProxySettings proxySettings = new ProxySettings(line.getOptionValue(PROXY));
			        	if (line.hasOption(PROXY_IGNORE_HOSTS)) {
			        		 proxySettings.setIgnoredHostList(line.getOptionValue(PROXY_IGNORE_HOSTS));
			        	}
			        	rockEater.setProxySettings(proxySettings);
			        }
			        
					rockEater.process(urls);
					
				} else {
					printHelp(options);
				}
		    } catch(ParseException exp) {
		    	printHelp(options);
			} catch (ProxySettingsException e) {
				System.out.println("RockEat ha rilevato un errore nella configurazione del proxy");
			} 
		}
}
