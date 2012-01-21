package it.rockeat.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

public class ProxySettings {
	
	private HttpHost host;
	private Credentials credentials;
	
	public ProxySettings(String optionLine) throws MalformedURLException {
		
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
		
		// Setting up host
		URL url = new URL(proxyUrl);
		host = new HttpHost(url.getHost(), url.getPort());
	}

	public HttpHost getHost() {
		return host;
	}

	public Credentials getCredentials() {
		return credentials;
	}
	
}
