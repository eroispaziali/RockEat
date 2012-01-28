package it.rockeat.util;

import it.rockeat.exception.ProxySettingsException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

public class ProxySettings {
	
	private HttpHost host;
	private Credentials credentials;
	private Set<String> ignoredHosts = new HashSet<String>();
	
	public ProxySettings(String optionLine) throws ProxySettingsException {
		
		String proxyUrl = StringUtils.EMPTY;
		if (StringUtils.contains(optionLine, "@")) {

			// With authentication
			String credentialString = StringUtils.substringBefore(optionLine,"@");
			String username = StringUtils.substringBefore(credentialString, ":");
			String password = StringUtils.substringAfter(credentialString, ":");
			credentials = new UsernamePasswordCredentials(username, password);
			
			proxyUrl = StringUtils.substringAfter(optionLine,"@");
		} else {
			
			// Without authentication
			credentials = null;
			proxyUrl = optionLine;
		}
		
		try {
			URL url = new URL(proxyUrl);
			host = new HttpHost(url.getHost(), url.getPort(), "http");
		} catch (MalformedURLException e) {
			throw new ProxySettingsException(e);
		}
		
	}

	public void setIgnoredHostList(String ignoredHostsString) {
		final String SEPARATORS = ";, ";
		StringTokenizer tokens = new StringTokenizer(ignoredHostsString, SEPARATORS);
		while (tokens.hasMoreTokens()) {
			String token = StringUtils.trim(tokens.nextToken());
			String regex = ParsingUtils.wildcardToRegex(token).replace("$", "").concat("(:[0-9]{1,5})?(/(.+))*");
			ignoredHosts.add(regex);
		}
	}

	
	public HttpHost getHost() {
		return host;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public Set<String> getIgnoredHosts() {
		return ignoredHosts;
	}
	
}
