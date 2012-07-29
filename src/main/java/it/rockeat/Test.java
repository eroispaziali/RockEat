package it.rockeat;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Album;
import it.rockeat.source.soundcloud.SoundCloud;
import it.rockeat.util.FormatUtils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Injector injector = Guice.createInjector(new RockEatModule());
			SoundCloud sc = injector.getInstance(SoundCloud.class);
			sc.tuneIn("http://soundcloud.com/youngdreams");
			Album album = sc.findAlbum();
			System.out.println(FormatUtils.formatAlbumData(album));
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
