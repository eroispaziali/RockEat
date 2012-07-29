package it.rockeat.util;

import it.rockeat.model.Album;
import it.rockeat.model.Track;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

public class FileManagementUtils {

	public static String escapeSpecialCharsFromFilename(String filename) {
		if (StringUtils.isNotBlank(filename)) {
			filename = filename.replaceAll("[;:<>%?\\*\\/$\"\\\\]", "");
			filename = StringUtils.trim(filename.replaceAll(" +", " "));
			filename = FilenameUtils.normalize(filename);
		}
		return filename;
	}

	public static String createFolder(Album album) {
		try {
			String folderPath = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(album.getArtist())
					&& StringUtils.isNotBlank(album.getTitle())) {
				folderPath = album.getArtist() + " - " + album.getTitle();
			} else {
				folderPath = StringUtils.trim(album.getArtist() + " "
						+ album.getTitle());
			}
			folderPath = escapeSpecialCharsFromFilename(folderPath);
			FileUtils.forceMkdir(new File(folderPath));
			return folderPath + "/";
		} catch (IOException e) {
			return "";
		}
	}

	public static String createFilename(Album album, Track track) {
		String songTitle = StringUtils.trim(track.getTitle());
		Integer howManyDigits = StringUtils.length(Integer
				.toString(CollectionUtils.size(album.getTracks())));
		
		String extension = FilenameUtils.getExtension(track.getRemoteFilename());
		String filename = StringUtils.leftPad(
				Integer.toString(track.getOrder()), howManyDigits, "0")
				+ " - "
				+ escapeSpecialCharsFromFilename(songTitle)
				+ "."
				+ (StringUtils.isNotBlank(extension) ? extension : "mp3");
		return filename;
	}

	public static String findFile(String path, String filename) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] { "**/" + filename });
		scanner.setBasedir(path);
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
		return path + "/" + files[0];
	}

	public static List<String> searchLines(String fileName, String phrase)
			throws IOException {
		List<String> results = new ArrayList<String>();
		Scanner fileScanner = new Scanner(new File(fileName));
		Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				results.add(line);
			}
		}
		fileScanner.close();
		return results;
	}

	public static String searchLine(String fileName, String phrase)
			throws IOException {
		Scanner fileScanner = new Scanner(new File(fileName));
		Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				fileScanner.close();
				return line;
			}
		}
		return null;
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		is.close();
		return bytes;
	}

}
