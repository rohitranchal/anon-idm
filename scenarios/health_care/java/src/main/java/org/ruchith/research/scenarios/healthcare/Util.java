package org.ruchith.research.scenarios.healthcare;

import java.security.cert.Certificate;
import java.security.spec.KeySpec;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

public class Util {
	/**
	 * 
	 * @param Base64 encoded plainText
	 * @param Base64 encoded encryptionKey
	 * @return Base64 encoded cipherText
	 * @throws Exception
	 */
	public static String encrypt(String plainText, String sessionKey) throws Exception {
		  /* Derive the key, given password and salt. */
		  /* Salt generation */
		  byte[] salt = new byte[16];
		  SecureRandom randGen = new SecureRandom();
		  randGen.nextBytes(salt);
		  
		  /* password-based encryption mechanism derives key */
		  SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		  String decodedSessionKey = new String(Base64.decode(sessionKey));
		  KeySpec spec = new PBEKeySpec(decodedSessionKey.toCharArray(), salt, 65536, 256);
		  SecretKey tmp = factory.generateSecret(spec);
		  SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		  
		  /* Encrypt the message. */
		  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		  cipher.init(Cipher.ENCRYPT_MODE, secret);
		  AlgorithmParameters params = cipher.getParameters();
		  byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		  byte[] ciphertext = cipher.doFinal(plainText.getBytes());
		  
		  /* combine salt, iv and encrypted message */
		  byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
		  System.arraycopy(salt, 0, combined, 0, salt.length);
		  System.arraycopy(iv, 0, combined, salt.length, iv.length);
		  System.arraycopy(ciphertext, 0, combined, iv.length + salt.length, ciphertext.length);
		  
		  System.out.println("Check salt:" + new String(salt));
		  System.out.println("Check IV:" + new String(iv));
		  
		  return new String(Base64.encode(combined), "UTF-8");
	}
	
	/**
	 * 
	 * @param base64 encoded cipherText
	 * @param base64 encoded encryptionKey
	 * @return base64 encoded Deciphered Text
	 * @throws Exception
	 */
	public static String decrypt(String cipherText, String sessionKey) throws Exception{
		  // separate salt, iv and ciphertext
		  byte[] decoded_ciphertext = Base64.decode(cipherText);
		  byte[] salt = new byte[16];
		  byte[] iv = new byte[16];
		  System.arraycopy(decoded_ciphertext, 0, salt, 0, salt.length);
		  System.arraycopy(decoded_ciphertext, salt.length, iv, 0, iv.length);
	    
		  System.out.println("Check salt: " + new String(salt));
		  System.out.println("Check IV:" + new String(iv));
	    
		  byte[] target_ciphertext = new byte[decoded_ciphertext.length - salt.length - iv.length];
		  System.arraycopy(decoded_ciphertext, salt.length + iv.length, target_ciphertext, 0, target_ciphertext.length);
	    
		  /* password-based encryption mechanism derives key */
		  SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		  String decodedSessionKey = new String(Base64.decode(sessionKey));
		  KeySpec spec = new PBEKeySpec(decodedSessionKey.toCharArray(), salt, 65536, 256);
		  SecretKey tmp = factory.generateSecret(spec);
		  SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		  
		  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		  cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
		  return new String(cipher.doFinal(target_ciphertext),"UTF-8");
	}	
	
	public static String createB64Dgst(String content) throws Exception {
		// Create digest
		byte[] contentBytes = content.getBytes();
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		dgst.update(contentBytes);
		byte[] sha512Dgst = dgst.digest();
		return new String(Base64.encode(sha512Dgst));
	}
	
	public static String createB64Sig(String content, PrivateKey privKey) throws Exception {
		// Sign claim definition
		byte[] contentBytes = content.getBytes();
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(privKey);
		sig.update(contentBytes);
		byte[] sigBytes = sig.sign();
		return new String(Base64.encode(sigBytes));	
	}
	
	public static boolean verifyB64Dgst(String content, String recDgst) throws Exception {
		String recomputed = createB64Dgst(content);
		if(recomputed.equals(recDgst)) {
			return true;
		}
		else {
			return false;
		} 
	}
	
	public static boolean verifyB64Sig(String content, Certificate cert, String recvSig) throws Exception {
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initVerify(cert);
		sig.update(content.getBytes());
		if(sig.verify(Base64.decode(recvSig))) {
			return true;
		}
		else {
			return false;
		}
	}
}
