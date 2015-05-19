package org.ruchith.research.scenarios.healthcare.consumer;

import it.unisa.dia.gas.jpbc.Element;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.user.Client;
import org.ruchith.research.scenarios.healthcare.Util;

/**
 * 
 * @author Byungchan An
 *
 */
public class Doctor extends Client {
	public Doctor(String walletdir) throws Exception {
		super(walletdir);
	}
	
	public String extractG(String claimDefName) throws Exception {
		String params = super.getPublicParams(claimDefName);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode params_node = mapper.readTree(params);
		
		return params_node.get("g").asText();		
	}
	
	public String sessionSig(String content, String storePath, String storePass, String alias, String keyPass) throws Exception {
		// Get user's private key
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream is = new FileInputStream(storePath);
		ks.load(is, storePass.toCharArray());
		PrivateKey key = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
		//Certificate cert = (Certificate) ks.getCertificate(alias);
		
		String sessionSig = Util.createB64Sig(content, key);
		return sessionSig;
	}
	
	public String calculateLookupValue(String claimDefName, String param, String betaString) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
		// get a previous parameter
		String prevParamString = super.getPublicParams(claimDefName);
		JsonNode prevParamNode = mapper.readTree(prevParamString);
		ObjectNode prevParamOn = (ObjectNode) prevParamNode;
		System.out.println("Prev params: " + prevParamOn.toString());
		AEParameters prevParam = new AEParameters(prevParamOn);
		
		// new paramter
		ObjectNode on = (ObjectNode) mapper.readTree(param);
		AEParameters newParam = new AEParameters(on);
		
		// get a beta
		System.out.println("beta input: " + betaString);
		Element beta = newParam.getPairing().getZr().newElement();
		beta.setFromBytes(Base64.decode(betaString));
		System.out.println("beta read: " + new String(Base64.encode(beta.toBytes())));
		
		// randomIDClaim
		Element randomIDClaim = newParam.getPairing().getZr().newElement();
		randomIDClaim.setFromBytes(Base64.decode(super.getRandomIDclaim(claimDefName)));
		System.out.println("Checking correct recovery of random ID Claim: " + new String(Base64.encode(randomIDClaim.toBytes())));
		
		// calculated req value
		Element req = newParam.getH1().powZn(randomIDClaim);
		System.out.println("Prev req:" + new String(Base64.encode(req.toBytes())));
		
		Element h1 = newParam.getH1();
		System.out.println("h1: " + new String(Base64.encode(h1.toBytes())));
		System.out.println("master: " + (super.getRandomIDclaim(claimDefName)));		
		
		// lookup value
		Element lookupValue = req.powZn(beta);
		
		return new String(Base64.encode(lookupValue.toBytes()));
	}
}
