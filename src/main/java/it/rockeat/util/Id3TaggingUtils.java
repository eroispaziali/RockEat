package it.rockeat.util;

import it.rockeat.exception.Id3TaggingException;
import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.mpatric.mp3agic.ID3Wrapper;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Id3TaggingUtils {
	
	private static void cleanupTags(Mp3File mp3file) {
		ID3Wrapper oldId3Wrapper = new ID3Wrapper(mp3file.getId3v1Tag(), mp3file.getId3v2Tag());
		ID3Wrapper newId3Wrapper = new ID3Wrapper(new ID3v1Tag(), new ID3v23Tag());
		newId3Wrapper.setTrack(StringUtils.trim(oldId3Wrapper.getTrack()));
		newId3Wrapper.setArtist(StringUtils.trim(oldId3Wrapper.getArtist()));
		newId3Wrapper.setTitle(StringUtils.trim(oldId3Wrapper.getTitle()));
		newId3Wrapper.setArtist(StringUtils.trim(oldId3Wrapper.getArtist()));
		newId3Wrapper.setAlbum(StringUtils.trim(oldId3Wrapper.getAlbum()));
		newId3Wrapper.setYear(StringUtils.trim(oldId3Wrapper.getYear()));
		newId3Wrapper.setGenre(oldId3Wrapper.getGenre());
		newId3Wrapper.setComposer(StringUtils.trim(oldId3Wrapper.getComposer()));
		newId3Wrapper.setOriginalArtist(StringUtils.trim(oldId3Wrapper.getOriginalArtist()));
		newId3Wrapper.setCopyright(StringUtils.trim(oldId3Wrapper.getCopyright()));
		newId3Wrapper.setUrl(StringUtils.trim(oldId3Wrapper.getUrl()));
		newId3Wrapper.getId3v2Tag().setPadding(true);
		mp3file.setId3v1Tag(newId3Wrapper.getId3v1Tag());
		mp3file.setId3v2Tag(newId3Wrapper.getId3v2Tag());
	}
	
	public static void id3Tag(RockitAlbum album, RockitTrack track, File fileOnDisk) throws Id3TaggingException {
		String originalFilename = fileOnDisk.getAbsolutePath();
		String temporaryFilename =
				FilenameUtils.getFullPath(fileOnDisk.getAbsolutePath()) + 
				FilenameUtils.getBaseName(fileOnDisk.getName()) + "_tmp." + FilenameUtils.getExtension(fileOnDisk.getName());

		File temporaryFile = new File(temporaryFilename);
		File originalFile = new File(originalFilename);

		try {
			Mp3File mp3file = new Mp3File(fileOnDisk.getAbsolutePath());
			cleanupTags(mp3file);
			ID3v2 tag = mp3file.getId3v2Tag();
			tag.setAlbum(album.getTitle());
			tag.setArtist(track.getAuthor());
			tag.setTitle(track.getTitle());
			tag.setTrack(Integer.toString(track.getOrder()));
			mp3file.setId3v2Tag(tag);
			mp3file.save(temporaryFilename);
			
			// Replace with the new file
			FileUtils.deleteQuietly(fileOnDisk);
			FileUtils.moveFile(temporaryFile, originalFile);

		} catch (IOException e) {
			FileUtils.deleteQuietly(temporaryFile);
			throw new Id3TaggingException();
		} catch (InvalidDataException e) {
			FileUtils.deleteQuietly(temporaryFile);
			throw new Id3TaggingException();
		} catch (NotSupportedException e) {
			FileUtils.deleteQuietly(temporaryFile);
			throw new Id3TaggingException();
		} catch (UnsupportedTagException e) {
			FileUtils.deleteQuietly(temporaryFile);
			throw new Id3TaggingException();
		} catch (Exception e) {
			FileUtils.deleteQuietly(temporaryFile);
			throw new Id3TaggingException();			
		}
	}

}
