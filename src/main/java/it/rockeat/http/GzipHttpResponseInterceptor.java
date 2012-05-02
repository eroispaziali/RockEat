package it.rockeat.http;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;

public class GzipHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
	public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        Header ceheader = entity.getContentEncoding();
        if (ceheader != null) {
            HeaderElement[] codecs = ceheader.getElements();
            for (int i = 0; i < codecs.length; i++) {
                if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                    response.setEntity(
                            new GzipDecompressingEntity(response.getEntity()));
                    return;
                }
            }
        }
    }

}
