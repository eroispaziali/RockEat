package it.rockeat.http;

import it.rockeat.exception.ConnectionException;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class HttpUtils {
	
	public static final String USER_AGENT_STRING = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.X.Y.Z Safari/525.13";
	private final static Integer MaxRedirects = 10;
	private final static Boolean AllowCircularRedirects = Boolean.FALSE;
	private final static Integer httpConnectionTimeout = 10000;
	private final static Integer httpSocketTimeout = 10000;
	
	private static HttpParams createHttpParams() {
		HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setUserAgent(params, USER_AGENT_STRING);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
	    params.setIntParameter(ClientPNames.MAX_REDIRECTS, MaxRedirects);
	    params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, AllowCircularRedirects);
	    params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, httpSocketTimeout);
	    params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, httpConnectionTimeout);
	    
	    return params;
	}
	
	private static SchemeRegistry getSupportedSchemes() {
		SchemeRegistry registry = new SchemeRegistry();
		Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
		registry.register(http);
		SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
		Scheme https = new Scheme("https", 443, sf);
		registry.register(https);
		return registry;
	}
	
	public static HttpClient createClient() {
        DefaultHttpClient httpclient = new DefaultHttpClient(createHttpParams());
        httpclient.addRequestInterceptor(new GzipHttpRequestInterceptor());
        httpclient.addResponseInterceptor(new GzipHttpResponseInterceptor());
        return httpclient;
	}
	
	public static HttpClient createClient(HttpHost proxy, Credentials credentials) {
		ThreadSafeClientConnManager tsccm = new ThreadSafeClientConnManager(getSupportedSchemes());
        DefaultHttpClient client = new DefaultHttpClient(tsccm,createHttpParams());
		client.getCredentialsProvider().setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), credentials);
		client.setRoutePlanner(new ProxyHttpRoutePlanner(proxy));
		System.getProperties().put("http.proxySet", "true");
		System.getProperties().put("http.proxyHost", proxy.getHostName());
		System.getProperties().put("http.proxyPort", proxy.getPort());
		System.getProperties().put("http.proxyUser", credentials.getUserPrincipal());
		System.getProperties().put("http.proxyPassword", credentials.getPassword());
		return client;
	}
	
	public static InputStream httpGet(String url) throws ConnectionException {
		try {
			HttpGet httpget = new HttpGet(url);
			HttpClient httpClient = HttpUtils.createClient();
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity responseEntity = response.getEntity();
			return responseEntity.getContent();
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
	
}
