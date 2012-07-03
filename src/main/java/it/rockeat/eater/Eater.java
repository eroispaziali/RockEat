package it.rockeat.eater;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;

import java.io.OutputStream;
import java.net.MalformedURLException;

public interface Eater {

	public Album parse(String htmlCode) throws ParsingException;
	public void download(Track track, OutputStream outputStream) throws ConnectionException;
	public boolean selfDiagnosticTest(String url) throws ConnectionException, ParsingException, MalformedURLException;

}
