package org.ruchith.research.scenarios.healthcare.lab;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author Byungchan An
 *
 */
public class Lab {
	/**
	 * Extract public parameter in claim definition
	 * 
	 * @param	claimdef	Claim definition in JSON format
	 * @return	String		String encoded in Base64 for public parameter
	 * @throws	Exception
	 */
	public String extractPublicParams(String claimdef) throws Exception {
		String params = null;
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(claimdef);

			ObjectNode node = (ObjectNode) rootNode;
			params = node.get("params").asText();
			System.out.println("Encoded public params: " + params);
			
			/*			
			ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(params));
			
			System.out.println("Comparison String 1:" + new String(Base64.decode(params)));
			System.out.println("Comparison String 2: " + on.toString());
			String test = new String(Base64.decode(params));
			if(test.equals(on.toString()))
				System.out.println("Equal!");
			*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return params;
	}
}
