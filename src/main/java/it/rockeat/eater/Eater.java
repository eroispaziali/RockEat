package it.rockeat.eater;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;

import java.io.OutputStream;

public interface Eater {

	public Album parse(String htmlCode) throws ParsingException;
	public void download(Track track, OutputStream outputStream) throws ConnectionException;
	public boolean runTest();
	public void noticeDownloadSuccess();

}
