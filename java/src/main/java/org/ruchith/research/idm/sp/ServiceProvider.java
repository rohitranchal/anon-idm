package org.ruchith.research.idm.sp;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class ServiceProvider {
	
	
	/**
	 * Create a session key, encrypt it with the given request and claimDef.
	 * Return a serialized JSON node with the session key and challenge.
	 * 
	 * @param request Request from user.
	 * @param claimDef Claim definition to use
	 * @return 
	 * @throws Exception
	 */
	public String createChallange(String request, String claimDef) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode on = (ObjectNode) mapper.readTree(request);
		
		
		return "test";
	}

}
