package it.rockeat;

import it.rockeat.exception.ProxySettingsException;
import it.rockeat.util.ProxySettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class RockEat {
	
	private static final String OPTION_PROXY = "p";
	private static final String OPTION_PROXY_IGNORE_HOSTS = "n";
	private static final String OPTION_DISABLE_TAGGING = "d";
	private static final String OPTION_FILE = "f";
	private static final String OPTION_URL = "u";
	private static final String OPTION_SHOW_INFO = "v";
	
	/**
	 * Analizza un file di input, e se trova delle righe le aggiunge alla lista degli URL da processare
	 * @param urls
	 * @param filename
	 */
	private static void processInputFile(List<String> urls, String filename) throws IOException  {
    	File file = new File(filename);
    	if (file.exists()) {
    		try {
    			LineIterator it = FileUtils.lineIterator(file, "UTF-8");
    			try {
    				while (it.hasNext()) {
    					String fileLine = it.nextLine();
    					if (StringUtils.isNotBlank(fileLine) && (!urls.contains(fileLine))) {
    						urls.add(fileLine);
    					}
    				}
    			} finally {
    				it.close();
    			}
    		} catch (IOException e) { 
    			throw e;
    		}
    	} 
	}
	
	
	public static void main(String[] args) {
		
		Options options = new Options();
		Option optionUrl = new Option(OPTION_URL, "url", true, "specifica l'indirizzo della pagina da analizzare");
		Option optionTagging = new Option(OPTION_DISABLE_TAGGING, "disable-tagging", false, "disabilita la scrittura automatica dei tag ID3");
		Option optionFile = new Option(OPTION_FILE, "file", true, "specifica un file con una lista di indirizzi da analizzare");
		Option optionInfoOnly = new Option(OPTION_SHOW_INFO, "verbose", false, "mostra i dettagli delle tracce trovate");
		Option optionProxy = new Option(OPTION_PROXY, "proxy", true, "imposta un proxy HTTP da usare per la connessione");
		Option optionProxyIgnore = new Option(OPTION_PROXY_IGNORE_HOSTS, "no-proxy-for", true, "specifica una lista di indirizzi che non devono passare dal proxy (valori separati da virgola)");

		options.addOption(optionTagging);
		options.addOption(optionProxy);
		options.addOption(optionProxyIgnore);
		options.addOption(optionFile);
		options.addOption(optionUrl);
		options.addOption(optionInfoOnly);

	    try {
	    	CommandLineParser parser = new GnuParser();
	        CommandLine line = parser.parse(options,args);

			RockEater rockEater = new RockEater();
	        rockEater.setDownloadEnabled(Boolean.TRUE);
	        rockEater.setId3TaggingEnabled(!line.hasOption(OPTION_DISABLE_TAGGING));

			List<String> urls = new ArrayList<String>();

			if (line.hasOption(OPTION_URL)) {
	        	urls.add(line.getOptionValue(OPTION_URL));
	        }
	        if (line.hasOption(OPTION_FILE)) {
	        	 processInputFile(urls, line.getOptionValue(OPTION_FILE));
	        } else {
	        	processInputFile(urls, "rockeat.txt");
	        }
	        
	      
	        /* Proxy */
	        if (line.hasOption(OPTION_PROXY)) {
	        	ProxySettings proxySettings = new ProxySettings(line.getOptionValue(OPTION_PROXY));
	        	if (line.hasOption(OPTION_PROXY_IGNORE_HOSTS)) {
	        		 proxySettings.setIgnoredHostList(line.getOptionValue(OPTION_PROXY_IGNORE_HOSTS));
	        	}
	        	rockEater.setProxySettings(proxySettings);
	        }
	        
	        /* Run */
			rockEater.process(urls);
	        
	    } catch( ParseException exp ) {
	    	System.out.println("RockEat non sa cosa fare\n");
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "RockEat", options );
		} catch (ProxySettingsException e) {
			System.out.println("RockEat ha rilevato un errore nella configurazione del proxy");
		} catch (IOException e) {
			System.out.println("File specificato non è valido");
		}
		System.out.println("");
	}

}
