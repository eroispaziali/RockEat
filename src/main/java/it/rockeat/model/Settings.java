package it.rockeat.model;

import java.util.HashMap;
import java.util.Map;

public class Settings {
	
	private String uid;
	private User user;
	private Map<String,String> keypairs = new HashMap<String, String>();
	
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
	
	

}