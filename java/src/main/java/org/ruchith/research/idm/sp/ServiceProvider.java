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

		on.put(idClaimDef.getName(), ct.serializeJSON());
		String sk = new String(Base64.encode(sessionKey.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);

		return on.toString();

	}

	/**
	 * Challenge for the case claim1 AND claim2
	 * @param request
	 * @param claimDef1
	 * @param claimDef2
	 * @return
	 * @throws Exception
	 */
	public String createChallangeTwoClaims(String req1, String req2, String claimDef1, String claimDef2) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode claimDef1On = (ObjectNode) mapper.readTree(claimDef1);
		IdentityClaimDefinition idClaimDef1 = new IdentityClaimDefinition(claimDef1On);
		
		ObjectNode claimDef2On = (ObjectNode) mapper.readTree(claimDef2);
		IdentityClaimDefinition idClaimDef2 = new IdentityClaimDefinition(claimDef2On);
		
		byte[] req1ElemBytes = Base64.decode(req1);
		Element req1Elem = idClaimDef1.getParams().getPairing().getG1().newElement();
		req1Elem.setFromBytes(req1ElemBytes);
		
		byte[] req2ElemBytes = Base64.decode(req2);
		Element req2Elem = idClaimDef2.getParams().getPairing().getG1().newElement();
		req2Elem.setFromBytes(req2ElemBytes);

		
		Element sessionKey = idClaimDef1.getParams().getPairing().getGT().newRandomElement().getImmutable();
		
		Element split = idClaimDef1.getParams().getPairing().getGT().newRandomElement().getImmutable();
		Element part = sessionKey.sub(split);
		
		
		// Encrypt session key
		Encrypt encrypt = new Encrypt();
		encrypt.init(idClaimDef1.getParams());
		AECipherTextBlock ct1 = encrypt.doEncrypt(split, req1Elem);
		
		encrypt.init(idClaimDef2.getParams());
		AECipherTextBlock ct2 = encrypt.doEncrypt(part, req2Elem);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		on.put(idClaimDef1.getName(), ct1.serializeJSON());
		on.put(idClaimDef2.getName(), ct2.serializeJSON());
		String sk = new String(Base64.encode(sessionKey.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);

		return on.toString();

	}
	
	
}
