package it.rockeat;

import it.rockeat.bean.Album;

import java.io.IOException;

import org.apache.commons.collections.CollectionUtils;


public class RockEat {
	
	// http://www.rockit.it/nicolocarnesi/album/gli-eroi-non-escono-il-sabato/18316
	
	public static void run(String url) {
		RockEater rockEater = new RockEater();
		try {
			System.out.println("RockEat sta cercando da mangiare su " + url);
			Album album = rockEater.parse(url);
			if (CollectionUtils.isNotEmpty(album.getTracks())) {
				System.out.println("RockEat ha trovato un album: " + album.toString());
				rockEater.download(album);
			} else {
				System.out.println("RockEat non ha trovato niente da mangiare");
			}
		} catch (IOException e) {
			System.out.println("RockEat non riesce a collegarsi alla pagina");
		}
	}
	
	public static void main(String[] args) {
		if (args!=null && args.length>0) {
			run(args[0]);
		} else {
			System.out.println("Specificare un indirizzo RockIt");
		}
		//System.out.println("Esecuzione terminata");
	}

}
