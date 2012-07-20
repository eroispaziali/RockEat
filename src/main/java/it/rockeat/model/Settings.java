package it.rockeat.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Settings {
	
	private String uid;
	private User user;
	private Map<String,String> keypairs = new HashMap<String, String>();
	private String userAgent;
	private Date lastUpdated;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Map<String, String> getKeypairs() {
		return keypairs;
	}
	public void setKeypairs(Map<String, String> keypairs) {
		this.keypairs = keypairs;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	

}
