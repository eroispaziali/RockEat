package it.rockeat;

import it.rockeat.backend.Backend;
import it.rockeat.exception.BackendException;
import it.rockeat.model.Settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

public class SettingsManager {
	
	private static final String FILENAME = "rockeat.xml";
	private Settings settings = new Settings();
	private Backend backend;
	
	public SettingsManager(HttpClient httpClient) {
		backend = new Backend(httpClient);
		loadFromFile();
	}
	
	public void loadFromBackend() {
		try {
			Map<String, String> keypairs = backend.retrieveKeypairs();
			settings.setKeypairs(keypairs);
		} catch (BackendException e) {
			/* ignore */
			e.printStackTrace();
		}
		saveToFile();
	}
	
	public void loadFromFile() {
		try {
			FileReader reader = new FileReader(FILENAME);
			settings = (Settings)Unmarshaller.unmarshal(Settings.class, reader);
		} catch (FileNotFoundException e) {
			loadFromBackend();
		} catch (MarshalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void saveToFile() {
	    try {
	    	FileWriter writer = new FileWriter(FILENAME);
			Marshaller.marshal(settings, writer);
		} catch (MarshalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addNewKnownPlayer(String md5, String key) {
		try {
			Map<String, String> keyPairs = getSettings().getKeypairs();
			backend.storeKeyPair(md5, key);
	        keyPairs.put(md5, key);
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
