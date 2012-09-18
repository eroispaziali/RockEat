package it.rockeat.source.rockit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Playlist;
import it.rockeat.util.HashUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RockitTest {
	
	private Rockit rockit;
	private Playlist playlist;

	@Before
	public void setUp() throws Exception {
		Injector injector = Guice.createInjector();
		rockit = injector.getInstance(Rockit.class);
	}
	
	@After
	public void tearDown() throws Exception {
		rockit.release();
	}

	@Test(timeout=20000)
	public void testTuneIn() {
		try {
			rockit.tuneIn("http://www.rockit.it/lostatosociale/album/turisti-della-democrazia/18595");
			playlist = rockit.findTracks();
			assertEquals(playlist.getArtist(),"Lo Stato Sociale");
			assertEquals(playlist.getTitle(),"Turisti della democrazia");
			assertEquals(playlist.getTracksCount(), 11);
			assertEquals(HashUtils.md5(playlist.getArtwork()),"b35482082c3fdf6ac23737e1cefa2147");
		} catch (MalformedURLException e) {
			// unable to test
		} catch (ConnectionException e) {
			// unable to test
		} catch (ParsingException e) {
			fail("Il parsing della pagina ha fallito");
		}
	}
	
	@Test
	public void testSecret() {

		try {
			rockit.tuneIn("http://www.rockit.it/lostatosociale/album/turisti-della-democrazia/18595");
		} catch (Exception e) {
			fail("Impossibile testare la ricerca del secret");
		}

		try {
			assertNotNull(rockit.player);
			rockit.findSecretKey(rockit.hash);
		} catch (ParsingException e) {
			fail("La ricerca del secret ha fallito");			
		}
	}
	
	@Test
	public void testDownload() {

		try {
			rockit.tuneIn("http://www.rockit.it/lostatosociale/album/turisti-della-democrazia/18595");
			playlist = rockit.findTracks();
		} catch (Exception e) {
			fail("Impossibile testare il download");
		}

		String testFile = "rockit.test";
		OutputStream output;
		try {
			output = new FileOutputStream(testFile);
			rockit.download(playlist.getTracks().get(0), output);
			output.close();
			File fileOnDisk = new File(testFile);
			assertTrue(FileUtils.sizeOf(fileOnDisk) > 0);
			FileUtils.deleteQuietly(fileOnDisk);
		} catch (FileNotFoundException e) {
			fail("Problema di I/O durante il download");
		} catch (ConnectionException e) {
			fail("Problema di connessione durante il download");
		} catch (IOException e) {
			fail("Problema di scrittura su disco durante il download");
		}
	}

}
