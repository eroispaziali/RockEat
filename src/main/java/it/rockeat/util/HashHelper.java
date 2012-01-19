package it.rockeat.util;

import java.security.MessageDigest;

public class HashHelper {

	public static String toHex(byte[] bytes) {
		String hex = "0123456789abcdef";
		String result = "";

		for (byte byteData : bytes) {
			result += "" + hex.charAt((byteData >> 4) & 0xf)
			+ hex.charAt(byteData & 0xf);
		}
		return result;
	}

	
	public static String hash(String string) {
		if (string == null) {
			return null;
		}
		try {
			MessageDigest sha = MessageDigest.getInstance("MD5");
			sha.update(string.getBytes());
			return toHex(sha.digest());
		} catch (Exception e) {
			return "";
		}
	}

}
