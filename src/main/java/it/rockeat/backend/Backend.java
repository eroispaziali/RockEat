package it.rockeat.backend;

import it.rockeat.exception.BackendException;
import it.rockeat.exception.UnknownPlayerException;
import it.rockeat.http.ConnectionManager;
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
import com.google.inject.Inject;

public class Backend {
	
	private static final String ENDPOINT = "https://api.parse.com/1/";
	private static final String APPLICATION_ID = "SiPhSW3pVPd5k8TrHuASQFIZEczKukZBjHD569gn";
	private static final String CLIENT_KEY = "oztW8NntGlAkwgoIyMiVwrth1VBa6w8tpqFsNYNx";
	
	@Inject private ConnectionManager connectionManager;
        
	private InputStream doParseGet(String query) throws IllegalStateException, IOException {
		HttpClient httpClient = connectionManager.createClient();
		HttpGet request = new HttpGet(ENDPOINT + query);
		request.setHeader("Content-Type", "application/json");
		request.setHeader("X-Parse-Application-Id", APPLICATION_ID);
		request.setHeader("X-Parse-REST-API-Key", CLIENT_KEY);
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		return responseEntity.getContent();		
	}

	private InputStream doParseStore(String entity, Object item) throws IllegalStateException, IOException {
		HttpClient httpClient = connectionManager.createClient();
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
	
	public String findKeypair(String md5) throws BackendException, UnknownPlayerException {
		Map<String,String> results = retrieveKeypairs();
		if (results.containsKey(md5)) {
			return results.get(md5);
		} else {
			throw new UnknownPlayerException();	
		}
		
	}

	public Map<String,String> retrieveKeypairs() throws BackendException {
		try {
			String json = ParsingUtils.streamToString(doParseGet("classes/" + Keypair.REMOTE_CLASSNAME));
			Gson gson = new Gson();
			KeyPairHolder holder = gson.fromJson(json, KeyPairHolder.class);
			Map<String,String> secretMap = new HashMap<String,String>();
			Keypair[] results = holder.getResults();
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
	
	public String storeKeypair(String md5, String secret) throws BackendException {
		try {
			Keypair keyPair = new Keypair(md5, secret);
			String json = ParsingUtils.streamToString(doParseStore("classes/" + Keypair.REMOTE_CLASSNAME, keyPair));
			Gson gson = new Gson();
			StoreResponse response = gson.fromJson(json, StoreResponse.class);
			return response.getObjectId();
		} catch (JsonSyntaxException e) {
			throw new BackendException("Risposta inattesa dal backend", e);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}
	
	public String log(DownloadActivity activity) throws BackendException {
		try {
			String json = ParsingUtils.streamToString(doParseStore("classes/" + DownloadActivity.REMOTE_CLASSNAME, activity));
			Gson gson = new Gson();
			StoreResponse response = gson.fromJson(json, StoreResponse.class);
			return response.getObjectId();
		} catch (JsonSyntaxException e) {
			throw new BackendException("Risposta inattesa dal backend", e);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}

}
