package it.rockeat.util;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.Id3TaggingException;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.mpatric.mp3agic.ID3Wrapper;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.Mp3File;

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
	
	public static void id3Tag(Album album, Track track, File fileOnDisk) throws Id3TaggingException {
		try {
			String originalFilename = fileOnDisk.getAbsolutePath();
			String taggedFilename =
					FilenameUtils.getFullPath(fileOnDisk.getAbsolutePath()) + 
					FilenameUtils.getBaseName(fileOnDisk.getName()) + "_tmp." + FilenameUtils.getExtension(fileOnDisk.getName());
			
			Mp3File mp3file = new Mp3File(fileOnDisk.getAbsolutePath());
			cleanupTags(mp3file);
			ID3v2 tag = mp3file.getId3v2Tag();
			tag.setAlbum(album.getTitle());
			tag.setArtist(track.getAuthor());
			tag.setTitle(track.getTitle());
			tag.setTrack(Integer.toString(track.getOrder()));
			// tag.setComment(album.getUrl());
			mp3file.setId3v2Tag(tag);
			mp3file.save(taggedFilename);
			
			// Replace with the new file
			File taggedFile = new File(taggedFilename);
			File originalFile = new File(originalFilename);
			FileUtils.deleteQuietly(fileOnDisk);
			FileUtils.moveFile(taggedFile, originalFile);

		} catch (Exception e) {
			throw new Id3TaggingException();
		}
	}

}
