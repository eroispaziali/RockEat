package it.rockeat.source;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.Album;
import it.rockeat.model.Track;

import java.io.OutputStream;
import java.net.MalformedURLException;

public interface MusicSource {

	public void tuneIn(String url) throws ConnectionException,
			MalformedURLException, ParsingException;

	public void release();

	public Album findTracks() throws ParsingException;

	public void download(Track track, OutputStream outputStream)
			throws ConnectionException;

	public boolean runTest();

	public void noticeDownloadSuccess();

}
