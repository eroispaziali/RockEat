package it.rockeat.source;

import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;
import java.io.OutputStream;

public interface MusicSource {

	public RockitAlbum parse(String htmlCode) throws ParsingException;
	public void download(RockitTrack track, OutputStream outputStream) throws ConnectionException;
	public boolean runTest();
	public void noticeDownloadSuccess();

}
