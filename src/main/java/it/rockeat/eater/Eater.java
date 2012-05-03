package it.rockeat.eater;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;

import java.io.OutputStream;

import org.apache.http.client.HttpClient;

public interface Eater {

	public Album parse(HttpClient client, String htmlCode) throws ParsingException;
	public void download(HttpClient client, Track track, OutputStream outputStream) throws ConnectionException;

}
