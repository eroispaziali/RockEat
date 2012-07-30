package it.rockeat.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

public class ParsingUtils {

	/**
	 * Convert a wildcard string into a regular expression pattern.
	 * 
	 * @param wildcard
	 *            the string containing wildcards
	 * @return the correspondent regular expression pattern
	 */
	public static String wildcardToRegex(String wildcard) {
		if (wildcard != null) {
			StringBuffer s = new StringBuffer(wildcard.length());
			s.append('^');
			for (int i = 0, is = wildcard.length(); i < is; i++) {
				char c = wildcard.charAt(i);
				switch (c) {
				case '*':
					s.append(".*");
					break;
				case '?':
					s.append(".");
					break;
				// escape special regexp-characters
				case '(':
				case ')':
				case '[':
				case ']':
				case '$':
				case '^':
				case '.':
				case '{':
				case '}':
				case '|':
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
			return (s.toString());
		}
		return null;
	}

	/**
	 * Return a prefix-safe copy of the provided URL, cleaning up surrounding
	 * whitespaces Examples:
	 * <ul>
	 * <li>"  https://google.com " -> "https://google.com"</li>
	 * <li>"www.google.com" -> "http://www.google.com"</li>
	 * </ul>
	 * 
	 * @param url
	 * @return
	 */
	public static String addProtocolPrefixIfMissing(String url) {
		if (StringUtils.isNotBlank(url)) {
			final String PREFIX_DELIMITER = "://";
			final String DEFAULT_PREFIX = "http";
			url = url.trim();
			if (StringUtils.contains(url, PREFIX_DELIMITER)) {
				String urlBody = StringUtils.substringAfter(url,
						PREFIX_DELIMITER);
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

	public static boolean isValidUrl(String url) {
		try {
			@SuppressWarnings("unused")
			URL parsed = new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public static String getDomainName(String url) throws MalformedURLException {
	    if(!url.startsWith("http") && !url.startsWith("https")){
	         url = "http://" + url;
	    }        
	    URL netUrl = new URL(url);
	    String host = netUrl.getHost();
	    if(host.startsWith("www")){
	        host = host.substring("www".length()+1);
	    }
	    return host;
	}
	
	public static String streamToString(InputStream is) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			return sb.toString();
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}
	
	 public static String toUrlEncodedCommaSeparatedList(Object[] objects) {
        StringBuilder params = new StringBuilder();
        for (Object object : objects) {
            try {
                params.append(URLEncoder.encode(object.toString(), "UTF-8"));
                params.append(',');
            } catch (UnsupportedEncodingException e) { }
        }
        return (params.length() > 0 ? params.substring(0, params.length() - 1) : params.toString());
	}


}
