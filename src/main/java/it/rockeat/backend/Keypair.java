package it.rockeat.backend;

public class Keypair {

	public final static String REMOTE_CLASSNAME = "KeyPair";

	private String objectId;
	private String md5;
	private String secret;
	private String createdAt;
	private String updatedAt;

	public Keypair() {
		super();
	}

	public Keypair(String md5, String secret) {
		this.md5 = md5;
		this.secret = secret;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

}
