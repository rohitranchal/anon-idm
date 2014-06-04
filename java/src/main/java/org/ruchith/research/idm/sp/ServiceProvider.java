package org.ruchith.research.idm.sp;

import it.unisa.dia.gas.jpbc.Element;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherTextBlock;
import org.ruchith.ae.base.Encrypt;
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
	public String createChallange(String request, String claimDef) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode claimDefOn = (ObjectNode) mapper.readTree(claimDef);
		IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
		
		byte[] reqElemBytes = Base64.decode(request);
		Element reqElem = idClaimDef.getParams().getPairing().getG1().newElement();
		reqElem.setFromBytes(reqElemBytes);

		Element sessionKey = idClaimDef.getParams().getPairing().getGT().newRandomElement().getImmutable();

		// Encrypt session key
		Encrypt encrypt = new Encrypt();
		encrypt.init(idClaimDef.getParams());
		AECipherTextBlock ct = encrypt.doEncrypt(sessionKey, reqElem);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		on.put("EncryptedKey", ct.serializeJSON());
		on.put("SessionKey", new String(Base64.encode(sessionKey.toBytes())));

		return on.toString();

	}

}
