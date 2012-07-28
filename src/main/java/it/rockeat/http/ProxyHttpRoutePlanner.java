package it.rockeat.http;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpContext;

public class ProxyHttpRoutePlanner implements HttpRoutePlanner {

	private HttpHost proxy;
	private Set<String> ignoredHosts = new HashSet<String>();

	public ProxyHttpRoutePlanner(HttpHost proxy) {
		this.proxy = proxy;
	}

	private boolean shouldBypassProxy(String url) {
		if (url == null) {
			return false;
		}
		if (CollectionUtils.isNotEmpty(ignoredHosts)) {
			for (String regexPattern : ignoredHosts) {
				try {
					if (url.matches(regexPattern)) {
						return true;
					}
				} catch (PatternSyntaxException e) { /*
													 * invalid expression, just
													 * ignore
													 */
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public HttpRoute determineRoute(HttpHost httpHost, HttpRequest httpRequest,
			HttpContext httpContext) throws HttpException {
		String uri = httpRequest.getRequestLine().getUri();
		Boolean secure = (httpHost.getSchemeName().equals("https")) ? true
				: false;
		if (proxy != null || shouldBypassProxy(uri)) {
			return new HttpRoute(httpHost, null, proxy, secure);
		} else {
			return new HttpRoute(httpHost, null, secure);
		}
	}

}
