package it.rockeat;

import it.rockeat.backend.Backend;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.UnknownPlayerException;
import it.rockeat.model.Settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

public class SettingsManager {
	
	public static final String ROCKEAT_VERSION = "0.2";
	private static final String FILENAME = ".rockeat.xml";
	private Settings settings = new Settings();
	private Backend backend;
	
	public SettingsManager(HttpClient httpClient) {
		backend = new Backend(httpClient);
		loadFromFile();
	}
	
	public void firstTimeInitialize() {
		try {
			Map<String, String> keypairs = backend.retrieveKeypairs();
			settings.setKeypairs(keypairs);
		} catch (BackendException e) { /* ignore */ }
		settings.setUid(RandomStringUtils.randomAlphanumeric(16));
		saveToFile();
	}
	
	public void loadFromFile() {
		try {
			FileReader reader = new FileReader(FILENAME);
			settings = (Settings)Unmarshaller.unmarshal(Settings.class, reader);
		} catch (FileNotFoundException e) {
			firstTimeInitialize();
		} catch (MarshalException e) {
			firstTimeInitialize();
		} catch (ValidationException e) {
			firstTimeInitialize();
		}
		
	}
	
	public void saveToFile() {
	    try {
	    	FileWriter writer = new FileWriter(FILENAME);
	    	settings.setLastUpdated(Calendar.getInstance().getTime());
			Marshaller.marshal(settings, writer);
		} catch (MarshalException e) {
			/* silently ignore */
		} catch (ValidationException e) {
			/* silently ignore */
		} catch (IOException e) {
			/* silently ignore */
		}
	}
	
	public String findKey(String md5) throws UnknownPlayerException, BackendException {
		Map<String, String> keypairs = settings.getKeypairs();
    	if (keypairs.containsKey(md5)) {
    		return keypairs.get(md5);
    	} else {
    		String key = backend.findKeypair(md5);
    		keypairs.put(md5, key);
    		saveToFile();
    		return key;
    	}
	}
	
	public void addNewKey(String md5, String key) {
		try {
			Map<String, String> keyPairs = getSettings().getKeypairs();
			backend.storeKeypair(md5, key);
	        keyPairs.put(md5,key);
	        saveToFile();
		} catch (BackendException e) {
			/* silently ignore */
		}		
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

}
