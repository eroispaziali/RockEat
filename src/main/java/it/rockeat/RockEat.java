package it.rockeat;

import it.rockeat.bean.Album;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.collections.CollectionUtils;


public class RockEat {
	
	public static void run(String url) {
		RockEater rockEater = new RockEater();
		try {
			Album album = rockEater.parse(url);
			if (CollectionUtils.isNotEmpty(album.getTracks())) {
				System.out.println("RockEat ha trovato " + album.toString());
				rockEater.download(album);
			} else {
				System.out.println("RockEat non ha trovato niente. Ricorda: RockEat mangia solo roba italiana");
			}
		} catch (MalformedURLException e) {
			System.out.println("RockEat vorrebbe scaricarti della musica, ma ha bisogno di un indirizzo valido");
		} catch (IOException e) {
			System.out.println("RockEat è dispiaciuto perché non riesce a collegarsi alla pagina");
		}
	}
	
	public static void main(String[] args) {
		if (args!=null && args.length>0) {
			run(args[0]);
		} else {
			System.out.println("RockEat non sa cosa fare");
		}
	}

}
