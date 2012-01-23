package it.rockeat.util;

import org.apache.commons.lang3.StringUtils;

public class ParsingUtils {

	/**
	 * Convert a wildcard string into a regular expression pattern.
	 * @param wildcard the string containing wildcards
	 * @return the correspondent regular expression pattern
	 */
	public static String wildcardToRegex(String wildcard){
		if (wildcard!=null) {
			StringBuffer s = new StringBuffer(wildcard.length());
			s.append('^');
			for (int i = 0, is = wildcard.length(); i < is; i++) {
				char c = wildcard.charAt(i);
				switch(c) {
				case '*':
					s.append(".*");
					break;
				case '?':
					s.append(".");
					break;
					// escape special regexp-characters
				case '(': case ')': case '[': case ']': case '$':
				case '^': case '.': case '{': case '}': case '|':
				case '\\':
					s.append("\\");
					s.append(c);
					break;
				default:
					s.append(c);
				break;
				}
			}
			s.append('$');
			return(s.toString());
		}
		return null;
	}
	
	/**
	 * Return a prefix-safe copy of the provided URL, cleaning up surrounding whitespaces 
	 * Examples:
	 * <ul> 
	 * 	<li>"  https://google.com " -> "https://google.com"</li>
	 *  <li>"www.google.com" -> "http://www.google.com"</li>
	 * </ul>
	 * @param url
	 * @return
	 */
	public static String addProtocolPrefixIfMissing(String url) {
		if (StringUtils.isNotBlank(url)) {
			final String PREFIX_DELIMITER = "://";
			final String DEFAULT_PREFIX = "http";
			url = url.trim();
			if (StringUtils.contains(url, PREFIX_DELIMITER)) {
				String urlBody = StringUtils.substringAfter(url, PREFIX_DELIMITER);
				if (StringUtils.isBlank(urlBody)) {
					return StringUtils.EMPTY;
				}
				return url;
			} else {
				return DEFAULT_PREFIX.concat(PREFIX_DELIMITER).concat(url);
			}
		} else {
			return StringUtils.EMPTY;
		}
	}
}
