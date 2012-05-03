package it.rockeat.util;

import java.security.MessageDigest;

public class HashUtils {

	private static String toHex(byte[] bytes) {
		String hex = "0123456789abcdef";
		String result = "";

		for (byte byteData : bytes) {
			result += "" + hex.charAt((byteData >> 4) & 0xf)
			+ hex.charAt(byteData & 0xf);
		}
		return result;
	}

	
	public static String md5(String string) {
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
