package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.exception.ConnectionException;

import java.net.MalformedURLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;


public class RockEat {
	
	private static final String TAGGING_DISABLE = "noid3";
	
	public static void main(String[] args) {
		
		// Command-line options
		Options options = new Options();
		options.addOption(TAGGING_DISABLE, false, "disabilita il tagging ID3 delle tracce");

		RockEater rockEater = new RockEater();
		CommandLineParser parser = new GnuParser();
		
	    try {
	        CommandLine line = parser.parse(options,args);

	        // Configuration according to CL
	        rockEater.setId3TaggingEnabled(!line.hasOption(TAGGING_DISABLE));
	        
			Album album = rockEater.parse(args[0]);
			if (CollectionUtils.isNotEmpty(album.getTracks())) {
				System.out.println("RockEat ha trovato " + album.toString());
				rockEater.download(album);
			} else {
				System.out.println("RockEat non ha trovato niente.");
			}
	        
	    } catch( ParseException exp ) {
	    	System.err.println("RockEat non sa cosa fare.");
	    } catch (MalformedURLException e) {
			System.out.println("RockEat vorrebbe scaricarti della musica, ma ha bisogno di un indirizzo valido.");
		} catch (ConnectionException e) {
			System.out.println("RockEat è dispiaciuto perché non riesce a collegarsi alla pagina.");
		}
		System.out.println("");
	}

}
