package org.books.business.catalog.security;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityHelper {

	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String MAC_ALGORITHM = "HmacSHA256";
	private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	public static String getTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	public static String getSignature(String secretKey, String operation, String timestamp) {
		try {
			Mac mac = Mac.getInstance(MAC_ALGORITHM);
			SecretKey key = new SecretKeySpec(secretKey.getBytes(), MAC_ALGORITHM);
			mac.init(key);
			byte[] data = mac.doFinal((operation + timestamp).getBytes());
			return encodeBase64(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String encodeBase64(byte[] data) {
		String encoded = "";
		int padding = (3 - data.length % 3) % 3;
		byte[] padded = new byte[data.length + padding];
		System.arraycopy(data, 0, padded, 0, data.length);
		byte[] buffer = new byte[3];
		for (int i = 0; i < padded.length; i += 3) {
			System.arraycopy(padded, i, buffer, 0, 3);
			int n = new BigInteger(1, buffer).intValue();
			for (int j = 3; j >= 0; j--) {
				int k = (n >> (6 * j)) & 0x3f;
				encoded += BASE64_CHARS.charAt(k);
			}
		}
		return encoded.substring(0, encoded.length() - padding) + "==".substring(0, padding);
	}
}