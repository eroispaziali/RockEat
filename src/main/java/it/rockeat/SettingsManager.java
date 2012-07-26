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
import org.apache.http.HttpHost;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingsManager {
	
	public static final String ROCKEAT_VERSION = "0.2.1";
	public static final String ROCKEAT_DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11";
	private static final String FILENAME = ".rockeat.xml";
	private Settings settings = new Settings();
	
	@Inject private Backend backend;
	
	public SettingsManager() {
		loadFromFile();
	}
	
	public void firstTimeInitialize() {
		settings.setProxyEnabled(false);
		settings.setProxyHost(new HttpHost("",8080));
		settings.setProxyUsername("");
		settings.setProxyPassword("");
		settings.setUid(RandomStringUtils.randomAlphanumeric(16));
		settings.setUserAgent(ROCKEAT_DEFAULT_USER_AGENT);
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
	
	public String getUserAgent() {
		return settings.getUserAgent();
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
