package it.rockeat.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpClientFactory {
	
	private final static String  httpUserAgent = "Mozilla/5.0 (X11; Linux i686; rv:9.0.1) Gecko/20100101 Firefox/9.0.1";
	private final static Integer httpMaxRedirects = 10;
	private final static Boolean httpAllowCircularRedirects = Boolean.FALSE;
	private final static Integer httpConnectionTimeout = 10000;
	private final static Integer httpSocketTimeout = 10000;
	
	private static HttpParams createHttpParams() {
		HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setUserAgent(params, httpUserAgent);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
	    params.setIntParameter(ClientPNames.MAX_REDIRECTS, httpMaxRedirects);
	    params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, httpAllowCircularRedirects);
	    params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, httpSocketTimeout);
	    params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, httpConnectionTimeout);
	    return params;
	}
	
	private static SchemeRegistry registerSupportedSchemes() {
		SchemeRegistry registry = new SchemeRegistry();
		Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
		registry.register(http);
		SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
		Scheme https = new Scheme("https", 443, sf);
		registry.register(https);
		return registry;
	}
	
	public static DefaultHttpClient createInstance() {
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(createHttpParams());
        return defaultHttpClient;
	}
	
	public static HttpClient createInstance(HttpHost proxy, Credentials credentials) {
		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(registerSupportedSchemes());
        DefaultHttpClient client = new DefaultHttpClient(connectionManager, createHttpParams());
		client.getCredentialsProvider().setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), credentials);
		System.getProperties().put("http.proxySet", "true");
		System.getProperties().put("http.proxyHost", proxy.getHostName());
		System.getProperties().put("http.proxyPort", proxy.getPort());
		System.getProperties().put("http.proxyUser", credentials.getUserPrincipal());
		System.getProperties().put("http.proxyPassword", credentials.getPassword());
		return client;
	}
	
}
