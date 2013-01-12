package org.ruchith.research.idm;

import java.security.cert.Certificate;
import java.security.MessageDigest;

/**
 * Utility methods.
 * 
 * @author Ruchith Fernando
 *
 */
public class Util {
	
	/**
	 * Convert a given byte array to a hex.
	 * @param input byte array to be converted.
	 * @return String of hex values 
	 */
	public static String converToHexString(byte[] input) {
		int value;
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hex = new char[input.length * 3];
		for (int i = 0; i < input.length; i++) {
			value = input[i] & 0xFF;
			hex[i * 3] = hexArray[value >>> 4];
			hex[i * 3 + 1] = hexArray[value & 0x0F];
			if (i != input.length - 1) {
				hex[i * 3 + 2] = ':';
			}
		}
		return new String(hex);
	}
	
	/**
	 * Generate SHA1 fingerprint of a given certificate.
	 * @param cert {@link Certificate} instance.
	 * @return SHA1 fingerprint in hex.
	 * @throws Exception
	 */
	public static String getCerificateSHA1Fingerprint(Certificate cert) throws Exception {
		byte[] certBytes = cert.getEncoded();
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(certBytes);
		byte[] dgst = md.digest();
		return converToHexString(dgst);
	}

}
