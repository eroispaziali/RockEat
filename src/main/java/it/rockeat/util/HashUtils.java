package it.rockeat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
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
			return null;
		}
	}
	
	public static String md5(InputStream is) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[8192];
			int read = 0;
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			return output;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String md5(File file) {
		try {
			return (HashUtils.md5(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
}
