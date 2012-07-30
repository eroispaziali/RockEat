package it.rockeat.util;

import it.rockeat.model.Playlist;
import it.rockeat.model.Track;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class FormatUtils {

	public static String formatAlbumData(Playlist album) {
		String result = StringUtils.EMPTY;
		Integer howManyDigits = StringUtils.length(Integer
				.toString(CollectionUtils.size(album.getTracks())));
		Integer maxLenTitle = 0;
		Integer maxLenAuthor = 0;
		Integer maxLenUrl = 0;
		for (Track track : album.getTracks()) {
			if (StringUtils.length(track.getTitle()) > maxLenTitle)
				maxLenTitle = StringUtils.length(track.getTitle());
			if (StringUtils.length(track.getAuthor()) > maxLenAuthor)
				maxLenAuthor = StringUtils.length(track.getAuthor());
			if (StringUtils.length(track.getUrl()) > maxLenUrl)
				maxLenUrl = StringUtils.length(track.getUrl());
		}
		Integer cols = maxLenTitle + maxLenAuthor + maxLenUrl * 0 + 3 * 2
				+ howManyDigits;
		String title = " + " + StringUtils.leftPad("", cols, "-") + " +\n"
				+ " | " + StringUtils.rightPad(album.getTitle(), cols, " ")
				+ " |\n" + " + " + StringUtils.leftPad("", cols, "-") + " + ";
		result += title + "\n";
		for (Track track : album.getTracks()) {
			String line = " | "
					+ StringUtils.leftPad(Integer.toString(track.getOrder()),
							howManyDigits, " ")
					+ " | "
					+ StringUtils.rightPad(track.getTitle(), maxLenTitle, " ")
					+ " | "
					+ StringUtils
							.rightPad(track.getAuthor(), maxLenAuthor, " ")
					+ " | "
			/* StringUtils.rightPad(track.getUrl(), maxLenUrl, " ") +" | "; */;
			result += line + "\n";
		}
		String bottom = " + " + StringUtils.leftPad("", cols, "-") + " + ";
		result += bottom + "\n";
		return result;
	}

}
