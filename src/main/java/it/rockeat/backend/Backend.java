package it.rockeat.backend;

import it.rockeat.backend.model.Download;
import it.rockeat.backend.model.KeyPair;
import it.rockeat.backend.model.KeyPairHolder;
import it.rockeat.bean.Track;
import it.rockeat.exception.BackendException;
import it.rockeat.util.ParsingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Backend {
	
	private static final String ENDPOINT = "https://api.parse.com/1/";
	private static final String APPLICATION_ID = "SiPhSW3pVPd5k8TrHuASQFIZEczKukZBjHD569gn";
	private static final String CLIENT_KEY = "oztW8NntGlAkwgoIyMiVwrth1VBa6w8tpqFsNYNx";
	private HttpClient httpClient;
	
	public Backend(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	private InputStream doParseGet(String query) throws IllegalStateException, IOException {
		HttpGet request = new HttpGet(ENDPOINT + query);
		request.setHeader("Content-Type", "application/json");
		request.setHeader("X-Parse-Application-Id", APPLICATION_ID);
		request.setHeader("X-Parse-REST-API-Key", CLIENT_KEY);
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		return responseEntity.getContent();		
	}
	
	private InputStream doParseStore(String entity, Object item) throws IllegalStateException, IOException {
		HttpPost request = new HttpPost(ENDPOINT + entity);		
		request.setHeader("Content-Type", "application/json");
		request.setHeader("X-Parse-Application-Id", APPLICATION_ID);
		request.setHeader("X-Parse-REST-API-Key", CLIENT_KEY);
		Gson gson = new Gson();
		String json = gson.toJson(item);
		HttpEntity httpContent = new StringEntity(json);
		request.setEntity(httpContent);
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		return responseEntity.getContent();		
	}
	
	public Map<String,String> retrieveKnownKeyPairs() throws BackendException {
		try {
			String json = ParsingUtils.streamToString(doParseGet("classes/" + KeyPair.REMOTE_CLASSNAME));
			Gson gson = new Gson();
			KeyPairHolder holder = gson.fromJson(json, KeyPairHolder.class);
			Map<String,String> secretMap = new HashMap<String,String>();
			KeyPair[] results = holder.getResults();
			for (int i=0;i<results.length;i++) {
				secretMap.put(results[i].getMd5(), results[i].getSecret());
			}
			return secretMap;
		} catch (JsonSyntaxException e) {
			throw new BackendException("Risposta inattesa dal backend", e);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}
	
	@SuppressWarnings("unused")
	public void storeKeyPair(String md5, String secret) throws BackendException {
		try {
			KeyPair keyPair = new KeyPair(md5, secret);
			String json = ParsingUtils.streamToString(doParseStore("classes/" + KeyPair.REMOTE_CLASSNAME, keyPair));
			Gson gson = new Gson();
			StoreResponse response = gson.fromJson(json, StoreResponse.class);
		} catch (JsonSyntaxException e) {
			throw new BackendException("Risposta inattesa dal backend", e);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}
	
	@SuppressWarnings("unused")
	public void trackDownload(Track track, Long bytes) throws BackendException {
		try {
			Download download = new Download();
			download.setTitle(track.getTitle());
			download.setArtist(track.getAuthor());
			download.setUrl(track.getUrl());
			download.setSize(bytes);
			String json = ParsingUtils.streamToString(doParseStore("classes/" + Download.REMOTE_CLASSNAME, download));
			Gson gson = new Gson();
			StoreResponse response = gson.fromJson(json, StoreResponse.class);
		} catch (JsonSyntaxException e) {
			throw new BackendException("Risposta inattesa dal backend", e);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}

}
