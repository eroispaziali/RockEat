package it.rockeat.util;

import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;

import java.io.File;
import java.io.IOException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class FileManagementUtils {

	public static String escapeSpecialCharsFromFilename(String filename) {
		if (StringUtils.isNotBlank(filename)) {
			filename = filename.replaceAll("[;:<>%?\\*\\/$\"\\\\]","");
			filename = StringUtils.trim(filename.replaceAll(" +", " "));
			filename = FilenameUtils.normalize(filename);
		}
		return filename;
	}
	
	public static String createFolder(RockitAlbum album) {
		try {
			String folderPath = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(album.getArtist()) && StringUtils.isNotBlank(album.getTitle())) {
				folderPath = album.getArtist() + " - " + album.getTitle();
			} else {
				folderPath = StringUtils.trim(album.getArtist() + " " + album.getTitle());
			}
			folderPath = escapeSpecialCharsFromFilename(folderPath);
			FileUtils.forceMkdir(new File(folderPath));
			return folderPath + "/";
		} catch (IOException e) {
			return "";
		}
	}
	
	public static String createFilename(RockitAlbum album, RockitTrack track) {
		String songTitle = StringUtils.trim(track.getTitle());
		Integer howManyDigits = StringUtils.length(Integer.toString(CollectionUtils.size(album.getTracks())));
		String filename =
				StringUtils.leftPad(Integer.toString(track.getOrder()), howManyDigits, "0")
				+ " - " + escapeSpecialCharsFromFilename(songTitle) 
				+ "." + FilenameUtils.getExtension(track.getRemoteFilename());
		return filename;
	}

}
