package org.ruchith.research.idm.sp;

import it.unisa.dia.gas.jpbc.Element;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherText;
import org.ruchith.ae.base.Encrypt;
import org.ruchith.ae.base.TextEncoder;
import org.ruchith.research.idm.IdentityClaimDefinition;

public class ServiceProvider {

	/**
	 * Create a session key, encrypt it with the given request and claimDef. Return a serialized JSON node with the
	 * session key and challenge.
	 * 
	 * @param request
	 *            Request from user: Base64 encoded bytes of the request element.
	 * @param claimDef
	 *            Claim definition to use: JSON encoded
	 * @return
	 * @throws Exception
	 */
	public String createChallange(String request, String claimDef, String sessionKey) throws Exception {


		ObjectMapper mapper = new ObjectMapper();
		ObjectNode claimDefOn = (ObjectNode) mapper.readTree(claimDef);
		IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
		
		byte[] reqElemBytes = Base64.decode(request);
		Element reqElem = idClaimDef.getParams().getPairing().getG1().newElement();
		reqElem.setFromBytes(reqElemBytes);

		// Create AES key
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] k = new byte[128 / 8];
		sr.nextBytes(k);
		SecretKeySpec spec = new SecretKeySpec(k, "AES");
		byte[] iv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		IvParameterSpec ivs = new IvParameterSpec(iv);
		Cipher cps = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cps.init(Cipher.ENCRYPT_MODE, spec, ivs);
		byte[] encryptedSessionKeyBytes = cps.doFinal(sessionKey.getBytes());

		// Encrypt session key
		TextEncoder encoder = new TextEncoder();
		encoder.init(idClaimDef.getParams());
		Element[] encoded = encoder.encode(new String(Base64.encode(k)));

		Encrypt encrypt = new Encrypt();
		encrypt.init(idClaimDef.getParams());
		AECipherText ct = encrypt.doEncrypt(encoded, reqElem);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		on.put("EncryptedKey", ct.serializeJSON());
		on.put("EncryptedSessionKey", new String(Base64.encode(encryptedSessionKeyBytes)));

		return on.toString();

	}

}
