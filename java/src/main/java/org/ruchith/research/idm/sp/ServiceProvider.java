package org.ruchith.research.idm.sp;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherTextBlock;
import org.ruchith.ae.base.AEParameters;
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
	 * 
	 * @param request
	 * @param claimDef1
	 * @param claimDef2
	 * @return
	 * @throws Exception
	 */
	public String createChallangeTwoClaims(String req1, String req2, String claimDef1, String claimDef2)
			throws Exception {

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

	/**
	 * 
	 * @param request
	 * @param claimDefs
	 *            Array of claim defs
	 * @return
	 * @throws Exception
	 */
	public String createChallangeNClaims(String requests, String claimDefs) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		// System.out.println(requests);
		String[] split = requests.split(",");
		ArrayNode claimDefNodes = (ArrayNode) mapper.readTree(claimDefs);

		ArrayList<IdentityClaimDefinition> icds = new ArrayList<IdentityClaimDefinition>();
		ArrayList<Element> reqs = new ArrayList<Element>();

		for (int i = 0; i < split.length; i++) {
			String onVal = claimDefNodes.get(i).getTextValue();
			ObjectNode claimDefOn = (ObjectNode) mapper.readTree(onVal);
			IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
			icds.add(idClaimDef);

			Pairing pairing = idClaimDef.getParams().getPairing();
			// System.out.println(idClaimDef.serializeJSON());
			String tmpReq = split[i].replaceAll("\"", "");

			byte[] reqElemBytes = Base64.decode(tmpReq);
			// System.out.println(reqElemBytes.length);
			Element reqElem = pairing.getG1().newElement();
			reqElem.setFromBytes(reqElemBytes);
			// System.out.println(reqElem.getImmutable());

			reqs.add(reqElem);
		}

		Pairing pairing = icds.get(0).getParams().getPairing();
		Field gt = pairing.getGT();
		Element sessionKey = gt.newRandomElement().getImmutable();
		Element sessionKeyOrig = sessionKey.getImmutable();
		// System.out.println("Key: " + sessionKey);

		Encrypt encrypt = new Encrypt();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		for (int i = 0; i < split.length; i++) {
			IdentityClaimDefinition claimDef = icds.get(i);

			Element share = null;
			if (i < (split.length - 1)) {
				share = gt.newRandomElement().getImmutable();
				sessionKey = sessionKey.sub(share).getImmutable();
			} else {
				// Last one should be the remaining part of session key
				share = sessionKey;
			}

			encrypt.init(claimDef.getParams());
			// System.out.println("Part : " + i + " : " + share);
			AECipherTextBlock ct = encrypt.doEncrypt(share, reqs.get(i));

			on.put(claimDef.getName(), ct.serializeJSON());
		}

		String sk = new String(Base64.encode(sessionKeyOrig.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);
		return on.toString();
	}

	/**
	 * 
	 * @param request
	 * @param claimDefs
	 *            Array of claim defs
	 * @return
	 * @throws Exception
	 */
	public String createChallangeNClaimsThreads(String requests, String claimDefs) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		// System.out.println(requests);
		String[] split = requests.split(",");
		ArrayNode claimDefNodes = (ArrayNode) mapper.readTree(claimDefs);

		ArrayList<IdentityClaimDefinition> icds = new ArrayList<IdentityClaimDefinition>();
		ArrayList<Element> reqs = new ArrayList<Element>();

		for (int i = 0; i < split.length; i++) {
			String onVal = claimDefNodes.get(i).getTextValue();
			ObjectNode claimDefOn = (ObjectNode) mapper.readTree(onVal);
			IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
			icds.add(idClaimDef);

			Pairing pairing = idClaimDef.getParams().getPairing();
			// System.out.println(idClaimDef.serializeJSON());
			String tmpReq = split[i].replaceAll("\"", "");

			byte[] reqElemBytes = Base64.decode(tmpReq);
			// System.out.println(reqElemBytes.length);
			Element reqElem = pairing.getG1().newElement();
			reqElem.setFromBytes(reqElemBytes);
			// System.out.println(reqElem.getImmutable());

			reqs.add(reqElem);
		}

		Pairing pairing = icds.get(0).getParams().getPairing();
		Field gt = pairing.getGT();
		Element sessionKey = gt.newRandomElement().getImmutable();
		Element sessionKeyOrig = sessionKey.getImmutable();
		// System.out.println("Key: " + sessionKey);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		ArrayList<EncrypterThread> ets = new ArrayList<ServiceProvider.EncrypterThread>();

		for (int i = 0; i < split.length; i++) {
			IdentityClaimDefinition claimDef = icds.get(i);

			Element share = null;
			if (i < (split.length - 1)) {
				share = gt.newRandomElement().getImmutable();
				sessionKey = sessionKey.sub(share).getImmutable();
			} else {
				// Last one should be the remaining part of session key
				share = sessionKey;
			}

			EncrypterThread t = new EncrypterThread(claimDef.getName(), claimDef.getParams(), share, reqs.get(i), on);
			t.start();
			ets.add(t);
		}

		for (EncrypterThread t : ets) {
			t.join();
		}

		String sk = new String(Base64.encode(sessionKeyOrig.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);
		return on.toString();
	}

	
	public String createChallangeNAClaims(String req, String claimDefs, int size) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode claimDefNodes = (ArrayNode) mapper.readTree(claimDefs);

		req = req.replaceAll("\"", "");
		byte[] reqElemBytes = Base64.decode(req);

		Element reqElem = null;
		ArrayList<IdentityClaimDefinition> icds = new ArrayList<IdentityClaimDefinition>();

		for (int i = 0; i < size; i++) {
			String onVal = claimDefNodes.get(i).getTextValue();

			ObjectNode claimDefOn = (ObjectNode) mapper.readTree(onVal);
			IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
			icds.add(idClaimDef);

			if (reqElem == null) {
				Pairing pairing = idClaimDef.getParams().getPairing();
				reqElem = pairing.getG1().newElement();
				reqElem.setFromBytes(reqElemBytes);
//				System.out.println(reqElem);
			}

		}

		Pairing pairing = icds.get(0).getParams().getPairing();
		Field gt = pairing.getGT();
		Element sessionKey = gt.newRandomElement().getImmutable();
		Element sessionKeyOrig = sessionKey.getImmutable();
		// System.out.println("Key: " + sessionKey);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;
		Encrypt encrypt = new Encrypt();
		

		for (int i = 0; i < size; i++) {
			IdentityClaimDefinition claimDef = icds.get(i);

			Element share = null;
			if (i < (size - 1)) {
				share = gt.newRandomElement().getImmutable();
				sessionKey = sessionKey.sub(share).getImmutable();
			} else {
				// Last one should be the remaining part of session key
				share = sessionKey;
			}
			
			
			encrypt.init(claimDef.getParams());
			// System.out.println("Part : " + i + " : " + share);
			AECipherTextBlock ct = encrypt.doEncrypt(share, reqElem);

			on.put(claimDef.getName(), ct.serializeJSON());
		}


//		System.out.println(sessionKeyOrig);
		String sk = new String(Base64.encode(sessionKeyOrig.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);
		return on.toString();
	}

	
	public String createChallangeNAClaimsThreads(String req, String claimDefs, int size) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode claimDefNodes = (ArrayNode) mapper.readTree(claimDefs);

		req = req.replaceAll("\"", "");
		byte[] reqElemBytes = Base64.decode(req);

		Element reqElem = null;
		ArrayList<IdentityClaimDefinition> icds = new ArrayList<IdentityClaimDefinition>();

		for (int i = 0; i < size; i++) {
			String onVal = claimDefNodes.get(i).getTextValue();

			ObjectNode claimDefOn = (ObjectNode) mapper.readTree(onVal);
			IdentityClaimDefinition idClaimDef = new IdentityClaimDefinition(claimDefOn);
			icds.add(idClaimDef);

			if (reqElem == null) {
				Pairing pairing = idClaimDef.getParams().getPairing();
				reqElem = pairing.getG1().newElement();
				reqElem.setFromBytes(reqElemBytes);
//				System.out.println(reqElem);
			}

		}

		Pairing pairing = icds.get(0).getParams().getPairing();
		Field gt = pairing.getGT();
		Element sessionKey = gt.newRandomElement().getImmutable();
		Element sessionKeyOrig = sessionKey.getImmutable();
		// System.out.println("Key: " + sessionKey);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		ArrayList<EncrypterThread> ets = new ArrayList<ServiceProvider.EncrypterThread>();

		for (int i = 0; i < size; i++) {
			IdentityClaimDefinition claimDef = icds.get(i);

			Element share = null;
			if (i < (size - 1)) {
				share = gt.newRandomElement().getImmutable();
				sessionKey = sessionKey.sub(share).getImmutable();
			} else {
				// Last one should be the remaining part of session key
				share = sessionKey;
			}

			EncrypterThread t = new EncrypterThread(claimDef.getName(), claimDef.getParams(), share, reqElem, on);
			t.start();
			ets.add(t);
		}

		for (EncrypterThread t : ets) {
			t.join();
		}

//		System.out.println(sessionKeyOrig);
		String sk = new String(Base64.encode(sessionKeyOrig.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);
		return on.toString();
	}

	class EncrypterThread extends Thread {

		private AEParameters params;
		private Element share;
		private Element req;
		private ObjectNode on;
		private String claimName;

		public EncrypterThread(String claimName, AEParameters params, Element share, Element req, ObjectNode on) {
			this.claimName = claimName;
			this.params = params;
			this.share = share;
			this.req = req;
			this.on = on;
		}

		public void run() {
			Encrypt e = new Encrypt();
			e.init(this.params);
			AECipherTextBlock ct = e.doEncrypt(this.share, this.req);
			this.on.put(this.claimName, ct.serializeJSON());
		}

	}

}
