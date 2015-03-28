package org.ruchith.research.scenarios.healthcare.hie;

// TODO make own class
import it.unisa.dia.gas.jpbc.Element;

import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.idp.Configuration;
import org.ruchith.research.idm.sp.ServiceProvider;
import org.ruchith.research.scenarios.healthcare.hie.db.*;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherTextBlock;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.Encrypt;

/**
 * 
 * @author Byungchan An
 *
 */
public class HealthInformationExchange extends ServiceProvider {
	private Configuration config;
	private Database db;
	
	public HealthInformationExchange(String configPath) throws Exception {
		this.config = Configuration.getInstance(configPath);
		this.db = Database.getInstance(this.config.getDbHost(), this.config.getDbUser(), this.config.getDbPassword());		
	}
	
	/**
	 * Extracting g param
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public String extractG(String params) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode onParam = (ObjectNode) mapper.readTree(Base64.decode(params));
		
		String g = onParam.get("g").asText();
		return g;
	}
	
	/**
	 * Store record pairs which can be found by g param
	 * @param g
	 * @param ownerParam
	 * @param readParam
	 * @param Record
	 * @throws Exception
	 */
	public void storeRecord(String g, String ownerParam, String readParam, String Record)
		throws Exception {
		db.insertRecordPair(g, ownerParam, readParam, Record);
	}
	
	public String createChallangeByParam(String request, String params) throws Exception {
		/*
		AEParameters p = new AEParameters(onParam);
		System.out.println("Entire params:" + new String(Base64.decode(params)));
		if(p.serializeJSON().toString().equals(new String(Base64.decode(params))))
			System.out.println("True!");
		*/

		// create AEParameters from base64 encoded param
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode onParam = (ObjectNode) mapper.readTree(Base64.decode(params));
		AEParameters input_param = new AEParameters(onParam);

		byte[] reqElemBytes = Base64.decode(request);
		Element reqElem = input_param.getPairing().getG1().newElement();
		reqElem.setFromBytes(reqElemBytes);

		Element sessionKey = input_param.getPairing().getGT().newRandomElement().getImmutable();

		// Encrypt session key
		Encrypt encrypt = new Encrypt();
		encrypt.init(input_param);
		AECipherTextBlock ct = encrypt.doEncrypt(sessionKey, reqElem);

		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		// TODO do the different thing
		on.put("Anonymous", ct.serializeJSON());
		String sk = new String(Base64.encode(sessionKey.toBytes()));
		sk = sk.replaceAll(" ", "");
		on.put("SessionKey", sk);

		return on.toString();
	}
}
