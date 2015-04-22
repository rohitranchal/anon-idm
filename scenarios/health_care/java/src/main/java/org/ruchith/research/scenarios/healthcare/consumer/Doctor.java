package org.ruchith.research.scenarios.healthcare.consumer;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
		String claimdef = super.getClaimdef(claimDefName);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root_node = mapper.readTree(claimdef);
		String params = root_node.get("params").toString();
		System.out.println("params: " + params);
		
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
}
