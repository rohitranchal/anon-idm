package org.ruchith.research.scenarios.healthcare.consumer;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.research.idm.user.Client;


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
}
